package com.example.ui.dialogs

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Grading
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.School
import com.example.data.model.SchoolNotification
import com.example.data.model.UserRole
import com.example.ui.viewmodel.SchoolViewModel
import com.example.util.PdfExportUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationDialog(
    viewModel: SchoolViewModel,
    onDismiss: () -> Unit
) {
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val schools by viewModel.schools.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val currentSchool: School = remember(schools) {
        schools.firstOrNull() ?: School(
            id = 1L,
            name = "Groupe Scolaire Excellence de Bamako",
            code = "GSE-BKO",
            directorName = "Dr. Robert Kouassi"
        )
    }

    var showCreateAnnouncement by remember { mutableStateOf(false) }
    var titleInput by remember { mutableStateOf("") }
    var messageInput by remember { mutableStateOf("") }
    var selectedCategoryInput by remember { mutableStateOf("ADMINISTRATIF") }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    var selectedFilterCategory by remember { mutableStateOf("TOUS") }

    val isStudent = currentUser.role == UserRole.ELEVE
    val isParent = currentUser.role == UserRole.PARENT
    val isTeacher = currentUser.role == UserRole.PROFESSEUR
    val isScolariteOrAdmin = currentUser.role in listOf(UserRole.ADMIN, UserRole.DIRECTION, UserRole.SUPER_ADMIN)

    // Role-tailored categories and notifications
    val roleAllowedNotifications = remember(notifications, currentUser.role) {
        notifications.filter { notif ->
            val matchRole = notif.targetRole == "ALL" || notif.targetRole == currentUser.role.name
            when {
                isStudent -> matchRole && notif.category != "PAIE_PROFESSEUR"
                isParent -> matchRole && notif.category != "PAIE_PROFESSEUR"
                isTeacher -> matchRole
                else -> true
            }
        }
    }

    val categoriesList = remember(currentUser.role) {
        val list = mutableListOf("TOUS" to "Tous les messages")
        if (isStudent || isParent || isScolariteOrAdmin) {
            list.add("NOTES_RESULTATS" to "Notes & Résultats")
        }
        if (isTeacher || isScolariteOrAdmin) {
            list.add("PAIE_PROFESSEUR" to "Mes Salaires & Paie")
        }
        if (!isTeacher) {
            list.add("PAIEMENT_SCOLARITE" to "Frais & Scolarité")
        }
        list.add("CALENDRIER" to "Vacances & Trimestres")
        list.add("ADMINISTRATIF" to "Vie Scolaire & Général")
        list
    }

    val filteredNotifications = remember(roleAllowedNotifications, selectedFilterCategory) {
        if (selectedFilterCategory == "TOUS") roleAllowedNotifications
        else roleAllowedNotifications.filter { it.category == selectedFilterCategory }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("dialog_notifications"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
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
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Centre d'Annonces & Notifications",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Flux personnalisé pour ${currentUser.fullName} (${currentUser.role.label})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Bar: Print PDF + Create Announcement (Scolarité)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            val file = PdfExportUtil.exportAnnouncementsPdf(context, filteredNotifications, currentSchool)
                            if (file != null) {
                                PdfExportUtil.printPdf(context, file, "Annonces Officielles")
                            } else {
                                Toast.makeText(context, "Erreur génération du PDF", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_print_announcements")
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Imprimer / PDF", fontSize = 12.sp)
                    }

                    if (isScolariteOrAdmin) {
                        Button(
                            onClick = { showCreateAnnouncement = !showCreateAnnouncement },
                            modifier = Modifier.testTag("btn_toggle_create_announcement"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (showCreateAnnouncement) "Fermer Rédaction" else "Diffuser une Annonce")
                        }
                    }
                }

                if (showCreateAnnouncement && isScolariteOrAdmin) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Nouvelle Annonce Scolaire", fontWeight = FontWeight.Bold)

                            OutlinedTextField(
                                value = titleInput,
                                onValueChange = { titleInput = it },
                                label = { Text("Titre de l'annonce *") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            ExposedDropdownMenuBox(
                                expanded = categoryDropdownExpanded,
                                onExpandedChange = { categoryDropdownExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = categoriesList.find { it.first == selectedCategoryInput }?.second ?: selectedCategoryInput,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Catégorie de l'annonce *") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = categoryDropdownExpanded,
                                    onDismissRequest = { categoryDropdownExpanded = false }
                                ) {
                                    categoriesList.filter { it.first != "TOUS" }.forEach { (catKey, catLabel) ->
                                        DropdownMenuItem(
                                            text = { Text(catLabel) },
                                            onClick = {
                                                selectedCategoryInput = catKey
                                                categoryDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = messageInput,
                                onValueChange = { messageInput = it },
                                label = { Text("Message officiel *") },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 3
                            )

                            Button(
                                onClick = {
                                    if (titleInput.isNotBlank() && messageInput.isNotBlank()) {
                                        viewModel.createNotification(
                                            title = titleInput.trim(),
                                            message = messageInput.trim(),
                                            targetRole = "ALL",
                                            type = selectedCategoryInput
                                        )
                                        titleInput = ""
                                        messageInput = ""
                                        showCreateAnnouncement = false
                                    }
                                },
                                modifier = Modifier.align(Alignment.End),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Publier l'Annonce")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Category Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categoriesList.forEach { (catKey, catLabel) ->
                        FilterChip(
                            selected = selectedFilterCategory == catKey,
                            onClick = { selectedFilterCategory = catKey },
                            label = { Text(catLabel, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (filteredNotifications.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aucune annonce dans cette catégorie.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(380.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredNotifications, key = { it.id }) { notif ->
                            NotificationCard(
                                notification = notif,
                                onClick = { viewModel.markNotificationRead(notif.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    notification: SchoolNotification,
    onClick: () -> Unit
) {
    val (tagColor, tagBg, catLabel) = when (notification.category) {
        "NOTES_RESULTATS" -> Triple(Color(0xFF2563EB), Color(0xFFDBEAFE), "Notes & Résultats")
        "PAIE_PROFESSEUR" -> Triple(Color(0xFF7C3AED), Color(0xFFF3E8FF), "Paie Enseignants")
        "PAIEMENT_SCOLARITE" -> Triple(Color(0xFF059669), Color(0xFFD1FAE5), "Finances Scolarité")
        "CALENDRIER" -> Triple(Color(0xFFD97706), Color(0xFFFEF3C7), "Calendrier & Vacances")
        else -> Triple(Color(0xFF4B5563), Color(0xFFE5E7EB), "Directives & Vie Scolaire")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("notif_card_${notification.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (!notification.isRead) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(tagBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (notification.category) {
                        "NOTES_RESULTATS" -> Icons.Default.Grading
                        "PAIE_PROFESSEUR" -> Icons.Default.Payments
                        "PAIEMENT_SCOLARITE" -> Icons.Default.Payment
                        "CALENDRIER" -> Icons.Default.Event
                        else -> Icons.Default.School
                    },
                    contentDescription = null,
                    tint = tagColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(tagBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = catLabel,
                            color = tagColor,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
