const express = require('express');
const router = express.Router();
const schoolController = require('../controllers/schoolController');
const { tenantMiddleware } = require('../middlewares/tenantMiddleware');

router.get('/', tenantMiddleware, schoolController.getAllSchools);
router.get('/:id', tenantMiddleware, schoolController.getSchoolById);
router.post('/', tenantMiddleware, schoolController.createSchool);
router.get('/:id/subscriptions', tenantMiddleware, schoolController.getSchoolSubscriptions);

module.exports = router;
