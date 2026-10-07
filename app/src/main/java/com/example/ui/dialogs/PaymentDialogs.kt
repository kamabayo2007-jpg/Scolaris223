package com.example.ui.dialogs

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Payment
import com.example.data.model.PaymentStatus
import com.example.data.model.PaymentType
import com.example.ui.viewmodel.SchoolViewModel
import com.example.util.LocalizationUtil
import com.example.util.PdfExportUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPaymentDialog(
    viewModel: SchoolViewModel,
    onDismiss: () -> Unit
) {
    val students by viewModel.students.collectAsStateWithLifecycle()
    val classes by viewModel.classes.collectAsStateWithLifecycle()

    var selectedStudentId by remember { mutableStateOf(students.firstOrNull()?.id ?: 1L) }
    var selectedStudentExpanded by remember { mutableStateOf(false) }

    var selectedType by remember { mutableStateOf(PaymentType.SCOLARITE) }
    var selectedTypeExpanded by remember { mutableStateOf(false) }

    var amountTotalText by remember { mutableStateOf("150000") }
    var amountPaidText by remember { mutableStateOf("150000") }

    var selectedMethod by remember { mutableStateOf("Orange Money") }
    var selectedMethodExpanded by remember { mutableStateOf(false) }
    val methods = listOf("Orange Money", "Moov Money", "Wave", "Espèces", "Virement Bancaire", "Chèque")

    var remarks by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val currentSelectedStudent = students.find { it.id == selectedStudentId }
    val currentStudentClass = classes.find { it.id == currentSelectedStudent?.classId }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("dialog_add_payment")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Payment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Enregistrer un Paiement",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Select Student Dropdown
                ExposedDropdownMenuBox(
                    expanded = selectedStudentExpanded,
                    onExpandedChange = { selectedStudentExpanded = !selectedStudentExpanded }
                ) {
                    OutlinedTextField(
                        value = "${currentSelectedStudent?.fullName ?: "Sélectionner un élève"} (${currentStudentClass?.name ?: ""})",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Élève concerné") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = selectedStudentExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = selectedStudentExpanded,
                        onDismissRequest = { selectedStudentExpanded = false }
                    ) {
                        students.forEach { stu ->
                            val cName = classes.find { it.id == stu.classId }?.name ?: ""
                            DropdownMenuItem(
                                text = { Text("${stu.fullName} - ${stu.matricule} ($cName)") },
                                onClick = {
                                    selectedStudentId = stu.id
                                    selectedStudentExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Payment Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = selectedTypeExpanded,
                    onExpandedChange = { selectedTypeExpanded = !selectedTypeExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedType.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Nature des frais") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = selectedTypeExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = selectedTypeExpanded,
                        onDismissRequest = { selectedTypeExpanded = false }
                    ) {
                        PaymentType.values().forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.label) },
                                onClick = {
                                    selectedType = type
                                    selectedTypeExpanded = false
                                    if (type == PaymentType.INSCRIPTION) {
                                        amountTotalText = "150.0"
                                        amountPaidText = "150.0"
                                    } else if (type == PaymentType.CANTINE) {
                                        amountTotalText = "220.0"
                                        amountPaidText = "220.0"
                                    } else if (type == PaymentType.TRANSPORT) {
                                        amountTotalText = "180.0"
                                        amountPaidText = "180.0"
                                    } else {
                                        amountTotalText = "650.0"
                                        amountPaidText = "650.0"
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = amountTotalText,
                        onValueChange = { amountTotalText = it },
                        label = { Text("Montant Total (€)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_payment_total")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = amountPaidText,
                        onValueChange = { amountPaidText = it },
                        label = { Text("Montant Versé (€)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_payment_paid")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Payment Method
                ExposedDropdownMenuBox(
                    expanded = selectedMethodExpanded,
                    onExpandedChange = { selectedMethodExpanded = !selectedMethodExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedMethod,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Mode de Règlement") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = selectedMethodExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = selectedMethodExpanded,
                        onDismissRequest = { selectedMethodExpanded = false }
                    ) {
                        methods.forEach { method ->
                            DropdownMenuItem(
                                text = { Text(method) },
                                onClick = {
                                    selectedMethod = method
                                    selectedMethodExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Observations / Référence bancaire") },
                    placeholder = { Text("Ex: Virement reçu le 28/09, acompte n°1") },
                    modifier = Modifier.fillMaxWidth()
                )

                errorMessage?.let { err ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Annuler")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val total = amountTotalText.toDoubleOrNull()
                            val paid = amountPaidText.toDoubleOrNull()
                            if (total == null || paid == null || total <= 0) {
                                errorMessage = "Veuillez entrer des montants valides."
                                return@Button
                            }
                            viewModel.addPayment(
                                studentId = selectedStudentId,
                                type = selectedType,
                                amountTotal = total,
                                amountPaid = paid,
                                paymentMethod = selectedMethod,
                                remarks = remarks
                            )
                        },
                        modifier = Modifier.testTag("btn_confirm_add_payment")
                    ) {
                        Text("Valider & Générer Reçu")
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentReceiptDialog(
    payment: Payment,
    viewModel: SchoolViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val students by viewModel.students.collectAsStateWithLifecycle()
    val classes by viewModel.classes.collectAsStateWithLifecycle()
    val currentSchool by viewModel.currentSchool.collectAsStateWithLifecycle()

    val student = students.find { it.id == payment.studentId }
    val schoolClass = classes.find { it.id == student?.classId }
    val schoolName = currentSchool?.name ?: "Groupe Scolaire Excellence Cocody"

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("dialog_payment_receipt")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Receipt Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Receipt,
                            contentDescription = null,
                            tint = Color(0xFF047857),
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Reçu Officiel de Caisse",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Official Printable Certificate Card
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFBFBFB),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // School Header
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = schoolName.uppercase(),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                                color = Color(0xFF1E3A8A),
                                letterSpacing = 1.sp,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "${currentSchool?.city ?: "Abidjan"} • Code : ${currentSchool?.code ?: "GSE-ABJ"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = "Année Scolaire ${payment.academicYear} — Service Facturation & Comptabilité",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = Color(0xFF475569)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color(0xFFCBD5E1))
                        Spacer(modifier = Modifier.height(12.dp))

                        // Receipt Number & Date
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "RÉCÉPISSÉ N°",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = payment.receiptNumber,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "DATE D'ÉMISSION",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = payment.date,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF1E293B)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Student Details Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFF1F5F9))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = "DÉSIGNATION DE L'ÉLÈVE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF475569)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${student?.fullName ?: "Élève Inconnu"}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF0F172A)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Matricule : ${student?.matricule ?: "N/A"}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF334155)
                                    )
                                    Text(
                                        text = "Classe : ${schoolClass?.name ?: "N/A"}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF1E3A8A)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Transaction Details
                        ReceiptRow("Nature du Versement", payment.type.label)
                        ReceiptRow("Mode de Paiement", payment.paymentMethod)
                        if (payment.remarks.isNotBlank()) {
                            ReceiptRow("Observations", payment.remarks)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Color(0xFFE2E8F0))
                        Spacer(modifier = Modifier.height(8.dp))

                        // Amounts Breakdown
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Montant Total Facturé :", color = Color(0xFF475569), fontSize = 13.sp)
                            Text(
                                LocalizationUtil.formatFcfa(payment.amountTotal),
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1E293B)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Montant Versé / Encaissé :", fontWeight = FontWeight.Bold, color = Color(0xFF047857), fontSize = 14.sp)
                            Text(
                                LocalizationUtil.formatFcfa(payment.amountPaid),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF047857)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Solde Restant Dû :", color = Color(0xFF64748B), fontSize = 13.sp)
                            Text(
                                LocalizationUtil.formatFcfa(payment.remainingAmount),
                                fontWeight = FontWeight.Bold,
                                color = if (payment.remainingAmount > 0) Color(0xFFDC2626) else Color(0xFF16A34A)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stamp and Signature
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            // Stamp Box
                            Box(
                                modifier = Modifier
                                    .border(2.dp, Color(0xFF047857), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Verified,
                                            contentDescription = null,
                                            tint = Color(0xFF047857),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "CAISSE CENTRALE",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 10.sp,
                                            color = Color(0xFF047857)
                                        )
                                    }
                                    Text(
                                        text = if (payment.remainingAmount == 0.0) "ACQUITTÉ" else "ACOMPTE ENREGISTRÉ",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color(0xFF047857)
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Le Chef Comptable",
                                    fontSize = 11.sp,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    color = Color(0xFF64748B)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Fatou Diallo",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF1E293B)
                                )
                            }
                        }

                        // Visa officiel de la Scolarité pour les paiements locaux
                        if (payment.isLocalPayment) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (payment.isValidatedByScolarite) Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (payment.isValidatedByScolarite) "Visa Scolarité Validé" else "En attente de visa Scolarité",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (payment.isValidatedByScolarite) Color(0xFF16A34A) else Color(0xFFD97706)
                                        )
                                        Text(
                                            text = if (payment.isValidatedByScolarite) "Enregistré officiellement par ${payment.validatedBy} le ${payment.validationDate}"
                                            else "Ce paiement au guichet local doit être visé pour valider définitivement l'encaissement.",
                                            fontSize = 10.sp,
                                            color = if (payment.isValidatedByScolarite) Color(0xFF15803D) else Color(0xFFB45309)
                                        )
                                    }

                                    if (!payment.isValidatedByScolarite) {
                                        Button(
                                            onClick = {
                                                viewModel.validateLocalPayment(payment.id)
                                                onDismiss()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text("Viser & Enregistrer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions: Imprimer & Exporter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            Toast.makeText(
                                context,
                                "Impression du Reçu ${payment.receiptNumber} envoyée vers l'imprimante scolaire.",
                                Toast.LENGTH_LONG
                            ).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_print_receipt"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857))
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Imprimer Reçu")
                    }

                    OutlinedButton(
                        onClick = {
                            val file = PdfExportUtil.exportPaymentReceiptPdf(
                                context = context,
                                payment = payment,
                                student = student,
                                schoolClass = schoolClass,
                                schoolName = schoolName
                            )
                            if (file != null) {
                                PdfExportUtil.shareOrViewPdf(context, file, "Reçu Officiel ${payment.receiptNumber}")
                            } else {
                                Toast.makeText(context, "Erreur génération reçu PDF", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_download_receipt_pdf")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Télécharger PDF")
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiptRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "$label :", color = Color(0xFF64748B), fontSize = 12.sp)
        Text(text = value, fontWeight = FontWeight.Medium, color = Color(0xFF1E293B), fontSize = 12.sp)
    }
}
