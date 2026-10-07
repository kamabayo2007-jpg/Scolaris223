package com.example.ui.screens.attendance

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Download
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Attendance
import com.example.data.model.AttendanceStatus
import com.example.data.model.SchoolClass
import com.example.data.model.Student
import com.example.ui.components.AttendanceStatusChip
import com.example.ui.components.ClassFilterRow
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StudentAvatar
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusDangerContainer
import com.example.ui.theme.StatusInfo
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.viewmodel.SchoolViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AttendanceScreen(
    viewModel: SchoolViewModel,
    modifier: Modifier = Modifier
) {
    val attendances by viewModel.attendances.collectAsStateWithLifecycle()
    val students by viewModel.students.collectAsStateWithLifecycle()
    val classes by viewModel.classes.collectAsStateWithLifecycle()
    val selectedClassId by viewModel.selectedClassId.collectAsStateWithLifecycle()

    var statusFilter by remember { mutableStateOf<AttendanceStatus?>(null) }

    val studentsMap = remember(students) { students.associateBy { it.id } }
    val classesMap = remember(classes) { classes.associateBy { it.id } }

    val filteredAttendances = remember(attendances, selectedClassId, statusFilter) {
        attendances.filter { record ->
            val matchClass = selectedClassId == null || record.classId == selectedClassId
            val matchStatus = statusFilter == null || record.status == statusFilter
            matchClass && matchStatus
        }
    }

    // Counts
    val totalRecords = filteredAttendances.size
    val presentCount = filteredAttendances.count { it.status == AttendanceStatus.PRESENT }
    val lateCount = filteredAttendances.count { it.status == AttendanceStatus.LATE }
    val unjustifiedCount = filteredAttendances.count { it.status == AttendanceStatus.ABSENT_UNJUSTIFIED }
    val justifiedCount = filteredAttendances.count { it.status == AttendanceStatus.ABSENT_JUSTIFIED }

    Scaffold(
        modifier = modifier.testTag("attendance_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.setRollCallDialogOpen(true) },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.testTag("fab_roll_call")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.HowToReg, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Faire l'appel", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Class Filters
            ClassFilterRow(
                classes = classes,
                selectedClassId = selectedClassId,
                onSelectClass = { viewModel.selectClass(it) },
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Header Action Bar: Title & PDF Export for Direction and Parents
            val context = LocalContext.current
            val selectedClass = classes.find { it.id == selectedClassId }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Suivi & Assiduité",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                OutlinedButton(
                    onClick = {
                        val pdfFile = com.example.util.PdfExportUtil.exportAttendanceReportPdf(
                            context = context,
                            schoolClass = selectedClass,
                            attendances = filteredAttendances,
                            students = students
                        )
                        if (pdfFile != null) {
                            com.example.util.PdfExportUtil.shareOrViewPdf(context, pdfFile, "Rapport d'Assiduité Officiel")
                        } else {
                            Toast.makeText(context, "Erreur génération du rapport PDF", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.testTag("btn_export_attendance_pdf"),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Exporter Rapport PDF", style = MaterialTheme.typography.labelMedium)
                }
            }

            // Summary Stats Pill Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$presentCount",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = StatusSuccess
                        )
                        Text(
                            text = "Présents",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$lateCount",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = StatusWarning
                        )
                        Text(
                            text = "Retards",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$justifiedCount",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = StatusInfo
                        )
                        Text(
                            text = "Justifiées",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$unjustifiedCount",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = StatusDanger
                        )
                        Text(
                            text = "À justifier",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Status Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = statusFilter == null,
                        onClick = { statusFilter = null },
                        label = { Text("Tous les statuts") }
                    )
                }
                item {
                    FilterChip(
                        selected = statusFilter == AttendanceStatus.ABSENT_UNJUSTIFIED,
                        onClick = { statusFilter = AttendanceStatus.ABSENT_UNJUSTIFIED },
                        label = { Text("Non justifiées ($unjustifiedCount)") }
                    )
                }
                item {
                    FilterChip(
                        selected = statusFilter == AttendanceStatus.LATE,
                        onClick = { statusFilter = AttendanceStatus.LATE },
                        label = { Text("Retards ($lateCount)") }
                    )
                }
                item {
                    FilterChip(
                        selected = statusFilter == AttendanceStatus.ABSENT_JUSTIFIED,
                        onClick = { statusFilter = AttendanceStatus.ABSENT_JUSTIFIED },
                        label = { Text("Justifiées") }
                    )
                }
                item {
                    FilterChip(
                        selected = statusFilter == AttendanceStatus.PRESENT,
                        onClick = { statusFilter = AttendanceStatus.PRESENT },
                        label = { Text("Présents") }
                    )
                }
            }

            // Attendance list
            if (filteredAttendances.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.EventBusy,
                    title = "Aucun enregistrement d'appel",
                    description = "Faites l'appel pour une classe en cliquant sur 'Faire l'appel'."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredAttendances, key = { it.id }) { record ->
                        val student = studentsMap[record.studentId]
                        val schoolClass = classesMap[record.classId]

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("attendance_item_${record.id}"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
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
                                            size = 42
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = student?.fullName ?: "Élève inconnu",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                text = "${schoolClass?.name ?: "Classe"} • ${record.date} (${record.timeSlot})",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    AttendanceStatusChip(status = record.status)
                                }

                                if (record.reason.isNotBlank() || record.remarks.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            if (record.reason.isNotBlank()) {
                                                Text(
                                                    text = "Motif : ${record.reason}",
                                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                                                )
                                            }
                                            if (record.remarks.isNotBlank()) {
                                                Text(
                                                    text = "Remarque : ${record.remarks}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }

                                // If non justified absence, show quick action button
                                if (record.status == AttendanceStatus.ABSENT_UNJUSTIFIED) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Button(
                                            onClick = { viewModel.openJustificationDialog(record) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.testTag("btn_justify_${record.id}")
                                        ) {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Justifier cette absence", style = MaterialTheme.typography.labelMedium)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
