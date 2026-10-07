const { pool } = require('../config/database');
const { calculerMoyennePonderee, determinerMention } = require('../utils/helpers');

/**
 * Génère le bulletin officiel complet d'un élève pour une période donnée
 * GET /api/bulletins/eleve/:eleveId
 */
async function genererBulletin(req, res, next) {
    try {
        const { eleveId } = req.params;
        const { periode_id } = req.query;

        // Sécurité rôle : Un élève ne peut générer et voir que son propre bulletin
        if (req.user.role === 'ELEVE' && req.user.eleve_id !== parseInt(eleveId, 10)) {
            return res.status(403).json({
                succes: false,
                message: 'Accès refusé : vous ne pouvez consulter que votre propre bulletin scolaire.'
            });
        }

        // 1. Informations de l'élève et de sa classe
        const [eleves] = await pool.query(`
            SELECT e.*, c.nom AS nom_classe, c.niveau, ans.id AS annee_id, ans.libelle AS annee_scolaire
            FROM eleves e
            INNER JOIN classes c ON e.classe_id = c.id
            INNER JOIN annees_scolaires ans ON c.annee_scolaire_id = ans.id
            WHERE e.id = ? LIMIT 1
        `, [eleveId]);

        if (eleves.length === 0) {
            return res.status(404).json({ succes: false, message: 'Élève introuvable ou non affecté à une classe.' });
        }

        const eleve = eleves[0];

        // 2. Déterminer la période
        let periodeId = periode_id;
        if (!periodeId) {
            const [activePeriode] = await pool.query(
                'SELECT id, nom FROM periodes WHERE annee_scolaire_id = ? AND active = TRUE LIMIT 1',
                [eleve.annee_id]
            );
            periodeId = activePeriode[0]?.id || 1;
        }

        const [periodesInfo] = await pool.query('SELECT * FROM periodes WHERE id = ?', [periodeId]);
        const periode = periodesInfo[0] || { nom: 'Trimestre 1' };

        // 3. Toutes les matières enseignées dans la classe
        const [matieres] = await pool.query(`
            SELECT DISTINCT m.id, m.code, m.nom, m.coefficient,
                   p.nom AS nom_prof, p.prenom AS prenom_prof
            FROM matieres m
            INNER JOIN enseignements ens ON m.id = ens.matiere_id
            LEFT JOIN professeurs p ON ens.professeur_id = p.id
            WHERE ens.classe_id = ?
            ORDER BY m.nom ASC
        `, [eleve.classe_id]);

        // 4. Notes de l'élève pour cette période
        const [notesEleve] = await pool.query(`
            SELECT n.matiere_id, n.note, n.bareme, n.coefficient, n.commentaire
            FROM notes n
            WHERE n.eleve_id = ? AND n.periode_id = ?
        `, [eleveId, periodeId]);

        // 5. Notes de toute la classe pour calculer moyennes min/max/classe
        const [toutesNotesClasse] = await pool.query(`
            SELECT n.eleve_id, n.matiere_id, n.note, n.bareme, n.coefficient
            FROM notes n
            INNER JOIN eleves e ON n.eleve_id = e.id
            WHERE e.classe_id = ? AND n.periode_id = ?
        `, [eleve.classe_id, periodeId]);

        // Calculs détaillés par matière
        const detailMatieres = [];
        let totalPointsEleve = 0;
        let totalCoeffEleve = 0;

        for (const mat of matieres) {
            const notesMatiere = notesEleve.filter(n => n.matiere_id === mat.id);
            const notesClasseMatiere = toutesNotesClasse.filter(n => n.matiere_id === mat.id);

            const moyEleve = calculerMoyennePonderee(notesMatiere);
            const coeff = parseFloat(mat.coefficient) || 1.0;

            if (moyEleve !== null) {
                totalPointsEleve += moyEleve * coeff;
                totalCoeffEleve += coeff;
            }

            // Calcul min, max, moyenne classe pour cette matière
            const notesElevesClasse = {};
            notesClasseMatiere.forEach(n => {
                if (!notesElevesClasse[n.eleve_id]) notesElevesClasse[n.eleve_id] = [];
                notesElevesClasse[n.eleve_id].push(n);
            });

            const moyennesTous = Object.values(notesElevesClasse)
                .map(notes => calculerMoyennePonderee(notes))
                .filter(m => m !== null);

            const moyClasseMatiere = moyennesTous.length > 0
                ? Math.round((moyennesTous.reduce((a, b) => a + b, 0) / moyennesTous.length) * 100) / 100
                : null;
            const minClasse = moyennesTous.length > 0 ? Math.min(...moyennesTous) : null;
            const maxClasse = moyennesTous.length > 0 ? Math.max(...moyennesTous) : null;

            // Dernier commentaire de l'enseignant
            const dernierCommentaire = notesMatiere.map(n => n.commentaire).filter(Boolean).pop() || '';

            detailMatieres.push({
                matiere_id: mat.id,
                code: mat.code,
                nom: mat.nom,
                coefficient: coeff,
                professeur: `${mat.nom_prof || ''} ${mat.prenom_prof || ''}`.trim() || 'Enseignant',
                moyenne_eleve: moyEleve,
                moyenne_classe: moyClasseMatiere,
                note_min: minClasse,
                note_max: maxClasse,
                nombre_evaluations: notesMatiere.length,
                appreciation: dernierCommentaire || 'Travail régulier.'
            });
        }

        // Moyenne générale de l'élève
        const moyenneGenerale = totalCoeffEleve > 0
            ? Math.round((totalPointsEleve / totalCoeffEleve) * 100) / 100
            : null;

        // Calcul du classement dans la classe
        const [tousElevesClasse] = await pool.query(
            'SELECT id FROM eleves WHERE classe_id = ? AND actif = TRUE',
            [eleve.classe_id]
        );

        const classements = tousElevesClasse.map(e => {
            const notesDeCetEleve = toutesNotesClasse.filter(n => n.eleve_id === e.id);
            return {
                eleve_id: e.id,
                moyenne: calculerMoyennePonderee(notesDeCetEleve)
            };
        }).filter(e => e.moyenne !== null)
          .sort((a, b) => b.moyenne - a.moyenne);

        const rang = moyenneGenerale !== null
            ? classements.findIndex(c => c.eleve_id === parseInt(eleveId, 10)) + 1
            : null;

        const moyennesValidesClasse = classements.map(c => c.moyenne);
        const moyenneGeneraleClasse = moyennesValidesClasse.length > 0
            ? Math.round((moyennesValidesClasse.reduce((a, b) => a + b, 0) / moyennesValidesClasse.length) * 100) / 100
            : null;

        // 6. Statistiques d'absences pour la période
        const [absences] = await pool.query(`
            SELECT 
                COUNT(*) AS total_demi_journees,
                SUM(CASE WHEN type = 'ABSENCE' THEN 1 ELSE 0 END) AS total_absences,
                SUM(CASE WHEN type = 'ABSENCE' AND justifiee = FALSE THEN 1 ELSE 0 END) AS absences_non_justifiees,
                SUM(CASE WHEN type = 'RETARD' THEN 1 ELSE 0 END) AS total_retards
            FROM absences
            WHERE eleve_id = ?
        `, [eleveId]);

        const mention = determinerMention(moyenneGenerale);

        const bulletin = {
            etablissement: {
                nom: 'Lycée & Collège d\'Excellence Saint-Exupéry',
                academie: 'Académie de Paris',
                annee_scolaire: eleve.annee_scolaire,
                adresse: '15 Boulevard de l\'Éducation, 75005 Paris',
                telephone: '01 42 68 00 00',
                email: 'contact@lycee-saintexupery.fr'
            },
            periode: {
                id: periode.id,
                nom: periode.nom
            },
            eleve: {
                id: eleve.id,
                matricule: eleve.matricule,
                nom: eleve.nom,
                prenom: eleve.prenom,
                nom_complet: `${eleve.nom} ${eleve.prenom}`,
                date_naissance: eleve.date_naissance,
                classe: eleve.nom_classe,
                niveau: eleve.niveau,
                effectif_classe: tousElevesClasse.length
            },
            resultats_disciplines: detailMatieres,
            synthese: {
                moyenne_generale: moyenneGenerale,
                moyenne_classe: moyenneGeneraleClasse,
                rang,
                effectif: tousElevesClasse.length,
                total_coefficients: totalCoeffEleve,
                mention,
                assiduite: absences[0]
            },
            appreciation_generale: moyenneGenerale >= 14
                ? 'Excellent trimestre. Félicitations pour la rigueur du travail et la régularité des efforts.'
                : moyenneGenerale >= 10
                ? 'Trimestre satisfaisant dans l\'ensemble. Poursuivez vos efforts au prochain trimestre.'
                : 'Trimestre fragile. Une plus grande implication en classe et un travail personnel régulier sont indispensables.',
            date_generation: new Date().toLocaleDateString('fr-FR')
        };

        res.json({
            succes: true,
            bulletin
        });
    } catch (error) {
        next(error);
    }
}

module.exports = {
    genererBulletin
};
