const express = require('express');
const router = express.Router();
const backupController = require('../controllers/backupController');
const { tenantMiddleware } = require('../middlewares/tenantMiddleware');

router.get('/export/:schoolId', tenantMiddleware, backupController.exportSchoolBackup);
router.post('/restore', tenantMiddleware, backupController.restoreSchoolBackup);

module.exports = router;
