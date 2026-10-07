package com.example.ui.screens.dashboard

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.viewmodel.DashboardStats
import com.example.util.AppLanguage
import com.example.util.LocalizationUtil
import java.util.Locale

/**
 * Dashboard Component that provides an at-a-glance overview of key metrics:
 * - 'Total Students'
 * - 'Active Teachers'
 * - 'Pending Payments'
 * alongside secondary key indicators using modern visual cards, with bilingual and Franc CFA (FCFA) support.
 */
@Composable
fun DashboardOverviewMetrics(
    stats: DashboardStats,
    onNavigateToStudents: () -> Unit,
    onNavigateToTeachers: () -> Unit,
    onNavigateToPayments: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToGrades: () -> Unit,
    language: AppLanguage = AppLanguage.FR,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("dashboard_key_metrics_overview"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (language == AppLanguage.EN) "Key Performance Indicators" else "Indicateurs Clés & Performance",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (language == AppLanguage.EN) "Real Time • FCFA" else "Temps Réel • FCFA",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Primary Trio Row: Total Students, Active Teachers, Pending Payments
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Visual Card: Total Students
            DashboardMetricCard(
                title = LocalizationUtil.t("total_students", language),
                secondaryTitle = if (language == AppLanguage.EN) "Students" else "Élèves",
                value = "${stats.totalStudents}",
                subtitle = if (language == AppLanguage.EN) "${stats.totalClasses} classes" else "${stats.totalClasses} classes actives",
                icon = Icons.Default.School,
                accentColor = NavyPrimary,
                testTag = "card_metric_total_students",
                modifier = Modifier.weight(1f),
                onClick = onNavigateToStudents
            )

            // Visual Card: Active Teachers
            DashboardMetricCard(
                title = LocalizationUtil.t("active_teachers", language),
                secondaryTitle = if (language == AppLanguage.EN) "Teachers" else "Enseignants",
                value = "${stats.activeTeachers}",
                subtitle = if (language == AppLanguage.EN) "Faculty members" else "Corps professoral",
                icon = Icons.Default.Person,
                accentColor = MaterialTheme.colorScheme.secondary,
                testTag = "card_metric_active_teachers",
                modifier = Modifier.weight(1f),
                onClick = onNavigateToTeachers
            )

            // Visual Card: Pending Payments (Formatted in FCFA)
            DashboardMetricCard(
                title = LocalizationUtil.t("pending_payments", language),
                secondaryTitle = if (language == AppLanguage.EN) "Payments" else "Paiements",
                value = "${stats.pendingPaymentsCount}",
                subtitle = if (stats.pendingPaymentsAmount > 0) "${LocalizationUtil.formatFcfa(stats.pendingPaymentsAmount)} dus" else "À jour",
                icon = Icons.Default.Payment,
                accentColor = if (stats.pendingPaymentsCount > 0) StatusWarning else StatusSuccess,
                testTag = "card_metric_pending_payments",
                modifier = Modifier.weight(1f),
                onClick = onNavigateToPayments
            )
        }

        // Secondary Row: Taux de présence & Moyenne générale
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DashboardMetricCard(
                title = LocalizationUtil.t("attendance_rate", language),
                secondaryTitle = if (language == AppLanguage.EN) "Attendance" else "Assiduité",
                value = "${String.format(Locale.US, "%.1f", stats.attendanceRate)}%",
                subtitle = if (stats.unjustifiedAbsences > 0) "${stats.unjustifiedAbsences} non justifiées" else "Assiduité optimale",
                icon = Icons.Default.CheckCircle,
                accentColor = StatusSuccess,
                testTag = "card_metric_attendance_rate",
                modifier = Modifier.weight(1f),
                onClick = onNavigateToAttendance
            )

            DashboardMetricCard(
                title = LocalizationUtil.t("general_average", language),
                secondaryTitle = if (language == AppLanguage.EN) "School Level" else "Niveau global",
                value = if (stats.globalAverage > 0) "${String.format(Locale.US, "%.2f", stats.globalAverage)}/20" else "--",
                subtitle = "Trimestre 1",
                icon = Icons.AutoMirrored.Filled.Assignment,
                accentColor = Color(0xFF7C3AED),
                testTag = "card_metric_global_average",
                modifier = Modifier.weight(1f),
                onClick = onNavigateToGrades
            )
        }
    }
}

@Composable
fun DashboardMetricCard(
    title: String,
    secondaryTitle: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .testTag(testTag)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = secondaryTitle,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
