const fs = require('fs');
const path = require('path');
const mysql = require('mysql2/promise');
const bcrypt = require('bcryptjs');
const dotenv = require('dotenv');

dotenv.config();

async function runSeed() {
    console.log('🌱 Démarrage de l\'initialisation de la base de données ERP Gestion École...');

    const connection = await mysql.createConnection({
        host: process.env.DB_HOST || 'localhost',
        port: parseInt(process.env.DB_PORT, 10) || 3306,
        user: process.env.DB_USER || 'root',
        password: process.env.DB_PASSWORD || '',
        multipleStatements: true
    });

    try {
        console.log('1. Création de la base et des tables via schema.sql...');
        const schemaPath = path.join(__dirname, '../../database/schema.sql');
        const schemaSql = fs.readFileSync(schemaPath, 'utf8');
        await connection.query(schemaSql);
        console.log('   ✅ Schéma créé avec succès.');

        await connection.query('USE erp_ecole;');

        console.log('2. Hachage des mots de passe de démonstration...');
        const adminHash = await bcrypt.hash('Admin@123456', 10);
        const profHash = await bcrypt.hash('Prof@123456', 10);
        const eleveHash = await bcrypt.hash('Eleve@123456', 10);

        console.log('3. Insertion des données via seed.sql...');
        const seedPath = path.join(__dirname, '../../database/seed.sql');
        let seedSql = fs.readFileSync(seedPath, 'utf8');

        // Remplacement dynamique des hashs pour compatibilité parfaite
        seedSql = seedSql.replace(/\$2a\$10\$w0d1DqvF6m6oG8l4I9L3xO0F1\.V\/JzK0k6d8m9y5R3P0q8k9w2x7y/g, (match, offset) => {
            if (offset < 2500) return profHash;
            return eleveHash;
        });

        await connection.query(seedSql);

        // Mettre à jour l'admin avec son mot de passe exact
        await connection.query('UPDATE users SET password_hash = ? WHERE username = "admin"', [adminHash]);
        await connection.query('UPDATE users SET password_hash = ? WHERE role = "PROFESSEUR"', [profHash]);
        await connection.query('UPDATE users SET password_hash = ? WHERE role = "ELEVE"', [eleveHash]);

        console.log('   ✅ Données de démonstration insérées.');
        console.log('\n======================================================');
        console.log('🎉 Initialisation terminée avec succès !');
        console.log('Comptes de test prêts à l\'emploi :');
        console.log(' - ADMINISTRATEUR : Identifiant = "admin"        | Mot de passe = "Admin@123456"');
        console.log(' - PROFESSEUR     : Identifiant = "prof.bernard" | Mot de passe = "Prof@123456"');
        console.log(' - ÉLÈVE          : Identifiant = "lucas.moreau" | Mot de passe = "Eleve@123456"');
        console.log('======================================================\n');
    } catch (err) {
        console.error('❌ Erreur lors du seed :', err);
    } finally {
        await connection.end();
    }
}

runSeed();
