package com.example.ui.screens.finance

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Payment
import com.example.data.model.PaymentStatus
import com.example.data.model.PaymentType
import com.example.data.model.UserRole
import com.example.data.model.MobilePaymentMethods
import com.example.ui.viewmodel.SchoolViewModel
import com.example.util.LocalizationUtil

@Composable
fun FinanceScreen(
    viewModel: SchoolViewModel
) {
    val payments by viewModel.payments.collectAsStateWithLifecycle()
    val students by viewModel.students.collectAsStateWithLifecycle()
    val classes by viewModel.classes.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val currentSchool by viewModel.currentSchool.collectAsStateWithLifecycle()
    val currentLang by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val subscriptions by viewModel.schoolSubscriptions.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf<PaymentStatus?>(null) } // null = All
    var typeFilter by remember { mutableStateOf<PaymentType?>(null) }
    var methodFilter by remember { mutableStateOf<String?>(null) } // null = All

    // Computed totals in FCFA
    val totalCollected = payments.sumOf { it.amountPaid }
    val totalInvoiced = payments.sumOf { it.amountTotal }
    val totalOutstanding = maxOf(0.0, totalInvoiced - totalCollected)
    val recoveryRate = if (totalInvoiced > 0) (totalCollected / totalInvoiced) * 100 else 100.0

    // Filter payments based on search and selected chips
    val filteredPayments = payments.filter { p ->
        val stu = students.find { it.id == p.studentId }
        val matchesSearch = searchQuery.isBlank() ||
                (stu?.fullName?.contains(searchQuery, ignoreCase = true) == true) ||
                (stu?.matricule?.contains(searchQuery, ignoreCase = true) == true) ||
                p.receiptNumber.contains(searchQuery, ignoreCase = true)

        val matchesStatus = statusFilter == null || p.status == statusFilter
        val matchesType = typeFilter == null || p.type == typeFilter
        val matchesMethod = methodFilter == null || p.paymentMethod.contains(methodFilter!!, ignoreCase = true)

        // If current user is student or parent, restrict to their record
        val matchesUser = if (currentUser.role == UserRole.ELEVE) {
            p.studentId == currentUser.linkedStudentId
        } else if (currentUser.role == UserRole.PARENT) {
            val childId = viewModel.selectedChildStudentId.value ?: currentUser.linkedStudentId
            p.studentId == childId
        } else true

        matchesSearch && matchesStatus && matchesType && matchesMethod && matchesUser
    }

    Scaffold(
        floatingActionButton = {
            if (currentUser.role == UserRole.ADMIN || currentUser.role == UserRole.COMPTABLE || currentUser.role == UserRole.DIRECTION) {
                FloatingActionButton(
                    onClick = { viewModel.setAddPaymentDialogOpen(true) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("fab_add_payment")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Enregistrer Paiement")
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .testTag("finance_screen"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // === Bannière Multi-Écoles & Souscription Plateforme (Frais Inscription & Frais Annuel) ===
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_school_subscription_info"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Business,
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
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Souscription Plateforme • Devise : Franc CFA (XOF)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF16A34A).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "ACTIF",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF16A34A),
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Frais Inscription & Frais Annuel de l'école
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = LocalizationUtil.t("registration_fee", currentLang),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = LocalizationUtil.formatFcfa(currentSchool?.registrationFee ?: 250000.0),
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = LocalizationUtil.t("annual_fee", currentLang),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = LocalizationUtil.formatFcfa(currentSchool?.annualSubscriptionFee ?: 500000.0),
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF047857)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Boutons d'Action Rapide : Paiement Souscription, Multi-Écoles & Paie Enseignants
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = { viewModel.setSubscriptionDialogOpen(true) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_finance_manage_subscription"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(LocalizationUtil.t("subscriptions", currentLang), fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = { viewModel.setSchoolManagementDialogOpen(true) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_finance_all_schools"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(LocalizationUtil.t("schools", currentLang), fontSize = 11.sp)
                            }

                            Button(
                                onClick = { viewModel.setTeacherPayrollDialogOpen(true) },
                                modifier = Modifier
                                    .weight(1.1f)
                                    .testTag("btn_finance_teacher_payroll"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Paie Enseignants", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // === Bannière Rapide : Paiement Mobile Money Direct (Orange Money, Moov Money, Wave) ===
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_mobile_money_shortcuts"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.PhoneAndroid,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Règlement Express Mobile Money",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Button(
                                onClick = { viewModel.setMobilePaymentDialogOpen(true) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857)),
                                modifier = Modifier.testTag("btn_quick_mobile_pay")
                            ) {
                                Text("Payer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Payez la scolarité, cantine et transports instantanément avec Orange Money, Moov Money ou Wave en Franc CFA.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Badges des Opérateurs
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFF7900).copy(alpha = 0.15f),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.setMobilePaymentDialogOpen(true) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFFF7900)))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Orange", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFFFF7900))
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF00843D).copy(alpha = 0.15f),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.setMobilePaymentDialogOpen(true) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF00843D)))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Moov", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF00843D))
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF0284C7).copy(alpha = 0.15f),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.setMobilePaymentDialogOpen(true) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF0284C7)))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Wave", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF0284C7))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Actions Caisse Locale & Sauvegarde Technique Hors-Ligne
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.setLocalPaymentDialogOpen(true) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_open_local_payment"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.AttachMoney, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Paiement Espèces Guichet", fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = { viewModel.setBackupRestoreDialogOpen(true) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_finance_backup_restore"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sauvegarder Données", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Financial KPI Cards (Visible to Admin, Comptable, Direction, SuperAdmin)
            if (currentUser.role != UserRole.ELEVE && currentUser.role != UserRole.PARENT) {
                item {
                    Text(
                        text = "Tableau de Bord Financier (FCFA)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FinancialKpiCard(
                            title = "Total Encaissé",
                            value = LocalizationUtil.formatFcfa(totalCollected),
                            subtext = "${payments.count { it.status == PaymentStatus.PAYE }} règlements",
                            color = Color(0xFF047857),
                            icon = Icons.Default.CheckCircle,
                            modifier = Modifier.weight(1f)
                        )
                        FinancialKpiCard(
                            title = "Reste à Recouvrer",
                            value = LocalizationUtil.formatFcfa(totalOutstanding),
                            subtext = "Impayés & soldes",
                            color = Color(0xFFDC2626),
                            icon = Icons.Default.Warning,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FinancialKpiCard(
                            title = "Taux de Recouvrement",
                            value = String.format("%.1f%%", recoveryRate),
                            subtext = "Objectif trimestriel",
                            color = Color(0xFF1D4ED8),
                            icon = Icons.Default.AttachMoney,
                            modifier = Modifier.weight(1f)
                        )
                        FinancialKpiCard(
                            title = "Transactions",
                            value = "${payments.size}",
                            subtext = "Reçus officiels",
                            color = Color(0xFF7C3AED),
                            icon = Icons.Default.Receipt,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            } else {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "État de Compte de Scolarité",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Consultez l'historique complet des versements en Franc CFA et téléchargez les reçus officiels certifiés.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Rechercher un élève, matricule ou n° de reçu...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_search_finance")
                )
            }

            // Filter Chips: Status & Mobile Payment Methods
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Status filters
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = statusFilter == null,
                            onClick = { statusFilter = null },
                            label = { Text("Tous Statuts") }
                        )
                        FilterChip(
                            selected = statusFilter == PaymentStatus.PAYE,
                            onClick = { statusFilter = if (statusFilter == PaymentStatus.PAYE) null else PaymentStatus.PAYE },
                            label = { Text("Payés") }
                        )
                        FilterChip(
                            selected = statusFilter == PaymentStatus.PARTIEL,
                            onClick = { statusFilter = if (statusFilter == PaymentStatus.PARTIEL) null else PaymentStatus.PARTIEL },
                            label = { Text("Partiels") }
                        )
                        FilterChip(
                            selected = statusFilter == PaymentStatus.EN_ATTENTE,
                            onClick = { statusFilter = if (statusFilter == PaymentStatus.EN_ATTENTE) null else PaymentStatus.EN_ATTENTE },
                            label = { Text("En Attente") }
                        )
                    }

                    // Payment Method filters (Orange Money, Moov Money, Wave, etc.)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = methodFilter == null,
                            onClick = { methodFilter = null },
                            label = { Text("Tous Modes") }
                        )
                        FilterChip(
                            selected = methodFilter == "Orange",
                            onClick = { methodFilter = if (methodFilter == "Orange") null else "Orange" },
                            label = { Text("Orange Money") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFF7900).copy(alpha = 0.2f),
                                selectedLabelColor = Color(0xFFFF7900)
                            )
                        )
                        FilterChip(
                            selected = methodFilter == "Wave",
                            onClick = { methodFilter = if (methodFilter == "Wave") null else "Wave" },
                            label = { Text("Wave") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0284C7).copy(alpha = 0.2f),
                                selectedLabelColor = Color(0xFF0284C7)
                            )
                        )
                        FilterChip(
                            selected = methodFilter == "Moov",
                            onClick = { methodFilter = if (methodFilter == "Moov") null else "Moov" },
                            label = { Text("Moov Money") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF00843D).copy(alpha = 0.2f),
                                selectedLabelColor = Color(0xFF00843D)
                            )
                        )
                        FilterChip(
                            selected = methodFilter == "Espèces",
                            onClick = { methodFilter = if (methodFilter == "Espèces") null else "Espèces" },
                            label = { Text("Espèces") }
                        )
                    }
                }
            }

            // Payments List
            if (filteredPayments.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aucune transaction correspondant aux filtres.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(filteredPayments) { payment ->
                    val student = students.find { it.id == payment.studentId }
                    val sClass = classes.find { it.id == student?.classId }

                    PaymentItemCard(
                        payment = payment,
                        studentName = student?.fullName ?: "Élève Inconnu",
                        matricule = student?.matricule ?: "N/A",
                        className = sClass?.name ?: "Classe",
                        isScolariteOrAdmin = currentUser.role in listOf(UserRole.ADMIN, UserRole.DIRECTION, UserRole.SUPER_ADMIN, UserRole.COMPTABLE),
                        onValidateLocalPayment = { viewModel.validateLocalPayment(it) },
                        onOpenReceipt = { viewModel.showPaymentReceipt(payment) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FinancialKpiCard(
    title: String,
    value: String,
    subtext: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtext, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PaymentItemCard(
    payment: Payment,
    studentName: String,
    matricule: String,
    className: String,
    isScolariteOrAdmin: Boolean = false,
    onValidateLocalPayment: (Long) -> Unit = {},
    onOpenReceipt: () -> Unit
) {
    val statusColor = when (payment.status) {
        PaymentStatus.PAYE -> Color(0xFF047857)
        PaymentStatus.PARTIEL -> Color(0xFFD97706)
        PaymentStatus.EN_ATTENTE -> Color(0xFFDC2626)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenReceipt)
            .testTag("payment_card_${payment.receiptNumber}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                    .background(statusColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Receipt,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = studentName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = LocalizationUtil.formatFcfa(payment.amountPaid),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = statusColor
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${payment.type.label} • $className",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = payment.status.label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val methodColor = when {
                            payment.paymentMethod.contains("Orange", ignoreCase = true) -> Color(0xFFFF7900)
                            payment.paymentMethod.contains("Wave", ignoreCase = true) -> Color(0xFF0284C7)
                            payment.paymentMethod.contains("Moov", ignoreCase = true) -> Color(0xFF00843D)
                            else -> MaterialTheme.colorScheme.primary
                        }
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(methodColor)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${payment.receiptNumber} • ${payment.paymentMethod}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = payment.date,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (payment.isLocalPayment) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (payment.isValidatedByScolarite) {
                            Text(
                                text = "✓ Visa Scolarité Validé (${payment.validatedBy.ifBlank { "Scolarité" }})",
                                fontSize = 10.sp,
                                color = Color(0xFF16A34A),
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = "⏳ Guichet Local : En attente de visa",
                                fontSize = 10.sp,
                                color = Color(0xFFD97706),
                                fontWeight = FontWeight.Bold
                            )
                            if (isScolariteOrAdmin) {
                                OutlinedButton(
                                    onClick = { onValidateLocalPayment(payment.id) },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("Viser", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
