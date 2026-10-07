const { pool } = require('../config/database');
const { genererReferencePaiement } = require('../utils/helpers');

/**
 * Liste de tous les paiements avec filtres
 * GET /api/paiements
 */
async function getAllPaiements(req, res, next) {
    try {
        const { eleve_id, classe_id, statut, type, date_debut, date_fin } = req.query;

        let query = `
            SELECT p.*, e.nom AS nom_eleve, e.prenom AS prenom_eleve, e.matricule,
                   c.nom AS nom_classe, u.username AS enregistre_par
            FROM paiements p
            INNER JOIN eleves e ON p.eleve_id = e.id
            LEFT JOIN classes c ON e.classe_id = c.id
            LEFT JOIN users u ON p.enregistre_par_user_id = u.id
            WHERE 1=1
        `;
        const params = [];

        // Sécurité rôle : Un élève ne voit que ses paiements
        if (req.user.role === 'ELEVE') {
            query += ` AND p.eleve_id = ?`;
            params.push(req.user.eleve_id);
        } else if (eleve_id) {
            query += ` AND p.eleve_id = ?`;
            params.push(eleve_id);
        }

        if (classe_id) {
            query += ` AND e.classe_id = ?`;
            params.push(classe_id);
        }

        if (statut) {
            query += ` AND p.statut = ?`;
            params.push(statut);
        }

        if (type) {
            query += ` AND p.type = ?`;
            params.push(type);
        }

        if (date_debut) {
            query += ` AND p.date_paiement >= ?`;
            params.push(date_debut);
        }

        if (date_fin) {
            query += ` AND p.date_paiement <= ?`;
            params.push(date_fin);
        }

        query += ` ORDER BY p.date_paiement DESC, p.id DESC`;

        const [paiements] = await pool.query(query, params);

        res.json({
            succes: true,
            total: paiements.length,
            paiements
        });
    } catch (error) {
        next(error);
    }
}

/**
 * Enregistrement d'un paiement de scolarité ou inscription
 * POST /api/paiements
 */
async function createPaiement(req, res, next) {
    try {
        const {
            eleve_id, montant, type = 'SCOLARITE', date_paiement,
            mode_paiement = 'ESPECES', statut = 'PAYE', commentaire
        } = req.body;

        const montantVal = parseFloat(montant);
        if (isNaN(montantVal) || montantVal <= 0) {
            return res.status(400).json({
                succes: false,
                message: 'Le montant du paiement doit être supérieur à zéro.'
            });
        }

        if (!eleve_id) {
            return res.status(400).json({
                succes: false,
                message: 'Veuillez sélectionner l\'élève concerné.'
            });
        }

        const datePaiementVal = date_paiement || new Date().toISOString().split('T')[0];
        const reference = genererReferencePaiement();

        const [result] = await pool.query(`
            INSERT INTO paiements (eleve_id, montant, type, date_paiement, reference,
                                   mode_paiement, statut, commentaire, enregistre_par_user_id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        `, [eleve_id, montantVal, type, datePaiementVal, reference,
            mode_paiement, statut, commentaire || null, req.user.id]);

        // Notification envoyée au compte de l'élève
        const [eleve] = await pool.query('SELECT user_id, nom, prenom FROM eleves WHERE id = ?', [eleve_id]);
        if (eleve.length > 0) {
            await pool.query(`
                INSERT INTO notifications (user_id, titre, message, type)
                VALUES (?, ?, ?, 'SUCCES')
            `, [eleve[0].user_id, 'Reçu de paiement émis', `Paiement de ${montantVal.toFixed(2)} € reçu avec succès. Réf: ${reference}`]);
        }

        res.status(201).json({
            succes: true,
            message: 'Paiement enregistré avec succès.',
            paiement_id: result.insertId,
            reference
        });
    } catch (error) {
        next(error);
    }
}

/**
 * Récupère le récapitulatif financier d'un élève (frais, payé, solde restant)
 * GET /api/paiements/eleve/:eleveId/solde
 */
async function getSoldeEleve(req, res, next) {
    try {
        const { eleveId } = req.params;

        // Sécurité : l'élève ne peut consulter que son solde
        if (req.user.role === 'ELEVE' && req.user.eleve_id !== parseInt(eleveId, 10)) {
            return res.status(403).json({ succes: false, message: 'Accès refusé.' });
        }

        const [eleves] = await pool.query(`
            SELECT e.id, e.nom, e.prenom, e.matricule, c.nom AS nom_classe, c.frais_scolarite
            FROM eleves e
            LEFT JOIN classes c ON e.classe_id = c.id
            WHERE e.id = ? LIMIT 1
        `, [eleveId]);

        if (eleves.length === 0) {
            return res.status(404).json({ succes: false, message: 'Élève introuvable.' });
        }

        const eleve = eleves[0];
        const fraisTotalAttendu = parseFloat(eleve.frais_scolarite) || 0;

        const [paiements] = await pool.query(`
            SELECT * FROM paiements 
            WHERE eleve_id = ? AND statut = 'PAYE'
            ORDER BY date_paiement DESC
        `, [eleveId]);

        const totalPaye = paiements.reduce((acc, p) => acc + parseFloat(p.montant), 0);
        const resteAPayer = Math.max(0, fraisTotalAttendu - totalPaye);

        res.json({
            succes: true,
            eleve: {
                id: eleve.id,
                nom_complet: `${eleve.nom} ${eleve.prenom}`,
                classe: eleve.nom_classe,
                frais_attendu: fraisTotalAttendu,
                total_paye: totalPaye,
                reste_a_payer: resteAPayer
            },
            historique: paiements
        });
    } catch (error) {
        next(error);
    }
}

/**
 * Suppression d'un paiement
 * DELETE /api/paiements/:id
 */
async function deletePaiement(req, res, next) {
    try {
        const { id } = req.params;
        await pool.query('DELETE FROM paiements WHERE id = ?', [id]);
        res.json({ succes: true, message: 'Enregistrement de paiement supprimé.' });
    } catch (error) {
        next(error);
    }
}

module.exports = {
    getAllPaiements,
    createPaiement,
    getSoldeEleve,
    deletePaiement
};
