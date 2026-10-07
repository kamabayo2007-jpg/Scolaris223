const express = require('express');
const router = express.Router();
const paymentGatewayController = require('../controllers/paymentGatewayController');
const { authenticateToken } = require('../middlewares/authMiddleware');
const { tenantMiddleware } = require('../middlewares/tenantMiddleware');

// Routes protégées ou accessibles avec isolation multi-tenant
router.post('/initier', tenantMiddleware, paymentGatewayController.initierPaiementMobile);
router.post('/valider', tenantMiddleware, paymentGatewayController.validerPaiementMobile);
router.post('/local', tenantMiddleware, paymentGatewayController.enregistrerPaiementLocal);
router.post('/subscription', tenantMiddleware, paymentGatewayController.payerSouscriptionEcole);

module.exports = router;
