const { pool } = require('../config/database');

/**
 * Récupère la liste de toutes les classes
 * GET /api/classes
 */
async function getAllClasses(req, res, next) {
    try {
        const [classes] = await pool.query(`
            SELECT c.*, ans.libelle AS annee_scolaire,
                   (SELECT COUNT(*) FROM eleves e WHERE e.classe_id = c.id AND e.actif = TRUE) AS effectif_reel
            FROM classes c
            INNER JOIN annees_scolaires ans ON c.annee_scolaire_id = ans.id
            ORDER BY c.nom ASC
        `);

        res.json({
            succes: true,
            total: classes.length,
            classes
        });
    } catch (error) {
        next(error);
    }
}

/**
 * Récupère le détail d'une classe avec élèves, enseignants et matières
 * GET /api/classes/:id
 */
async function getClasseById(req, res, next) {
    try {
        const { id } = req.params;

        const [classes] = await pool.query(`
            SELECT c.*, ans.libelle AS annee_scolaire
            FROM classes c
            INNER JOIN annees_scolaires ans ON c.annee_scolaire_id = ans.id
            WHERE c.id = ? LIMIT 1
        `, [id]);

        if (classes.length === 0) {
            return res.status(404).json({ succes: false, message: 'Classe introuvable.' });
        }

        const classe = classes[0];

        // Liste des élèves de la classe
        const [eleves] = await pool.query(`
            SELECT id, matricule, nom, prenom, sexe, date_naissance, telephone, nom_parent, telephone_parent, actif
            FROM eleves
            WHERE classe_id = ? AND actif = TRUE
            ORDER BY nom ASC, prenom ASC
        `, [id]);

        // Enseignements (Matières & Professeurs affectés)
        const [enseignements] = await pool.query(`
            SELECT ens.id AS enseignement_id, m.id AS matiere_id, m.nom AS nom_matiere, m.code AS code_matiere, m.coefficient,
                   p.id AS professeur_id, p.nom AS nom_prof, p.prenom AS prenom_prof, p.email AS email_prof
            FROM enseignements ens
            INNER JOIN matieres m ON ens.matiere_id = m.id
            INNER JOIN professeurs p ON ens.professeur_id = p.id
            WHERE ens.classe_id = ?
            ORDER BY m.nom ASC
        `, [id]);

        // Emploi du temps de la classe
        const [edt] = await pool.query(`
            SELECT edt.id, edt.jour, edt.heure_debut, edt.heure_fin, edt.salle,
                   m.nom AS nom_matiere, m.couleur_hex, p.nom AS nom_prof, p.prenom AS prenom_prof
            FROM emplois_du_temps edt
            INNER JOIN matieres m ON edt.matiere_id = m.id
            INNER JOIN professeurs p ON edt.professeur_id = p.id
            WHERE edt.classe_id = ?
            ORDER BY FIELD(edt.jour, 'Lundi', 'Mardi', 'Mercredi', 'Jeudi', 'Vendredi', 'Samedi'), edt.heure_debut ASC
        `, [id]);

        res.json({
            succes: true,
            classe: {
                ...classe,
                eleves,
                enseignements,
                emplois_du_temps: edt
            }
        });
    } catch (error) {
        next(error);
    }
}

/**
 * Création d'une classe
 * POST /api/classes
 */
async function createClasse(req, res, next) {
    try {
        const { nom, niveau, frais_scolarite = 0.0, salle_principale, annee_scolaire_id } = req.body;

        if (!nom || !niveau) {
            return res.status(400).json({
                succes: false,
                message: 'Veuillez spécifier le nom et le niveau de la classe.'
            });
        }

        let anneeId = annee_scolaire_id;
        if (!anneeId) {
            const [active] = await pool.query('SELECT id FROM annees_scolaires WHERE active = TRUE LIMIT 1');
            anneeId = active[0]?.id;
        }

        const [result] = await pool.query(`
            INSERT INTO classes (nom, niveau, annee_scolaire_id, frais_scolarite, salle_principale)
            VALUES (?, ?, ?, ?, ?)
        `, [nom, niveau, anneeId, frais_scolarite, salle_principale || null]);

        res.status(201).json({
            succes: true,
            message: 'Classe créée avec succès.',
            classe_id: result.insertId
        });
    } catch (error) {
        next(error);
    }
}

/**
 * Mise à jour d'une classe
 * PUT /api/classes/:id
 */
async function updateClasse(req, res, next) {
    try {
        const { id } = req.params;
        const { nom, niveau, frais_scolarite, salle_principale } = req.body;

        await pool.query(`
            UPDATE classes SET
                nom = COALESCE(?, nom),
                niveau = COALESCE(?, niveau),
                frais_scolarite = COALESCE(?, frais_scolarite),
                salle_principale = COALESCE(?, salle_principale)
            WHERE id = ?
        `, [nom, niveau, frais_scolarite, salle_principale, id]);

        res.json({
            succes: true,
            message: 'Classe mise à jour avec succès.'
        });
    } catch (error) {
        next(error);
    }
}

/**
 * Suppression d'une classe
 * DELETE /api/classes/:id
 */
async function deleteClasse(req, res, next) {
    try {
        const { id } = req.params;

        // Vérification de sécurité métier : la classe ne doit pas contenir d'élèves
        const [eleves] = await pool.query('SELECT COUNT(*) AS total FROM eleves WHERE classe_id = ?', [id]);
        if (eleves[0].total > 0) {
            return res.status(400).json({
                succes: false,
                message: `Impossible de supprimer cette classe : elle contient encore ${eleves[0].total} élève(s). Réaffectez d'abord les élèves.`
            });
        }

        await pool.query('DELETE FROM classes WHERE id = ?', [id]);

        res.json({
            succes: true,
            message: 'Classe supprimée avec succès.'
        });
    } catch (error) {
        next(error);
    }
}

module.exports = {
    getAllClasses,
    getClasseById,
    createClasse,
    updateClasse,
    deleteClasse
};
