package com.example.ui.dialogs

import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.PaymentType
import com.example.ui.viewmodel.SchoolViewModel
import com.example.util.LocalizationUtil
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalPaymentDialog(
    viewModel: SchoolViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val students by viewModel.students.collectAsStateWithLifecycle()
    val classes by viewModel.classes.collectAsStateWithLifecycle()
    val currentSchool by viewModel.currentSchool.collectAsStateWithLifecycle()
    val currentLang by viewModel.currentLanguage.collectAsStateWithLifecycle()

    var selectedStudentId by remember { mutableStateOf(students.firstOrNull()?.id ?: 1L) }
    var studentDropdownExpanded by remember { mutableStateOf(false) }

    var selectedFeeType by remember { mutableStateOf(PaymentType.SCOLARITE) }
    var feeTypeExpanded by remember { mutableStateOf(false) }

    var amountText by remember { mutableStateOf("100000") }
    var payerName by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var paymentReceiptNumber by remember { mutableStateOf<String?>(null) }

    val currentStudent = students.find { it.id == selectedStudentId }
    val currentClass = classes.find { it.id == currentStudent?.classId }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("dialog_local_payment")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // En-tête Caisse Locale
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
                                .background(Color(0xFF047857).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AttachMoney,
                                contentDescription = null,
                                tint = Color(0xFF047857),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Paiement Local en Espèces",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Guichet Caisse • ${currentSchool?.code ?: "GSE-ABJ"}",
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

                if (paymentReceiptNumber != null) {
                    // Écran de succès avec confirmation immédiate
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF047857),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Encaissement Espèces Validé",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF065F46)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Montant Perçu : ${LocalizationUtil.formatFcfa(amountText.toDoubleOrNull() ?: 0.0)}",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF047857),
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "N° de Quittance : $paymentReceiptNumber",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = Color(0xFF047857)
                            )
                            Text(
                                text = "Élève : ${currentStudent?.fullName ?: "N/A"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF047857)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                Toast.makeText(context, "Quittance $paymentReceiptNumber imprimée à la caisse.", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857))
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Imprimer")
                        }
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Terminer")
                        }
                    }
                } else {
                    // Formulaire de saisie du paiement guichet
                    // 1. Choix de l'élève
                    Text(
                        text = "Élève Bénéficiaire :",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ExposedDropdownMenuBox(
                        expanded = studentDropdownExpanded,
                        onExpandedChange = { studentDropdownExpanded = !studentDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = currentStudent?.let { "${it.fullName} (${it.matricule})" } ?: "Sélectionner un élève",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = studentDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = studentDropdownExpanded,
                            onDismissRequest = { studentDropdownExpanded = false }
                        ) {
                            students.forEach { stu ->
                                val cls = classes.find { it.id == stu.classId }
                                DropdownMenuItem(
                                    text = { Text("${stu.fullName} • ${cls?.name ?: "Classe"} (${stu.matricule})") },
                                    onClick = {
                                        selectedStudentId = stu.id
                                        studentDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. Type de versement
                    Text(
                        text = "Nature du Paiement :",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ExposedDropdownMenuBox(
                        expanded = feeTypeExpanded,
                        onExpandedChange = { feeTypeExpanded = !feeTypeExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedFeeType.label,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = feeTypeExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = feeTypeExpanded,
                            onDismissRequest = { feeTypeExpanded = false }
                        ) {
                            PaymentType.values().forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.label) },
                                    onClick = {
                                        selectedFeeType = type
                                        feeTypeExpanded = false
                                        if (type == PaymentType.CANTINE) amountText = "25000"
                                        else if (type == PaymentType.TRANSPORT) amountText = "35000"
                                        else if (type == PaymentType.INSCRIPTION) amountText = "150000"
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3. Montant en Franc CFA (FCFA)
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Montant Reçu en Espèces (Franc CFA)") },
                        trailingIcon = { Text("FCFA", fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 12.dp)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_local_payment_amount")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 4. Nom du déposant / parent
                    OutlinedTextField(
                        value = payerName,
                        onValueChange = { payerName = it },
                        label = { Text("Nom du Déposant / Parent (Optionnel)") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 5. Observations
                    OutlinedTextField(
                        value = remarks,
                        onValueChange = { remarks = it },
                        label = { Text("Observations / Réf bordereau") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Bouton de validation
                    Button(
                        onClick = {
                            val montant = amountText.toDoubleOrNull() ?: 0.0
                            if (montant <= 0) {
                                Toast.makeText(context, "Montant invalide", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isProcessing = true
                            val generatedRef = "CAISSE-LOC-${Date().time.toString().takeLast(8)}"
                            viewModel.recordLocalPayment(
                                studentId = selectedStudentId,
                                type = selectedFeeType,
                                amount = montant,
                                payerName = payerName.ifBlank { "Parent" },
                                receiptNumber = generatedRef,
                                remarks = remarks
                            )
                            isProcessing = false
                            paymentReceiptNumber = generatedRef
                            Toast.makeText(context, "Paiement local espèces enregistré : ${LocalizationUtil.formatFcfa(montant)}", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_confirm_local_payment"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857)),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isProcessing
                    ) {
                        Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isProcessing) "Enregistrement..." else "Valider Encaissement Espèces",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
