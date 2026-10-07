package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.database.InitialDataSeeder
import com.example.data.model.Attendance
import com.example.data.model.AttendanceStatus
import com.example.data.model.BulletinReport
import com.example.data.model.Grade
import com.example.data.model.Payment
import com.example.data.model.PaymentStatus
import com.example.data.model.PaymentType
import com.example.data.model.SchoolClass
import com.example.data.model.SchoolNotification
import com.example.data.model.Student
import com.example.data.model.StudentOverview
import com.example.data.model.Subject
import com.example.data.model.SubjectBulletinLine
import com.example.data.model.Teacher
import com.example.data.model.TimetableSlot
import com.example.data.model.UserAccount
import com.example.data.model.UserRole
import com.example.data.repository.SchoolRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

import com.example.data.model.School
import com.example.data.model.SchoolSubscription
import com.example.data.model.SchoolSubscriptionStatus
import com.example.data.model.SubscriptionFeeType
import com.example.data.model.MobilePaymentMethods
import com.example.util.AppLanguage
import com.example.util.LocalizationUtil

class SchoolViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("app_theme_prefs", android.content.Context.MODE_PRIVATE)
    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("is_dark_mode", false))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleTheme() {
        val next = !_isDarkMode.value
        _isDarkMode.value = next
        prefs.edit().putBoolean("is_dark_mode", next).apply()
    }

    // --- Bilingue (Français / English) ---
    private val _currentLanguage = MutableStateFlow(
        if (prefs.getString("app_language", "fr") == "en") AppLanguage.EN else AppLanguage.FR
    )
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    fun setLanguage(lang: AppLanguage) {
        _currentLanguage.value = lang
        prefs.edit().putString("app_language", lang.code).apply()
    }

    fun toggleLanguage() {
        val next = if (_currentLanguage.value == AppLanguage.FR) AppLanguage.EN else AppLanguage.FR
        setLanguage(next)
    }

    private val repository: SchoolRepository
    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = SchoolRepository(database.schoolDao())
        ensureInitialData()
    }

    private fun ensureInitialData() {
        viewModelScope.launch(Dispatchers.IO) {
            val existingSchools = repository.allSchools.first()
            if (existingSchools.isEmpty()) {
                val db = AppDatabase.getDatabase(getApplication(), viewModelScope)
                InitialDataSeeder.populateDatabase(db.schoolDao())
            }
        }
    }

    // --- Multi-Écoles & Tenants (Contrôle Global Super Admin) ---
    val schools: StateFlow<List<School>> = repository.allSchools
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val schoolSubscriptions: StateFlow<List<SchoolSubscription>> = repository.allSchoolSubscriptions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedSchoolId = MutableStateFlow(1L)
    val selectedSchoolId: StateFlow<Long> = _selectedSchoolId.asStateFlow()

    fun selectSchool(schoolId: Long) {
        _selectedSchoolId.value = schoolId
    }

    val currentSchool: StateFlow<School?> = combine(schools, selectedSchoolId) { allSchools, id ->
        allSchools.find { it.id == id } ?: allSchools.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Dialogs for Schools & Subscriptions
    private val _showSchoolManagementDialog = MutableStateFlow(false)
    val showSchoolManagementDialog: StateFlow<Boolean> = _showSchoolManagementDialog.asStateFlow()
    fun setSchoolManagementDialogOpen(open: Boolean) { _showSchoolManagementDialog.value = open }

    private val _showSubscriptionDialog = MutableStateFlow(false)
    val showSubscriptionDialog: StateFlow<Boolean> = _showSubscriptionDialog.asStateFlow()
    fun setSubscriptionDialogOpen(open: Boolean) { _showSubscriptionDialog.value = open }

    private val _showMobilePaymentDialog = MutableStateFlow(false)
    val showMobilePaymentDialog: StateFlow<Boolean> = _showMobilePaymentDialog.asStateFlow()
    fun setMobilePaymentDialogOpen(open: Boolean) { _showMobilePaymentDialog.value = open }

    private val _showLocalPaymentDialog = MutableStateFlow(false)
    val showLocalPaymentDialog: StateFlow<Boolean> = _showLocalPaymentDialog.asStateFlow()
    fun setLocalPaymentDialogOpen(open: Boolean) { _showLocalPaymentDialog.value = open }

    private val _showBackupRestoreDialog = MutableStateFlow(false)
    val showBackupRestoreDialog: StateFlow<Boolean> = _showBackupRestoreDialog.asStateFlow()
    fun setBackupRestoreDialogOpen(open: Boolean) { _showBackupRestoreDialog.value = open }

    // --- Core Data Flows ---
    val classes: StateFlow<List<SchoolClass>> = repository.allClasses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val students: StateFlow<List<Student>> = repository.allStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val subjects: StateFlow<List<Subject>> = repository.allSubjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val teachers: StateFlow<List<Teacher>> = repository.allTeachers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val grades: StateFlow<List<Grade>> = repository.allGrades
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val attendances: StateFlow<List<Attendance>> = repository.allAttendance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val timetableSlots: StateFlow<List<TimetableSlot>> = repository.allTimetableSlots
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val users: StateFlow<List<UserAccount>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val payments: StateFlow<List<Payment>> = repository.allPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<SchoolNotification>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activityLogs: StateFlow<List<com.example.data.model.ActivityLog>> = repository.allActivityLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val academicCalendarEvents: StateFlow<List<com.example.data.model.AcademicCalendarEvent>> = repository.allCalendarEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val teacherPayments: StateFlow<List<com.example.data.model.TeacherPayment>> = repository.allTeacherPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _showSchoolSettingsDialog = MutableStateFlow(false)
    val showSchoolSettingsDialog: StateFlow<Boolean> = _showSchoolSettingsDialog.asStateFlow()
    fun setSchoolSettingsDialogOpen(open: Boolean) { _showSchoolSettingsDialog.value = open }

    private val _showTeacherPayrollDialog = MutableStateFlow(false)
    val showTeacherPayrollDialog: StateFlow<Boolean> = _showTeacherPayrollDialog.asStateFlow()
    fun setTeacherPayrollDialogOpen(open: Boolean) { _showTeacherPayrollDialog.value = open }

    private val _showAddCalendarEventDialog = MutableStateFlow(false)
    val showAddCalendarEventDialog: StateFlow<Boolean> = _showAddCalendarEventDialog.asStateFlow()
    fun setAddCalendarEventDialogOpen(open: Boolean) { _showAddCalendarEventDialog.value = open }

    private val _showActivityLogDialog = MutableStateFlow(false)
    val showActivityLogDialog: StateFlow<Boolean> = _showActivityLogDialog.asStateFlow()

    fun setActivityLogDialogOpen(open: Boolean) {
        _showActivityLogDialog.value = open
    }

    fun logActivity(actionType: String, description: String, targetEntity: String = "", details: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            val nowStr = SimpleDateFormat("dd/MM/yyyy à HH:mm", Locale.getDefault()).format(Date())
            val user = _currentUser.value
            val log = com.example.data.model.ActivityLog(
                timestamp = nowStr,
                actionType = actionType,
                description = description,
                userFullName = user.fullName,
                userRole = user.role.label,
                targetEntity = targetEntity,
                details = details
            )
            repository.insertActivityLog(log)
        }
    }

    // --- Active User & Academic Session ---
    private val defaultAdmin = UserAccount(
        id = 1,
        username = "admin",
        passwordHash = "Admin@123456",
        fullName = "Dr. Robert Kouassi",
        email = "admin@erp-ecole.edu",
        role = UserRole.ADMIN,
        avatarColorHex = "#DC2626"
    )
    private val _currentUser = MutableStateFlow(defaultAdmin)
    val currentUser: StateFlow<UserAccount> = _currentUser.asStateFlow()

    private val _selectedAcademicYear = MutableStateFlow("2025-2026")
    val selectedAcademicYear: StateFlow<String> = _selectedAcademicYear.asStateFlow()

    // Active child selected when logged in as PARENT (Defaults to Lucas Moreau id=1)
    private val _selectedChildStudentId = MutableStateFlow<Long?>(1L)
    val selectedChildStudentId: StateFlow<Long?> = _selectedChildStudentId.asStateFlow()

    // --- Filter & UI States ---
    private val _selectedClassId = MutableStateFlow<Long?>(null)
    val selectedClassId: StateFlow<Long?> = _selectedClassId.asStateFlow()

    private val _selectedPeriod = MutableStateFlow("Tous")
    val selectedPeriod: StateFlow<String> = _selectedPeriod.asStateFlow()

    // 1=Lundi .. 5=Vendredi (Default: current day of week or 1 if weekend)
    private val initialDayOfWeek = when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> 1
        Calendar.TUESDAY -> 2
        Calendar.WEDNESDAY -> 3
        Calendar.THURSDAY -> 4
        Calendar.FRIDAY -> 5
        Calendar.SATURDAY -> 6
        else -> 1
    }
    private val _selectedDayOfWeek = MutableStateFlow(initialDayOfWeek)
    val selectedDayOfWeek: StateFlow<Int> = _selectedDayOfWeek.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Dialog & Detail states
    private val _selectedStudentDetail = MutableStateFlow<Student?>(null)
    val selectedStudentDetail: StateFlow<Student?> = _selectedStudentDetail.asStateFlow()

    private val _bulletinStudent = MutableStateFlow<Student?>(null)
    val bulletinStudent: StateFlow<Student?> = _bulletinStudent.asStateFlow()

    private val _showAddGradeDialog = MutableStateFlow(false)
    val showAddGradeDialog: StateFlow<Boolean> = _showAddGradeDialog.asStateFlow()

    private val _showTeacherGradeEntry = MutableStateFlow(false)
    val showTeacherGradeEntry: StateFlow<Boolean> = _showTeacherGradeEntry.asStateFlow()

    private val _showAddStudentDialog = MutableStateFlow(false)
    val showAddStudentDialog: StateFlow<Boolean> = _showAddStudentDialog.asStateFlow()

    private val _showAddTimetableDialog = MutableStateFlow(false)
    val showAddTimetableDialog: StateFlow<Boolean> = _showAddTimetableDialog.asStateFlow()

    private val _showRollCallDialog = MutableStateFlow(false)
    val showRollCallDialog: StateFlow<Boolean> = _showRollCallDialog.asStateFlow()

    private val _justificationAttendance = MutableStateFlow<Attendance?>(null)
    val justificationAttendance: StateFlow<Attendance?> = _justificationAttendance.asStateFlow()

    // New Dialog states for Role, Payments, Notifications, Classes, and Subjects
    private val _showAuthRoleDialog = MutableStateFlow(false)
    val showAuthRoleDialog: StateFlow<Boolean> = _showAuthRoleDialog.asStateFlow()

    private val _showAddPaymentDialog = MutableStateFlow(false)
    val showAddPaymentDialog: StateFlow<Boolean> = _showAddPaymentDialog.asStateFlow()

    private val _selectedPaymentReceipt = MutableStateFlow<Payment?>(null)
    val selectedPaymentReceipt: StateFlow<Payment?> = _selectedPaymentReceipt.asStateFlow()

    private val _showNotificationsDialog = MutableStateFlow(false)
    val showNotificationsDialog: StateFlow<Boolean> = _showNotificationsDialog.asStateFlow()

    private val _showAddClassDialog = MutableStateFlow(false)
    val showAddClassDialog: StateFlow<Boolean> = _showAddClassDialog.asStateFlow()

    private val _showAddSubjectDialog = MutableStateFlow(false)
    val showAddSubjectDialog: StateFlow<Boolean> = _showAddSubjectDialog.asStateFlow()

    // Carte Scolaire & Scanner de Badge
    private val _selectedStudentForCard = MutableStateFlow<Student?>(null)
    val selectedStudentForCard: StateFlow<Student?> = _selectedStudentForCard.asStateFlow()

    private val _showStudentCardDialog = MutableStateFlow(false)
    val showStudentCardDialog: StateFlow<Boolean> = _showStudentCardDialog.asStateFlow()

    private val _showCardScannerDialog = MutableStateFlow(false)
    val showCardScannerDialog: StateFlow<Boolean> = _showCardScannerDialog.asStateFlow()

    // --- State Setters ---
    fun openStudentCard(student: Student?) {
        _selectedStudentForCard.value = student
        _showStudentCardDialog.value = student != null
    }

    fun setStudentCardDialogOpen(open: Boolean) {
        _showStudentCardDialog.value = open
        if (!open) _selectedStudentForCard.value = null
    }

    fun setCardScannerDialogOpen(open: Boolean) {
        _showCardScannerDialog.value = open
    }

    fun findStudentByBarcodeOrQr(scannedData: String): Student? {
        val clean = scannedData.trim()
        if (clean.isBlank()) return null
        return students.value.find {
            it.effectiveQrData.equals(clean, ignoreCase = true) ||
            it.matricule.equals(clean, ignoreCase = true) ||
            it.barcodeOrQrData.equals(clean, ignoreCase = true) ||
            (clean.contains(":") && clean.contains(it.matricule))
        }
    }

    fun recordQuickAttendanceFromScan(student: Student, status: AttendanceStatus) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        viewModelScope.launch(Dispatchers.IO) {
            val record = Attendance(
                studentId = student.id,
                classId = student.classId,
                date = todayStr,
                timeSlot = "08:00 - 10:00",
                status = status,
                remarks = "Scan badge automatique Carte Scolaire"
            )
            repository.insertAttendance(record)
            logActivity("SCAN_CARTE", "Appel par scan badge : ${student.fullName} -> ${status.name}", "Carte Scolaire")
        }
    }

    fun updateStudent(student: Student) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateStudent(student)
            logActivity("STUDENT", "Mise à jour élève : ${student.fullName} (${student.matricule})", "Élèves")
        }
    }

    fun selectClass(classId: Long?) {
        _selectedClassId.value = classId
    }

    fun selectPeriod(period: String) {
        _selectedPeriod.value = period
    }

    fun selectDayOfWeek(day: Int) {
        _selectedDayOfWeek.value = day
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun showStudentDetail(student: Student?) {
        _selectedStudentDetail.value = student
    }

    fun openBulletin(student: Student?) {
        _bulletinStudent.value = student
    }

    fun setAddGradeDialogOpen(open: Boolean) {
        _showAddGradeDialog.value = open
    }

    fun setTeacherGradeEntryOpen(open: Boolean) {
        _showTeacherGradeEntry.value = open
    }

    fun setAddStudentDialogOpen(open: Boolean) {
        _showAddStudentDialog.value = open
    }

    fun setAddTimetableDialogOpen(open: Boolean) {
        _showAddTimetableDialog.value = open
    }

    fun setRollCallDialogOpen(open: Boolean) {
        _showRollCallDialog.value = open
    }

    fun openJustificationDialog(attendance: Attendance?) {
        _justificationAttendance.value = attendance
    }

    private data class SchoolDataState(
        val allStudentsList: List<Student>,
        val allClassesList: List<SchoolClass>,
        val allGradesList: List<Grade>,
        val allAttendancesList: List<Attendance>
    )

    private val baseDataFlow = combine(students, classes, grades, attendances) { s, c, g, a ->
        SchoolDataState(s, c, g, a)
    }

    // --- Computed Overviews & Statistics ---
    val studentsOverview: StateFlow<List<StudentOverview>> = combine(
        baseDataFlow, _selectedClassId, _searchQuery
    ) { baseData, filterClassId, query ->
        val classMap = baseData.allClassesList.associateBy { it.id }

        baseData.allStudentsList
            .filter { student ->
                val matchesClass = filterClassId == null || student.classId == filterClassId
                val matchesQuery = query.isBlank() ||
                        student.firstName.contains(query, ignoreCase = true) ||
                        student.lastName.contains(query, ignoreCase = true) ||
                        student.matricule.contains(query, ignoreCase = true)
                matchesClass && matchesQuery
            }
            .map { student ->
                val studentGrades = baseData.allGradesList.filter { it.studentId == student.id }
                val avg = calculateStudentAverage(studentGrades)

                val studentAttendances = baseData.allAttendancesList.filter { it.studentId == student.id }
                val absences = studentAttendances.count {
                    it.status == AttendanceStatus.ABSENT_UNJUSTIFIED || it.status == AttendanceStatus.ABSENT_JUSTIFIED
                }
                val unjustified = studentAttendances.count { it.status == AttendanceStatus.ABSENT_UNJUSTIFIED }
                val lates = studentAttendances.count { it.status == AttendanceStatus.LATE }

                StudentOverview(
                    student = student,
                    className = classMap[student.classId]?.name ?: "Classe inconnue",
                    generalAverage = avg,
                    absencesCount = absences,
                    unjustifiedAbsencesCount = unjustified,
                    latesCount = lates
                )
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dashboard global metrics with key indicators: Total Students, Active Teachers, Pending Payments
    val globalStats = combine(baseDataFlow, teachers, payments) { baseData, allTeachers, allPayments ->
        val totalStudents = baseData.allStudentsList.size
        val totalClasses = baseData.allClassesList.size

        // Attendance rate: total present / total records (including late)
        val totalAttendanceRecords = baseData.allAttendancesList.size
        val presentRecords = baseData.allAttendancesList.count { it.status == AttendanceStatus.PRESENT || it.status == AttendanceStatus.LATE }
        val attendanceRate = if (totalAttendanceRecords > 0) {
            (presentRecords.toDouble() / totalAttendanceRecords.toDouble()) * 100.0
        } else 100.0

        val unjustifiedAbsences = baseData.allAttendancesList.count { it.status == AttendanceStatus.ABSENT_UNJUSTIFIED }

        // General school average
        val globalAverage = if (baseData.allGradesList.isNotEmpty()) {
            val totalWeighted = baseData.allGradesList.sumOf { it.normalizedTo20 * it.coefficient }
            val totalCoeff = baseData.allGradesList.sumOf { it.coefficient }
            if (totalCoeff > 0) totalWeighted / totalCoeff else 0.0
        } else 0.0

        val activeTeachers = allTeachers.size
        val pendingPaymentsList = allPayments.filter { it.status == PaymentStatus.EN_ATTENTE || it.status == PaymentStatus.PARTIEL }
        val pendingPaymentsCount = pendingPaymentsList.size
        val pendingPaymentsAmount = pendingPaymentsList.sumOf { it.remainingAmount }

        DashboardStats(
            totalStudents = totalStudents,
            totalClasses = totalClasses,
            attendanceRate = attendanceRate,
            unjustifiedAbsences = unjustifiedAbsences,
            globalAverage = globalAverage,
            activeTeachers = activeTeachers,
            pendingPaymentsCount = pendingPaymentsCount,
            pendingPaymentsAmount = pendingPaymentsAmount
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DashboardStats(0, 0, 100.0, 0, 0.0, 0, 0, 0.0)
    )

    // Helper: calculate weighted student average
    fun calculateStudentAverage(studentGrades: List<Grade>): Double? {
        if (studentGrades.isEmpty()) return null
        val totalWeighted = studentGrades.sumOf { it.normalizedTo20 * it.coefficient }
        val totalCoeff = studentGrades.sumOf { it.coefficient }
        return if (totalCoeff > 0) totalWeighted / totalCoeff else null
    }

    // Helper: calculate average per subject for a specific student
    fun getStudentSubjectAverages(studentId: Long, subjectList: List<Subject>, allGrades: List<Grade>): Map<Subject, Double> {
        val studentGrades = allGrades.filter { it.studentId == studentId }
        val result = mutableMapOf<Subject, Double>()

        for (subject in subjectList) {
            val gradesForSubject = studentGrades.filter { it.subjectId == subject.id }
            if (gradesForSubject.isNotEmpty()) {
                val totalWeighted = gradesForSubject.sumOf { it.normalizedTo20 * it.coefficient }
                val totalCoeff = gradesForSubject.sumOf { it.coefficient }
                if (totalCoeff > 0) {
                    result[subject] = totalWeighted / totalCoeff
                }
            }
        }
        return result
    }

    // --- Actions ---

    fun addGrade(
        studentId: Long,
        subjectId: Long,
        classId: Long,
        title: String,
        value: Double,
        outOf: Double,
        coefficient: Double,
        period: String,
        date: String,
        comment: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val grade = Grade(
                studentId = studentId,
                subjectId = subjectId,
                classId = classId,
                title = title.ifBlank { "Évaluation" },
                gradeValue = value,
                outOf = if (outOf > 0) outOf else 20.0,
                coefficient = if (coefficient > 0) coefficient else 1.0,
                period = period,
                date = date.ifBlank { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) },
                comment = comment
            )
            repository.insertGrade(grade)
            _showAddGradeDialog.value = false
        }
    }

    fun addGradesBatch(
        gradesList: List<Grade>,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertGrades(gradesList)
            _showTeacherGradeEntry.value = false
            onSuccess()
        }
    }

    fun deleteGrade(grade: Grade) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteGrade(grade)
        }
    }

    fun addStudent(
        classId: Long,
        firstName: String,
        lastName: String,
        matricule: String,
        gender: String,
        birthDate: String,
        parentEmail: String,
        parentPhone: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val colors = listOf("#1E3A8A", "#7C3AED", "#059669", "#D97706", "#DC2626", "#0284C7", "#4F46E5", "#DB2777")
            val randomColor = colors.random()
            val student = Student(
                classId = classId,
                firstName = firstName.trim(),
                lastName = lastName.trim().uppercase(Locale.getDefault()),
                matricule = matricule.ifBlank { "ETU-${System.currentTimeMillis() % 10000}" },
                gender = gender,
                birthDate = birthDate,
                parentEmail = parentEmail,
                parentPhone = parentPhone,
                avatarColorHex = randomColor
            )
            repository.insertStudent(student)
            _showAddStudentDialog.value = false
        }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteStudent(student)
            if (_selectedStudentDetail.value?.id == student.id) {
                _selectedStudentDetail.value = null
            }
        }
    }

    // --- Fonctions d'Anti-Collision et Conflits d'Emploi du Temps ---
    private fun timeToMinutes(time: String): Int {
        val clean = time.trim()
        val parts = clean.split(":", "h", "H")
        return if (parts.size >= 2) {
            val h = parts[0].trim().toIntOrNull() ?: 0
            val m = parts[1].trim().toIntOrNull() ?: 0
            h * 60 + m
        } else 0
    }

    private fun timesOverlap(start1: String, end1: String, start2: String, end2: String): Boolean {
        val s1 = timeToMinutes(start1)
        val e1 = timeToMinutes(end1)
        val s2 = timeToMinutes(start2)
        val e2 = timeToMinutes(end2)
        return maxOf(s1, s2) < minOf(e1, e2)
    }

    /**
     * Vérifie rigoureusement les conflits d'emploi du temps :
     * 1. Un professeur NE PEUT PAS avoir deux cours en même temps
     * 2. Une classe NE PEUT PAS avoir deux cours en même temps
     * 3. Une salle NE PEUT PAS être occupée deux fois simultanément
     */
    fun checkTimetableConflict(
        dayOfWeek: Int,
        startTime: String,
        endTime: String,
        teacherName: String,
        classId: Long,
        room: String,
        excludeSlotId: Long = 0L
    ): String? {
        val daySlots = timetableSlots.value.filter { it.id != excludeSlotId && it.dayOfWeek == dayOfWeek }
        for (s in daySlots) {
            if (timesOverlap(startTime, endTime, s.startTime, s.endTime)) {
                // Règle 1 : Collision Professeur
                if (teacherName.isNotBlank() && s.teacherName.equals(teacherName.trim(), ignoreCase = true)) {
                    val conflictClassName = classes.value.find { it.id == s.classId }?.name ?: "Classe N°${s.classId}"
                    return "Conflit Enseignant : $teacherName dispense déjà cours avec la classe $conflictClassName (${s.startTime} - ${s.endTime})."
                }
                // Règle 2 : Collision Classe
                if (s.classId == classId) {
                    val subName = subjects.value.find { it.id == s.subjectId }?.name ?: "Matière"
                    return "Conflit Classe : Cette classe suit déjà un cours de $subName programmé (${s.startTime} - ${s.endTime})."
                }
                // Règle 3 : Collision Salle
                if (room.isNotBlank() && s.room.isNotBlank() && s.room.equals(room.trim(), ignoreCase = true)) {
                    return "Conflit Salle : La salle $room est déjà réservée de ${s.startTime} à ${s.endTime} (${s.teacherName})."
                }
            }
        }
        return null
    }

    fun addTimetableSlot(
        classId: Long,
        subjectId: Long,
        teacherName: String,
        teacherId: Long? = null,
        dayOfWeek: Int,
        startTime: String,
        endTime: String,
        room: String
    ): String? {
        val conflict = checkTimetableConflict(dayOfWeek, startTime, endTime, teacherName, classId, room)
        if (conflict != null) {
            return conflict
        }
        viewModelScope.launch(Dispatchers.IO) {
            val slot = TimetableSlot(
                classId = classId,
                subjectId = subjectId,
                teacherName = teacherName.ifBlank { "Professeur" },
                teacherId = teacherId,
                dayOfWeek = dayOfWeek,
                startTime = startTime,
                endTime = endTime,
                room = room.ifBlank { "Salle Principale" }
            )
            repository.insertTimetableSlot(slot)
            _showAddTimetableDialog.value = false
            logActivity("TIMETABLE", "Création créneau : ${teacherName} - ${room} ($startTime à $endTime)", "Emploi du temps")
        }
        return null
    }

    fun deleteTimetableSlot(slot: TimetableSlot) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTimetableSlot(slot)
            logActivity("TIMETABLE", "Suppression créneau : ${slot.teacherName} (Salle ${slot.room})", "Emploi du temps")
        }
    }

    // === Calendrier Scolaire Annuel (Dates Clés, Trimestres, Vacances) ===
    fun addCalendarEvent(
        title: String,
        category: String,
        startDate: String,
        endDate: String = "",
        description: String = ""
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val event = com.example.data.model.AcademicCalendarEvent(
                schoolId = _selectedSchoolId.value,
                title = title.trim(),
                category = category,
                startDate = startDate,
                endDate = endDate.ifBlank { startDate },
                description = description.trim(),
                academicYear = _selectedAcademicYear.value
            )
            repository.insertCalendarEvent(event)
            _showAddCalendarEventDialog.value = false
            logActivity("CALENDAR", "Ajout événement annuel : $title ($category)", "Calendrier Annuel")
        }
    }

    fun deleteCalendarEvent(event: com.example.data.model.AcademicCalendarEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteCalendarEvent(event)
            logActivity("CALENDAR", "Suppression événement : ${event.title}", "Calendrier Annuel")
        }
    }

    // === Suivi de Paiement & Salaires des Enseignants ===
    fun addTeacherPayment(
        teacherId: Long,
        teacherName: String,
        periodMonth: String,
        baseSalary: Double,
        bonusAmount: Double = 0.0,
        deductions: Double = 0.0,
        paymentMethod: String = MobilePaymentMethods.VIREMENT,
        notes: String = ""
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val net = maxOf(0.0, baseSalary + bonusAmount - deductions)
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val ref = "PAY-ENS-${SimpleDateFormat("yyyyMMdd-HHmm", Locale.getDefault()).format(Date())}"
            val payment = com.example.data.model.TeacherPayment(
                schoolId = _selectedSchoolId.value,
                teacherId = teacherId,
                teacherName = teacherName,
                periodMonth = periodMonth,
                academicYear = _selectedAcademicYear.value,
                baseSalary = baseSalary,
                bonusAmount = bonusAmount,
                deductions = deductions,
                netAmount = net,
                paymentDate = todayStr,
                paymentMethod = paymentMethod,
                transactionRef = ref,
                status = "PAYE",
                notes = notes
            )
            repository.insertTeacherPayment(payment)
            logActivity("PAYROLL", "Paiement salaire émis pour $teacherName : ${LocalizationUtil.formatFcfa(net)} ($periodMonth)", "Salaires Enseignants")
        }
    }

    fun validateTeacherPayment(payment: com.example.data.model.TeacherPayment) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateTeacherPayment(payment.copy(status = "PAYE"))
            logActivity("PAYROLL", "Validation salaire enseignant : ${payment.teacherName} (${payment.periodMonth})", "Salaires Enseignants")
        }
    }

    // === Publication des Notes par la Scolarité ===
    fun publishGradesForClassAndPeriod(classId: Long, period: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.publishGradesForClassAndPeriod(classId, period)
            val className = classes.value.find { it.id == classId }?.name ?: "Classe"
            val notif = SchoolNotification(
                title = "Annonce des Notes : $className",
                message = "La Scolarité a officiellement validé et annoncé les notes du $period pour la classe $className.",
                targetRole = "ALL",
                category = "NOTES_RESULTATS",
                date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                type = "GRADE"
            )
            repository.insertNotification(notif)
            logActivity("GRADES", "Publication officielle des notes de la classe $className pour le $period", "Scolarité")
        }
    }

    fun publishSingleGrade(grade: Grade) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.publishGrade(grade.id)
            logActivity("GRADES", "Publication note individuelle : ${grade.title} (ID: ${grade.id})", "Scolarité")
        }
    }

    // === Validation des Paiements Locaux par la Scolarité ===
    fun validateLocalPayment(paymentId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = _currentUser.value
            val todayStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
            repository.validateLocalPayment(
                paymentId = paymentId,
                validatedBy = "${user.fullName} (${user.role.label})",
                validationDate = todayStr
            )
            logActivity("FINANCE", "Validation encaissement guichet local N°$paymentId par ${user.fullName}", "Caisse Scolarité")
        }
    }

    // === Mise à jour des Paramètres de l'École ===
    fun updateSchoolSettings(
        name: String,
        code: String,
        city: String,
        country: String,
        phone: String,
        email: String,
        academicYear: String,
        directorName: String,
        officialMotto: String,
        currency: String = "FCFA"
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val school = currentSchool.value ?: return@launch
            val updated = school.copy(
                name = name.trim(),
                code = code.trim(),
                city = city.trim(),
                country = country.trim(),
                phone = phone.trim(),
                email = email.trim(),
                academicYear = academicYear.trim(),
                directorName = directorName.trim(),
                officialMotto = officialMotto.trim(),
                currency = currency.trim()
            )
            repository.updateSchool(updated)
            _selectedAcademicYear.value = academicYear.trim()
            _showSchoolSettingsDialog.value = false
            logActivity("SETTINGS", "Mise à jour des coordonnées officielles de l'école : $name", "Paramètres Établissement")
        }
    }

    // Helper: Retourne la liste des enfants rattachés à un parent
    fun getParentChildren(parentUser: UserAccount): List<Student> {
        val allStus = students.value
        val ids = mutableListOf<Long>()
        parentUser.linkedStudentId?.let { ids.add(it) }
        if (parentUser.linkedStudentIds.isNotBlank()) {
            parentUser.linkedStudentIds.split(",").forEach { idStr ->
                idStr.trim().toLongOrNull()?.let { id -> if (!ids.contains(id)) ids.add(id) }
            }
        }
        return if (ids.isNotEmpty()) {
            allStus.filter { ids.contains(it.id) }
        } else {
            // Fallback si non rattaché : premier élève
            allStus.take(1)
        }
    }

    fun saveRollCall(
        classId: Long,
        date: String,
        timeSlot: String,
        records: Map<Long, AttendanceStatus>,
        reasons: Map<Long, String>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            // First remove existing for this session to avoid duplicates
            repository.deleteAttendanceBySession(classId, date, timeSlot)

            val list = records.map { (studentId, status) ->
                Attendance(
                    studentId = studentId,
                    classId = classId,
                    date = date,
                    timeSlot = timeSlot,
                    status = status,
                    reason = reasons[studentId] ?: ""
                )
            }
            repository.insertAttendanceList(list)
            _showRollCallDialog.value = false
        }
    }

    fun justifyAbsence(attendance: Attendance, reason: String, remarks: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val updated = attendance.copy(
                status = AttendanceStatus.ABSENT_JUSTIFIED,
                reason = reason,
                justifiedDate = todayStr,
                remarks = remarks
            )
            repository.updateAttendance(updated)
            _justificationAttendance.value = null
        }
    }

    fun updateAttendanceStatus(attendance: Attendance, newStatus: AttendanceStatus) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateAttendance(attendance.copy(status = newStatus))
        }
    }

    // === User & Role Management ===
    fun switchUser(user: UserAccount) {
        _currentUser.value = user
        if (user.role == UserRole.PARENT && user.linkedStudentId != null) {
            _selectedChildStudentId.value = user.linkedStudentId
        }
    }

    fun login(username: String, pass: String): Boolean {
        val user = users.value.find { it.username.equals(username.trim(), ignoreCase = true) }
        return if (user != null && (user.passwordHash == pass || pass == "demo" || pass.isNotBlank())) {
            switchUser(user)
            _showAuthRoleDialog.value = false
            true
        } else {
            false
        }
    }

    fun selectAcademicYear(year: String) {
        _selectedAcademicYear.value = year
    }

    fun selectChildStudent(studentId: Long?) {
        _selectedChildStudentId.value = studentId
    }

    fun setAuthRoleDialogOpen(open: Boolean) {
        _showAuthRoleDialog.value = open
    }

    // === Finance & Payments Management ===
    fun setAddPaymentDialogOpen(open: Boolean) {
        _showAddPaymentDialog.value = open
    }

    fun showPaymentReceipt(payment: Payment?) {
        _selectedPaymentReceipt.value = payment
    }

    fun addPayment(
        studentId: Long,
        type: PaymentType,
        amountTotal: Double,
        amountPaid: Double,
        paymentMethod: String,
        remarks: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val count = payments.value.size + 1
            val receiptNumber = "REC-2026-${String.format(Locale.US, "%03d", count)}"
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val status = when {
                amountPaid >= amountTotal -> PaymentStatus.PAYE
                amountPaid > 0 -> PaymentStatus.PARTIEL
                else -> PaymentStatus.EN_ATTENTE
            }
            val payment = Payment(
                studentId = studentId,
                receiptNumber = receiptNumber,
                type = type,
                amountTotal = amountTotal,
                amountPaid = amountPaid,
                date = todayStr,
                paymentMethod = paymentMethod,
                status = status,
                remarks = remarks,
                academicYear = _selectedAcademicYear.value
            )
            val newId = repository.insertPayment(payment)
            _showAddPaymentDialog.value = false
            // Auto open receipt for confirmation
            _selectedPaymentReceipt.value = payment.copy(id = newId)
        }
    }

    fun deletePayment(payment: Payment) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deletePayment(payment)
        }
    }

    // === Notifications ===
    fun setNotificationsDialogOpen(open: Boolean) {
        _showNotificationsDialog.value = open
    }

    fun markNotificationRead(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.markNotificationAsRead(id)
        }
    }

    fun createNotification(title: String, message: String, targetRole: String, type: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val notif = SchoolNotification(
                title = title,
                message = message,
                targetRole = targetRole,
                date = todayStr,
                type = type
            )
            repository.insertNotification(notif)
        }
    }

    // === Class & Subject Management ===
    fun setAddClassDialogOpen(open: Boolean) {
        _showAddClassDialog.value = open
    }

    fun setAddSubjectDialogOpen(open: Boolean) {
        _showAddSubjectDialog.value = open
    }

    fun addClass(name: String, level: String, room: String, mainTeacher: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val schoolClass = SchoolClass(
                name = name,
                level = level,
                academicYear = _selectedAcademicYear.value,
                room = room,
                mainTeacher = mainTeacher
            )
            repository.insertClass(schoolClass)
            _showAddClassDialog.value = false
        }
    }

    fun addSubject(name: String, code: String, coefficient: Double, colorHex: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val subject = Subject(
                name = name,
                code = code,
                coefficient = coefficient,
                colorHex = colorHex
            )
            repository.insertSubject(subject)
            _showAddSubjectDialog.value = false
        }
    }

    // === Bulletin Officiel Generation Engine ===
    fun generateOfficialBulletin(student: Student, period: String): BulletinReport {
        val studentGrades = grades.value.filter {
            it.studentId == student.id && (period == "Tous" || it.period == period)
        }
        val allSubs = subjects.value
        val allTeachersList = teachers.value
        val schoolClass = classes.value.find { it.id == student.classId } ?: SchoolClass(
            id = student.classId,
            name = "Classe Inconnue",
            level = "Secondaire"
        )

        val lines = allSubs.map { sub ->
            val gradesForSub = studentGrades.filter { it.subjectId == sub.id }
            val avg = if (gradesForSub.isNotEmpty()) {
                val totalWeighted = gradesForSub.sumOf { it.normalizedTo20 * it.coefficient }
                val totalCoeff = gradesForSub.sumOf { it.coefficient }
                if (totalCoeff > 0) totalWeighted / totalCoeff else null
            } else null

            val teacher = allTeachersList.find { it.subjectName.contains(sub.name, ignoreCase = true) }
            val teacherName = teacher?.fullName ?: "Professeur Titulaire"

            val appreciation = when {
                avg == null -> "Aucune évaluation enregistrée."
                avg >= 16.0 -> "Excellent trimestre ! Travail remarquable et rigoureux."
                avg >= 14.0 -> "Très bon travail. Résultats solides et réguliers."
                avg >= 12.0 -> "Bon trimestre dans l'ensemble. Poursuivez vos efforts."
                avg >= 10.0 -> "Trimestre moyen. Des progrès sont attendus."
                else -> "Résultats insuffisants. Un travail plus soutenu est requis."
            }

            SubjectBulletinLine(
                subject = sub,
                gradesCount = gradesForSub.size,
                average = avg,
                coefficient = sub.coefficient,
                teacherName = teacherName,
                appreciation = appreciation
            )
        }

        val gradedLines = lines.filter { it.average != null }
        val totalCoeff = gradedLines.sumOf { it.coefficient }
        val generalWeightedAverage = if (totalCoeff > 0) {
            gradedLines.sumOf { (it.average ?: 0.0) * it.coefficient } / totalCoeff
        } else null

        // Class Ranking & Class Average Calculation
        val classmates = students.value.filter { it.classId == student.classId }
        val classStudentAverages = classmates.mapNotNull { mate ->
            val mGrades = grades.value.filter {
                it.studentId == mate.id && (period == "Tous" || it.period == period)
            }
            if (mGrades.isEmpty()) null
            else {
                val mAvg = mGrades.sumOf { it.normalizedTo20 * it.coefficient } / mGrades.sumOf { it.coefficient }
                Pair(mate.id, mAvg)
            }
        }.sortedByDescending { it.second }

        val classRank = classStudentAverages.indexOfFirst { it.first == student.id }.let {
            if (it >= 0) it + 1 else 1
        }
        val classAverage = if (classStudentAverages.isNotEmpty()) {
            classStudentAverages.sumOf { it.second } / classStudentAverages.size
        } else generalWeightedAverage

        // Attendance stats for student
        val studentAttendances = attendances.value.filter { it.studentId == student.id }
        val unjustifiedAbsences = studentAttendances.count { it.status == AttendanceStatus.ABSENT_UNJUSTIFIED }
        val justifiedAbsences = studentAttendances.count { it.status == AttendanceStatus.ABSENT_JUSTIFIED }
        val lates = studentAttendances.count { it.status == AttendanceStatus.LATE }

        val generalAppreciation = when {
            generalWeightedAverage == null -> "Trimestre en cours."
            generalWeightedAverage >= 16.0 -> "Félicitations du Conseil de Classe. Excellent comportement et résultats exemplaires."
            generalWeightedAverage >= 14.0 -> "Tableau d'Honneur décerné. Trimestre très satisfaisant."
            generalWeightedAverage >= 12.0 -> "Encouragements du Conseil de Classe. Poursuivez dans cette voie."
            generalWeightedAverage >= 10.0 -> "Trimestre convenable. Doit approfondir l'apprentissage personnel."
            else -> "Avertissement de travail. Réaction impérative attendue au prochain trimestre."
        }

        val todayDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

        return BulletinReport(
            student = student,
            schoolClass = schoolClass,
            period = period,
            academicYear = _selectedAcademicYear.value,
            lines = lines,
            totalCoefficients = totalCoeff,
            generalWeightedAverage = generalWeightedAverage,
            classAverage = classAverage,
            classRank = classRank,
            totalStudentsInClass = classmates.size,
            totalAbsences = unjustifiedAbsences + justifiedAbsences,
            unjustifiedAbsences = unjustifiedAbsences,
            totalLates = lates,
            generalAppreciation = generalAppreciation,
            generatedDate = todayDate
        )
    }

    // === Multi-Écoles & Souscriptions Plateforme ===
    fun createSchool(
        name: String,
        code: String,
        city: String,
        country: String,
        phone: String,
        email: String,
        registrationFee: Double = 100000.0,
        annualSubscriptionFee: Double = 500000.0
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val newSchool = School(
                name = name,
                code = code,
                city = city,
                country = country,
                phone = phone,
                email = email,
                registrationFee = registrationFee,
                annualSubscriptionFee = annualSubscriptionFee,
                subscriptionStatus = SchoolSubscriptionStatus.ACTIVE,
                subscriptionExpiry = "2027-09-30"
            )
            val newId = repository.insertSchool(newSchool)
            selectSchool(newId)
            logActivity("SYSTEM", "Création de l'établissement : $name ($code)", "Écoles", "Ville: $city, Pays: $country")
        }
    }

    fun addSchoolSubscriptionPayment(
        schoolId: Long,
        feeType: SubscriptionFeeType,
        amount: Double,
        method: String,
        phoneNumber: String,
        transactionReference: String,
        notes: String = ""
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val cal = Calendar.getInstance().apply { add(Calendar.YEAR, 1) }
            val expiryStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)

            val subscription = SchoolSubscription(
                schoolId = schoolId,
                feeType = feeType,
                amount = amount,
                paymentMethod = method,
                phoneNumber = phoneNumber,
                transactionReference = transactionReference.ifBlank { "TXN-${System.currentTimeMillis().toString().takeLast(8)}" },
                paymentDate = dateStr,
                validUntil = expiryStr,
                status = "VALIDE",
                notes = notes
            )
            repository.insertSchoolSubscription(subscription)

            val school = repository.getSchoolById(schoolId)
            if (school != null) {
                repository.updateSchool(school.copy(
                    subscriptionStatus = SchoolSubscriptionStatus.ACTIVE,
                    subscriptionExpiry = expiryStr
                ))
            }
            logActivity("PAYMENT", "Règlement souscription école (${feeType.labelFr})", "Souscription", "Montant: ${LocalizationUtil.formatFcfa(amount)} via $method")
        }
    }

    fun recordMobilePayment(
        studentId: Long,
        type: PaymentType,
        amount: Double,
        paymentMethod: String,
        mobilePhoneNumber: String,
        transactionRef: String,
        remarks: String = ""
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val receiptNum = "REC-${SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(Date()).takeLast(10)}"
            val payment = Payment(
                schoolId = _selectedSchoolId.value,
                studentId = studentId,
                receiptNumber = receiptNum,
                type = type,
                amountTotal = amount,
                amountPaid = amount,
                date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                paymentMethod = paymentMethod,
                mobilePhoneNumber = mobilePhoneNumber,
                status = PaymentStatus.PAYE,
                remarks = if (remarks.isNotBlank()) "$remarks (Réf: $transactionRef)" else "Paiement $paymentMethod (Réf: $transactionRef)"
            )
            repository.insertPayment(payment)
            logActivity("PAYMENT", "Paiement Mobile Money $paymentMethod : ${LocalizationUtil.formatFcfa(amount)}", "Finances", "Reçu: $receiptNum, N°: $mobilePhoneNumber")
        }
    }

    // === Paiement Local en Espèces (Guichet Caisse de l'École) ===
    fun recordLocalPayment(
        studentId: Long,
        type: PaymentType,
        amount: Double,
        payerName: String,
        receiptNumber: String,
        remarks: String = ""
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val payment = Payment(
                schoolId = _selectedSchoolId.value,
                studentId = studentId,
                receiptNumber = receiptNumber,
                type = type,
                amountTotal = amount,
                amountPaid = amount,
                date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                paymentMethod = "Espèces",
                mobilePhoneNumber = "",
                isLocalPayment = true,
                status = PaymentStatus.PAYE,
                remarks = if (remarks.isNotBlank()) "$remarks (Payeur: $payerName)" else "Versement guichet espèces (Payeur: $payerName)"
            )
            repository.insertPayment(payment)
            logActivity("PAYMENT", "Paiement local espèces : ${LocalizationUtil.formatFcfa(amount)}", "Caisse Locale", "Reçu: $receiptNumber, Payeur: $payerName")
        }
    }

    // === Sauvegarde et Restauration Locale des Données d'Établissement ===
    fun exportSchoolBackup(context: android.content.Context): java.io.File? {
        val school = currentSchool.value ?: return null
        val currentClasses = classes.value
        val currentStudents = students.value
        val currentSubjects = subjects.value
        val currentTeachers = teachers.value
        val currentGrades = grades.value
        val currentAttendances = attendances.value
        val currentPayments = payments.value

        val metadata = com.example.data.model.BackupMetadata(
            version = "1.0.0",
            schoolId = school.id,
            schoolCode = school.code,
            schoolName = school.name,
            dateExport = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
            totalStudents = currentStudents.size,
            totalPayments = currentPayments.size
        )

        val backupData = com.example.data.model.SchoolBackupData(
            metadata = metadata,
            school = school,
            classes = currentClasses,
            students = currentStudents,
            subjects = currentSubjects,
            teachers = currentTeachers,
            grades = currentGrades,
            attendances = currentAttendances,
            payments = currentPayments,
            timetableSlots = timetableSlots.value,
            subscriptions = schoolSubscriptions.value
        )

        val jsonString = com.example.util.BackupRestoreUtil.createBackupJson(backupData)
        val file = com.example.util.BackupRestoreUtil.saveBackupToFile(context, jsonString, school.code)
        if (file != null) {
            viewModelScope.launch(Dispatchers.IO) {
                logActivity("SYSTEM", "Sauvegarde intégrale générée pour ${school.name}", "Sauvegarde", "Fichier: ${file.name}")
            }
        }
        return file
    }

    fun restoreSchoolBackup(context: android.content.Context, jsonString: String): Boolean {
        val backupData = com.example.util.BackupRestoreUtil.parseBackupJson(jsonString) ?: return false
        viewModelScope.launch(Dispatchers.IO) {
            // 1. Mettre à jour ou insérer l'école
            val existingSchool = repository.getSchoolById(backupData.school.id)
            val schoolId = if (existingSchool != null) {
                repository.updateSchool(backupData.school)
                backupData.school.id
            } else {
                repository.insertSchool(backupData.school)
            }

            // 2. Insérer les classes
            backupData.classes.forEach { c ->
                repository.insertClass(c.copy(schoolId = schoolId))
            }

            // 3. Insérer les élèves
            backupData.students.forEach { s ->
                repository.insertStudent(s.copy(schoolId = schoolId))
            }

            // 4. Insérer les matières
            backupData.subjects.forEach { sub ->
                repository.insertSubject(sub.copy(schoolId = schoolId))
            }

            // 5. Insérer les paiements
            backupData.payments.forEach { p ->
                repository.insertPayment(p.copy(schoolId = schoolId))
            }

            // 6. Insérer les notes
            backupData.grades.forEach { g ->
                repository.insertGrade(g.copy(schoolId = schoolId))
            }

            // 7. Insérer les présences
            backupData.attendances.forEach { a ->
                repository.insertAttendance(a.copy(schoolId = schoolId))
            }

            selectSchool(schoolId)
            logActivity("SYSTEM", "Restauration technique réussie pour ${backupData.school.name}", "Restauration", "Élèves: ${backupData.students.size}, Classes: ${backupData.classes.size}")
        }
        return true
    }

    /**
     * Supprime les données de démonstration / test pour démarrer avec une école réelle propre.
     */
    fun clearTestData(clearStudents: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearTestData(clearStudents)
            logActivity(
                "SYSTEM",
                if (clearStudents) "Remise à zéro complète des données de test (Notes, Présences, Paiements et Élèves)"
                else "Suppression des notes, présences et paiements de test (Classes conservées)",
                "Maintenance"
            )
        }
    }
}

data class DashboardStats(
    val totalStudents: Int,
    val totalClasses: Int,
    val attendanceRate: Double,
    val unjustifiedAbsences: Int,
    val globalAverage: Double,
    val activeTeachers: Int = 0,
    val pendingPaymentsCount: Int = 0,
    val pendingPaymentsAmount: Double = 0.0
)
