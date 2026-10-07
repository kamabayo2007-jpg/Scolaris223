const { pool } = require('../config/database');
const { calculerMoyennePonderee } = require('../utils/helpers');

/**
 * Récupère les notes selon les filtres (classe, matière, période, élève)
 * GET /api/notes
 */
async function getAllNotes(req, res, next) {
    try {
        const { classe_id, matiere_id, periode_id, eleve_id } = req.query;

        let query = `
            SELECT n.id, n.eleve_id, n.matiere_id, n.professeur_id, n.periode_id,
                   n.titre_evaluation, n.note, n.bareme, n.coefficient, n.date_evaluation, n.commentaire,
                   e.nom AS nom_eleve, e.prenom AS prenom_eleve, e.matricule, e.classe_id,
                   c.nom AS nom_classe, m.nom AS nom_matiere, m.code AS code_matiere,
                   p.nom AS nom_prof, p.prenom AS prenom_prof, per.nom AS nom_periode
            FROM notes n
            INNER JOIN eleves e ON n.eleve_id = e.id
            INNER JOIN classes c ON e.classe_id = c.id
            INNER JOIN matieres m ON n.matiere_id = m.id
            INNER JOIN professeurs p ON n.professeur_id = p.id
            INNER JOIN periodes per ON n.periode_id = per.id
            WHERE 1=1
        `;
        const params = [];

        // Sécurité rôle : L'élève ne voit que ses notes
        if (req.user.role === 'ELEVE') {
            query += ` AND n.eleve_id = ?`;
            params.push(req.user.eleve_id);
        } else if (eleve_id) {
            query += ` AND n.eleve_id = ?`;
            params.push(eleve_id);
        }

        if (classe_id) {
            query += ` AND e.classe_id = ?`;
            params.push(classe_id);
        }

        if (matiere_id) {
            query += ` AND n.matiere_id = ?`;
            params.push(matiere_id);
        }

        if (periode_id) {
            query += ` AND n.periode_id = ?`;
            params.push(periode_id);
        }

        query += ` ORDER BY n.date_evaluation DESC, e.nom ASC`;

        const [notes] = await pool.query(query, params);

        res.json({
            succes: true,
            total: notes.length,
            notes
        });
    } catch (error) {
        next(error);
    }
}

/**
 * Enregistrement d'une note d'évaluation
 * POST /api/notes
 */
