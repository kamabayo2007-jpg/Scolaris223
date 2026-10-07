const { pool } = require('../config/database');

/**
 * Récupère toutes les matières
 * GET /api/matieres
 */
async function getAllMatieres(req, res, next) {
    try {
        const [matieres] = await pool.query(`
            SELECT m.*,
                   (SELECT COUNT(DISTINCT ens.professeur_id) FROM enseignements ens WHERE ens.matiere_id = m.id) AS nombre_professeurs,
                   (SELECT COUNT(DISTINCT ens.classe_id) FROM enseignements ens WHERE ens.matiere_id = m.id) AS nombre_classes
            FROM matieres m
            ORDER BY m.nom ASC
        `);

        res.json({
            succes: true,
            total: matieres.length,
            matieres
        });
    } catch (error) {
        next(error);
    }
}

/**
 * Création d'une nouvelle matière
 * POST /api/matieres
 */
async function createMatiere(req, res, next) {
    try {
        const { code, nom, coefficient = 1.0, niveau = 'Général', couleur_hex = '#2563EB' } = req.body;

        if (!code || !nom) {
            return res.status(400).json({
                succes: false,
                message: 'Veuillez renseigner le code (ex: MATH) et le nom de la matière.'
            });
        }

        const [result] = await pool.query(`
            INSERT INTO matieres (code, nom, coefficient, niveau, couleur_hex)
            VALUES (?, ?, ?, ?, ?)
        `, [code.toUpperCase().trim(), nom.trim(), coefficient, niveau, couleur_hex]);

        res.status(201).json({
            succes: true,
            message: 'Matière créée avec succès.',
            matiere_id: result.insertId
        });
    } catch (error) {
        next(error);
    }
}

/**
 * Modification d'une matière
 * PUT /api/matieres/:id
 */
async function updateMatiere(req, res, next) {
    try {
        const { id } = req.params;
        const { code, nom, coefficient, niveau, couleur_hex } = req.body;

        await pool.query(`
            UPDATE matieres SET
                code = COALESCE(?, code),
                nom = COALESCE(?, nom),
                coefficient = COALESCE(?, coefficient),
                niveau = COALESCE(?, niveau),
                couleur_hex = COALESCE(?, couleur_hex)
            WHERE id = ?
        `, [code ? code.toUpperCase().trim() : null, nom, coefficient, niveau, couleur_hex, id]);

        res.json({
            succes: true,
            message: 'Matière mise à jour avec succès.'
        });
    } catch (error) {
        next(error);
    }
}

/**
 * Suppression d'une matière
 * DELETE /api/matieres/:id
 */
async function deleteMatiere(req, res, next) {
    try {
        const { id } = req.params;
        await pool.query('DELETE FROM matieres WHERE id = ?', [id]);
        res.json({
            succes: true,
            message: 'Matière supprimée avec succès.'
        });
    } catch (error) {
        next(error);
    }
}

module.exports = {
    getAllMatieres,
    createMatiere,
    updateMatiere,
    deleteMatiere
};
