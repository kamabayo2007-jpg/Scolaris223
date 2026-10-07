package com.example.ui.screens.students

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Student
import com.example.data.model.StudentOverview
import com.example.ui.components.ClassFilterRow
import com.example.ui.components.EmptyStateView
import com.example.ui.components.GradeBadge
import com.example.ui.components.StudentAvatar
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusDangerContainer
import com.example.ui.theme.StatusDangerText
import com.example.ui.theme.StatusInfo
import com.example.ui.theme.StatusInfoContainer
import com.example.ui.theme.StatusInfoText
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusSuccessContainer
import com.example.ui.theme.StatusSuccessText
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.StatusWarningContainer
import com.example.ui.theme.StatusWarningText
import com.example.util.PdfExportUtil
import com.example.ui.viewmodel.SchoolViewModel

enum class StudentStatusFilter(val label: String) {
    ALL("Tous les statuts"),
    EXEMPLAIRE("Assiduité exemplaire"),
    ABSENCES("Absences à justifier"),
    RETARDS("Retards constatés"),
    EXCELLENCE("Excellence (≥ 14)"),
    DIFFICULTY("Soutien requis (< 10)")
}

enum class StudentSortColumn {
    NAME_ASC,
    NAME_DESC,
    CLASS_ASC,
    CLASS_DESC,
    STATUS_ASC,
    AVERAGE_DESC,
    AVERAGE_ASC
}

