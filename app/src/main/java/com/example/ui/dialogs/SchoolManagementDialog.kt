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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.data.model.School
import com.example.data.model.SchoolSubscriptionStatus
import com.example.ui.viewmodel.SchoolViewModel
import com.example.util.LocalizationUtil

@Composable
fun SchoolManagementDialog(
    viewModel: SchoolViewModel,
    onDismiss: () -> Unit
) {
    val schools by viewModel.schools.collectAsStateWithLifecycle()
    val selectedSchoolId by viewModel.selectedSchoolId.collectAsStateWithLifecycle()
    val currentLang by viewModel.currentLanguage.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Liste des Écoles, 1: Nouvelle École

    // Form state for adding school
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Abidjan") }
    var country by remember { mutableStateOf("Côte d'Ivoire") }
    var phone by remember { mutableStateOf("+225 ") }
    var email by remember { mutableStateOf("") }
    var registrationFeeStr by remember { mutableStateOf("100000") }
    var annualFeeStr by remember { mutableStateOf("500000") }
    var errorMessage by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 24.dp)
                .testTag("dialog_school_management"),
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
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = LocalizationUtil.t("all_schools_overview", currentLang),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Super Admin : Contrôle Global & Multi-Tenant (${schools.size} écoles)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("btn_close_school_dialog")) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Fermer")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Établissements (${schools.size})", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(LocalizationUtil.t("add_new_school", currentLang), fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTab == 0) {
                    // School List
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .height(420.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(schools, key = { it.id }) { school ->
                            val isSelected = school.id == selectedSchoolId
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectSchool(school.id)
                                    }
                                    .testTag("school_item_${school.id}"),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                ),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
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
                                                Text(
                                                    text = school.code.take(3),
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = school.name,
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                                )
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    Spacer(modifier = Modifier.width(2.dp))
                                                    Text(
                                                        text = "${school.city}, ${school.country}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }

                                        if (isSelected) {
                                            Surface(
                                                shape = RoundedCornerShape(20.dp),
                                                color = MaterialTheme.colorScheme.primary
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Actif", style = MaterialTheme.typography.labelSmall, color = Color.White)
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Subscription status badge
                                        val statusColor = when (school.subscriptionStatus) {
                                            SchoolSubscriptionStatus.ACTIVE -> Color(0xFF16A34A)
                                            SchoolSubscriptionStatus.PENDING_RENEWAL -> Color(0xFFD97706)
                                            SchoolSubscriptionStatus.EXPIRED -> Color(0xFFDC2626)
                                            SchoolSubscriptionStatus.TRIAL -> Color(0xFF2563EB)
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = statusColor.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = if (currentLang == com.example.util.AppLanguage.EN) school.subscriptionStatus.labelEn else school.subscriptionStatus.labelFr,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = statusColor),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }

                                        Text(
                                            text = "Abonnement : ${LocalizationUtil.formatFcfa(school.annualSubscriptionFee)} / an",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                viewModel.selectSchool(school.id)
                                                viewModel.setSubscriptionDialogOpen(true)
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Gérer Souscription", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Form New School
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .height(420.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Nom de l'établissement *") },
                            placeholder = { Text("Ex: Complexe Scolaire l'Élite") },
                            modifier = Modifier.fillMaxWidth().testTag("input_school_name"),
                            singleLine = true
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = code,
                                onValueChange = { code = it.uppercase() },
                                label = { Text("Code unique *") },
                                placeholder = { Text("Ex: CSE-ABJ") },
                                modifier = Modifier.weight(1f).testTag("input_school_code"),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = city,
                                onValueChange = { city = it },
                                label = { Text("Ville *") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = country,
                                onValueChange = { country = it },
                                label = { Text("Pays *") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text("Téléphone *") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email administratif") },
                            placeholder = { Text("direction@ecole.ci") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = registrationFeeStr,
                                onValueChange = { registrationFeeStr = it },
                                label = { Text("Frais Inscription (FCFA)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = annualFeeStr,
                                onValueChange = { annualFeeStr = it },
                                label = { Text("Abonnement Annuel (FCFA)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        if (errorMessage.isNotBlank()) {
                            Text(text = errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = {
                                if (name.isBlank() || code.isBlank()) {
                                    errorMessage = "Le nom et le code de l'école sont requis."
                                    return@Button
                                }
                                val regFee = registrationFeeStr.toDoubleOrNull() ?: 100000.0
                                val annFee = annualFeeStr.toDoubleOrNull() ?: 500000.0
                                viewModel.createSchool(
                                    name = name.trim(),
                                    code = code.trim(),
                                    city = city.trim(),
                                    country = country.trim(),
                                    phone = phone.trim(),
                                    email = email.trim(),
                                    registrationFee = regFee,
                                    annualSubscriptionFee = annFee
                                )
                                selectedTab = 0
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_save_new_school"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Enregistrer l'école & Initialiser l'espace", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
