const bcrypt = require('bcryptjs');
const { pool } = require('../config/database');

/**
 * Liste de tous les professeurs
 * GET /api/professeurs
 */
async function getAllProfesseurs(req, res, next) {
    try {
        const { recherche } = req.query;

        let query = `
            SELECT p.id, p.user_id, p.matricule, p.nom, p.prenom, p.email, p.telephone, 
                   p.specialite, u.actif,
                   COUNT(DISTINCT ens.classe_id) AS nombre_classes,
                   COUNT(DISTINCT ens.matiere_id) AS nombre_matieres
            FROM professeurs p
            INNER JOIN users u ON p.user_id = u.id
            LEFT JOIN enseignements ens ON p.id = ens.professeur_id
            WHERE 1=1
        `;
        const params = [];

        if (recherche) {
            query += ` AND (p.nom LIKE ? OR p.prenom LIKE ? OR p.matricule LIKE ? OR p.specialite LIKE ?)`;
            const motif = `%${recherche.trim()}%`;
            params.push(motif, motif, motif, motif);
        }

        query += ` GROUP BY p.id ORDER BY p.nom ASC, p.prenom ASC`;

        const [professeurs] = await pool.query(query, params);

        res.json({
            succes: true,
            total: professeurs.length,
            professeurs
        });
    } catch (error) {
        next(error);
    }
}

/**
 * Récupère le détail d'un professeur avec ses classes, matières et cours
 * GET /api/professeurs/:id
 */
async function getProfesseurById(req, res, next) {
    try {
        const { id } = req.params;

        // Sécurité : Un professeur ne peut consulter que sa propre fiche sauf si ADMIN
        if (req.user.role === 'PROFESSEUR' && req.user.professeur_id !== parseInt(id, 10)) {
            return res.status(403).json({
                succes: false,
                message: 'Accès refusé : vous ne pouvez consulter que votre propre fiche professeur.'
            });
        }

        const [profs] = await pool.query(
            `SELECT p.*, u.username, u.actif 
             FROM professeurs p 
             INNER JOIN users u ON p.user_id = u.id 
             WHERE p.id = ? LIMIT 1`,
            [id]
        );

        if (profs.length === 0) {
            return res.status(404).json({
                succes: false,
                message: 'Professeur introuvable.'
            });
        }

        const professeur = profs[0];

        // Enseignements attribués (Classes et Matières)
        const [enseignements] = await pool.query(
            `SELECT ens.id AS enseignement_id, c.id AS classe_id, c.nom AS nom_classe, c.niveau,
                    m.id AS matiere_id, m.nom AS nom_matiere, m.code AS code_matiere, m.coefficient,
                    ans.libelle AS annee_scolaire
             FROM enseignements ens
             INNER JOIN classes c ON ens.classe_id = c.id
             INNER JOIN matieres m ON ens.matiere_id = m.id
             INNER JOIN annees_scolaires ans ON ens.annee_scolaire_id = ans.id
             WHERE ens.professeur_id = ?
             ORDER BY c.nom ASC, m.nom ASC`,
            [id]
        );

        // Emploi du temps de la semaine
        const [cours] = await pool.query(
            `SELECT edt.id, edt.jour, edt.heure_debut, edt.heure_fin, edt.salle,
                    c.nom AS nom_classe, m.nom AS nom_matiere, m.couleur_hex
             FROM emplois_du_temps edt
             INNER JOIN classes c ON edt.classe_id = c.id
             INNER JOIN matieres m ON edt.matiere_id = m.id
             WHERE edt.professeur_id = ?
             ORDER BY FIELD(edt.jour, 'Lundi', 'Mardi', 'Mercredi', 'Jeudi', 'Vendredi', 'Samedi'), edt.heure_debut ASC`,
            [id]
        );

        res.json({
            succes: true,
            professeur: {
                ...professeur,
                enseignements,
                emplois_du_temps: cours
            }
        });
    } catch (error) {
        next(error);
    }
}

/**
 * Création d'un professeur
 * POST /api/professeurs
 */
