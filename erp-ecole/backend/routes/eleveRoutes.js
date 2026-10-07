const express = require('express');
const router = express.Router();
const eleveController = require('../controllers/eleveController');
const { authenticateToken, requireRole } = require('../middlewares/authMiddleware');

router.use(authenticateToken);

router.get('/', requireRole('ADMIN', 'PROFESSEUR'), eleveController.getAllEleves);
router.get('/:id', eleveController.getEleveById);
router.post('/', requireRole('ADMIN'), eleveController.createEleve);
router.put('/:id', requireRole('ADMIN'), eleveController.updateEleve);
router.delete('/:id', requireRole('ADMIN'), eleveController.deleteEleve);

module.exports = router;
