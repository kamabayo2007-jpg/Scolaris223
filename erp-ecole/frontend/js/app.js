/**
 * Application principale SPA - ERP Gestion École
 */

let vueActuelle = 'dashboard';

// Démarrage de l'application au chargement du DOM
document.addEventListener('DOMContentLoaded', () => {
    verifierSession();
});

/**
 * Bascule la visibilité du menu mobile
 */
function toggleSidebar() {
    const sidebar = document.getElementById('app-sidebar');
    const overlay = document.getElementById('sidebar-overlay');
    sidebar.classList.toggle('open');
    overlay.classList.toggle('active');
}

/**
 * Initialise le menu latéral en fonction du rôle connecté
 */
function initialiserMenuEtNavigation() {
    const role = getRole();
    const navContainer = document.getElementById('sidebar-nav-links');
    if (!navContainer) return;

    let liens = [];

    if (role === 'ADMIN') {
        liens = [
            { id: 'dashboard', titre: 'Tableau de bord', icone: '📊' },
            { id: 'eleves', titre: 'Élèves & Inscriptions', icone: '👨‍🎓' },
            { id: 'professeurs', titre: 'Professeurs', icone: '👨‍🏫' },
            { id: 'classes', titre: 'Classes', icone: '🏫' },
            { id: 'matieres', titre: 'Matières & Coeffs', icone: '📚' },
            { id: 'notes', titre: 'Notes & Moyennes', icone: '📝' },
            { id: 'absences', titre: 'Absences & Retards', icone: '⏱️' },
            { id: 'emploi-du-temps', titre: 'Emplois du temps', icone: '🗓️' },
            { id: 'paiements', titre: 'Paiements Scolarité', icone: '💳' },
            { id: 'bulletins', titre: 'Bulletins Scolaires', icone: '📜' },
            { id: 'utilisateurs', titre: 'Comptes Utilisateurs', icone: '⚙️' }
        ];
    } else if (role === 'DIRECTION') {
        liens = [
            { id: 'dashboard', titre: 'Tableau de bord', icone: '📊' },
            { id: 'eleves', titre: 'Élèves & Inscriptions', icone: '👨‍🎓' },
            { id: 'professeurs', titre: 'Corps Enseignant', icone: '👨‍🏫' },
            { id: 'classes', titre: 'Classes & Effectifs', icone: '🏫' },
            { id: 'notes', titre: 'Notes & Résultats', icone: '📝' },
            { id: 'absences', titre: 'Suivi de l\'Assiduité', icone: '⏱️' },
            { id: 'emploi-du-temps', titre: 'Emplois du temps', icone: '🗓️' },
            { id: 'bulletins', titre: 'Conseils & Bulletins', icone: '📜' }
        ];
    } else if (role === 'PARENT') {
        liens = [
            { id: 'dashboard', titre: 'Espace Famille', icone: '🏠' },
            { id: 'notes', titre: 'Notes & Moyennes', icone: '📝' },
            { id: 'absences', titre: 'Absences & Retards', icone: '⏱️' },
            { id: 'emploi-du-temps', titre: 'Planning des Cours', icone: '🗓️' },
            { id: 'paiements', titre: 'Scolarité & Facturation', icone: '💳' },
            { id: 'bulletins', titre: 'Bulletins Officiels', icone: '📜' }
        ];
    } else if (role === 'COMPTABLE') {
        liens = [
            { id: 'dashboard', titre: 'Synthèse Financière', icone: '📊' },
            { id: 'paiements', titre: 'Paiements & Reçus', icone: '💳' },
            { id: 'eleves', titre: 'Élèves & Dossiers', icone: '👨‍🎓' }
        ];
    } else if (role === 'PROFESSEUR') {
        liens = [
            { id: 'dashboard', titre: 'Mon Tableau de bord', icone: '📊' },
            { id: 'classes', titre: 'Mes Classes & Élèves', icone: '🏫' },
            { id: 'notes', titre: 'Saisie des Notes', icone: '📝' },
            { id: 'absences', titre: 'Faire l\'Appel', icone: '⏱️' },
            { id: 'emploi-du-temps', titre: 'Mon Emploi du temps', icone: '🗓️' },
            { id: 'bulletins', titre: 'Bulletins de classe', icone: '📜' }
        ];
    } else if (role === 'ELEVE') {
        liens = [
            { id: 'dashboard', titre: 'Mon Espace', icone: '🏠' },
            { id: 'notes', titre: 'Mes Notes & Moyennes', icone: '📝' },
            { id: 'absences', titre: 'Mes Absences', icone: '⏱️' },
            { id: 'emploi-du-temps', titre: 'Mon Emploi du temps', icone: '🗓️' },
            { id: 'paiements', titre: 'Mes Paiements', icone: '💳' },
            { id: 'bulletins', titre: 'Mon Bulletin Officiel', icone: '📜' }
        ];
    }

    navContainer.innerHTML = liens.map(lien => `
        <div class="nav-item ${lien.id === 'dashboard' ? 'active' : ''}" 
             id="nav-link-${lien.id}" 
             onclick="naviguerVers('${lien.id}')">
            <span class="nav-icon">${lien.icone}</span>
            <span class="nav-label">${lien.titre}</span>
        </div>
    `).join('');

    naviguerVers('dashboard');
}

/**
 * Routeur SPA - Navigation entre les vues
 */
function naviguerVers(vueId) {
    vueActuelle = vueId;

    // Fermer le menu mobile s'il est ouvert
    const sidebar = document.getElementById('app-sidebar');
    const overlay = document.getElementById('sidebar-overlay');
    if (sidebar) sidebar.classList.remove('open');
    if (overlay) overlay.classList.remove('active');

    // Mettre à jour l'élément actif dans la sidebar
    document.querySelectorAll('.nav-item').forEach(el => el.classList.remove('active'));
    const actifEl = document.getElementById(`nav-link-${vueId}`);
    if (actifEl) actifEl.classList.add('active');

    const role = getRole();

    switch (vueId) {
        case 'dashboard':
            if (role === 'ADMIN' || role === 'DIRECTION') chargerDashboardAdmin();
            else if (role === 'PROFESSEUR') chargerDashboardProfesseur();
            else if (role === 'COMPTABLE') chargerVuePaiements();
            else chargerDashboardEleve();
            break;
        case 'eleves':
            chargerVueEleves();
            break;
        case 'professeurs':
            chargerVueProfesseurs();
            break;
        case 'classes':
            chargerVueClasses();
            break;
        case 'matieres':
            chargerVueMatieres();
            break;
        case 'notes':
            chargerVueNotes();
            break;
        case 'absences':
            chargerVueAbsences();
            break;
        case 'emploi-du-temps':
            chargerVueEmploiDuTemps();
            break;
        case 'paiements':
            chargerVuePaiements();
            break;
        case 'bulletins':
            chargerVueBulletins();
            break;
        case 'utilisateurs':
            chargerVueUsers();
            break;
        default:
            chargerDashboardAdmin();
    }
}

/* =====================================================================
   VUE 1 : DASHBOARDS PAR RÔLE
   ===================================================================== */
async function chargerDashboardAdmin() {
    const container = document.getElementById('main-view-container');
    container.innerHTML = '<div class="loader-container"><div class="spinner"></div><p>Chargement des statistiques...</p></div>';

    try {
        const res = await api.get('/statistiques/admin');
        const s = res.statistiques;

        container.innerHTML = `
            <div class="view-header">
                <div class="view-title-group">
                    <h1>Tableau de Bord Administrateur</h1>
                    <p class="view-subtitle">Vue d'ensemble en temps réel de l'établissement scolaire</p>
                </div>
                <div class="header-actions">
                    <button class="btn btn-primary" onclick="ouvrirModaleInscrireEleve()">+ Inscrire un élève</button>
                    <button class="btn btn-secondary" onclick="ouvrirModaleEnregistrerPaiement()">+ Encaisser scolarité</button>
                </div>
            </div>

            <!-- Cartes Statistiques Principales -->
            <div class="stats-grid">
                <div class="stat-card">
                    <div class="stat-card-header">
                        <span class="stat-title">Total Élèves Actifs</span>
                        <span class="stat-icon" style="background:#EFF6FF; color:#1D4ED8;">👨‍🎓</span>
                    </div>
                    <div class="stat-value">${s.effectifs.total_eleves}</div>
                    <div class="stat-desc">Répartis sur ${s.effectifs.total_classes} classes</div>
                </div>

                <div class="stat-card">
                    <div class="stat-card-header">
                        <span class="stat-title">Corps Professoral</span>
                        <span class="stat-icon" style="background:#FEF3C7; color:#B45309;">👨‍🏫</span>
                    </div>
                    <div class="stat-value">${s.effectifs.total_professeurs}</div>
                    <div class="stat-desc">Pour ${s.effectifs.total_matieres} matières enseignées</div>
                </div>

                <div class="stat-card">
                    <div class="stat-card-header">
                        <span class="stat-title">Scolarité Encaissée</span>
                        <span class="stat-icon" style="background:#DCFCE7; color:#15803D;">💳</span>
                    </div>
                    <div class="stat-value">${s.finances.total_encaisse.toLocaleString('fr-FR')} €</div>
                    <div class="stat-desc">Reste à recouvrer : ${s.finances.reste_a_recouvrer.toLocaleString('fr-FR')} €</div>
                </div>

                <div class="stat-card">
                    <div class="stat-card-header">
                        <span class="stat-title">Taux de Présence</span>
                        <span class="stat-icon" style="background:#FEE2E2; color:#B91C1C;">⏱️</span>
                    </div>
                    <div class="stat-value">${s.pedagogie.taux_presence}%</div>
                    <div class="stat-desc">${s.pedagogie.absences_non_justifiees} absence(s) non justifiée(s)</div>
                </div>
            </div>

            <!-- Grille 2 colonnes : Dernières inscriptions & Derniers paiements -->
            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(360px, 1fr)); gap: 20px; margin-top: 10px;">
                <div class="card-table-wrapper">
                    <div class="table-toolbar">
                        <h3>Dernières inscriptions d'élèves</h3>
                        <button class="btn btn-sm btn-outline" onclick="naviguerVers('eleves')">Voir tous</button>
                    </div>
                    <div class="table-responsive">
                        <table class="custom-table">
                            <thead>
                                <tr>
                                    <th>Matricule</th>
                                    <th>Nom & Prénom</th>
                                    <th>Classe</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${s.recents.inscriptions.map(e => `
                                    <tr>
                                        <td><strong>${e.matricule}</strong></td>
                                        <td>${e.nom} ${e.prenom}</td>
                                        <td><span class="badge badge-info">${e.nom_classe || 'Sans classe'}</span></td>
                                    </tr>
                                `).join('')}
                            </tbody>
                        </table>
                    </div>
                </div>

                <div class="card-table-wrapper">
                    <div class="table-toolbar">
                        <h3>Derniers règlements reçus</h3>
                        <button class="btn btn-sm btn-outline" onclick="naviguerVers('paiements')">Voir tous</button>
                    </div>
                    <div class="table-responsive">
                        <table class="custom-table">
                            <thead>
                                <tr>
                                    <th>Référence</th>
                                    <th>Élève</th>
                                    <th>Montant</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${s.recents.paiements.map(p => `
                                    <tr>
                                        <td><small>${p.reference}</small></td>
                                        <td>${p.nom_eleve} ${p.prenom_eleve}</td>
                                        <td><strong style="color:var(--success);">${parseFloat(p.montant).toFixed(2)} €</strong></td>
                                    </tr>
                                `).join('')}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        `;
    } catch (err) {
        container.innerHTML = `<div class="alert alert-danger">${err.message}</div>`;
    }
}

