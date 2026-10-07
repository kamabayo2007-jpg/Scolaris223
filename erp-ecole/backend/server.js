const path = require('path');
const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const morgan = require('morgan');
const dotenv = require('dotenv');

dotenv.config();

const { testConnection } = require('./config/database');
const { errorHandler, notFoundHandler } = require('./middlewares/errorMiddleware');

// Import des routes
const authRoutes = require('./routes/authRoutes');
const eleveRoutes = require('./routes/eleveRoutes');
const professeurRoutes = require('./routes/professeurRoutes');
const classeRoutes = require('./routes/classeRoutes');
const matiereRoutes = require('./routes/matiereRoutes');
const noteRoutes = require('./routes/noteRoutes');
const absenceRoutes = require('./routes/absenceRoutes');
const emploiDuTempsRoutes = require('./routes/emploiDuTempsRoutes');
const paiementRoutes = require('./routes/paiementRoutes');
const bulletinRoutes = require('./routes/bulletinRoutes');
const statistiqueRoutes = require('./routes/statistiqueRoutes');
const userRoutes = require('./routes/userRoutes');
const schoolRoutes = require('./routes/schoolRoutes');
const paymentGatewayRoutes = require('./routes/paymentGatewayRoutes');
const backupRoutes = require('./routes/backupRoutes');

const app = express();
const PORT = parseInt(process.env.PORT, 10) || 5000;

// Configuration de sécurité
app.use(helmet({
    contentSecurityPolicy: false // Permet le chargement aisé des polices et icônes locales
}));

app.use(cors({
    origin: '*', // Permet les requêtes depuis n'importe quel client web ou mobile
    methods: ['GET', 'POST', 'PUT', 'PATCH', 'DELETE'],
    allowedHeaders: ['Content-Type', 'Authorization']
}));

app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(morgan('dev'));

// Fichiers statiques du frontend (Single Page Application)
const frontendPath = path.join(__dirname, '../frontend');
app.use(express.static(frontendPath));

// Route de santé de l'API
app.get('/api/sante', (req, res) => {
    res.json({
        succes: true,
        message: 'API ERP Gestion École fonctionnelle et opérationnelle.',
        horodatage: new Date().toISOString(),
        version: '1.0.0'
    });
});

// Enregistrement des routes de l'API REST
app.use('/api/auth', authRoutes);
app.use('/api/eleves', eleveRoutes);
app.use('/api/professeurs', professeurRoutes);
app.use('/api/classes', classeRoutes);
app.use('/api/matieres', matiereRoutes);
app.use('/api/notes', noteRoutes);
app.use('/api/absences', absenceRoutes);
app.use('/api/emplois-du-temps', emploiDuTempsRoutes);
app.use('/api/paiements', paiementRoutes);
app.use('/api/mobile-payments', paymentGatewayRoutes);
app.use('/api/schools', schoolRoutes);
app.use('/api/backup', backupRoutes);
app.use('/api/bulletins', bulletinRoutes);
app.use('/api/statistiques', statistiqueRoutes);
app.use('/api/users', userRoutes);

// Redirection SPA pour les routes frontend
app.get('*', (req, res, next) => {
    if (req.path.startsWith('/api')) {
        return next();
    }
    res.sendFile(path.join(frontendPath, 'index.html'));
});

// Middlewares d'erreurs
app.use(notFoundHandler);
app.use(errorHandler);

// Démarrage du serveur et test de la base
async function startServer() {
    await testConnection();
    app.listen(PORT, '0.0.0.0', () => {
        console.log(`\n🚀 Serveur ERP Gestion École démarré avec succès.`);
        console.log(`📍 URL locale : http://localhost:${PORT}`);
        console.log(`🌐 Accès réseau : http://0.0.0.0:${PORT}`);
        console.log(`🛠️ Environnement : ${process.env.NODE_ENV || 'development'}\n`);
    });
}

startServer();
