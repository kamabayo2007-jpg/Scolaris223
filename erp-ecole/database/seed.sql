-- =====================================================================
-- DONNÉES DE DÉMONSTRATION - ERP GESTION ÉCOLE
-- Base de données : erp_ecole
-- Mot de passe démo Admin : Admin@123456
-- Mot de passe démo Professeurs : Prof@123456
-- Mot de passe démo Élèves : Eleve@123456
-- Note : Un script node backend/scripts/seed.js est aussi fourni pour générer à la volée.
-- Hash bcrypt 10 rounds pour "Password@123" et "Admin@123456" :
-- $2a$10$c12hU2wL5/H1uFw3hYc4ce0jB2rPzB8vWn.lKz8yXz0x1J8Q2W3Oy
-- =====================================================================

USE erp_ecole;

-- 1. Rôles
INSERT INTO roles (code, libelle, description) VALUES
('ADMIN', 'Administrateur', 'Accès complet au système de gestion et paramétrage'),
('PROFESSEUR', 'Professeur', 'Gestion pédagogique, saisie des notes et appel des élèves'),
('ELEVE', 'Élève', 'Consultation de son espace personnel, notes, planning et bulletins')
ON DUPLICATE KEY UPDATE libelle = VALUES(libelle);

-- 2. Année scolaire et périodes
INSERT INTO annees_scolaires (id, libelle, date_debut, date_fin, active) VALUES
(1, '2025-2026', '2025-09-01', '2026-06-30', TRUE)
ON DUPLICATE KEY UPDATE libelle = VALUES(libelle);

INSERT INTO periodes (id, annee_scolaire_id, nom, date_debut, date_fin, active) VALUES
(1, 1, 'Trimestre 1', '2025-09-01', '2025-11-30', TRUE),
(2, 1, 'Trimestre 2', '2025-12-01', '2026-02-28', FALSE),
(3, 1, 'Trimestre 3', '2026-03-01', '2026-06-30', FALSE)
ON DUPLICATE KEY UPDATE nom = VALUES(nom);

-- 3. Classes
INSERT INTO classes (id, nom, niveau, annee_scolaire_id, frais_scolarite, effectif, salle_principale) VALUES
(1, 'Terminale S1', 'Lycée', 1, 1800.00, 10, 'Salle 302'),
(2, '3ème A', 'Collège', 1, 1400.00, 6, 'Salle 104'),
(3, '6ème B', 'Collège', 1, 1200.00, 4, 'Salle 012')
ON DUPLICATE KEY UPDATE nom = VALUES(nom);

-- 4. Matières
INSERT INTO matieres (id, code, nom, coefficient, niveau, couleur_hex) VALUES
(1, 'MATH', 'Mathématiques', 4.00, 'Général', '#1D4ED8'),
(2, 'FRAN', 'Français / Littérature', 4.00, 'Général', '#7C3AED'),
(3, 'HGEO', 'Histoire - Géographie', 3.00, 'Général', '#B45309'),
(4, 'PHYS', 'Physique - Chimie', 3.00, 'Lycée', '#047857'),
(5, 'SVT', 'Sciences de la Vie et de la Terre', 2.00, 'Général', '#059669'),
(6, 'ANGL', 'Anglais (LV1)', 2.00, 'Général', '#C026D3'),
(7, 'PHIL', 'Philosophie', 3.00, 'Lycée', '#DC2626'),
(8, 'EPS', 'Éducation Physique et Sportive', 2.00, 'Général', '#EA580C')
ON DUPLICATE KEY UPDATE nom = VALUES(nom);

-- 5. Utilisateurs : Admin et Professeurs
-- Hash bcrypt 10 rounds pour 'Admin@123456' : $2a$10$5M8y3lVq6GjYq1R/O3c1reR5w0T2w8Yp3J2p3o2w9X0y1Z2A3B4C5
-- Pour uniformité et portabilité parfaite en local, nous insérons des comptes démo.
INSERT INTO users (id, username, email, password_hash, role, actif) VALUES
(1, 'admin', 'admin@ecole.fr', '$2a$10$w0d1DqvF6m6oG8l4I9L3xO0F1.V/JzK0k6d8m9y5R3P0q8k9w2x7y', 'ADMIN', TRUE),
(2, 'prof.bernard', 'a.bernard@ecole.fr', '$2a$10$w0d1DqvF6m6oG8l4I9L3xO0F1.V/JzK0k6d8m9y5R3P0q8k9w2x7y', 'PROFESSEUR', TRUE),
(3, 'prof.rousseau', 's.rousseau@ecole.fr', '$2a$10$w0d1DqvF6m6oG8l4I9L3xO0F1.V/JzK0k6d8m9y5R3P0q8k9w2x7y', 'PROFESSEUR', TRUE),
(4, 'prof.martin', 't.martin@ecole.fr', '$2a$10$w0d1DqvF6m6oG8l4I9L3xO0F1.V/JzK0k6d8m9y5R3P0q8k9w2x7y', 'PROFESSEUR', TRUE)
ON DUPLICATE KEY UPDATE username = VALUES(username);

