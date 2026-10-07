const { pool } = require('../config/database');
const { calculerMoyennePonderee } = require('../utils/helpers');

/**
 * Statistiques complètes pour le Dashboard Administrateur
 * GET /api/statistiques/admin
 */
async function getDashboardAdmin(req, res, next) {
    try {
        // Effectifs
        const [elevesCount] = await pool.query('SELECT COUNT(*) AS total FROM eleves WHERE actif = TRUE');
        const [profsCount] = await pool.query('SELECT COUNT(*) AS total FROM professeurs');
        const [classesCount] = await pool.query('SELECT COUNT(*) AS total FROM classes');
        const [matieresCount] = await pool.query('SELECT COUNT(*) AS total FROM matieres');

        // Finances
        const [finances] = await pool.query(`
            SELECT 
                COALESCE(SUM(c.frais_scolarite), 0) AS total_attendu_theorique,
                (SELECT COALESCE(SUM(montant), 0) FROM paiements WHERE statut = 'PAYE') AS total_encaisse
            FROM eleves e
            INNER JOIN classes c ON e.classe_id = c.id
            WHERE e.actif = TRUE
        `);

        const totalAttendu = parseFloat(finances[0].total_attendu_theorique) || 0;
        const totalEncaisse = parseFloat(finances[0].total_encaisse) || 0;
        const soldeRestant = Math.max(0, totalAttendu - totalEncaisse);

        // Assiduité globale
        const [absencesStats] = await pool.query(`
            SELECT 
                COUNT(*) AS total_enregistrements,
                SUM(CASE WHEN type = 'ABSENCE' AND justifiee = FALSE THEN 1 ELSE 0 END) AS total_non_justifiees,
                SUM(CASE WHEN type = 'ABSENCE' THEN 1 ELSE 0 END) AS total_absences,
                SUM(CASE WHEN type = 'RETARD' THEN 1 ELSE 0 END) AS total_retards
            FROM absences
        `);

        const totalEnr = absencesStats[0].total_enregistrements;
        const totalAbs = absencesStats[0].total_absences;
        // Taux de présence approximatif
        const tauxPresence = totalEnr > 0 ? Math.max(70, Math.round(((totalEnr - totalAbs) / totalEnr) * 1000) / 10) : 95.0;

        // Moyenne générale globale de l'école
        const [toutesNotes] = await pool.query('SELECT note, bareme, coefficient FROM notes');
        const moyenneGlobale = calculerMoyennePonderee(toutesNotes);

        // Dernières inscriptions
        const [dernieresInscriptions] = await pool.query(`
            SELECT e.id, e.nom, e.prenom, e.matricule, e.created_at, c.nom AS nom_classe
            FROM eleves e
            LEFT JOIN classes c ON e.classe_id = c.id
            ORDER BY e.created_at DESC LIMIT 5
        `);

        // Derniers paiements
        const [derniersPaiements] = await pool.query(`
            SELECT p.id, p.montant, p.type, p.date_paiement, p.reference, p.statut,
                   e.nom AS nom_eleve, e.prenom AS prenom_eleve, c.nom AS nom_classe
            FROM paiements p
            INNER JOIN eleves e ON p.eleve_id = e.id
            LEFT JOIN classes c ON e.classe_id = c.id
            ORDER BY p.date_paiement DESC, p.id DESC LIMIT 5
        `);

        // Alertes (Absences non justifiées récentes)
        const [alertesAbsences] = await pool.query(`
            SELECT a.id, a.date, a.creneau_horaire, e.nom AS nom_eleve, e.prenom AS prenom_eleve, c.nom AS nom_classe
            FROM absences a
            INNER JOIN eleves e ON a.eleve_id = e.id
            LEFT JOIN classes c ON e.classe_id = c.id
            WHERE a.type = 'ABSENCE' AND a.justifiee = FALSE
            ORDER BY a.date DESC LIMIT 5
        `);

        res.json({
            succes: true,
            statistiques: {
                effectifs: {
                    total_eleves: elevesCount[0].total,
                    total_professeurs: profsCount[0].total,
                    total_classes: classesCount[0].total,
                    total_matieres: matieresCount[0].total
                },
                finances: {
                    total_attendu: totalAttendu,
                    total_encaisse: totalEncaisse,
                    reste_a_recouvrer: soldeRestant,
                    taux_recouvrement: totalAttendu > 0 ? Math.round((totalEncaisse / totalAttendu) * 100) : 0
                },
                pedagogie: {
                    moyenne_generale_ecole: moyenneGlobale,
                    taux_presence: tauxPresence,
                    absences_non_justifiees: absencesStats[0].total_non_justifiees
                },
                recents: {
                    inscriptions: dernieresInscriptions,
                    paiements: derniersPaiements,
                    alertes: alertesAbsences
                }
            }
        });
    } catch (error) {
        next(error);
    }
}

/**
 * Statistiques pour le Dashboard Professeur
 * GET /api/statistiques/professeur
 */
