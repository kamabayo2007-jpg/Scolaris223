const { pool } = require('../config/database');

/**
 * Exporte l'intégralité des données d'un établissement sous forme de fichier JSON autonome
 * GET /api/backup/export/:schoolId
 */
async function exportSchoolBackup(req, res, next) {
    try {
        const schoolId = parseInt(req.params.schoolId || req.school_id || 1, 10);

        // Vérifier l'école
        const [schools] = await pool.query(`SELECT * FROM schools WHERE id = ? LIMIT 1`, [schoolId]);
        if (schools.length === 0) {
            return res.status(404).json({ succes: false, message: 'Établissement introuvable.' });
        }
        const school = schools[0];

        // Récupérer toutes les données liées à ce school_id
        const [classes] = await pool.query(`SELECT * FROM classes WHERE school_id = ?`, [schoolId]);
        const [matieres] = await pool.query(`SELECT * FROM matieres WHERE school_id = ?`, [schoolId]);
        const [professeurs] = await pool.query(`SELECT * FROM professeurs WHERE school_id = ?`, [schoolId]);
        const [eleves] = await pool.query(`SELECT * FROM eleves WHERE school_id = ?`, [schoolId]);
        const [notes] = await pool.query(`SELECT * FROM notes WHERE school_id = ?`, [schoolId]);
        const [absences] = await pool.query(`SELECT * FROM absences WHERE school_id = ?`, [schoolId]);
        const [emplois] = await pool.query(`SELECT * FROM emplois_du_temps WHERE school_id = ?`, [schoolId]);
        const [paiements] = await pool.query(`SELECT * FROM paiements WHERE school_id = ?`, [schoolId]);
        const [subscriptions] = await pool.query(`SELECT * FROM school_subscriptions WHERE school_id = ?`, [schoolId]);

        const backupData = {
            metadata: {
                version: '1.0.0',
                type: 'ERP_GESTION_ECOLE_BACKUP',
                date_sauvegarde: new Date().toISOString(),
                school_id: school.id,
                school_code: school.code,
                school_name: school.nom,
                total_eleves: eleves.length,
                total_classes: classes.length,
                total_paiements: paiements.length
            },
            school,
            classes,
            matieres,
            professeurs,
            eleves,
            notes,
            absences,
            emplois_du_temps: emplois,
            paiements,
            subscriptions
        };

        const filename = `sauvegarde_${school.code.toLowerCase()}_${Date.now()}.json`;
        res.setHeader('Content-Type', 'application/json');
        res.setHeader('Content-Disposition', `attachment; filename="${filename}"`);
        res.status(200).send(JSON.stringify(backupData, null, 2));
    } catch (error) {
        next(error);
    }
}

/**
 * Restaure une sauvegarde complète dans la base de données
 * POST /api/backup/restore
 */
async function restoreSchoolBackup(req, res, next) {
    const connection = await pool.getConnection();
    try {
        const backupData = req.body;

        if (!backupData || !backupData.metadata || backupData.metadata.type !== 'ERP_GESTION_ECOLE_BACKUP') {
            return res.status(400).json({
                succes: false,
                message: 'Format de fichier de sauvegarde invalide ou non reconnu par le système.'
            });
        }

        await connection.beginTransaction();

        const school = backupData.school;
        let schoolId = school.id;

        // 1. Mettre à jour ou insérer l'école
        const [existingSchool] = await connection.query(`SELECT id FROM schools WHERE code = ? LIMIT 1`, [school.code]);
        if (existingSchool.length > 0) {
            schoolId = existingSchool[0].id;
            await connection.query(`
                UPDATE schools 
                SET nom = ?, ville = ?, pays = ?, telephone = ?, email = ?, devise = ?, statut_souscription = ?
                WHERE id = ?
            `, [school.nom, school.ville, school.pays, school.telephone, school.email, school.devise || 'FCFA', school.statut_souscription || 'ACTIF', schoolId]);
        } else {
            const [newSch] = await connection.query(`
                INSERT INTO schools (code, nom, ville, pays, telephone, email, devise, statut_souscription)
                VALUES (?, ?, ?, ?, ?, ?, ?, 'ACTIF')
            `, [school.code, school.nom, school.ville, school.pays, school.telephone, school.email, school.devise || 'FCFA']);
            schoolId = newSch.insertId;
        }

        // 2. Réinsérer les classes
        if (Array.isArray(backupData.classes)) {
            for (const c of backupData.classes) {
                await connection.query(`
                    INSERT INTO classes (id, school_id, nom, niveau, annee_scolaire_id, frais_scolarite, effectif, salle_principale)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE nom = VALUES(nom), frais_scolarite = VALUES(frais_scolarite)
                `, [c.id, schoolId, c.nom, c.niveau, c.annee_scolaire_id || 1, c.frais_scolarite || 0, c.effectif || 0, c.salle_principale]);
            }
        }

        // 3. Réinsérer les matières
        if (Array.isArray(backupData.matieres)) {
            for (const m of backupData.matieres) {
                await connection.query(`
                    INSERT INTO matieres (id, school_id, code, nom, coefficient, niveau, couleur_hex)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE nom = VALUES(nom), coefficient = VALUES(coefficient)
                `, [m.id, schoolId, m.code, m.nom, m.coefficient || 1, m.niveau, m.couleur_hex || '#2563EB']);
            }
        }

        // 4. Réinsérer les élèves
        if (Array.isArray(backupData.eleves)) {
            for (const e of backupData.eleves) {
                await connection.query(`
                    INSERT INTO eleves (id, school_id, user_id, matricule, nom, prenom, date_naissance, nom_parent, telephone_parent, classe_id)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE nom = VALUES(nom), prenom = VALUES(prenom), classe_id = VALUES(classe_id)
                `, [e.id, schoolId, e.user_id || 1, e.matricule, e.nom, e.prenom, e.date_naissance || '2008-01-01', e.nom_parent || 'Parent', e.telephone_parent || '00000000', e.classe_id]);
            }
        }

        // 5. Réinsérer les paiements en FCFA
        if (Array.isArray(backupData.paiements)) {
            for (const p of backupData.paiements) {
                await connection.query(`
                    INSERT INTO paiements (id, school_id, eleve_id, montant, devise, type, date_paiement, reference, mode_paiement, statut)
                    VALUES (?, ?, ?, ?, 'FCFA', ?, ?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE montant = VALUES(montant), statut = VALUES(statut)
                `, [p.id, schoolId, p.eleve_id, p.montant, p.type || 'SCOLARITE', p.date_paiement, p.reference, p.mode_paiement || 'ESPECES', p.statut || 'PAYE']);
            }
        }

        await connection.commit();

        res.json({
            succes: true,
            message: `Données de l'école [${school.nom}] restaurées avec succès en cas de sinistre technique.`,
            school_id: schoolId,
            elements_restaures: {
                classes: backupData.classes ? backupData.classes.length : 0,
                eleves: backupData.eleves ? backupData.eleves.length : 0,
                paiements: backupData.paiements ? backupData.paiements.length : 0
            }
        });
    } catch (error) {
        await connection.rollback();
        next(error);
    } finally {
        connection.release();
    }
}

module.exports = {
    exportSchoolBackup,
    restoreSchoolBackup
};