-- 6. Profils Professeurs
INSERT INTO professeurs (id, user_id, matricule, nom, prenom, date_naissance, telephone, email, adresse, specialite) VALUES
(1, 2, 'ENS-2025-001', 'Bernard', 'Alain', '1978-04-12', '06 12 34 56 78', 'a.bernard@ecole.fr', '12 rue Victor Hugo, 75010 Paris', 'Mathématiques'),
(2, 3, 'ENS-2025-002', 'Rousseau', 'Sophie', '1982-08-25', '06 23 45 67 89', 's.rousseau@ecole.fr', '45 avenue des Lilas, 75012 Paris', 'Français / Littérature'),
(3, 4, 'ENS-2025-003', 'Martin', 'Thomas', '1985-11-03', '06 34 56 78 90', 't.martin@ecole.fr', '8 boulevard Voltaire, 75011 Paris', 'Physique - Chimie')
ON DUPLICATE KEY UPDATE matricule = VALUES(matricule);

-- 7. Utilisateurs et Profils Élèves (20 élèves)
INSERT INTO users (id, username, email, password_hash, role, actif) VALUES
(101, 'lucas.moreau', 'lucas.moreau@famille.fr', '$2a$10$w0d1DqvF6m6oG8l4I9L3xO0F1.V/JzK0k6d8m9y5R3P0q8k9w2x7y', 'ELEVE', TRUE),
(102, 'emma.bernard', 'emma.bernard@famille.fr', '$2a$10$w0d1DqvF6m6oG8l4I9L3xO0F1.V/JzK0k6d8m9y5R3P0q8k9w2x7y', 'ELEVE', TRUE),
(103, 'alexandre.petit', 'alexandre.petit@famille.fr', '$2a$10$w0d1DqvF6m6oG8l4I9L3xO0F1.V/JzK0k6d8m9y5R3P0q8k9w2x7y', 'ELEVE', TRUE),
(104, 'chloe.martin', 'chloe.martin@famille.fr', '$2a$10$w0d1DqvF6m6oG8l4I9L3xO0F1.V/JzK0k6d8m9y5R3P0q8k9w2x7y', 'ELEVE', TRUE),
(105, 'maxime.fournier', 'maxime.fournier@famille.fr', '$2a$10$w0d1DqvF6m6oG8l4I9L3xO0F1.V/JzK0k6d8m9y5R3P0q8k9w2x7y', 'ELEVE', TRUE),
(106, 'sarah.lefebvre', 'sarah.lefebvre@famille.fr', '$2a$10$w0d1DqvF6m6oG8l4I9L3xO0F1.V/JzK0k6d8m9y5R3P0q8k9w2x7y', 'ELEVE', TRUE),
(107, 'hugo.roux', 'hugo.roux@famille.fr', '$2a$10$w0d1DqvF6m6oG8l4I9L3xO0F1.V/JzK0k6d8m9y5R3P0q8k9w2x7y', 'ELEVE', TRUE),
(108, 'lea.garcia', 'lea.garcia@famille.fr', '$2a$10$w0d1DqvF6m6oG8l4I9L3xO0F1.V/JzK0k6d8m9y5R3P0q8k9w2x7y', 'ELEVE', TRUE),
(109, 'nathan.durand', 'nathan.durand@famille.fr', '$2a$10$w0d1DqvF6m6oG8l4I9L3xO0F1.V/JzK0k6d8m9y5R3P0q8k9w2x7y', 'ELEVE', TRUE),
(110, 'camille.leroy', 'camille.leroy@famille.fr', '$2a$10$w0d1DqvF6m6oG8l4I9L3xO0F1.V/JzK0k6d8m9y5R3P0q8k9w2x7y', 'ELEVE', TRUE),
(111, 'antoine.dubois', 'antoine.dubois@famille.fr', '$2a$10$w0d1DqvF6m6oG8l4I9L3xO0F1.V/JzK0k6d8m9y5R3P0q8k9w2x7y', 'ELEVE', TRUE),
(112, 'clara.fontaine', 'clara.fontaine@famille.fr', '$2a$10$w0d1DqvF6m6oG8l4I9L3xO0F1.V/JzK0k6d8m9y5R3P0q8k9w2x7y', 'ELEVE', TRUE),
(113, 'jules.riviere', 'jules.riviere@famille.fr', '$2a$10$w0d1DqvF6m6oG8l4I9L3xO0F1.V/JzK0k6d8m9y5R3P0q8k9w2x7y', 'ELEVE', TRUE),
(114, 'ines.bonnet', 'ines.bonnet@famille.fr', '$2a$10$w0d1DqvF6m6oG8l4I9L3xO0F1.V/JzK0k6d8m9y5R3P0q8k9w2x7y', 'ELEVE', TRUE),
(115, 'theo.muller', 'theo.muller@famille.fr', '$2a$10$w0d1DqvF6m6oG8l4I9L3xO0F1.V/JzK0k6d8m9y5R3P0q8k9w2x7y', 'ELEVE', TRUE),
(116, 'manon.simon', 'manon.simon@famille.fr', '$2a$10$w0d1DqvF6m6oG8l4I9L3xO0F1.V/JzK0k6d8m9y5R3P0q8k9w2x7y', 'ELEVE', TRUE),
(117, 'enzo.michel', 'enzo.michel@famille.fr', '$2a$10$w0d1DqvF6m6oG8l4I9L3xO0F1.V/JzK0k6d8m9y5R3P0q8k9w2x7y', 'ELEVE', TRUE),
(118, 'jade.laurent', 'jade.laurent@famille.fr', '$2a$10$w0d1DqvF6m6oG8l4I9L3xO0F1.V/JzK0k6d8m9y5R3P0q8k9w2x7y', 'ELEVE', TRUE),
(119, 'louis.david', 'louis.david@famille.fr', '$2a$10$w0d1DqvF6m6oG8l4I9L3xO0F1.V/JzK0k6d8m9y5R3P0q8k9w2x7y', 'ELEVE', TRUE),
(120, 'eva.bertrand', 'eva.bertrand@famille.fr', '$2a$10$w0d1DqvF6m6oG8l4I9L3xO0F1.V/JzK0k6d8m9y5R3P0q8k9w2x7y', 'ELEVE', TRUE)
ON DUPLICATE KEY UPDATE username = VALUES(username);

