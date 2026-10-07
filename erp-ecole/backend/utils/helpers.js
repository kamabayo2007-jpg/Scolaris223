/**
 * Utilitaires et fonctions métiers pour l'ERP Gestion École
 */

/**
 * Calcule la moyenne pondérée d'un ensemble de notes
 * @param {Array<{ note: number, coefficient: number, bareme?: number }>} notes 
 * @returns {number|null} Moyenne ramenée sur 20, arrondie à 2 décimales
 */
function calculerMoyennePonderee(notes) {
    if (!notes || notes.length === 0) return null;

    let sommePonderee = 0;
    let sommeCoefficients = 0;

    for (const item of notes) {
        const noteVal = parseFloat(item.note);
        const coeff = parseFloat(item.coefficient) || 1.0;
        const bareme = parseFloat(item.bareme) || 20.0;

        if (!isNaN(noteVal) && coeff > 0) {
            // Normalisation sur 20 si le barème diffère
            const noteSur20 = bareme > 0 ? (noteVal / bareme) * 20.0 : noteVal;
            sommePonderee += noteSur20 * coeff;
            sommeCoefficients += coeff;
        }
    }

    if (sommeCoefficients === 0) return null;

    const moyenne = sommePonderee / sommeCoefficients;
    return Math.round(moyenne * 100) / 100;
}

/**
 * Attribue une mention officielle du conseil de classe selon la moyenne générale
 * @param {number} moyenne 
 * @returns {string}
 */
function determinerMention(moyenne) {
    if (moyenne === null || isNaN(moyenne)) return 'Non évalué';
    if (moyenne >= 16.0) return 'Félicitations du Conseil de classe';
    if (moyenne >= 14.0) return 'Tableau d\'Honneur';
    if (moyenne >= 12.0) return 'Encouragements';
    if (moyenne >= 10.0) return 'Résultats convenables';
    if (moyenne >= 8.0) return 'Avertissement travail - Des efforts sont attendus';
    return 'Insuffisant - Risque de non validation';
}

/**
 * Génère une référence unique pour un reçu de paiement
 */
function genererReferencePaiement() {
    const timestamp = Date.now().toString().slice(-6);
    const random = Math.floor(1000 + Math.random() * 9000);
    return `PAY-${timestamp}-${random}`;
}

/**
 * Formate un nombre au format monétaire lisible
 */
function formaterMontant(montant) {
    const num = parseFloat(montant) || 0;
    return new Intl.NumberFormat('fr-FR', {
        style: 'currency',
        currency: 'EUR'
    }).format(num);
}

module.exports = {
    calculerMoyennePonderee,
    determinerMention,
    genererReferencePaiement,
    formaterMontant
};