async function createNote(req, res, next) {
    try {
        const {
            eleve_id, matiere_id, periode_id, titre_evaluation,
            note, bareme = 20.0, coefficient = 1.0, date_evaluation, commentaire
        } = req.body;

        const noteVal = parseFloat(note);
        const baremeVal = parseFloat(bareme) || 20.0;
        const coeffVal = parseFloat(coefficient) || 1.0;

        if (isNaN(noteVal) || noteVal < 0 || noteVal > baremeVal) {
            return res.status(400).json({
                succes: false,
                message: `La note saisie (${note}) est invalide. Elle doit être comprise entre 0 et le barème (${baremeVal}).`
            });
        }

        if (!eleve_id || !matiere_id || !periode_id) {
            return res.status(400).json({
                succes: false,
                message: 'Veuillez spécifier l\'élève, la matière et la période scolaire.'
            });
        }

        // Déterminer le professeur_id qui saisit la note
        let professeurId = null;
        if (req.user.role === 'PROFESSEUR') {
            professeurId = req.user.professeur_id;

            // Vérifier que ce professeur est affecté à la classe de l'élève pour cette matière
            const [eleveClasse] = await pool.query('SELECT classe_id FROM eleves WHERE id = ?', [eleve_id]);
            if (eleveClasse.length === 0) {
                return res.status(404).json({ succes: false, message: 'Élève introuvable.' });
            }

            const classeId = eleveClasse[0].classe_id;
            const [autorise] = await pool.query(
                'SELECT id FROM enseignements WHERE professeur_id = ? AND matiere_id = ? AND classe_id = ? LIMIT 1',
                [professeurId, matiere_id, classeId]
            );

            if (autorise.length === 0) {
                return res.status(403).json({
                    succes: false,
                    message: 'Accès refusé : vous n\'enseignez pas cette matière dans la classe de cet élève.'
                });
            }
        } else if (req.user.role === 'ADMIN') {
            // L'administrateur peut attribuer la note au professeur titulaire ou spécifié
            professeurId = req.body.professeur_id;
            if (!professeurId) {
                const [titulaire] = await pool.query(
                    'SELECT professeur_id FROM enseignements WHERE matiere_id = ? LIMIT 1',
                    [matiere_id]
                );
                professeurId = titulaire[0]?.professeur_id || 1;
            }
        } else {
            return res.status(403).json({
                succes: false,
                message: 'Un élève ne peut pas saisir de notes.'
            });
        }

        const dateEval = date_evaluation || new Date().toISOString().split('T')[0];

        const [result] = await pool.query(`
            INSERT INTO notes (eleve_id, matiere_id, professeur_id, periode_id, titre_evaluation,
                               note, bareme, coefficient, date_evaluation, commentaire)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        `, [eleve_id, matiere_id, professeurId, periode_id, titre_evaluation || 'Contrôle continu',
            noteVal, baremeVal, coeffVal, dateEval, commentaire || null]);

        // Créer une notification pour l'élève
        const [eleveUser] = await pool.query('SELECT user_id FROM eleves WHERE id = ?', [eleve_id]);
        if (eleveUser.length > 0) {
            const [mat] = await pool.query('SELECT nom FROM matieres WHERE id = ?', [matiere_id]);
            await pool.query(`
                INSERT INTO notifications (user_id, titre, message, type)
                VALUES (?, ?, ?, 'INFO')
            `, [eleveUser[0].user_id, `Nouvelle note : ${mat[0]?.nom}`,
                `Une note de ${noteVal}/${baremeVal} a été enregistrée (${titre_evaluation || 'Évaluation'}).`]);
        }

        res.status(201).json({
            succes: true,
            message: 'Note enregistrée avec succès.',
            note_id: result.insertId
        });
    } catch (error) {
        next(error);
    }
}

/**
 * Mise à jour d'une note
 * PUT /api/notes/:id
 */
async function updateNote(req, res, next) {
    try {
        const { id } = req.params;
        const { note, coefficient, commentaire, titre_evaluation } = req.body;

        const [existing] = await pool.query('SELECT * FROM notes WHERE id = ? LIMIT 1', [id]);
        if (existing.length === 0) {
            return res.status(404).json({ succes: false, message: 'Note introuvable.' });
        }

        // Si professeur, vérifier qu'il est l'auteur de la note
        if (req.user.role === 'PROFESSEUR' && existing[0].professeur_id !== req.user.professeur_id) {
            return res.status(403).json({
                succes: false,
                message: 'Accès refusé : vous ne pouvez modifier que les notes que vous avez saisies.'
            });
        }

        const noteVal = note !== undefined ? parseFloat(note) : existing[0].note;
        if (noteVal < 0 || noteVal > existing[0].bareme) {
            return res.status(400).json({
                succes: false,
                message: `La note doit être comprise entre 0 et ${existing[0].bareme}.`
            });
        }

        await pool.query(`
            UPDATE notes SET
                note = COALESCE(?, note),
                coefficient = COALESCE(?, coefficient),
                commentaire = COALESCE(?, commentaire),
                titre_evaluation = COALESCE(?, titre_evaluation)
            WHERE id = ?
        `, [noteVal, coefficient, commentaire, titre_evaluation, id]);

        res.json({
            succes: true,
            message: 'Note mise à jour avec succès.'
        });
    } catch (error) {
        next(error);
    }
}

/**
 * Suppression d'une note
 * DELETE /api/notes/:id
 */
async function deleteNote(req, res, next) {
    try {
        const { id } = req.params;

        const [existing] = await pool.query('SELECT * FROM notes WHERE id = ? LIMIT 1', [id]);
        if (existing.length === 0) {
            return res.status(404).json({ succes: false, message: 'Note introuvable.' });
        }

        if (req.user.role === 'PROFESSEUR' && existing[0].professeur_id !== req.user.professeur_id) {
            return res.status(403).json({
                succes: false,
                message: 'Accès refusé : vous ne pouvez supprimer que vos propres notes.'
            });
        }

        await pool.query('DELETE FROM notes WHERE id = ?', [id]);

        res.json({
            succes: true,
            message: 'Note supprimée avec succès.'
        });
    } catch (error) {
        next(error);
    }
}

