const mysql = require('mysql2/promise');
const dotenv = require('dotenv');

dotenv.config();

const pool = mysql.createPool({
    host: process.env.DB_HOST || 'localhost',
    port: parseInt(process.env.DB_PORT, 10) || 3306,
    user: process.env.DB_USER || 'root',
    password: process.env.DB_PASSWORD || '',
    database: process.env.DB_NAME || 'erp_ecole',
    waitForConnections: true,
    connectionLimit: parseInt(process.env.DB_CONNECTION_LIMIT, 10) || 15,
    queueLimit: 0,
    enableKeepAlive: true,
    keepAliveInitialDelay: 10000,
    decimalNumbers: true
});

/**
 * Vérifie la connectivité à la base de données au démarrage
 */
async function testConnection() {
    try {
        const connection = await pool.getConnection();
        console.log('✅ Connexion à la base de données MariaDB/MySQL établie avec succès.');
        connection.release();
        return true;
    } catch (error) {
        console.error('❌ Échec de connexion à la base de données :');
        console.error(`Détail : ${error.message}`);
        console.error('Veuillez vous assurer que MariaDB/MySQL est démarré et que les identifiants dans .env sont corrects.');
        return false;
    }
}

module.exports = {
    pool,
    testConnection
};
