package com.example.ui.dialogs

import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AttendanceStatus
import com.example.data.model.School
import com.example.data.model.SchoolClass
import com.example.data.model.Student
import com.example.ui.viewmodel.SchoolViewModel
import com.example.util.PdfExportUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentCardDialog(
    viewModel: SchoolViewModel,
    initialStudent: Student? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val allStudents by viewModel.students.collectAsStateWithLifecycle()
    val classes by viewModel.classes.collectAsStateWithLifecycle()
    val schools by viewModel.schools.collectAsStateWithLifecycle()

    val currentSchool: School = remember(schools) {
        schools.firstOrNull() ?: School(
            id = 1L,
            name = "Groupe Scolaire Excellence de Bamako",
            code = "GSE-BKO",
            city = "Bamako",
            country = "Mali",
            academyName = "Académie d'Enseignement de Bamako Rive Droite",
            academicYear = "2025-2026",
            directorName = "Dr. Robert Kouassi"
        )
    }

    val classesMap = remember(classes) { classes.associateBy { it.id } }

    // Étudiant courant affiché
    var currentStudentIndex by remember(allStudents, initialStudent) {
        val idx = if (initialStudent != null) allStudents.indexOfFirst { it.id == initialStudent.id } else 0
        mutableIntStateOf(if (idx >= 0) idx else 0)
    }

    val student = allStudents.getOrNull(currentStudentIndex) ?: initialStudent

    // Onglets : 0 = Badge Scolaire, 1 = Scanner de Carte
    var selectedTab by remember { mutableIntStateOf(0) }

    // État pour édition rapide du numéro d'examen par la Scolarité
    var showEditExamNumberDialog by remember { mutableStateOf(false) }
    var examNumberInput by remember(student) { mutableStateOf(student?.academyExamNumber ?: "") }

    // Scanner state
    var scanQuery by remember { mutableStateOf("") }
    var scannedResultStudent by remember { mutableStateOf<Student?>(null) }
    var lastAttendanceActionMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .widthIn(max = 560.dp)
                .clip(RoundedCornerShape(24.dp))
                .testTag("dialog_student_card"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Barre de titre
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.School,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Carte Scolaire Officielle",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Badge d'identité & Scan appel — Mali",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_student_card_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Onglets Badge vs Scanner
                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Badge Scolaire")
                            }
                        },
                        modifier = Modifier.testTag("tab_badge_view")
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Scanner de Carte")
                            }
                        },
                        modifier = Modifier.testTag("tab_scanner_view")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedTab == 0) {
                    // ==========================================
                    // VUE 1 : CARTE SCOLAIRE / BADGE PHYSIQUE
                    // ==========================================
                    if (student == null) {
                        Text(
                            text = "Aucun élève sélectionné",
                            modifier = Modifier.padding(24.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        val schoolClass = classesMap[student.classId]

                        // Navigation entre élèves
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    if (currentStudentIndex > 0) currentStudentIndex--
                                    else currentStudentIndex = allStudents.size - 1
                                },
                                enabled = allStudents.size > 1
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Élève précédent")
                            }

                            Text(
                                text = "Élève ${currentStudentIndex + 1} / ${allStudents.size}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            IconButton(
                                onClick = {
                                    if (currentStudentIndex < allStudents.size - 1) currentStudentIndex++
                                    else currentStudentIndex = 0
                                },
                                enabled = allStudents.size > 1
                            ) {
                                Icon(Icons.Default.ArrowForward, contentDescription = "Élève suivant")
                            }
                        }

                        // BADGE COMPOSANT VISUEL
                        Column(
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            StudentBadgeCardVisual(
                                student = student,
                                schoolClass = schoolClass,
                                school = currentSchool,
                                onEditExamNumber = {
                                    examNumberInput = student.academyExamNumber
                                    showEditExamNumberDialog = true
                                }
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Boutons d'action : Imprimer & Télécharger PDF
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val file = PdfExportUtil.exportStudentCardPdf(context, student, schoolClass, currentSchool)
                                        if (file != null) {
                                            PdfExportUtil.printPdf(context, file, "Carte Scolaire - ${student.fullName}")
                                        } else {
                                            Toast.makeText(context, "Erreur lors de la génération de la carte", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("btn_print_student_card"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Imprimer")
                                }

                                OutlinedButton(
                                    onClick = {
                                        val file = PdfExportUtil.exportStudentCardPdf(context, student, schoolClass, currentSchool)
                                        if (file != null) {
                                            PdfExportUtil.shareOrViewPdf(context, file, "Carte Scolaire - ${student.fullName}")
                                        } else {
                                            Toast.makeText(context, "Erreur génération du PDF", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("btn_export_student_card_pdf"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Export PDF")
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Export par lot de toute la classe
                            OutlinedButton(
                                onClick = {
                                    val classStudents = allStudents.filter { it.classId == student.classId }
                                    val file = PdfExportUtil.exportClassStudentCardsPdf(
                                        context = context,
                                        students = classStudents,
                                        classesMap = classesMap,
                                        school = currentSchool
                                    )
                                    if (file != null) {
                                        PdfExportUtil.shareOrViewPdf(context, file, "Planche Cartes Scolaires - ${schoolClass?.name ?: ""}")
                                    } else {
                                        Toast.makeText(context, "Erreur export des cartes de la classe", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_export_class_cards_pdf"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Imprimer toutes les cartes de la classe (${schoolClass?.name ?: ""})", fontSize = 12.sp)
                            }
                        }
                    }
                } else {
                    // ==========================================
                    // VUE 2 : SCANNER DE CARTE (APPEL & RECHERCHE)
                    // ==========================================
                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Viseur de scan animé
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                // Animation du laser de scan
                                val infiniteTransition = rememberInfiniteTransition(label = "scan_laser")
                                val laserY by infiniteTransition.animateFloat(
                                    initialValue = 0.1f,
                                    targetValue = 0.9f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(1400, easing = FastOutSlowInEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "laser_y"
                                )

                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val w = size.width
                                    val h = size.height

                                    // Coins du viseur de scan
                                    val cornerSize = 24.dp.toPx()
                                    val strokeW = 3.dp.toPx()
                                    val pad = 24.dp.toPx()

                                    // Ligne rouge laser animée
                                    val lineY = h * laserY
                                    drawLine(
                                        color = Color(0xFFEF4444),
                                        start = Offset(pad, lineY),
                                        end = Offset(w - pad, lineY),
                                        strokeWidth = 2.dp.toPx()
                                    )
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Icon(
                                        Icons.Default.QrCodeScanner,
                                        contentDescription = null,
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Viseur Scanner Actif",
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "Positionnez la carte scolaire ou tapez le matricule",
                                        color = Color(0xFF94A3B8),
                                        style = MaterialTheme.typography.bodySmall,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Champ de saisie / scan instantané
                        OutlinedTextField(
                            value = scanQuery,
                            onValueChange = { query: String ->
                                scanQuery = query
                                if (query.isNotBlank()) {
                                    val found = viewModel.findStudentByBarcodeOrQr(query)
                                    if (found != null) {
                                        scannedResultStudent = found
                                        lastAttendanceActionMessage = null
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_scan_student_card"),
                            placeholder = { Text("Ex: TS1-001 ou flasher le QR code...") },
                            label = { Text("Donnée du Scanner / Matricule") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null)
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        val found = viewModel.findStudentByBarcodeOrQr(scanQuery)
                                        scannedResultStudent = found
                                        if (found == null) {
                                            Toast.makeText(context, "Aucun élève trouvé pour ce code", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = "Rechercher",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Simulation de scan rapide en 1 clic
                        Text(
                            text = "Badges à tester en 1 clic (Simulation Scanner) :",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            allStudents.take(3).forEach { st ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            scanQuery = st.matricule
                                            scannedResultStudent = st
                                            lastAttendanceActionMessage = null
                                        }
                                ) {
                                    Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(st.firstName, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), maxLines = 1)
                                        Text(st.matricule, style = MaterialTheme.typography.bodySmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Résultat du scan & Fiche de présence instantanée
                        val scanned = scannedResultStudent
                        if (scanned != null) {
                            val scannedClass = classesMap[scanned.classId]

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("scanned_student_card_result"),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = CircleShape,
                                                color = try { Color(android.graphics.Color.parseColor(scanned.avatarColorHex)) } catch (_: Exception) { MaterialTheme.colorScheme.primary },
                                                modifier = Modifier.size(42.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = "${scanned.firstName.take(1)}${scanned.lastName.take(1)}",
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = scanned.fullName,
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                                )
                                                Text(
                                                    text = "${scannedClass?.name ?: "Classe"} • Matricule : ${scanned.matricule}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        // Badge Examen
                                        if (scanned.isExamCandidate) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (scanned.academyExamNumber.isNotBlank()) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                                            ) {
                                                Text(
                                                    text = if (scanned.academyExamNumber.isNotBlank()) "Candidat BAC/DEF" else "Examen en attente",
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (scanned.academyExamNumber.isNotBlank()) Color(0xFF166534) else Color(0xFFB45309)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Action 1 : FAIRE L'APPEL EN 1 CLIC DEPUIS LE SCAN
                                    Text(
                                        text = "Faire l'appel immédiat pour aujourd'hui :",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                viewModel.recordQuickAttendanceFromScan(scanned, AttendanceStatus.PRESENT)
                                                lastAttendanceActionMessage = "✓ ${scanned.fullName} marqué PRÉSENT avec succès"
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("btn_mark_present_scan"),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("Présent", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = {
                                                viewModel.recordQuickAttendanceFromScan(scanned, AttendanceStatus.LATE)
                                                lastAttendanceActionMessage = "⚠ ${scanned.fullName} marqué EN RETARD"
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("btn_mark_late_scan"),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("Retard", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = {
                                                viewModel.recordQuickAttendanceFromScan(scanned, AttendanceStatus.ABSENT_UNJUSTIFIED)
                                                lastAttendanceActionMessage = "✗ ${scanned.fullName} marqué ABSENT"
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("btn_mark_absent_scan"),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("Absent", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    // Message de confirmation de l'appel
                                    if (lastAttendanceActionMessage != null) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surface,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = Color(0xFF16A34A),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = lastAttendanceActionMessage ?: "",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Bouton pour basculer sur sa carte scolaire
                                    OutlinedButton(
                                        onClick = {
                                            val idx = allStudents.indexOfFirst { it.id == scanned.id }
                                            if (idx >= 0) currentStudentIndex = idx
                                            selectedTab = 0
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Afficher son Badge d'Identité Scolaire", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogue d'attribution du numéro d'examen par la Scolarité
    if (showEditExamNumberDialog && student != null) {
        Dialog(onDismissRequest = { showEditExamNumberDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Attribution N° Académie — Examen",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Pour les élèves en classe d'examen (DEF, BAC), le numéro officiel est délivré par l'Académie après proclamation des listes d'admission.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = examNumberInput,
                        onValueChange = { examNumberInput = it },
                        label = { Text("Numéro de table Académie") },
                        placeholder = { Text("Ex: BAC-BKO-2026-8841") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { showEditExamNumberDialog = false },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Annuler")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val updated = student.copy(
                                    isExamCandidate = true,
                                    academyExamNumber = examNumberInput.trim()
                                )
                                viewModel.updateStudent(updated)
                                showEditExamNumberDialog = false
                                Toast.makeText(context, "Numéro d'examen enregistré", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Valider")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Rendu visuel d'un badge de carte scolaire proportionné (Format tour de cou / main).
 */
@Composable
fun StudentBadgeCardVisual(
    student: Student,
    schoolClass: SchoolClass?,
    school: School,
    onEditExamNumber: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .testTag("visual_student_badge"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFCBD5E1))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Fente pour cordon tour de cou (Neck lanyard hole slot)
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier
                    .width(46.dp)
                    .height(9.dp),
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFFE2E8F0),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF94A3B8))
            ) {}
            Spacer(modifier = Modifier.height(6.dp))

            // 2. Drapeau officiel du Mali (Vert - Jaune - Rouge)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
            ) {
                Box(modifier = Modifier.weight(1f).height(6.dp).background(Color(0xFF16A34A)))
                Box(modifier = Modifier.weight(1f).height(6.dp).background(Color(0xFFEAB308)))
                Box(modifier = Modifier.weight(1f).height(6.dp).background(Color(0xFFDC2626)))
            }

            // 3. Bandeau d'en-tête bleu marine institutionnel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF1E3A8A), Color(0xFF0F172A))
                        )
                    )
                    .padding(vertical = 8.dp, horizontal = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "RÉPUBLIQUE DU MALI",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Un Peuple • Un But • Une Foi",
                        color = Color(0xFFFDE68A),
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = school.academyName.ifBlank { "Académie d'Enseignement de Bamako" }.uppercase(),
                        color = Color(0xFFE2E8F0),
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = school.name.uppercase(),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "CARTE D'IDENTITÉ SCOLAIRE • ${school.academicYear}",
                        color = Color(0xFF93C5FD),
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Filet doré
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp)
                    .background(Color(0xFFF59E0B))
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 4. Photo et Identité de l'élève
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Photo élève avec cadre doré officiel
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = try { Color(android.graphics.Color.parseColor(student.avatarColorHex)) } catch (_: Exception) { Color(0xFF2563EB) },
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFF59E0B)),
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${student.firstName.take(1)}${student.lastName.take(1)}".uppercase(),
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Textes identité
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${student.lastName.uppercase()} ${student.firstName}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                        color = Color(0xFF0F172A),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Matricule : ", fontSize = 9.sp, color = Color(0xFF475569))
                        Text(student.matricule, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Classe : ", fontSize = 9.sp, color = Color(0xFF475569))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFECFDF5)
                        ) {
                            Text(
                                text = schoolClass?.name ?: "Classe",
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF059669)
                            )
                        }
                    }
                    Text(
                        text = "Né(e) le : ${student.birthDate} (${student.gender})",
                        fontSize = 8.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 5. Section Statut Examen d'Académie
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .clickable { onEditExamNumber() },
                shape = RoundedCornerShape(8.dp),
                color = if (student.isExamCandidate) {
                    if (student.academyExamNumber.isNotBlank()) Color(0xFFECFDF5) else Color(0xFFFFFBEB)
                } else Color(0xFFF1F5F9),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (student.isExamCandidate) {
                        if (student.academyExamNumber.isNotBlank()) Color(0xFF10B981) else Color(0xFFF59E0B)
                    } else Color(0xFFCBD5E1)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (student.isExamCandidate) {
                            if (student.academyExamNumber.isNotBlank()) Icons.Default.CheckCircle else Icons.Default.Warning
                        } else Icons.Default.School,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (student.isExamCandidate) {
                            if (student.academyExamNumber.isNotBlank()) Color(0xFF059669) else Color(0xFFB45309)
                        } else Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        if (student.isExamCandidate) {
                            if (student.academyExamNumber.isNotBlank()) {
                                Text(
                                    text = "CANDIDAT EXAMEN OFFICIEL (DEF/BAC)",
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF065F46)
                                )
                                Text(
                                    text = "N° Table : ${student.academyExamNumber}",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF047857)
                                )
                            } else {
                                Text(
                                    text = "CANDIDAT EXAMEN EN ATTENTE D'ATTRIBUTION",
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB45309)
                                )
                                Text(
                                    text = "N° officiel en attente des listes Académie",
                                    fontSize = 8.sp,
                                    color = Color(0xFF78350F)
                                )
                            }
                        } else {
                            Text(
                                text = "RÉGIME SCOLAIRE GÉNÉRAL",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF334155)
                            )
                            Text(
                                text = "Élève sous contrat d'études régulier",
                                fontSize = 7.5.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Modifier",
                        modifier = Modifier.size(12.dp),
                        tint = Color(0xFF94A3B8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 6. Zone QR Code & Scanner
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF8FAFC),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // QR Code factice haute résolution
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.QrCode,
                                contentDescription = "QR Code",
                                tint = Color.Black,
                                modifier = Modifier.size(42.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = "SCANNER SCOLARIS",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Appel, présence & contrôle",
                            fontSize = 7.5.sp,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = student.effectiveQrData.take(22) + "...",
                            fontSize = 7.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF2563EB)
                        )
                        Text(
                            text = "Urgence : ${student.parentPhone}",
                            fontSize = 7.sp,
                            color = Color(0xFF475569)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 7. Bas de carte : Sceau officiel & Signature du Directeur
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sceau de l'école
                Surface(
                    shape = CircleShape,
                    color = Color.Transparent,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E3A8A)),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🇲🇱", fontSize = 9.sp)
                            Text("SCEAU", fontSize = 5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A))
                        }
                    }
                }

                // Signature du Directeur
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Le Directeur de l'Établissement",
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = school.directorName.ifBlank { "Dr. Robert Kouassi" },
                        fontSize = 7.sp,
                        color = Color(0xFF475569)
                    )
                    Text(
                        text = "Signature & Cachet Officiels",
                        fontSize = 6.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}