/**
 * Calcul et synthèse des moyennes d'une classe pour une période
 * GET /api/notes/moyennes-classe
 */
async function getMoyennesClasse(req, res, next) {
    try {
        const { classe_id, periode_id } = req.query;

        if (!classe_id || !periode_id) {
            return res.status(400).json({
                succes: false,
                message: 'Veuillez spécifier la classe et la période.'
            });
        }

        // Élèves de la classe
        const [eleves] = await pool.query(
            'SELECT id, matricule, nom, prenom FROM eleves WHERE classe_id = ? AND actif = TRUE ORDER BY nom, prenom',
            [classe_id]
        );

        // Matières enseignées dans cette classe
        const [matieres] = await pool.query(`
            SELECT DISTINCT m.id, m.nom, m.code, m.coefficient
            FROM matieres m
            INNER JOIN enseignements ens ON m.id = ens.matiere_id
            WHERE ens.classe_id = ?
        `, [classe_id]);

        // Notes de la classe sur la période
        const [toutesLesNotes] = await pool.query(`
            SELECT n.eleve_id, n.matiere_id, n.note, n.bareme, n.coefficient
            FROM notes n
            INNER JOIN eleves e ON n.eleve_id = e.id
            WHERE e.classe_id = ? AND n.periode_id = ?
        `, [classe_id, periode_id]);

        // Calculs par élève
        const resultatEleves = eleves.map(eleve => {
            const notesEleve = toutesLesNotes.filter(n => n.eleve_id === eleve.id);
            const moyenneGen = calculerMoyennePonderee(notesEleve);

            // Moyennes par matière
            const moyennesMatieres = {};
            matieres.forEach(m => {
                const notesMatiere = notesEleve.filter(n => n.matiere_id === m.id);
                moyennesMatieres[m.id] = {
                    nom: m.nom,
                    code: m.code,
                    moyenne: calculerMoyennePonderee(notesMatiere),
                    nombre_evaluations: notesMatiere.length
                };
            });

            return {
                eleve_id: eleve.id,
                matricule: eleve.matricule,
                nom_complet: `${eleve.nom} ${eleve.prenom}`,
                moyenne_generale: moyenneGen,
                moyennes_matieres: moyennesMatieres
            };
        });

        // Calcul du classement
        const elevesClasses = [...resultatEleves]
            .filter(e => e.moyenne_generale !== null)
            .sort((a, b) => b.moyenne_generale - a.moyenne_generale);

        resultatEleves.forEach(e => {
            if (e.moyenne_generale !== null) {
                const rang = elevesClasses.findIndex(x => x.eleve_id === e.eleve_id) + 1;
                e.rang = rang;
            } else {
                e.rang = null;
            }
        });

        // Moyenne générale de la classe
        const moyennesValides = resultatEleves.filter(e => e.moyenne_generale !== null).map(e => e.moyenne_generale);
        const moyenneClasse = moyennesValides.length > 0
            ? Math.round((moyennesValides.reduce((acc, v) => acc + v, 0) / moyennesValides.length) * 100) / 100
            : null;

        res.json({
            succes: true,
            classe_id: parseInt(classe_id, 10),
            periode_id: parseInt(periode_id, 10),
            effectif: eleves.length,
            moyenne_classe: moyenneClasse,
            matieres,
            resultats: resultatEleves
        });
    } catch (error) {
        next(error);
    }
}

/**
 * Saisie dynamique en lot de plusieurs notes par élève pour une classe et matière
 * POST /api/notes/lot
 */
