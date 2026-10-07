package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Settings
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.ui.dialogs.ActivityLogDialog
import com.example.util.AppLanguage
import com.example.util.LocalizationUtil
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.UserRole
import com.example.ui.dialogs.AddClassDialog
import com.example.ui.dialogs.AddGradeDialog
import com.example.ui.dialogs.AddPaymentDialog
import com.example.ui.dialogs.AddStudentDialog
import com.example.ui.dialogs.AddSubjectDialog
import com.example.ui.dialogs.AddTimetableDialog
import com.example.ui.dialogs.AuthRoleDialog
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.filled.Close
import com.example.ui.dialogs.RealTimeSearchResultsView
import com.example.ui.dialogs.SearchCategoryFilter
import com.example.ui.dialogs.GlobalSearchDialog
import com.example.ui.dialogs.JustifyAbsenceDialog
import com.example.ui.dialogs.NotificationDialog
import com.example.ui.dialogs.PaymentReceiptDialog
import com.example.ui.dialogs.RollCallDialog
import com.example.ui.dialogs.StudentBulletinDialog
import com.example.ui.dialogs.StudentDetailDialog
import com.example.ui.screens.attendance.AttendanceScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.finance.FinanceScreen
import com.example.ui.screens.grades.GradesScreen
import com.example.ui.screens.grades.TeacherGradeEntryScreen
import com.example.ui.screens.students.StudentsScreen
import com.example.ui.screens.timetable.TimetableScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.SchoolViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: SchoolViewModel = viewModel()
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
            MyApplicationTheme(darkTheme = isDarkMode) {
                EduGestionApp(viewModel = viewModel)
            }
        }
    }
}

