package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.MobilePaymentMethods
import com.example.data.model.TeacherPayment
import com.example.data.model.UserRole
import com.example.ui.components.EmptyStateView
import com.example.ui.viewmodel.SchoolViewModel
import com.example.util.LocalizationUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherPayrollDialog(
    viewModel: SchoolViewModel,
    onDismiss: () -> Unit
) {
    val teacherPayments by viewModel.teacherPayments.collectAsStateWithLifecycle()
    val teachers by viewModel.teachers.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    val isTeacher = currentUser.role == UserRole.PROFESSEUR
    val isScolariteOrAdmin = currentUser.role in listOf(UserRole.ADMIN, UserRole.DIRECTION, UserRole.SUPER_ADMIN, UserRole.COMPTABLE)

    // For a teacher: filter to only their own payments
    val displayedPayments = if (isTeacher && currentUser.linkedTeacherId != null) {
        teacherPayments.filter { it.teacherId == currentUser.linkedTeacherId }
    } else {
        teacherPayments
    }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Historique des Salaires, 1: Émettre un Paiement

    // Form states for emitting salary payment
    var selectedTeacher by remember(teachers) { mutableStateOf(teachers.firstOrNull()) }
    var teacherDropdownExpanded by remember { mutableStateOf(false) }

    var periodMonth by remember { mutableStateOf("Octobre 2025") }
    var baseSalaryStr by remember { mutableStateOf("350000") }
    var bonusStr by remember { mutableStateOf("0") }
    var deductionStr by remember { mutableStateOf("0") }
    var paymentMethod by remember { mutableStateOf(MobilePaymentMethods.VIREMENT) }
    var methodDropdownExpanded by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 20.dp)
                .testTag("dialog_teacher_payroll")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isTeacher) "Mes Salaires & Rémunérations" else "Suivi de la Paie du Corps Professoral",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (isTeacher) "Historique officiel des virements et honoraires" else "Gestion comptable & validation des règlements enseignants",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("btn_close_payroll")) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Fermer")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tabs if admin / scolarité
                if (isScolariteOrAdmin) {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clip(RoundedCornerShape(10.dp))
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Paiements & Historique (${displayedPayments.size})", fontWeight = FontWeight.SemiBold) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Émettre un Virement / Salaire", fontWeight = FontWeight.SemiBold) }
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (selectedTab == 0 || isTeacher) {
                    // List of payments
                    if (displayedPayments.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Default.Payment,
                            title = "Aucun règlement enregistré",
                            description = "Les paiements de salaires apparaîtront ici dès leur émission par la comptabilité.",
                            modifier = Modifier.height(280.dp)
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            items(displayedPayments, key = { it.id }) { item ->
                                TeacherPaymentCard(
                                    payment = item,
                                    canValidate = isScolariteOrAdmin && item.status != "PAYE",
                                    onValidate = { viewModel.validateTeacherPayment(item) }
                                )
                            }
                        }
                    }
                } else {
                    // New Payment Form for Admin / Scolarité
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        // Teacher Dropdown
                        ExposedDropdownMenuBox(
                            expanded = teacherDropdownExpanded,
                            onExpandedChange = { teacherDropdownExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedTeacher?.fullName ?: "Sélectionner un professeur",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Enseignant bénéficiaire *") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = teacherDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = teacherDropdownExpanded,
                                onDismissRequest = { teacherDropdownExpanded = false }
                            ) {
                                teachers.forEach { t ->
                                    DropdownMenuItem(
                                        text = { Text("${t.fullName} (${t.subjectName})") },
                                        onClick = {
                                            selectedTeacher = t
                                            teacherDropdownExpanded = false
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
                                value = periodMonth,
                                onValueChange = { periodMonth = it },
                                label = { Text("Période / Mois *") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )

                            // Payment Method Dropdown
                            ExposedDropdownMenuBox(
                                expanded = methodDropdownExpanded,
                                onExpandedChange = { methodDropdownExpanded = it },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = paymentMethod,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Moyen *") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = methodDropdownExpanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = methodDropdownExpanded,
                                    onDismissRequest = { methodDropdownExpanded = false }
                                ) {
                                    MobilePaymentMethods.all.forEach { m ->
                                        DropdownMenuItem(
                                            text = { Text(m) },
                                            onClick = {
                                                paymentMethod = m
                                                methodDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = baseSalaryStr,
                                onValueChange = { baseSalaryStr = it },
                                label = { Text("Salaire de base (FCFA) *") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = bonusStr,
                                onValueChange = { bonusStr = it },
                                label = { Text("Primes (FCFA)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = deductionStr,
                                onValueChange = { deductionStr = it },
                                label = { Text("Retenues (FCFA)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        val base = baseSalaryStr.toDoubleOrNull() ?: 0.0
                        val bonus = bonusStr.toDoubleOrNull() ?: 0.0
                        val ded = deductionStr.toDoubleOrNull() ?: 0.0
                        val net = maxOf(0.0, base + bonus - ded)

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Salaire Net à Régler :", fontWeight = FontWeight.Bold)
                                Text(
                                    LocalizationUtil.formatFcfa(net),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Notes / Référence du virement") },
                            placeholder = { Text("Ex: Honoraires cours de soutien, prime trimestrielle...") },
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

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                val t = selectedTeacher
                                if (t == null) {
                                    errorMessage = "Veuillez désigner l'enseignant bénéficiaire."
                                    return@Button
                                }
                                if (base <= 0) {
                                    errorMessage = "Le salaire de base doit être supérieur à zéro."
                                    return@Button
                                }
                                viewModel.addTeacherPayment(
                                    teacherId = t.id,
                                    teacherName = t.fullName,
                                    periodMonth = periodMonth.trim(),
                                    baseSalary = base,
                                    bonusAmount = bonus,
                                    deductions = ded,
                                    paymentMethod = paymentMethod,
                                    notes = notes.trim()
                                )
                                selectedTab = 0
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_submit_teacher_payment")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Enregistrer & Valider le Paiement Enseignant", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TeacherPaymentCard(
    payment: TeacherPayment,
    canValidate: Boolean,
    onValidate: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = payment.teacherName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${payment.periodMonth} • Réf: ${payment.transactionRef}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                val isPaid = payment.status == "PAYE"
                val badgeColor = if (isPaid) Color(0xFF16A34A) else Color(0xFFD97706)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isPaid) "Payé" else "En Attente",
                        color = badgeColor,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Payment,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${payment.paymentMethod} • Date: ${payment.paymentDate}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Text(
                    text = LocalizationUtil.formatFcfa(payment.netAmount),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (payment.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Détails: ${payment.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (canValidate) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onValidate,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Valider le Déblocage & Marquer Payé")
                }
            }
        }
    }
}
