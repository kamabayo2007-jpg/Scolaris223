const { pool } = require('../config/database');

/**
 * Récupère l'emploi du temps d'une classe
 * GET /api/emplois-du-temps/classe/:classeId
 */
async function getParClasse(req, res, next) {
    try {
        const { classeId } = req.params;

        const [cours] = await pool.query(`
            SELECT edt.*, m.nom AS nom_matiere, m.code AS code_matiere, m.couleur_hex,
                   p.nom AS nom_prof, p.prenom AS prenom_prof, c.nom AS nom_classe
            FROM emplois_du_temps edt
            INNER JOIN matieres m ON edt.matiere_id = m.id
            INNER JOIN professeurs p ON edt.professeur_id = p.id
            INNER JOIN classes c ON edt.classe_id = c.id
            WHERE edt.classe_id = ?
            ORDER BY FIELD(edt.jour, 'Lundi', 'Mardi', 'Mercredi', 'Jeudi', 'Vendredi', 'Samedi'), edt.heure_debut ASC
        `, [classeId]);

        res.json({
            succes: true,
            classe_id: parseInt(classeId, 10),
            cours
        });
    } catch (error) {
        next(error);
    }
}

/**
 * Récupère l'emploi du temps de l'utilisateur connecté (Professeur ou Élève)
 * GET /api/emplois-du-temps/mon-planning
 */
async function getMonPlanning(req, res, next) {
    try {
        let cours = [];

        if (req.user.role === 'PROFESSEUR') {
            const profId = req.user.professeur_id;
            const [rows] = await pool.query(`
                SELECT edt.*, m.nom AS nom_matiere, m.code AS code_matiere, m.couleur_hex,
                       c.nom AS nom_classe, p.nom AS nom_prof, p.prenom AS prenom_prof
                FROM emplois_du_temps edt
                INNER JOIN matieres m ON edt.matiere_id = m.id
                INNER JOIN classes c ON edt.classe_id = c.id
                INNER JOIN professeurs p ON edt.professeur_id = p.id
                WHERE edt.professeur_id = ?
                ORDER BY FIELD(edt.jour, 'Lundi', 'Mardi', 'Mercredi', 'Jeudi', 'Vendredi', 'Samedi'), edt.heure_debut ASC
            `, [profId]);
            cours = rows;
        } else if (req.user.role === 'ELEVE') {
            const [eleve] = await pool.query('SELECT classe_id FROM eleves WHERE id = ?', [req.user.eleve_id]);
            if (eleve.length > 0 && eleve[0].classe_id) {
                const [rows] = await pool.query(`
                    SELECT edt.*, m.nom AS nom_matiere, m.code AS code_matiere, m.couleur_hex,
                           p.nom AS nom_prof, p.prenom AS prenom_prof, c.nom AS nom_classe
                    FROM emplois_du_temps edt
                    INNER JOIN matieres m ON edt.matiere_id = m.id
                    INNER JOIN professeurs p ON edt.professeur_id = p.id
                    INNER JOIN classes c ON edt.classe_id = c.id
                    WHERE edt.classe_id = ?
                    ORDER BY FIELD(edt.jour, 'Lundi', 'Mardi', 'Mercredi', 'Jeudi', 'Vendredi', 'Samedi'), edt.heure_debut ASC
                `, [eleve[0].classe_id]);
                cours = rows;
            }
        }

        res.json({
            succes: true,
            cours
        });
    } catch (error) {
        next(error);
    }
}

/**
 * Création d'un créneau dans l'emploi du temps avec vérification de conflits
 * POST /api/emplois-du-temps
 */
async function createSlot(req, res, next) {
    try {
        const {
            classe_id, matiere_id, professeur_id, jour,
            heure_debut, heure_fin, salle, annee_scolaire_id
        } = req.body;

        if (!classe_id || !matiere_id || !professeur_id || !jour || !heure_debut || !heure_fin || !salle) {
            return res.status(400).json({
                succes: false,
                message: 'Veuillez remplir tous les champs : classe, matière, professeur, jour, horaires et salle.'
            });
        }

        let anneeId = annee_scolaire_id;
        if (!anneeId) {
            const [active] = await pool.query('SELECT id FROM annees_scolaires WHERE active = TRUE LIMIT 1');
            anneeId = active[0]?.id;
        }

        // 1. Détection de conflit : Le professeur est-il déjà occupé ?
        const [conflitProf] = await pool.query(`
            SELECT id FROM emplois_du_temps 
            WHERE professeur_id = ? AND jour = ? AND annee_scolaire_id = ?
              AND ((heure_debut < ? AND heure_fin > ?) OR (heure_debut >= ? AND heure_debut < ?))
            LIMIT 1
        `, [professeur_id, jour, anneeId, heure_fin, heure_debut, heure_debut, heure_fin]);

        if (conflitProf.length > 0) {
            return res.status(409).json({
                succes: false,
                message: `Conflit horaire : cet enseignant donne déjà un cours le ${jour} sur cette tranche horaire.`
            });
        }

        // 2. Détection de conflit : La salle est-elle déjà occupée ?
        const [conflitSalle] = await pool.query(`
            SELECT id FROM emplois_du_temps 
            WHERE salle = ? AND jour = ? AND annee_scolaire_id = ?
              AND ((heure_debut < ? AND heure_fin > ?) OR (heure_debut >= ? AND heure_debut < ?))
            LIMIT 1
        `, [salle, jour, anneeId, heure_fin, heure_debut, heure_debut, heure_fin]);

        if (conflitSalle.length > 0) {
            return res.status(409).json({
                succes: false,
                message: `Conflit de salle : la ${salle} est déjà réservée le ${jour} sur cette tranche horaire.`
            });
        }

        const [result] = await pool.query(`
            INSERT INTO emplois_du_temps (classe_id, matiere_id, professeur_id, jour, heure_debut, heure_fin, salle, annee_scolaire_id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        `, [classe_id, matiere_id, professeur_id, jour, heure_debut, heure_fin, salle, anneeId]);

        res.status(201).json({
            succes: true,
            message: 'Cours programmé dans l\'emploi du temps avec succès.',
            slot_id: result.insertId
        });
    } catch (error) {
        next(error);
    }
}

/**
 * Suppression d'un créneau d'emploi du temps
 * DELETE /api/emplois-du-temps/:id
 */
async function deleteSlot(req, res, next) {
    try {
        const { id } = req.params;
        await pool.query('DELETE FROM emplois_du_temps WHERE id = ?', [id]);
        res.json({ succes: true, message: 'Créneau d\'emploi du temps supprimé.' });
    } catch (error) {
        next(error);
    }
}

module.exports = {
    getParClasse,
    getMonPlanning,
    createSlot,
    deleteSlot
};
