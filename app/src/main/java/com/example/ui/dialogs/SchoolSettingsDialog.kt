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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.viewmodel.SchoolViewModel

@Composable
fun SchoolSettingsDialog(
    viewModel: SchoolViewModel,
    onDismiss: () -> Unit
) {
    val currentSchool by viewModel.currentSchool.collectAsStateWithLifecycle()

    var name by remember(currentSchool) { mutableStateOf(currentSchool?.name ?: "") }
    var code by remember(currentSchool) { mutableStateOf(currentSchool?.code ?: "") }
    var city by remember(currentSchool) { mutableStateOf(currentSchool?.city ?: "") }
    var country by remember(currentSchool) { mutableStateOf(currentSchool?.country ?: "") }
    var phone by remember(currentSchool) { mutableStateOf(currentSchool?.phone ?: "") }
    var email by remember(currentSchool) { mutableStateOf(currentSchool?.email ?: "") }
    var academicYear by remember(currentSchool) { mutableStateOf(currentSchool?.academicYear ?: "2025-2026") }
    var directorName by remember(currentSchool) { mutableStateOf(currentSchool?.directorName ?: "Dr. Robert Kouassi") }
    var officialMotto by remember(currentSchool) { mutableStateOf(currentSchool?.officialMotto ?: "Discipline - Travail - Succès") }
    var currency by remember(currentSchool) { mutableStateOf(currentSchool?.currency ?: "FCFA") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var showConfirmClearDialog by remember { mutableStateOf(false) }
    var clearStudentsOption by remember { mutableStateOf(false) }

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
                .testTag("dialog_school_settings")
        ) {
            Column(
                modifier = Modifier
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
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Informations de l'Établissement",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Configuration officielle & coordonnées de l'école",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("btn_close_school_settings")) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Fermer")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Business,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ces informations apparaissent sur tous les reçus de caisse officiels, les bulletins de notes et les attestations scolaires.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Form Fields
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom officiel de l'école *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Code Établissement *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = academicYear,
                        onValueChange = { academicYear = it },
                        label = { Text("Année Scolaire *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("Ville / Commune") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = country,
                        onValueChange = { country = it },
                        label = { Text("Pays") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Téléphone secrétariat") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email administratif") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = directorName,
                    onValueChange = { directorName = it },
                    label = { Text("Nom du Chef d'Établissement / Proviseur") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = officialMotto,
                        onValueChange = { officialMotto = it },
                        label = { Text("Devise de l'école") },
                        modifier = Modifier.weight(2f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = currency,
                        onValueChange = { currency = it },
                        label = { Text("Devise") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (successMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = successMessage ?: "",
                        color = Color(0xFF16A34A),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (name.isBlank() || code.isBlank()) {
                            errorMessage = "Le nom et le code de l'école sont obligatoires."
                            return@Button
                        }
                        viewModel.updateSchoolSettings(
                            name = name,
                            code = code,
                            city = city,
                            country = country,
                            phone = phone,
                            email = email,
                            academicYear = academicYear,
                            directorName = directorName,
                            officialMotto = officialMotto,
                            currency = currency
                        )
                        successMessage = "Paramètres de l'école enregistrés avec succès."
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_save_school_settings")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enregistrer les Modifications", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(20.dp))

                // Zone de Maintenance & Suppression des Données de Test
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CleaningServices,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Maintenance & Données de Test",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Au premier lancement, l'application contient des données de test (élèves fictifs, notes, paiements et présences d'exemple). Pour démarrer une véritable année scolaire propre :",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Option 1 : Supprimer notes & évaluations (Garder les élèves et classes)
                        OutlinedButton(
                            onClick = {
                                clearStudentsOption = false
                                showConfirmClearDialog = true
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_clear_test_evaluations")
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Vider les notes, présences & paiements (Conserver les élèves)")
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Option 2 : Remise à zéro totale (Supprimer aussi les élèves de test)
                        Button(
                            onClick = {
                                clearStudentsOption = true
                                showConfirmClearDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_clear_all_test_data")
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Remise à zéro complète (Supprimer aussi les élèves de test)")
                        }
                    }
                }
            }
        }
    }

    if (showConfirmClearDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmClearDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = if (clearStudentsOption) "Confirmer la remise à zéro totale ?" else "Vider les notes & évaluations de test ?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (clearStudentsOption)
                        "Cette action va supprimer définitivement TOUS les élèves de test, leurs notes, bulletins, cartes scolaires, pointages de présence et paiements d'exemple.\n\nLes classes et matières configurées seront conservées pour vous permettre d'inscrire vos vrais élèves."
                    else
                        "Cette action va réinitialiser à zéro toutes les notes saisies, les bulletins, les relevés d'absences et l'historique des paiements de test. Les élèves et les classes seront conservés."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearTestData(clearStudents = clearStudentsOption)
                        showConfirmClearDialog = false
                        successMessage = if (clearStudentsOption)
                            "Remise à zéro effectuée avec succès ! Les élèves de test ont été supprimés."
                        else
                            "Les notes, présences et paiements de test ont été vidés avec succès."
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Oui, Supprimer", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmClearDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}
