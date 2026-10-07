const { pool } = require('../config/database');

/**
 * Liste de toutes les écoles ou de l'école active
 * GET /api/schools
 */
async function getAllSchools(req, res, next) {
    try {
        let query = `SELECT * FROM schools ORDER BY id ASC`;
        const params = [];

        // Si l'utilisateur n'est pas Super Admin, il ne voit que son école
        if (!req.isSuperAdmin && req.school_id) {
            query = `SELECT * FROM schools WHERE id = ? LIMIT 1`;
            params.push(req.school_id);
        }

        const [schools] = await pool.query(query, params);

        res.json({
            succes: true,
            total: schools.length,
            schools
        });
    } catch (error) {
        next(error);
    }
}

/**
 * Détails d'une école spécifique
 * GET /api/schools/:id
 */
async function getSchoolById(req, res, next) {
    try {
        const { id } = req.params;
        const [schools] = await pool.query(`SELECT * FROM schools WHERE id = ? LIMIT 1`, [id]);

        if (schools.length === 0) {
            return res.status(404).json({ succes: false, message: 'Établissement introuvable.' });
        }

        const school = schools[0];
        const [subs] = await pool.query(
            `SELECT * FROM school_subscriptions WHERE school_id = ? ORDER BY date_paiement DESC`,
            [id]
        );

        res.json({
            succes: true,
            school,
            subscriptions: subs
        });
    } catch (error) {
        next(error);
    }
}

/**
 * Création d'une nouvelle école (Super Administrateur)
 * POST /api/schools
 */
async function createSchool(req, res, next) {
    try {
        const {
            code, nom, ville = 'Abidjan', pays = "Côte d'Ivoire",
            telephone, email, devise = 'FCFA',
            frais_inscription = 250000, frais_annuel = 500000,
            couleur_banniere = '#1E3A8A'
        } = req.body;

        if (!code || !nom) {
            return res.status(400).json({ succes: false, message: 'Code et nom de l\'école obligatoires.' });
        }

        const [result] = await pool.query(`
            INSERT INTO schools (
                code, nom, ville, pays, telephone, email, devise,
                frais_inscription, frais_annuel, statut_souscription, couleur_banniere
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'ACTIF', ?)
        `, [
            code.trim().toUpperCase(),
            nom.trim(),
            ville,
            pays,
            telephone || null,
            email || null,
            devise,
            parseFloat(frais_inscription),
            parseFloat(frais_annuel),
            couleur_banniere
        ]);

        res.status(201).json({
            succes: true,
            message: 'Nouvel établissement scolaire créé avec succès.',
            school_id: result.insertId
        });
    } catch (error) {
        next(error);
    }
}

/**
 * Historique des souscriptions d'une école
 * GET /api/schools/:id/subscriptions
 */
async function getSchoolSubscriptions(req, res, next) {
    try {
        const { id } = req.params;
        const [subs] = await pool.query(
            `SELECT * FROM school_subscriptions WHERE school_id = ? ORDER BY date_paiement DESC`,
            [id]
        );
        res.json({
            succes: true,
            total: subs.length,
            subscriptions: subs
        });
    } catch (error) {
        next(error);
    }
}

module.exports = {
    getAllSchools,
    getSchoolById,
    createSchool,
    getSchoolSubscriptions
};
