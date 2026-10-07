const express = require('express');
const router = express.Router();
const userController = require('../controllers/userController');
const { authenticateToken, requireRole } = require('../middlewares/authMiddleware');

router.use(authenticateToken);
router.use(requireRole('ADMIN'));

router.get('/', userController.getAllUsers);
router.patch('/:id/statut', userController.toggleUserStatus);
router.post('/:id/reinitialiser-mdp', userController.resetPassword);

module.exports = router;
