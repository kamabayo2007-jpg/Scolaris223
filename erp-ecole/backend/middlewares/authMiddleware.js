const jwt = require('jsonwebtoken');
const { pool } = require('../config/database');

/**
 * Middleware d'authentification par JWT
 * Vérifie le token dans l'entête Authorization (Bearer <token>)
 */
async function authenticateToken(req, res, next) {
    const authHeader = req.headers['authorization'];
    const token = authHeader && authHeader.split(' ')[1];

    if (!token) {
        return res.status(401).json({
            succes: false,
            message: 'Accès non autorisé : aucun jeton d\'authentification fourni.'
        });
    }

    try {
        const decoded = jwt.verify(token, process.env.JWT_SECRET || 'cle_secrete_jwt_erp_ecole_2025');

        // Vérifier si l'utilisateur existe toujours et est actif
        const [rows] = await pool.query(
            'SELECT id, username, email, role, actif FROM users WHERE id = ? LIMIT 1',
            [decoded.id]
        );

        if (rows.length === 0) {
            return res.status(401).json({
                succes: false,
                message: 'Utilisateur introuvable ou compte supprimé.'
            });
        }

        const user = rows[0];
        if (!user.actif) {
            return res.status(403).json({
                succes: false,
                message: 'Ce compte utilisateur a été désactivé par l\'administration.'
            });
        }

        // Si l'utilisateur est un professeur ou un élève, charger son ID métier
        if (user.role === 'PROFESSEUR') {
            const [profs] = await pool.query(
                'SELECT id, matricule, nom, prenom FROM professeurs WHERE user_id = ? LIMIT 1',
                [user.id]
            );
            if (profs.length > 0) {
                user.professeur_id = profs[0].id;
                user.professeur = profs[0];
            }
        } else if (user.role === 'ELEVE') {
            const [eleves] = await pool.query(
                'SELECT id, matricule, nom, prenom, classe_id FROM eleves WHERE user_id = ? LIMIT 1',
                [user.id]
            );
            if (eleves.length > 0) {
                user.eleve_id = eleves[0].id;
                user.eleve = eleves[0];
            }
        }

        req.user = user;
        next();
    } catch (error) {
        if (error.name === 'TokenExpiredError') {
            return res.status(401).json({
                succes: false,
                message: 'Votre session a expiré. Veuillez vous reconnecter.',
                code: 'TOKEN_EXPIRED'
            });
        }

        return res.status(403).json({
            succes: false,
            message: 'Jeton d\'authentification invalide ou altéré.'
        });
    }
}

/**
 * Middleware de contrôle strict des rôles côté serveur
 * Ne fait jamais confiance aux données envoyées par le client
 * @param  {...string} rolesAutorises - Liste des rôles autorisés (ex: 'ADMIN', 'PROFESSEUR')
 */
function requireRole(...rolesAutorises) {
    return (req, res, next) => {
        if (!req.user) {
            return res.status(401).json({
                succes: false,
                message: 'Authentification requise.'
            });
        }

        if (!rolesAutorises.includes(req.user.role)) {
            return res.status(403).json({
                succes: false,
                message: `Accès refusé : cette action requiert l'un des rôles suivants : [${rolesAutorises.join(', ')}]. Votre rôle actuel est : ${req.user.role}.`
            });
        }

        next();
    };
}

module.exports = {
    authenticateToken,
    requireRole
};
