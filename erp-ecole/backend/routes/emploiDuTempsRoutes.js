const express = require('express');
const router = express.Router();
const emploiDuTempsController = require('../controllers/emploiDuTempsController');
const { authenticateToken, requireRole } = require('../middlewares/authMiddleware');

router.use(authenticateToken);

router.get('/mon-planning', emploiDuTempsController.getMonPlanning);
router.get('/classe/:classeId', emploiDuTempsController.getParClasse);
router.post('/', requireRole('ADMIN'), emploiDuTempsController.createSlot);
router.delete('/:id', requireRole('ADMIN'), emploiDuTempsController.deleteSlot);

module.exports = router;