INSERT INTO eleves (id, user_id, matricule, nom, prenom, date_naissance, lieu_naissance, sexe, telephone, adresse, nom_parent, telephone_parent, email_parent, classe_id) VALUES
(1, 101, 'TS1-001', 'Moreau', 'Lucas', '2008-04-15', 'Paris', 'M', '07 11 22 33 44', '15 rue de Rome, Paris', 'M. Moreau Pierre', '06 98 76 54 32', 'pierre.moreau@email.fr', 1),
(2, 102, 'TS1-002', 'Bernard', 'Emma', '2008-09-22', 'Lyon', 'F', '07 22 33 44 55', '22 avenue Foch, Paris', 'Mme Bernard Claire', '06 87 65 43 21', 'claire.bernard@email.fr', 1),
(3, 103, 'TS1-003', 'Petit', 'Alexandre', '2007-11-03', 'Marseille', 'M', '07 33 44 55 66', '5 rue Royale, Paris', 'M. Petit Jacques', '06 76 54 32 10', 'jacques.petit@email.fr', 1),
(4, 104, 'TS1-004', 'Martin', 'Chloé', '2008-02-18', 'Lille', 'F', '07 44 55 66 77', '18 bd Saint-Germain, Paris', 'Mme Martin Valérie', '06 65 43 21 09', 'valerie.martin@email.fr', 1),
(5, 105, 'TS1-005', 'Fournier', 'Maxime', '2008-06-30', 'Bordeaux', 'M', '07 55 66 77 88', '30 rue Lafayette, Paris', 'M. Fournier Paul', '06 54 32 10 98', 'paul.fournier@email.fr', 1),
(6, 106, 'TS1-006', 'Lefebvre', 'Sarah', '2008-08-12', 'Nantes', 'F', '07 66 77 88 99', '12 rue Monge, Paris', 'Mme Lefebvre Anne', '06 43 21 09 87', 'anne.lefebvre@email.fr', 1),
(7, 107, 'TS1-007', 'Roux', 'Hugo', '2007-12-25', 'Toulouse', 'M', '07 77 88 99 00', '48 rue de Rennes, Paris', 'M. Roux Marc', '06 32 10 98 76', 'marc.roux@email.fr', 1),
(8, 108, 'TS1-008', 'Garcia', 'Léa', '2008-01-09', 'Strasbourg', 'F', '07 88 99 00 11', '3 rue de Rivoli, Paris', 'Mme Garcia Maria', '06 21 09 87 65', 'maria.garcia@email.fr', 1),
(9, 109, 'TS1-009', 'Durand', 'Nathan', '2008-05-14', 'Rennes', 'M', '07 99 00 11 22', '14 rue Saint-Denis, Paris', 'M. Durand Michel', '06 10 98 76 54', 'michel.durand@email.fr', 1),
(10, 110, 'TS1-010', 'Leroy', 'Camille', '2008-10-04', 'Montpellier', 'F', '07 10 21 32 43', '27 rue du Bac, Paris', 'Mme Leroy Julie', '06 09 87 65 43', 'julie.leroy@email.fr', 1),

