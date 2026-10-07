const express = require('express');
const router = express.Router();
const authController = require('../controllers/authController');
const { authenticateToken } = require('../middlewares/authMiddleware');

router.post('/login', authController.login);
router.post('/logout', authController.logout);
router.get('/me', authenticateToken, authController.getMe);
router.put('/mot-de-passe', authenticateToken, authController.changerMotDePasse);

module.exports = router;
