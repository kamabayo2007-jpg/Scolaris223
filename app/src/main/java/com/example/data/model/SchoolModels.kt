package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// === Multi-Écoles & Tenants ===
enum class SchoolSubscriptionStatus(val labelFr: String, val labelEn: String, val colorHex: String) {
    ACTIVE("Abonnement Actif", "Active Subscription", "#16A34A"),
    PENDING_RENEWAL("À Renouveler", "Pending Renewal", "#D97706"),
    EXPIRED("Expiré", "Expired", "#DC2626"),
    TRIAL("Période d'Essai", "Trial Period", "#2563EB")
}

enum class SubscriptionFeeType(val labelFr: String, val labelEn: String) {
    INSCRIPTION("Frais d'Adhésion / Inscription", "Initial Registration Fee"),
    ABONNEMENT_ANNUEL("Abonnement Annuel Plateforme", "Annual Platform Subscription")
}

@Entity(tableName = "schools")
data class School(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val code: String,
    val city: String = "Abidjan",
    val country: String = "Côte d'Ivoire",
    val phone: String = "+225 07 00 00 00 00",
    val email: String = "contact@ecole.ci",
    val registrationFee: Double = 100000.0, // Frais d'inscription initiale (FCFA)
    val annualSubscriptionFee: Double = 500000.0, // Frais annuel de la plateforme (FCFA)
    val subscriptionStatus: SchoolSubscriptionStatus = SchoolSubscriptionStatus.ACTIVE,
    val subscriptionExpiry: String = "2027-09-30",
    val bannerColor: String = "#1E3A8A",
    val academicYear: String = "2025-2026",
    val directorName: String = "Dr. Robert Kouassi",
    val officialMotto: String = "Discipline - Travail - Succès",
    val currency: String = "FCFA",
    val academyName: String = "Académie d'Enseignement de Bamako",
    val countrySeal: String = "🇲🇱 République du Mali — Un Peuple, Un But, Une Foi",
    val ministryName: String = "Ministère de l'Éducation Nationale",
    val isActive: Boolean = true
)

