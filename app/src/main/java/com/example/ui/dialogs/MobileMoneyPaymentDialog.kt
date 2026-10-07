package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.MobilePaymentMethods
import com.example.data.model.PaymentType
import com.example.data.model.Student
import com.example.ui.viewmodel.SchoolViewModel
import com.example.util.LocalizationUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileMoneyPaymentDialog(
    viewModel: SchoolViewModel,
    onDismiss: () -> Unit
) {
    val students by viewModel.students.collectAsStateWithLifecycle()
    val currentLang by viewModel.currentLanguage.collectAsStateWithLifecycle()

    var selectedStudent by remember { mutableStateOf(students.firstOrNull()) }
    var studentDropdownExpanded by remember { mutableStateOf(false) }

    var selectedPaymentType by remember { mutableStateOf(PaymentType.SCOLARITE) }
    var selectedMethod by remember { mutableStateOf(MobilePaymentMethods.ORANGE_MONEY) }
    var amountStr by remember { mutableStateOf("250000") }
    var phoneNumber by remember { mutableStateOf("+225 07 ") }
    var txnRef by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }
    var isSubmitted by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 24.dp)
                .testTag("dialog_mobile_money_payment"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF7900).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhoneAndroid,
                                contentDescription = null,
                                tint = Color(0xFFFF7900),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Paiement Mobile Money",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Orange Money • Moov Money • Wave (Devise : FCFA)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Fermer")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (isSubmitted) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(60.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Paiement Enregistré avec Succès !", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Un reçu officiel a été généré pour ${selectedStudent?.fullName} pour un montant de ${LocalizationUtil.formatFcfa(amountStr.toDoubleOrNull() ?: 0.0)}.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("Terminer")
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(440.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Élève sélectionné
                        item {
                            Text("1. Sélection de l'Élève", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(4.dp))
                            ExposedDropdownMenuBox(
                                expanded = studentDropdownExpanded,
                                onExpandedChange = { studentDropdownExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = selectedStudent?.let { "${it.fullName} (${it.matricule})" } ?: "Sélectionner un élève",
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = studentDropdownExpanded) },
                                    modifier = Modifier
                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                                        .fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = studentDropdownExpanded,
                                    onDismissRequest = { studentDropdownExpanded = false }
                                ) {
                                    students.forEach { s ->
                                        DropdownMenuItem(
                                            text = { Text("${s.fullName} - ${s.matricule}") },
                                            onClick = {
                                                selectedStudent = s
                                                if (s.parentPhone.isNotBlank()) phoneNumber = s.parentPhone
                                                studentDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Opérateur Mobile Money
                        item {
                            Text("2. Opérateur Mobile Money", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                listOf(
                                    Pair(MobilePaymentMethods.ORANGE_MONEY, Color(0xFFFF7900)),
                                    Pair(MobilePaymentMethods.WAVE, Color(0xFF1DC3EC)),
                                    Pair(MobilePaymentMethods.MOOV_MONEY, Color(0xFF00843D))
                                ).forEach { (method, brandColor) ->
                                    val isSelected = selectedMethod == method
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { selectedMethod = method },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) brandColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, brandColor) else null
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .clip(CircleShape)
                                                    .background(brandColor)
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = method.replace(" Money", ""),
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = if (isSelected) brandColor else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Type de Frais & Montant en FCFA
                        item {
                            Text("3. Montant à Régler en Franc CFA (FCFA)", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = amountStr,
                                    onValueChange = { amountStr = it },
                                    label = { Text("Montant (FCFA) *") },
                                    modifier = Modifier.weight(1.2f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = phoneNumber,
                                    onValueChange = { phoneNumber = it },
                                    label = { Text("N° Débiteur *") },
                                    modifier = Modifier.weight(1.3f),
                                    singleLine = true
                                )
                            }
                        }

                        // Référence de transaction
                        item {
                            OutlinedTextField(
                                value = txnRef,
                                onValueChange = { txnRef = it },
                                label = { Text("Référence Transaction (SMS Orange/Wave/Moov)") },
                                placeholder = { Text("Ex: OM-849102 / WV-772109") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }

                        // Remarques
                        item {
                            OutlinedTextField(
                                value = remarks,
                                onValueChange = { remarks = it },
                                label = { Text("Commentaire / Motif") },
                                placeholder = { Text("Ex: Scolarité Trimestre 1") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }

                        // Validation Button
                        item {
                            Spacer(modifier = Modifier.height(6.dp))
                            val amt = amountStr.toDoubleOrNull() ?: 0.0
                            Button(
                                onClick = {
                                    val st = selectedStudent ?: return@Button
                                    val ref = if (txnRef.isNotBlank()) txnRef else "${selectedMethod.take(2).uppercase()}-${System.currentTimeMillis().toString().takeLast(6)}"
                                    viewModel.recordMobilePayment(
                                        studentId = st.id,
                                        type = selectedPaymentType,
                                        amount = amt,
                                        paymentMethod = selectedMethod,
                                        mobilePhoneNumber = phoneNumber,
                                        transactionRef = ref,
                                        remarks = remarks
                                    )
                                    isSubmitted = true
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("btn_confirm_mobile_payment"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = when (selectedMethod) {
                                        MobilePaymentMethods.ORANGE_MONEY -> Color(0xFFFF7900)
                                        MobilePaymentMethods.WAVE -> Color(0xFF0284C7)
                                        MobilePaymentMethods.MOOV_MONEY -> Color(0xFF00843D)
                                        else -> MaterialTheme.colorScheme.primary
                                    }
                                )
                            ) {
                                Icon(imageVector = Icons.Default.Payment, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Valider l'Encaissement de ${LocalizationUtil.formatFcfa(amt)}",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