async function chargerDashboardProfesseur() {
    const container = document.getElementById('main-view-container');
    container.innerHTML = '<div class="loader-container"><div class="spinner"></div><p>Chargement de votre espace enseignant...</p></div>';

    try {
        const res = await api.get('/statistiques/professeur');
        const p = res.professeur;

        container.innerHTML = `
            <div class="view-header">
                <div class="view-title-group">
                    <h1>Espace Professeur</h1>
                    <p class="view-subtitle">Suivi de vos classes, saisie des notes et gestion des cours du jour</p>
                </div>
                <div class="header-actions">
                    <button class="btn btn-primary" onclick="ouvrirModaleSaisieNote()">+ Saisir une note</button>
                    <button class="btn btn-secondary" onclick="ouvrirModaleFaireAppel()">⏱️ Faire l'appel</button>
                </div>
            </div>

            <div class="stats-grid">
                <div class="stat-card">
                    <div class="stat-card-header"><span class="stat-title">Mes Élèves</span><span class="stat-icon">👨‍🎓</span></div>
                    <div class="stat-value">${p.total_eleves}</div>
                    <div class="stat-desc">Sur ${p.nombre_classes} classe(s) assignée(s)</div>
                </div>
                <div class="stat-card">
                    <div class="stat-card-header"><span class="stat-title">Cours du jour</span><span class="stat-icon">🗓️</span></div>
                    <div class="stat-value">${p.cours_aujourdhui.length}</div>
                    <div class="stat-desc">Séances programmées aujourd'hui</div>
                </div>
            </div>

            <div class="card-table-wrapper">
                <div class="table-toolbar">
                    <h3>Mes Classes & Matières assignées</h3>
                </div>
                <div class="table-responsive">
                    <table class="custom-table">
                        <thead>
                            <tr>
                                <th>Classe</th>
                                <th>Matière enseignée</th>
                                <th>Effectif</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${p.enseignements.map(ens => `
                                <tr>
                                    <td><strong>${ens.nom_classe}</strong></td>
                                    <td>${ens.nom_matiere}</td>
                                    <td>${ens.effectif_classe} élève(s)</td>
                                    <td>
                                        <button class="btn btn-sm btn-outline" onclick="naviguerVers('notes')">Noter</button>
                                        <button class="btn btn-sm btn-outline" onclick="ouvrirModaleFaireAppel(${ens.classe_id})">Appel</button>
                                    </td>
                                </tr>
                            `).join('')}
                        </tbody>
                    </table>
                </div>
            </div>
        `;
    } catch (err) {
        container.innerHTML = `<div class="alert alert-danger">${err.message}</div>`;
    }
}

async function chargerDashboardEleve() {
    const container = document.getElementById('main-view-container');
    container.innerHTML = '<div class="loader-container"><div class="spinner"></div><p>Chargement de votre dossier scolaire...</p></div>';

    try {
        const res = await api.get('/statistiques/eleve');
        const d = res.eleve;

        container.innerHTML = `
            <div class="view-header">
                <div class="view-title-group">
                    <h1>Bonjour, ${d.profil.prenom} ${d.profil.nom}</h1>
                    <p class="view-subtitle">Classe : <strong>${d.profil.nom_classe || 'Non affecté'}</strong> • Matricule : ${d.profil.matricule}</p>
                </div>
                <div class="header-actions">
                    <button class="btn btn-primary" onclick="ouvrirBulletinEleve(${d.profil.id})">📜 Consulter mon bulletin</button>
                </div>
            </div>

            <div class="stats-grid">
                <div class="stat-card">
                    <div class="stat-card-header"><span class="stat-title">Moyenne Générale</span><span class="stat-icon">🎓</span></div>
                    <div class="stat-value" style="color:var(--primary);">${d.moyenne_generale !== null ? d.moyenne_generale + '/20' : 'N/A'}</div>
                    <div class="stat-desc">Calculée sur l'ensemble des évaluations</div>
                </div>

                <div class="stat-card">
                    <div class="stat-card-header"><span class="stat-title">Assiduité</span><span class="stat-icon">⏱️</span></div>
                    <div class="stat-value">${d.absences.total_absences || 0}</div>
                    <div class="stat-desc">${d.absences.non_justifiees || 0} non justifiée(s) • ${d.absences.retards || 0} retard(s)</div>
                </div>

                <div class="stat-card">
                    <div class="stat-card-header"><span class="stat-title">Frais de Scolarité</span><span class="stat-icon">💳</span></div>
                    <div class="stat-value">${d.finances.total_paye} €</div>
                    <div class="stat-desc">Reste à payer : ${d.finances.reste_a_payer} €</div>
                </div>
            </div>

            <div class="card-table-wrapper">
                <div class="table-toolbar">
                    <h3>Mes Dernières Notes</h3>
                </div>
                <div class="table-responsive">
                    <table class="custom-table">
                        <thead>
                            <tr>
                                <th>Date</th>
                                <th>Matière</th>
                                <th>Évaluation</th>
                                <th>Note obtenue</th>
                                <th>Coefficient</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${d.dernieres_notes.map(n => `
                                <tr>
                                    <td>${n.date_evaluation}</td>
                                    <td><strong>${n.nom_matiere}</strong></td>
                                    <td>${n.titre_evaluation}</td>
                                    <td><span class="grade-pill ${getGradeClass(n.note, n.bareme)}">${n.note} / ${n.bareme}</span></td>
                                    <td>${n.coefficient}</td>
                                </tr>
                            `).join('')}
                        </tbody>
                    </table>
                </div>
            </div>
        `;
    } catch (err) {
        container.innerHTML = `<div class="alert alert-danger">${err.message}</div>`;
    }
}

function getGradeClass(note, bareme) {
    const sur20 = (parseFloat(note) / parseFloat(bareme)) * 20;
    if (sur20 >= 16) return 'grade-excellent';
    if (sur20 >= 12) return 'grade-good';
    if (sur20 >= 10) return 'grade-average';
    return 'grade-poor';
}

/* =====================================================================
   VUE 2 : GESTION DES ÉLÈVES (ADMIN)
   ===================================================================== */
async function chargerVueEleves() {
    const container = document.getElementById('main-view-container');
    container.innerHTML = '<div class="loader-container"><div class="spinner"></div><p>Chargement des élèves...</p></div>';

    try {
        const [resEleves, resClasses] = await Promise.all([
            api.get('/eleves'),
            api.get('/classes')
        ]);

        const eleves = resEleves.eleves;
        const classes = resClasses.classes;

        container.innerHTML = `
            <div class="view-header">
                <div class="view-title-group">
                    <h1>Gestion des Élèves</h1>
                    <p class="view-subtitle">${eleves.length} élève(s) enregistré(s)</p>
                </div>
                <div class="header-actions">
                    <button class="btn btn-primary" onclick="ouvrirModaleInscrireEleve()">+ Inscrire un élève</button>
                </div>
            </div>

            <div class="card-table-wrapper">
                <div class="table-toolbar">
                    <div style="display:flex; gap:10px; flex-wrap:wrap; flex:1;">
                        <input type="text" id="filtre-recherche-eleve" class="form-control" placeholder="Rechercher par nom, prénom ou matricule..." style="max-width:320px;" oninput="filtrerTableEleves()">
                        <select id="filtre-classe-eleve" class="form-control" style="max-width:200px;" onchange="filtrerTableEleves()">
                            <option value="">Toutes les classes</option>
                            ${classes.map(c => `<option value="${c.id}">${c.nom}</option>`).join('')}
                        </select>
                    </div>
                </div>
                <div class="table-responsive">
                    <table class="custom-table" id="table-eleves">
                        <thead>
                            <tr>
                                <th>Matricule</th>
                                <th>Nom & Prénom</th>
                                <th>Classe</th>
                                <th>Sexe / Âge</th>
                                <th>Parent & Téléphone</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${eleves.map(e => `
                                <tr data-classe-id="${e.classe_id || ''}" data-search="${(e.nom + ' ' + e.prenom + ' ' + e.matricule).toLowerCase()}">
                                    <td><strong>${e.matricule}</strong></td>
                                    <td>${e.nom} ${e.prenom}</td>
                                    <td><span class="badge badge-info">${e.nom_classe || 'Non affecté'}</span></td>
                                    <td>${e.sexe} (${e.date_naissance})</td>
                                    <td>${e.nom_parent} <br><small>${e.telephone_parent}</small></td>
                                    <td>
                                        <button class="btn btn-sm btn-outline" onclick="ouvrirFicheEleve(${e.id})" title="Voir fiche">Fiche</button>
                                        <button class="btn btn-sm btn-outline" onclick="ouvrirBulletinEleve(${e.id})" title="Bulletin">Bulletin</button>
                                        ${getRole() === 'ADMIN' ? `<button class="btn btn-sm btn-outline-danger" onclick="supprimerEleve(${e.id})" title="Supprimer">✕</button>` : ''}
                                    </td>
                                </tr>
                            `).join('')}
                        </tbody>
                    </table>
                </div>
            </div>
        `;
    } catch (err) {
        container.innerHTML = `<div class="alert alert-danger">${err.message}</div>`;
    }
}

function filtrerTableEleves() {
    const searchVal = document.getElementById('filtre-recherche-eleve').value.toLowerCase().trim();
    const classeVal = document.getElementById('filtre-classe-eleve').value;
    const lignes = document.querySelectorAll('#table-eleves tbody tr');

    lignes.forEach(tr => {
        const textSearch = tr.getAttribute('data-search') || '';
        const classeId = tr.getAttribute('data-classe-id') || '';

        const correspondSearch = !searchVal || textSearch.includes(searchVal);
        const correspondClasse = !classeVal || classeId === classeVal;

        tr.style.display = (correspondSearch && correspondClasse) ? '' : 'none';
    });
}

/* =====================================================================
   VUE 3 : PROFESSEURS
   ===================================================================== */
