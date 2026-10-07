const express = require('express');
const router = express.Router();
const statistiqueController = require('../controllers/statistiqueController');
const { authenticateToken, requireRole } = require('../middlewares/authMiddleware');

router.use(authenticateToken);

router.get('/admin', requireRole('ADMIN'), statistiqueController.getDashboardAdmin);
router.get('/professeur', requireRole('PROFESSEUR', 'ADMIN'), statistiqueController.getDashboardProfesseur);
router.get('/eleve', requireRole('ELEVE'), statistiqueController.getDashboardEleve);

module.exports = router;
