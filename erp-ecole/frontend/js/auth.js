/**
 * Gestion de l'authentification et des sessions utilisateur
 */

function estConnecte() {
    return Boolean(localStorage.getItem('erp_token'));
}

function getUser() {
    try {
        return JSON.parse(localStorage.getItem('erp_user')) || null;
    } catch {
        return null;
    }
}

function getRole() {
    const user = getUser();
    return user ? user.role : null;
}

/**
 * Traite la soumission du formulaire de connexion
 */
async function handleLogin(event) {
    event.preventDefault();
    const usernameInput = document.getElementById('login-username');
    const passwordInput = document.getElementById('login-password');
    const submitBtn = document.getElementById('btn-login-submit');

    const username_ou_email = usernameInput.value.trim();
    const password = passwordInput.value;

    if (!username_ou_email || !password) {
        afficherNotification('Veuillez saisir votre identifiant et votre mot de passe.', 'warning');
        return;
    }

    try {
        submitBtn.disabled = true;
        submitBtn.textContent = 'Connexion en cours...';

        const res = await api.post('/auth/login', { username_ou_email, password });

        if (res.succes && res.token) {
            localStorage.setItem('erp_token', res.token);
            localStorage.setItem('erp_user', JSON.stringify(res.user));

            afficherNotification(`Bienvenue ${res.user.username} ! Connexion réussie.`, 'success');

            // Masquer écran login et afficher l'application
            document.getElementById('auth-screen').classList.add('hidden');
            document.getElementById('app-layout').classList.remove('hidden');

            mettreAJourTopbar();
            initialiserMenuEtNavigation();
        }
    } catch (error) {
        afficherNotification(error.message || 'Échec de la connexion.', 'danger');
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Se connecter à l\'espace scolaire';
    }
}

/**
 * Remplissage rapide pour les comptes de démonstration
 */
function remplirIdentifiants(username, password) {
    document.getElementById('login-username').value = username;
    document.getElementById('login-password').value = password;
}

/**
 * Déconnexion de l'utilisateur
 */
async function logout() {
    try {
        await api.post('/auth/logout');
    } catch {
        // Ignorer l'erreur éventuelle de déconnexion réseau
    } finally {
        localStorage.removeItem('erp_token');
        localStorage.removeItem('erp_user');
        afficherNotification('Vous êtes déconnecté.', 'info');
        afficherEcranConnexion();
    }
}

/**
 * Affiche l'écran de login
 */
function afficherEcranConnexion() {
    document.getElementById('auth-screen').classList.remove('hidden');
    document.getElementById('app-layout').classList.add('hidden');
}

/**
 * Met à jour la barre supérieure avec le nom et rôle de l'utilisateur connecté
 */
function mettreAJourTopbar() {
    const user = getUser();
    if (!user) return;

    const avatarEl = document.getElementById('topbar-user-avatar');
    const nameEl = document.getElementById('topbar-user-name');
    const roleEl = document.getElementById('topbar-user-role');

    if (avatarEl) {
        avatarEl.textContent = (user.username || 'U').substring(0, 2).toUpperCase();
    }
    if (nameEl) {
        const nomComplet = user.profil ? `${user.profil.prenom || ''} ${user.profil.nom || ''}`.trim() : user.username;
        nameEl.textContent = nomComplet || user.username;
    }
    if (roleEl) {
        roleEl.textContent = user.role;
        roleEl.className = `user-role-badge role-${user.role.toLowerCase()}`;
    }
}

/**
 * Vérifie la validité de la session au chargement de la page
 */
async function verifierSession() {
    if (!estConnecte()) {
        afficherEcranConnexion();
        return;
    }

    try {
        const res = await api.get('/auth/me');
        if (res.succes && res.user) {
            localStorage.setItem('erp_user', JSON.stringify(res.user));
            document.getElementById('auth-screen').classList.add('hidden');
            document.getElementById('app-layout').classList.remove('hidden');
            mettreAJourTopbar();
            initialiserMenuEtNavigation();
        }
    } catch {
        afficherEcranConnexion();
    }
}
