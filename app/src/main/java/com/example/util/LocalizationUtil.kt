package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

enum class AppLanguage(val code: String, val displayName: String, val flagEmoji: String) {
    FR("fr", "Français", "🇫🇷"),
    EN("en", "English", "🇬🇧")
}

object LocalizationUtil {

    private val fcfaSymbols = DecimalFormatSymbols(Locale.FRENCH).apply {
        groupingSeparator = ' '
    }
    private val fcfaFormatter = DecimalFormat("#,##0", fcfaSymbols)

    /**
     * Formats an amount into the official Franc CFA (FCFA / XOF) currency string.
     * Example: 250000.0 -> "250 000 FCFA"
     */
    fun formatFcfa(amount: Double): String {
        return "${fcfaFormatter.format(amount)} FCFA"
    }

    /**
     * Translation dictionary for bilingual support (FR / EN)
     */
    fun t(key: String, lang: AppLanguage): String {
        val fr = translations[key]?.first ?: key
        val en = translations[key]?.second ?: fr
        return if (lang == AppLanguage.EN) en else fr
    }

    private val translations = mapOf(
        // Navigation & Top Bar
        "nav_dashboard" to Pair("Accueil", "Dashboard"),
        "nav_grades" to Pair("Notes", "Grades"),
        "nav_attendance" to Pair("Assiduité", "Attendance"),
        "nav_timetable" to Pair("Planning", "Timetable"),
        "nav_students" to Pair("Élèves", "Students"),
        "nav_finance" to Pair("Finances", "Finance"),
        "nav_schools" to Pair("Multi-Écoles", "Multi-Schools"),
        "nav_subscriptions" to Pair("Souscriptions", "Subscriptions"),
        "app_title" to Pair("Scolaris", "Scolaris"),
        "search_hint" to Pair("Rechercher élève, classe, enseignant, reçu...", "Search student, class, teacher, receipt..."),

        // Roles
        "role_super_admin" to Pair("Super Administrateur", "Super Administrator"),
        "role_admin" to Pair("Administrateur", "Administrator"),
        "role_direction" to Pair("Direction", "Management"),
        "role_professeur" to Pair("Professeur", "Teacher"),
        "role_eleve" to Pair("Élève", "Student"),
        "role_parent" to Pair("Parent d'Élève", "Parent"),
        "role_comptable" to Pair("Comptable", "Accountant"),

        // Multi-School & Subscriptions
        "school_selector" to Pair("Établissement Scolaire", "School Institution"),
        "switch_school" to Pair("Changer d'école", "Switch School"),
        "all_schools_overview" to Pair("Toutes les Écoles (Super Admin)", "All Schools (Super Admin)"),
        "subscription_status" to Pair("Statut de Souscription", "Subscription Status"),
        "subscription_active" to Pair("Abonnement Actif", "Active Subscription"),
        "subscription_pending" to Pair("Renouvellement Requis", "Renewal Required"),
        "subscription_expired" to Pair("Abonnement Expiré", "Subscription Expired"),
        "subscription_trial" to Pair("Période d'Essai", "Trial Period"),
        "registration_fee" to Pair("Frais d'Adhésion / Inscription", "Initial Registration Fee"),
        "annual_subscription_fee" to Pair("Frais d'Abonnement Annuel", "Annual Subscription Fee"),
        "pay_subscription" to Pair("Régler la Souscription", "Pay Subscription"),
        "renew_subscription" to Pair("Renouveler l'Abonnement", "Renew Subscription"),
        "school_name" to Pair("Nom de l'établissement", "School Name"),
        "school_code" to Pair("Code Établissement", "School Code"),
        "school_city" to Pair("Ville", "City"),
        "school_country" to Pair("Pays", "Country"),
        "add_new_school" to Pair("Ajouter une Nouvelle École", "Add New School"),

        // Payment Methods (Orange Money, Moov Money, Wave, etc.)
        "payment_method" to Pair("Mode de Paiement", "Payment Method"),
        "orange_money" to Pair("Orange Money", "Orange Money"),
        "moov_money" to Pair("Moov Money", "Moov Money"),
        "wave" to Pair("Wave Mobile Money", "Wave Mobile Money"),
        "cash" to Pair("Espèces", "Cash"),
        "bank_transfer" to Pair("Virement Bancaire", "Bank Transfer"),
        "phone_number" to Pair("Numéro de Téléphone (Mobile Money)", "Phone Number (Mobile Money)"),
        "txn_reference" to Pair("Référence de Transaction", "Transaction Reference"),
        "amount_fcfa" to Pair("Montant en Franc CFA", "Amount in CFA Francs"),

        // Dashboard & Metrics
        "total_students" to Pair("Total Élèves", "Total Students"),
        "active_teachers" to Pair("Enseignants Actifs", "Active Teachers"),
        "pending_payments" to Pair("Paiements en Attente", "Pending Payments"),
        "attendance_rate" to Pair("Taux de Présence", "Attendance Rate"),
        "general_average" to Pair("Moyenne Générale", "General Average"),
        "total_classes" to Pair("Classes Ouvertes", "Active Classes"),
        "quick_actions" to Pair("Actions Rapides", "Quick Actions"),
        "recent_grades" to Pair("Dernières Notes Enregistrées", "Recent Grades Recorded"),
        "activity_logs" to Pair("Journal d'Activité & Audit", "Activity Log & Audit"),

        // Actions & Buttons
        "btn_export_pdf" to Pair("Exporter PDF", "Export PDF"),
        "btn_add" to Pair("Ajouter", "Add"),
        "btn_save" to Pair("Enregistrer", "Save"),
        "btn_cancel" to Pair("Annuler", "Cancel"),
        "btn_validate" to Pair("Valider", "Validate"),
        "btn_filter" to Pair("Filtrer", "Filter"),
        "btn_view_all" to Pair("Voir Tout", "View All"),
        "btn_details" to Pair("Détails", "Details"),
        "btn_bulletin" to Pair("Bulletin Officiel", "Report Card"),
        "theme_toggle" to Pair("Mode Sombre / Clair", "Dark / Light Mode"),
        "language_select" to Pair("Langue / Language", "Language / Langue")
    )
}
