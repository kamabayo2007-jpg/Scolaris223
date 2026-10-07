/**
 * Client API REST pour l'application ERP Gestion École
 */
const API_BASE_URL = '/api';

const api = {
    /**
     * Méthode générique pour exécuter une requête HTTP
     */
    async requete(endpoint, methode = 'GET', donnees = null) {
        const token = localStorage.getItem('erp_token');
        const headers = {
            'Content-Type': 'application/json'
        };

        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        const options = {
            method: methode,
            headers
        };

        if (donnees && (methode === 'POST' || methode === 'PUT' || methode === 'PATCH')) {
            options.body = JSON.stringify(donnees);
        }

        try {
            const reponse = await fetch(`${API_BASE_URL}${endpoint}`, options);
            const resultat = await reponse.json().catch(() => ({}));

            if (!reponse.ok) {
                // Gestion de session expirée (401)
                if (reponse.status === 401) {
                    if (localStorage.getItem('erp_token')) {
                        afficherNotification('Votre session a expiré. Veuillez vous reconnecter.', 'warning');
                        localStorage.removeItem('erp_token');
                        localStorage.removeItem('erp_user');
                        setTimeout(() => {
                            if (typeof afficherEcranConnexion === 'function') {
                                afficherEcranConnexion();
                            }
                        }, 1000);
                    }
                }

                const messageErreur = resultat.message || `Erreur serveur (${reponse.status})`;
                throw new Error(messageErreur);
            }

            return resultat;
        } catch (erreur) {
            console.error(`Erreur API [${methode} ${endpoint}] :`, erreur);
            throw erreur;
        }
    },

    get(endpoint) {
        return this.requete(endpoint, 'GET');
    },

    post(endpoint, donnees) {
        return this.requete(endpoint, 'POST', donnees);
    },

    put(endpoint, donnees) {
        return this.requete(endpoint, 'PUT', donnees);
    },

    patch(endpoint, donnees) {
        return this.requete(endpoint, 'PATCH', donnees);
    },

    delete(endpoint) {
        return this.requete(endpoint, 'DELETE');
    }
};

/**
 * Système de notifications Toast professionnel
 */
function afficherNotification(message, type = 'info', dureeMs = 4000) {
    const container = document.getElementById('toast-container');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;

    let icone = 'ℹ️';
    if (type === 'success') icone = '✅';
    if (type === 'danger') icone = '❌';
    if (type === 'warning') icone = '⚠️';

    toast.innerHTML = `
        <span class="toast-icon">${icone}</span>
        <span class="toast-message">${message}</span>
    `;

    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, dureeMs);
}