@Composable
fun StudentsScreen(
    viewModel: SchoolViewModel,
    modifier: Modifier = Modifier
) {
    val overviews by viewModel.studentsOverview.collectAsStateWithLifecycle()
    val classes by viewModel.classes.collectAsStateWithLifecycle()
    val selectedClassId by viewModel.selectedClassId.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Column filtering & sorting states
    var statusFilter by remember { mutableStateOf(StudentStatusFilter.ALL) }
    var sortColumn by remember { mutableStateOf(StudentSortColumn.NAME_ASC) }
    var isDetailedTableView by remember { mutableStateOf(true) }

    // Filter by Status & Sort
    val filteredStudents = remember(overviews, statusFilter, sortColumn) {
        val filtered = overviews.filter { overview ->
            when (statusFilter) {
                StudentStatusFilter.ALL -> true
                StudentStatusFilter.EXEMPLAIRE -> overview.absencesCount == 0 && overview.latesCount == 0
                StudentStatusFilter.ABSENCES -> overview.unjustifiedAbsencesCount > 0
                StudentStatusFilter.RETARDS -> overview.latesCount > 0
                StudentStatusFilter.EXCELLENCE -> (overview.generalAverage ?: 0.0) >= 14.0
                StudentStatusFilter.DIFFICULTY -> (overview.generalAverage ?: 0.0) < 10.0 && overview.generalAverage != null
            }
        }

        when (sortColumn) {
            StudentSortColumn.NAME_ASC -> filtered.sortedBy { it.student.lastName + " " + it.student.firstName }
            StudentSortColumn.NAME_DESC -> filtered.sortedByDescending { it.student.lastName + " " + it.student.firstName }
            StudentSortColumn.CLASS_ASC -> filtered.sortedBy { it.className }
            StudentSortColumn.CLASS_DESC -> filtered.sortedByDescending { it.className }
            StudentSortColumn.STATUS_ASC -> filtered.sortedBy { it.unjustifiedAbsencesCount }
            StudentSortColumn.AVERAGE_DESC -> filtered.sortedByDescending { it.generalAverage ?: -1.0 }
            StudentSortColumn.AVERAGE_ASC -> filtered.sortedBy { it.generalAverage ?: 99.0 }
        }
    }

    Scaffold(
        modifier = modifier.testTag("students_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.setAddStudentDialogOpen(true) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_student")
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Ajouter un élève")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search field (Filter by Name)
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .testTag("input_search_student"),
                placeholder = { Text("Filtrer par nom, prénom ou matricule...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Effacer")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )

            // Class filter row (Filter by Class)
            ClassFilterRow(
                classes = classes,
                selectedClassId = selectedClassId,
                onSelectClass = { viewModel.selectClass(it) },
                modifier = Modifier.padding(bottom = 2.dp)
            )

            // Status filter chips row (Filter by Status)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp)
                    .testTag("row_status_filters"),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(StudentStatusFilter.values()) { filter ->
                    val isSelected = statusFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { statusFilter = filter },
                        label = { Text(filter.label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("filter_chip_status_${filter.name.lowercase()}")
                    )
                }
            }

            // Toolbar: Results Count, View Toggle (Detailed Table vs Cards) & Export PDF
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredStudents.size} élève(s) répertorié(s)",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Toggle Detailed Table View / Card View
                    IconButton(
                        onClick = { isDetailedTableView = !isDetailedTableView },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("btn_toggle_view_mode")
                    ) {
                        Icon(
                            imageVector = if (isDetailedTableView) Icons.Default.ViewAgenda else Icons.Default.TableChart,
                            contentDescription = if (isDetailedTableView) "Vue Cartes" else "Vue Tableau Détaillé",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Quick export all visible students to PDF
                    OutlinedButton(
                        onClick = {
                            val selectedClass = classes.find { it.id == selectedClassId }
                            val fakeAttendances = emptyList<com.example.data.model.Attendance>()
                            val file = com.example.util.PdfExportUtil.exportAttendanceReportPdf(
                                context = context,
                                schoolClass = selectedClass,
                                attendances = fakeAttendances,
                                students = filteredStudents.map { it.student }
                            )
                            if (file != null) {
                                com.example.util.PdfExportUtil.shareOrViewPdf(context, file, "Registre des Élèves")
                            } else {
                                Toast.makeText(context, "Erreur lors de l'exportation du PDF", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.testTag("btn_export_students_pdf"),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export PDF", fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Button(
                        onClick = { viewModel.setStudentCardDialogOpen(true) },
                        modifier = Modifier.testTag("btn_open_student_cards"),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cartes & Scanner", fontSize = 11.sp)
                    }
                }
            }

            if (filteredStudents.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.People,
                    title = "Aucun élève trouvé",
                    description = "Aucun élève ne correspond aux filtres appliqués (Classe, Nom ou Statut)."
                )
            } else if (isDetailedTableView) {
                // === TABLEAU DÉTAILLÉ AVEC COLONNES FILTRABLES & TRIABLES ===
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .testTag("detailed_students_table_view")
                ) {
                    // En-tête des colonnes interactives (Classe, Nom, Statut, Moyenne)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Colonne Nom (Sortable)
                            Row(
                                modifier = Modifier
                                    .weight(1.8f)
                                    .clickable {
                                        sortColumn = if (sortColumn == StudentSortColumn.NAME_ASC) {
                                            StudentSortColumn.NAME_DESC
                                        } else {
                                            StudentSortColumn.NAME_ASC
                                        }
                                    }
                                    .testTag("col_header_name"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Élève / Nom",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    imageVector = if (sortColumn == StudentSortColumn.NAME_DESC) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = if (sortColumn == StudentSortColumn.NAME_ASC || sortColumn == StudentSortColumn.NAME_DESC) MaterialTheme.colorScheme.primary else Color.Transparent
                                )
                            }

                            // Colonne Classe (Sortable)
                            Row(
                                modifier = Modifier
                                    .weight(1.0f)
                                    .clickable {
                                        sortColumn = if (sortColumn == StudentSortColumn.CLASS_ASC) {
                                            StudentSortColumn.CLASS_DESC
                                        } else {
                                            StudentSortColumn.CLASS_ASC
                                        }
                                    }
                                    .testTag("col_header_class"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Classe",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    imageVector = if (sortColumn == StudentSortColumn.CLASS_DESC) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = if (sortColumn == StudentSortColumn.CLASS_ASC || sortColumn == StudentSortColumn.CLASS_DESC) MaterialTheme.colorScheme.primary else Color.Transparent
                                )
                            }

                            // Colonne Statut (Filterable)
                            Text(
                                text = "Statut",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier
                                    .weight(1.2f)
                                    .testTag("col_header_status")
                            )

                            // Colonne Moyenne / Actions
                            Text(
                                text = "Actions",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier
                                    .weight(1.0f)
                                    .testTag("col_header_actions")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Lignes détaillées du tableau
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(filteredStudents, key = { it.student.id }) { overview ->
                            val student = overview.student
                            DetailedStudentRow(
                                overview = overview,
                                onClick = { viewModel.showStudentDetail(student) },
                                onOpenBulletin = { viewModel.openBulletin(student) },
                                onOpenCard = { viewModel.openStudentCard(student) },
                                onExportPdf = {
                                    val report = viewModel.generateOfficialBulletin(student, "Trimestre 1")
                                    val file = com.example.util.PdfExportUtil.exportBulletinPdf(context, report)
                                    if (file != null) {
                                        com.example.util.PdfExportUtil.shareOrViewPdf(context, file, "Bulletin Officiel - ${student.fullName}")
                                    } else {
                                        Toast.makeText(context, "Erreur génération du PDF", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    }
                }
            } else {
                // === VUE CARTES ALTERNATIVE ===
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("cards_students_view"),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredStudents, key = { it.student.id }) { overview ->
                        val student = overview.student
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.showStudentDetail(student) }
                                .testTag("student_item_${student.id}"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "${overview.className} • Matr: ${student.matricule}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        // Status Chip
                                        StudentStatusBadge(overview = overview)
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (overview.generalAverage != null) {
                                        GradeBadge(gradeValue = overview.generalAverage, outOf = 20.0)
                                    }

                                    IconButton(
                                        onClick = { viewModel.openStudentCard(student) },
                                        modifier = Modifier.testTag("btn_student_card_${student.id}")
                                    ) {
                                        Icon(
                                            Icons.Default.CreditCard,
                                            contentDescription = "Carte Scolaire",
                                            tint = MaterialTheme.colorScheme.tertiary
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.openBulletin(student) },
                                        modifier = Modifier.testTag("btn_student_bulletin_${student.id}")
                                    ) {
                                        Icon(
                                            Icons.Default.Description,
                                            contentDescription = "Bulletin",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            val report = viewModel.generateOfficialBulletin(student, "Trimestre 1")
                                            val file = com.example.util.PdfExportUtil.exportBulletinPdf(context, report)
                                            if (file != null) {
                                                com.example.util.PdfExportUtil.shareOrViewPdf(context, file, "Bulletin PDF")
                                            }
                                        },
                                        modifier = Modifier.testTag("btn_export_pdf_${student.id}")
                                    ) {
                                        Icon(
                                            Icons.Default.Download,
                                            contentDescription = "PDF",
                                            tint = MaterialTheme.colorScheme.secondary
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
}

/**
 * Ligne de données pour le tableau détaillé des élèves avec colonnes 'Nom', 'Classe', 'Statut' et 'Actions'.
 */
@Composable
fun DetailedStudentRow(
    overview: StudentOverview,
    onClick: () -> Unit,
    onOpenBulletin: () -> Unit,
    onOpenCard: () -> Unit,
    onExportPdf: () -> Unit,
    modifier: Modifier = Modifier
) {
    val student = overview.student
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("row_student_${student.id}"),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Colonne Nom & Matricule
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1.8f)
            ) {
                StudentAvatar(name = student.fullName, colorHex = student.avatarColorHex, size = 32)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = student.fullName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = student.matricule,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 2. Colonne Classe
            Box(modifier = Modifier.weight(1.0f)) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = overview.className,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        maxLines = 1
                    )
                }
            }

            // 3. Colonne Statut
            Box(modifier = Modifier.weight(1.2f)) {
                StudentStatusBadge(overview = overview, compact = true)
            }

            // 4. Colonne Actions & Moyenne
            Row(
                modifier = Modifier.weight(1.1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                if (overview.generalAverage != null) {
                    GradeBadge(gradeValue = overview.generalAverage, outOf = 20.0)
                    Spacer(modifier = Modifier.width(4.dp))
                }

                IconButton(
                    onClick = onOpenCard,
                    modifier = Modifier.size(28.dp).testTag("btn_table_card_${student.id}")
                ) {
                    Icon(
                        Icons.Default.CreditCard,
                        contentDescription = "Carte Scolaire",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onOpenBulletin,
                    modifier = Modifier.size(28.dp).testTag("btn_table_bulletin_${student.id}")
                ) {
                    Icon(
                        Icons.Default.Description,
                        contentDescription = "Bulletin",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onExportPdf,
                    modifier = Modifier.size(28.dp).testTag("btn_table_pdf_${student.id}")
                ) {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = "Exporter PDF",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StudentStatusBadge(
    overview: StudentOverview,
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    val (label, containerColor, textColor) = when {
        overview.unjustifiedAbsencesCount > 0 -> Triple(
            if (compact) "${overview.unjustifiedAbsencesCount} abs." else "${overview.unjustifiedAbsencesCount} abs. non justifiée(s)",
            StatusDangerContainer,
            StatusDangerText
        )
        overview.latesCount > 0 -> Triple(
            if (compact) "${overview.latesCount} ret." else "${overview.latesCount} retard(s)",
            StatusWarningContainer,
            StatusWarningText
        )
        overview.absencesCount > 0 -> Triple(
            if (compact) "${overview.absencesCount} just." else "${overview.absencesCount} abs. justifiée(s)",
            StatusInfoContainer,
            StatusInfoText
        )
        else -> Triple(
            if (compact) "Assidu" else "Assiduité parfaite",
            StatusSuccessContainer,
            StatusSuccessText
        )
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = containerColor,
        modifier = modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
