const { pool } = require('../config/database');
const PaymentGatewayService = require('../services/paymentGatewayService');

/**
 * Initialisation d'une transaction Mobile Money
 * POST /api/mobile-payments/initier
 */
async function initierPaiementMobile(req, res, next) {
    try {
        const { operateur, montant, telephone, eleve_id, type = 'SCOLARITE' } = req.body;
        const schoolId = req.school_id || 1;

        if (!operateur || !montant || !telephone) {
            return res.status(400).json({
                succes: false,
                message: 'Champs requis : operateur (ORANGE_MONEY, MOOV_MONEY, WAVE), montant en FCFA et telephone.'
            });
        }

        const resultat = await PaymentGatewayService.initierPaiementMobile({
            operateur,
            montant,
            telephone,
            eleveId: eleve_id,
            schoolId,
            typeFrais: type
        });

        res.status(200).json(resultat);
    } catch (error) {
        res.status(400).json({
            succes: false,
            message: error.message
        });
    }
}

/**
 * Validation côté serveur d'une transaction Mobile Money et enregistrement
 * POST /api/mobile-payments/valider
 */
async function validerPaiementMobile(req, res, next) {
    try {
        const {
            reference,
            code_otp,
            operateur,
            montant,
            eleve_id,
            telephone,
            type = 'SCOLARITE',
            commentaire
        } = req.body;

        const schoolId = req.school_id || 1;
        const montantNum = parseFloat(montant);

        // Validation serveur
        const validation = await PaymentGatewayService.validerTransaction({
            reference,
            codeOtp: code_otp,
            montantAttendu: montantNum
        });

        const today = new Date().toISOString().split('T')[0];
        const modePaiementDb = operateur ? operateur.toUpperCase() : 'ORANGE_MONEY';

        // Enregistrement en base dans la table multi-tenant 'paiements'
        const [result] = await pool.query(`
            INSERT INTO paiements (
                school_id, eleve_id, montant, devise, type, date_paiement, reference,
                mode_paiement, statut, numero_telephone_mobile, reference_operateur,
                est_paiement_local, commentaire, enregistre_par_user_id
            )
            VALUES (?, ?, ?, 'FCFA', ?, ?, ?, ?, 'PAYE', ?, ?, FALSE, ?, ?)
        `, [
            schoolId,
            eleve_id || 1,
            montantNum,
            type,
            today,
            reference,
            modePaiementDb,
            telephone || null,
            `TXN-${operateur}-${Date.now().toString().slice(-6)}`,
            commentaire || `Paiement ${modePaiementDb} validé côté serveur`,
            req.user ? req.user.id : null
        ]);

        // Audit log
        await pool.query(`
            INSERT INTO activity_logs (school_id, user_id, user_full_name, user_role, action_type, description, target_entity)
            VALUES (?, ?, ?, ?, 'PAYMENT', ?, ?)
        `, [
            schoolId,
            req.user ? req.user.id : null,
            req.user ? req.user.username : 'Système Paiement',
            req.user ? req.user.role : 'PASSERELLE',
            `Paiement Mobile Money validé : ${montantNum.toLocaleString('fr-FR')} FCFA via ${modePaiementDb}`,
            reference
        ]);

        res.status(201).json({
            succes: true,
            message: `Paiement Mobile Money de ${montantNum.toLocaleString('fr-FR')} FCFA validé avec succès.`,
            paiement_id: result.insertId,
            reference,
            recu: {
                reference,
                montant: montantNum,
                devise: 'FCFA',
                operateur: modePaiementDb,
                date: today,
                statut: 'PAYE'
            }
        });
    } catch (error) {
        next(error);
    }
}

/**
 * Enregistrement d'un paiement local (Espèces en guichet)
 * POST /api/mobile-payments/local
 */
