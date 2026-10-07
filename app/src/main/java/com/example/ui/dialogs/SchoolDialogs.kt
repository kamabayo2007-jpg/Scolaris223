package com.example.ui.dialogs

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Attendance
import com.example.data.model.AttendanceStatus
import com.example.data.model.SchoolClass
import com.example.data.model.Student
import com.example.data.model.Subject
import com.example.ui.components.AttendanceStatusChip
import com.example.ui.components.GradeBadge
import com.example.ui.components.StudentAvatar
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusDangerContainer
import com.example.ui.theme.StatusDangerText
import com.example.ui.theme.StatusInfo
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusSuccessContainer
import com.example.ui.theme.StatusSuccessText
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.StatusWarningContainer
import com.example.ui.theme.StatusWarningText
import com.example.ui.viewmodel.SchoolViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGradeDialog(
    viewModel: SchoolViewModel,
    onDismiss: () -> Unit
) {
    val classes by viewModel.classes.collectAsStateWithLifecycle()
    val allStudents by viewModel.students.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()

    var selectedClassId by remember(classes) { mutableStateOf(classes.firstOrNull()?.id ?: 0L) }
    val classStudents = remember(allStudents, selectedClassId) {
        allStudents.filter { it.classId == selectedClassId }
    }
    var selectedStudentId by remember(classStudents) { mutableStateOf(classStudents.firstOrNull()?.id ?: 0L) }
    var selectedSubjectId by remember(subjects) { mutableStateOf(subjects.firstOrNull()?.id ?: 0L) }

    var title by remember { mutableStateOf("Contrôle écrit") }
    var gradeValueStr by remember { mutableStateOf("15.0") }
    var outOfStr by remember { mutableStateOf("20") }
    var coefficientStr by remember { mutableStateOf("1.0") }
    var period by remember { mutableStateOf("Trimestre 1") }
    var comment by remember { mutableStateOf("") }
    val todayDateStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    var classExpanded by remember { mutableStateOf(false) }
    var studentExpanded by remember { mutableStateOf(false) }
    var subjectExpanded by remember { mutableStateOf(false) }
    var periodExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Saisir une nouvelle note", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Class Selector
                ExposedDropdownMenuBox(
                    expanded = classExpanded,
                    onExpandedChange = { classExpanded = !classExpanded }
                ) {
                    OutlinedTextField(
                        value = classes.find { it.id == selectedClassId }?.name ?: "Sélectionner classe",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Classe") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = classExpanded,
                        onDismissRequest = { classExpanded = false }
                    ) {
                        classes.forEach { schoolClass ->
                            DropdownMenuItem(
                                text = { Text(schoolClass.name) },
                                onClick = {
                                    selectedClassId = schoolClass.id
                                    classExpanded = false
                                }
                            )
                        }
                    }
                }

                // Student Selector
                ExposedDropdownMenuBox(
                    expanded = studentExpanded,
                    onExpandedChange = { studentExpanded = !studentExpanded }
                ) {
                    OutlinedTextField(
                        value = classStudents.find { it.id == selectedStudentId }?.fullName ?: "Sélectionner élève",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Élève") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = studentExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = studentExpanded,
                        onDismissRequest = { studentExpanded = false }
                    ) {
                        classStudents.forEach { student ->
                            DropdownMenuItem(
                                text = { Text(student.fullName) },
                                onClick = {
                                    selectedStudentId = student.id
                                    studentExpanded = false
                                }
                            )
                        }
                    }
                }

                // Subject Selector
                ExposedDropdownMenuBox(
                    expanded = subjectExpanded,
                    onExpandedChange = { subjectExpanded = !subjectExpanded }
                ) {
                    OutlinedTextField(
                        value = subjects.find { it.id == selectedSubjectId }?.name ?: "Sélectionner matière",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Matière") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = subjectExpanded,
                        onDismissRequest = { subjectExpanded = false }
                    ) {
                        subjects.forEach { subject ->
                            DropdownMenuItem(
                                text = { Text("${subject.name} (Coeff ${subject.coefficient})") },
                                onClick = {
                                    selectedSubjectId = subject.id
                                    coefficientStr = subject.coefficient.toString()
                                    subjectExpanded = false
                                }
                            )
                        }
                    }
                }

                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titre de l'évaluation") },
                    placeholder = { Text("Ex: DS N°2, Interrogation orale...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Grade value & Out of
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = gradeValueStr,
                        onValueChange = { gradeValueStr = it },
                        label = { Text("Note obtenue") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = outOfStr,
                        onValueChange = { outOfStr = it },
                        label = { Text("Sur (/20)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Coefficient
                OutlinedTextField(
                    value = coefficientStr,
                    onValueChange = { coefficientStr = it },
                    label = { Text("Coefficient") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Period Dropdown
                ExposedDropdownMenuBox(
                    expanded = periodExpanded,
                    onExpandedChange = { periodExpanded = !periodExpanded }
                ) {
                    OutlinedTextField(
                        value = period,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Période scolaire") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = periodExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = periodExpanded,
                        onDismissRequest = { periodExpanded = false }
                    ) {
                        listOf("Trimestre 1", "Trimestre 2", "Trimestre 3").forEach { p ->
                            DropdownMenuItem(
                                text = { Text(p) },
                                onClick = {
                                    period = p
                                    periodExpanded = false
                                }
                            )
                        }
                    }
                }

                // Comment
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Commentaire / Appréciation (optionnel)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val gradeVal = gradeValueStr.toDoubleOrNull() ?: 10.0
                    val outOfVal = outOfStr.toDoubleOrNull() ?: 20.0
                    val coeffVal = coefficientStr.toDoubleOrNull() ?: 1.0

                    if (selectedStudentId > 0 && selectedSubjectId > 0) {
                        viewModel.addGrade(
                            studentId = selectedStudentId,
                            subjectId = selectedSubjectId,
                            classId = selectedClassId,
                            title = title,
                            value = gradeVal,
                            outOf = outOfVal,
                            coefficient = coeffVal,
                            period = period,
                            date = todayDateStr,
                            comment = comment
                        )
                    }
                },
                modifier = Modifier.testTag("btn_save_grade")
            ) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStudentDialog(
    viewModel: SchoolViewModel,
    onDismiss: () -> Unit
) {
    val classes by viewModel.classes.collectAsStateWithLifecycle()
    var selectedClassId by remember(classes) { mutableStateOf(classes.firstOrNull()?.id ?: 0L) }
    var classExpanded by remember { mutableStateOf(false) }

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var matricule by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("M") }
    var birthDate by remember { mutableStateOf("15/05/2008") }
    var parentEmail by remember { mutableStateOf("") }
    var parentPhone by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Inscrire un nouvel élève", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Class Selector
                ExposedDropdownMenuBox(
                    expanded = classExpanded,
                    onExpandedChange = { classExpanded = !classExpanded }
                ) {
                    OutlinedTextField(
                        value = classes.find { it.id == selectedClassId }?.name ?: "Sélectionner classe",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Classe") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = classExpanded,
                        onDismissRequest = { classExpanded = false }
                    ) {
                        classes.forEach { schoolClass ->
                            DropdownMenuItem(
                                text = { Text(schoolClass.name) },
                                onClick = {
                                    selectedClassId = schoolClass.id
                                    classExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Nom de famille *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("Prénom *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = matricule,
                    onValueChange = { matricule = it },
                    label = { Text("Matricule / Identifiant (laisser vide pour auto)") },
                    placeholder = { Text("Ex: TS1-010") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Genre :", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    RadioButton(selected = gender == "M", onClick = { gender = "M" })
                    Text("Masculin", modifier = Modifier.clickable { gender = "M" })
                    Spacer(modifier = Modifier.width(12.dp))
                    RadioButton(selected = gender == "F", onClick = { gender = "F" })
                    Text("Féminin", modifier = Modifier.clickable { gender = "F" })
                }

                OutlinedTextField(
                    value = birthDate,
                    onValueChange = { birthDate = it },
                    label = { Text("Date de naissance") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = parentPhone,
                    onValueChange = { parentPhone = it },
                    label = { Text("Téléphone des parents") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = parentEmail,
                    onValueChange = { parentEmail = it },
                    label = { Text("Email des parents") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (firstName.isNotBlank() && lastName.isNotBlank() && selectedClassId > 0) {
                        viewModel.addStudent(
                            classId = selectedClassId,
                            firstName = firstName,
                            lastName = lastName,
                            matricule = matricule,
                            gender = gender,
                            birthDate = birthDate,
                            parentEmail = parentEmail,
                            parentPhone = parentPhone
                        )
                    }
                },
                enabled = firstName.isNotBlank() && lastName.isNotBlank(),
                modifier = Modifier.testTag("btn_save_student")
            ) {
                Text("Inscrire")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTimetableDialog(
    viewModel: SchoolViewModel,
    onDismiss: () -> Unit
) {
    val classes by viewModel.classes.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val teachers by viewModel.teachers.collectAsStateWithLifecycle()

    var selectedClassId by remember(classes) { mutableStateOf(classes.firstOrNull()?.id ?: 0L) }
    var selectedSubjectId by remember(subjects) { mutableStateOf(subjects.firstOrNull()?.id ?: 0L) }
    var selectedDay by remember { mutableIntStateOf(1) } // Lundi

    var selectedTeacher by remember(teachers) { mutableStateOf(teachers.firstOrNull()) }
    var teacherName by remember(selectedTeacher) { mutableStateOf(selectedTeacher?.fullName ?: "Alain Bernard") }
    var startTime by remember { mutableStateOf("08:00") }
    var endTime by remember { mutableStateOf("10:00") }
    var room by remember { mutableStateOf("Salle 302") }

    var classExpanded by remember { mutableStateOf(false) }
    var subjectExpanded by remember { mutableStateOf(false) }
    var teacherExpanded by remember { mutableStateOf(false) }
    var dayExpanded by remember { mutableStateOf(false) }

    var conflictErrorMessage by remember { mutableStateOf<String?>(null) }

    val daysList = listOf(
        1 to "Lundi", 2 to "Mardi", 3 to "Mercredi", 4 to "Jeudi", 5 to "Vendredi", 6 to "Samedi"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Programmer un cours (Scolarité)", fontWeight = FontWeight.Bold)
                Text(
                    "Attribution des enseignants & contrôle anti-collision horaire",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Class Selector
                ExposedDropdownMenuBox(
                    expanded = classExpanded,
                    onExpandedChange = { classExpanded = !classExpanded }
                ) {
                    OutlinedTextField(
                        value = classes.find { it.id == selectedClassId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Classe *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = classExpanded,
                        onDismissRequest = { classExpanded = false }
                    ) {
                        classes.forEach { c ->
                            DropdownMenuItem(
                                text = { Text("${c.name} (${c.level})") },
                                onClick = {
                                    selectedClassId = c.id
                                    classExpanded = false
                                    conflictErrorMessage = null
                                }
                            )
                        }
                    }
                }

                // Teacher Selector (Une classe peut avoir plusieurs profs, mais un prof ne peut pas avoir 2 cours en même temps)
                ExposedDropdownMenuBox(
                    expanded = teacherExpanded,
                    onExpandedChange = { teacherExpanded = !teacherExpanded }
                ) {
                    OutlinedTextField(
                        value = teacherName,
                        onValueChange = { teacherName = it; conflictErrorMessage = null },
                        label = { Text("Professeur attribué *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = teacherExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = teacherExpanded,
                        onDismissRequest = { teacherExpanded = false }
                    ) {
                        teachers.forEach { t ->
                            DropdownMenuItem(
                                text = { Text("${t.fullName} (${t.subjectName})") },
                                onClick = {
                                    selectedTeacher = t
                                    teacherName = t.fullName
                                    teacherExpanded = false
                                    conflictErrorMessage = null
                                }
                            )
                        }
                    }
                }

                // Day Selector
                ExposedDropdownMenuBox(
                    expanded = dayExpanded,
                    onExpandedChange = { dayExpanded = !dayExpanded }
                ) {
                    OutlinedTextField(
                        value = daysList.find { it.first == selectedDay }?.second ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Jour de la semaine") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dayExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = dayExpanded,
                        onDismissRequest = { dayExpanded = false }
                    ) {
                        daysList.forEach { (d, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    selectedDay = d
                                    dayExpanded = false
                                    conflictErrorMessage = null
                                }
                            )
                        }
                    }
                }

                // Subject Selector
                ExposedDropdownMenuBox(
                    expanded = subjectExpanded,
                    onExpandedChange = { subjectExpanded = !subjectExpanded }
                ) {
                    OutlinedTextField(
                        value = subjects.find { it.id == selectedSubjectId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Matière / Cours") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = subjectExpanded,
                        onDismissRequest = { subjectExpanded = false }
                    ) {
                        subjects.forEach { s ->
                            DropdownMenuItem(
                                text = { Text("${s.name} (${s.code})") },
                                onClick = {
                                    selectedSubjectId = s.id
                                    subjectExpanded = false
                                    conflictErrorMessage = null
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it; conflictErrorMessage = null },
                        label = { Text("Début (HH:mm)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it; conflictErrorMessage = null },
                        label = { Text("Fin (HH:mm)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = room,
                    onValueChange = { room = it; conflictErrorMessage = null },
                    label = { Text("Salle de cours") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Conflict error card
                if (conflictErrorMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = conflictErrorMessage ?: "",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedClassId > 0 && selectedSubjectId > 0) {
                        val conflict = viewModel.addTimetableSlot(
                            classId = selectedClassId,
                            subjectId = selectedSubjectId,
                            teacherName = teacherName,
                            teacherId = selectedTeacher?.id,
                            dayOfWeek = selectedDay,
                            startTime = startTime,
                            endTime = endTime,
                            room = room
                        )
                        if (conflict != null) {
                            conflictErrorMessage = conflict
                        }
                    }
                },
                modifier = Modifier.testTag("btn_save_timetable_slot")
            ) {
                Text("Valider & Enregistrer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCalendarEventDialog(
    viewModel: SchoolViewModel,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("TRIMESTRE") }
    var startDate by remember { mutableStateOf("2025-10-24") }
    var endDate by remember { mutableStateOf("2025-11-03") }
    var description by remember { mutableStateOf("") }

    val categories = listOf(
        "TRIMESTRE" to "Trimestre / Rentrée",
        "VACANCES" to "Vacances & Congés",
        "EXAMEN" to "Examens & Évaluations",
        "FERIE" to "Jour Férié",
        "REUNION" to "Conseil / Réunion"
    )
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Ajouter au Calendrier Scolaire", fontWeight = FontWeight.Bold)
                Text("Date clé, vacances, examens ou trimestre", style = MaterialTheme.typography.bodySmall)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Intitulé de l'événement *") },
                    placeholder = { Text("Ex: Vacances de Toussaint, Examens Blancs...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = categories.find { it.first == category }?.second ?: category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Catégorie *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        categories.forEach { (cat, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    category = cat
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("Date de début *") },
                        placeholder = { Text("AAAA-MM-JJ") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { endDate = it },
                        label = { Text("Date de fin") },
                        placeholder = { Text("AAAA-MM-JJ") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description / Précisions") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank() || startDate.isBlank()) {
                        errorMessage = "L'intitulé et la date de début sont obligatoires."
                        return@Button
                    }
                    viewModel.addCalendarEvent(
                        title = title,
                        category = category,
                        startDate = startDate,
                        endDate = endDate,
                        description = description
                    )
                }
            ) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RollCallDialog(
    viewModel: SchoolViewModel,
    onDismiss: () -> Unit
) {
    val classes by viewModel.classes.collectAsStateWithLifecycle()
    val allStudents by viewModel.students.collectAsStateWithLifecycle()

    var selectedClassId by remember(classes) { mutableStateOf(classes.firstOrNull()?.id ?: 0L) }
    val classStudents = remember(allStudents, selectedClassId) {
        allStudents.filter { it.classId == selectedClassId }
    }

    var timeSlot by remember { mutableStateOf("08:00 - 10:00") }
    val todayDateStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    // Map studentId -> AttendanceStatus
    val statusMap = remember(classStudents) {
        mutableStateMapOf<Long, AttendanceStatus>().apply {
            classStudents.forEach { put(it.id, AttendanceStatus.PRESENT) }
        }
    }
    val reasonsMap = remember { mutableStateMapOf<Long, String>() }

    var classExpanded by remember { mutableStateOf(false) }
    var slotExpanded by remember { mutableStateOf(false) }

    val timeSlots = listOf("08:00 - 10:00", "10:15 - 12:15", "13:30 - 15:30", "15:45 - 17:45")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Faire l'appel de la classe",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Date : $todayDateStr",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Selectors row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Class selector
                    ExposedDropdownMenuBox(
                        expanded = classExpanded,
                        onExpandedChange = { classExpanded = !classExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = classes.find { it.id == selectedClassId }?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Classe") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = classExpanded,
                            onDismissRequest = { classExpanded = false }
                        ) {
                            classes.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c.name) },
                                    onClick = {
                                        selectedClassId = c.id
                                        classExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Slot selector
                    ExposedDropdownMenuBox(
                        expanded = slotExpanded,
                        onExpandedChange = { slotExpanded = !slotExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = timeSlot,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Créneau") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = slotExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = slotExpanded,
                            onDismissRequest = { slotExpanded = false }
                        ) {
                            timeSlots.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s) },
                                    onClick = {
                                        timeSlot = s
                                        slotExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick bulk action: All Present
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${classStudents.size} élèves inscrits",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    OutlinedButton(
                        onClick = {
                            classStudents.forEach { statusMap[it.id] = AttendanceStatus.PRESENT }
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tous Présents")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Students Roll Call list
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(classStudents, key = { it.id }) { student ->
                        val currentStatus = statusMap[student.id] ?: AttendanceStatus.PRESENT

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
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
                                            name = student.fullName,
                                            colorHex = student.avatarColorHex,
                                            size = 38
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = student.fullName,
                                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                text = student.matricule,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    // Quick 3-button status toggle
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        // Present
                                        Surface(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { statusMap[student.id] = AttendanceStatus.PRESENT }
                                                .background(if (currentStatus == AttendanceStatus.PRESENT) StatusSuccessContainer else MaterialTheme.colorScheme.surfaceVariant),
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (currentStatus == AttendanceStatus.PRESENT) StatusSuccessContainer else MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Text(
                                                text = "P",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = if (currentStatus == AttendanceStatus.PRESENT) StatusSuccessText else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                            )
                                        }

                                        // Late
                                        Surface(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { statusMap[student.id] = AttendanceStatus.LATE }
                                                .background(if (currentStatus == AttendanceStatus.LATE) StatusWarningContainer else MaterialTheme.colorScheme.surfaceVariant),
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (currentStatus == AttendanceStatus.LATE) StatusWarningContainer else MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Text(
                                                text = "R",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = if (currentStatus == AttendanceStatus.LATE) StatusWarningText else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                            )
                                        }

                                        // Absent
                                        Surface(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { statusMap[student.id] = AttendanceStatus.ABSENT_UNJUSTIFIED }
                                                .background(if (currentStatus == AttendanceStatus.ABSENT_UNJUSTIFIED || currentStatus == AttendanceStatus.ABSENT_JUSTIFIED) StatusDangerContainer else MaterialTheme.colorScheme.surfaceVariant),
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (currentStatus == AttendanceStatus.ABSENT_UNJUSTIFIED || currentStatus == AttendanceStatus.ABSENT_JUSTIFIED) StatusDangerContainer else MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Text(
                                                text = "A",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = if (currentStatus == AttendanceStatus.ABSENT_UNJUSTIFIED || currentStatus == AttendanceStatus.ABSENT_JUSTIFIED) StatusDangerText else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                            )
                                        }
                                    }
                                }

                                if (currentStatus != AttendanceStatus.PRESENT) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = reasonsMap[student.id] ?: "",
                                        onValueChange = { reasonsMap[student.id] = it },
                                        placeholder = {
                                            Text(
                                                if (currentStatus == AttendanceStatus.LATE) "Motif du retard (ex: Panne de bus)"
                                                else "Motif d'absence (ex: Maladie, non justifiée...)"
                                            )
                                        },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom validation button
                Button(
                    onClick = {
                        viewModel.saveRollCall(
                            classId = selectedClassId,
                            date = todayDateStr,
                            timeSlot = timeSlot,
                            records = statusMap,
                            reasons = reasonsMap
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_validate_roll_call"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Valider et enregistrer l'appel", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
fun JustifyAbsenceDialog(
    attendance: Attendance,
    viewModel: SchoolViewModel,
    onDismiss: () -> Unit
) {
    var reason by remember { mutableStateOf("Certificat médical fourni") }
    var remarks by remember { mutableStateOf("") }

    val presetReasons = listOf(
        "Certificat médical / Maladie",
        "Raison familiale impérieuse",
        "Problème de transport",
        "Convocation officielle",
        "Rendez-vous dentaire / spécialisé"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Justifier l'absence", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Date : ${attendance.date} (${attendance.timeSlot})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Sélectionner ou saisir un motif officiel :",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )

                presetReasons.forEach { r ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { reason = r }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = reason == r,
                            onClick = { reason = r }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(r, style = MaterialTheme.typography.bodySmall)
                    }
                }

                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Précisions administratives (optionnel)") },
                    placeholder = { Text("Ex: Reçu par la vie scolaire le 26/09...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.justifyAbsence(attendance, reason, remarks)
                },
                modifier = Modifier.testTag("btn_confirm_justify")
            ) {
                Text("Valider la justification")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
fun StudentBulletinDialog(
    student: Student,
    viewModel: SchoolViewModel,
    onDismiss: () -> Unit
) {
    val classes by viewModel.classes.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val allGrades by viewModel.grades.collectAsStateWithLifecycle()
    val allAttendances by viewModel.attendances.collectAsStateWithLifecycle()

    val studentClass = classes.find { it.id == student.classId }
    val studentGrades = allGrades.filter { it.studentId == student.id }
    val studentAttendances = allAttendances.filter { it.studentId == student.id }

    // Weighted average
    val totalWeighted = studentGrades.sumOf { it.normalizedTo20 * it.coefficient }
    val totalCoeff = studentGrades.sumOf { it.coefficient }
    val generalAverage = if (totalCoeff > 0) totalWeighted / totalCoeff else 0.0

    // Mention
    val mention = when {
        generalAverage >= 16.0 -> "Félicitations du Conseil de classe" to StatusSuccess
        generalAverage >= 14.0 -> "Compliments du Conseil de classe" to StatusInfo
        generalAverage >= 12.0 -> "Encouragements" to MaterialTheme.colorScheme.secondary
        generalAverage >= 10.0 -> "Résultats convenables" to MaterialTheme.colorScheme.onSurface
        else -> "Avertissement travail - Doit redoubler d'efforts" to StatusDanger
    }

    val absencesCount = studentAttendances.count {
        it.status == AttendanceStatus.ABSENT_UNJUSTIFIED || it.status == AttendanceStatus.ABSENT_JUSTIFIED
    }
    val unjustifiedAbsences = studentAttendances.count { it.status == AttendanceStatus.ABSENT_UNJUSTIFIED }
    val latesCount = studentAttendances.count { it.status == AttendanceStatus.LATE }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Bulletin Scolaire Officiel",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Établissement d'Enseignement Secondaire • Trimestre 1",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Student Identity Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = student.fullName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Classe : ${studentClass?.name ?: ""} • Année : ${studentClass?.academicYear ?: "2025-2026"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Prof. Principal : ${studentClass?.mainTeacher ?: "M. Bernard"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Matricule",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = student.matricule,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Grades table
                Text(
                    text = "Détail par discipline :",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(subjects, key = { it.id }) { subject ->
                        val gradesForSubject = studentGrades.filter { it.subjectId == subject.id }
                        val subjectAvg = if (gradesForSubject.isNotEmpty()) {
                            gradesForSubject.sumOf { it.normalizedTo20 * it.coefficient } / gradesForSubject.sumOf { it.coefficient }
                        } else null

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = subject.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "Coefficient : ${subject.coefficient} • ${gradesForSubject.size} note(s)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (gradesForSubject.isNotEmpty()) {
                                        val comments = gradesForSubject.mapNotNull { it.comment.ifBlank { null } }
                                        if (comments.isNotEmpty()) {
                                            Text(
                                                text = "Appréciation : ${comments.last()}",
                                                style = MaterialTheme.typography.labelSmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }

                                if (subjectAvg != null) {
                                    GradeBadge(gradeValue = subjectAvg, outOf = 20.0)
                                } else {
                                    Text(
                                        text = "-- / 20",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Summary Box (General Average, Mention, Attendance)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "MOYENNE GÉNÉRALE :",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = mention.first,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = mention.second
                                )
                            }
                            Text(
                                text = "${String.format(Locale.getDefault(), "%.2f", generalAverage)} / 20",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(8.dp))

                        // Attendance report
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Assiduité du trimestre :",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "$absencesCount demi-journées ($unjustifiedAbsences injustifiée(s)) • $latesCount retard(s)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Official School Stamp & Signatures Box
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Visa du Professeur Principal",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = studentClass?.mainTeacher ?: "M. Bernard",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        // Seal
                        Box(
                            modifier = Modifier
                                .border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "CACHET OFFICIEL\nÉCOLE HOMOLOGUÉE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Le Chef d'Établissement",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Mme Hélène Mercier",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Actions: Imprimer & Télécharger
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val context = LocalContext.current
                    Button(
                        onClick = {
                            val report = viewModel.generateOfficialBulletin(student, "Trimestre 1")
                            val file = com.example.util.PdfExportUtil.exportBulletinPdf(context, report)
                            if (file != null) {
                                com.example.util.PdfExportUtil.shareOrViewPdf(context, file, "Imprimer Bulletin Officiel - ${student.fullName}")
                            } else {
                                Toast.makeText(context, "Erreur lors de la génération du bulletin", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f).testTag("btn_print_bulletin")
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Imprimer")
                    }

                    OutlinedButton(
                        onClick = {
                            val report = viewModel.generateOfficialBulletin(student, "Trimestre 1")
                            val file = com.example.util.PdfExportUtil.exportBulletinPdf(context, report)
                            if (file != null) {
                                com.example.util.PdfExportUtil.shareOrViewPdf(context, file, "Bulletin Scolaire Officiel - ${student.fullName}")
                            } else {
                                Toast.makeText(context, "Erreur lors de l'exportation du PDF", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f).testTag("btn_download_bulletin")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export PDF")
                    }

                    TextButton(onClick = onDismiss) {
                        Text("Fermer")
                    }
                }
            }
        }
    }
}

@Composable
fun StudentDetailDialog(
    student: Student,
    viewModel: SchoolViewModel,
    onDismiss: () -> Unit
) {
    val classes by viewModel.classes.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val allGrades by viewModel.grades.collectAsStateWithLifecycle()
    val allAttendances by viewModel.attendances.collectAsStateWithLifecycle()

    val studentClass = classes.find { it.id == student.classId }
    val studentGrades = allGrades.filter { it.studentId == student.id }
    val studentAttendances = allAttendances.filter { it.studentId == student.id }

    var selectedSubTab by remember { mutableIntStateOf(0) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StudentAvatar(name = student.fullName, colorHex = student.avatarColorHex, size = 48)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = student.fullName,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "${studentClass?.name ?: ""} • Matricule: ${student.matricule}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions row: Voir Bulletin, Export PDF, Supprimer
                val context = LocalContext.current
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            onDismiss()
                            viewModel.openBulletin(student)
                        },
                        modifier = Modifier.weight(1.2f).testTag("btn_detail_view_bulletin"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Voir Bulletin")
                    }

                    OutlinedButton(
                        onClick = {
                            val report = viewModel.generateOfficialBulletin(student, "Trimestre 1")
                            val file = com.example.util.PdfExportUtil.exportBulletinPdf(context, report)
                            if (file != null) {
                                com.example.util.PdfExportUtil.shareOrViewPdf(context, file, "Bulletin PDF - ${student.fullName}")
                            } else {
                                Toast.makeText(context, "Erreur génération PDF", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1.1f).testTag("btn_detail_export_bulletin_pdf"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PDF")
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.deleteStudent(student)
                            onDismiss()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_detail_delete_student")
                    ) {
                        Text("Supprimer")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Contact card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Coordonnées & Informations :",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = student.parentPhone.ifBlank { "Non renseigné" }, style = MaterialTheme.typography.bodySmall)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = student.parentEmail.ifBlank { "Non renseigné" }, style = MaterialTheme.typography.bodySmall)
                        }
                        Text(
                            text = "Date de naissance : ${student.birthDate.ifBlank { "Non renseignée" }} • Genre : ${if (student.gender == "M") "Masculin" else "Féminin"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Historique récent :",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (studentGrades.isEmpty() && studentAttendances.isEmpty()) {
                        item {
                            Text(
                                text = "Aucun enregistrement pour cet élève.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    items(studentGrades) { grade ->
                        val sub = subjects.find { it.id == grade.subjectId }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${sub?.name ?: "Matière"} : ${grade.title}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                    Text(
                                        text = "${grade.date} • Coeff ${grade.coefficient}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                GradeBadge(gradeValue = grade.gradeValue, outOf = grade.outOf)
                            }
                        }
                    }

                    items(studentAttendances) { att ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${att.date} (${att.timeSlot})",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                    if (att.reason.isNotBlank()) {
                                        Text(
                                            text = att.reason,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                AttendanceStatusChip(status = att.status, compact = true)
                            }
                        }
                    }
                }
            }
        }
    }
}
