-- =====================================================================
-- APPLICATION ERP GESTION ÉCOLE - ARCHITECTURE MULTI-TENANT
-- Schéma relationnel MariaDB / MySQL avec isolation par school_id
-- Base de données : erp_ecole
-- Devise par défaut : Franc CFA (FCFA / XOF)
-- =====================================================================

CREATE DATABASE IF NOT EXISTS erp_ecole CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE erp_ecole;

-- ---------------------------------------------------------------------
-- 0. Table des Établissements Scolaires (Multi-Tenants)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS schools (
    id INT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    nom VARCHAR(150) NOT NULL,
    ville VARCHAR(100) NOT NULL DEFAULT 'Abidjan',
    pays VARCHAR(100) NOT NULL DEFAULT 'Côte d''Ivoire',
    telephone VARCHAR(30) NULL,
    email VARCHAR(100) NULL,
    devise VARCHAR(10) NOT NULL DEFAULT 'FCFA',
    frais_inscription DECIMAL(12,2) NOT NULL DEFAULT 250000.00,
    frais_annuel DECIMAL(12,2) NOT NULL DEFAULT 500000.00,
    statut_souscription ENUM('ACTIF', 'EN_ATTENTE', 'SUSPENDU', 'EXPIRE') NOT NULL DEFAULT 'ACTIF',
    date_expiration DATE NULL,
    couleur_banniere VARCHAR(10) DEFAULT '#1E3A8A',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 0.1 Table des Souscriptions et Abonnements Écoles
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS school_subscriptions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    school_id INT NOT NULL,
    type_frais ENUM('FRAIS_INSCRIPTION', 'FRAIS_ANNUEL') NOT NULL,
    montant DECIMAL(12,2) NOT NULL,
    devise VARCHAR(10) NOT NULL DEFAULT 'FCFA',
    mode_paiement ENUM('ORANGE_MONEY', 'MOOV_MONEY', 'WAVE', 'VIREMENT', 'ESPECES') NOT NULL,
    reference_transaction VARCHAR(100) NOT NULL UNIQUE,
    telephone_payeur VARCHAR(30) NULL,
    statut ENUM('VALIDE', 'EN_ATTENTE', 'ECHOUE') NOT NULL DEFAULT 'VALIDE',
    date_paiement TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_validite_fin DATE NULL,
    notes TEXT NULL,
    CONSTRAINT fk_sub_school FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 1. Table des rôles
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS roles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    libelle VARCHAR(50) NOT NULL,
    description VARCHAR(255) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 2. Table des utilisateurs (comptes d'accès avec school_id)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    school_id INT NULL, -- NULL uniquement pour SUPER_ADMIN (contrôle total sur toutes les écoles)
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    doit_changer_mdp BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_school FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE,
    CONSTRAINT fk_users_role FOREIGN KEY (role) REFERENCES roles(code) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 3. Table des années scolaires (liée à l'école)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS annees_scolaires (
    id INT AUTO_INCREMENT PRIMARY KEY,
    school_id INT NOT NULL,
    libelle VARCHAR(50) NOT NULL,
    date_debut DATE NOT NULL,
    date_fin DATE NOT NULL,
    active BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_annee_ecole (school_id, libelle),
    CONSTRAINT fk_annees_school FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 4. Table des périodes scolaires (Trimestres / Semestres)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS periodes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    school_id INT NOT NULL,
    annee_scolaire_id INT NOT NULL,
    nom VARCHAR(50) NOT NULL,
    date_debut DATE NOT NULL,
    date_fin DATE NOT NULL,
    active BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_periodes_school FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE,
    CONSTRAINT fk_periodes_annee FOREIGN KEY (annee_scolaire_id) REFERENCES annees_scolaires(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 5. Table des classes
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS classes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    school_id INT NOT NULL,
    nom VARCHAR(50) NOT NULL,
    niveau VARCHAR(50) NOT NULL,
    annee_scolaire_id INT NOT NULL,
    frais_scolarite DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    effectif INT NOT NULL DEFAULT 0,
    salle_principale VARCHAR(50) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_classes_school FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE,
    CONSTRAINT fk_classes_annee FOREIGN KEY (annee_scolaire_id) REFERENCES annees_scolaires(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 6. Table des matières
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS matieres (
    id INT AUTO_INCREMENT PRIMARY KEY,
    school_id INT NOT NULL,
    code VARCHAR(20) NOT NULL,
    nom VARCHAR(100) NOT NULL,
    coefficient DECIMAL(4,2) NOT NULL DEFAULT 1.00,
    niveau VARCHAR(50) NOT NULL,
    couleur_hex VARCHAR(10) DEFAULT '#2563EB',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_matiere_school (school_id, code),
    CONSTRAINT fk_matieres_school FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 7. Table des professeurs
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS professeurs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    school_id INT NOT NULL,
    user_id INT NOT NULL UNIQUE,
    matricule VARCHAR(30) NOT NULL,
    nom VARCHAR(60) NOT NULL,
    prenom VARCHAR(60) NOT NULL,
    date_naissance DATE NULL,
    telephone VARCHAR(30) NULL,
    email VARCHAR(100) NOT NULL,
    adresse VARCHAR(255) NULL,
    specialite VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_prof_school_matricule (school_id, matricule),
    CONSTRAINT fk_professeurs_school FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE,
    CONSTRAINT fk_professeurs_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 8. Table des élèves
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS eleves (
    id INT AUTO_INCREMENT PRIMARY KEY,
    school_id INT NOT NULL,
    user_id INT NOT NULL UNIQUE,
    matricule VARCHAR(30) NOT NULL,
    nom VARCHAR(60) NOT NULL,
    prenom VARCHAR(60) NOT NULL,
    date_naissance DATE NOT NULL,
    lieu_naissance VARCHAR(100) NULL,
    sexe ENUM('M', 'F') NOT NULL DEFAULT 'M',
    telephone VARCHAR(30) NULL,
    adresse VARCHAR(255) NULL,
    nom_parent VARCHAR(120) NOT NULL,
    telephone_parent VARCHAR(30) NOT NULL,
    email_parent VARCHAR(100) NULL,
    classe_id INT NULL,
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_eleve_school_matricule (school_id, matricule),
    CONSTRAINT fk_eleves_school FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE,
    CONSTRAINT fk_eleves_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_eleves_classe FOREIGN KEY (classe_id) REFERENCES classes(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 9. Table des inscriptions (Historique académique annuel)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS inscriptions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    school_id INT NOT NULL,
    eleve_id INT NOT NULL,
    classe_id INT NOT NULL,
    annee_scolaire_id INT NOT NULL,
    date_inscription DATE NOT NULL,
    statut ENUM('INSCRIT', 'CONFIRME', 'TRANSFERE', 'ABANDONNE') NOT NULL DEFAULT 'INSCRIT',
    frais_total DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_eleve_annee (eleve_id, annee_scolaire_id),
    CONSTRAINT fk_inscriptions_school FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE,
    CONSTRAINT fk_inscriptions_eleve FOREIGN KEY (eleve_id) REFERENCES eleves(id) ON DELETE CASCADE,
    CONSTRAINT fk_inscriptions_classe FOREIGN KEY (classe_id) REFERENCES classes(id) ON DELETE RESTRICT,
    CONSTRAINT fk_inscriptions_annee FOREIGN KEY (annee_scolaire_id) REFERENCES annees_scolaires(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 10. Table des enseignements (Affectation Professeur - Matière - Classe)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS enseignements (
    id INT AUTO_INCREMENT PRIMARY KEY,
    school_id INT NOT NULL,
    professeur_id INT NOT NULL,
    matiere_id INT NOT NULL,
    classe_id INT NOT NULL,
    annee_scolaire_id INT NOT NULL,
    coefficient_specifique DECIMAL(4,2) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_prof_matiere_classe (professeur_id, matiere_id, classe_id, annee_scolaire_id),
    CONSTRAINT fk_ens_school FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE,
    CONSTRAINT fk_ens_prof FOREIGN KEY (professeur_id) REFERENCES professeurs(id) ON DELETE CASCADE,
    CONSTRAINT fk_ens_matiere FOREIGN KEY (matiere_id) REFERENCES matieres(id) ON DELETE CASCADE,
    CONSTRAINT fk_ens_classe FOREIGN KEY (classe_id) REFERENCES classes(id) ON DELETE CASCADE,
    CONSTRAINT fk_ens_annee FOREIGN KEY (annee_scolaire_id) REFERENCES annees_scolaires(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 11. Table des notes (Évaluations)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS notes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    school_id INT NOT NULL,
    eleve_id INT NOT NULL,
    matiere_id INT NOT NULL,
    professeur_id INT NOT NULL,
    periode_id INT NOT NULL,
    titre_evaluation VARCHAR(100) NOT NULL DEFAULT 'Contrôle continu',
    note DECIMAL(4,2) NOT NULL,
    bareme DECIMAL(4,2) NOT NULL DEFAULT 20.00,
    coefficient DECIMAL(4,2) NOT NULL DEFAULT 1.00,
    date_evaluation DATE NOT NULL,
    commentaire VARCHAR(255) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_note_valeur CHECK (note >= 0 AND note <= bareme),
    CONSTRAINT fk_notes_school FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE,
    CONSTRAINT fk_notes_eleve FOREIGN KEY (eleve_id) REFERENCES eleves(id) ON DELETE CASCADE,
    CONSTRAINT fk_notes_matiere FOREIGN KEY (matiere_id) REFERENCES matieres(id) ON DELETE RESTRICT,
    CONSTRAINT fk_notes_prof FOREIGN KEY (professeur_id) REFERENCES professeurs(id) ON DELETE RESTRICT,
    CONSTRAINT fk_notes_periode FOREIGN KEY (periode_id) REFERENCES periodes(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 12. Table des absences et retards
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS absences (
    id INT AUTO_INCREMENT PRIMARY KEY,
    school_id INT NOT NULL,
    eleve_id INT NOT NULL,
    professeur_id INT NULL,
    date DATE NOT NULL,
    creneau_horaire VARCHAR(50) NOT NULL DEFAULT '08:00 - 10:00',
    type ENUM('ABSENCE', 'RETARD') NOT NULL DEFAULT 'ABSENCE',
    justifiee BOOLEAN NOT NULL DEFAULT FALSE,
    motif VARCHAR(255) NULL,
    date_justification DATE NULL,
    remarques TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_absences_school FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE,
    CONSTRAINT fk_absences_eleve FOREIGN KEY (eleve_id) REFERENCES eleves(id) ON DELETE CASCADE,
    CONSTRAINT fk_absences_prof FOREIGN KEY (professeur_id) REFERENCES professeurs(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 13. Table des emplois du temps
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS emplois_du_temps (
    id INT AUTO_INCREMENT PRIMARY KEY,
    school_id INT NOT NULL,
    classe_id INT NOT NULL,
    matiere_id INT NOT NULL,
    professeur_id INT NOT NULL,
    jour ENUM('Lundi', 'Mardi', 'Mercredi', 'Jeudi', 'Vendredi', 'Samedi') NOT NULL,
    heure_debut TIME NOT NULL,
    heure_fin TIME NOT NULL,
    salle VARCHAR(50) NOT NULL,
    annee_scolaire_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_edt_school FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE,
    CONSTRAINT fk_edt_classe FOREIGN KEY (classe_id) REFERENCES classes(id) ON DELETE CASCADE,
    CONSTRAINT fk_edt_matiere FOREIGN KEY (matiere_id) REFERENCES matieres(id) ON DELETE CASCADE,
    CONSTRAINT fk_edt_prof FOREIGN KEY (professeur_id) REFERENCES professeurs(id) ON DELETE CASCADE,
    CONSTRAINT fk_edt_annee FOREIGN KEY (annee_scolaire_id) REFERENCES annees_scolaires(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 14. Table des paiements de scolarité (Mobile Money & Espèces Locales)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS paiements (
    id INT AUTO_INCREMENT PRIMARY KEY,
    school_id INT NOT NULL,
    eleve_id INT NOT NULL,
    montant DECIMAL(12,2) NOT NULL,
    devise VARCHAR(10) NOT NULL DEFAULT 'FCFA',
    type ENUM('INSCRIPTION', 'SCOLARITE', 'CANTINE', 'TRANSPORT', 'AUTRE') NOT NULL DEFAULT 'SCOLARITE',
    date_paiement DATE NOT NULL,
    reference VARCHAR(50) NOT NULL UNIQUE,
    mode_paiement ENUM('ESPECES', 'CHEQUE', 'VIREMENT', 'ORANGE_MONEY', 'MOOV_MONEY', 'WAVE', 'CARTE') NOT NULL DEFAULT 'ESPECES',
    statut ENUM('EN_ATTENTE', 'PAYE', 'PARTIEL', 'ANNULE') NOT NULL DEFAULT 'PAYE',
    numero_telephone_mobile VARCHAR(30) NULL,
    reference_operateur VARCHAR(100) NULL,
    est_paiement_local BOOLEAN NOT NULL DEFAULT FALSE,
    commentaire VARCHAR(255) NULL,
    enregistre_par_user_id INT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_paiement_montant CHECK (montant > 0),
    CONSTRAINT fk_paiements_school FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE,
    CONSTRAINT fk_paiements_eleve FOREIGN KEY (eleve_id) REFERENCES eleves(id) ON DELETE CASCADE,
    CONSTRAINT fk_paiements_user FOREIGN KEY (enregistre_par_user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 15. Table des bulletins enregistrés
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS bulletins (
    id INT AUTO_INCREMENT PRIMARY KEY,
    school_id INT NOT NULL,
    eleve_id INT NOT NULL,
    classe_id INT NOT NULL,
    periode_id INT NOT NULL,
    annee_scolaire_id INT NOT NULL,
    moyenne_generale DECIMAL(4,2) NOT NULL,
    rang INT NULL,
    effectif_total INT NOT NULL,
    total_absences INT NOT NULL DEFAULT 0,
    total_non_justifiees INT NOT NULL DEFAULT 0,
    mention VARCHAR(80) NULL,
    appreciation_conseil TEXT NULL,
    date_generation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_bulletin_periode (eleve_id, periode_id),
    CONSTRAINT fk_bulletins_school FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE,
    CONSTRAINT fk_bulletins_eleve FOREIGN KEY (eleve_id) REFERENCES eleves(id) ON DELETE CASCADE,
    CONSTRAINT fk_bulletins_classe FOREIGN KEY (classe_id) REFERENCES classes(id) ON DELETE RESTRICT,
    CONSTRAINT fk_bulletins_periode FOREIGN KEY (periode_id) REFERENCES periodes(id) ON DELETE RESTRICT,
    CONSTRAINT fk_bulletins_annee FOREIGN KEY (annee_scolaire_id) REFERENCES annees_scolaires(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 16. Table des notifications
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS notifications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    school_id INT NULL,
    user_id INT NOT NULL,
    titre VARCHAR(120) NOT NULL,
    message TEXT NOT NULL,
    type ENUM('INFO', 'ALERTE', 'SUCCES') NOT NULL DEFAULT 'INFO',
    lu BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notifications_school FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE,
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 17. Table du journal d'activité (Audit logs)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS activity_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    school_id INT NULL,
    user_id INT NULL,
    user_full_name VARCHAR(120) NOT NULL,
    user_role VARCHAR(30) NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    target_entity VARCHAR(100) NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_logs_school FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Index de performance multi-tenant
CREATE INDEX idx_schools_code ON schools(code);
CREATE INDEX idx_users_school ON users(school_id);
CREATE INDEX idx_eleves_school_classe ON eleves(school_id, classe_id);
CREATE INDEX idx_notes_school_eleve ON notes(school_id, eleve_id);
CREATE INDEX idx_absences_school_eleve ON absences(school_id, eleve_id);
CREATE INDEX idx_paiements_school_date ON paiements(school_id, date_paiement);
CREATE INDEX idx_activity_logs_school ON activity_logs(school_id, timestamp);
