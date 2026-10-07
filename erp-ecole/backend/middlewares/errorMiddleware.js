/**
 * Middleware de gestion centralisée des erreurs de l'API REST
 */
function errorHandler(err, req, res, next) {
    console.error('Erreur serveur capturée :', err);

    // Erreur de contrainte d'unicité SQL (duplicata de clé)
    if (err.code === 'ER_DUP_ENTRY') {
        return res.status(409).json({
            succes: false,
            message: 'Un enregistrement avec cette valeur unique (email, nom d\'utilisateur, matricule ou référence) existe déjà.',
            erreur: process.env.NODE_ENV === 'development' ? err.sqlMessage : undefined
        });
    }

    // Erreur de clé étrangère
    if (err.code === 'ER_NO_REFERENCED_ROW_2') {
        return res.status(400).json({
            succes: false,
            message: 'L\'élément référencé (classe, professeur, matière ou élève) est introuvable.',
            erreur: process.env.NODE_ENV === 'development' ? err.sqlMessage : undefined
        });
    }

    if (err.code === 'ER_ROW_IS_REFERENCED_2') {
        return res.status(409).json({
            succes: false,
            message: 'Impossible de supprimer cet enregistrement car il est lié à d\'autres données (notes, inscriptions ou cours).',
            erreur: process.env.NODE_ENV === 'development' ? err.sqlMessage : undefined
        });
    }

    // Erreur de validation de contrainte CHECK
    if (err.code === 'ER_CHECK_CONSTRAINT_VIOLATED') {
        return res.status(422).json({
            succes: false,
            message: 'Les données fournies ne respectent pas les contraintes métiers (ex: note entre 0 et 20 ou montant positif).',
            erreur: process.env.NODE_ENV === 'development' ? err.sqlMessage : undefined
        });
    }

    const statusCode = err.statusCode || 500;
    const message = err.message || 'Une erreur interne inattendue est survenue sur le serveur.';

    res.status(statusCode).json({
        succes: false,
        message,
        erreur: process.env.NODE_ENV === 'development' ? err.stack : undefined
    });
}

/**
 * Middleware pour routes non trouvées (404)
 */
function notFoundHandler(req, res, next) {
    res.status(404).json({
        succes: false,
        message: `La route demandée [${req.method} ${req.originalUrl}] n'existe pas sur cette API.`
    });
}

module.exports = {
    errorHandler,
    notFoundHandler
};