async function chargerVueProfesseurs() {
    const container = document.getElementById('main-view-container');
    container.innerHTML = '<div class="loader-container"><div class="spinner"></div><p>Chargement des professeurs...</p></div>';

    try {
        const [resProfs, resClasses, resMatieres] = await Promise.all([
            api.get('/professeurs'),
            api.get('/classes'),
            api.get('/matieres')
        ]);

        const profs = resProfs.professeurs;

        container.innerHTML = `
            <div class="view-header">
                <div class="view-title-group">
                    <h1>Corps Enseignant</h1>
                    <p class="view-subtitle">${profs.length} professeur(s) en activité</p>
                </div>
                ${getRole() === 'ADMIN' ? `
                    <div class="header-actions">
                        <button class="btn btn-primary" onclick="ouvrirModaleAjoutProfesseur()">+ Ajouter un professeur</button>
                    </div>
                ` : ''}
            </div>

            <div class="card-table-wrapper">
                <div class="table-responsive">
                    <table class="custom-table">
                        <thead>
                            <tr>
                                <th>Matricule</th>
                                <th>Nom & Prénom</th>
                                <th>Spécialité</th>
                                <th>Contact</th>
                                <th>Affectations</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${profs.map(p => `
                                <tr>
                                    <td><strong>${p.matricule}</strong></td>
                                    <td>${p.nom} ${p.prenom}</td>
                                    <td><span class="badge badge-success">${p.specialite}</span></td>
                                    <td>${p.email}<br><small>${p.telephone || 'Non renseigné'}</small></td>
                                    <td>${p.nombre_classes} classe(s) • ${p.nombre_matieres} matière(s)</td>
                                    <td>
                                        <button class="btn btn-sm btn-outline" onclick="ouvrirFicheProfesseur(${p.id})">Affectations</button>
                                        ${getRole() === 'ADMIN' ? `<button class="btn btn-sm btn-outline-danger" onclick="supprimerProfesseur(${p.id})">✕</button>` : ''}
                                    </td>
                                </tr>
                            `).join('')}
                        </tbody>
                    </table>
                </div>
            </div>
        `;
    } catch (err) {
        container.innerHTML = `<div class="alert alert-danger">${err.message}</div>`;
    }
}

/* =====================================================================
   VUE 4 : CLASSES & MATIÈRES
   ===================================================================== */
async function chargerVueClasses() {
    const container = document.getElementById('main-view-container');
    container.innerHTML = '<div class="loader-container"><div class="spinner"></div><p>Chargement des classes...</p></div>';

    try {
        const res = await api.get('/classes');
        const classes = res.classes;

        container.innerHTML = `
            <div class="view-header">
                <div class="view-title-group">
                    <h1>Gestion des Classes</h1>
                    <p class="view-subtitle">${classes.length} classe(s) configurée(s)</p>
                </div>
                ${getRole() === 'ADMIN' ? `
                    <div class="header-actions">
                        <button class="btn btn-primary" onclick="ouvrirModaleAjoutClasse()">+ Nouvelle classe</button>
                    </div>
                ` : ''}
            </div>

            <div style="display:grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 20px;">
                ${classes.map(c => `
                    <div class="stat-card" style="border-top: 4px solid var(--primary);">
                        <div class="stat-card-header">
                            <div>
                                <h3 style="font-size:18px;">${c.nom}</h3>
                                <span class="badge badge-info">${c.niveau}</span>
                            </div>
                            <span class="stat-icon">🏫</span>
                        </div>
                        <div style="margin: 12px 0;">
                            <p><strong>Effectif :</strong> ${c.effectif_reel} élève(s)</p>
                            <p><strong>Salle principale :</strong> ${c.salle_principale || 'Non assignée'}</p>
                            <p><strong>Scolarité annuelle :</strong> ${parseFloat(c.frais_scolarite).toFixed(2)} €</p>
                        </div>
                        <div style="display:flex; gap:8px; justify-content:flex-end;">
                            <button class="btn btn-sm btn-outline" onclick="ouvrirDetailsClasse(${c.id})">Détails & Emploi du temps</button>
                            ${getRole() === 'ADMIN' ? `<button class="btn btn-sm btn-outline-danger" onclick="supprimerClasse(${c.id})">✕</button>` : ''}
                        </div>
                    </div>
                `).join('')}
            </div>
        `;
    } catch (err) {
        container.innerHTML = `<div class="alert alert-danger">${err.message}</div>`;
    }
}

async function chargerVueMatieres() {
    const container = document.getElementById('main-view-container');
    container.innerHTML = '<div class="loader-container"><div class="spinner"></div><p>Chargement des matières...</p></div>';

    try {
        const res = await api.get('/matieres');
        const matieres = res.matieres;

        container.innerHTML = `
            <div class="view-header">
                <div class="view-title-group">
                    <h1>Matières & Coefficients</h1>
                    <p class="view-subtitle">${matieres.length} matière(s) au programme</p>
                </div>
                ${getRole() === 'ADMIN' ? `
                    <div class="header-actions">
                        <button class="btn btn-primary" onclick="ouvrirModaleAjoutMatiere()">+ Ajouter une matière</button>
                    </div>
                ` : ''}
            </div>

            <div class="card-table-wrapper">
                <div class="table-responsive">
                    <table class="custom-table">
                        <thead>
                            <tr>
                                <th>Code</th>
                                <th>Intitulé</th>
                                <th>Coefficient</th>
                                <th>Niveau</th>
                                <th>Professeurs affectés</th>
                                ${getRole() === 'ADMIN' ? '<th>Actions</th>' : ''}
                            </tr>
                        </thead>
                        <tbody>
                            ${matieres.map(m => `
                                <tr>
                                    <td><strong style="color:${m.couleur_hex};">${m.code}</strong></td>
                                    <td>${m.nom}</td>
                                    <td><span class="badge badge-warning">Coeff ${m.coefficient}</span></td>
                                    <td>${m.niveau}</td>
                                    <td>${m.nombre_professeurs} enseignant(s)</td>
                                    ${getRole() === 'ADMIN' ? `
                                        <td>
                                            <button class="btn btn-sm btn-outline-danger" onclick="supprimerMatiere(${m.id})">✕</button>
                                        </td>
                                    ` : ''}
                                </tr>
                            `).join('')}
                        </tbody>
                    </table>
                </div>
            </div>
        `;
    } catch (err) {
        container.innerHTML = `<div class="alert alert-danger">${err.message}</div>`;
    }
}

/* =====================================================================
   VUE 5 : NOTES & MOYENNES (AVEC GRILLE DYNAMIQUE ENSEIGNANT)
   ===================================================================== */
let donneesGrilleNotes = {
    classe_id: null,
    matiere_id: null,
    periode_id: 1,
    classes: [],
    matieres: [],
    eleves: [],
    evaluations: [] // [ { id, titre, coeff, bareme, date, notes: { [eleve_id]: val, ... } } ]
};

async function chargerVueNotes(modeSaisieDirecte = false) {
    const role = getRole();
    if (role === 'ELEVE') {
        chargerVueNotesEleve();
        return;
    }

    const container = document.getElementById('main-view-container');
    container.innerHTML = '<div class="loader-container"><div class="spinner"></div><p>Chargement du module de notation...</p></div>';

    try {
        const [resClasses, resMatieres] = await Promise.all([
            api.get('/classes'),
            api.get('/matieres')
        ]);

        donneesGrilleNotes.classes = resClasses.classes;
        donneesGrilleNotes.matieres = resMatieres.matieres;

        // Classe et matière par défaut
        if (!donneesGrilleNotes.classe_id && donneesGrilleNotes.classes.length > 0) {
            donneesGrilleNotes.classe_id = donneesGrilleNotes.classes[0].id;
        }
        if (!donneesGrilleNotes.matiere_id && donneesGrilleNotes.matieres.length > 0) {
            donneesGrilleNotes.matiere_id = donneesGrilleNotes.matieres[0].id;
        }

        afficherInterfaceGrilleNotes(container);
    } catch (err) {
        container.innerHTML = `<div class="alert alert-danger">${err.message}</div>`;
    }
}

