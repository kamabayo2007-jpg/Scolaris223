package com.example.ui.dialogs

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ui.viewmodel.SchoolViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddClassDialog(
    viewModel: SchoolViewModel,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var level by remember { mutableStateOf("Lycée") }
    var levelExpanded by remember { mutableStateOf(false) }
    val levels = listOf("Collège", "Lycée", "Primaire", "Supérieur")

    var room by remember { mutableStateOf("") }
    var mainTeacher by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("dialog_add_class")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Créer une Nouvelle Classe",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorMessage = null },
                    label = { Text("Nom de la classe (ex: Terminale S2, 4ème B)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_class_name")
                )

                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = levelExpanded,
                    onExpandedChange = { levelExpanded = !levelExpanded }
                ) {
                    OutlinedTextField(
                        value = level,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Niveau d'enseignement") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = levelExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = levelExpanded,
                        onDismissRequest = { levelExpanded = false }
                    ) {
                        levels.forEach { lvl ->
                            DropdownMenuItem(
                                text = { Text(lvl) },
                                onClick = {
                                    level = lvl
                                    levelExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = room,
                    onValueChange = { room = it },
                    label = { Text("Salle de classe principale (ex: Salle 304)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_class_room")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = mainTeacher,
                    onValueChange = { mainTeacher = it },
                    label = { Text("Professeur Principal (ex: M. Bernard)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_class_teacher")
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
                    OutlinedButton(onClick = onDismiss) { Text("Annuler") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                errorMessage = "Veuillez renseigner le nom de la classe."
                                return@Button
                            }
                            viewModel.addClass(name.trim(), level, room.trim(), mainTeacher.trim())
                        },
                        modifier = Modifier.testTag("btn_confirm_add_class")
                    ) {
                        Text("Enregistrer")
                    }
                }
            }
        }
    }
}

@Composable
fun AddSubjectDialog(
    viewModel: SchoolViewModel,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var coeffText by remember { mutableStateOf("2.0") }
    var selectedColor by remember { mutableStateOf("#2563EB") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val colors = listOf("#1D4ED8", "#7C3AED", "#B45309", "#047857", "#059669", "#C026D3", "#DC2626", "#EA580C")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("dialog_add_subject")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Créer une Matière",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = null
                        if (code.isBlank() && it.length >= 3) {
                            code = it.take(4).uppercase()
                        }
                    },
                    label = { Text("Intitulé (ex: Informatique / NSI)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_subject_name")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it.uppercase() },
                        label = { Text("Code (ex: NSI)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("input_subject_code")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = coeffText,
                        onValueChange = { coeffText = it },
                        label = { Text("Coefficient") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("input_subject_coeff")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Couleur thématique :", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colors.forEach { hex ->
                        val color = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { selectedColor = hex }
                                .then(
                                    if (selectedColor == hex) Modifier.size(36.dp) else Modifier
                                )
                        )
                    }
                }

                errorMessage?.let { err ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) { Text("Annuler") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val coeff = coeffText.toDoubleOrNull()
                            if (name.isBlank() || code.isBlank() || coeff == null || coeff <= 0) {
                                errorMessage = "Veuillez vérifier les informations saisies."
                                return@Button
                            }
                            viewModel.addSubject(name.trim(), code.trim(), coeff, selectedColor)
                        },
                        modifier = Modifier.testTag("btn_confirm_add_subject")
                    ) {
                        Text("Enregistrer")
                    }
                }
            }
        }
    }
}
