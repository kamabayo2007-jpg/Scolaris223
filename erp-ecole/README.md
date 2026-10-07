# ERP Gestion École 🎓

Application web professionnelle complète de gestion scolaire (Système d'Information et de Vie Scolaire).

L'application est conçue pour fonctionner avec un **Backend Node.js / Express**, une base de données **MariaDB ou MySQL**, et un **Frontend Single Page Application (HTML5 / CSS3 / Vanilla JS moderne)** entièrement responsive (Mobile-first, tablette et ordinateur de bureau).

---

## 🏗️ Architecture du Projet

```text
erp-ecole/
├── database/
│   ├── schema.sql             # Définition des tables, contraintes, index et clés étrangères
│   └── seed.sql               # Données de test (1 admin, 3 profs, 20 élèves, classes, notes, etc.)
├── backend/
│   ├── config/
│   │   └── database.js        # Pool de connexions mysql2/promise
│   ├── controllers/
│   │   ├── authController.js          # Authentification JWT, login, me, changement mot de passe
│   │   ├── eleveController.js         # CRUD élèves, filtres, recherche multi-critères
│   │   ├── professeurController.js    # CRUD professeurs, affectations classes/matières
│   │   ├── classeController.js        # CRUD classes, effectifs, listes élèves/profs
│   │   ├── matiereController.js       # CRUD matières et coefficients
│   │   ├── noteController.js          # Saisie notes (0-20), vérification droits, moyennes
│   │   ├── absenceController.js       # Appel interactif de classe, justifications
│   │   ├── emploiDuTempsController.js # Planning hebdomadaire, détection conflits horaires
│   │   ├── paiementController.js      # Encaissement scolarité, solde restant, reçus
│   │   ├── bulletinController.js      # Génération bulletin officiel imprimable A4
│   │   ├── statistiqueController.js   # Dashboards spécifiques par rôle
│   │   └── userController.js          # Administration des comptes utilisateurs
│   ├── middlewares/
│   │   ├── authMiddleware.js          # Vérification JWT et contrôle strict des rôles
│   │   └── errorMiddleware.js         # Gestion centralisée des erreurs (400, 401, 403, 404, 409, 500)
│   ├── routes/
│   │   ├── authRoutes.js
│   │   ├── eleveRoutes.js
│   │   ├── professeurRoutes.js
│   │   ├── classeRoutes.js
│   │   ├── matiereRoutes.js
│   │   ├── noteRoutes.js
│   │   ├── absenceRoutes.js
│   │   ├── emploiDuTempsRoutes.js
│   │   ├── paiementRoutes.js
│   │   ├── bulletinRoutes.js
│   │   ├── statistiqueRoutes.js
│   │   └── userRoutes.js
│   ├── scripts/
│   │   └── seed.js            # Script automatisé d'initialisation et hachage bcrypt
│   ├── utils/
│   │   └── helpers.js         # Calcul de moyenne pondérée, mentions, références reçus
│   ├── .env.example           # Gabarit des variables d'environnement
│   ├── package.json           # Dépendances Node.js (express, mysql2, bcryptjs, jsonwebtoken, etc.)
│   └── server.js              # Point d'entrée serveur Express (sert aussi le frontend statique)
├── frontend/
│   ├── css/
│   │   └── style.css          # Design system CSS moderne responsive + styles impression A4 (@media print)
│   ├── js/
│   │   ├── api.js             # Client HTTP avec gestion automatique du Bearer token et toasts
│   │   ├── auth.js            # Gestion de session, connexion et déconnexion
│   │   └── app.js             # Contrôleur SPA complet (vues, modales, filtres et formulaires)
│   └── index.html             # Page unique SPA (layout, sidebar, topbar, modales)
└── README.md                  # Guide complet d'installation et d'exploitation
```

---

## ⚡ Prérequis Système

- **Node.js** : Version 18.x ou 20.x LTS
- **Gestionnaire de paquets** : `npm` (inclus avec Node.js)
- **Base de données** : **MariaDB** (recommandé) ou **MySQL 8.0+**
- Compatible avec : **Linux (Ubuntu, Debian, CentOS)**, **macOS**, **Windows (PowerShell ou WSL2)**, et **Android (via Termux)**.

---

## 🚀 Guide d'Installation Pas à Pas

### 1. Configuration de MariaDB / MySQL

Démarrez le service de base de données :

```bash
# Sur Linux / Ubuntu / Debian :
sudo systemctl start mariadb   # ou sudo systemctl start mysql

# Sur Android (Termux) :
pkg install mariadb
mariadb-install-db
mysqld_safe -u root &
```

Connectez-vous à MySQL/MariaDB en administrateur :

```bash
mysql -u root -p
```

Créez la base de données et l'utilisateur dédié :

```sql
CREATE DATABASE IF NOT EXISTS erp_ecole CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'ecole_user'@'localhost' IDENTIFIED BY 'MotDePasseSecurise2025!';
GRANT ALL PRIVILEGES ON erp_ecole.* TO 'ecole_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

---

### 2. Configuration des Variables d'Environnement (.env)

Déplacez-vous dans le dossier `backend` :

```bash
cd erp-ecole/backend
cp .env.example .env
```

Éditez le fichier `.env` avec vos identifiants réels :

```ini
PORT=5000
NODE_ENV=development

# Base de données MariaDB / MySQL
DB_HOST=localhost
DB_PORT=3306
DB_USER=ecole_user
DB_PASSWORD=MotDePasseSecurise2025!
DB_NAME=erp_ecole

# Sécurité & Jeton JWT
JWT_SECRET=votre_cle_tres_longue_et_aleatoire_pour_le_chiffrement_jwt_2025
JWT_EXPIRES_IN=24h
```

---

### 3. Installation des Dépendances Node.js

Dans le dossier `backend` :

```bash
npm install
```

---

### 4. Initialisation Automatisée de la Base et Données de Test

Exécutez le script d'initialisation fourni qui crée le schéma SQL et injecte les 20 élèves, 3 professeurs, 3 classes, matières, notes, paiements et emplois du temps :

```bash
npm run seed
```

> **Méthode alternative manuelle via la ligne de commande MySQL :**
> ```bash
> mysql -u ecole_user -p erp_ecole < ../database/schema.sql
> mysql -u ecole_user -p erp_ecole < ../database/seed.sql
> ```

---

### 5. Démarrage de l'Application

Lancez le serveur Express (qui héberge également le frontend statique) :

```bash
npm start
```

Pour le mode développement avec rechargement automatique :

```bash
npm run dev
```

Ouvrez ensuite votre navigateur sur : **`http://localhost:5000`** (ou `http://VOTRE_IP_LOCALE:5000` depuis un téléphone ou une tablette sur le même réseau Wi-Fi).

---

## 🔑 Identifiants des Comptes de Démonstration

Tous les mots de passe de test sont hachés avec **bcrypt (10 rounds)**. Ils doivent être modifiés lors d'une mise en production réelle.

| Rôle | Identifiant (Username) | Mot de passe démo | Description |
| :--- | :--- | :--- | :--- |
| **ADMINISTRATEUR** | `admin` | `Admin@123456` | Accès total : paramétrage, finances, inscriptions, effectifs, utilisateurs |
| **PROFESSEUR** | `prof.bernard` | `Prof@123456` | Enseignant de Mathématiques (Terminale S1 et 3ème A) |
| **PROFESSEUR** | `prof.rousseau` | `Prof@123456` | Enseignante de Français |
| **PROFESSEUR** | `prof.martin` | `Prof@123456` | Enseignant de Physique-Chimie |
| **ÉLÈVE** | `lucas.moreau` | `Eleve@123456` | Élève de Terminale S1 (Matricule : `TS1-001`) |
| **ÉLÈVE** | `emma.bernard` | `Eleve@123456` | Élève de Terminale S1 (Matricule : `TS1-002`) |
| **ÉLÈVE** | `antoine.dubois` | `Eleve@123456` | Élève de 3ème A (Matricule : `3A-001`) |

---

## 📱 Utilisation par Rôle & Fonctionnalités

### 1. Espace Administrateur (`ADMIN`)
- **Tableau de Bord** : Indicateurs financiers en temps réel (total facturé, encaissé, reste à recouvrer), taux de présence global, moyenne générale de l'école.
- **Gestion des Élèves** : Inscription avec génération automatique du matricule et du compte d'accès. Recherche multi-critères.
- **Gestion des Professeurs** : Fiches des enseignants et affectations matières/classes dans la table `enseignements`.
- **Gestion des Classes & Matières** : Définition des niveaux, salles principales et coefficients.
- **Finances & Scolarité** : Saisie des règlements avec émission automatique de référence unique (`PAY-...`).
- **Gestion des Comptes** : Activation / désactivation des utilisateurs, réinitialisation de mot de passe.

### 2. Espace Professeur (`PROFESSEUR`)
- **Mes Classes & Élèves** : Consultation des effectifs et des coordonnées d'urgence des parents.
- **Saisie des Notes** : Formulaire sécurisé bloquant toute saisie < 0 ou > 20, avec contrôle strict côté serveur vérifiant que l'enseignant est bien affecté à la classe.
- **Faire l'Appel** : Interface ultra-rapide par créneau horaire avec sélection en 1 tap (Présent, Retard, Absent) et notification automatique aux parents/élèves.
- **Emploi du Temps** : Planning de cours hebdomadaire.

### 3. Espace Élève (`ELEVE`)
- **Mon Dossier** : Consultation de ses notes récentes avec codes couleurs selon le résultat.
- **Calcul de Moyenne** : Affichage transparent de la moyenne générale pondérée selon la formule officielle :
  $$\text{Moyenne Générale} = \frac{\sum (\text{Note} \times \text{Coefficient})}{\sum \text{Coefficients}}$$
- **Assiduité** : Suivi des absences justifiées et non justifiées.
- **Paiements** : Historique des versements et solde restant à payer pour l'année.
- **Bulletin Scolaire Officiel** : Consultation et **impression A4 directe** (mise en page optimisée pour imprimante ou PDF avec entête officiel, appréciations des professeurs, rang et mentions d'honneur).

---

## 🧪 Tests et Commandes de Vérification

Pour tester les points de terminaison de l'API REST via `curl` :

```bash
# 1. Vérification de santé de l'API
curl -X GET http://localhost:5000/api/sante

# 2. Test de connexion (obtention du token JWT)
curl -X POST http://localhost:5000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username_ou_email":"admin","password":"Admin@123456"}'

# 3. Récupération des élèves avec le token obtenu
curl -X GET http://localhost:5000/api/eleves \
  -H "Authorization: Bearer VOTRE_TOKEN_ICI"
```

---

## 🛡️ Sécurité & Bonnes Pratiques Appliquées

1. **Aucun mot de passe en clair** : Hachage systématique avec `bcryptjs` (sel de 10 tours).
2. **Requêtes SQL 100% paramétrées** : Prévention intégrale des failles d'injection SQL via `mysql2/promise`.
3. **Contrôle strict des rôles côté serveur** : Middleware `requireRole` vérifiant les privilèges en base de données.
4. **En-têtes sécurisés HTTP** : Intégration de `helmet` et gestion rigoureuse des stratégies CORS.
5. **Gestion centralisée des exceptions** : Aucune trace de pile technique sensible exposée en mode production.
