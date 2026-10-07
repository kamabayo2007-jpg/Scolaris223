package com.example.ui.screens.dashboard

import com.example.util.LocalizationUtil

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Grading
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Attendance
import com.example.data.model.AttendanceStatus
import com.example.data.model.Grade
import com.example.data.model.Payment
import com.example.data.model.PaymentStatus
import com.example.data.model.SchoolClass
import com.example.data.model.Student
import com.example.data.model.Subject
import com.example.data.model.TimetableSlot
import com.example.data.model.UserRole
import com.example.ui.components.AttendanceStatusChip
import com.example.ui.components.GradeBadge
import com.example.ui.components.StatCard
import com.example.ui.components.StudentAvatar
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusDangerContainer
import com.example.ui.theme.StatusDangerText
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.viewmodel.SchoolViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: SchoolViewModel,
    onNavigateToAttendance: () -> Unit,
    onNavigateToGrades: () -> Unit,
    onNavigateToTimetable: () -> Unit,
    onNavigateToStudents: () -> Unit = {},
    onNavigateToFinance: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val stats by viewModel.globalStats.collectAsStateWithLifecycle()
    val classes by viewModel.classes.collectAsStateWithLifecycle()
    val students by viewModel.students.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val grades by viewModel.grades.collectAsStateWithLifecycle()
    val attendances by viewModel.attendances.collectAsStateWithLifecycle()
    val timetableSlots by viewModel.timetableSlots.collectAsStateWithLifecycle()
    val payments by viewModel.payments.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val selectedAcademicYear by viewModel.selectedAcademicYear.collectAsStateWithLifecycle()
    val selectedChildId by viewModel.selectedChildStudentId.collectAsStateWithLifecycle()
    val activityLogs by viewModel.activityLogs.collectAsStateWithLifecycle()
    val currentSchool by viewModel.currentSchool.collectAsStateWithLifecycle()
    val currentLang by viewModel.currentLanguage.collectAsStateWithLifecycle()

    val studentsMap = students.associateBy { it.id }
    val subjectsMap = subjects.associateBy { it.id }
    val classesMap = classes.associateBy { it.id }

    val todayCalendar = Calendar.getInstance()
    val currentDayOfWeek = when (todayCalendar.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> 1
        Calendar.TUESDAY -> 2
        Calendar.WEDNESDAY -> 3
        Calendar.THURSDAY -> 4
        Calendar.FRIDAY -> 5
        Calendar.SATURDAY -> 6
        else -> 1
    }
    val todaySlots = timetableSlots
        .filter { it.dayOfWeek == currentDayOfWeek }
        .sortedBy { it.startTime }

    val recentGrades = grades.take(5)
    val todayDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val todayAbsences = attendances.filter {
        it.date == todayDateStr && (it.status == AttendanceStatus.ABSENT_UNJUSTIFIED || it.status == AttendanceStatus.ABSENT_JUSTIFIED || it.status == AttendanceStatus.LATE)
    }

    // Role-specific student selection (for Eleve or Parent)
    val activeStudent = when (currentUser.role) {
        UserRole.ELEVE -> students.find { it.id == currentUser.linkedStudentId } ?: students.firstOrNull()
        UserRole.PARENT -> students.find { it.id == (selectedChildId ?: currentUser.linkedStudentId) } ?: students.firstOrNull()
        else -> null
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Multi-School & Subscription Executive Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp)
                    .testTag("dashboard_school_banner"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = currentSchool?.name ?: "Groupe Scolaire Excellence Cocody",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${currentSchool?.code ?: "GSE-ABJ"} • ${currentSchool?.city ?: "Abidjan"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF16A34A).copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "Souscription Active",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFF16A34A),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = { viewModel.setSubscriptionDialogOpen(true) },
                            modifier = Modifier.testTag("btn_open_subscription_dialog")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payment,
                                contentDescription = "Souscription",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = { viewModel.setSchoolManagementDialogOpen(true) },
                            modifier = Modifier.testTag("btn_open_schools_dialog")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = "Établissements",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = { viewModel.setSchoolSettingsDialogOpen(true) },
                            modifier = Modifier.testTag("btn_open_school_settings")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Paramètres École",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Active Role Banner & Switcher
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(currentUser.role.badgeColorHex)))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Connecté : ${currentUser.fullName} (${currentUser.role.label})",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    OutlinedButton(
                        onClick = { viewModel.setAuthRoleDialogOpen(true) },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("btn_switch_role_top")
                    ) {
                        Icon(Icons.Default.SwitchAccount, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Changer", fontSize = 12.sp)
                    }
                }
            }
        }

        // Multi-child selector for PARENT role
        if (currentUser.role == UserRole.PARENT) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "Sélection de l'enfant (Accès Prioritaire) :",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val children = viewModel.getParentChildren(currentUser)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        children.forEach { child ->
                            val isSelected = child.id == (activeStudent?.id ?: 1L)
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.selectChildStudent(child.id) },
                                label = { Text("${child.fullName} (${classesMap[child.classId]?.name ?: ""})") },
                                modifier = Modifier.testTag("chip_child_${child.id}")
                            )
                        }
                    }
                }
            }
        }

        // Hero Header
        item {
            val heroTitle = when (currentUser.role) {
                UserRole.SUPER_ADMIN -> "Super Administrateur — Contrôle Global Écoles"
                UserRole.ADMIN -> "ERP Gestion École — Administration"
                UserRole.DIRECTION -> "Direction Pédagogique & Vie Scolaire"
                UserRole.PROFESSEUR -> "Espace Enseignant • ${currentUser.fullName}"
                UserRole.ELEVE -> "Mon Espace Élève • ${currentUser.fullName}"
                UserRole.PARENT -> "Espace Famille • Suivi Scolaire"
                UserRole.COMPTABLE -> "Comptabilité & Gestion Financière"
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("dashboard_hero_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = heroTitle,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                text = "Année Scolaire $selectedAcademicYear • ${SimpleDateFormat("EEEE d MMMM yyyy", Locale.FRENCH).format(Date()).replaceFirstChar { it.uppercase() }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Role Quick Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (currentUser.role == UserRole.SUPER_ADMIN) {
                            Button(
                                onClick = { viewModel.setSchoolManagementDialogOpen(true) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MaterialTheme.colorScheme.onSecondary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1.2f).testTag("btn_super_schools"),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.Business, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Écoles", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { viewModel.setSubscriptionDialogOpen(true) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF16A34A),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1.3f).testTag("btn_super_subscriptions"),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Souscriptions", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { viewModel.setMobilePaymentDialogOpen(true) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFF7900),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1.2f).testTag("btn_super_mobile_money"),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.PhoneAndroid, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Mobile Money", fontSize = 11.sp)
                            }
                        } else if (currentUser.role == UserRole.ADMIN || currentUser.role == UserRole.DIRECTION) {
                            Button(
                                onClick = { viewModel.setRollCallDialogOpen(true) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MaterialTheme.colorScheme.onSecondary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).testTag("btn_quick_roll_call"),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.HowToReg, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Appel", fontSize = 11.sp)
                            }

                            Button(
                                onClick = { viewModel.setTeacherGradeEntryOpen(true) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.onPrimary,
                                    contentColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1.2f).testTag("btn_quick_teacher_grades"),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.Grading, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Notes", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { viewModel.setMobilePaymentDialogOpen(true) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFF7900),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1.3f).testTag("btn_quick_mobile_money"),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.PhoneAndroid, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Mobile Money", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { viewModel.setActivityLogDialogOpen(true) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f),
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1.0f).testTag("btn_quick_activity_log"),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Audit", fontSize = 11.sp)
                            }
                        } else if (currentUser.role == UserRole.PROFESSEUR) {
                            Button(
                                onClick = { viewModel.setRollCallDialogOpen(true) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).testTag("btn_prof_roll_call")
                            ) {
                                Icon(Icons.Default.HowToReg, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Appel", fontSize = 12.sp)
                            }
                            Button(
                                onClick = { viewModel.setTeacherGradeEntryOpen(true) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.onPrimary,
                                    contentColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1.2f).testTag("btn_prof_grades")
                            ) {
                                Icon(Icons.Default.Grading, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Notes", fontSize = 12.sp)
                            }
                            Button(
                                onClick = { viewModel.setTeacherPayrollDialogOpen(true) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f),
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1.1f).testTag("btn_prof_payroll")
                            ) {
                                Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Salaires", fontSize = 12.sp)
                            }
                        } else if (currentUser.role == UserRole.ELEVE || currentUser.role == UserRole.PARENT) {
                            activeStudent?.let { stu ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.openBulletin(stu) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.onPrimary,
                                            contentColor = MaterialTheme.colorScheme.primary
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f).testTag("btn_open_student_bulletin")
                                    ) {
                                        Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Bulletin", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }

                                    Button(
                                        onClick = { viewModel.openStudentCard(stu) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF10B981),
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f).testTag("btn_open_student_card_dash")
                                    ) {
                                        Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Carte Scolaire", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        } else if (currentUser.role == UserRole.COMPTABLE) {
                            Button(
                                onClick = { viewModel.setAddPaymentDialogOpen(true) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.onPrimary,
                                    contentColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1.2f).testTag("btn_compta_payment")
                            ) {
                                Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Enregistrer Versement", fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { viewModel.setNotificationsDialogOpen(true) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f),
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Relances")
                            }
                        }
                    }
                }
            }
        }

        // Student / Parent Personalized Overview Card
        if ((currentUser.role == UserRole.ELEVE || currentUser.role == UserRole.PARENT) && activeStudent != null) {
            val studentGrades = grades.filter { it.studentId == activeStudent.id }
            val studentAvg = if (studentGrades.isNotEmpty()) {
                studentGrades.sumOf { it.normalizedTo20 * it.coefficient } / studentGrades.sumOf { it.coefficient }
            } else 16.5

            val studentAbs = attendances.filter { it.studentId == activeStudent.id }
            val studentPayments = payments.filter { it.studentId == activeStudent.id }
            val remainingTuition = studentPayments.sumOf { it.remainingAmount }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Performance & Situation de ${activeStudent.fullName}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = "Moyenne Générale",
                            value = "${String.format(Locale.US, "%.2f", studentAvg)} / 20",
                            subtitle = "Classement : 1er / 24",
                            icon = Icons.Default.Assignment,
                            accentColor = Color(0xFF047857),
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToGrades
                        )

                        StatCard(
                            title = "Assiduité",
                            value = "${studentAbs.count { it.status == AttendanceStatus.PRESENT }} présences",
                            subtitle = "${studentAbs.count { it.status != AttendanceStatus.PRESENT }} absences/retard",
                            icon = Icons.Default.CheckCircle,
                            accentColor = Color(0xFF1D4ED8),
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToAttendance
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = "Scolarité Trimestre 1",
                            value = if (remainingTuition == 0.0) "Acquittée" else "${remainingTuition} € Dû",
                            subtitle = "${studentPayments.size} règlements enregistrés",
                            icon = Icons.Default.Receipt,
                            accentColor = if (remainingTuition == 0.0) Color(0xFF047857) else Color(0xFFDC2626),
                            modifier = Modifier.weight(1f),
                            onClick = {
                                val firstPayment = studentPayments.firstOrNull()
                                if (firstPayment != null) viewModel.showPaymentReceipt(firstPayment)
                            }
                        )

                        StatCard(
                            title = "Emploi du temps",
                            value = "${todaySlots.size} cours aujourd'hui",
                            subtitle = "Prochain : ${todaySlots.firstOrNull()?.startTime ?: "08:00"}",
                            icon = Icons.Default.EventNote,
                            accentColor = Color(0xFF7C3AED),
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToTimetable
                        )
                    }
                }
            }
        }

        // Comptable Specialized Overview
        if (currentUser.role == UserRole.COMPTABLE) {
            val totalCollected = payments.sumOf { it.amountPaid }
            val totalDue = payments.sumOf { it.remainingAmount }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Synthèse Comptable de l'Établissement",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = "Encaissé à ce jour",
                            value = LocalizationUtil.formatFcfa(totalCollected),
                            subtitle = "${payments.count { it.status == PaymentStatus.PAYE }} reçus acquittés",
                            icon = Icons.Default.CheckCircle,
                            accentColor = Color(0xFF047857),
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Impayés & Restes",
                            value = LocalizationUtil.formatFcfa(totalDue),
                            subtitle = "Relances en cours",
                            icon = Icons.Default.NotificationsActive,
                            accentColor = Color(0xFFDC2626),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Global Key Stats Grid with Total Students, Active Teachers, Pending Payments (For Super Admin, Admin, Direction, Professeur)
        if (currentUser.role == UserRole.SUPER_ADMIN || currentUser.role == UserRole.ADMIN || currentUser.role == UserRole.DIRECTION || currentUser.role == UserRole.PROFESSEUR) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    DashboardOverviewMetrics(
                        stats = stats,
                        onNavigateToStudents = onNavigateToStudents,
                        onNavigateToTeachers = onNavigateToTimetable,
                        onNavigateToPayments = onNavigateToFinance,
                        onNavigateToAttendance = onNavigateToAttendance,
                        onNavigateToGrades = onNavigateToGrades,
                        language = currentLang
                    )
                }
            }
        }

        // Section Journal d'Activité (Traçabilité & Modifications pour Administrateurs & Direction)
        if (currentUser.role == UserRole.SUPER_ADMIN || currentUser.role == UserRole.ADMIN || currentUser.role == UserRole.DIRECTION) {
            val recentLogs = activityLogs.take(4)

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("section_activity_logs_dashboard"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFDC2626).copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        tint = Color(0xFFDC2626),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Journal d'Activité & Audit",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "Dernières modifications du système scolaire",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Text(
                                text = "Consulter tout (${activityLogs.size})",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier
                                    .clickable { viewModel.setActivityLogDialogOpen(true) }
                                    .padding(4.dp)
                                    .testTag("btn_see_all_activity_logs")
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (recentLogs.isEmpty()) {
                            Text(
                                text = "Aucune modification récente enregistrée.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                recentLogs.forEach { log ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { viewModel.setActivityLogDialogOpen(true) }
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    when (log.actionType) {
                                                        "GRADE" -> Color(0xFF2563EB)
                                                        "ATTENDANCE" -> Color(0xFF059669)
                                                        "PAYMENT" -> Color(0xFFD97706)
                                                        "STUDENT" -> Color(0xFF7C3AED)
                                                        else -> Color(0xFFDC2626)
                                                    }
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = log.description,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${log.userFullName} (${log.userRole}) • ${log.timestamp}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Today's Timetable Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cours du jour (Aujourd'hui)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "Voir planning",
                        style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .clickable { onNavigateToTimetable() }
                            .padding(4.dp)
                    )
                }

                if (todaySlots.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            text = "Aucun cours programmé aujourd'hui.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(todaySlots, key = { it.id }) { slot ->
                            val subject = subjectsMap[slot.subjectId]
                            val schoolClass = classesMap[slot.classId]
                            Card(
                                modifier = Modifier
                                    .width(220.dp)
                                    .testTag("dashboard_slot_${slot.id}"),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer
                                        ) {
                                            Text(
                                                text = "${slot.startTime} - ${slot.endTime}",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Text(
                                            text = slot.room,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = subject?.name ?: "Matière inconnue",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = schoolClass?.name ?: "Classe",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.secondary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = slot.teacherName,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Today's Absence Alerts
        if (todayAbsences.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = StatusDanger,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Absences & Retards signalés aujourd'hui",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            todayAbsences.forEach { attendance ->
                                val student = studentsMap[attendance.studentId]
                                val studentClass = student?.let { classesMap[it.classId] }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (attendance.status == AttendanceStatus.ABSENT_UNJUSTIFIED) {
                                                viewModel.openJustificationDialog(attendance)
                                            }
                                        }
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        StudentAvatar(
                                            name = student?.fullName ?: "Élève",
                                            colorHex = student?.avatarColorHex ?: "#1E3A8A",
                                            size = 36
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = student?.fullName ?: "Élève inconnu",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                            )
                                            Text(
                                                text = "${studentClass?.name ?: ""} • ${attendance.timeSlot}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    AttendanceStatusChip(status = attendance.status, compact = true)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recent Grades
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.EventNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Dernières notes enregistrées",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "Toutes les notes",
                        style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .clickable { onNavigateToGrades() }
                            .padding(4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        recentGrades.forEach { grade ->
                            val student = studentsMap[grade.studentId]
                            val subject = subjectsMap[grade.subjectId]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    StudentAvatar(
                                        name = student?.fullName ?: "Élève",
                                        colorHex = student?.avatarColorHex ?: "#1E3A8A",
                                        size = 38
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = student?.fullName ?: "Élève inconnu",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                        )
                                        Text(
                                            text = "${subject?.name ?: "Matière"} • ${grade.title}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                GradeBadge(gradeValue = grade.gradeValue, outOf = grade.outOf)
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