async function createProfesseur(req, res, next) {
    const connection = await pool.getConnection();
    try {
        await connection.beginTransaction();

        const {
            nom, prenom, email, telephone, date_naissance, adresse,
            specialite, username, password
        } = req.body;

        if (!nom || !prenom || !email || !specialite) {
            await connection.rollback();
            return res.status(400).json({
                succes: false,
                message: 'Veuillez remplir les champs requis : nom, prénom, email et spécialité.'
            });
        }

        const cleanNom = nom.toLowerCase().replace(/[^a-z0-9]/g, '');
        const cleanPrenom = prenom.toLowerCase().replace(/[^a-z0-9]/g, '');
        const finalUsername = username || `prof.${cleanPrenom}.${cleanNom}`;
        const defaultPassword = password || 'Prof@123456';

        const passwordHash = await bcrypt.hash(defaultPassword, 10);

        // 1. Création compte utilisateur
        const [userResult] = await connection.query(
            `INSERT INTO users (username, email, password_hash, role, actif, doit_changer_mdp) 
             VALUES (?, ?, ?, 'PROFESSEUR', TRUE, TRUE)`,
            [finalUsername, email, passwordHash]
        );
        const userId = userResult.insertId;

        // 2. Générer matricule
        const [count] = await connection.query('SELECT COUNT(*) AS total FROM professeurs');
        const matricule = `ENS-${new Date().getFullYear()}-${String(count[0].total + 1).padStart(3, '0')}`;

        // 3. Profil professeur
        const [profResult] = await connection.query(
            `INSERT INTO professeurs (user_id, matricule, nom, prenom, date_naissance, telephone, email, adresse, specialite)
             VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)`,
            [userId, matricule, nom, prenom, date_naissance || null, telephone || null, email, adresse || null, specialite]
        );

        await connection.commit();

        res.status(201).json({
            succes: true,
            message: 'Professeur créé avec succès.',
            professeur_id: profResult.insertId,
            matricule,
            identifiants_temporaires: {
                username: finalUsername,
                mot_de_passe: defaultPassword
            }
        });
    } catch (error) {
        await connection.rollback();
        next(error);
    } finally {
        connection.release();
    }
}

/**
 * Mise à jour d'un professeur
 * PUT /api/professeurs/:id
 */
async function updateProfesseur(req, res, next) {
    try {
        const { id } = req.params;
        const { nom, prenom, date_naissance, telephone, email, adresse, specialite } = req.body;

        await pool.query(
            `UPDATE professeurs SET
                nom = COALESCE(?, nom),
                prenom = COALESCE(?, prenom),
                date_naissance = COALESCE(?, date_naissance),
                telephone = COALESCE(?, telephone),
                email = COALESCE(?, email),
                adresse = COALESCE(?, adresse),
                specialite = COALESCE(?, specialite)
             WHERE id = ?`,
            [nom, prenom, date_naissance, telephone, email, adresse, specialite, id]
        );

        // Synchroniser email dans la table users
        if (email) {
            const [profs] = await pool.query('SELECT user_id FROM professeurs WHERE id = ?', [id]);
            if (profs.length > 0) {
                await pool.query('UPDATE users SET email = ? WHERE id = ?', [email, profs[0].user_id]);
            }
        }

        res.json({
            succes: true,
            message: 'Informations du professeur mises à jour.'
        });
    } catch (error) {
        next(error);
    }
}

/**
 * Affecter un professeur à une matière et une classe
 * POST /api/professeurs/:id/enseignements
 */
async function affecterEnseignement(req, res, next) {
    try {
        const professeur_id = req.params.id;
        const { matiere_id, classe_id, annee_scolaire_id } = req.body;

        if (!matiere_id || !classe_id) {
            return res.status(400).json({
                succes: false,
                message: 'Veuillez spécifier la matière et la classe à affecter.'
            });
        }

        let anneeId = annee_scolaire_id;
        if (!anneeId) {
            const [activeAnnee] = await pool.query('SELECT id FROM annees_scolaires WHERE active = TRUE LIMIT 1');
            anneeId = activeAnnee[0]?.id;
        }

        await pool.query(
            `INSERT INTO enseignements (professeur_id, matiere_id, classe_id, annee_scolaire_id)
             VALUES (?, ?, ?, ?)
             ON DUPLICATE KEY UPDATE annee_scolaire_id = VALUES(annee_scolaire_id)`,
            [professeur_id, matiere_id, classe_id, anneeId]
        );

        res.status(201).json({
            succes: true,
            message: 'Affectation pédagogique enregistrée avec succès.'
        });
    } catch (error) {
        next(error);
    }
}

/**
 * Supprimer une affectation pédagogique
 * DELETE /api/professeurs/enseignements/:enseignementId
 */
async function retirerEnseignement(req, res, next) {
    try {
        const { enseignementId } = req.params;
        await pool.query('DELETE FROM enseignements WHERE id = ?', [enseignementId]);
        res.json({ succes: true, message: 'Affectation retirée.' });
    } catch (error) {
        next(error);
    }
}

/**
 * Suppression d'un professeur
 * DELETE /api/professeurs/:id
 */
async function deleteProfesseur(req, res, next) {
    try {
        const { id } = req.params;
        const [profs] = await pool.query('SELECT user_id FROM professeurs WHERE id = ?', [id]);
        if (profs.length === 0) {
            return res.status(404).json({ succes: false, message: 'Professeur introuvable.' });
        }

        await pool.query('DELETE FROM users WHERE id = ?', [profs[0].user_id]);

        res.json({
            succes: true,
            message: 'Professeur et compte utilisateur supprimés avec succès.'
        });
    } catch (error) {
        next(error);
    }
}

module.exports = {
    getAllProfesseurs,
    getProfesseurById,
    createProfesseur,
    updateProfesseur,
    affecterEnseignement,
    retirerEnseignement,
    deleteProfesseur
};