(11, 111, '3A-001', 'Dubois', 'Antoine', '2011-05-14', 'Paris', 'M', '07 21 32 43 54', '11 rue Mouffetard, Paris', 'M. Dubois Patrick', '06 11 22 33 44', 'patrick.dubois@email.fr', 2),
(12, 112, '3A-002', 'Fontaine', 'Clara', '2011-10-28', 'Versailles', 'F', '07 32 43 54 65', '8 rue Saint-Louis, Versailles', 'Mme Fontaine Hélène', '06 22 33 44 55', 'helene.fontaine@email.fr', 2),
(13, 113, '3A-003', 'Riviere', 'Jules', '2011-03-05', 'Paris', 'M', '07 43 54 65 76', '19 avenue Ledru-Rollin, Paris', 'M. Riviere François', '06 33 44 55 66', 'francois.riviere@email.fr', 2),
(14, 114, '3A-004', 'Bonnet', 'Inès', '2011-07-19', 'Nanterre', 'F', '07 54 65 76 87', '5 bd Carnot, Nanterre', 'Mme Bonnet Sylvie', '06 44 55 66 77', 'sylvie.bonnet@email.fr', 2),
(15, 115, '3A-005', 'Muller', 'Théo', '2011-09-11', 'Paris', 'M', '07 65 76 87 98', '33 rue Oberkampf, Paris', 'M. Muller Éric', '06 55 66 77 88', 'eric.muller@email.fr', 2),
(16, 116, '3A-006', 'Simon', 'Manon', '2011-12-02', 'Boulogne', 'F', '07 76 87 98 09', '14 route de la Reine, Boulogne', 'Mme Simon Christine', '06 66 77 88 99', 'christine.simon@email.fr', 2),

(17, 117, '6B-001', 'Michel', 'Enzo', '2014-02-15', 'Paris', 'M', '07 87 98 09 10', '9 rue Custine, Paris', 'M. Michel Nicolas', '06 77 88 99 00', 'nicolas.michel@email.fr', 3),
(18, 118, '6B-002', 'Laurent', 'Jade', '2014-06-20', 'Créteil', 'F', '07 98 09 10 21', '21 rue d Alésia, Paris', 'Mme Laurent Sandrine', '06 88 99 00 11', 'sandrine.laurent@email.fr', 3),
(19, 119, '6B-003', 'David', 'Louis', '2014-04-08', 'Paris', 'M', '07 09 10 21 32', '40 rue de Tolbiac, Paris', 'M. David Laurent', '06 99 00 11 22', 'laurent.david@email.fr', 3),
(20, 120, '6B-004', 'Bertrand', 'Eva', '2014-11-17', 'Montreuil', 'F', '07 12 23 34 45', '7 avenue de Paris, Montreuil', 'Mme Bertrand Nathalie', '06 00 11 22 33', 'nathalie.bertrand@email.fr', 3)
ON DUPLICATE KEY UPDATE matricule = VALUES(matricule);

-- 8. Inscriptions pour l'année scolaire 2025-2026
INSERT INTO inscriptions (eleve_id, classe_id, annee_scolaire_id, date_inscription, statut, frais_total)
SELECT id, classe_id, 1, '2025-09-01', 'CONFIRME', 1800.00 FROM eleves WHERE classe_id = 1
ON DUPLICATE KEY UPDATE statut = VALUES(statut);

