const express = require('express');
const router = express.Router();
const matiereController = require('../controllers/matiereController');
const { authenticateToken, requireRole } = require('../middlewares/authMiddleware');

router.use(authenticateToken);

router.get('/', matiereController.getAllMatieres);
router.post('/', requireRole('ADMIN'), matiereController.createMatiere);
router.put('/:id', requireRole('ADMIN'), matiereController.updateMatiere);
router.delete('/:id', requireRole('ADMIN'), matiereController.deleteMatiere);

module.exports = router;
