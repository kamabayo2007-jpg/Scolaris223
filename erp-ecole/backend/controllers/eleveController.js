const bcrypt = require('bcryptjs');
const { pool } = require('../config/database');
const { calculerMoyennePonderee } = require('../utils/helpers');

/**
 * Récupère la liste de tous les élèves avec filtres et recherche
 * GET /api/eleves
 */
async function getAllEleves(req, res, next) {
    try {
        const { recherche, classe_id, actif, page = 1, limite = 50 } = req.query;
        const offset = (parseInt(page, 10) - 1) * parseInt(limite, 10);

        let query = `
            SELECT e.id, e.user_id, e.matricule, e.nom, e.prenom, e.date_naissance, 
                   e.sexe, e.telephone, e.nom_parent, e.telephone_parent, e.email_parent, 
                   e.classe_id, e.actif, c.nom AS nom_classe, c.niveau
            FROM eleves e
            LEFT JOIN classes c ON e.classe_id = c.id
            WHERE 1=1
        `;
        const params = [];

        if (recherche) {
            query += ` AND (e.nom LIKE ? OR e.prenom LIKE ? OR e.matricule LIKE ? OR e.nom_parent LIKE ?)`;
            const motif = `%${recherche.trim()}%`;
            params.push(motif, motif, motif, motif);
        }

        if (classe_id) {
            query += ` AND e.classe_id = ?`;
            params.push(classe_id);
        }

        if (actif !== undefined) {
            query += ` AND e.actif = ?`;
            params.push(actif === 'true' || actif === '1');
        }

        query += ` ORDER BY e.nom ASC, e.prenom ASC LIMIT ? OFFSET ?`;
        params.push(parseInt(limite, 10), offset);

        const [eleves] = await pool.query(query, params);

        // Récupérer le nombre total pour la pagination
        let countQuery = `SELECT COUNT(*) AS total FROM eleves e WHERE 1=1`;
        const countParams = [];
        if (recherche) {
            countQuery += ` AND (e.nom LIKE ? OR e.prenom LIKE ? OR e.matricule LIKE ?)`;
            const motif = `%${recherche.trim()}%`;
            countParams.push(motif, motif, motif);
        }
        if (classe_id) {
            countQuery += ` AND e.classe_id = ?`;
            countParams.push(classe_id);
        }
        const [countResult] = await pool.query(countQuery, countParams);

        res.json({
            succes: true,
            total: countResult[0].total,
            page: parseInt(page, 10),
            limite: parseInt(limite, 10),
            eleves
        });
    } catch (error) {
        next(error);
    }
}

/**
 * Récupère le détail complet d'un élève
 * GET /api/eleves/:id
 */