@Entity(
    tableName = "school_subscriptions",
    foreignKeys = [
        ForeignKey(
            entity = School::class,
            parentColumns = ["id"],
            childColumns = ["schoolId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("schoolId")]
)
data class SchoolSubscription(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val schoolId: Long,
    val feeType: SubscriptionFeeType,
    val amount: Double,
    val paymentMethod: String = "Orange Money", // Orange Money, Moov Money, Wave, Virement, Espèces
    val phoneNumber: String = "",
    val transactionReference: String = "",
    val paymentDate: String = "",
    val validUntil: String = "",
    val status: String = "VALIDE", // VALIDE, EN_ATTENTE
    val notes: String = ""
)

@Entity(tableName = "classes")
data class SchoolClass(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val schoolId: Long = 1L,
    val name: String,
    val level: String,
    val academicYear: String = "2025-2026",
    val room: String = "",
    val mainTeacher: String = ""
)

@Entity(
    tableName = "students",
    foreignKeys = [
        ForeignKey(
            entity = SchoolClass::class,
            parentColumns = ["id"],
            childColumns = ["classId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("classId")]
)
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val schoolId: Long = 1L,
    val classId: Long,
    val firstName: String,
    val lastName: String,
    val matricule: String,
    val gender: String = "M",
    val birthDate: String = "",
    val parentEmail: String = "",
    val parentPhone: String = "",
    val avatarColorHex: String = "#1E3A8A",
    val isExamCandidate: Boolean = false, // Candidat officiel à un examen d'académie (DEF, BAC, etc.)
    val academyExamNumber: String = "", // Numéro officiel délivré par l'Académie (vide si en attente d'attribution)
    val barcodeOrQrData: String = "" // Donnée de scan pour présence et appel rapide
) {
    val fullName: String get() = "$firstName $lastName"
    val effectiveQrData: String get() = barcodeOrQrData.ifBlank { "SCOLARIS:STU:$id:$matricule" }
}

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val schoolId: Long = 1L,
    val name: String,
    val code: String,
    val coefficient: Double = 1.0,
    val colorHex: String = "#2563EB",
    val iconName: String = "book"
)

@Entity(tableName = "teachers")
data class Teacher(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val schoolId: Long = 1L,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String = "",
    val subjectName: String = ""
) {
    val fullName: String get() = "$firstName $lastName"
}

@Entity(
    tableName = "grades",
    foreignKeys = [
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("studentId"), Index("subjectId"), Index("classId")]
)
data class Grade(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val schoolId: Long = 1L,
    val studentId: Long,
    val subjectId: Long,
    val classId: Long,
    val title: String,
    val gradeValue: Double,
    val outOf: Double = 20.0,
    val coefficient: Double = 1.0,
    val period: String = "Trimestre 1",
    val date: String,
    val comment: String = "",
    val isMainGrade: Boolean = false, // Note principale / Examen vs Contrôle Continu
    val isPublished: Boolean = false, // Validé et publié par la Scolarité (visible aux élèves)
    val createdAt: Long = System.currentTimeMillis() // Horodatage pour règle stricte de verrouillage des 5 jours
) {
    val normalizedTo20: Double get() = if (outOf > 0) (gradeValue / outOf) * 20.0 else gradeValue

    // Vérifie si le professeur ne peut plus modifier la note (délai de 5 jours écoulé)
    fun isLockedForTeacher(now: Long = System.currentTimeMillis()): Boolean {
        val fiveDaysMillis = 5L * 24 * 60 * 60 * 1000L
        return (now - createdAt) > fiveDaysMillis
    }
}

enum class AttendanceStatus {
    PRESENT,
    ABSENT_UNJUSTIFIED,
    ABSENT_JUSTIFIED,
    LATE
}

@Entity(
    tableName = "attendance",
    foreignKeys = [
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("studentId"), Index("classId"), Index("date")]
)
data class Attendance(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val schoolId: Long = 1L,
    val studentId: Long,
    val classId: Long,
    val date: String,
    val timeSlot: String,
    val status: AttendanceStatus,
    val reason: String = "",
    val justifiedDate: String = "",
    val remarks: String = ""
)

@Entity(
    tableName = "timetable_slots",
    foreignKeys = [
        ForeignKey(
            entity = SchoolClass::class,
            parentColumns = ["id"],
            childColumns = ["classId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("classId"), Index("subjectId")]
)
data class TimetableSlot(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val schoolId: Long = 1L,
    val classId: Long,
    val subjectId: Long,
    val teacherName: String,
    val teacherId: Long? = null,
    val dayOfWeek: Int,
    val startTime: String,
    val endTime: String,
    val room: String
)

data class TimetableConflict(
    val reason: String,
    val dayOfWeek: Int,
    val startTime: String,
    val endTime: String,
    val conflictingSlot: TimetableSlot
)

// === Calendrier Scolaire Annuel (Dates clés, vacances, trimestres) ===
@Entity(tableName = "academic_calendar_events")
data class AcademicCalendarEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val schoolId: Long = 1L,
    val title: String,
    val category: String = "TRIMESTRE", // TRIMESTRE, VACANCES, EXAMEN, FERIE, REUNION, EVENEMENT
    val startDate: String,
    val endDate: String = "",
    val description: String = "",
    val academicYear: String = "2025-2026"
)

data class StudentOverview(
    val student: Student,
    val className: String,
    val generalAverage: Double?,
    val absencesCount: Int,
    val unjustifiedAbsencesCount: Int,
    val latesCount: Int
)

data class SubjectAverage(
    val subject: Subject,
    val average: Double,
    val gradesCount: Int
)

data class TimetableDisplaySlot(
    val slot: TimetableSlot,
    val subject: Subject?,
    val className: String
)

// === Utilisateurs & Rôles ===
enum class UserRole(val label: String, val badgeColorHex: String) {
    SUPER_ADMIN("Super Administrateur", "#0F172A"),
    ADMIN("Administrateur", "#DC2626"),
    DIRECTION("Direction", "#7C3AED"),
    PROFESSEUR("Professeur", "#2563EB"),
    ELEVE("Élève", "#059669"),
    PARENT("Parent d'Élève", "#D97706"),
    COMPTABLE("Comptable", "#0891B2")
}

@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true)]
)
data class UserAccount(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val schoolId: Long = 1L,
    val username: String,
    val passwordHash: String,
    val fullName: String,
    val email: String,
    val role: UserRole,
    val linkedStudentId: Long? = null,
    val linkedStudentIds: String = "", // Identifiants séparés par virgule (ex: "1,2") pour les parents multi-enfants
    val linkedTeacherId: Long? = null,
    val avatarColorHex: String = "#1E3A8A",
    val isActive: Boolean = true,
    val lastLogin: String = ""
)

// === Modes de Paiement Mobile Money & Traditionnels ===
object MobilePaymentMethods {
    const val ORANGE_MONEY = "Orange Money"
    const val MOOV_MONEY = "Moov Money"
    const val WAVE = "Wave"
    const val ESPECES = "Espèces"
    const val VIREMENT = "Virement Bancaire"
    const val CHEQUE = "Chèque"

    val all = listOf(ORANGE_MONEY, MOOV_MONEY, WAVE, ESPECES, VIREMENT)
}

enum class PaymentType(val label: String) {
    SCOLARITE("Scolarité Annuelle"),
    INSCRIPTION("Frais d'Inscription"),
    CANTINE("Restauration Scolaire"),
    TRANSPORT("Transport Scolaire"),
    AUTRE("Frais Divers")
}

enum class PaymentStatus(val label: String) {
    PAYE("Payé Intégralement"),
    PARTIEL("Paiement Partiel"),
    EN_ATTENTE("En Attente")
}

@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("studentId"), Index("date")]
)
data class Payment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val schoolId: Long = 1L,
    val studentId: Long,
    val receiptNumber: String,
    val type: PaymentType,
    val amountTotal: Double,
    val amountPaid: Double,
    val date: String,
    val paymentMethod: String = MobilePaymentMethods.ORANGE_MONEY,
    val mobilePhoneNumber: String = "",
    val isLocalPayment: Boolean = false,
    val isValidatedByScolarite: Boolean = false, // Validation par la scolarité pour les paiements locaux
    val validatedBy: String = "", // Nom de l'agent de scolarité ayant validé l'encaissement
    val validationDate: String = "",
    val status: PaymentStatus,
    val remarks: String = "",
    val academicYear: String = "2025-2026"
) {
    val remainingAmount: Double get() = maxOf(0.0, amountTotal - amountPaid)
}