async function enregistrerPaiementLocal(req, res, next) {
    try {
        const { eleve_id, montant, type = 'SCOLARITE', nom_payeur, commentaire } = req.body;
        const schoolId = req.school_id || 1;
        const montantNum = parseFloat(montant);

        if (isNaN(montantNum) || montantNum <= 0) {
            return res.status(400).json({
                succes: false,
                message: 'Montant en Franc CFA invalide.'
            });
        }

        const datePaiement = new Date().toISOString().split('T')[0];
        const referenceLocale = `LOC-CFA-${Date.now().toString().slice(-6)}`;

        const [result] = await pool.query(`
            INSERT INTO paiements (
                school_id, eleve_id, montant, devise, type, date_paiement, reference,
                mode_paiement, statut, est_paiement_local, commentaire, enregistre_par_user_id
            )
            VALUES (?, ?, ?, 'FCFA', ?, ?, ?, 'ESPECES', 'PAYE', TRUE, ?, ?)
        `, [
            schoolId,
            eleve_id || 1,
            montantNum,
            type,
            datePaiement,
            referenceLocale,
            commentaire || `Paiement guichet espèces versé par ${nom_payeur || 'Parent'}`,
            req.user ? req.user.id : null
        ]);

        // Audit log
        await pool.query(`
            INSERT INTO activity_logs (school_id, user_id, user_full_name, user_role, action_type, description, target_entity)
            VALUES (?, ?, ?, ?, 'PAYMENT', ?, ?)
        `, [
            schoolId,
            req.user ? req.user.id : null,
            req.user ? req.user.username : 'Caissier',
            req.user ? req.user.role : 'COMPTABLE',
            `Versement local espèces enregistré : ${montantNum.toLocaleString('fr-FR')} FCFA`,
            referenceLocale
        ]);

        res.status(201).json({
            succes: true,
            message: `Versement espèces de ${montantNum.toLocaleString('fr-FR')} FCFA validé au guichet local.`,
            paiement_id: result.insertId,
            reference: referenceLocale,
            est_paiement_local: true,
            statut: 'PAYE'
        });
    } catch (error) {
        next(error);
    }
}

/**
 * Paiement de la souscription d'une école (Frais d'inscription & Frais annuel)
 * POST /api/mobile-payments/subscription
 */
async function payerSouscriptionEcole(req, res, next) {
    try {
        const { school_id, type_frais, montant, mode_paiement, telephone, notes } = req.body;
        const targetSchoolId = school_id || req.school_id || 1;
        const montantNum = parseFloat(montant);

        if (!type_frais || isNaN(montantNum) || montantNum <= 0) {
            return res.status(400).json({
                succes: false,
                message: 'Veuillez spécifier type_frais (FRAIS_INSCRIPTION ou FRAIS_ANNUEL) et un montant valide.'
            });
        }

        const referenceSub = `SUB-${mode_paiement || 'MOBILE'}-${Date.now().toString().slice(-6)}`;
        const validUntil = new Date();
        validUntil.setFullYear(validUntil.getFullYear() + 1);
        const validUntilStr = validUntil.toISOString().split('T')[0];

        const [result] = await pool.query(`
            INSERT INTO school_subscriptions (
                school_id, type_frais, montant, devise, mode_paiement,
                reference_transaction, telephone_payeur, statut, date_validite_fin, notes
            )
            VALUES (?, ?, ?, 'FCFA', ?, ?, ?, 'VALIDE', ?, ?)
        `, [
            targetSchoolId,
            type_frais,
            montantNum,
            mode_paiement || 'ORANGE_MONEY',
            referenceSub,
            telephone || null,
            validUntilStr,
            notes || `Renouvellement ${type_frais}`
        ]);

        // Mettre à jour le statut de l'école
        await pool.query(`
            UPDATE schools 
            SET statut_souscription = 'ACTIF', date_expiration = ?
            WHERE id = ?
        `, [validUntilStr, targetSchoolId]);

        res.status(201).json({
            succes: true,
            message: `Souscription ${type_frais} de ${montantNum.toLocaleString('fr-FR')} FCFA validée avec succès.`,
            subscription_id: result.insertId,
            reference: referenceSub,
            date_expiration: validUntilStr,
            statut: 'VALIDE'
        });
    } catch (error) {
        next(error);
    }
}

module.exports = {
    initierPaiementMobile,
    validerPaiementMobile,
    enregistrerPaiementLocal,
    payerSouscriptionEcole
};