async function getEleveById(req, res, next) {
    try {
        const { id } = req.params;

        // Sécurité rôle : Un élève ne peut consulter que son propre profil
        if (req.user.role === 'ELEVE' && req.user.eleve_id !== parseInt(id, 10)) {
            return res.status(403).json({
                succes: false,
                message: 'Accès refusé : vous ne pouvez consulter que votre propre fiche élève.'
            });
        }

        const [eleves] = await pool.query(
            `SELECT e.*, u.username, u.email AS email_compte, c.nom AS nom_classe, c.niveau, c.frais_scolarite 
             FROM eleves e
             INNER JOIN users u ON e.user_id = u.id
             LEFT JOIN classes c ON e.classe_id = c.id
             WHERE e.id = ? LIMIT 1`,
            [id]
        );

        if (eleves.length === 0) {
            return res.status(404).json({
                succes: false,
                message: 'Élève introuvable.'
            });
        }

        const eleve = eleves[0];

        // Notes récentes
        const [notes] = await pool.query(
            `SELECT n.id, n.titre_evaluation, n.note, n.bareme, n.coefficient, n.date_evaluation, 
                    n.commentaire, m.nom AS nom_matiere, m.code AS code_matiere, p.nom AS nom_periode
             FROM notes n
             INNER JOIN matieres m ON n.matiere_id = m.id
             INNER JOIN periodes p ON n.periode_id = p.id
             WHERE n.eleve_id = ?
             ORDER BY n.date_evaluation DESC LIMIT 10`,
            [id]
        );

        // Moyenne générale globale calculée
        const [allNotes] = await pool.query(
            'SELECT note, bareme, coefficient FROM notes WHERE eleve_id = ?',
            [id]
        );
        const moyenneGenerale = calculerMoyennePonderee(allNotes);

        // Synthèse des absences
        const [absencesStats] = await pool.query(
            `SELECT 
                COUNT(*) AS total_enregistrements,
                SUM(CASE WHEN type = 'ABSENCE' THEN 1 ELSE 0 END) AS total_absences,
                SUM(CASE WHEN type = 'RETARD' THEN 1 ELSE 0 END) AS total_retards,
                SUM(CASE WHEN type = 'ABSENCE' AND justifiee = FALSE THEN 1 ELSE 0 END) AS absences_non_justifiees
             FROM absences WHERE eleve_id = ?`,
            [id]
        );

        // Synthèse financière des paiements
        const [paiementStats] = await pool.query(
            `SELECT COALESCE(SUM(montant), 0) AS total_paye 
             FROM paiements WHERE eleve_id = ? AND statut = 'PAYE'`,
            [id]
        );

        const fraisClasse = parseFloat(eleve.frais_scolarite) || 0;
        const totalPaye = parseFloat(paiementStats[0].total_paye) || 0;
        const resteAPayer = Math.max(0, fraisClasse - totalPaye);

        res.json({
            succes: true,
            eleve: {
                ...eleve,
                moyenne_generale: moyenneGenerale,
                notes_recentes: notes,
                statistiques_absences: absencesStats[0],
                statistiques_financieres: {
                    frais_scolarite: fraisClasse,
                    total_paye: totalPaye,
                    reste_a_payer: resteAPayer
                }
            }
        });
    } catch (error) {
        next(error);
    }
}

/**
 * Crée un nouvel élève avec son compte utilisateur lié
 * POST /api/eleves
 */
