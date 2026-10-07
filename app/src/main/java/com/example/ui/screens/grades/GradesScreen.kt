package com.example.ui.screens.grades

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
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Grading
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Grade
import com.example.data.model.Student
import com.example.data.model.Subject
import com.example.data.model.UserRole
import com.example.ui.components.ClassFilterRow
import com.example.ui.components.EmptyStateView
import com.example.ui.components.GradeBadge
import com.example.ui.components.StudentAvatar
import com.example.ui.theme.StatusSuccess
import com.example.ui.viewmodel.SchoolViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradesScreen(
    viewModel: SchoolViewModel,
    modifier: Modifier = Modifier
) {
    val classes by viewModel.classes.collectAsStateWithLifecycle()
    val students by viewModel.students.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val grades by viewModel.grades.collectAsStateWithLifecycle()
    val selectedClassId by viewModel.selectedClassId.collectAsStateWithLifecycle()
    val selectedPeriod by viewModel.selectedPeriod.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val selectedChildId by viewModel.selectedChildStudentId.collectAsStateWithLifecycle()

    val isStudent = currentUser.role == UserRole.ELEVE
    val isParent = currentUser.role == UserRole.PARENT
    val isTeacher = currentUser.role == UserRole.PROFESSEUR
    val isScolariteOrAdmin = currentUser.role in listOf(UserRole.ADMIN, UserRole.DIRECTION, UserRole.SUPER_ADMIN)

    // Tab titles based on role
    val tabTitles = if (isStudent || isParent) {
        listOf("Moyennes & Relevé", "Toutes les notes publiées", "Statistiques par matière")
    } else {
        listOf("Moyennes élèves", "Toutes les notes", "Par matière", "Saisie Professeur")
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val studentsMap = remember(students) { students.associateBy { it.id } }
    val subjectsMap = remember(subjects) { subjects.associateBy { it.id } }
    val classesMap = remember(classes) { classes.associateBy { it.id } }

    val periods = listOf("Tous", "Trimestre 1", "Trimestre 2", "Trimestre 3")

    // Filter grades: For student/parent, ONLY show published grades!
    val roleFilteredGrades = remember(grades, currentUser, selectedChildId) {
        when {
            isStudent -> {
                val studentId = currentUser.linkedStudentId ?: 1L
                grades.filter { it.studentId == studentId && it.isPublished }
            }
            isParent -> {
                val childId = selectedChildId ?: currentUser.linkedStudentId ?: 1L
                grades.filter { it.studentId == childId && it.isPublished }
            }
            else -> grades
        }
    }

    val filteredGrades = remember(roleFilteredGrades, selectedClassId, selectedPeriod) {
        roleFilteredGrades.filter { grade ->
            val matchClass = selectedClassId == null || grade.classId == selectedClassId
            val matchPeriod = selectedPeriod == "Tous" || grade.period == selectedPeriod
            matchClass && matchPeriod
        }
    }

    // Filter students
    val filteredStudents = remember(students, selectedClassId, isStudent, isParent, currentUser, selectedChildId) {
        when {
            isStudent -> {
                val studentId = currentUser.linkedStudentId ?: 1L
                students.filter { it.id == studentId }
            }
            isParent -> {
                val childId = selectedChildId ?: currentUser.linkedStudentId ?: 1L
                students.filter { it.id == childId }
            }
            selectedClassId != null -> students.filter { it.classId == selectedClassId }
            else -> students
        }
    }

    // Count published vs pending in selected scope
    val publishedCount = remember(filteredGrades) { filteredGrades.count { it.isPublished } }
    val unpublishedCount = remember(filteredGrades) { filteredGrades.count { !it.isPublished } }

    Scaffold(
        modifier = modifier.testTag("grades_screen"),
        floatingActionButton = {
            if (!isStudent && !isParent && selectedTabIndex != 3) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ExtendedFloatingActionButton(
                        onClick = { selectedTabIndex = 3 },
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.testTag("fab_teacher_grade_entry")
                    ) {
                        Icon(Icons.Default.Grading, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Saisie Professeur", fontWeight = FontWeight.Bold)
                    }

                    if (isScolariteOrAdmin) {
                        FloatingActionButton(
                            onClick = { viewModel.setAddGradeDialogOpen(true) },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.testTag("fab_add_grade")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Saisir une note")
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Sub-tabs
            PrimaryTabRow(
                selectedTabIndex = selectedTabIndex.coerceAtMost(tabTitles.size - 1),
                modifier = Modifier.fillMaxWidth()
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    )
                }
            }

            if (!isStudent && !isParent && selectedTabIndex == 3) {
                TeacherGradeEntryScreen(
                    viewModel = viewModel,
                    onBack = { selectedTabIndex = 0 },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Banner for students / parents: explain that grades are published by Scolarité
                if (isStudent || isParent) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Proclamation officielle : Seules les notes validées et annoncées par la Scolarité sont affichées ci-dessous.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                // Scolarite / Admin Publication Action Banner
                if (isScolariteOrAdmin && selectedClassId != null) {
                    val currentClass = classes.find { it.id == selectedClassId }
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Validation Scolarité • ${currentClass?.name ?: ""}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "$publishedCount note(s) publiée(s) aux élèves • $unpublishedCount en attente",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (unpublishedCount > 0) Color(0xFFD97706) else Color(0xFF16A34A)
                                )
                            }

                            if (unpublishedCount > 0) {
                                Button(
                                    onClick = {
                                        val periodToPublish = if (selectedPeriod == "Tous") "Trimestre 1" else selectedPeriod
                                        viewModel.publishGradesForClassAndPeriod(selectedClassId ?: 1L, periodToPublish)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Publish, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Publier les notes", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Class Filters (Hidden for Student to keep clean view of their own class)
                if (!isStudent) {
                    ClassFilterRow(
                        classes = classes,
                        selectedClassId = selectedClassId,
                        onSelectClass = { viewModel.selectClass(it) },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                // Period Filter
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(periods) { period ->
                        FilterChip(
                            selected = selectedPeriod == period,
                            onClick = { viewModel.selectPeriod(period) },
                            label = { Text(period) }
                        )
                    }
                }

                when (selectedTabIndex) {
                    0 -> StudentAveragesTab(
                        students = filteredStudents,
                        grades = filteredGrades,
                        classesMap = classesMap,
                        onOpenBulletin = { student -> viewModel.openBulletin(student) },
                        onSelectStudent = { student -> viewModel.showStudentDetail(student) }
                    )
                    1 -> AllGradesListTab(
                        grades = filteredGrades,
                        studentsMap = studentsMap,
                        subjectsMap = subjectsMap,
                        classesMap = classesMap,
                        isScolarite = isScolariteOrAdmin,
                        isStudentOrParent = isStudent || isParent,
                        onPublishGrade = { grade -> viewModel.publishSingleGrade(grade) },
                        onDeleteGrade = { grade -> viewModel.deleteGrade(grade) },
                        onSubmitClaim = { grade, reason ->
                            viewModel.logActivity(
                                actionType = "CLAIM",
                                description = "Réclamation note : ${grade.title} (${grade.gradeValue}/${grade.outOf}) par ${currentUser.fullName}",
                                targetEntity = "Scolarité",
                                details = "Motif : $reason"
                            )
                        }
                    )
                    2 -> SubjectStatsTab(
                        subjects = subjects,
                        grades = filteredGrades
                    )
                }
            }
        }
    }
}

@Composable
private fun StudentAveragesTab(
    students: List<Student>,
    grades: List<Grade>,
    classesMap: Map<Long, com.example.data.model.SchoolClass>,
    onOpenBulletin: (Student) -> Unit,
    onSelectStudent: (Student) -> Unit
) {
    if (students.isEmpty()) {
        EmptyStateView(
            icon = Icons.Default.Grading,
            title = "Aucune note publiée",
            description = "Les notes sont en cours de validation par la Scolarité et n'ont pas encore été annoncées officiellement."
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(students, key = { it.id }) { student ->
            val studentGrades = grades.filter { it.studentId == student.id }
            val weightedSum = studentGrades.sumOf { it.normalizedTo20 * it.coefficient }
            val coeffSum = studentGrades.sumOf { it.coefficient }
            val average = if (coeffSum > 0) weightedSum / coeffSum else null

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectStudent(student) }
                    .testTag("student_grade_card_${student.id}"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
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
                            size = 46
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = student.fullName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${classesMap[student.classId]?.name ?: "Classe"} • ${studentGrades.size} note(s) enregistrée(s)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (average != null) {
                            GradeBadge(gradeValue = average, outOf = 20.0)
                        } else {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "N/A",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = { onOpenBulletin(student) },
                            modifier = Modifier.testTag("btn_bulletin_${student.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = "Bulletin",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AllGradesListTab(
    grades: List<Grade>,
    studentsMap: Map<Long, Student>,
    subjectsMap: Map<Long, Subject>,
    classesMap: Map<Long, com.example.data.model.SchoolClass>,
    isScolarite: Boolean,
    isStudentOrParent: Boolean = false,
    onPublishGrade: (Grade) -> Unit,
    onDeleteGrade: (Grade) -> Unit,
    onSubmitClaim: (Grade, String) -> Unit = { _, _ -> }
) {
    var claimTargetGrade by remember { mutableStateOf<Grade?>(null) }
    var claimReasonInput by remember { mutableStateOf("") }
    var claimSentSuccess by remember { mutableStateOf(false) }

    if (grades.isEmpty()) {
        EmptyStateView(
            icon = Icons.Default.Assignment,
            title = "Aucune note publiée",
            description = "Les notes du trimestre n'ont pas encore été proclamées officiellement par l'établissement."
        )
        return
    }

    if (claimTargetGrade != null) {
        val target = claimTargetGrade!!
        AlertDialog(
            onDismissRequest = { claimTargetGrade = null },
            title = { Text("Réclamation de Note", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Vous contestez la note de « ${target.title} » (${target.gradeValue}/${target.outOf}).",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Conformément au règlement scolaire, seules les notes déjà proclamées peuvent faire l'objet d'un recours officiel auprès de la Scolarité.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = claimReasonInput,
                        onValueChange = { claimReasonInput = it },
                        label = { Text("Motif de votre réclamation") },
                        placeholder = { Text("Ex: Omission de points dans l'exercice 2, copie non revue...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSubmitClaim(target, claimReasonInput.ifBlank { "Contestation standard de copie" })
                        claimTargetGrade = null
                        claimReasonInput = ""
                        claimSentSuccess = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Transmettre à la Scolarité")
                }
            },
            dismissButton = {
                TextButton(onClick = { claimTargetGrade = null }) {
                    Text("Annuler")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(grades, key = { it.id }) { grade ->
            val student = studentsMap[grade.studentId]
            val subject = subjectsMap[grade.subjectId]
            val schoolClass = classesMap[grade.classId]

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("grade_item_${grade.id}"),
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = subject?.code ?: "MAT",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${subject?.name ?: "Matière"} (Coeff ${grade.coefficient})",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Badges: Main Grade & Publication Status
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (grade.isMainGrade) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFFEF3C7))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Note Principale",
                                        color = Color(0xFFD97706),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }

                            val pubBg = if (grade.isPublished) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                            val pubText = if (grade.isPublished) Color(0xFF16A34A) else Color(0xFFDC2626)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(pubBg)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (grade.isPublished) "Publié" else "En attente",
                                    color = pubText,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = grade.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "${student?.fullName ?: "Élève"} • ${schoolClass?.name ?: "Classe"} • ${grade.date}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (grade.comment.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "« ${grade.comment} »",
                                    style = MaterialTheme.typography.bodySmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            GradeBadge(gradeValue = grade.gradeValue, outOf = grade.outOf)

                            if (isScolarite) {
                                if (!grade.isPublished) {
                                    IconButton(onClick = { onPublishGrade(grade) }) {
                                        Icon(
                                            imageVector = Icons.Default.Publish,
                                            contentDescription = "Publier cette note",
                                            tint = Color(0xFF16A34A),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                IconButton(onClick = { onDeleteGrade(grade) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Supprimer",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            if (isStudentOrParent && grade.isPublished) {
                                Spacer(modifier = Modifier.width(6.dp))
                                OutlinedButton(
                                    onClick = { claimTargetGrade = grade },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("Réclamer", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubjectStatsTab(
    subjects: List<Subject>,
    grades: List<Grade>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(subjects, key = { it.id }) { subject ->
            val subjectGrades = grades.filter { it.subjectId == subject.id }
            val count = subjectGrades.size
            val avg = if (count > 0) {
                subjectGrades.sumOf { it.normalizedTo20 * it.coefficient } / subjectGrades.sumOf { it.coefficient }
            } else null
            val maxGrade = subjectGrades.maxOfOrNull { it.normalizedTo20 }
            val minGrade = subjectGrades.minOfOrNull { it.normalizedTo20 }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = subject.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Coeff ${subject.coefficient}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Évaluations", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "$count", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Moyenne", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = if (avg != null) String.format(Locale.getDefault(), "%.1f", avg) else "-",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (avg != null && avg >= 10.0) StatusSuccess else MaterialTheme.colorScheme.error
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Note Max", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = if (maxGrade != null) String.format(Locale.getDefault(), "%.1f", maxGrade) else "-",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = StatusSuccess
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Note Min", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = if (minGrade != null) String.format(Locale.getDefault(), "%.1f", minGrade) else "-",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}