async function afficherInterfaceGrilleNotes(container) {
    const { classes, matieres, classe_id, matiere_id, periode_id } = donneesGrilleNotes;

    // Charger les élèves de la classe sélectionnée et leurs notes existantes pour cette matière
    const [resEleves, resNotes] = await Promise.all([
        api.get(`/eleves?classe_id=${classe_id}`),
        api.get(`/notes?classe_id=${classe_id}&matiere_id=${matiere_id}&periode_id=${periode_id}`)
    ]);

    donneesGrilleNotes.eleves = resEleves.eleves;
    const notesExistantes = resNotes.notes;

    // Structurer les évaluations existantes en colonnes dynamiques
    const mapEvals = {};
    notesExistantes.forEach(n => {
        const cle = n.titre_evaluation || 'Évaluation 1';
        if (!mapEvals[cle]) {
            mapEvals[cle] = {
                titre: cle,
                coeff: parseFloat(n.coefficient) || 1.0,
                bareme: parseFloat(n.bareme) || 20.0,
                date: n.date_evaluation,
                notes: {},
                notes_ids: {}
            };
        }
        mapEvals[cle].notes[n.eleve_id] = parseFloat(n.note);
        mapEvals[cle].notes_ids[n.eleve_id] = n.id;
    });

    donneesGrilleNotes.evaluations = Object.values(mapEvals);

    // Si aucune évaluation n'existe encore pour cette matière/classe, créer 2 colonnes d'évaluations par défaut
    if (donneesGrilleNotes.evaluations.length === 0) {
        donneesGrilleNotes.evaluations = [
            {
                titre: 'Devoir Surveillé N°1',
                coeff: 2.0,
                bareme: 20.0,
                date: new Date().toISOString().split('T')[0],
                notes: {},
                notes_ids: {}
            },
            {
                titre: 'Interrogation / Contrôle continu',
                coeff: 1.0,
                bareme: 20.0,
                date: new Date().toISOString().split('T')[0],
                notes: {},
                notes_ids: {}
            }
        ];
    }

    container.innerHTML = `
        <div class="gradebook-container">
            <div class="view-header">
                <div class="view-title-group">
                    <h1>Carnet de Notes & Saisie Dynamique</h1>
                    <p class="view-subtitle">Grille interactive permettant de saisir plusieurs notes par élève pour la matière sélectionnée</p>
                </div>
                <div class="header-actions">
                    <button class="btn btn-outline" onclick="ajouterColonneEvaluation()">+ Ajouter une évaluation</button>
                    <button class="btn btn-primary" id="btn-save-all-grades" onclick="enregistrerToutesLesNotes()">💾 Enregistrer toutes les notes</button>
                </div>
            </div>

            <!-- Barre de configuration (Classe, Matière, Période) -->
            <div class="gradebook-config-card">
                <div class="form-group" style="margin-bottom:0; min-width:200px;">
                    <label>Classe à évaluer :</label>
                    <select id="select-classe-notes" class="form-control" onchange="changerClasseOuMatiereGrille(this.value, null, null)">
                        ${classes.map(c => `<option value="${c.id}" ${c.id == classe_id ? 'selected' : ''}>${c.nom} (${c.niveau})</option>`).join('')}
                    </select>
                </div>

                <div class="form-group" style="margin-bottom:0; min-width:240px;">
                    <label>Matière enseignée :</label>
                    <select id="select-matiere-notes" class="form-control" onchange="changerClasseOuMatiereGrille(null, this.value, null)">
                        ${matieres.map(m => `<option value="${m.id}" ${m.id == matiere_id ? 'selected' : ''}>${m.nom} (Coeff ${m.coefficient})</option>`).join('')}
                    </select>
                </div>

                <div class="form-group" style="margin-bottom:0; min-width:180px;">
                    <label>Période scolaire :</label>
                    <select id="select-periode-notes" class="form-control" onchange="changerClasseOuMatiereGrille(null, null, this.value)">
                        <option value="1" ${periode_id == 1 ? 'selected' : ''}>Trimestre 1</option>
                        <option value="2" ${periode_id == 2 ? 'selected' : ''}>Trimestre 2</option>
                        <option value="3" ${periode_id == 3 ? 'selected' : ''}>Trimestre 3</option>
                    </select>
                </div>

                <div style="flex:1; display:flex; justify-content:flex-end;">
                    <button class="btn btn-sm btn-outline" onclick="reinitialiserNotesGrille()">Annuler les saisies</button>
                </div>
            </div>

            <!-- Statistiques dynamiques en direct de la classe -->
            <div class="gradebook-stats-bar" id="gradebook-live-stats">
                <div class="gradebook-stat-item">
                    <div class="gradebook-stat-label">Élèves de la classe</div>
                    <div class="gradebook-stat-val" id="stat-effectif">${donneesGrilleNotes.eleves.length}</div>
                </div>
                <div class="gradebook-stat-item">
                    <div class="gradebook-stat-label">Moyenne de classe</div>
                    <div class="gradebook-stat-val" id="stat-moyenne-classe">-- / 20</div>
                </div>
                <div class="gradebook-stat-item">
                    <div class="gradebook-stat-label">Note la plus haute</div>
                    <div class="gradebook-stat-val" style="color:#86EFAC;" id="stat-note-max">--</div>
                </div>
                <div class="gradebook-stat-item">
                    <div class="gradebook-stat-label">Note la plus basse</div>
                    <div class="gradebook-stat-val" style="color:#FCA5A5;" id="stat-note-min">--</div>
                </div>
                <div class="gradebook-stat-item">
                    <div class="gradebook-stat-label">Taux de réussite (≥ 10)</div>
                    <div class="gradebook-stat-val" id="stat-taux-reussite">--%</div>
                </div>
            </div>

            <!-- Grille dynamique interactive de saisie -->
            <div class="card-table-wrapper">
                <div class="table-responsive">
                    <table class="custom-table" id="table-gradebook">
                        <thead>
                            <tr>
                                <th style="width: 240px;">Élève</th>
                                ${donneesGrilleNotes.evaluations.map((evalCol, idx) => `
                                    <th style="min-width: 140px; text-align: center;">
                                        <div class="eval-col-header-box">
                                            <input type="text" 
                                                   class="form-control form-control-sm" 
                                                   value="${evalCol.titre}" 
                                                   onchange="mettreAJourTitreEval(${idx}, this.value)"
                                                   style="font-weight:700; text-align:center; margin-bottom:4px;"
                                                   title="Modifier le titre de l'évaluation">
                                            <div style="display:flex; justify-content:center; gap:6px; align-items:center;">
                                                <small style="color:var(--text-secondary);">Coeff :</small>
                                                <input type="number" 
                                                       step="0.5" 
                                                       min="0.5" 
                                                       max="10" 
                                                       value="${evalCol.coeff}" 
                                                       onchange="mettreAJourCoeffEval(${idx}, this.value)"
                                                       style="width:50px; text-align:center; padding:2px; font-size:11px;"
                                                       class="form-control form-control-sm">
                                            </div>
                                        </div>
                                    </th>
                                `).join('')}
                                <th style="width: 110px; text-align: center;">Moyenne /20</th>
                                <th style="min-width: 180px;">Appréciation de l'élève</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${donneesGrilleNotes.eleves.map((eleve, elIdx) => `
                                <tr data-eleve-id="${eleve.id}">
                                    <td>
                                        <strong>${eleve.nom} ${eleve.prenom}</strong>
                                        <br><small style="color:var(--text-muted);">${eleve.matricule}</small>
                                    </td>
                                    ${donneesGrilleNotes.evaluations.map((evalCol, evIdx) => {
                                        const noteVal = evalCol.notes[eleve.id] !== undefined ? evalCol.notes[eleve.id] : '';
                                        return `
                                            <td style="text-align: center;">
                                                <input type="number" 
                                                       step="0.25" 
                                                       min="0" 
                                                       max="${evalCol.bareme}" 
                                                       value="${noteVal !== '' ? noteVal : ''}"
                                                       placeholder="-- / 20"
                                                       class="grade-input"
                                                       data-eleve-id="${eleve.id}"
                                                       data-eval-idx="${evIdx}"
                                                       oninput="recalculerGrilleEnDirect(${eleve.id})"
                                                       onkeydown="gererNavigationClavierGrille(event, ${elIdx}, ${evIdx})">
                                            </td>
                                        `;
                                    }).join('')}
                                    <td style="text-align: center;">
                                        <span class="student-avg-cell" id="avg-badge-${eleve.id}">--</span>
                                    </td>
                                    <td>
                                        <input type="text" 
                                               id="comment-eleve-${eleve.id}" 
                                               class="comment-input" 
                                               placeholder="Observations..."
                                               value="">
                                    </td>
                                </tr>
                            `).join('')}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    `;

    // Recalcul initial en direct
    recalculerToutesLesMoyennes();
}

/**
 * Change la classe ou la matière sélectionnée et recharge la grille
 */
function changerClasseOuMatiereGrille(nouvelleClasseId, nouvelleMatiereId, nouvellePeriodeId) {
    if (nouvelleClasseId) donneesGrilleNotes.classe_id = nouvelleClasseId;
    if (nouvelleMatiereId) donneesGrilleNotes.matiere_id = nouvelleMatiereId;
    if (nouvellePeriodeId) donneesGrilleNotes.periode_id = nouvellePeriodeId;

    const container = document.getElementById('main-view-container');
    afficherInterfaceGrilleNotes(container);
}

/**
 * Ajoute dynamiquement une nouvelle colonne d'évaluation
 */
function ajouterColonneEvaluation() {
    const num = donneesGrilleNotes.evaluations.length + 1;
    donneesGrilleNotes.evaluations.push({
        titre: `Évaluation N°${num}`,
        coeff: 1.0,
        bareme: 20.0,
        date: new Date().toISOString().split('T')[0],
        notes: {},
        notes_ids: {}
    });

    const container = document.getElementById('main-view-container');
    afficherInterfaceGrilleNotes(container);
    afficherNotification(`Colonne « Évaluation N°${num} » ajoutée à la grille.`, 'info');
}

function mettreAJourTitreEval(evalIdx, nouveauTitre) {
    if (donneesGrilleNotes.evaluations[evalIdx]) {
        donneesGrilleNotes.evaluations[evalIdx].titre = nouveauTitre.trim() || `Évaluation ${evalIdx + 1}`;
    }
}

function mettreAJourCoeffEval(evalIdx, nouveauCoeff) {
    const val = parseFloat(nouveauCoeff) || 1.0;
    if (donneesGrilleNotes.evaluations[evalIdx]) {
        donneesGrilleNotes.evaluations[evalIdx].coeff = Math.max(0.5, val);
        recalculerToutesLesMoyennes();
    }
}

/**
 * Navigation clavier intuitive pour la saisie ultra-rapide (Flèches & Entrée)
 */
function gererNavigationClavierGrille(event, elIdx, evIdx) {
    if (event.key === 'Enter' || event.key === 'ArrowDown') {
        event.preventDefault();
        const nextInput = document.querySelector(`input[data-eval-idx="${evIdx}"][data-eleve-id="${donneesGrilleNotes.eleves[elIdx + 1]?.id}"]`);
        if (nextInput) nextInput.focus();
    } else if (event.key === 'ArrowUp') {
        event.preventDefault();
        const prevInput = document.querySelector(`input[data-eval-idx="${evIdx}"][data-eleve-id="${donneesGrilleNotes.eleves[elIdx - 1]?.id}"]`);
        if (prevInput) prevInput.focus();
    }
}

/**
 * Recalcule la moyenne d'un élève en direct à chaque saisie
 */
function recalculerGrilleEnDirect(eleveId) {
    const inputs = document.querySelectorAll(`input.grade-input[data-eleve-id="${eleveId}"]`);
    let sommePonderee = 0;
    let sommeCoeffs = 0;

    inputs.forEach(input => {
        const evIdx = parseInt(input.getAttribute('data-eval-idx'), 10);
        const coeff = donneesGrilleNotes.evaluations[evIdx]?.coeff || 1.0;
        const valStr = input.value.trim();

        if (valStr !== '') {
            const noteVal = parseFloat(valStr);
            if (!isNaN(noteVal) && noteVal >= 0 && noteVal <= 20) {
                sommePonderee += noteVal * coeff;
                sommeCoeffs += coeff;
                input.className = `grade-input ${noteVal >= 12 ? 'is-good' : noteVal < 10 ? 'is-poor' : ''}`;
            } else {
                input.className = 'grade-input is-poor';
            }
        } else {
            input.className = 'grade-input';
        }
    });

    const badge = document.getElementById(`avg-badge-${eleveId}`);
    if (badge) {
        if (sommeCoeffs > 0) {
            const moy = Math.round((sommePonderee / sommeCoeffs) * 100) / 100;
            badge.textContent = moy.toFixed(2);
            badge.className = `student-avg-cell ${getGradeClass(moy, 20)}`;
        } else {
            badge.textContent = '--';
            badge.className = 'student-avg-cell';
        }
    }

    recalculerStatistiquesClasseEnDirect();
}

/**
 * Recalcule toutes les moyennes d'élèves de la grille
 */
function recalculerToutesLesMoyennes() {
    donneesGrilleNotes.eleves.forEach(e => {
        recalculerGrilleEnDirect(e.id);
    });
}

/**
 * Recalcule les statistiques globales affichées dans la bannière
 */
function recalculerStatistiquesClasseEnDirect() {
    const moyennesEleves = [];

    donneesGrilleNotes.eleves.forEach(e => {
        const badge = document.getElementById(`avg-badge-${e.id}`);
        if (badge && badge.textContent !== '--') {
            const v = parseFloat(badge.textContent);
            if (!isNaN(v)) moyennesEleves.push(v);
        }
    });

    const statMoyClasse = document.getElementById('stat-moyenne-classe');
    const statMax = document.getElementById('stat-note-max');
    const statMin = document.getElementById('stat-note-min');
    const statTaux = document.getElementById('stat-taux-reussite');

    if (moyennesEleves.length > 0) {
        const som = moyennesEleves.reduce((a, b) => a + b, 0);
        const moyClasse = Math.round((som / moyennesEleves.length) * 100) / 100;
        const noteMax = Math.max(...moyennesEleves);
        const noteMin = Math.min(...moyennesEleves);
        const reussite = Math.round((moyennesEleves.filter(m => m >= 10).length / moyennesEleves.length) * 100);

        if (statMoyClasse) statMoyClasse.textContent = `${moyClasse.toFixed(2)} / 20`;
        if (statMax) statMax.textContent = `${noteMax.toFixed(2)}`;
        if (statMin) statMin.textContent = `${noteMin.toFixed(2)}`;
        if (statTaux) statTaux.textContent = `${reussite}%`;
    } else {
        if (statMoyClasse) statMoyClasse.textContent = '-- / 20';
        if (statMax) statMax.textContent = '--';
        if (statMin) statMin.textContent = '--';
        if (statTaux) statTaux.textContent = '--%';
    }
}

/**
 * Envoie toutes les notes de la grille au backend en un seul appel atomique
 */
