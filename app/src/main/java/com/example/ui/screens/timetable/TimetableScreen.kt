package com.example.ui.screens.timetable

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.material3.Button
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
import androidx.compose.material3.PrimaryScrollableTabRow
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AcademicCalendarEvent
import com.example.data.model.TimetableSlot
import com.example.data.model.UserRole
import com.example.ui.components.ClassFilterRow
import com.example.ui.components.EmptyStateView
import com.example.ui.dialogs.AddCalendarEventDialog
import com.example.ui.viewmodel.SchoolViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen(
    viewModel: SchoolViewModel,
    modifier: Modifier = Modifier
) {
    val timetableSlots by viewModel.timetableSlots.collectAsStateWithLifecycle()
    val calendarEvents by viewModel.academicCalendarEvents.collectAsStateWithLifecycle()
    val classes by viewModel.classes.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val selectedClassId by viewModel.selectedClassId.collectAsStateWithLifecycle()
    val selectedDayOfWeek by viewModel.selectedDayOfWeek.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    val isScolariteOrAdmin = currentUser.role in listOf(UserRole.ADMIN, UserRole.DIRECTION, UserRole.SUPER_ADMIN)
    var selectedMainTab by remember { mutableIntStateOf(0) } // 0: Hebdomadaire, 1: Calendrier Annuel
    val showAddCalendarDialog by viewModel.showAddCalendarEventDialog.collectAsStateWithLifecycle()

    val days = listOf(
        1 to "Lundi",
        2 to "Mardi",
        3 to "Mercredi",
        4 to "Jeudi",
        5 to "Vendredi",
        6 to "Samedi"
    )

    val subjectsMap = remember(subjects) { subjects.associateBy { it.id } }
    val classesMap = remember(classes) { classes.associateBy { it.id } }

    val daySlots = remember(timetableSlots, selectedClassId, selectedDayOfWeek) {
        timetableSlots
            .filter { slot ->
                val matchDay = slot.dayOfWeek == selectedDayOfWeek
                val matchClass = selectedClassId == null || slot.classId == selectedClassId
                matchDay && matchClass
            }
            .sortedBy { it.startTime }
    }

    // State for annual calendar filtering
    var selectedCalendarCategory by remember { mutableStateOf("TOUS") }
    val filteredCalendarEvents = remember(calendarEvents, selectedCalendarCategory) {
        if (selectedCalendarCategory == "TOUS") calendarEvents
        else calendarEvents.filter { it.category == selectedCalendarCategory }
    }

    Scaffold(
        modifier = modifier.testTag("timetable_screen"),
        floatingActionButton = {
            if (isScolariteOrAdmin) {
                if (selectedMainTab == 0) {
                    FloatingActionButton(
                        onClick = { viewModel.setAddTimetableDialogOpen(true) },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.testTag("fab_add_course_slot")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Ajouter un cours (Scolarité)")
                    }
                } else {
                    ExtendedFloatingActionButton(
                        onClick = { viewModel.setAddCalendarEventDialogOpen(true) },
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.testTag("fab_add_calendar_event")
                    ) {
                        Icon(Icons.Default.Event, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ajouter une Date Clé", fontWeight = FontWeight.Bold)
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
            // Main Top Tabs: Hebdomadaire vs Annuel
            PrimaryTabRow(
                selectedTabIndex = selectedMainTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedMainTab == 0,
                    onClick = { selectedMainTab = 0 },
                    icon = { Icon(Icons.Default.ViewWeek, contentDescription = null) },
                    text = { Text("Emploi du Temps Hebdomadaire", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
                Tab(
                    selected = selectedMainTab == 1,
                    onClick = { selectedMainTab = 1 },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                    text = { Text("Calendrier Annuel & Dates Clés", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
            }

            // Role Banner explaining permissions
            Surface(
                color = if (isScolariteOrAdmin) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isScolariteOrAdmin) Icons.Default.Security else Icons.Default.Info,
                        contentDescription = null,
                        tint = if (isScolariteOrAdmin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isScolariteOrAdmin)
                            "Espace Scolarité : Seule la direction et la scolarité sont habilitées à créer, modifier et supprimer les créneaux et classes."
                        else
                            "Mode Consultation : Les professeurs et élèves ne peuvent pas modifier l'emploi du temps fixé par la Scolarité.",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isScolariteOrAdmin) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (selectedMainTab == 0) {
                // ==================== ONGLET HEBDOMADAIRE ====================
                // Class Filters
                ClassFilterRow(
                    classes = classes,
                    selectedClassId = selectedClassId,
                    onSelectClass = { viewModel.selectClass(it) },
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                // Day of Week Tabs
                PrimaryScrollableTabRow(
                    selectedTabIndex = days.indexOfFirst { it.first == selectedDayOfWeek }.coerceAtLeast(0),
                    edgePadding = 16.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    days.forEach { (dayIndex, dayName) ->
                        Tab(
                            selected = selectedDayOfWeek == dayIndex,
                            onClick = { viewModel.selectDayOfWeek(dayIndex) },
                            text = {
                                Text(
                                    text = dayName,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = if (selectedDayOfWeek == dayIndex) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            }
                        )
                    }
                }

                // Day schedule summary
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${days.find { it.first == selectedDayOfWeek }?.second} • ${daySlots.size} créneau(x) programmé(s)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Contrôle anti-conflits actif",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF16A34A),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (daySlots.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Default.CalendarToday,
                        title = "Aucun cours programmé ce jour",
                        description = if (isScolariteOrAdmin) "Ajoutez un cours à l'emploi du temps avec le bouton '+'." else "Aucun cours prévu par la scolarité pour ce jour."
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(daySlots, key = { it.id }) { slot ->
                            val subject = subjectsMap[slot.subjectId]
                            val schoolClass = classesMap[slot.classId]

                            val subjectColor = try {
                                Color(android.graphics.Color.parseColor(subject?.colorHex ?: "#1E3A8A"))
                            } catch (_: Exception) {
                                MaterialTheme.colorScheme.primary
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("timetable_slot_${slot.id}"),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Color accent bar
                                    Box(
                                        modifier = Modifier
                                            .width(8.dp)
                                            .height(96.dp)
                                            .background(subjectColor)
                                    )

                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(14.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = MaterialTheme.colorScheme.primaryContainer
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        Icons.Default.AccessTime,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "${slot.startTime} - ${slot.endTime}",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                }
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = MaterialTheme.colorScheme.surfaceVariant
                                            ) {
                                                Text(
                                                    text = schoolClass?.name ?: "Classe",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = subject?.name ?: "Matière",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.Person,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = slot.teacherName,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.MeetingRoom,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.secondary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = slot.room,
                                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                                    color = MaterialTheme.colorScheme.secondary
                                                )
                                            }
                                        }
                                    }

                                    // Only Scolarite / Admin can delete timetable slots!
                                    if (isScolariteOrAdmin) {
                                        IconButton(
                                            onClick = { viewModel.deleteTimetableSlot(slot) },
                                            modifier = Modifier.padding(end = 8.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Supprimer (Scolarité)",
                                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // ==================== ONGLET CALENDRIER ANNUEL ====================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(10.dp))

                    // Category Filter Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "TOUS" to "Tous les événements",
                            "TRIMESTRE" to "Trimestres",
                            "VACANCES" to "Vacances & Congés",
                            "EXAMEN" to "Examens"
                        ).forEach { (catKey, catLabel) ->
                            FilterChip(
                                selected = selectedCalendarCategory == catKey,
                                onClick = { selectedCalendarCategory = catKey },
                                label = { Text(catLabel, fontSize = 12.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Année Scolaire en cours • ${filteredCalendarEvents.size} dates importantes",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (filteredCalendarEvents.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Default.DateRange,
                            title = "Aucune date clé trouvée",
                            description = "La Scolarité publiera le calendrier des trimestres et vacances."
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 80.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredCalendarEvents, key = { it.id }) { event ->
                                AcademicCalendarEventCard(
                                    event = event,
                                    canDelete = isScolariteOrAdmin,
                                    onDelete = { viewModel.deleteCalendarEvent(event) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddCalendarDialog) {
        AddCalendarEventDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.setAddCalendarEventDialogOpen(false) }
        )
    }
}

@Composable
fun AcademicCalendarEventCard(
    event: AcademicCalendarEvent,
    canDelete: Boolean,
    onDelete: () -> Unit
) {
    val (badgeBg, badgeText, catLabel) = when (event.category) {
        "VACANCES" -> Triple(Color(0xFFFEF3C7), Color(0xFFD97706), "Vacances & Congés")
        "EXAMEN" -> Triple(Color(0xFFFEE2E2), Color(0xFFDC2626), "Examens & Évaluations")
        "TRIMESTRE" -> Triple(Color(0xFFDBEAFE), Color(0xFF2563EB), "Période Pédagogique")
        "FERIE" -> Triple(Color(0xFFF3E8FF), Color(0xFF7C3AED), "Jour Férié")
        else -> Triple(Color(0xFFE0E7FF), Color(0xFF4F46E5), "Événement Scolaire")
    }

    ElevatedCard(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(badgeBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (event.category) {
                        "VACANCES" -> Icons.Default.CalendarMonth
                        "EXAMEN" -> Icons.Default.School
                        else -> Icons.Default.Event
                    },
                    contentDescription = null,
                    tint = badgeText,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(badgeBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = catLabel,
                            color = badgeText,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (event.endDate.isNotBlank() && event.endDate != event.startDate)
                            "Du ${event.startDate} au ${event.endDate}"
                        else "Date : ${event.startDate}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (event.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = event.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (canDelete) {
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Supprimer",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
