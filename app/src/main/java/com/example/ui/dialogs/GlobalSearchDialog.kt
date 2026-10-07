package com.example.ui.dialogs

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.text.style.TextAlign
import com.example.data.model.SchoolClass
import com.example.data.model.Student
import com.example.data.model.Teacher
import com.example.ui.components.StudentAvatar
import com.example.ui.viewmodel.SchoolViewModel

enum class SearchCategoryFilter {
    ALL, STUDENTS, TEACHERS, CLASSES
}

@Composable
fun RealTimeSearchResultsView(
    searchQuery: String,
    selectedFilter: SearchCategoryFilter,
    onFilterChange: (SearchCategoryFilter) -> Unit,
    viewModel: SchoolViewModel,
    onSelectStudent: (Student) -> Unit,
    onSelectClass: (SchoolClass) -> Unit,
    onSelectTeacher: (Teacher) -> Unit,
    onClearSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val students by viewModel.students.collectAsStateWithLifecycle()
    val teachers by viewModel.teachers.collectAsStateWithLifecycle()
    val classes by viewModel.classes.collectAsStateWithLifecycle()

    val classesMap = remember(classes) { classes.associateBy { it.id } }
    val q = searchQuery.trim()

    val matchingStudents = remember(q, students, classesMap) {
        if (q.isBlank()) emptyList()
        else students.filter {
            it.fullName.contains(q, ignoreCase = true) ||
                    it.matricule.contains(q, ignoreCase = true) ||
                    (classesMap[it.classId]?.name?.contains(q, ignoreCase = true) == true) ||
                    it.parentPhone.contains(q, ignoreCase = true) ||
                    it.parentEmail.contains(q, ignoreCase = true)
        }
    }

    val matchingTeachers = remember(q, teachers) {
        if (q.isBlank()) emptyList()
        else teachers.filter {
            it.fullName.contains(q, ignoreCase = true) ||
                    it.email.contains(q, ignoreCase = true) ||
                    it.subjectName.contains(q, ignoreCase = true) ||
                    it.phone.contains(q, ignoreCase = true)
        }
    }

    val matchingClasses = remember(q, classes) {
        if (q.isBlank()) emptyList()
        else classes.filter {
            it.name.contains(q, ignoreCase = true) ||
                    it.level.contains(q, ignoreCase = true) ||
                    it.mainTeacher.contains(q, ignoreCase = true) ||
                    it.room.contains(q, ignoreCase = true)
        }
    }

    val totalResults = matchingStudents.size + matchingTeachers.size + matchingClasses.size

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("realtime_search_results_container")
    ) {
        // Category Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedFilter == SearchCategoryFilter.ALL,
                onClick = { onFilterChange(SearchCategoryFilter.ALL) },
                label = { Text("Tous ($totalResults)") },
                modifier = Modifier.testTag("chip_topbar_filter_all")
            )
            FilterChip(
                selected = selectedFilter == SearchCategoryFilter.STUDENTS,
                onClick = { onFilterChange(SearchCategoryFilter.STUDENTS) },
                label = { Text("Élèves (${matchingStudents.size})") },
                modifier = Modifier.testTag("chip_topbar_filter_students")
            )
            FilterChip(
                selected = selectedFilter == SearchCategoryFilter.TEACHERS,
                onClick = { onFilterChange(SearchCategoryFilter.TEACHERS) },
                label = { Text("Profs (${matchingTeachers.size})") },
                modifier = Modifier.testTag("chip_topbar_filter_teachers")
            )
            FilterChip(
                selected = selectedFilter == SearchCategoryFilter.CLASSES,
                onClick = { onFilterChange(SearchCategoryFilter.CLASSES) },
                label = { Text("Classes (${matchingClasses.size})") },
                modifier = Modifier.testTag("chip_topbar_filter_classes")
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Query status summary
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recherche en direct : « $q »",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$totalResults résultat(s)",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (totalResults == 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Aucun résultat trouvé",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Aucun élève, professeur ou classe ne correspond à « $q ».",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = onClearSearch,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Effacer la recherche")
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("list_search_results"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Élèves
                if ((selectedFilter == SearchCategoryFilter.ALL || selectedFilter == SearchCategoryFilter.STUDENTS) && matchingStudents.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Élèves", count = matchingStudents.size, color = Color(0xFF047857))
                    }
                    items(matchingStudents, key = { "stu_${it.id}" }) { student ->
                        val sClass = classesMap[student.classId]
                        StudentSearchResultCard(
                            student = student,
                            className = sClass?.name ?: "Classe",
                            onClick = { onSelectStudent(student) }
                        )
                    }
                }

                // Professeurs
                if ((selectedFilter == SearchCategoryFilter.ALL || selectedFilter == SearchCategoryFilter.TEACHERS) && matchingTeachers.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Professeurs", count = matchingTeachers.size, color = Color(0xFF2563EB))
                    }
                    items(matchingTeachers, key = { "teach_${it.id}" }) { teacher ->
                        TeacherSearchResultCard(
                            teacher = teacher,
                            onClick = { onSelectTeacher(teacher) }
                        )
                    }
                }

                // Classes
                if ((selectedFilter == SearchCategoryFilter.ALL || selectedFilter == SearchCategoryFilter.CLASSES) && matchingClasses.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Classes", count = matchingClasses.size, color = Color(0xFF7C3AED))
                    }
                    items(matchingClasses, key = { "class_${it.id}" }) { schoolClass ->
                        val studentCount = students.count { it.classId == schoolClass.id }
                        ClassSearchResultCard(
                            schoolClass = schoolClass,
                            studentCount = studentCount,
                            onClick = { onSelectClass(schoolClass) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GlobalSearchDialog(
    viewModel: SchoolViewModel,
    onDismiss: () -> Unit,
    onSelectStudent: (Student) -> Unit,
    onSelectClass: (SchoolClass) -> Unit,
    onSelectTeacher: (Teacher) -> Unit
) {
    val students by viewModel.students.collectAsStateWithLifecycle()
    val teachers by viewModel.teachers.collectAsStateWithLifecycle()
    val classes by viewModel.classes.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(SearchCategoryFilter.ALL) }
    val focusRequester = remember { FocusRequester() }

    val classesMap = remember(classes) { classes.associateBy { it.id } }

    val q = searchQuery.trim()

    val matchingStudents = remember(q, students) {
        if (q.isBlank()) emptyList()
        else students.filter {
            it.fullName.contains(q, ignoreCase = true) ||
                    it.matricule.contains(q, ignoreCase = true) ||
                    (classesMap[it.classId]?.name?.contains(q, ignoreCase = true) == true)
        }
    }

    val matchingTeachers = remember(q, teachers) {
        if (q.isBlank()) emptyList()
        else teachers.filter {
            it.fullName.contains(q, ignoreCase = true) ||
                    it.email.contains(q, ignoreCase = true) ||
                    it.subjectName.contains(q, ignoreCase = true)
        }
    }

    val matchingClasses = remember(q, classes) {
        if (q.isBlank()) emptyList()
        else classes.filter {
            it.name.contains(q, ignoreCase = true) ||
                    it.level.contains(q, ignoreCase = true) ||
                    it.mainTeacher.contains(q, ignoreCase = true) ||
                    it.room.contains(q, ignoreCase = true)
        }
    }

    val totalResults = matchingStudents.size + matchingTeachers.size + matchingClasses.size

    LaunchedEffect(Unit) {
        try {
            focusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .testTag("dialog_global_search"),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top Search Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester)
                            .testTag("input_global_search"),
                        placeholder = { Text("Rechercher élève, professeur, classe...") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { searchQuery = "" },
                                    modifier = Modifier.testTag("btn_clear_global_search")
                                ) {
                                    Icon(Icons.Default.Clear, contentDescription = "Effacer")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_global_search")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Category Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == SearchCategoryFilter.ALL,
                        onClick = { selectedFilter = SearchCategoryFilter.ALL },
                        label = { Text("Tous ($totalResults)") },
                        modifier = Modifier.testTag("chip_filter_all")
                    )
                    FilterChip(
                        selected = selectedFilter == SearchCategoryFilter.STUDENTS,
                        onClick = { selectedFilter = SearchCategoryFilter.STUDENTS },
                        label = { Text("Élèves (${matchingStudents.size})") },
                        modifier = Modifier.testTag("chip_filter_students")
                    )
                    FilterChip(
                        selected = selectedFilter == SearchCategoryFilter.TEACHERS,
                        onClick = { selectedFilter = SearchCategoryFilter.TEACHERS },
                        label = { Text("Profs (${matchingTeachers.size})") },
                        modifier = Modifier.testTag("chip_filter_teachers")
                    )
                    FilterChip(
                        selected = selectedFilter == SearchCategoryFilter.CLASSES,
                        onClick = { selectedFilter = SearchCategoryFilter.CLASSES },
                        label = { Text("Classes (${matchingClasses.size})") },
                        modifier = Modifier.testTag("chip_filter_classes")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Results List
                if (q.isBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Recherche Globale en Temps Réel",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Saisissez un nom d'élève, un matricule, un professeur ou une classe.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else if (totalResults == 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Aucun résultat trouvé pour « $q »",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Vérifiez l'orthographe ou essayez un mot-clé plus court.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("list_search_results"),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Section: Élèves
                        if ((selectedFilter == SearchCategoryFilter.ALL || selectedFilter == SearchCategoryFilter.STUDENTS) && matchingStudents.isNotEmpty()) {
                            item {
                                SectionHeader(title = "Élèves", count = matchingStudents.size, color = Color(0xFF047857))
                            }
                            items(matchingStudents, key = { "stu_${it.id}" }) { student ->
                                val sClass = classesMap[student.classId]
                                StudentSearchResultCard(
                                    student = student,
                                    className = sClass?.name ?: "Classe",
                                    onClick = { onSelectStudent(student) }
                                )
                            }
                        }

                        // Section: Professeurs
                        if ((selectedFilter == SearchCategoryFilter.ALL || selectedFilter == SearchCategoryFilter.TEACHERS) && matchingTeachers.isNotEmpty()) {
                            item {
                                SectionHeader(title = "Professeurs", count = matchingTeachers.size, color = Color(0xFF2563EB))
                            }
                            items(matchingTeachers, key = { "teach_${it.id}" }) { teacher ->
                                TeacherSearchResultCard(
                                    teacher = teacher,
                                    onClick = { onSelectTeacher(teacher) }
                                )
                            }
                        }

                        // Section: Classes
                        if ((selectedFilter == SearchCategoryFilter.ALL || selectedFilter == SearchCategoryFilter.CLASSES) && matchingClasses.isNotEmpty()) {
                            item {
                                SectionHeader(title = "Classes", count = matchingClasses.size, color = Color(0xFF7C3AED))
                            }
                            items(matchingClasses, key = { "class_${it.id}" }) { schoolClass ->
                                val studentCount = students.count { it.classId == schoolClass.id }
                                ClassSearchResultCard(
                                    schoolClass = schoolClass,
                                    studentCount = studentCount,
                                    onClick = { onSelectClass(schoolClass) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, count: Int, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.width(6.dp))
        Badge(containerColor = color.copy(alpha = 0.2f), contentColor = color) {
            Text("$count", fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun StudentSearchResultCard(
    student: Student,
    className: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("search_result_student_${student.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StudentAvatar(
                name = student.fullName,
                colorHex = student.avatarColorHex,
                size = 38
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.fullName,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Matricule : ${student.matricule} • Classe : $className",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun TeacherSearchResultCard(
    teacher: Teacher,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("search_result_teacher_${teacher.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2563EB).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = Color(0xFF2563EB),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = teacher.fullName,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Matière : ${teacher.subjectName} • ${teacher.email}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun ClassSearchResultCard(
    schoolClass: SchoolClass,
    studentCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("search_result_class_${schoolClass.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF7C3AED).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.School,
                    contentDescription = null,
                    tint = Color(0xFF7C3AED),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = schoolClass.name,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Niveau : ${schoolClass.level} • $studentCount élèves • ${schoolClass.room}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
