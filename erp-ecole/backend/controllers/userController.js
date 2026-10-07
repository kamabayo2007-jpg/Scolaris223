const bcrypt = require('bcryptjs');
const { pool } = require('../config/database');

/**
 * Récupère tous les comptes utilisateurs
 * GET /api/users
 */
async function getAllUsers(req, res, next) {
    try {
        const { role, recherche, actif } = req.query;

        let query = `
            SELECT id, username, email, role, actif, doit_changer_mdp, created_at, updated_at
            FROM users
            WHERE 1=1
        `;
        const params = [];

        if (role) {
            query += ` AND role = ?`;
            params.push(role);
        }

        if (actif !== undefined) {
            query += ` AND actif = ?`;
            params.push(actif === 'true' || actif === '1');
        }

        if (recherche) {
            query += ` AND (username LIKE ? OR email LIKE ?)`;
            const motif = `%${recherche.trim()}%`;
            params.push(motif, motif);
        }

        query += ` ORDER BY created_at DESC`;

        const [users] = await pool.query(query, params);

        res.json({
            succes: true,
            total: users.length,
            users
        });
    } catch (error) {
        next(error);
    }
}

/**
 * Active ou désactive un compte utilisateur
 * PATCH /api/users/:id/statut
 */
async function toggleUserStatus(req, res, next) {
    try {
        const { id } = req.params;
        const { actif } = req.body;

        // Empêcher de désactiver son propre compte admin
        if (parseInt(id, 10) === req.user.id) {
            return res.status(400).json({
                succes: false,
                message: 'Action interdite : vous ne pouvez pas désactiver votre propre compte administrateur.'
            });
        }

        await pool.query('UPDATE users SET actif = ? WHERE id = ?', [Boolean(actif), id]);

        res.json({
            succes: true,
            message: `Le compte utilisateur a été ${actif ? 'activé' : 'désactivé'} avec succès.`
        });
    } catch (error) {
        next(error);
    }
}

/**
 * Réinitialisation administrative du mot de passe
 * POST /api/users/:id/reinitialiser-mdp
 */
async function resetPassword(req, res, next) {
    try {
        const { id } = req.params;
        const { nouveau_mdp } = req.body;

        const mdpFinal = nouveau_mdp || 'Ecole@2025';
        const hash = await bcrypt.hash(mdpFinal, 10);

        await pool.query(
            'UPDATE users SET password_hash = ?, doit_changer_mdp = TRUE WHERE id = ?',
            [hash, id]
        );

        res.json({
            succes: true,
            message: 'Mot de passe réinitialisé avec succès.',
            mot_de_passe_temporaire: mdpFinal
        });
    } catch (error) {
        next(error);
    }
}

module.exports = {
    getAllUsers,
    toggleUserStatus,
    resetPassword
};
