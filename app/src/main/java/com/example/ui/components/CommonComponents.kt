package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceStatus
import com.example.data.model.SchoolClass
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
import java.util.Locale

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .testTag("stat_card_${title.lowercase().replace(" ", "_")}")
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun GradeBadge(
    gradeValue: Double,
    outOf: Double = 20.0,
    modifier: Modifier = Modifier
) {
    val normalized = if (outOf > 0) (gradeValue / outOf) * 20.0 else gradeValue

    val (bg, textCol) = when {
        normalized >= 15.0 -> StatusSuccessContainer to StatusSuccessText
        normalized >= 12.0 -> StatusInfoContainer to StatusInfoText
        normalized >= 10.0 -> StatusWarningContainer to StatusWarningText
        else -> StatusDangerContainer to StatusDangerText
    }

    Surface(
        modifier = modifier.testTag("grade_badge_${gradeValue}"),
        shape = RoundedCornerShape(8.dp),
        color = bg
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = String.format(Locale.getDefault(), "%.1f", gradeValue),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = textCol
            )
            Text(
                text = "/${outOf.toInt()}",
                style = MaterialTheme.typography.labelSmall,
                color = textCol.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun AttendanceStatusChip(
    status: AttendanceStatus,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val (label, bg, textCol, icon) = when (status) {
        AttendanceStatus.PRESENT -> Quadruple(
            "Présent", StatusSuccessContainer, StatusSuccessText, Icons.Default.CheckCircle
        )
        AttendanceStatus.LATE -> Quadruple(
            "Retard", StatusWarningContainer, StatusWarningText, Icons.Default.Schedule
        )
        AttendanceStatus.ABSENT_JUSTIFIED -> Quadruple(
            "Abs. Justifiée", StatusInfoContainer, StatusInfoText, Icons.Default.HelpOutline
        )
        AttendanceStatus.ABSENT_UNJUSTIFIED -> Quadruple(
            "Abs. Non Justifiée", StatusDangerContainer, StatusDangerText, Icons.Default.Error
        )
    }

    Surface(
        modifier = modifier.testTag("attendance_chip_${status.name}"),
        shape = RoundedCornerShape(12.dp),
        color = bg
    ) {
        Row(
            modifier = Modifier.padding(horizontal = if (compact) 6.dp else 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textCol,
                modifier = Modifier.size(if (compact) 12.dp else 14.dp)
            )
            Text(
                text = label,
                style = if (compact) MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        else MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = textCol
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun StudentAvatar(
    name: String,
    colorHex: String,
    modifier: Modifier = Modifier,
    size: Int = 44
) {
    val parsedColor = try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (_: Exception) {
        MaterialTheme.colorScheme.primary
    }

    val initials = name.split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .uppercase(Locale.getDefault())

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(parsedColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = (size / 2.6).sp
            )
        )
    }
}

@Composable
fun ClassFilterRow(
    classes: List<SchoolClass>,
    selectedClassId: Long?,
    onSelectClass: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .testTag("class_filter_row"),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedClassId == null,
                onClick = { onSelectClass(null) },
                label = { Text("Toutes les classes") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.testTag("filter_all_classes")
            )
        }
        items(classes, key = { it.id }) { schoolClass ->
            FilterChip(
                selected = selectedClassId == schoolClass.id,
                onClick = { onSelectClass(schoolClass.id) },
                label = { Text(schoolClass.name) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.testTag("filter_class_${schoolClass.id}")
            )
        }
    }
}

@Composable
fun EmptyStateView(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