async function saisirNotesGroupees(req, res, next) {
    const connection = await pool.getConnection();
    try {
        await connection.beginTransaction();

        const {
            classe_id,
            matiere_id,
            periode_id = 1,
            evaluations
            // evaluations = [
            //   {
            //     id: null (ou note_id si modification),
            //     titre_evaluation: "DS 1",
            //     bareme: 20,
            //     coefficient: 2,
            //     date_evaluation: "2025-10-10",
            //     notes_eleves: [
            //       { eleve_id: 1, note: 15.5, commentaire: "Bon travail" },
            //       { eleve_id: 2, note: 18, commentaire: "Excellent" }
            //     ]
            //   }
            // ]
        } = req.body;

        if (!classe_id || !matiere_id || !Array.isArray(evaluations) || evaluations.length === 0) {
            await connection.rollback();
            return res.status(400).json({
                succes: false,
                message: 'Veuillez spécifier la classe, la matière et au moins une évaluation avec les notes.'
            });
        }

        // Vérification des droits enseignant
        let professeurId = null;
        if (req.user.role === 'PROFESSEUR') {
            professeurId = req.user.professeur_id;
            const [autorise] = await connection.query(
                'SELECT id FROM enseignements WHERE professeur_id = ? AND matiere_id = ? AND classe_id = ? LIMIT 1',
                [professeurId, matiere_id, classe_id]
            );
            if (autorise.length === 0) {
                await connection.rollback();
                return res.status(403).json({
                    succes: false,
                    message: 'Accès refusé : vous n\'êtes pas l\'enseignant affecté à cette classe pour cette matière.'
                });
            }
        } else if (req.user.role === 'ADMIN') {
            const [titulaire] = await connection.query(
                'SELECT professeur_id FROM enseignements WHERE matiere_id = ? AND classe_id = ? LIMIT 1',
                [matiere_id, classe_id]
            );
            professeurId = titulaire[0]?.professeur_id || 1;
        } else {
            await connection.rollback();
            return res.status(403).json({ succes: false, message: 'Rôle non autorisé à saisir des notes.' });
        }

        let totalNotesEnregistrees = 0;

        for (const evalItem of evaluations) {
            const titre = evalItem.titre_evaluation || 'Évaluation';
            const bareme = parseFloat(evalItem.bareme) || 20.0;
            const coeff = parseFloat(evalItem.coefficient) || 1.0;
            const dateEval = evalItem.date_evaluation || new Date().toISOString().split('T')[0];

            if (Array.isArray(evalItem.notes_eleves)) {
                for (const ne of evalItem.notes_eleves) {
                    if (ne.note === '' || ne.note === null || ne.note === undefined) {
                        continue; // Note non saisie, passer
                    }

                    const noteVal = parseFloat(ne.note);
                    if (isNaN(noteVal) || noteVal < 0 || noteVal > bareme) {
                        await connection.rollback();
                        return res.status(400).json({
                            succes: false,
                            message: `La note ${ne.note} pour l'élève #${ne.eleve_id} est invalide (doit être comprise entre 0 et ${bareme}).`
                        });
                    }

                    if (ne.note_id) {
                        // Mise à jour d'une note existante
                        await connection.query(`
                            UPDATE notes SET
                                note = ?,
                                bareme = ?,
                                coefficient = ?,
                                titre_evaluation = ?,
                                date_evaluation = ?,
                                commentaire = ?
                            WHERE id = ? AND eleve_id = ?
                        `, [noteVal, bareme, coeff, titre, dateEval, ne.commentaire || null, ne.note_id, ne.eleve_id]);
                    } else {
                        // Insertion d'une nouvelle note
                        await connection.query(`
                            INSERT INTO notes (eleve_id, matiere_id, professeur_id, periode_id,
                                               titre_evaluation, note, bareme, coefficient, date_evaluation, commentaire)
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        `, [ne.eleve_id, matiere_id, professeurId, periode_id, titre,
                            noteVal, bareme, coeff, dateEval, ne.commentaire || null]);
                    }
                    totalNotesEnregistrees++;
                }
            }
        }

        await connection.commit();

        res.json({
            succes: true,
            message: `${totalNotesEnregistrees} note(s) enregistrée(s) avec succès pour la classe.`,
            total_notes: totalNotesEnregistrees
        });
    } catch (error) {
        await connection.rollback();
        next(error);
    } finally {
        connection.release();
    }
}

module.exports = {
    getAllNotes,
    createNote,
    updateNote,
    deleteNote,
    getMoyennesClasse,
    saisirNotesGroupees
};