async function getDashboardProfesseur(req, res, next) {
    try {
        const profId = req.user.professeur_id;

        // Classes et matières enseignées
        const [enseignements] = await pool.query(`
            SELECT ens.classe_id, c.nom AS nom_classe, ens.matiere_id, m.nom AS nom_matiere,
                   (SELECT COUNT(*) FROM eleves e WHERE e.classe_id = ens.classe_id AND e.actif = TRUE) AS effectif_classe
            FROM enseignements ens
            INNER JOIN classes c ON ens.classe_id = c.id
            INNER JOIN matieres m ON ens.matiere_id = m.id
            WHERE ens.professeur_id = ?
        `, [profId]);

        // Total d'élèves uniques sous la responsabilité de ce professeur
        const [totalEleves] = await pool.query(`
            SELECT COUNT(DISTINCT e.id) AS total
            FROM eleves e
            INNER JOIN enseignements ens ON e.classe_id = ens.classe_id
            WHERE ens.professeur_id = ? AND e.actif = TRUE
        `, [profId]);

        // Dernières notes saisies par ce professeur
        const [dernieresNotes] = await pool.query(`
            SELECT n.id, n.titre_evaluation, n.note, n.bareme, n.coefficient, n.date_evaluation,
                   e.nom AS nom_eleve, e.prenom AS prenom_eleve, m.nom AS nom_matiere, c.nom AS nom_classe
            FROM notes n
            INNER JOIN eleves e ON n.eleve_id = e.id
            INNER JOIN classes c ON e.classe_id = c.id
            INNER JOIN matieres m ON n.matiere_id = m.id
            WHERE n.professeur_id = ?
            ORDER BY n.date_evaluation DESC LIMIT 6
        `, [profId]);

        // Cours du jour de la semaine actuelle
        const joursSemaine = ['Dimanche', 'Lundi', 'Mardi', 'Mercredi', 'Jeudi', 'Vendredi', 'Samedi'];
        const jourActuel = joursSemaine[new Date().getDay()] || 'Lundi';

        const [coursDuJour] = await pool.query(`
            SELECT edt.*, m.nom AS nom_matiere, m.couleur_hex, c.nom AS nom_classe
            FROM emplois_du_temps edt
            INNER JOIN matieres m ON edt.matiere_id = m.id
            INNER JOIN classes c ON edt.classe_id = c.id
            WHERE edt.professeur_id = ? AND edt.jour = ?
            ORDER BY edt.heure_debut ASC
        `, [profId, jourActuel]);

        res.json({
            succes: true,
            professeur: {
                total_eleves: totalEleves[0].total,
                nombre_classes: new Set(enseignements.map(e => e.classe_id)).size,
                enseignements,
                cours_aujourdhui: coursDuJour,
                dernieres_notes: dernieresNotes
            }
        });
    } catch (error) {
        next(error);
    }
}

/**
 * Statistiques pour le Dashboard Élève
 * GET /api/statistiques/eleve
 */
async function getDashboardEleve(req, res, next) {
    try {
        const eleveId = req.user.eleve_id;

        // Informations générales et classe
        const [eleve] = await pool.query(`
            SELECT e.*, c.nom AS nom_classe, c.frais_scolarite 
            FROM eleves e
            LEFT JOIN classes c ON e.classe_id = c.id
            WHERE e.id = ? LIMIT 1
        `, [eleveId]);

        if (eleve.length === 0) {
            return res.status(404).json({ succes: false, message: 'Élève introuvable.' });
        }

        // Moyenne générale globale
        const [toutesNotes] = await pool.query('SELECT note, bareme, coefficient FROM notes WHERE eleve_id = ?', [eleveId]);
        const moyenneGenerale = calculerMoyennePonderee(toutesNotes);

        // Dernières notes obtenues
        const [dernieresNotes] = await pool.query(`
            SELECT n.id, n.titre_evaluation, n.note, n.bareme, n.coefficient, n.date_evaluation, n.commentaire,
                   m.nom AS nom_matiere, m.code AS code_matiere, m.couleur_hex
            FROM notes n
            INNER JOIN matieres m ON n.matiere_id = m.id
            WHERE n.eleve_id = ?
            ORDER BY n.date_evaluation DESC LIMIT 5
        `, [eleveId]);

        // Absences
        const [absences] = await pool.query(`
            SELECT 
                COUNT(*) AS total_absences,
                SUM(CASE WHEN justifiee = FALSE THEN 1 ELSE 0 END) AS non_justifiees,
                SUM(CASE WHEN type = 'RETARD' THEN 1 ELSE 0 END) AS retards
            FROM absences WHERE eleve_id = ?
        `, [eleveId]);

        // Situation financière
        const [paiements] = await pool.query(`
            SELECT COALESCE(SUM(montant), 0) AS total_paye 
            FROM paiements WHERE eleve_id = ? AND statut = 'PAYE'
        `, [eleveId]);

        const fraisTotal = parseFloat(eleve[0].frais_scolarite) || 0;
        const totalPaye = parseFloat(paiements[0].total_paye) || 0;
        const reste = Math.max(0, fraisTotal - totalPaye);

        // Cours du jour
        const joursSemaine = ['Dimanche', 'Lundi', 'Mardi', 'Mercredi', 'Jeudi', 'Vendredi', 'Samedi'];
        const jourActuel = joursSemaine[new Date().getDay()] || 'Lundi';

        let coursDuJour = [];
        if (eleve[0].classe_id) {
            const [cours] = await pool.query(`
                SELECT edt.*, m.nom AS nom_matiere, m.couleur_hex, p.nom AS nom_prof, p.prenom AS prenom_prof
                FROM emplois_du_temps edt
                INNER JOIN matieres m ON edt.matiere_id = m.id
                INNER JOIN professeurs p ON edt.professeur_id = p.id
                WHERE edt.classe_id = ? AND edt.jour = ?
                ORDER BY edt.heure_debut ASC
            `, [eleve[0].classe_id, jourActuel]);
            coursDuJour = cours;
        }

        res.json({
            succes: true,
            eleve: {
                profil: eleve[0],
                moyenne_generale: moyenneGenerale,
                dernieres_notes: dernieresNotes,
                absences: absences[0],
                finances: {
                    frais_scolarite: fraisTotal,
                    total_paye: totalPaye,
                    reste_a_payer: reste
                },
                cours_aujourdhui: coursDuJour
            }
        });
    } catch (error) {
        next(error);
    }
}

module.exports = {
    getDashboardAdmin,
    getDashboardProfesseur,
    getDashboardEleve
};