async function enregistrerToutesLesNotes() {
    const saveBtn = document.getElementById('btn-save-all-grades');
    try {
        saveBtn.disabled = true;
        saveBtn.textContent = 'Enregistrement en cours...';

        // Assembler la charge utile (payload)
        const payloadEvaluations = donneesGrilleNotes.evaluations.map((evalCol, evIdx) => {
            const notes_eleves = [];

            donneesGrilleNotes.eleves.forEach(eleve => {
                const input = document.querySelector(`input.grade-input[data-eval-idx="${evIdx}"][data-eleve-id="${eleve.id}"]`);
                const commentInput = document.getElementById(`comment-eleve-${eleve.id}`);
                const valStr = input ? input.value.trim() : '';

                if (valStr !== '') {
                    const noteVal = parseFloat(valStr);
                    if (!isNaN(noteVal)) {
                        notes_eleves.push({
                            eleve_id: eleve.id,
                            note_id: evalCol.notes_ids ? evalCol.notes_ids[eleve.id] : null,
                            note: noteVal,
                            commentaire: commentInput ? commentInput.value.trim() : ''
                        });
                    }
                }
            });

            return {
                titre_evaluation: evalCol.titre,
                coefficient: evalCol.coeff,
                bareme: evalCol.bareme,
                date_evaluation: evalCol.date || new Date().toISOString().split('T')[0],
                notes_eleves
            };
        });

        const res = await api.post('/notes/lot', {
            classe_id: donneesGrilleNotes.classe_id,
            matiere_id: donneesGrilleNotes.matiere_id,
            periode_id: donneesGrilleNotes.periode_id,
            evaluations: payloadEvaluations
        });

        afficherNotification(res.message || 'Toutes les notes ont été enregistrées avec succès !', 'success', 6000);
    } catch (err) {
        afficherNotification(err.message, 'danger');
    } finally {
        saveBtn.disabled = false;
        saveBtn.textContent = '💾 Enregistrer toutes les notes';
    }
}

function reinitialiserNotesGrille() {
    if (confirm('Voulez-vous recharger la grille et annuler les modifications non enregistrées ?')) {
        const container = document.getElementById('main-view-container');
        afficherInterfaceGrilleNotes(container);
    }
}

async function chargerVueNotesEleve() {
    const container = document.getElementById('main-view-container');
    container.innerHTML = '<div class="loader-container"><div class="spinner"></div><p>Chargement de vos notes...</p></div>';

    try {
        const res = await api.get('/notes');
        const notes = res.notes;

        container.innerHTML = `
            <div class="view-header">
                <div class="view-title-group">
                    <h1>Mes Notes & Évaluations</h1>
                    <p class="view-subtitle">Historique complet de vos résultats scolaires</p>
                </div>
            </div>

            <div class="card-table-wrapper">
                <div class="table-responsive">
                    <table class="custom-table">
                        <thead>
                            <tr>
                                <th>Date</th>
                                <th>Matière</th>
                                <th>Évaluation</th>
                                <th>Note obtenue</th>
                                <th>Coeff</th>
                                <th>Commentaire Enseignant</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${notes.map(n => `
                                <tr>
                                    <td>${n.date_evaluation}</td>
                                    <td><strong>${n.nom_matiere}</strong></td>
                                    <td>${n.titre_evaluation}</td>
                                    <td><span class="grade-pill ${getGradeClass(n.note, n.bareme)}">${n.note} / ${n.bareme}</span></td>
                                    <td>${n.coefficient}</td>
                                    <td style="font-style:italic; font-size:13px;">${n.commentaire || 'Aucun commentaire'}</td>
                                </tr>
                            `).join('')}
                        </tbody>
                    </table>
                </div>
            </div>
        `;
    } catch (err) {
        container.innerHTML = `<div class="alert alert-danger">${err.message}</div>`;
    }
}

/* =====================================================================
   VUE 6 : ABSENCES & RETARDS
   ===================================================================== */
async function chargerVueAbsences() {
    const container = document.getElementById('main-view-container');
    container.innerHTML = '<div class="loader-container"><div class="spinner"></div><p>Chargement des absences...</p></div>';

    try {
        const res = await api.get('/absences');
        const absences = res.absences;
        const role = getRole();

        container.innerHTML = `
            <div class="view-header">
                <div class="view-title-group">
                    <h1>Absences & Retards</h1>
                    <p class="view-subtitle">${absences.length} enregistrement(s)</p>
                </div>
                ${role !== 'ELEVE' ? `
                    <div class="header-actions">
                        <button class="btn btn-primary" onclick="ouvrirModaleFaireAppel()">⏱️ Faire l'appel de classe</button>
                    </div>
                ` : ''}
            </div>

            <div class="card-table-wrapper">
                <div class="table-responsive">
                    <table class="custom-table">
                        <thead>
                            <tr>
                                <th>Date & Créneau</th>
                                <th>Élève</th>
                                <th>Classe</th>
                                <th>Type</th>
                                <th>Statut</th>
                                <th>Motif</th>
                                ${role !== 'ELEVE' ? '<th>Actions</th>' : ''}
                            </tr>
                        </thead>
                        <tbody>
                            ${absences.map(a => `
                                <tr>
                                    <td>${a.date} <br><small>${a.creneau_horaire}</small></td>
                                    <td><strong>${a.nom_eleve} ${a.prenom_eleve}</strong></td>
                                    <td>${a.nom_classe}</td>
                                    <td>
                                        <span class="badge ${a.type === 'ABSENCE' ? 'badge-danger' : 'badge-warning'}">
                                            ${a.type}
                                        </span>
                                    </td>
                                    <td>
                                        <span class="badge ${a.justifiee ? 'badge-success' : 'badge-danger'}">
                                            ${a.justifiee ? 'Justifiée' : 'Non justifiée'}
                                        </span>
                                    </td>
                                    <td>${a.motif || 'Aucun motif fourni'}</td>
                                    ${role !== 'ELEVE' ? `
                                        <td>
                                            ${!a.justifiee ? `<button class="btn btn-sm btn-outline" onclick="ouvrirModaleJustifierAbsence(${a.id})">Justifier</button>` : ''}
                                            <button class="btn btn-sm btn-outline-danger" onclick="supprimerAbsence(${a.id})">✕</button>
                                        </td>
                                    ` : ''}
                                </tr>
                            `).join('')}
                        </tbody>
                    </table>
                </div>
            </div>
        `;
    } catch (err) {
        container.innerHTML = `<div class="alert alert-danger">${err.message}</div>`;
    }
}

/* =====================================================================
   VUE 7 : EMPLOI DU TEMPS
   ===================================================================== */
async function chargerVueEmploiDuTemps() {
    const container = document.getElementById('main-view-container');
    container.innerHTML = '<div class="loader-container"><div class="spinner"></div><p>Chargement du planning...</p></div>';

    try {
        const res = await api.get('/emplois-du-temps/mon-planning');
        const cours = res.cours;

        const jours = ['Lundi', 'Mardi', 'Mercredi', 'Jeudi', 'Vendredi', 'Samedi'];

        container.innerHTML = `
            <div class="view-header">
                <div class="view-title-group">
                    <h1>Emploi du Temps Hebdomadaire</h1>
                    <p class="view-subtitle">Grille interactive des cours programmés</p>
                </div>
                ${getRole() === 'ADMIN' ? `
                    <div class="header-actions">
                        <button class="btn btn-primary" onclick="ouvrirModaleProgrammerCours()">+ Programmer un cours</button>
                    </div>
                ` : ''}
            </div>

            <div class="timetable-grid">
                ${jours.map(jour => {
                    const coursDuJour = cours.filter(c => c.jour === jour);
                    return `
                        <div class="timetable-day-card">
                            <div class="day-header">${jour} (${coursDuJour.length} cours)</div>
                            <div class="day-body">
                                ${coursDuJour.length === 0 ? `
                                    <div style="padding:16px; color:var(--text-muted); font-size:13px; text-align:center;">
                                        Aucun cours programmé ce jour.
                                    </div>
                                ` : coursDuJour.map(c => `
                                    <div class="course-item">
                                        <div>
                                            <span class="course-time">${c.heure_debut.slice(0,5)} - ${c.heure_fin.slice(0,5)}</span>
                                            <div class="course-subject" style="margin-top:4px;">${c.nom_matiere}</div>
                                            <div class="course-details">${c.nom_classe ? c.nom_classe + ' • ' : ''}${c.salle}</div>
                                        </div>
                                        ${getRole() === 'ADMIN' ? `<button class="btn-icon" style="color:var(--danger);" onclick="supprimerCours(${c.id})">✕</button>` : ''}
                                    </div>
                                `).join('')}
                            </div>
                        </div>
                    `;
                }).join('')}
            </div>
        `;
    } catch (err) {
        container.innerHTML = `<div class="alert alert-danger">${err.message}</div>`;
    }
}

/* =====================================================================
   VUE 8 : PAIEMENTS DE SCOLARITÉ
   ===================================================================== */
async function chargerVuePaiements() {
    const container = document.getElementById('main-view-container');
    container.innerHTML = '<div class="loader-container"><div class="spinner"></div><p>Chargement des paiements...</p></div>';

    try {
        const res = await api.get('/paiements');
        const paiements = res.paiements;
        const total = paiements.reduce((acc, p) => acc + parseFloat(p.montant), 0);

        container.innerHTML = `
            <div class="view-header">
                <div class="view-title-group">
                    <h1>Gestion des Paiements de Scolarité</h1>
                    <p class="view-subtitle">Total enregistré : <strong>${total.toFixed(2)} €</strong></p>
                </div>
                ${getRole() === 'ADMIN' ? `
                    <div class="header-actions">
                        <button class="btn btn-primary" onclick="ouvrirModaleEnregistrerPaiement()">+ Encaisser un paiement</button>
                    </div>
                ` : ''}
            </div>

            <div class="card-table-wrapper">
                <div class="table-responsive">
                    <table class="custom-table">
                        <thead>
                            <tr>
                                <th>Date</th>
                                <th>Référence</th>
                                <th>Élève</th>
                                <th>Type</th>
                                <th>Mode</th>
                                <th>Montant</th>
                                <th>Statut</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${paiements.map(p => `
                                <tr>
                                    <td>${p.date_paiement}</td>
                                    <td><strong>${p.reference}</strong></td>
                                    <td>${p.nom_eleve} ${p.prenom_eleve}</td>
                                    <td><span class="badge badge-info">${p.type}</span></td>
                                    <td>${p.mode_paiement}</td>
                                    <td><strong style="color:var(--success); font-size:15px;">${parseFloat(p.montant).toFixed(2)} €</strong></td>
                                    <td><span class="badge badge-success">${p.statut}</span></td>
                                </tr>
                            `).join('')}
                        </tbody>
                    </table>
                </div>
            </div>
        `;
    } catch (err) {
        container.innerHTML = `<div class="alert alert-danger">${err.message}</div>`;
    }
}

/* =====================================================================
   VUE 9 : BULLETINS SCOLAIRES IMPRIMABLES (A4)
   ===================================================================== */