// === Suivi de Paiement & Salaires des Professeurs ===
@Entity(
    tableName = "teacher_payments",
    indices = [Index("teacherId"), Index("periodMonth")]
)
data class TeacherPayment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val schoolId: Long = 1L,
    val teacherId: Long,
    val teacherName: String,
    val periodMonth: String, // ex: "Octobre 2025"
    val academicYear: String = "2025-2026",
    val baseSalary: Double, // en FCFA
    val bonusAmount: Double = 0.0,
    val deductions: Double = 0.0,
    val netAmount: Double, // Salaire net à payer (FCFA)
    val paymentDate: String,
    val paymentMethod: String = MobilePaymentMethods.VIREMENT, // Orange Money, Wave, Virement, Espèces
    val transactionRef: String = "",
    val status: String = "PAYE", // PAYE, EN_ATTENTE, EN_COURS
    val notes: String = ""
)

// === Modèles de Sauvegarde et Restauration Technique Locale ===
data class BackupMetadata(
    val version: String = "1.0.0",
    val schoolId: Long,
    val schoolCode: String,
    val schoolName: String,
    val dateExport: String,
    val totalStudents: Int,
    val totalPayments: Int,
    val systemSignature: String = "ERP_GE_SECURE_BACKUP"
)

data class SchoolBackupData(
    val metadata: BackupMetadata,
    val school: School,
    val classes: List<SchoolClass>,
    val students: List<Student>,
    val subjects: List<Subject>,
    val teachers: List<Teacher>,
    val grades: List<Grade>,
    val attendances: List<Attendance>,
    val payments: List<Payment>,
    val timetableSlots: List<TimetableSlot>,
    val subscriptions: List<SchoolSubscription>
)

@Entity(tableName = "notifications")
data class SchoolNotification(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val schoolId: Long = 1L,
    val title: String,
    val message: String,
    val targetRole: String = "ALL",
    val targetStudentId: Long? = null,
    val category: String = "GENERAL", // NOTES_RESULTATS, PAIE_PROFESSEUR, PAIEMENT_SCOLARITE, CALENDRIER, ADMINISTRATIF, GENERAL
    val date: String,
    val isRead: Boolean = false,
    val type: String = "INFO"
)

data class SubjectBulletinLine(
    val subject: Subject,
    val gradesCount: Int,
    val average: Double?,
    val coefficient: Double,
    val teacherName: String,
    val appreciation: String
)

data class BulletinReport(
    val student: Student,
    val schoolClass: SchoolClass,
    val period: String,
    val academicYear: String,
    val lines: List<SubjectBulletinLine>,
    val totalCoefficients: Double,
    val generalWeightedAverage: Double?,
    val classAverage: Double?,
    val classRank: Int,
    val totalStudentsInClass: Int,
    val totalAbsences: Int,
    val unjustifiedAbsences: Int,
    val totalLates: Int,
    val generalAppreciation: String,
    val generatedDate: String
)

@Entity(tableName = "activity_logs")
data class ActivityLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val schoolId: Long = 1L,
    val timestamp: String,
    val actionType: String,
    val description: String,
    val userFullName: String,
    val userRole: String,
    val targetEntity: String = "",
    val details: String = ""
)