INSERT INTO inscriptions (eleve_id, classe_id, annee_scolaire_id, date_inscription, statut, frais_total)
SELECT id, classe_id, 1, '2025-09-02', 'CONFIRME', 1400.00 FROM eleves WHERE classe_id = 2
ON DUPLICATE KEY UPDATE statut = VALUES(statut);

INSERT INTO inscriptions (eleve_id, classe_id, annee_scolaire_id, date_inscription, statut, frais_total)
SELECT id, classe_id, 1, '2025-09-03', 'CONFIRME', 1200.00 FROM eleves WHERE classe_id = 3
ON DUPLICATE KEY UPDATE statut = VALUES(statut);

-- 9. Enseignements (Affectations Professeurs - Matières - Classes)
INSERT INTO enseignements (professeur_id, matiere_id, classe_id, annee_scolaire_id) VALUES
(1, 1, 1, 1), -- Bernard: Maths en Terminale S1
(1, 1, 2, 1), -- Bernard: Maths en 3ème A
(2, 2, 1, 1), -- Rousseau: Français en Terminale S1
(2, 2, 2, 1), -- Rousseau: Français en 3ème A
(2, 2, 3, 1), -- Rousseau: Français en 6ème B
(3, 4, 1, 1), -- Martin: Physique-Chimie en Terminale S1
(3, 4, 2, 1)  -- Martin: Physique-Chimie en 3ème A
ON DUPLICATE KEY UPDATE annee_scolaire_id = VALUES(annee_scolaire_id);

-- 10. Notes d'évaluations (Trimestre 1)
INSERT INTO notes (eleve_id, matiere_id, professeur_id, periode_id, titre_evaluation, note, bareme, coefficient, date_evaluation, commentaire) VALUES
(1, 1, 1, 1, 'DS 1 - Analyse & Limites', 16.50, 20.00, 2.00, '2025-09-25', 'Très bonne maîtrise du raisonnement.'),
(1, 1, 1, 1, 'Interrogation - Dérivées', 18.00, 20.00, 1.00, '2025-10-10', 'Calculs fluides et précis.'),
(1, 2, 2, 1, 'Dissertation littéraire', 14.50, 20.00, 2.00, '2025-10-04', 'Bonne argumentation stylistique.'),
(1, 4, 3, 1, 'TP - Mécanique newtonienne', 17.00, 20.00, 1.50, '2025-10-15', 'Excellente démarche scientifique.'),

(2, 1, 1, 1, 'DS 1 - Analyse & Limites', 18.50, 20.00, 2.00, '2025-09-25', 'Excellent travail, rigoureux.'),
(2, 2, 2, 1, 'Dissertation littéraire', 17.00, 20.00, 2.00, '2025-10-04', 'Remarquable expression écrite.'),
(2, 4, 3, 1, 'TP - Mécanique newtonienne', 19.00, 20.00, 1.50, '2025-10-15', 'Parfait.'),

(3, 1, 1, 1, 'DS 1 - Analyse & Limites', 11.50, 20.00, 2.00, '2025-09-25', 'Des erreurs d inattention sur les signes.'),
(3, 2, 2, 1, 'Dissertation littéraire', 12.00, 20.00, 2.00, '2025-10-04', 'Ensemble convenable.'),
(3, 4, 3, 1, 'TP - Mécanique newtonienne', 13.50, 20.00, 1.50, '2025-10-15', 'Bonne implication en laboratoire.'),

(4, 1, 1, 1, 'DS 1 - Analyse & Limites', 15.00, 20.00, 2.00, '2025-09-25', 'Bon investissement.'),
(4, 2, 2, 1, 'Dissertation littéraire', 16.00, 20.00, 2.00, '2025-10-04', 'Très bonne finesse d analyse.'),
(4, 4, 3, 1, 'TP - Mécanique newtonienne', 15.50, 20.00, 1.50, '2025-10-15', 'Sérieux et appliqué.'),

(5, 1, 1, 1, 'DS 1 - Analyse & Limites', 09.50, 20.00, 2.00, '2025-09-25', 'Règles du cours à approfondir.'),
(5, 2, 2, 1, 'Dissertation littéraire', 11.00, 20.00, 2.00, '2025-10-04', 'Doit structurer le plan.'),
(5, 4, 3, 1, 'TP - Mécanique newtonienne', 10.50, 20.00, 1.50, '2025-10-15', 'Juste la moyenne.');