async function createEleve(req, res, next) {
    const connection = await pool.getConnection();
    try {
        await connection.beginTransaction();

        const {
            nom, prenom, date_naissance, lieu_naissance, sexe = 'M',
            telephone, adresse, nom_parent, telephone_parent, email_parent,
            classe_id, username, password
        } = req.body;

        if (!nom || !prenom || !date_naissance || !nom_parent || !telephone_parent) {
            await connection.rollback();
            return res.status(400).json({
                succes: false,
                message: 'Veuillez renseigner les champs obligatoires : nom, prénom, date de naissance, nom du parent et téléphone.'
            });
        }

        // Créer nom d'utilisateur et email par défaut si non fournis
        const cleanNom = nom.toLowerCase().replace(/[^a-z0-9]/g, '');
        const cleanPrenom = prenom.toLowerCase().replace(/[^a-z0-9]/g, '');
        const finalUsername = username || `${cleanPrenom}.${cleanNom}${Math.floor(10 + Math.random() * 90)}`;
        const finalEmail = email_parent || `${finalUsername}@eleve.ecole.fr`;
        const defaultPassword = password || 'Eleve@123456';

        const passwordHash = await bcrypt.hash(defaultPassword, 10);

        // 1. Création compte utilisateur
        const [userResult] = await connection.query(
            `INSERT INTO users (username, email, password_hash, role, actif, doit_changer_mdp) 
             VALUES (?, ?, ?, 'ELEVE', TRUE, TRUE)`,
            [finalUsername, finalEmail, passwordHash]
        );
        const userId = userResult.insertId;

        // 2. Générer matricule unique
        const [matriculeCount] = await connection.query('SELECT COUNT(*) AS total FROM eleves');
        const matricule = `ELV-${new Date().getFullYear()}-${String(matriculeCount[0].total + 1).padStart(4, '0')}`;

        // 3. Création profil élève
        const [eleveResult] = await connection.query(
            `INSERT INTO eleves (user_id, matricule, nom, prenom, date_naissance, lieu_naissance, 
                                 sexe, telephone, adresse, nom_parent, telephone_parent, email_parent, classe_id)
             VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
            [userId, matricule, nom, prenom, date_naissance, lieu_naissance || null,
             sexe, telephone || null, adresse || null, nom_parent, telephone_parent, email_parent || null, classe_id || null]
        );
        const eleveId = eleveResult.insertId;

        // 4. Inscription annuelle si une classe est renseignée
        if (classe_id) {
            const [activeAnnee] = await connection.query('SELECT id FROM annees_scolaires WHERE active = TRUE LIMIT 1');
            if (activeAnnee.length > 0) {
                const anneeId = activeAnnee[0].id;
                const [classeInfo] = await connection.query('SELECT frais_scolarite FROM classes WHERE id = ?', [classe_id]);
                const frais = classeInfo[0]?.frais_scolarite || 0;

                await connection.query(
                    `INSERT INTO inscriptions (eleve_id, classe_id, annee_scolaire_id, date_inscription, statut, frais_total)
                     VALUES (?, ?, ?, CURDATE(), 'CONFIRME', ?)`,
                    [eleveId, classe_id, anneeId, frais]
                );

                // Mettre à jour l'effectif de la classe
                await connection.query('UPDATE classes SET effectif = effectif + 1 WHERE id = ?', [classe_id]);
            }
        }

        await connection.commit();

        res.status(201).json({
            succes: true,
            message: 'Élève inscrit et compte d\'accès créé avec succès.',
            eleve_id: eleveId,
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
 * Mise à jour d'un élève
 * PUT /api/eleves/:id
 */
async function updateEleve(req, res, next) {
    try {
        const { id } = req.params;
        const {
            nom, prenom, date_naissance, lieu_naissance, sexe,
            telephone, adresse, nom_parent, telephone_parent, email_parent,
            classe_id, actif
        } = req.body;

        await pool.query(
            `UPDATE eleves SET 
                nom = COALESCE(?, nom),
                prenom = COALESCE(?, prenom),
                date_naissance = COALESCE(?, date_naissance),
                lieu_naissance = COALESCE(?, lieu_naissance),
                sexe = COALESCE(?, sexe),
                telephone = COALESCE(?, telephone),
                adresse = COALESCE(?, adresse),
                nom_parent = COALESCE(?, nom_parent),
                telephone_parent = COALESCE(?, telephone_parent),
                email_parent = COALESCE(?, email_parent),
                classe_id = COALESCE(?, classe_id),
                actif = COALESCE(?, actif)
             WHERE id = ?`,
            [nom, prenom, date_naissance, lieu_naissance, sexe, telephone, adresse,
             nom_parent, telephone_parent, email_parent, classe_id, actif, id]
        );

        res.json({
            succes: true,
            message: 'Informations de l\'élève mises à jour avec succès.'
        });
    } catch (error) {
        next(error);
    }
}

/**
 * Suppression ou désactivation d'un élève
 * DELETE /api/eleves/:id
 */
async function deleteEleve(req, res, next) {
    try {
        const { id } = req.params;

        // Récupérer l'utilisateur associé
        const [eleves] = await pool.query('SELECT user_id, classe_id FROM eleves WHERE id = ?', [id]);
        if (eleves.length === 0) {
            return res.status(404).json({ succes: false, message: 'Élève introuvable.' });
        }

        const { user_id, classe_id } = eleves[0];

        // Supprimer l'utilisateur lié (entraîne suppression en cascade de l'élève)
        await pool.query('DELETE FROM users WHERE id = ?', [user_id]);

        if (classe_id) {
            await pool.query('UPDATE classes SET effectif = GREATEST(0, effectif - 1) WHERE id = ?', [classe_id]);
        }

        res.json({
            succes: true,
            message: 'Élève et compte utilisateur supprimés avec succès.'
        });
    } catch (error) {
        next(error);
    }
}

module.exports = {
    getAllEleves,
    getEleveById,
    createEleve,
    updateEleve,
    deleteEleve
};
