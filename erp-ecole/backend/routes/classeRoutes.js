const express = require('express');
const router = express.Router();
const classeController = require('../controllers/classeController');
const { authenticateToken, requireRole } = require('../middlewares/authMiddleware');

router.use(authenticateToken);

router.get('/', classeController.getAllClasses);
router.get('/:id', classeController.getClasseById);
router.post('/', requireRole('ADMIN'), classeController.createClasse);
router.put('/:id', requireRole('ADMIN'), classeController.updateClasse);
router.delete('/:id', requireRole('ADMIN'), classeController.deleteClasse);

module.exports = router;
