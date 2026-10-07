/**
 * Middleware d'isolation Multi-Tenant pour ERP Gestion École
 * Garantit que chaque établissement scolaire n'accède qu'à ses propres données
 * et permet au Super Administrateur un contrôle total inter-écoles.
 */

function tenantMiddleware(req, res, next) {
    // Si l'utilisateur est authentifié
    if (req.user) {
        // Le rôle SUPER_ADMIN possède le contrôle total sur l'ensemble des écoles
        if (req.user.role === 'SUPER_ADMIN' || req.user.role === 'ADMIN_SYSTEME') {
            // Le super admin peut cibler une école spécifique via l'en-tête ou le paramètre
            const targetSchoolId = req.headers['x-school-id'] || req.query.school_id || req.body.school_id;
            req.school_id = targetSchoolId ? parseInt(targetSchoolId, 10) : null;
            req.isSuperAdmin = true;
            return next();
        }

        // Pour tous les autres rôles (Admin d'école, Professeur, Comptable, Élève, Parent),
        // l'accès est strictement verrouillé sur l'établissement de rattachement
        req.school_id = req.user.school_id || 1;
        req.isSuperAdmin = false;
        return next();
    }

    // Requêtes publiques ou non-authentifiées (ex: webhooks de paiement)
    const headerSchoolId = req.headers['x-school-id'] || req.query.school_id;
    req.school_id = headerSchoolId ? parseInt(headerSchoolId, 10) : 1;
    req.isSuperAdmin = false;
    next();
}

/**
 * Helper SQL pour injecter la clause WHERE school_id de manière transparente
 * @param {Object} req 
 * @param {string} tablePrefix (ex: 'e.' ou '')
 * @returns {{ sqlClause: string, params: Array }}
 */
function getTenantFilter(req, tablePrefix = '') {
    if (req.isSuperAdmin && !req.school_id) {
        return { sqlClause: ' 1=1 ', params: [] };
    }
    const prefix = tablePrefix ? `${tablePrefix}` : '';
    return {
        sqlClause: ` ${prefix}school_id = ? `,
        params: [req.school_id || 1]
    };
}

module.exports = {
    tenantMiddleware,
    getTenantFilter
};
