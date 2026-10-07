const express = require('express');
const router = express.Router();
const paiementController = require('../controllers/paiementController');
const { authenticateToken, requireRole } = require('../middlewares/authMiddleware');

router.use(authenticateToken);

router.get('/', paiementController.getAllPaiements);
router.get('/eleve/:eleveId/solde', paiementController.getSoldeEleve);
router.post('/', requireRole('ADMIN'), paiementController.createPaiement);
router.delete('/:id', requireRole('ADMIN'), paiementController.deletePaiement);

module.exports = router;