async function chargerVueBulletins() {
    const role = getRole();
    const user = getUser();
    if (role === 'ELEVE' && user?.profil?.id) {
        ouvrirBulletinEleve(user.profil.id);
        return;
    }

    const container = document.getElementById('main-view-container');
    container.innerHTML = '<div class="loader-container"><div class="spinner"></div><p>Chargement des élèves...</p></div>';

    try {
        const res = await api.get('/eleves');
        const eleves = res.eleves;

        container.innerHTML = `
            <div class="view-header">
                <div class="view-title-group">
                    <h1>Bulletins Scolaires Officiels</h1>
                    <p class="view-subtitle">Génération et impression A4 des bulletins trimestriels</p>
                </div>
            </div>

            <div class="card-table-wrapper">
                <div class="table-toolbar">
                    <h3>Sélectionnez un élève pour générer son bulletin officiel :</h3>
                </div>
                <div class="table-responsive">
                    <table class="custom-table">
                        <thead>
                            <tr>
                                <th>Matricule</th>
                                <th>Nom & Prénom</th>
                                <th>Classe</th>
                                <th>Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${eleves.map(e => `
                                <tr>
                                    <td><strong>${e.matricule}</strong></td>
                                    <td>${e.nom} ${e.prenom}</td>
                                    <td><span class="badge badge-info">${e.nom_classe || 'Non affecté'}</span></td>
                                    <td>
                                        <button class="btn btn-sm btn-primary" onclick="ouvrirBulletinEleve(${e.id})">
                                            📜 Générer le Bulletin
                                        </button>
                                    </td>
                                </tr>
                            `).join('')}
                        </tbody>
                    </table>
                </div>
            </div>
        `;
    } catch (err) {
        container.innerHTML = `<div class="alert alert-danger">${err.message}</div>`;
    }
}

/**
 * Affiche la modale grand format du Bulletin Scolaire officiel imprimable A4
 */
async function ouvrirBulletinEleve(eleveId) {
    try {
        const res = await api.get(`/bulletins/eleve/${eleveId}`);
        const b = res.bulletin;

        const html = `
            <div class="bulletin-a4-sheet" id="bulletin-a4-printable">
                <!-- En-tête officiel établissement -->
                <div style="border-bottom: 2px solid #1E3A8A; padding-bottom: 12px; margin-bottom: 16px; display:flex; justify-content:space-between; align-items:center;">
                    <div>
                        <h2 style="font-size:20px; color:#1E3A8A; margin:0;">${b.etablissement.nom}</h2>
                        <p style="margin:2px 0; font-size:12px; color:#475569;">${b.etablissement.academie} • ${b.etablissement.adresse}</p>
                        <p style="margin:0; font-size:12px; color:#475569;">Tél : ${b.etablissement.telephone} • Email : ${b.etablissement.email}</p>
                    </div>
                    <div style="text-align:right;">
                        <h3 style="font-size:16px; text-transform:uppercase; margin:0;">BULLETIN SCOLAIRE</h3>
                        <p style="margin:2px 0; font-weight:700; color:#1E3A8A;">${b.periode.nom}</p>
                        <p style="margin:0; font-size:12px;">Année : ${b.etablissement.annee_scolaire}</p>
                    </div>
                </div>

                <!-- Informations Élève -->
                <div style="background:#F8FAFC; border:1px solid #E2E8F0; border-radius:8px; padding:10px 14px; margin-bottom:16px; display:flex; justify-content:space-between;">
                    <div>
                        <p style="margin:0; font-size:15px;"><strong>Élève :</strong> ${b.eleve.nom_complet}</p>
                        <p style="margin:4px 0 0; font-size:13px; color:#475569;">Né(e) le : ${b.eleve.date_naissance}</p>
                    </div>
                    <div style="text-align:right;">
                        <p style="margin:0; font-size:14px;"><strong>Classe :</strong> ${b.eleve.classe}</p>
                        <p style="margin:4px 0 0; font-size:13px; color:#475569;">Matricule : <strong>${b.eleve.matricule}</strong></p>
                    </div>
                </div>

                <!-- Tableau détaillé des notes par discipline -->
                <table class="bulletin-grades-table" style="width:100%; border-collapse:collapse; margin-bottom:16px; font-size:13px;">
                    <thead>
                        <tr style="background:#1E3A8A; color:#fff;">
                            <th style="padding:8px; text-align:left;">Matière & Enseignant</th>
                            <th style="padding:8px; text-align:center; width:60px;">Coeff</th>
                            <th style="padding:8px; text-align:center; width:80px;">Élève /20</th>
                            <th style="padding:8px; text-align:center; width:80px;">Classe</th>
                            <th style="padding:8px; text-align:center; width:90px;">Min - Max</th>
                            <th style="padding:8px; text-align:left;">Appréciation de l'enseignant</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${b.resultats_disciplines.map(d => `
                            <tr>
                                <td style="padding:8px; border-bottom:1px solid #E2E8F0;">
                                    <strong>${d.nom}</strong><br>
                                    <small style="color:#64748B;">${d.professeur}</small>
                                </td>
                                <td style="padding:8px; text-align:center; border-bottom:1px solid #E2E8F0;">${d.coefficient}</td>
                                <td style="padding:8px; text-align:center; border-bottom:1px solid #E2E8F0; font-weight:700; color:#1E3A8A; font-size:14px;">
                                    ${d.moyenne_eleve !== null ? d.moyenne_eleve.toFixed(2) : '--'}
                                </td>
                                <td style="padding:8px; text-align:center; border-bottom:1px solid #E2E8F0;">
                                    ${d.moyenne_classe !== null ? d.moyenne_classe.toFixed(2) : '--'}
                                </td>
                                <td style="padding:8px; text-align:center; border-bottom:1px solid #E2E8F0; font-size:12px; color:#64748B;">
                                    ${d.note_min !== null ? d.note_min.toFixed(1) + ' - ' + d.note_max.toFixed(1) : '--'}
                                </td>
                                <td style="padding:8px; border-bottom:1px solid #E2E8F0; font-style:italic; font-size:12px;">
                                    ${d.appreciation}
                                </td>
                            </tr>
                        `).join('')}
                    </tbody>
                </table>

                <!-- Bilan général & Synthèse -->
                <div style="background:#EFF6FF; border:1px solid #BFDBFE; border-radius:8px; padding:12px 16px; margin-bottom:16px; display:flex; justify-content:space-between; align-items:center;">
                    <div>
                        <div style="font-size:12px; font-weight:700; color:#1E3A8A; text-transform:uppercase;">MOYENNE GÉNÉRALE</div>
                        <div style="font-size:24px; font-weight:800; color:#1E3A8A;">
                            ${b.synthese.moyenne_generale !== null ? b.synthese.moyenne_generale.toFixed(2) + ' / 20' : 'N/A'}
                        </div>
                        <div style="font-size:12px; color:#475569;">
                            Moyenne de classe : <strong>${b.synthese.moyenne_classe}</strong> • Rang : <strong>${b.synthese.rang || '-'} / ${b.synthese.effectif} élèves</strong>
                        </div>
                    </div>
                    <div style="text-align:right;">
                        <span class="badge badge-success" style="font-size:13px; padding:6px 12px;">${b.synthese.mention}</span>
                        <div style="font-size:12px; color:#475569; margin-top:6px;">
                            Assiduité : <strong>${b.synthese.assiduite.total_absences || 0} absence(s)</strong> (${b.synthese.assiduite.absences_non_justifiees || 0} non justifiée)
                        </div>
                    </div>
                </div>

                <!-- Avis du Conseil de classe -->
                <div style="border:1px solid #CBD5E1; border-radius:8px; padding:12px; margin-bottom:20px;">
                    <div style="font-size:12px; font-weight:700; text-transform:uppercase; margin-bottom:4px;">Appréciation globale du Conseil de classe :</div>
                    <p style="margin:0; font-size:13px; font-style:italic;">« ${b.appreciation_generale} »</p>
                </div>

                <!-- Signatures officielles -->
                <div class="bulletin-footer-signatures" style="display:flex; justify-content:space-between; margin-top:30px; text-align:center;">
                    <div class="signature-block" style="width:30%; border-top:1px solid #000; padding-top:6px; font-size:12px;">
                        Le Professeur Principal
                    </div>
                    <div class="signature-block" style="width:30%; border-top:1px solid #000; padding-top:6px; font-size:12px;">
                        Les Parents / Responsables
                    </div>
                    <div class="signature-block" style="width:30%; border-top:1px solid #000; padding-top:6px; font-size:12px;">
                        Le Chef d'Établissement
                    </div>
                </div>
            </div>
        `;

        ouvrirModale(html, {
            titre: `Bulletin Officiel - ${b.eleve.nom_complet}`,
            grandFormat: true,
            boutons: [
                {
                    label: '🖨️ Imprimer en A4',
                    classe: 'btn-primary',
                    action: () => window.print()
                },
                {
                    label: 'Fermer',
                    classe: 'btn-outline',
                    action: () => fermerModale()
                }
            ]
        });
    } catch (err) {
        afficherNotification(err.message, 'danger');
    }
}

/* =====================================================================
   VUE 10 : COMPTES UTILISATEURS (ADMIN)
   ===================================================================== */
async function chargerVueUsers() {
    const container = document.getElementById('main-view-container');
    container.innerHTML = '<div class="loader-container"><div class="spinner"></div><p>Chargement des utilisateurs...</p></div>';

    try {
        const res = await api.get('/users');
        const users = res.users;

        container.innerHTML = `
            <div class="view-header">
                <div class="view-title-group">
                    <h1>Gestion des Comptes Utilisateurs</h1>
                    <p class="view-subtitle">${users.length} compte(s) au total</p>
                </div>
            </div>

            <div class="card-table-wrapper">
                <div class="table-responsive">
                    <table class="custom-table">
                        <thead>
                            <tr>
                                <th>Identifiant</th>
                                <th>Email</th>
                                <th>Rôle Système</th>
                                <th>Statut Compte</th>
                                <th>Date Création</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${users.map(u => `
                                <tr>
                                    <td><strong>${u.username}</strong></td>
                                    <td>${u.email}</td>
                                    <td><span class="role-badge role-${u.role.toLowerCase()}">${u.role}</span></td>
                                    <td>
                                        <span class="badge ${u.actif ? 'badge-success' : 'badge-danger'}">
                                            ${u.actif ? 'Actif' : 'Désactivé'}
                                        </span>
                                    </td>
                                    <td>${new Date(u.created_at).toLocaleDateString('fr-FR')}</td>
                                    <td>
                                        <button class="btn btn-sm btn-outline" onclick="toggleStatutUser(${u.id}, ${!u.actif})">
                                            ${u.actif ? 'Désactiver' : 'Activer'}
                                        </button>
                                        <button class="btn btn-sm btn-outline" onclick="reinitialiserMdpUser(${u.id})">
                                            Reset MDP
                                        </button>
                                    </td>
                                </tr>
                            `).join('')}
                        </tbody>
                    </table>
                </div>
            </div>
        `;
    } catch (err) {
        container.innerHTML = `<div class="alert alert-danger">${err.message}</div>`;
    }
}

async function toggleStatutUser(userId, actif) {
    try {
        await api.patch(`/users/${userId}/statut`, { actif });
        afficherNotification('Statut du compte mis à jour.', 'success');
        chargerVueUsers();
    } catch (err) {
        afficherNotification(err.message, 'danger');
    }
}

async function reinitialiserMdpUser(userId) {
    if (!confirm('Voulez-vous réinitialiser le mot de passe de cet utilisateur ?')) return;
    try {
        const res = await api.post(`/users/${userId}/reinitialiser-mdp`, {});
        afficherNotification(`Nouveau mot de passe temporaire : ${res.mot_de_passe_temporaire}`, 'success', 8000);
    } catch (err) {
        afficherNotification(err.message, 'danger');
    }
}

