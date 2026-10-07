const express = require('express');
const router = express.Router();
const noteController = require('../controllers/noteController');
const { authenticateToken, requireRole } = require('../middlewares/authMiddleware');

router.use(authenticateToken);

router.get('/', noteController.getAllNotes);
router.get('/moyennes-classe', requireRole('ADMIN', 'PROFESSEUR'), noteController.getMoyennesClasse);
router.post('/lot', requireRole('ADMIN', 'PROFESSEUR'), noteController.saisirNotesGroupees);
router.post('/', requireRole('ADMIN', 'PROFESSEUR'), noteController.createNote);
router.put('/:id', requireRole('ADMIN', 'PROFESSEUR'), noteController.updateNote);
router.delete('/:id', requireRole('ADMIN', 'PROFESSEUR'), noteController.deleteNote);

module.exports = router;
