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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.viewmodel.SchoolViewModel
import com.example.util.BackupRestoreUtil
import com.example.util.LocalizationUtil
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BackupRestoreDialog(
    viewModel: SchoolViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val currentSchool by viewModel.currentSchool.collectAsStateWithLifecycle()
    val students by viewModel.students.collectAsStateWithLifecycle()
    val classes by viewModel.classes.collectAsStateWithLifecycle()
    val payments by viewModel.payments.collectAsStateWithLifecycle()
    val currentLang by viewModel.currentLanguage.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Exporter, 1: Restaurer
    var exportedFile by remember { mutableStateOf<File?>(null) }
    var restoreJsonText by remember { mutableStateOf("") }
    var restoreSuccessMessage by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("dialog_backup_restore")
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
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Backup,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Sauvegarde & Restauration",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${currentSchool?.name ?: "École"} • ${currentSchool?.code ?: "GSE"}",
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

                // Onglets Exporter / Restaurer
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Exporter (.JSON)", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                        icon = { Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Restaurer (Sinistre)", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                        icon = { Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTab == 0) {
                    // === ONGLET EXPORT SAUVEGARDE ===
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Plan de Continuité & Sauvegarde Hors-Ligne",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Générez une copie intégrale des données de votre école (élèves, classes, notes, assiduité, règlements en Franc CFA). Le fichier JSON autonome peut être conservé sur clé USB, disque dur ou drive sécurisé.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Données prêtes à être sauvegardées
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Élèves inscrits : ${students.size}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text("Classes : ${classes.size}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text("Paiements : ${payments.size}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (exportedFile != null) {
                        // Fichier généré avec succès
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF047857), modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Fichier de Sauvegarde Prêt", fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = exportedFile!!.name,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = Color(0xFF047857)
                                )
                                Text(
                                    text = "Taille : ${exportedFile!!.length() / 1024} Ko • Emplacement : Stockage local de l'appareil",
                                    fontSize = 11.sp,
                                    color = Color(0xFF065F46)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                BackupRestoreUtil.shareBackupFile(context, exportedFile!!)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("btn_share_backup"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sauvegarder sur Ordinateur / Drive / Clé USB")
                        }
                    } else {
                        Button(
                            onClick = {
                                isProcessing = true
                                val file = viewModel.exportSchoolBackup(context)
                                isProcessing = false
                                if (file != null) {
                                    exportedFile = file
                                    Toast.makeText(context, "Sauvegarde créée : ${file.name}", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Erreur création de sauvegarde", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_generate_backup"),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isProcessing
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isProcessing) "Génération du JSON..." else "Créer la Sauvegarde Locale",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    // === ONGLET RESTAURATION SINISTRE ===
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Restauration Système en Cas de Perte Technique",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF991B1B),
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "En cas de panne, de perte d'appareil ou d'incident serveur, injectez le fichier de sauvegarde JSON pour rétablir immédiatement l'ensemble des données de l'école.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFB91C1C)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (restoreSuccessMessage != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(restoreSuccessMessage!!, fontWeight = FontWeight.Bold, color = Color(0xFF047857))
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    OutlinedTextField(
                        value = restoreJsonText,
                        onValueChange = { restoreJsonText = it },
                        label = { Text("Contenu du Fichier de Sauvegarde JSON") },
                        placeholder = { Text("Collez le contenu JSON de votre sauvegarde ici...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .testTag("input_restore_json")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Option de chargement automatique de la dernière sauvegarde locale
                    OutlinedButton(
                        onClick = {
                            val backupDir = File(context.filesDir, "backups")
                            val lastFile = backupDir.listFiles()?.sortedByDescending { it.lastModified() }?.firstOrNull()
                            if (lastFile != null && lastFile.exists()) {
                                restoreJsonText = lastFile.readText(Charsets.UTF_8)
                                Toast.makeText(context, "Dernière sauvegarde locale chargée (${lastFile.name})", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Aucune sauvegarde locale précédente trouvée dans l'appareil.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Charger la dernière sauvegarde enregistrée sur l'appareil", fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (restoreJsonText.isBlank()) {
                                Toast.makeText(context, "Veuillez coller le JSON de sauvegarde ou charger un fichier.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isProcessing = true
                            val success = viewModel.restoreSchoolBackup(context, restoreJsonText)
                            isProcessing = false
                            if (success) {
                                restoreSuccessMessage = "Données scolaires restaurées avec succès !"
                                Toast.makeText(context, "Restauration terminée avec succès !", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Échec de restauration : format JSON invalide", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_confirm_restore"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isProcessing && restoreJsonText.isNotBlank()
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isProcessing) "Restauration en cours..." else "Rétablir les Données de l'École",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
