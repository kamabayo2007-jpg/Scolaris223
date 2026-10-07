const express = require('express');
const router = express.Router();
const bulletinController = require('../controllers/bulletinController');
const { authenticateToken } = require('../middlewares/authMiddleware');

router.use(authenticateToken);

router.get('/eleve/:eleveId', bulletinController.genererBulletin);

module.exports = router;
