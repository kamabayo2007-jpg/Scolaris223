const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const { pool } = require('../config/database');

/**
 * Connexion d'un utilisateur (Admin, Professeur ou Élève)
 * POST /api/auth/login
 */
async function login(req, res, next) {
    try {
        const { username_ou_email, password } = req.body;

        if (!username_ou_email || !password) {
            return res.status(400).json({
                succes: false,
                message: 'Veuillez fournir un nom d\'utilisateur (ou email) et un mot de passe.'
            });
        }

        // Recherche par nom d'utilisateur ou par email
        const [users] = await pool.query(
            `SELECT id, username, email, password_hash, role, actif, doit_changer_mdp 
             FROM users 
             WHERE username = ? OR email = ? 
             LIMIT 1`,
            [username_ou_email.trim(), username_ou_email.trim()]
        );

        if (users.length === 0) {
            return res.status(401).json({
                succes: false,
                message: 'Identifiants incorrects. Veuillez vérifier vos accès.'
            });
        }

        const user = users[0];

        if (!user.actif) {
            return res.status(403).json({
                succes: false,
                message: 'Votre compte est désactivé. Veuillez contacter l\'administration de l\'école.'
            });
        }

        // Vérification sécurisée du mot de passe avec bcrypt
        const passwordMatch = await bcrypt.compare(password, user.password_hash);
        if (!passwordMatch) {
            return res.status(401).json({
                succes: false,
                message: 'Identifiants incorrects. Mot de passe invalide.'
            });
        }

        // Récupération des données métiers associées au rôle
        let profilMetier = null;
        if (user.role === 'PROFESSEUR') {
            const [profs] = await pool.query(
                'SELECT id, matricule, nom, prenom, specialite FROM professeurs WHERE user_id = ? LIMIT 1',
                [user.id]
            );
            profilMetier = profs[0] || null;
        } else if (user.role === 'ELEVE') {
            const [eleves] = await pool.query(
                `SELECT e.id, e.matricule, e.nom, e.prenom, e.classe_id, c.nom AS nom_classe 
                 FROM eleves e 
                 LEFT JOIN classes c ON e.classe_id = c.id 
                 WHERE e.user_id = ? LIMIT 1`,
                [user.id]
            );
            profilMetier = eleves[0] || null;
        }

        // Génération du token JWT
        const token = jwt.sign(
            {
                id: user.id,
                username: user.username,
                email: user.email,
                role: user.role
            },
            process.env.JWT_SECRET || 'cle_secrete_jwt_erp_ecole_2025',
            { expiresIn: process.env.JWT_EXPIRES_IN || '24h' }
        );

        res.json({
            succes: true,
            message: 'Connexion réussie avec succès.',
            token,
            user: {
                id: user.id,
                username: user.username,
                email: user.email,
                role: user.role,
                doit_changer_mdp: Boolean(user.doit_changer_mdp),
                profil: profilMetier
            }
        });
    } catch (error) {
        next(error);
    }
}

/**
 * Récupère le profil de l'utilisateur connecté
 * GET /api/auth/me
 */
async function getMe(req, res, next) {
    try {
        const userId = req.user.id;

        const [users] = await pool.query(
            'SELECT id, username, email, role, actif, created_at FROM users WHERE id = ? LIMIT 1',
            [userId]
        );

        if (users.length === 0) {
            return res.status(404).json({
                succes: false,
                message: 'Utilisateur introuvable.'
            });
        }

        const user = users[0];
        let profil = null;

        if (user.role === 'PROFESSEUR') {
            const [profs] = await pool.query(
                'SELECT * FROM professeurs WHERE user_id = ? LIMIT 1',
                [userId]
            );
            profil = profs[0] || null;
        } else if (user.role === 'ELEVE') {
            const [eleves] = await pool.query(
                `SELECT e.*, c.nom AS nom_classe, c.niveau 
                 FROM eleves e 
                 LEFT JOIN classes c ON e.classe_id = c.id 
                 WHERE e.user_id = ? LIMIT 1`,
                [userId]
            );
            profil = eleves[0] || null;
        }

        res.json({
            succes: true,
            user: {
                ...user,
                profil
            }
        });
    } catch (error) {
        next(error);
    }
}

/**
 * Changement du mot de passe par l'utilisateur connecté
 * PUT /api/auth/mot-de-passe
 */
async function changerMotDePasse(req, res, next) {
    try {
        const userId = req.user.id;
        const { ancien_mdp, nouveau_mdp } = req.body;

        if (!ancien_mdp || !nouveau_mdp) {
            return res.status(400).json({
                succes: false,
                message: 'Veuillez spécifier votre ancien et votre nouveau mot de passe.'
            });
        }

        if (nouveau_mdp.length < 6) {
            return res.status(400).json({
                succes: false,
                message: 'Le nouveau mot de passe doit contenir au minimum 6 caractères.'
            });
        }

        const [users] = await pool.query(
            'SELECT password_hash FROM users WHERE id = ? LIMIT 1',
            [userId]
        );

        const passwordMatch = await bcrypt.compare(ancien_mdp, users[0].password_hash);
        if (!passwordMatch) {
            return res.status(400).json({
                succes: false,
                message: 'L\'ancien mot de passe saisi est incorrect.'
            });
        }

        const nouveauHash = await bcrypt.hash(nouveau_mdp, 10);

        await pool.query(
            'UPDATE users SET password_hash = ?, doit_changer_mdp = FALSE WHERE id = ?',
            [nouveauHash, userId]
        );

        res.json({
            succes: true,
            message: 'Votre mot de passe a été modifié avec succès.'
        });
    } catch (error) {
        next(error);
    }
}

/**
 * Déconnexion (côté client, le token est détruit)
 * POST /api/auth/logout
 */
function logout(req, res) {
    res.json({
        succes: true,
        message: 'Déconnexion effectuée avec succès.'
    });
}

module.exports = {
    login,
    getMe,
    changerMotDePasse,
    logout
};
