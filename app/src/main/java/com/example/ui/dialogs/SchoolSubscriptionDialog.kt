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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.data.model.SchoolSubscriptionStatus
import com.example.data.model.SubscriptionFeeType
import com.example.ui.viewmodel.SchoolViewModel
import com.example.util.LocalizationUtil

@Composable
fun SchoolSubscriptionDialog(
    viewModel: SchoolViewModel,
    onDismiss: () -> Unit
) {
    val currentSchool by viewModel.currentSchool.collectAsStateWithLifecycle()
    val allSubscriptions by viewModel.schoolSubscriptions.collectAsStateWithLifecycle()
    val currentLang by viewModel.currentLanguage.collectAsStateWithLifecycle()

    val school = currentSchool ?: return

    val schoolSubs = allSubscriptions.filter { it.schoolId == school.id }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Régler / Renouveler, 1: Historique des Reçus

    var selectedFeeType by remember { mutableStateOf(SubscriptionFeeType.ABONNEMENT_ANNUEL) }
    var selectedMethod by remember { mutableStateOf(MobilePaymentMethods.ORANGE_MONEY) }
    var phoneNumber by remember { mutableStateOf(school.phone) }
    var transactionRef by remember { mutableStateOf("") }
    var customAmountStr by remember { mutableStateOf(school.annualSubscriptionFee.toInt().toString()) }
    var isSuccess by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 24.dp)
                .testTag("dialog_school_subscription"),
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
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF16A34A).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payment,
                                contentDescription = null,
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Souscription & Abonnement Plateforme",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "${school.name} (${school.code})",
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

                // Subscription Status Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when (school.subscriptionStatus) {
                            SchoolSubscriptionStatus.ACTIVE -> Color(0xFF16A34A).copy(alpha = 0.12f)
                            SchoolSubscriptionStatus.PENDING_RENEWAL -> Color(0xFFD97706).copy(alpha = 0.12f)
                            SchoolSubscriptionStatus.EXPIRED -> Color(0xFFDC2626).copy(alpha = 0.12f)
                            SchoolSubscriptionStatus.TRIAL -> Color(0xFF2563EB).copy(alpha = 0.12f)
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = if (currentLang == com.example.util.AppLanguage.EN) school.subscriptionStatus.labelEn else school.subscriptionStatus.labelFr,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = when (school.subscriptionStatus) {
                                    SchoolSubscriptionStatus.ACTIVE -> Color(0xFF16A34A)
                                    SchoolSubscriptionStatus.PENDING_RENEWAL -> Color(0xFFD97706)
                                    SchoolSubscriptionStatus.EXPIRED -> Color(0xFFDC2626)
                                    SchoolSubscriptionStatus.TRIAL -> Color(0xFF2563EB)
                                }
                            )
                            Text(
                                text = "Valable jusqu'au : ${school.subscriptionExpiry}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Tarif Annuel",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = LocalizationUtil.formatFcfa(school.annualSubscriptionFee),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Paiement & Renouvellement", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Historique (${schoolSubs.size})", fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (selectedTab == 0) {
                    if (isSuccess) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(56.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Paiement de Souscription Validé !", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("L'abonnement de votre établissement a été prolongé de 12 mois.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(18.dp))
                            Button(
                                onClick = {
                                    isSuccess = false
                                    selectedTab = 1
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Voir l'historique des reçus")
                            }
                        }
                    } else {
                        // Form
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(380.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Text("1. Type de Frais à Régler", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    // Inscription
                                    val isReg = selectedFeeType == SubscriptionFeeType.INSCRIPTION
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                selectedFeeType = SubscriptionFeeType.INSCRIPTION
                                                customAmountStr = school.registrationFee.toInt().toString()
                                            },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isReg) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        border = if (isReg) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text("Adhésion / Inscription", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                            Text(LocalizationUtil.formatFcfa(school.registrationFee), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }

                                    // Abonnement Annuel
                                    val isAnn = selectedFeeType == SubscriptionFeeType.ABONNEMENT_ANNUEL
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                selectedFeeType = SubscriptionFeeType.ABONNEMENT_ANNUEL
                                                customAmountStr = school.annualSubscriptionFee.toInt().toString()
                                            },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isAnn) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        border = if (isAnn) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text("Abonnement Annuel", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                            Text(LocalizationUtil.formatFcfa(school.annualSubscriptionFee), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }

                            item {
                                Text("2. Choisissez votre Moyen de Paiement Mobile Money", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                                Spacer(modifier = Modifier.height(6.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf(
                                        Triple(MobilePaymentMethods.ORANGE_MONEY, Color(0xFFFF7900), "Orange Money CI/SN/ML"),
                                        Triple(MobilePaymentMethods.WAVE, Color(0xFF1DC3EC), "Wave Mobile Money (0% frais)"),
                                        Triple(MobilePaymentMethods.MOOV_MONEY, Color(0xFF00843D), "Moov Africa Money"),
                                        Triple(MobilePaymentMethods.VIREMENT, Color(0xFF2563EB), "Virement Bancaire Direct")
                                    ).forEach { (method, color, desc) ->
                                        val isSel = selectedMethod == method
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { selectedMethod = method },
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isSel) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                            border = if (isSel) androidx.compose.foundation.BorderStroke(2.dp, color) else null
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(16.dp)
                                                        .clip(CircleShape)
                                                        .background(color)
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(text = method, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                                    Text(text = desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                                if (isSel) {
                                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            item {
                                Text("3. Coordonnées & Validation", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = phoneNumber,
                                    onValueChange = { phoneNumber = it },
                                    label = { Text("Numéro Mobile Money Débiteur *") },
                                    leadingIcon = { Icon(imageVector = Icons.Default.PhoneAndroid, contentDescription = null) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = transactionRef,
                                    onValueChange = { transactionRef = it },
                                    label = { Text("Référence de Transaction (SMS de confirmation)") },
                                    placeholder = { Text("Ex: OM-983421 / WV-009182") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }

                            item {
                                Spacer(modifier = Modifier.height(6.dp))
                                Button(
                                    onClick = {
                                        val amt = customAmountStr.toDoubleOrNull() ?: school.annualSubscriptionFee
                                        val ref = if (transactionRef.isNotBlank()) transactionRef else "${selectedMethod.take(2).uppercase()}-${System.currentTimeMillis().toString().takeLast(6)}"
                                        viewModel.addSchoolSubscriptionPayment(
                                            schoolId = school.id,
                                            feeType = selectedFeeType,
                                            amount = amt,
                                            method = selectedMethod,
                                            phoneNumber = phoneNumber,
                                            transactionReference = ref,
                                            notes = "Souscription réglée par $selectedMethod"
                                        )
                                        isSuccess = true
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("btn_confirm_subscription_payment"),
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
                                        text = "Confirmer le Paiement (${LocalizationUtil.formatFcfa(customAmountStr.toDoubleOrNull() ?: school.annualSubscriptionFee)})",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // History
                    if (schoolSubs.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Aucun paiement de souscription enregistré pour le moment.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(380.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(schoolSubs, key = { it.id }) { sub ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = if (currentLang == com.example.util.AppLanguage.EN) sub.feeType.labelEn else sub.feeType.labelFr,
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                text = "${sub.paymentMethod} • Réf: ${sub.transactionReference}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "Payé le : ${sub.paymentDate} (Valable jusqu'au ${sub.validUntil})",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF16A34A)
                                            )
                                        }

                                        Text(
                                            text = LocalizationUtil.formatFcfa(sub.amount),
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
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
