const express = require('express');
const router = express.Router();
const professeurController = require('../controllers/professeurController');
const { authenticateToken, requireRole } = require('../middlewares/authMiddleware');

router.use(authenticateToken);

router.get('/', requireRole('ADMIN', 'PROFESSEUR'), professeurController.getAllProfesseurs);
router.get('/:id', requireRole('ADMIN', 'PROFESSEUR'), professeurController.getProfesseurById);
router.post('/', requireRole('ADMIN'), professeurController.createProfesseur);
router.put('/:id', requireRole('ADMIN'), professeurController.updateProfesseur);
router.post('/:id/enseignements', requireRole('ADMIN'), professeurController.affecterEnseignement);
router.delete('/enseignements/:enseignementId', requireRole('ADMIN'), professeurController.retirerEnseignement);
router.delete('/:id', requireRole('ADMIN'), professeurController.deleteProfesseur);

module.exports = router;