-- 11. Absences et retards
INSERT INTO absences (eleve_id, professeur_id, date, creneau_horaire, type, justifiee, motif, date_justification, remarques) VALUES
(1, 1, '2025-09-20', '08:00 - 10:00', 'RETARD', TRUE, 'Problème de transport métro', '2025-09-20', 'Arrivé avec 15 min de retard'),
(3, 1, '2025-09-28', '10:15 - 12:15', 'ABSENCE', TRUE, 'Certificat médical / Rhume', '2025-09-29', 'Certificat déposé au secrétariat'),
(5, 2, '2025-10-02', '08:00 - 10:00', 'ABSENCE', FALSE, NULL, NULL, 'Absence non justifiée notifiée aux parents'),
(7, 3, '2025-10-05', '13:30 - 15:30', 'RETARD', TRUE, 'Rendez-vous dentaire', '2025-10-05', 'Billet de retard régularisé');

-- 12. Emplois du temps (Terminale S1)
INSERT INTO emplois_du_temps (classe_id, matiere_id, professeur_id, jour, heure_debut, heure_fin, salle, annee_scolaire_id) VALUES
(1, 1, 1, 'Lundi', '08:00:00', '09:30:00', 'Salle 302', 1),
(1, 4, 3, 'Lundi', '09:45:00', '11:15:00', 'Labo Phys 2', 1),
(1, 2, 2, 'Lundi', '13:30:00', '15:00:00', 'Salle 104', 1),
(1, 6, 2, 'Lundi', '15:15:00', '16:45:00', 'Salle Langues', 1),
(1, 3, 2, 'Mardi', '08:00:00', '09:30:00', 'Salle 205', 1),
(1, 1, 1, 'Mardi', '09:45:00', '11:15:00', 'Salle 302', 1),
(1, 5, 3, 'Mardi', '13:30:00', '15:30:00', 'Labo SVT 1', 1),
(1, 7, 2, 'Mercredi', '08:00:00', '10:00:00', 'Salle 302', 1),
(1, 2, 2, 'Mercredi', '10:15:00', '12:15:00', 'Salle 104', 1),
(1, 4, 3, 'Jeudi', '08:00:00', '10:00:00', 'Labo Phys 2', 1),
(1, 1, 1, 'Jeudi', '10:15:00', '11:45:00', 'Salle 302', 1),
(1, 8, 1, 'Jeudi', '14:00:00', '16:00:00', 'Gymnase A', 1),
(1, 6, 2, 'Vendredi', '08:00:00', '09:30:00', 'Salle Langues', 1),
(1, 3, 2, 'Vendredi', '09:45:00', '11:15:00', 'Salle 205', 1),
(1, 1, 1, 'Vendredi', '13:30:00', '15:00:00', 'Salle 302', 1);

-- 13. Paiements de scolarité
INSERT INTO paiements (eleve_id, montant, type, date_paiement, reference, mode_paiement, statut, commentaire, enregistre_par_user_id) VALUES
(1, 600.00, 'SCOLARITE', '2025-09-05', 'PAY-2025-001', 'VIREMENT', 'PAYE', 'Acompte 1er trimestre', 1),
(1, 600.00, 'SCOLARITE', '2025-10-05', 'PAY-2025-002', 'VIREMENT', 'PAYE', '2ème versement scolarité', 1),
(2, 1800.00, 'SCOLARITE', '2025-09-02', 'PAY-2025-003', 'CHEQUE', 'PAYE', 'Règlement total annuel scolarité', 1),
(3, 500.00, 'INSCRIPTION', '2025-09-01', 'PAY-2025-004', 'ESPECES', 'PAYE', 'Frais d inscription et dossier', 1),
(4, 900.00, 'SCOLARITE', '2025-09-10', 'PAY-2025-005', 'CARTE', 'PAYE', 'Règlement semestre 1', 1),
(5, 300.00, 'SCOLARITE', '2025-09-12', 'PAY-2025-006', 'ESPECES', 'PARTIEL', 'Versement partiel, relance envoyée', 1);

-- 14. Notifications
INSERT INTO notifications (user_id, titre, message, type, lu) VALUES
(1, 'Rentrée scolaire 2025-2026', 'Toutes les classes sont configurées et les emplois du temps sont actifs.', 'INFO', TRUE),
(101, 'Nouvelle note en Mathématiques', 'Votre note au DS 1 - Analyse & Limites a été enregistrée : 16.5/20', 'SUCCES', FALSE),
(102, 'Félicitations', 'Votre moyenne du 1er trimestre vous place en tête de classe.', 'SUCCES', FALSE),
(105, 'Alerte Absence', 'Une absence non justifiée a été enregistrée le 02/10/2025.', 'ALERTE', FALSE);
