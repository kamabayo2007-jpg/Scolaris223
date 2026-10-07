const express = require('express');
const router = express.Router();
const absenceController = require('../controllers/absenceController');
const { authenticateToken, requireRole } = require('../middlewares/authMiddleware');

router.use(authenticateToken);

router.get('/', absenceController.getAllAbsences);
router.post('/appel', requireRole('ADMIN', 'PROFESSEUR'), absenceController.enregistrerAppel);
router.put('/:id/justifier', requireRole('ADMIN', 'PROFESSEUR'), absenceController.justifierAbsence);
router.delete('/:id', requireRole('ADMIN'), absenceController.deleteAbsence);

module.exports = router;