/* =====================================================================
   GESTION DES MODALES D'INTERACTION & FORMULAIRES
   ===================================================================== */
function ouvrirModale(contenuHtml, { titre = 'Détails', grandFormat = false, boutons = [] } = {}) {
    const backdrop = document.getElementById('modal-container');
    const dialog = document.getElementById('modal-dialog-content');

    dialog.className = `modal-dialog ${grandFormat ? 'modal-large' : ''}`;
    dialog.innerHTML = `
        <div class="modal-header">
            <h2>${titre}</h2>
            <button class="btn-icon" onclick="fermerModale()">✕</button>
        </div>
        <div class="modal-body">
            ${contenuHtml}
        </div>
        <div class="modal-footer">
            ${boutons.map((b, i) => `
                <button class="btn ${b.classe || 'btn-outline'}" id="modal-btn-${i}">${b.label}</button>
            `).join('')}
        </div>
    `;

    boutons.forEach((b, i) => {
        const btnEl = document.getElementById(`modal-btn-${i}`);
        if (btnEl && b.action) btnEl.onclick = b.action;
    });

    backdrop.classList.remove('hidden');
}

function fermerModale() {
    const backdrop = document.getElementById('modal-container');
    if (backdrop) backdrop.classList.add('hidden');
}

// Modal Inscription Élève
async function ouvrirModaleInscrireEleve() {
    const resClasses = await api.get('/classes');
    const classes = resClasses.classes;

    const html = `
        <form id="form-nouvel-eleve" onsubmit="soumettreNouvelEleve(event)">
            <div class="form-row">
                <div class="form-group">
                    <label>Nom de famille *</label>
                    <input type="text" id="el-nom" class="form-control" required placeholder="Ex: Dupont">
                </div>
                <div class="form-group">
                    <label>Prénom *</label>
                    <input type="text" id="el-prenom" class="form-control" required placeholder="Ex: Thomas">
                </div>
            </div>
            <div class="form-row">
                <div class="form-group">
                    <label>Date de naissance *</label>
                    <input type="date" id="el-date-naissance" class="form-control" required>
                </div>
                <div class="form-group">
                    <label>Sexe</label>
                    <select id="el-sexe" class="form-control">
                        <option value="M">Masculin</option>
                        <option value="F">Féminin</option>
                    </select>
                </div>
            </div>
            <div class="form-group">
                <label>Classe d'affectation *</label>
                <select id="el-classe" class="form-control" required>
                    <option value="">Sélectionnez une classe</option>
                    ${classes.map(c => `<option value="${c.id}">${c.nom} (${c.niveau})</option>`).join('')}
                </select>
            </div>
            <div class="form-row">
                <div class="form-group">
                    <label>Nom du responsable légal / parent *</label>
                    <input type="text" id="el-parent-nom" class="form-control" required placeholder="Ex: Dupont Michel">
                </div>
                <div class="form-group">
                    <label>Téléphone parent *</label>
                    <input type="tel" id="el-parent-tel" class="form-control" required placeholder="06 12 34 56 78">
                </div>
            </div>
            <div class="form-group">
                <label>Email du parent (pour envoi des identifiants)</label>
                <input type="email" id="el-parent-email" class="form-control" placeholder="parent@email.fr">
            </div>
            <button type="submit" id="btn-submit-eleve" class="btn btn-primary btn-block" style="margin-top:10px;">
                Valider l'inscription de l'élève
            </button>
        </form>
    `;

    ouvrirModale(html, { titre: 'Inscrire un nouvel élève' });
}

async function soumettreNouvelEleve(event) {
    event.preventDefault();
    try {
        const donnees = {
            nom: document.getElementById('el-nom').value.trim(),
            prenom: document.getElementById('el-prenom').value.trim(),
            date_naissance: document.getElementById('el-date-naissance').value,
            sexe: document.getElementById('el-sexe').value,
            classe_id: document.getElementById('el-classe').value,
            nom_parent: document.getElementById('el-parent-nom').value.trim(),
            telephone_parent: document.getElementById('el-parent-tel').value.trim(),
            email_parent: document.getElementById('el-parent-email').value.trim()
        };

        const res = await api.post('/eleves', donnees);
        fermerModale();
        afficherNotification(`Élève inscrit ! Identifiant : ${res.identifiants_temporaires.username}`, 'success', 7000);
        naviguerVers('eleves');
    } catch (err) {
        afficherNotification(err.message, 'danger');
    }
}

// Modal Saisie de Note
async function ouvrirModaleSaisieNote() {
    const [resClasses, resMatieres] = await Promise.all([
        api.get('/classes'),
        api.get('/matieres')
    ]);

    const html = `
        <form id="form-nouvelle-note" onsubmit="soumettreNouvelleNote(event)">
            <div class="form-row">
                <div class="form-group">
                    <label>Classe</label>
                    <select id="note-classe" class="form-control" onchange="chargerElevesPourSaisieNote(this.value)" required>
                        <option value="">Sélectionnez la classe</option>
                        ${resClasses.classes.map(c => `<option value="${c.id}">${c.nom}</option>`).join('')}
                    </select>
                </div>
                <div class="form-group">
                    <label>Matière</label>
                    <select id="note-matiere" class="form-control" required>
                        ${resMatieres.matieres.map(m => `<option value="${m.id}">${m.nom} (Coeff ${m.coefficient})</option>`).join('')}
                    </select>
                </div>
            </div>
            <div class="form-group">
                <label>Élève</label>
                <select id="note-eleve" class="form-control" required>
                    <option value="">Sélectionnez d'abord une classe</option>
                </select>
            </div>
            <div class="form-row">
                <div class="form-group">
                    <label>Intitulé de l'évaluation</label>
                    <input type="text" id="note-titre" class="form-control" required value="Contrôle continu">
                </div>
                <div class="form-group">
                    <label>Note obtenue /20 *</label>
                    <input type="number" id="note-valeur" class="form-control" step="0.25" min="0" max="20" required placeholder="Ex: 15.5">
                </div>
            </div>
            <div class="form-group">
                <label>Appréciation / Commentaire</label>
                <input type="text" id="note-commentaire" class="form-control" placeholder="Ex: Bon raisonnement...">
            </div>
            <button type="submit" class="btn btn-primary btn-block">Enregistrer la note</button>
        </form>
    `;

    ouvrirModale(html, { titre: 'Saisir une note d\'évaluation' });
}

async function chargerElevesPourSaisieNote(classeId) {
    if (!classeId) return;
    const res = await api.get(`/eleves?classe_id=${classeId}`);
    const select = document.getElementById('note-eleve');
    select.innerHTML = res.eleves.map(e => `<option value="${e.id}">${e.nom} ${e.prenom} (${e.matricule})</option>`).join('');
}

async function soumettreNouvelleNote(event) {
    event.preventDefault();
    try {
        const donnees = {
            eleve_id: document.getElementById('note-eleve').value,
            matiere_id: document.getElementById('note-matiere').value,
            periode_id: 1, // Trimestre 1
            titre_evaluation: document.getElementById('note-titre').value,
            note: document.getElementById('note-valeur').value,
            bareme: 20,
            coefficient: 1,
            commentaire: document.getElementById('note-commentaire').value
        };

        await api.post('/notes', donnees);
        fermerModale();
        afficherNotification('Note enregistrée avec succès !', 'success');
        if (vueActuelle === 'notes') chargerVueNotes();
    } catch (err) {
        afficherNotification(err.message, 'danger');
    }
}

// Modal Appel de classe
async function ouvrirModaleFaireAppel(classeIdParam = null) {
    const resClasses = await api.get('/classes');
    const classes = resClasses.classes;
    const defaultClasseId = classeIdParam || (classes[0] ? classes[0].id : null);

    const html = `
        <div>
            <div class="form-row" style="margin-bottom:14px;">
                <div class="form-group">
                    <label>Classe à appeler :</label>
                    <select id="appel-classe-id" class="form-control" onchange="chargerListeElevesAppel(this.value)">
                        ${classes.map(c => `<option value="${c.id}" ${c.id === defaultClasseId ? 'selected' : ''}>${c.nom}</option>`).join('')}
                    </select>
                </div>
                <div class="form-group">
                    <label>Créneau horaire :</label>
                    <select id="appel-creneau" class="form-control">
                        <option value="08:00 - 10:00">08:00 - 10:00</option>
                        <option value="10:15 - 12:15">10:15 - 12:15</option>
                        <option value="13:30 - 15:30">13:30 - 15:30</option>
                        <option value="15:45 - 17:45">15:45 - 17:45</option>
                    </select>
                </div>
            </div>

            <div id="liste-appel-container" style="max-height:350px; overflow-y:auto; margin-bottom:16px;">
                <div class="spinner"></div>
            </div>

            <button type="button" class="btn btn-primary btn-block" onclick="validerAppelClasse()">
                Valider et enregistrer l'appel
            </button>
        </div>
    `;

    ouvrirModale(html, { titre: 'Faire l\'appel de classe', grandFormat: true });
    if (defaultClasseId) chargerListeElevesAppel(defaultClasseId);
}

async function chargerListeElevesAppel(classeId) {
    const container = document.getElementById('liste-appel-container');
    const res = await api.get(`/eleves?classe_id=${classeId}`);
    const eleves = res.eleves;

    container.innerHTML = `
        <table class="custom-table">
            <thead>
                <tr>
                    <th>Élève</th>
                    <th style="text-align:center;">Présence</th>
                    <th>Motif si absence</th>
                </tr>
            </thead>
            <tbody>
                ${eleves.map(e => `
                    <tr>
                        <td><strong>${e.nom} ${e.prenom}</strong></td>
                        <td style="text-align:center;">
                            <label style="margin-right:8px;"><input type="radio" name="statut_${e.id}" value="PRESENT" checked> P</label>
                            <label style="margin-right:8px;"><input type="radio" name="statut_${e.id}" value="RETARD"> R</label>
                            <label><input type="radio" name="statut_${e.id}" value="ABSENT"> A</label>
                        </td>
                        <td>
                            <input type="text" id="motif_${e.id}" class="form-control form-control-sm" placeholder="Motif éventuel...">
                        </td>
                    </tr>
                `).join('')}
            </tbody>
        </table>
    `;
}

async function validerAppelClasse() {
    try {
        const classeId = document.getElementById('appel-classe-id').value;
        const creneau = document.getElementById('appel-creneau').value;
        const radios = document.querySelectorAll('input[type="radio"]:checked');

        const appel = [];
        radios.forEach(r => {
            const eleveId = r.name.replace('statut_', '');
            const motif = document.getElementById(`motif_${eleveId}`)?.value || '';
            appel.push({
                eleve_id: parseInt(eleveId, 10),
                statut: r.value,
                motif
            });
        });

        const res = await api.post('/absences/appel', {
            classe_id: classeId,
            date: new Date().toISOString().split('T')[0],
            creneau_horaire: creneau,
            appel
        });

        fermerModale();
        afficherNotification(res.message, 'success');
        if (vueActuelle === 'absences') chargerVueAbsences();
    } catch (err) {
        afficherNotification(err.message, 'danger');
    }
}

