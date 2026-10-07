package com.example.ui.screens.grades

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Grade
import com.example.data.model.SchoolClass
import com.example.data.model.Student
import com.example.data.model.Subject
import com.example.data.model.Teacher
import com.example.data.model.UserRole
import com.example.ui.components.EmptyStateView
import com.example.ui.components.GradeBadge
import com.example.ui.components.StudentAvatar
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusDangerText
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusSuccessContainer
import com.example.ui.theme.StatusSuccessText
import com.example.ui.theme.StatusWarning
import com.example.ui.viewmodel.SchoolViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Modèle pour une évaluation dynamique configurable par le professeur.
 */
data class EvaluationConfig(
    val id: String = UUID.randomUUID().toString(),
    var title: String,
    var outOf: Double = 20.0,
    var coefficient: Double = 1.0,
    var date: String,
    var isMainGrade: Boolean = false
)

/**
 * Note additionnelle dynamique spécifique à un élève (rattrapage, oral supplémentaire, etc.).
 */
data class StudentExtraGrade(
    val id: String = UUID.randomUUID().toString(),
    var title: String = "Évaluation supplémentaire",
    var gradeValueStr: String = "",
    var outOfStr: String = "20",
    var coefficientStr: String = "1.0",
    var comment: String = ""
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TeacherGradeEntryScreen(
    viewModel: SchoolViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val classes by viewModel.classes.collectAsStateWithLifecycle()
    val allStudents by viewModel.students.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val teachers by viewModel.teachers.collectAsStateWithLifecycle()
    val existingGrades by viewModel.grades.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Date actuelle par défaut
    val todayDateStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val timetableSlots by viewModel.timetableSlots.collectAsStateWithLifecycle()

    val isTeacher = currentUser.role == UserRole.PROFESSEUR
    val myTeacher = remember(teachers, currentUser) {
        teachers.find { it.id == currentUser.linkedTeacherId }
            ?: teachers.find { it.fullName.contains(currentUser.fullName, ignoreCase = true) || currentUser.fullName.contains(it.lastName, ignoreCase = true) }
            ?: teachers.firstOrNull()
    }

    // Sélecteurs de contexte enseignant
    var selectedTeacherId by remember(teachers, myTeacher, isTeacher) {
        mutableStateOf(if (isTeacher && myTeacher != null) myTeacher.id else (teachers.firstOrNull()?.id ?: 0L))
    }

    // Classes autorisées : si professeur connecté, strictement les classes qui lui sont attribuées par la Scolarité
    val authorizedClasses = remember(classes, timetableSlots, isTeacher, selectedTeacherId) {
        if (isTeacher) {
            val assignedClassIds = timetableSlots
                .filter { it.teacherId == selectedTeacherId || (myTeacher != null && it.teacherName.contains(myTeacher.lastName, ignoreCase = true)) }
                .map { it.classId }
                .toSet()
            if (assignedClassIds.isNotEmpty()) {
                classes.filter { it.id in assignedClassIds }
            } else classes
        } else classes
    }

    var selectedClassId by remember(authorizedClasses) {
        mutableStateOf(authorizedClasses.firstOrNull()?.id ?: 0L)
    }

    // Matières autorisées : pour un enseignant, sa matière d'affectation
    val currentTeacherObj = remember(teachers, selectedTeacherId) { teachers.find { it.id == selectedTeacherId } }
    val authorizedSubjects = remember(subjects, isTeacher, currentTeacherObj) {
        if (isTeacher && currentTeacherObj != null && currentTeacherObj.subjectName.isNotBlank()) {
            val filtered = subjects.filter { it.name.contains(currentTeacherObj.subjectName, ignoreCase = true) }
            if (filtered.isNotEmpty()) filtered else subjects
        } else subjects
    }

    var selectedSubjectId by remember(authorizedSubjects) {
        mutableStateOf(authorizedSubjects.firstOrNull()?.id ?: 0L)
    }
    var selectedPeriod by remember {
        mutableStateOf("Trimestre 1")
    }

    // Liste des évaluations dynamiques globales de la session
    val evaluations = remember {
        mutableStateListOf(
            EvaluationConfig(
                title = "Contrôle continu N°1",
                outOf = 20.0,
                coefficient = 2.0,
                date = todayDateStr
            ),
            EvaluationConfig(
                title = "Interrogation orale",
                outOf = 10.0,
                coefficient = 1.0,
                date = todayDateStr
            )
        )
    }

    // Élèves de la classe choisie
    val classStudents = remember(allStudents, selectedClassId) {
        allStudents.filter { it.classId == selectedClassId }
    }

    // Dictionnaire des notes principales : clé = "studentId_evaluationId" -> valeur texte
    val gradeInputs = remember { mutableStateMapOf<String, String>() }

    // Dictionnaire des appréciations par élève : clé = studentId -> appreciation
    val studentComments = remember { mutableStateMapOf<Long, String>() }

    // Dictionnaire des notes supplémentaires dynamiques par élève
    val extraGrades = remember { mutableStateMapOf<Long, MutableList<StudentExtraGrade>>() }

    // États déroulants des menus
    var teacherExpanded by remember { mutableStateOf(false) }
    var classExpanded by remember { mutableStateOf(false) }
    var subjectExpanded by remember { mutableStateOf(false) }
    var periodExpanded by remember { mutableStateOf(false) }

    // Dialogues
    var showAddEvalDialog by remember { mutableStateOf(false) }
    var showBatchFillDialog by remember { mutableStateOf(false) }
    var showHistorySection by remember { mutableStateOf(false) }

    // Calcul en temps réel des statistiques de la session de saisie
    val currentEvaluationsCount = evaluations.size
    val totalValidGradesCount = remember(gradeInputs.values, extraGrades.values) {
        val countMain = gradeInputs.values.count { it.isNotBlank() && it.toDoubleOrNull() != null }
        val countExtra = extraGrades.values.flatten().count { it.gradeValueStr.isNotBlank() && it.gradeValueStr.toDoubleOrNull() != null }
        countMain + countExtra
    }

    val sessionAverage = remember(gradeInputs.toMap(), extraGrades.toMap(), evaluations.toList()) {
        var weightedSum = 0.0
        var totalCoeff = 0.0

        classStudents.forEach { student ->
            evaluations.forEach { eval ->
                val key = "${student.id}_${eval.id}"
                val value = gradeInputs[key]?.toDoubleOrNull()
                if (value != null && eval.outOf > 0) {
                    val normalized = (value / eval.outOf) * 20.0
                    weightedSum += normalized * eval.coefficient
                    totalCoeff += eval.coefficient
                }
            }
            extraGrades[student.id]?.forEach { extra ->
                val v = extra.gradeValueStr.toDoubleOrNull()
                val outOf = extra.outOfStr.toDoubleOrNull() ?: 20.0
                val coeff = extra.coefficientStr.toDoubleOrNull() ?: 1.0
                if (v != null && outOf > 0) {
                    val normalized = (v / outOf) * 20.0
                    weightedSum += normalized * coeff
                    totalCoeff += coeff
                }
            }
        }

        if (totalCoeff > 0) weightedSum / totalCoeff else null
    }

    // Historique des notes déjà enregistrées pour cette matière et cette classe
    val classSubjectHistory = remember(existingGrades, selectedClassId, selectedSubjectId) {
        existingGrades.filter { it.classId == selectedClassId && it.subjectId == selectedSubjectId }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("teacher_grade_entry_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Saisie des notes — Professeur",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = "Formulaire dynamique multi-notes par matière",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            // Validation et sauvegarde des notes
                            val gradesToSave = mutableListOf<Grade>()
                            val selectedClassName = classes.find { it.id == selectedClassId }?.name ?: ""
                            val selectedSubjectName = subjects.find { it.id == selectedSubjectId }?.name ?: ""

                            classStudents.forEach { student ->
                                val studentComment = studentComments[student.id]?.trim() ?: ""

                                // 1. Notes des évaluations configurées
                                evaluations.forEach { eval ->
                                    val key = "${student.id}_${eval.id}"
                                    val gradeVal = gradeInputs[key]?.toDoubleOrNull()
                                    if (gradeVal != null) {
                                        gradesToSave.add(
                                            Grade(
                                                studentId = student.id,
                                                subjectId = selectedSubjectId,
                                                classId = selectedClassId,
                                                title = eval.title.ifBlank { "Évaluation" },
                                                gradeValue = gradeVal,
                                                outOf = eval.outOf,
                                                coefficient = eval.coefficient,
                                                period = selectedPeriod,
                                                date = eval.date,
                                                comment = studentComment,
                                                isMainGrade = eval.isMainGrade,
                                                isPublished = false,
                                                createdAt = System.currentTimeMillis()
                                            )
                                        )
                                    }
                                }

                                // 2. Notes individuelles supplémentaires
                                extraGrades[student.id]?.forEach { extra ->
                                    val extraVal = extra.gradeValueStr.toDoubleOrNull()
                                    if (extraVal != null) {
                                        val outOf = extra.outOfStr.toDoubleOrNull() ?: 20.0
                                        val coeff = extra.coefficientStr.toDoubleOrNull() ?: 1.0
                                        val cmt = if (extra.comment.isNotBlank()) extra.comment else studentComment
                                        gradesToSave.add(
                                            Grade(
                                                studentId = student.id,
                                                subjectId = selectedSubjectId,
                                                classId = selectedClassId,
                                                title = extra.title.ifBlank { "Évaluation complémentaire" },
                                                gradeValue = extraVal,
                                                outOf = if (outOf > 0) outOf else 20.0,
                                                coefficient = if (coeff > 0) coeff else 1.0,
                                                period = selectedPeriod,
                                                date = todayDateStr,
                                                comment = cmt
                                            )
                                        )
                                    }
                                }
                            }

                            if (gradesToSave.isEmpty()) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Aucune note valide n'a été saisie. Remplissez au moins une note.")
                                }
                            } else {
                                viewModel.addGradesBatch(gradesToSave) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            "${gradesToSave.size} note(s) enregistrée(s) avec succès pour $selectedClassName en $selectedSubjectName !"
                                        )
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("btn_save_all_grades")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Enregistrer (${totalValidGradesCount})", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. CARTE CONTEXTE PROFESSEUR, MATIÈRE & CLASSE
            item {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("teacher_context_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.School,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Contexte d'Enseignement",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Sélectionnez votre profil, la classe et la matière concernée",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        HorizontalDivider()

                        // Ligne 1 : Professeur & Matière
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Sélecteur Professeur
                            ExposedDropdownMenuBox(
                                expanded = teacherExpanded,
                                onExpandedChange = { teacherExpanded = !teacherExpanded },
                                modifier = Modifier.weight(1f)
                            ) {
                                val currentTeacher = teachers.find { it.id == selectedTeacherId }
                                OutlinedTextField(
                                    value = currentTeacher?.fullName ?: "Sélectionner enseignant",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Professeur") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = teacherExpanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                        .testTag("select_teacher_dropdown"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                ExposedDropdownMenu(
                                    expanded = teacherExpanded,
                                    onDismissRequest = { teacherExpanded = false }
                                ) {
                                    teachers.forEach { teacher ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(teacher.fullName, fontWeight = FontWeight.SemiBold)
                                                    if (teacher.subjectName.isNotBlank()) {
                                                        Text(teacher.subjectName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                                    }
                                                }
                                            },
                                            onClick = {
                                                selectedTeacherId = teacher.id
                                                teacherExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Sélecteur Matière
                            ExposedDropdownMenuBox(
                                expanded = subjectExpanded,
                                onExpandedChange = { subjectExpanded = !subjectExpanded },
                                modifier = Modifier.weight(1f)
                            ) {
                                val currentSubject = subjects.find { it.id == selectedSubjectId }
                                OutlinedTextField(
                                    value = currentSubject?.let { "${it.name} (Coeff ${it.coefficient})" } ?: "Sélectionner matière",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Matière enseignée") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                        .testTag("select_subject_dropdown"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                ExposedDropdownMenu(
                                    expanded = subjectExpanded,
                                    onDismissRequest = { subjectExpanded = false }
                                ) {
                                    authorizedSubjects.forEach { subject ->
                                        DropdownMenuItem(
                                            text = { Text("${subject.name} (Coeff ${subject.coefficient})") },
                                            onClick = {
                                                selectedSubjectId = subject.id
                                                subjectExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Ligne 2 : Classe & Période
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Sélecteur Classe
                            ExposedDropdownMenuBox(
                                expanded = classExpanded,
                                onExpandedChange = { classExpanded = !classExpanded },
                                modifier = Modifier.weight(1f)
                            ) {
                                val currentClass = authorizedClasses.find { it.id == selectedClassId } ?: authorizedClasses.firstOrNull()
                                OutlinedTextField(
                                    value = currentClass?.name ?: "Sélectionner classe",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text(if (isTeacher) "Classe attribuée" else "Classe") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classExpanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                        .testTag("select_class_dropdown"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                ExposedDropdownMenu(
                                    expanded = classExpanded,
                                    onDismissRequest = { classExpanded = false }
                                ) {
                                    authorizedClasses.forEach { schoolClass ->
                                        DropdownMenuItem(
                                            text = { Text("${schoolClass.name} - ${schoolClass.level}") },
                                            onClick = {
                                                selectedClassId = schoolClass.id
                                                classExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Sélecteur Période
                            ExposedDropdownMenuBox(
                                expanded = periodExpanded,
                                onExpandedChange = { periodExpanded = !periodExpanded },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = selectedPeriod,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Période / Trimestre") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = periodExpanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                        .testTag("select_period_dropdown"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                ExposedDropdownMenu(
                                    expanded = periodExpanded,
                                    onDismissRequest = { periodExpanded = false }
                                ) {
                                    listOf("Trimestre 1", "Trimestre 2", "Trimestre 3").forEach { p ->
                                        DropdownMenuItem(
                                            text = { Text(p) },
                                            onClick = {
                                                selectedPeriod = p
                                                periodExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. BANNIÈRE STATISTIQUES EN DIRECT DE LA SESSION
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Élèves : ${classStudents.size}  |  Notes saisies : $totalValidGradesCount",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Évaluations configurées : ${evaluations.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }

                        if (sessionAverage != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (sessionAverage >= 10.0) StatusSuccessContainer else MaterialTheme.colorScheme.errorContainer
                            ) {
                                Text(
                                    text = "Moyenne: ${String.format(Locale.getDefault(), "%.2f", sessionAverage)}/20",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = if (sessionAverage >= 10.0) StatusSuccessText else MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        } else {
                            Text(
                                text = "En attente de notes",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }

            // 3. GESTION DES ÉVALUATIONS DYNAMIQUES ("Évaluations de la session")
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Grade,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Évaluations configurées (${evaluations.size})",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            Button(
                                onClick = { showAddEvalDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("btn_add_evaluation")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ajouter évaluation", style = MaterialTheme.typography.labelMedium)
                            }
                        }

                        Text(
                            text = "Définissez les différentes notes que chaque élève va recevoir pour cette matière (ex: Contrôle, Devoir maison, Oral). Vous pouvez en ajouter autant que souhaité.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Liste horizontale/verticale des évaluations créées
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            evaluations.forEachIndexed { index, eval ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.outlineVariant
                                    )
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = eval.title,
                                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                text = "Sur /${eval.outOf.toInt()} • Coeff ${eval.coefficient} • ${eval.date}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        if (evaluations.size > 1) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            IconButton(
                                                onClick = {
                                                    evaluations.removeAt(index)
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Close,
                                                    contentDescription = "Supprimer",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Boutons d'actions rapides : Remplir par lot
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = { showBatchFillDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Remplissage rapide par défaut", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }

            // 4. LISTE DYNAMIQUE DES ÉLÈVES AVEC SAISIE MULTI-NOTES
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Élèves de la classe (${classStudents.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Text(
                        text = "Saisie dynamique par élève",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (classStudents.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.Person,
                        title = "Aucun élève trouvé",
                        description = "Aucun élève n'est encore inscrit dans cette classe."
                    )
                }
            } else {
                items(classStudents, key = { it.id }) { student ->
                    StudentGradeRowCard(
                        student = student,
                        evaluations = evaluations,
                        gradeInputs = gradeInputs,
                        studentComments = studentComments,
                        extraGradesList = extraGrades.getOrPut(student.id) { mutableListOf() },
                        onAddExtraGrade = {
                            extraGrades.getOrPut(student.id) { mutableListOf() }.add(
                                StudentExtraGrade(title = "Note bonus / rattrapage")
                            )
                        },
                        onRemoveExtraGrade = { extra ->
                            extraGrades[student.id]?.remove(extra)
                        }
                    )
                }
            }

            // 5. ACCORDÉON HISTORIQUE DES NOTES DÉJÀ EXISTANTES
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showHistorySection = !showHistorySection },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.History,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Historique existant (${classSubjectHistory.size} notes enregistrées dans cette matière)",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Icon(
                                imageVector = if (showHistorySection) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null
                            )
                        }

                        AnimatedVisibility(visible = showHistorySection) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (classSubjectHistory.isEmpty()) {
                                    Text(
                                        text = "Aucune note n'a encore été enregistrée dans cette matière pour cette classe.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    val studentsMap = remember(allStudents) { allStudents.associateBy { it.id } }
                                    classSubjectHistory.take(8).forEach { grade ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(6.dp))
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val isLocked = grade.isLockedForTeacher()
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = studentsMap[grade.studentId]?.fullName ?: "Élève #${grade.studentId}",
                                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                                                    )
                                                    if (grade.isMainGrade) {
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Surface(
                                                            shape = RoundedCornerShape(4.dp),
                                                            color = Color(0xFFFEF3C7)
                                                        ) {
                                                            Text(
                                                                text = "Note Principale",
                                                                color = Color(0xFFD97706),
                                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                                Text(
                                                    text = "${grade.title} • ${grade.period} • ${grade.date}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.outline
                                                )
                                                if (isLocked) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(11.dp))
                                                        Spacer(modifier = Modifier.width(3.dp))
                                                        Text(
                                                            text = "Verrouillé après 5 jours (Modification par Scolarité uniquement)",
                                                            fontSize = 10.sp,
                                                            color = Color(0xFFDC2626),
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    }
                                                } else {
                                                    Text(
                                                        text = "Modifiable par l'enseignant (délai de 5 jours actif)",
                                                        fontSize = 10.sp,
                                                        color = Color(0xFF16A34A),
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            }
                                            GradeBadge(
                                                gradeValue = grade.gradeValue,
                                                outOf = grade.outOf
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bouton final de sauvegarde
            item {
                Button(
                    onClick = {
                        val gradesToSave = mutableListOf<Grade>()
                        val selectedClassName = classes.find { it.id == selectedClassId }?.name ?: ""
                        val selectedSubjectName = subjects.find { it.id == selectedSubjectId }?.name ?: ""

                        classStudents.forEach { student ->
                            val studentComment = studentComments[student.id]?.trim() ?: ""

                            evaluations.forEach { eval ->
                                val key = "${student.id}_${eval.id}"
                                val gradeVal = gradeInputs[key]?.toDoubleOrNull()
                                if (gradeVal != null) {
                                    gradesToSave.add(
                                        Grade(
                                            studentId = student.id,
                                            subjectId = selectedSubjectId,
                                            classId = selectedClassId,
                                            title = eval.title.ifBlank { "Évaluation" },
                                            gradeValue = gradeVal,
                                            outOf = eval.outOf,
                                            coefficient = eval.coefficient,
                                            period = selectedPeriod,
                                            date = eval.date,
                                            comment = studentComment,
                                            isMainGrade = eval.isMainGrade,
                                            isPublished = false,
                                            createdAt = System.currentTimeMillis()
                                        )
                                    )
                                }
                            }

                            extraGrades[student.id]?.forEach { extra ->
                                val extraVal = extra.gradeValueStr.toDoubleOrNull()
                                if (extraVal != null) {
                                    val outOf = extra.outOfStr.toDoubleOrNull() ?: 20.0
                                    val coeff = extra.coefficientStr.toDoubleOrNull() ?: 1.0
                                    val cmt = if (extra.comment.isNotBlank()) extra.comment else studentComment
                                    gradesToSave.add(
                                        Grade(
                                            studentId = student.id,
                                            subjectId = selectedSubjectId,
                                            classId = selectedClassId,
                                            title = extra.title.ifBlank { "Évaluation complémentaire" },
                                            gradeValue = extraVal,
                                            outOf = if (outOf > 0) outOf else 20.0,
                                            coefficient = if (coeff > 0) coeff else 1.0,
                                            period = selectedPeriod,
                                            date = todayDateStr,
                                            comment = cmt
                                        )
                                    )
                                }
                            }
                        }

                        if (gradesToSave.isEmpty()) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Veuillez saisir au moins une note pour un élève.")
                            }
                        } else {
                            viewModel.addGradesBatch(gradesToSave) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        "Bravo ! ${gradesToSave.size} notes ont été enregistrées avec succès pour $selectedClassName !"
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_bottom_save_grades"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Valider et enregistrer toutes les notes ($totalValidGradesCount)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }

    // Modal pour ajouter une nouvelle évaluation globale
    if (showAddEvalDialog) {
        var newTitle by remember { mutableStateOf("") }
        var newOutOfStr by remember { mutableStateOf("20") }
        var newCoeffStr by remember { mutableStateOf("1.0") }
        var newDate by remember { mutableStateOf(todayDateStr) }
        var newIsMainGrade by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddEvalDialog = false },
            title = { Text("Nouvelle Évaluation", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Ajoutez une nouvelle colonne de notation pour cette matière.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Titre de l'évaluation") },
                        placeholder = { Text("Ex: DS N°2, Interro de vocabulaire...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newOutOfStr,
                            onValueChange = { newOutOfStr = it },
                            label = { Text("Barème (/20, /10)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = newCoeffStr,
                            onValueChange = { newCoeffStr = it },
                            label = { Text("Coefficient") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = newDate,
                        onValueChange = { newDate = it },
                        label = { Text("Date (AAAA-MM-JJ)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .clickable { newIsMainGrade = !newIsMainGrade }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.Checkbox(
                            checked = newIsMainGrade,
                            onCheckedChange = { newIsMainGrade = it }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("Note Principale (Composition / Examen)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Distinguer les devoirs continus de l'épreuve majeure", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val outOf = newOutOfStr.toDoubleOrNull() ?: 20.0
                        val coeff = newCoeffStr.toDoubleOrNull() ?: 1.0
                        evaluations.add(
                            EvaluationConfig(
                                title = newTitle.ifBlank { "Évaluation ${evaluations.size + 1}" },
                                outOf = if (outOf > 0) outOf else 20.0,
                                coefficient = if (coeff > 0) coeff else 1.0,
                                date = newDate.ifBlank { todayDateStr },
                                isMainGrade = newIsMainGrade
                            )
                        )
                        showAddEvalDialog = false
                    }
                ) {
                    Text("Ajouter")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddEvalDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Modal pour remplissage automatique / rapide par défaut
    if (showBatchFillDialog) {
        var defaultGradeStr by remember { mutableStateOf("15.0") }
        var targetEvalIndex by remember { mutableIntStateOf(0) }

        AlertDialog(
            onDismissRequest = { showBatchFillDialog = false },
            title = { Text("Remplissage rapide par défaut", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Attribuez rapidement une note de base à tous les élèves qui n'ont pas encore de note.",
                        style = MaterialTheme.typography.bodySmall
                    )

                    OutlinedTextField(
                        value = defaultGradeStr,
                        onValueChange = { defaultGradeStr = it },
                        label = { Text("Note par défaut") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Text(
                        text = "Pour quelle évaluation ?",
                        style = MaterialTheme.typography.labelMedium
                    )

                    evaluations.forEachIndexed { idx, eval ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { targetEvalIndex = idx }
                                .padding(vertical = 4.dp)
                        ) {
                            androidx.compose.material3.RadioButton(
                                selected = targetEvalIndex == idx,
                                onClick = { targetEvalIndex = idx }
                            )
                            Text(eval.title)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val targetEval = evaluations.getOrNull(targetEvalIndex)
                        if (targetEval != null && defaultGradeStr.isNotBlank()) {
                            classStudents.forEach { stu ->
                                val key = "${stu.id}_${targetEval.id}"
                                if (gradeInputs[key].isNullOrBlank()) {
                                    gradeInputs[key] = defaultGradeStr
                                }
                            }
                        }
                        showBatchFillDialog = false
                    }
                ) {
                    Text("Appliquer à tous")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBatchFillDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

/**
 * Composant de carte pour chaque élève permettant de saisir dynamiquement plusieurs notes :
 * - Notes correspondant aux évaluations globales
 * - Notes supplémentaires individuelles (+ Note additionnelle)
 * - Commentaire / Appréciation enseignant
 * - Calcul en direct de la moyenne prévisionnelle de l'élève
 */
@Composable
fun StudentGradeRowCard(
    student: Student,
    evaluations: List<EvaluationConfig>,
    gradeInputs: MutableMap<String, String>,
    studentComments: MutableMap<Long, String>,
    extraGradesList: MutableList<StudentExtraGrade>,
    onAddExtraGrade: () -> Unit,
    onRemoveExtraGrade: (StudentExtraGrade) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    // Calcul en direct de la moyenne de l'élève sur cette saisie
    var currentWeightedSum = 0.0
    var currentTotalCoeff = 0.0
    var validGradesCount = 0

    evaluations.forEach { eval ->
        val key = "${student.id}_${eval.id}"
        val v = gradeInputs[key]?.toDoubleOrNull()
        if (v != null && eval.outOf > 0) {
            val normalized = (v / eval.outOf) * 20.0
            currentWeightedSum += normalized * eval.coefficient
            currentTotalCoeff += eval.coefficient
            validGradesCount++
        }
    }

    extraGradesList.forEach { extra ->
        val v = extra.gradeValueStr.toDoubleOrNull()
        val outOf = extra.outOfStr.toDoubleOrNull() ?: 20.0
        val coeff = extra.coefficientStr.toDoubleOrNull() ?: 1.0
        if (v != null && outOf > 0) {
            val normalized = (v / outOf) * 20.0
            currentWeightedSum += normalized * coeff
            currentTotalCoeff += coeff
            validGradesCount++
        }
    }

    val studentSimulatedAverage = if (currentTotalCoeff > 0) currentWeightedSum / currentTotalCoeff else null

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("student_grade_row_${student.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // En-tête élève
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
                        size = 40
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = student.fullName,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Matricule: ${student.matricule}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                // Badge moyenne prévisionnelle
                if (studentSimulatedAverage != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when {
                            studentSimulatedAverage >= 14.0 -> StatusSuccessContainer
                            studentSimulatedAverage >= 10.0 -> MaterialTheme.colorScheme.secondaryContainer
                            else -> MaterialTheme.colorScheme.errorContainer
                        }
                    ) {
                        Text(
                            text = "Moyenne: ${String.format(Locale.getDefault(), "%.1f", studentSimulatedAverage)}/20",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = when {
                                studentSimulatedAverage >= 14.0 -> StatusSuccessText
                                studentSimulatedAverage >= 10.0 -> MaterialTheme.colorScheme.onSecondaryContainer
                                else -> MaterialTheme.colorScheme.onErrorContainer
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // 1. NOTES DES ÉVALUATIONS GLOBALES POUR CET ÉLÈVE
            evaluations.forEach { eval ->
                val key = "${student.id}_${eval.id}"
                var currentValue = gradeInputs[key] ?: ""

                val numericVal = currentValue.toDoubleOrNull()
                val isInvalid = numericVal != null && (numericVal < 0 || numericVal > eval.outOf)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = eval.title,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "Barème: /${eval.outOf.toInt()} • Coeff: ${eval.coefficient}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Champ de saisie de la note
                    Column(horizontalAlignment = Alignment.End) {
                        OutlinedTextField(
                            value = currentValue,
                            onValueChange = { gradeInputs[key] = it },
                            placeholder = { Text("/${eval.outOf.toInt()}") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            isError = isInvalid,
                            modifier = Modifier
                                .width(95.dp)
                                .height(52.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                        if (isInvalid) {
                            Text(
                                text = "Max ${eval.outOf.toInt()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // 2. NOTES COMPLÉMENTAIRES DYNAMIQUES POUR CET ÉLÈVE (+ Note individuelle)
            if (extraGradesList.isNotEmpty()) {
                Text(
                    text = "Notes individuelles spécifiques :",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                extraGradesList.forEach { extra ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = extra.title,
                            onValueChange = { extra.title = it },
                            label = { Text("Titre") },
                            modifier = Modifier.weight(1.5f),
                            singleLine = true,
                            shape = RoundedCornerShape(6.dp)
                        )

                        OutlinedTextField(
                            value = extra.gradeValueStr,
                            onValueChange = { extra.gradeValueStr = it },
                            label = { Text("Note") },
                            placeholder = { Text("/20") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(6.dp)
                        )

                        IconButton(
                            onClick = { onRemoveExtraGrade(extra) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Supprimer note",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Actions : Ajouter note individuelle + Appréciation enseignant
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onAddExtraGrade,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Note individuelle", style = MaterialTheme.typography.labelSmall)
                }

                TextButton(
                    onClick = { expanded = !expanded },
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        if (studentComments[student.id].isNullOrBlank()) "Ajouter appréciation" else "Modifier appréciation",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            // Zone d'appréciation / observation extensible
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = studentComments[student.id] ?: "",
                        onValueChange = { studentComments[student.id] = it },
                        label = { Text("Appréciation / Commentaire enseignant") },
                        placeholder = { Text("Ex: Très bon travail, persévérer dans cette voie...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Suggestions rapides d'appréciations
                    val suggestions = listOf("Très bon trimestre", "Travail sérieux", "En progrès", "Manque de rigueur", "Excellente participation")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        suggestions.forEach { text ->
                            AssistChip(
                                onClick = { studentComments[student.id] = text },
                                label = { Text(text, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
            }
        }
    }
}
