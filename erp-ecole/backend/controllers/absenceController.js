const { pool } = require('../config/database');

/**
 * Récupère les absences et retards avec filtres
 * GET /api/absences
 */
async function getAllAbsences(req, res, next) {
    try {
        const { classe_id, eleve_id, date, type, justifiee } = req.query;

        let query = `
            SELECT a.id, a.eleve_id, a.professeur_id, a.date, a.creneau_horaire,
                   a.type, a.justifiee, a.motif, a.date_justification, a.remarques,
                   e.nom AS nom_eleve, e.prenom AS prenom_eleve, e.matricule, e.classe_id,
                   c.nom AS nom_classe, p.nom AS nom_prof, p.prenom AS prenom_prof
            FROM absences a
            INNER JOIN eleves e ON a.eleve_id = e.id
            LEFT JOIN classes c ON e.classe_id = c.id
            LEFT JOIN professeurs p ON a.professeur_id = p.id
            WHERE 1=1
        `;
        const params = [];

        // Sécurité rôle : Un élève ne voit que ses propres absences
        if (req.user.role === 'ELEVE') {
            query += ` AND a.eleve_id = ?`;
            params.push(req.user.eleve_id);
        } else if (eleve_id) {
            query += ` AND a.eleve_id = ?`;
            params.push(eleve_id);
        }

        if (classe_id) {
            query += ` AND e.classe_id = ?`;
            params.push(classe_id);
        }

        if (date) {
            query += ` AND a.date = ?`;
            params.push(date);
        }

        if (type) {
            query += ` AND a.type = ?`;
            params.push(type);
        }

        if (justifiee !== undefined) {
            query += ` AND a.justifiee = ?`;
            params.push(justifiee === 'true' || justifiee === '1');
        }

        query += ` ORDER BY a.date DESC, a.id DESC`;

        const [absences] = await pool.query(query, params);

        res.json({
            succes: true,
            total: absences.length,
            absences
        });
    } catch (error) {
        next(error);
    }
}

/**
 * Enregistrement de l'appel pour une classe complète
 * POST /api/absences/appel
 */
async function enregistrerAppel(req, res, next) {
    const connection = await pool.getConnection();
    try {
        await connection.beginTransaction();

        const { classe_id, date, creneau_horaire = '08:00 - 10:00', appel } = req.body;
        // appel est un tableau : [{ eleve_id: 1, statut: 'PRESENT' | 'ABSENT' | 'RETARD', motif: '' }]

        if (!classe_id || !date || !Array.isArray(appel)) {
            await connection.rollback();
            return res.status(400).json({
                succes: false,
                message: 'Données d\'appel incomplètes. Spécifiez la classe, la date et la liste des présences.'
            });
        }

        let profId = null;
        if (req.user.role === 'PROFESSEUR') {
            profId = req.user.professeur_id;
        }

        // Supprimer les anciens enregistrements pour ce créneau et cette classe pour éviter les doublons
        const eleveIds = appel.map(a => a.eleve_id);
        if (eleveIds.length > 0) {
            await connection.query(
                `DELETE FROM absences 
                 WHERE date = ? AND creneau_horaire = ? AND eleve_id IN (?)`,
                [date, creneau_horaire, eleveIds]
            );
        }

        let nbAbsents = 0;
        let nbRetards = 0;

        for (const item of appel) {
            if (item.statut === 'ABSENT' || item.statut === 'RETARD') {
                const justifiee = Boolean(item.justifiee) || false;
                await connection.query(`
                    INSERT INTO absences (eleve_id, professeur_id, date, creneau_horaire, type, justifiee, motif, remarques)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                `, [item.eleve_id, profId, date, creneau_horaire, item.statut, justifiee, item.motif || null, item.remarques || null]);

                if (item.statut === 'ABSENT') nbAbsents++;
                if (item.statut === 'RETARD') nbRetards++;

                // Notification automatique pour l'élève si absent
                if (item.statut === 'ABSENT' && !justifiee) {
                    const [elv] = await connection.query('SELECT user_id FROM eleves WHERE id = ?', [item.eleve_id]);
                    if (elv.length > 0) {
                        await connection.query(`
                            INSERT INTO notifications (user_id, titre, message, type)
                            VALUES (?, ?, ?, 'ALERTE')
                        `, [elv[0].user_id, 'Absence enregistrée', `Une absence a été signalée le ${date} (${creneau_horaire}). Merci de fournir un justificatif.`]);
                    }
                }
            }
        }

        await connection.commit();

        res.json({
            succes: true,
            message: `Appel validé : ${nbAbsents} absence(s) et ${nbRetards} retard(s) enregistrés.`
        });
    } catch (error) {
        await connection.rollback();
        next(error);
    } finally {
        connection.release();
    }
}

/**
 * Justification d'une absence par l'administration ou un professeur
 * PUT /api/absences/:id/justifier
 */
async function justifierAbsence(req, res, next) {
    try {
        const { id } = req.params;
        const { motif, remarques } = req.body;

        if (!motif) {
            return res.status(400).json({
                succes: false,
                message: 'Veuillez indiquer le motif officiel de la justification (ex: Certificat médical).'
            });
        }

        await pool.query(`
            UPDATE absences SET
                justifiee = TRUE,
                motif = ?,
                date_justification = CURDATE(),
                remarques = COALESCE(?, remarques)
            WHERE id = ?
        `, [motif, remarques, id]);

        res.json({
            succes: true,
            message: 'Absence marquée comme justifiée avec succès.'
        });
    } catch (error) {
        next(error);
    }
}

/**
 * Suppression d'un enregistrement d'absence
 * DELETE /api/absences/:id
 */
async function deleteAbsence(req, res, next) {
    try {
        const { id } = req.params;
        await pool.query('DELETE FROM absences WHERE id = ?', [id]);
        res.json({ succes: true, message: 'Enregistrement d\'absence supprimé.' });
    } catch (error) {
        next(error);
    }
}

module.exports = {
    getAllAbsences,
    enregistrerAppel,
    justifierAbsence,
    deleteAbsence
};