enum class NavigationScreen(val title: String, val icon: ImageVector, val tag: String) {
    DASHBOARD("Accueil", Icons.Default.Dashboard, "nav_dashboard"),
    GRADES("Notes", Icons.Default.Assignment, "nav_grades"),
    ATTENDANCE("Absences", Icons.Default.HowToReg, "nav_attendance"),
    TIMETABLE("Planning", Icons.Default.CalendarMonth, "nav_timetable"),
    STUDENTS("Élèves", Icons.Default.Group, "nav_students"),
    FINANCE("Finances", Icons.Default.Payment, "nav_finance")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EduGestionApp(
    viewModel: SchoolViewModel = viewModel()
) {
    var currentScreen by remember { mutableIntStateOf(0) }
    var isGlobalSearchOpen by remember { mutableStateOf(false) }
    var topbarSearchQuery by remember { mutableStateOf("") }
    var searchCategoryFilter by remember { mutableStateOf(SearchCategoryFilter.ALL) }

    // If searching, pressing back clears the search; otherwise returns to Dashboard if on sub-screen
    BackHandler(enabled = topbarSearchQuery.isNotBlank() || currentScreen != 0) {
        if (topbarSearchQuery.isNotBlank()) {
            topbarSearchQuery = ""
        } else {
            currentScreen = 0
        }
    }

    // Modal states
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val unreadNotifs = notifications.count { !it.isRead && (it.targetRole == "ALL" || it.targetRole == currentUser.role.name) }

    val showAuthRoleDialog by viewModel.showAuthRoleDialog.collectAsStateWithLifecycle()
    val showAddPaymentDialog by viewModel.showAddPaymentDialog.collectAsStateWithLifecycle()
    val selectedPaymentReceipt by viewModel.selectedPaymentReceipt.collectAsStateWithLifecycle()
    val showNotificationsDialog by viewModel.showNotificationsDialog.collectAsStateWithLifecycle()
    val showAddClassDialog by viewModel.showAddClassDialog.collectAsStateWithLifecycle()
    val showAddSubjectDialog by viewModel.showAddSubjectDialog.collectAsStateWithLifecycle()

    val showAddGradeDialog by viewModel.showAddGradeDialog.collectAsStateWithLifecycle()
    val showTeacherGradeEntry by viewModel.showTeacherGradeEntry.collectAsStateWithLifecycle()
    val showAddStudentDialog by viewModel.showAddStudentDialog.collectAsStateWithLifecycle()
    val showAddTimetableDialog by viewModel.showAddTimetableDialog.collectAsStateWithLifecycle()
    val showRollCallDialog by viewModel.showRollCallDialog.collectAsStateWithLifecycle()
    val justificationAttendance by viewModel.justificationAttendance.collectAsStateWithLifecycle()
    val bulletinStudent by viewModel.bulletinStudent.collectAsStateWithLifecycle()
    val selectedStudentDetail by viewModel.selectedStudentDetail.collectAsStateWithLifecycle()

    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val currentSchool by viewModel.currentSchool.collectAsStateWithLifecycle()
    val showSchoolManagementDialog by viewModel.showSchoolManagementDialog.collectAsStateWithLifecycle()
    val showSubscriptionDialog by viewModel.showSubscriptionDialog.collectAsStateWithLifecycle()
    val showMobilePaymentDialog by viewModel.showMobilePaymentDialog.collectAsStateWithLifecycle()
    val showLocalPaymentDialog by viewModel.showLocalPaymentDialog.collectAsStateWithLifecycle()
    val showBackupRestoreDialog by viewModel.showBackupRestoreDialog.collectAsStateWithLifecycle()
    val showSchoolSettingsDialog by viewModel.showSchoolSettingsDialog.collectAsStateWithLifecycle()
    val showTeacherPayrollDialog by viewModel.showTeacherPayrollDialog.collectAsStateWithLifecycle()
    val showStudentCardDialog by viewModel.showStudentCardDialog.collectAsStateWithLifecycle()
    val selectedStudentForCard by viewModel.selectedStudentForCard.collectAsStateWithLifecycle()

    val screens = NavigationScreen.values()
    val activeScreen = screens[currentScreen]

    val roleBadgeColor = Color(android.graphics.Color.parseColor(currentUser.role.badgeColorHex))

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 3.dp,
                modifier = Modifier.testTag("app_top_bar")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    // Header Row: App branding, Role badge, Notifications & Profile actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Role Badge & App Title
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .clickable { viewModel.setAuthRoleDialogOpen(true) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("badge_role_selector")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .clip(CircleShape)
                                    .background(roleBadgeColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(1f, fill = false)) {
                                Text(
                                    text = currentSchool?.name ?: LocalizationUtil.t("app_title", currentLanguage),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${currentUser.role.label} • ${currentSchool?.code ?: "GSE-ABJ"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                                )
                            }
                        }

                        // Actions: Language Toggle, Schools, Notifications & Profile
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (topbarSearchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { topbarSearchQuery = "" },
                                    modifier = Modifier.testTag("btn_top_clear_query")
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Effacer la recherche",
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }

                            // Language Switcher (FR / EN)
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier
                                    .clickable { viewModel.toggleLanguage() }
                                    .padding(horizontal = 4.dp)
                                    .testTag("btn_language_toggle")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${currentLanguage.flagEmoji} ${currentLanguage.name}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }

                            // Super Admin Multi-School Switcher
                            IconButton(
                                onClick = { viewModel.setSchoolManagementDialogOpen(true) },
                                modifier = Modifier.testTag("btn_top_schools_mgmt")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = "Établissements Scolaires",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }

                            // Paramètres de l'établissement (Scolarité / Direction)
                            IconButton(
                                onClick = { viewModel.setSchoolSettingsDialogOpen(true) },
                                modifier = Modifier.testTag("btn_top_school_settings")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Paramètres de l'école",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }

                            // Bouton Sauvegarde & Restauration Locale des données
                            IconButton(
                                onClick = { viewModel.setBackupRestoreDialogOpen(true) },
                                modifier = Modifier.testTag("btn_top_backup_restore")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Backup,
                                    contentDescription = "Sauvegarde & Restauration",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }

                            // Bouton Exporter PDF dans la TopBar pour les vues de liste (Élèves, Professeurs, Notes)
                            if (activeScreen == NavigationScreen.STUDENTS || activeScreen == NavigationScreen.GRADES || activeScreen == NavigationScreen.TIMETABLE) {
                                val context = LocalContext.current
                                IconButton(
                                    onClick = {
                                        when (activeScreen) {
                                            NavigationScreen.STUDENTS -> {
                                                val overviews = viewModel.studentsOverview.value
                                                val currentClass = viewModel.classes.value.find { it.id == viewModel.selectedClassId.value }
                                                val file = com.example.util.PdfExportUtil.exportStudentsListPdf(
                                                    context = context,
                                                    students = overviews,
                                                    className = currentClass?.name
                                                )
                                                if (file != null) {
                                                    com.example.util.PdfExportUtil.shareOrViewPdf(context, file, "Liste Officielle des Élèves")
                                                } else {
                                                    Toast.makeText(context, "Erreur génération PDF Élèves", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                            NavigationScreen.GRADES -> {
                                                val gradesList = viewModel.grades.value
                                                val stuMap = viewModel.students.value.associateBy { it.id }
                                                val subMap = viewModel.subjects.value.associateBy { it.id }
                                                val clsMap = viewModel.classes.value.associateBy { it.id }
                                                val file = com.example.util.PdfExportUtil.exportGradesListPdf(
                                                    context = context,
                                                    grades = gradesList,
                                                    students = stuMap,
                                                    subjects = subMap,
                                                    classes = clsMap,
                                                    period = viewModel.selectedPeriod.value
                                                )
                                                if (file != null) {
                                                    com.example.util.PdfExportUtil.shareOrViewPdf(context, file, "Relevé Général des Notes")
                                                } else {
                                                    Toast.makeText(context, "Erreur génération PDF Notes", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                            NavigationScreen.TIMETABLE -> {
                                                val teachersList = viewModel.teachers.value
                                                val subList = viewModel.subjects.value
                                                val file = com.example.util.PdfExportUtil.exportTeachersListPdf(
                                                    context = context,
                                                    teachers = teachersList,
                                                    subjects = subList
                                                )
                                                if (file != null) {
                                                    com.example.util.PdfExportUtil.shareOrViewPdf(context, file, "Répertoire du Corps Professoral")
                                                } else {
                                                    Toast.makeText(context, "Erreur génération PDF Professeurs", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                            else -> {}
                                        }
                                    },
                                    modifier = Modifier.testTag("btn_topbar_export_pdf")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = "Exporter la liste en PDF",
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }

                            // Theme Toggle Button (Light / Dark Mode)
                            IconButton(
                                onClick = { viewModel.toggleTheme() },
                                modifier = Modifier.testTag("btn_theme_toggle")
                            ) {
                                Icon(
                                    imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = if (isDarkMode) "Passer en mode clair" else "Passer en mode sombre",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }

                            // Notifications Action
                            IconButton(
                                onClick = { viewModel.setNotificationsDialogOpen(true) },
                                modifier = Modifier.testTag("btn_top_notifications")
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (unreadNotifs > 0) {
                                            Badge { Text("$unreadNotifs") }
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Notifications,
                                        contentDescription = "Notifications",
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }

                            // User Profile Action
                            IconButton(
                                onClick = { viewModel.setAuthRoleDialogOpen(true) },
                                modifier = Modifier.testTag("btn_top_user_profile")
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Changer de profil",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Champ de Recherche Globale dans la TopBar
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("topbar_global_search_container"),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Recherche globale",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            BasicTextField(
                                value = topbarSearchQuery,
                                onValueChange = { topbarSearchQuery = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 10.dp)
                                    .testTag("topbar_global_search_input"),
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                decorationBox = { innerTextField ->
                                    if (topbarSearchQuery.isEmpty()) {
                                        Text(
                                            text = "Rechercher élève, professeur, classe...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            maxLines = 1
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                            if (topbarSearchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { topbarSearchQuery = "" },
                                    modifier = Modifier
                                        .size(28.dp)
                                        .testTag("btn_clear_topbar_search")
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Effacer la recherche",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("bottom_nav_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                screens.forEachIndexed { index, screen ->
                    val screenLabel = when (screen) {
                        NavigationScreen.DASHBOARD -> LocalizationUtil.t("nav_dashboard", currentLanguage)
                        NavigationScreen.GRADES -> LocalizationUtil.t("nav_grades", currentLanguage)
                        NavigationScreen.ATTENDANCE -> LocalizationUtil.t("nav_attendance", currentLanguage)
                        NavigationScreen.TIMETABLE -> LocalizationUtil.t("nav_timetable", currentLanguage)
                        NavigationScreen.STUDENTS -> LocalizationUtil.t("nav_students", currentLanguage)
                        NavigationScreen.FINANCE -> LocalizationUtil.t("nav_finance", currentLanguage)
                    }
                    NavigationBarItem(
                        selected = currentScreen == index,
                        onClick = { currentScreen = index },
                        icon = { Icon(screen.icon, contentDescription = screenLabel) },
                        label = { Text(screenLabel, fontSize = 11.sp, fontWeight = if (currentScreen == index) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag(screen.tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (topbarSearchQuery.isNotBlank()) {
                RealTimeSearchResultsView(
                    searchQuery = topbarSearchQuery,
                    selectedFilter = searchCategoryFilter,
                    onFilterChange = { searchCategoryFilter = it },
                    viewModel = viewModel,
                    onSelectStudent = { student ->
                        viewModel.showStudentDetail(student)
                        currentScreen = 4 // Students screen
                        topbarSearchQuery = ""
                    },
                    onSelectClass = { schoolClass ->
                        viewModel.selectClass(schoolClass.id)
                        currentScreen = 4 // Students screen
                        topbarSearchQuery = ""
                    },
                    onSelectTeacher = { _ ->
                        currentScreen = 3 // Timetable screen
                        topbarSearchQuery = ""
                    },
                    onClearSearch = { topbarSearchQuery = "" }
                )
            } else {
                when (activeScreen) {
                    NavigationScreen.DASHBOARD -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToAttendance = { currentScreen = 2 },
                        onNavigateToGrades = { currentScreen = 1 },
                        onNavigateToTimetable = { currentScreen = 3 },
                        onNavigateToStudents = { currentScreen = 4 },
                        onNavigateToFinance = { currentScreen = 5 }
                    )
                    NavigationScreen.GRADES -> GradesScreen(viewModel = viewModel)
                    NavigationScreen.ATTENDANCE -> AttendanceScreen(viewModel = viewModel)
                    NavigationScreen.TIMETABLE -> TimetableScreen(viewModel = viewModel)
                    NavigationScreen.STUDENTS -> StudentsScreen(viewModel = viewModel)
                    NavigationScreen.FINANCE -> FinanceScreen(viewModel = viewModel)
                }
            }
        }

        // Active Dialogs
        if (isGlobalSearchOpen) {
            GlobalSearchDialog(
                viewModel = viewModel,
                onDismiss = { isGlobalSearchOpen = false },
                onSelectStudent = { student ->
                    viewModel.showStudentDetail(student)
                    currentScreen = 4 // Students screen
                    isGlobalSearchOpen = false
                },
                onSelectClass = { schoolClass ->
                    viewModel.selectClass(schoolClass.id)
                    currentScreen = 4 // Students screen filtered by class
                    isGlobalSearchOpen = false
                },
                onSelectTeacher = { _ ->
                    currentScreen = 3 // Timetable screen
                    isGlobalSearchOpen = false
                }
            )
        }

        if (showAuthRoleDialog) {
            AuthRoleDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.setAuthRoleDialogOpen(false) }
            )
        }

        if (showAddPaymentDialog) {
            AddPaymentDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.setAddPaymentDialogOpen(false) }
            )
        }

        selectedPaymentReceipt?.let { payment ->
            PaymentReceiptDialog(
                payment = payment,
                viewModel = viewModel,
                onDismiss = { viewModel.showPaymentReceipt(null) }
            )
        }

        if (showNotificationsDialog) {
            NotificationDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.setNotificationsDialogOpen(false) }
            )
        }

        if (showAddClassDialog) {
            AddClassDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.setAddClassDialogOpen(false) }
            )
        }

        if (showAddSubjectDialog) {
            AddSubjectDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.setAddSubjectDialogOpen(false) }
            )
        }

        if (showTeacherGradeEntry) {
            Dialog(
                onDismissRequest = { viewModel.setTeacherGradeEntryOpen(false) },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TeacherGradeEntryScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.setTeacherGradeEntryOpen(false) }
                    )
                }
            }
        }

        if (showAddGradeDialog) {
            AddGradeDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.setAddGradeDialogOpen(false) }
            )
        }

        if (showAddStudentDialog) {
            AddStudentDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.setAddStudentDialogOpen(false) }
            )
        }

        if (showAddTimetableDialog) {
            AddTimetableDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.setAddTimetableDialogOpen(false) }
            )
        }

        if (showRollCallDialog) {
            RollCallDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.setRollCallDialogOpen(false) }
            )
        }

        justificationAttendance?.let { att ->
            JustifyAbsenceDialog(
                attendance = att,
                viewModel = viewModel,
                onDismiss = { viewModel.openJustificationDialog(null) }
            )
        }

        bulletinStudent?.let { stu ->
            StudentBulletinDialog(
                student = stu,
                viewModel = viewModel,
                onDismiss = { viewModel.openBulletin(null) }
            )
        }

        selectedStudentDetail?.let { stu ->
            StudentDetailDialog(
                student = stu,
                viewModel = viewModel,
                onDismiss = { viewModel.showStudentDetail(null) }
            )
        }

        if (showSchoolManagementDialog) {
            com.example.ui.dialogs.SchoolManagementDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.setSchoolManagementDialogOpen(false) }
            )
        }

        if (showSubscriptionDialog) {
            com.example.ui.dialogs.SchoolSubscriptionDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.setSubscriptionDialogOpen(false) }
            )
        }

        if (showMobilePaymentDialog) {
            com.example.ui.dialogs.MobileMoneyPaymentDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.setMobilePaymentDialogOpen(false) }
            )
        }

        if (showLocalPaymentDialog) {
            com.example.ui.dialogs.LocalPaymentDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.setLocalPaymentDialogOpen(false) }
            )
        }

        if (showBackupRestoreDialog) {
            com.example.ui.dialogs.BackupRestoreDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.setBackupRestoreDialogOpen(false) }
            )
        }

        if (showSchoolSettingsDialog) {
            com.example.ui.dialogs.SchoolSettingsDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.setSchoolSettingsDialogOpen(false) }
            )
        }

        if (showTeacherPayrollDialog) {
            com.example.ui.dialogs.TeacherPayrollDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.setTeacherPayrollDialogOpen(false) }
            )
        }

        if (showStudentCardDialog) {
            com.example.ui.dialogs.StudentCardDialog(
                viewModel = viewModel,
                initialStudent = selectedStudentForCard,
                onDismiss = { viewModel.setStudentCardDialogOpen(false) }
            )
        }
    }
}

