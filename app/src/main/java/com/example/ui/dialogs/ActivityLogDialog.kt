package com.example.ui.dialogs

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ActivityLog
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusDangerContainer
import com.example.ui.theme.StatusDangerText
import com.example.ui.theme.StatusInfo
import com.example.ui.theme.StatusInfoContainer
import com.example.ui.theme.StatusInfoText
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusSuccessContainer
import com.example.ui.theme.StatusSuccessText
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.StatusWarningContainer
import com.example.ui.theme.StatusWarningText
import com.example.util.PdfExportUtil
import com.example.ui.viewmodel.SchoolViewModel

@Composable
fun ActivityLogDialog(
    viewModel: SchoolViewModel,
    onDismiss: () -> Unit
) {
    val activityLogs by viewModel.activityLogs.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedCategory by remember { mutableStateOf("ALL") }
    val categories = listOf(
        "ALL" to "Tous les événements",
        "GRADE" to "Notes & Évaluations",
        "ATTENDANCE" to "Appel & Absences",
        "STUDENT" to "Élèves",
        "PAYMENT" to "Finances & Règlements",
        "SYSTEM" to "Système & Sécurité"
    )

    val filteredLogs = remember(activityLogs, selectedCategory) {
        if (selectedCategory == "ALL") activityLogs
        else activityLogs.filter { it.actionType == selectedCategory }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .testTag("dialog_activity_logs"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // En-tête
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
                                .background(Color(0xFFDC2626).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Journal d'Activité & Audit",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Traçabilité des modifications effectuées sur le système",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("btn_close_activity_logs")) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Toolbar d'actions : Filtres & Export PDF
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${filteredLogs.size} événement(s) enregistré(s)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedButton(
                        onClick = {
                            val file = com.example.util.PdfExportUtil.exportActivityLogsPdf(context, filteredLogs)
                            if (file != null) {
                                com.example.util.PdfExportUtil.shareOrViewPdf(context, file, "Journal d'Activité Système")
                            } else {
                                Toast.makeText(context, "Erreur génération du PDF", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.testTag("btn_export_activity_logs_pdf"),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Exporter Journal PDF", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Filtres de catégories
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(categories) { (key, label) ->
                        val isSelected = selectedCategory == key
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = key },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.testTag("chip_activity_category_$key")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Liste chronologique des activités
                if (filteredLogs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.History,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Aucune activité trouvée pour cette catégorie.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .testTag("list_chronological_activity_logs"),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredLogs, key = { it.id }) { log ->
                            ActivityLogItemCard(log = log)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActivityLogItemCard(
    log: ActivityLog,
    modifier: Modifier = Modifier
) {
    val (actionIcon, actionColor, bgContainer, textBadgeColor) = when (log.actionType) {
        "GRADE" -> Quadruple(Icons.Default.Assignment, Color(0xFF2563EB), StatusInfoContainer, StatusInfoText)
        "ATTENDANCE" -> Quadruple(Icons.Default.HowToReg, Color(0xFF059669), StatusSuccessContainer, StatusSuccessText)
        "PAYMENT" -> Quadruple(Icons.Default.Payment, Color(0xFFD97706), StatusWarningContainer, StatusWarningText)
        "STUDENT" -> Quadruple(Icons.Default.People, Color(0xFF7C3AED), Color(0xFFEDE9FE), Color(0xFF6D28D9))
        "SECURITY" -> Quadruple(Icons.Default.Shield, Color(0xFFDC2626), StatusDangerContainer, StatusDangerText)
        else -> Quadruple(Icons.Default.Security, Color(0xFF475569), Color(0xFFF1F5F9), Color(0xFF334155))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("item_activity_log_${log.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icône stylisée de l'action
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(actionColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = actionIcon,
                    contentDescription = log.actionType,
                    tint = actionColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Ligne Titre & Badge Type
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = log.description,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = bgContainer
                    ) {
                        Text(
                            text = log.actionType,
                            style = MaterialTheme.typography.labelSmall,
                            color = textBadgeColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Entité cible si présente
                if (log.targetEntity.isNotBlank()) {
                    Text(
                        text = "Cible : ${log.targetEntity}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Détails techniques
                if (log.details.isNotBlank()) {
                    Text(
                        text = log.details,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Auteur et Horodatage
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Par : ${log.userFullName} (${log.userRole})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = log.timestamp,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