// Modal Encaisser Paiement
async function ouvrirModaleEnregistrerPaiement() {
    const resEleves = await api.get('/eleves');

    const html = `
        <form onsubmit="soumettrePaiement(event)">
            <div class="form-group">
                <label>Élève bénéficiaire *</label>
                <select id="pay-eleve" class="form-control" required>
                    <option value="">Sélectionnez l'élève</option>
                    ${resEleves.eleves.map(e => `<option value="${e.id}">${e.nom} ${e.prenom} (${e.nom_classe || 'Sans classe'})</option>`).join('')}
                </select>
            </div>
            <div class="form-row">
                <div class="form-group">
                    <label>Montant en Euros (€) *</label>
                    <input type="number" id="pay-montant" class="form-control" step="0.01" min="1" required placeholder="Ex: 500">
                </div>
                <div class="form-group">
                    <label>Mode de règlement</label>
                    <select id="pay-mode" class="form-control">
                        <option value="ESPECES">Espèces</option>
                        <option value="VIREMENT">Virement bancaire</option>
                        <option value="CHEQUE">Chèque</option>
                        <option value="CARTE">Carte bancaire</option>
                    </select>
                </div>
            </div>
            <div class="form-group">
                <label>Commentaire sur le reçu</label>
                <input type="text" id="pay-comment" class="form-control" placeholder="Ex: 1er versement de rentrée">
            </div>
            <button type="submit" class="btn btn-primary btn-block">Enregistrer le reçu de paiement</button>
        </form>
    `;

    ouvrirModale(html, { titre: 'Encaisser un paiement de scolarité' });
}

async function soumettrePaiement(event) {
    event.preventDefault();
    try {
        const donnees = {
            eleve_id: document.getElementById('pay-eleve').value,
            montant: document.getElementById('pay-montant').value,
            type: 'SCOLARITE',
            mode_paiement: document.getElementById('pay-mode').value,
            commentaire: document.getElementById('pay-comment').value
        };

        const res = await api.post('/paiements', donnees);
        fermerModale();
        afficherNotification(`Paiement validé ! Référence : ${res.reference}`, 'success');
        if (vueActuelle === 'paiements') chargerVuePaiements();
    } catch (err) {
        afficherNotification(err.message, 'danger');
    }
}

// Modal Justifier Absence
function ouvrirModaleJustifierAbsence(absenceId) {
    const html = `
        <form onsubmit="soumettreJustification(event, ${absenceId})">
            <div class="form-group">
                <label>Motif officiel de justification *</label>
                <select id="justif-motif" class="form-control" required>
                    <option value="Certificat médical / Maladie">Certificat médical / Maladie</option>
                    <option value="Raison familiale impérieuse">Raison familiale impérieuse</option>
                    <option value="Problème de transport avéré">Problème de transport avéré</option>
                    <option value="Convocation administrative">Convocation administrative</option>
                    <option value="Autre motif valable">Autre motif valable</option>
                </select>
            </div>
            <div class="form-group">
                <label>Précisions / Remarques administratives</label>
                <input type="text" id="justif-remarques" class="form-control" placeholder="Ex: Justificatif papier reçu...">
            </div>
            <button type="submit" class="btn btn-success btn-block">Valider la justification</button>
        </form>
    `;

    ouvrirModale(html, { titre: 'Justifier l\'absence' });
}

async function soumettreJustification(event, absenceId) {
    event.preventDefault();
    try {
        await api.put(`/absences/${absenceId}/justifier`, {
            motif: document.getElementById('justif-motif').value,
            remarques: document.getElementById('justif-remarques').value
        });
        fermerModale();
        afficherNotification('Absence justifiée avec succès.', 'success');
        if (vueActuelle === 'absences') chargerVueAbsences();
    } catch (err) {
        afficherNotification(err.message, 'danger');
    }
}

// Suppressions génériques avec confirmation
async function supprimerEleve(id) {
    if (!confirm('Êtes-vous sûr de vouloir supprimer définitivement cet élève et son compte ?')) return;
    try {
        await api.delete(`/eleves/${id}`);
        afficherNotification('Élève supprimé.', 'info');
        chargerVueEleves();
    } catch (err) {
        afficherNotification(err.message, 'danger');
    }
}

async function supprimerProfesseur(id) {
    if (!confirm('Supprimer ce professeur ?')) return;
    try {
        await api.delete(`/professeurs/${id}`);
        afficherNotification('Professeur supprimé.', 'info');
        chargerVueProfesseurs();
    } catch (err) {
        afficherNotification(err.message, 'danger');
    }
}

async function supprimerClasse(id) {
    if (!confirm('Supprimer cette classe ?')) return;
    try {
        await api.delete(`/classes/${id}`);
        afficherNotification('Classe supprimée.', 'info');
        chargerVueClasses();
    } catch (err) {
        afficherNotification(err.message, 'danger');
    }
}

async function supprimerMatiere(id) {
    if (!confirm('Supprimer cette matière ?')) return;
    try {
        await api.delete(`/matieres/${id}`);
        afficherNotification('Matière supprimée.', 'info');
        chargerVueMatieres();
    } catch (err) {
        afficherNotification(err.message, 'danger');
    }
}

async function supprimerNote(id) {
    if (!confirm('Supprimer cette note d\'évaluation ?')) return;
    try {
        await api.delete(`/notes/${id}`);
        afficherNotification('Note supprimée.', 'info');
        chargerVueNotes();
    } catch (err) {
        afficherNotification(err.message, 'danger');
    }
}

async function supprimerAbsence(id) {
    if (!confirm('Supprimer cet enregistrement d\'absence ?')) return;
    try {
        await api.delete(`/absences/${id}`);
        afficherNotification('Enregistrement supprimé.', 'info');
        chargerVueAbsences();
    } catch (err) {
        afficherNotification(err.message, 'danger');
    }
}

async function supprimerCours(id) {
    if (!confirm('Supprimer ce créneau d\'emploi du temps ?')) return;
    try {
        await api.delete(`/emplois-du-temps/${id}`);
        afficherNotification('Créneau supprimé.', 'info');
        chargerVueEmploiDuTemps();
    } catch (err) {
        afficherNotification(err.message, 'danger');
    }
}

/**
 * Recherche Globale en temps réel (Élèves, Professeurs, Classes)
 */
let searchDebounceTimer = null;
let globalCache = { eleves: null, professeurs: null, classes: null };

async function handleGlobalSearch(event) {
    const query = event.target.value.trim().toLowerCase();
    const dropdown = document.getElementById('global-search-results');
    if (!dropdown) return;

    if (!query || query.length < 2) {
        dropdown.innerHTML = '';
        dropdown.classList.add('hidden');
        return;
    }

    clearTimeout(searchDebounceTimer);
    searchDebounceTimer = setTimeout(async () => {
        try {
            // Charger les données si pas encore en cache
            if (!globalCache.eleves) {
                const [eRes, pRes, cRes] = await Promise.all([
                    api.get('/eleves').catch(() => ({ donnees: [] })),
                    api.get('/professeurs').catch(() => ({ donnees: [] })),
                    api.get('/classes').catch(() => ({ donnees: [] }))
                ]);
                globalCache.eleves = eRes.donnees || [];
                globalCache.professeurs = pRes.donnees || [];
                globalCache.classes = cRes.donnees || [];
            }

            const elevesMatches = globalCache.eleves.filter(e =>
                (e.nom && e.nom.toLowerCase().includes(query)) ||
                (e.prenom && e.prenom.toLowerCase().includes(query)) ||
                (e.matricule && e.matricule.toLowerCase().includes(query))
            );

            const profsMatches = globalCache.professeurs.filter(p =>
                (p.nom && p.nom.toLowerCase().includes(query)) ||
                (p.prenom && p.prenom.toLowerCase().includes(query)) ||
                (p.matiere && p.matiere.toLowerCase().includes(query))
            );

            const classesMatches = globalCache.classes.filter(c =>
                (c.nom && c.nom.toLowerCase().includes(query)) ||
                (c.niveau && c.niveau.toLowerCase().includes(query))
            );

            const total = elevesMatches.length + profsMatches.length + classesMatches.length;

            if (total === 0) {
                dropdown.innerHTML = `<div class="search-empty">Aucun résultat trouvé pour « ${query} »</div>`;
                dropdown.classList.remove('hidden');
                return;
            }

            let html = '';

            if (elevesMatches.length > 0) {
                html += `<div class="search-section-title">Élèves (${elevesMatches.length})</div>`;
                html += elevesMatches.slice(0, 5).map(e => `
                    <div class="search-item" onclick="selectionnerEleveRecherche(${e.id})">
                        <span class="search-icon-badge badge-eleve">👨‍🎓</span>
                        <div class="search-item-info">
                            <strong>${e.nom} ${e.prenom}</strong>
                            <small>Matricule: ${e.matricule || 'N/A'} • ${e.classe_nom || 'Sans classe'}</small>
                        </div>
                    </div>
                `).join('');
            }

            if (profsMatches.length > 0) {
                html += `<div class="search-section-title">Professeurs (${profsMatches.length})</div>`;
                html += profsMatches.slice(0, 5).map(p => `
                    <div class="search-item" onclick="selectionnerProfRecherche(${p.id})">
                        <span class="search-icon-badge badge-prof">👨‍🏫</span>
                        <div class="search-item-info">
                            <strong>${p.nom} ${p.prenom}</strong>
                            <small>${p.matiere || 'Enseignant'} • ${p.email || ''}</small>
                        </div>
                    </div>
                `).join('');
            }

            if (classesMatches.length > 0) {
                html += `<div class="search-section-title">Classes (${classesMatches.length})</div>`;
                html += classesMatches.slice(0, 5).map(c => `
                    <div class="search-item" onclick="selectionnerClasseRecherche(${c.id})">
                        <span class="search-icon-badge badge-classe">🏫</span>
                        <div class="search-item-info">
                            <strong>${c.nom}</strong>
                            <small>${c.niveau || 'Niveau'} • ${c.salle || 'Salle principale'}</small>
                        </div>
                    </div>
                `).join('');
            }

            dropdown.innerHTML = html;
            dropdown.classList.remove('hidden');
        } catch (err) {
            console.error('Erreur recherche globale :', err);
        }
    }, 150);
}

function fermerRechercheGlobale() {
    const dropdown = document.getElementById('global-search-results');
    if (dropdown) dropdown.classList.add('hidden');
    const input = document.getElementById('global-search-input');
    if (input) input.value = '';
}

function selectionnerEleveRecherche(id) {
    fermerRechercheGlobale();
    naviguerVers('eleves');
    setTimeout(() => {
        if (typeof ouvrirModalDossierEleve === 'function') ouvrirModalDossierEleve(id);
    }, 300);
}

function selectionnerProfRecherche(id) {
    fermerRechercheGlobale();
    naviguerVers('professeurs');
}

function selectionnerClasseRecherche(id) {
    fermerRechercheGlobale();
    naviguerVers('classes');
}

// Fermer le dropdown si on clique en dehors
document.addEventListener('click', (e) => {
    const container = document.querySelector('.global-search-container');
    if (container && !container.contains(e.target)) {
        const dropdown = document.getElementById('global-search-results');
        if (dropdown) dropdown.classList.add('hidden');
    }
});
