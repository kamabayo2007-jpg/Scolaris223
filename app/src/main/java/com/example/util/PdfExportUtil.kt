package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.Attendance
import com.example.data.model.AttendanceStatus
import com.example.data.model.BulletinReport
import com.example.data.model.School
import com.example.data.model.SchoolClass
import com.example.data.model.SchoolNotification
import com.example.data.model.Student
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExportUtil {

    private const val PAGE_WIDTH = 595 // A4 standard width in points
    private const val PAGE_HEIGHT = 842 // A4 standard height in points
    private const val MARGIN = 36f

    /**
     * Exporte le bulletin scolaire officiel d'un élève au format PDF.
     */
    fun exportBulletinPdf(context: Context, report: BulletinReport): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 1. Fond blanc
        paint.color = Color.WHITE
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), paint)

        // 2. Bandeau d'en-tête Institutionnel
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E3A8A") // Bleu Marine Académique
        }
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 68f, headerPaint)

        // Décoration dorée
        val goldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D97706")
        }
        canvas.drawRect(0f, 68f, PAGE_WIDTH.toFloat(), 72f, goldPaint)

        // Texte En-tête
        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 10f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("RÉPUBLIQUE FRANÇAISE • MINISTÈRE DE L'ÉDUCATION NATIONALE", PAGE_WIDTH / 2f, 22f, paint)

        paint.textSize = 14f
        canvas.drawText("LYCÉE & COLLÈGE D'EXCELLENCE ACADÉMIQUE", PAGE_WIDTH / 2f, 40f, paint)

        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("BULLETIN SCOLAIRE OFFICIEL • ${report.period.uppercase()}", PAGE_WIDTH / 2f, 58f, paint)

        var currentY = 88f

        // 3. Cadre Identité Élève et Classe
        val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1f
            color = Color.parseColor("#CBD5E1")
        }
        val boxBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.parseColor("#F8FAFC")
        }
        val studentBox = RectF(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + 68f)
        canvas.drawRoundRect(studentBox, 8f, 8f, boxBgPaint)
        canvas.drawRoundRect(studentBox, 8f, 8f, boxPaint)

        paint.textAlign = Paint.Align.LEFT
        paint.color = Color.parseColor("#0F172A")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 12f
        canvas.drawText("Élève : ${report.student.fullName}", MARGIN + 12f, currentY + 20f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 9.5f
        paint.color = Color.parseColor("#475569")
        canvas.drawText("Matricule : ${report.student.matricule}", MARGIN + 12f, currentY + 36f, paint)
        canvas.drawText("Date de naissance : ${report.student.birthDate}", MARGIN + 12f, currentY + 52f, paint)

        // Colonne de droite dans le cadre
        val rightColX = PAGE_WIDTH / 2f + 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.parseColor("#0F172A")
        canvas.drawText("Classe : ${report.schoolClass.name} (${report.schoolClass.level})", rightColX, currentY + 20f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.parseColor("#475569")
        canvas.drawText("Prof. Principal : ${report.schoolClass.mainTeacher}", rightColX, currentY + 36f, paint)
        canvas.drawText("Année : ${report.academicYear} | Édité le : ${report.generatedDate}", rightColX, currentY + 52f, paint)

        currentY += 80f

        // 4. Titre du Tableau
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 11f
        paint.color = Color.parseColor("#1E3A8A")
        canvas.drawText("RÉSULTATS DISCIPLINAIRES & APPRÉCIATIONS", MARGIN, currentY, paint)
        currentY += 8f

        // 5. En-tête du tableau des matières
        val tableHeaderY = currentY
        val tableHeaderHeight = 22f
        val tableHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E293B")
        }
        canvas.drawRect(MARGIN, tableHeaderY, PAGE_WIDTH - MARGIN, tableHeaderY + tableHeaderHeight, tableHeaderPaint)

        paint.color = Color.WHITE
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("DISCIPLINE", MARGIN + 8f, tableHeaderY + 14f, paint)

        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("COEFF", MARGIN + 130f, tableHeaderY + 14f, paint)
        canvas.drawText("MOY /20", MARGIN + 180f, tableHeaderY + 14f, paint)

        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("ENSEIGNANT & APPRÉCIATION DE L'ÉQUIPE", MARGIN + 225f, tableHeaderY + 14f, paint)

        currentY += tableHeaderHeight

        // Lignes du tableau
        val rowHeight = 26f
        val altRowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F1F5F9")
        }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E2E8F0")
            strokeWidth = 0.5f
        }

        report.lines.forEachIndexed { index, line ->
            val rowY = currentY
            if (index % 2 == 1) {
                canvas.drawRect(MARGIN, rowY, PAGE_WIDTH - MARGIN, rowY + rowHeight, altRowPaint)
            }
            canvas.drawLine(MARGIN, rowY + rowHeight, PAGE_WIDTH - MARGIN, rowY + rowHeight, linePaint)

            // Nom de la matière
            paint.textAlign = Paint.Align.LEFT
            paint.color = Color.parseColor("#0F172A")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 9f
            canvas.drawText(line.subject.name, MARGIN + 8f, rowY + 16f, paint)

            // Coefficient
            paint.textAlign = Paint.Align.CENTER
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.color = Color.parseColor("#475569")
            canvas.drawText(String.format(Locale.US, "%.1f", line.coefficient), MARGIN + 130f, rowY + 16f, paint)

            // Moyenne /20
            if (line.average != null) {
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.color = when {
                    line.average >= 14.0 -> Color.parseColor("#047857") // Vert
                    line.average >= 10.0 -> Color.parseColor("#1D4ED8") // Bleu
                    else -> Color.parseColor("#DC2626") // Rouge
                }
                canvas.drawText(String.format(Locale.US, "%.2f", line.average), MARGIN + 180f, rowY + 16f, paint)
            } else {
                paint.color = Color.parseColor("#94A3B8")
                canvas.drawText("--", MARGIN + 180f, rowY + 16f, paint)
            }

            // Enseignant & Appréciation
            paint.textAlign = Paint.Align.LEFT
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.color = Color.parseColor("#334155")
            paint.textSize = 8.5f
            val teacherPrefix = "${line.teacherName} : "
            val fullAppreciation = teacherPrefix + line.appreciation
            val truncated = if (fullAppreciation.length > 56) fullAppreciation.take(53) + "..." else fullAppreciation
            canvas.drawText(truncated, MARGIN + 225f, rowY + 16f, paint)

            currentY += rowHeight
        }

        // Bordure globale du tableau
        canvas.drawRect(MARGIN, tableHeaderY, PAGE_WIDTH - MARGIN, currentY, boxPaint)

        currentY += 12f

        // 6. Cadre Bilan Global, Moyenne Générale & Assiduité
        val summaryBoxHeight = 84f
        val summaryBox = RectF(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + summaryBoxHeight)
        val summaryBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#EFF6FF") // Bleu clair
        }
        val summaryBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = Color.parseColor("#93C5FD")
            strokeWidth = 1f
        }
        canvas.drawRoundRect(summaryBox, 8f, 8f, summaryBg)
        canvas.drawRoundRect(summaryBox, 8f, 8f, summaryBorder)

        // Moyenne générale
        paint.textAlign = Paint.Align.LEFT
        paint.color = Color.parseColor("#1E3A8A")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 10f
        canvas.drawText("MOYENNE GÉNÉRALE DU TRIMESTRE :", MARGIN + 14f, currentY + 22f, paint)

        paint.textSize = 16f
        val avgStr = report.generalWeightedAverage?.let { String.format(Locale.US, "%.2f / 20", it) } ?: "-- / 20"
        canvas.drawText(avgStr, MARGIN + 14f, currentY + 44f, paint)

        // Mention / Bilan
        paint.textSize = 10f
        paint.color = Color.parseColor("#0F172A")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Avis du Conseil de Classe :", MARGIN + 14f, currentY + 62f, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        paint.color = Color.parseColor("#2563EB")
        canvas.drawText(report.generalAppreciation, MARGIN + 160f, currentY + 62f, paint)

        // Rangs & statistiques de classe (colonne droite)
        val rightStatsX = PAGE_WIDTH / 2f + 20f
        paint.textAlign = Paint.Align.LEFT
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 9.5f
        paint.color = Color.parseColor("#334155")
        canvas.drawText("Moyenne de classe : ${String.format(Locale.US, "%.2f", report.classAverage ?: 0.0)} / 20", rightStatsX, currentY + 22f, paint)
        canvas.drawText("Classement : ${report.classRank}e sur ${report.totalStudentsInClass} élèves", rightStatsX, currentY + 38f, paint)

        // Bilan Assiduité
        canvas.drawText(
            "Assiduité : ${report.totalAbsences} absence(s) (${report.unjustifiedAbsences} injustifiée(s)) • ${report.totalLates} retard(s)",
            rightStatsX,
            currentY + 54f,
            paint
        )

        currentY += summaryBoxHeight + 14f

        // 7. Bloc Signatures et Cachet Officiel
        val signBoxHeight = 88f
        val signBox = RectF(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + signBoxHeight)
        val signBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#F8FAFC") }
        canvas.drawRoundRect(signBox, 8f, 8f, signBg)
        canvas.drawRoundRect(signBox, 8f, 8f, boxPaint)

        // Signature Professeur Principal
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.parseColor("#475569")
        canvas.drawText("Visa du Professeur Principal", MARGIN + 14f, currentY + 18f, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.parseColor("#0F172A")
        canvas.drawText(report.schoolClass.mainTeacher, MARGIN + 14f, currentY + 34f, paint)
        paint.color = Color.parseColor("#94A3B8")
        canvas.drawText("[ Signature numérique validée ]", MARGIN + 14f, currentY + 68f, paint)

        // Cachet Officiel (Milieu)
        val stampCenterX = PAGE_WIDTH / 2f
        val stampCenterY = currentY + 44f
        val stampPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = Color.parseColor("#1E3A8A")
            strokeWidth = 1.5f
        }
        canvas.drawCircle(stampCenterX, stampCenterY, 32f, stampPaint)
        canvas.drawCircle(stampCenterX, stampCenterY, 28f, stampPaint)

        val stampTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E3A8A")
            textSize = 7f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("CACHET OFFICIEL", stampCenterX, stampCenterY - 8f, stampTextPaint)
        canvas.drawText("ÉCOLE HOMOLOGUÉE", stampCenterX, stampCenterY + 4f, stampTextPaint)
        canvas.drawText("RÉPUBLIQUE", stampCenterX, stampCenterY + 16f, stampTextPaint)

        // Signature Chef d'établissement (Droite)
        paint.textAlign = Paint.Align.RIGHT
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.parseColor("#475569")
        canvas.drawText("Le Chef d'Établissement", PAGE_WIDTH - MARGIN - 14f, currentY + 18f, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.parseColor("#0F172A")
        canvas.drawText("Mme Hélène Mercier", PAGE_WIDTH - MARGIN - 14f, currentY + 34f, paint)
        paint.color = Color.parseColor("#94A3B8")
        canvas.drawText("[ Document certifié conforme ]", PAGE_WIDTH - MARGIN - 14f, currentY + 68f, paint)

        // 8. Pied de page
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#94A3B8")
            textSize = 7.5f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "Ce bulletin scolaire est un document officiel délivré par le système d'information de l'établissement. Toute falsification est passible de sanctions.",
            PAGE_WIDTH / 2f,
            PAGE_HEIGHT - 16f,
            footerPaint
        )

        pdfDocument.finishPage(page)

        // Sauvegarde dans le cache
        return try {
            val reportsDir = File(context.cacheDir, "reports").apply { if (!exists()) mkdirs() }
            val cleanName = report.student.fullName.replace(" ", "_").lowercase(Locale.getDefault())
            val file = File(reportsDir, "bulletin_${cleanName}_T1.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    /**
     * Exporte un rapport officiel d'assiduité et de ponctualité au format PDF.
     */
    fun exportAttendanceReportPdf(
        context: Context,
        schoolClass: SchoolClass?,
        attendances: List<Attendance>,
        students: List<Student>,
        academicYear: String = "2025-2026"
    ): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Fond blanc
        paint.color = Color.WHITE
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), paint)

        // Bandeau d'en-tête
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F766E") // Vert Canard Académique
        }
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 68f, headerPaint)

        val goldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D97706")
        }
        canvas.drawRect(0f, 68f, PAGE_WIDTH.toFloat(), 72f, goldPaint)

        // Textes d'en-tête
        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 10f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("VIE SCOLAIRE • DIRECTION PÉDAGOGIQUE", PAGE_WIDTH / 2f, 22f, paint)

        paint.textSize = 13f
        canvas.drawText("RAPPORT OFFICIEL D'ASSIDUITÉ & DE PONCTUALITÉ", PAGE_WIDTH / 2f, 40f, paint)

        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val className = schoolClass?.name ?: "Toutes les Classes de l'Établissement"
        canvas.drawText("Classe : $className • Année Scolaire $academicYear", PAGE_WIDTH / 2f, 58f, paint)

        var currentY = 88f

        // Cartouches KPI Statistiques
        val totalSessions = attendances.size
        val presentCount = attendances.count { it.status == AttendanceStatus.PRESENT }
        val lateCount = attendances.count { it.status == AttendanceStatus.LATE }
        val unjustifiedCount = attendances.count { it.status == AttendanceStatus.ABSENT_UNJUSTIFIED }
        val justifiedCount = attendances.count { it.status == AttendanceStatus.ABSENT_JUSTIFIED }
        val presenceRate = if (totalSessions > 0) {
            ((presentCount + lateCount).toDouble() / totalSessions.toDouble()) * 100.0
        } else 100.0

        val kpiWidth = (PAGE_WIDTH - (MARGIN * 2) - 30f) / 4f
        val kpiHeight = 52f

        val kpiData = listOf(
            Triple("Taux de présence", String.format(Locale.US, "%.1f%%", presenceRate), "#047857"),
            Triple("Séances totales", "$totalSessions", "#1E3A8A"),
            Triple("Absences injustifiées", "$unjustifiedCount", if (unjustifiedCount > 0) "#DC2626" else "#047857"),
            Triple("Retards constatés", "$lateCount", if (lateCount > 0) "#D97706" else "#64748B")
        )

        kpiData.forEachIndexed { i, kpi ->
            val kpiX = MARGIN + (i * (kpiWidth + 10f))
            val kpiBox = RectF(kpiX, currentY, kpiX + kpiWidth, currentY + kpiHeight)
            val kpiBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#F8FAFC") }
            val kpiBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                color = Color.parseColor("#E2E8F0")
                strokeWidth = 1f
            }
            canvas.drawRoundRect(kpiBox, 6f, 6f, kpiBg)
            canvas.drawRoundRect(kpiBox, 6f, 6f, kpiBorder)

            paint.textAlign = Paint.Align.CENTER
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 8.5f
            paint.color = Color.parseColor("#64748B")
            canvas.drawText(kpi.first, kpiX + (kpiWidth / 2f), currentY + 18f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 14f
            paint.color = Color.parseColor(kpi.third)
            canvas.drawText(kpi.second, kpiX + (kpiWidth / 2f), currentY + 40f, paint)
        }

        currentY += kpiHeight + 16f

        // Titre du registre
        paint.textAlign = Paint.Align.LEFT
        paint.color = Color.parseColor("#0F766E")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 11f
        canvas.drawText("REGISTRE DES ÉMARGEMENTS & SUIVI INDIVIDUEL", MARGIN, currentY, paint)
        currentY += 8f

        // En-tête de table
        val tableHeaderY = currentY
        val tableHeaderHeight = 22f
        val tableHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#134E4A") }
        canvas.drawRect(MARGIN, tableHeaderY, PAGE_WIDTH - MARGIN, tableHeaderY + tableHeaderHeight, tableHeaderPaint)

        paint.color = Color.WHITE
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("DATE", MARGIN + 8f, tableHeaderY + 14f, paint)
        canvas.drawText("CRÉNEAU", MARGIN + 70f, tableHeaderY + 14f, paint)
        canvas.drawText("ÉLÈVE CONCERNÉ", MARGIN + 145f, tableHeaderY + 14f, paint)
        canvas.drawText("STATUT", MARGIN + 310f, tableHeaderY + 14f, paint)
        canvas.drawText("MOTIF / JUSTIFICATIF", MARGIN + 410f, tableHeaderY + 14f, paint)

        currentY += tableHeaderHeight

        val studentsMap = students.associateBy { it.id }
        val rowHeight = 22f
        val altRowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#F0FDFA") }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E2E8F0")
            strokeWidth = 0.5f
        }

        val recordsToPrint = attendances.take(24) // Limite ergonomique pour tenir sur la page
        recordsToPrint.forEachIndexed { index, att ->
            val rowY = currentY
            if (index % 2 == 1) {
                canvas.drawRect(MARGIN, rowY, PAGE_WIDTH - MARGIN, rowY + rowHeight, altRowPaint)
            }
            canvas.drawLine(MARGIN, rowY + rowHeight, PAGE_WIDTH - MARGIN, rowY + rowHeight, linePaint)

            paint.color = Color.parseColor("#334155")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 8.5f

            // Date
            canvas.drawText(att.date, MARGIN + 8f, rowY + 14f, paint)

            // Créneau
            canvas.drawText(att.timeSlot, MARGIN + 70f, rowY + 14f, paint)

            // Nom élève
            val studentName = studentsMap[att.studentId]?.fullName ?: "Élève #${att.studentId}"
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(studentName.take(24), MARGIN + 145f, rowY + 14f, paint)

            // Statut
            val (statusLabel, statusColor) = when (att.status) {
                AttendanceStatus.PRESENT -> "PRÉSENT" to "#047857"
                AttendanceStatus.ABSENT_UNJUSTIFIED -> "ABS. INJUSTIFIÉE" to "#DC2626"
                AttendanceStatus.ABSENT_JUSTIFIED -> "ABS. JUSTIFIÉE" to "#475569"
                AttendanceStatus.LATE -> "RETARD" to "#D97706"
            }
            paint.color = Color.parseColor(statusColor)
            canvas.drawText(statusLabel, MARGIN + 310f, rowY + 14f, paint)

            // Justification / Motif
            paint.color = Color.parseColor("#64748B")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            val justificationText = if (att.reason.isNotBlank()) {
                att.reason
            } else if (att.status == AttendanceStatus.ABSENT_JUSTIFIED) {
                "Motif médical / Famille"
            } else if (att.status == AttendanceStatus.ABSENT_UNJUSTIFIED) {
                "Non justifié à ce jour"
            } else {
                att.remarks.ifBlank { "RAS" }
            }
            canvas.drawText(justificationText.take(24), MARGIN + 410f, rowY + 14f, paint)

            currentY += rowHeight
        }

        // Bordure du tableau
        val tableBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = Color.parseColor("#CBD5E1")
            strokeWidth = 1f
        }
        canvas.drawRect(MARGIN, tableHeaderY, PAGE_WIDTH - MARGIN, currentY, tableBorder)

        currentY += 14f

        // Bloc Signatures Vie Scolaire & Direction
        val signBoxHeight = 74f
        val signBox = RectF(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + signBoxHeight)
        val signBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#F8FAFC") }
        canvas.drawRoundRect(signBox, 8f, 8f, signBg)
        canvas.drawRoundRect(signBox, 8f, 8f, tableBorder)

        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.parseColor("#134E4A")
        canvas.drawText("Conseiller Principal d'Éducation (CPE)", MARGIN + 14f, currentY + 18f, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.parseColor("#475569")
        canvas.drawText("Service de la Vie Scolaire", MARGIN + 14f, currentY + 34f, paint)
        canvas.drawText("[ Registre visé et conforme ]", MARGIN + 14f, currentY + 54f, paint)

        // Cachet
        val stampCenterX = PAGE_WIDTH / 2f
        val stampCenterY = currentY + 36f
        val stampPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = Color.parseColor("#0F766E")
            strokeWidth = 1.5f
        }
        canvas.drawCircle(stampCenterX, stampCenterY, 26f, stampPaint)
        val stampTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F766E")
            textSize = 6.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("VIE SCOLAIRE", stampCenterX, stampCenterY - 4f, stampTextPaint)
        canvas.drawText("CERTIFIÉ", stampCenterX, stampCenterY + 6f, stampTextPaint)

        // Direction
        paint.textAlign = Paint.Align.RIGHT
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.parseColor("#134E4A")
        canvas.drawText("La Direction de l'Établissement", PAGE_WIDTH - MARGIN - 14f, currentY + 18f, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.parseColor("#475569")
        canvas.drawText("Mme Hélène Mercier", PAGE_WIDTH - MARGIN - 14f, currentY + 34f, paint)
        canvas.drawText("Transmis aux parents & archives", PAGE_WIDTH - MARGIN - 14f, currentY + 54f, paint)

        // Pied de page
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#94A3B8")
            textSize = 7.5f
            textAlign = Paint.Align.CENTER
        }
        val todayStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText(
            "Document généré le $todayStr par le Système ERP Gestion École. Certifié conforme aux registres d'assiduité officiels.",
            PAGE_WIDTH / 2f,
            PAGE_HEIGHT - 16f,
            footerPaint
        )

        pdfDocument.finishPage(page)

        return try {
            val reportsDir = File(context.cacheDir, "reports").apply { if (!exists()) mkdirs() }
            val file = File(reportsDir, "rapport_assiduite_${System.currentTimeMillis()}.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    /**
     * Exporte la liste officielle des élèves au format PDF structuré.
     */
    fun exportStudentsListPdf(
        context: Context,
        students: List<com.example.data.model.StudentOverview>,
        className: String?
    ): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.WHITE
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), paint)

        // Bandeau d'en-tête Marine
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#1E3A8A") }
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 68f, headerPaint)
        val goldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#D97706") }
        canvas.drawRect(0f, 68f, PAGE_WIDTH.toFloat(), 72f, goldPaint)

        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 10f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("DIRECTION PÉDAGOGIQUE • REGISTRE DE SCOLARITÉ", PAGE_WIDTH / 2f, 22f, paint)

        paint.textSize = 13f
        canvas.drawText("LISTE OFFICIELLE DES ÉLÈVES & SUIVI ACADÉMIQUE", PAGE_WIDTH / 2f, 40f, paint)

        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val sub = className?.let { "Classe : $it • ${students.size} élèves inscrits" } ?: "Toutes les Classes • ${students.size} élèves inscrits"
        canvas.drawText(sub, PAGE_WIDTH / 2f, 58f, paint)

        var currentY = 88f

        // En-tête du tableau
        val tableHeaderHeight = 22f
        val tableHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#1E293B") }
        canvas.drawRect(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + tableHeaderHeight, tableHeaderPaint)

        paint.color = Color.WHITE
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("MATRICULE", MARGIN + 8f, currentY + 14f, paint)
        canvas.drawText("NOM & PRÉNOM", MARGIN + 90f, currentY + 14f, paint)
        canvas.drawText("CLASSE", MARGIN + 260f, currentY + 14f, paint)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("MOYENNE", MARGIN + 350f, currentY + 14f, paint)
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("STATUT / ASSIDUITÉ", MARGIN + 400f, currentY + 14f, paint)

        currentY += tableHeaderHeight

        val rowHeight = 22f
        val altRowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#F8FAFC") }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#E2E8F0"); strokeWidth = 0.5f }

        students.take(28).forEachIndexed { index, item ->
            val rowY = currentY
            if (index % 2 == 1) {
                canvas.drawRect(MARGIN, rowY, PAGE_WIDTH - MARGIN, rowY + rowHeight, altRowPaint)
            }
            canvas.drawLine(MARGIN, rowY + rowHeight, PAGE_WIDTH - MARGIN, rowY + rowHeight, linePaint)

            paint.color = Color.parseColor("#475569")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 8f
            paint.textAlign = Paint.Align.LEFT

            // Matricule
            canvas.drawText(item.student.matricule, MARGIN + 8f, rowY + 14f, paint)

            // Nom
            paint.color = Color.parseColor("#0F172A")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(item.student.fullName.take(28), MARGIN + 90f, rowY + 14f, paint)

            // Classe
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.color = Color.parseColor("#2563EB")
            canvas.drawText(item.className, MARGIN + 260f, rowY + 14f, paint)

            // Moyenne
            paint.textAlign = Paint.Align.CENTER
            if (item.generalAverage != null) {
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.color = if (item.generalAverage >= 14.0) Color.parseColor("#047857") else Color.parseColor("#1E3A8A")
                canvas.drawText(String.format(Locale.US, "%.2f", item.generalAverage), MARGIN + 350f, rowY + 14f, paint)
            } else {
                paint.color = Color.parseColor("#94A3B8")
                canvas.drawText("--", MARGIN + 350f, rowY + 14f, paint)
            }

            // Statut
            paint.textAlign = Paint.Align.LEFT
            val statusStr = when {
                item.unjustifiedAbsencesCount > 0 -> "${item.unjustifiedAbsencesCount} abs. injustifiée(s)"
                item.latesCount > 0 -> "${item.latesCount} retard(s)"
                else -> "Assiduité parfaite"
            }
            paint.color = if (item.unjustifiedAbsencesCount > 0) Color.parseColor("#DC2626") else Color.parseColor("#047857")
            canvas.drawText(statusStr, MARGIN + 400f, rowY + 14f, paint)

            currentY += rowHeight
        }

        // Bordure du tableau
        val tableBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; color = Color.parseColor("#CBD5E1"); strokeWidth = 1f }
        canvas.drawRect(MARGIN, 88f, PAGE_WIDTH - MARGIN, currentY, tableBorder)

        // Pied de page
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#94A3B8"); textSize = 7.5f; textAlign = Paint.Align.CENTER }
        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("Document officiel généré le $dateStr • Établissement Scolaire ERP", PAGE_WIDTH / 2f, PAGE_HEIGHT - 16f, footerPaint)

        pdfDocument.finishPage(page)

        return try {
            val reportsDir = File(context.cacheDir, "reports").apply { if (!exists()) mkdirs() }
            val file = File(reportsDir, "liste_eleves_${System.currentTimeMillis()}.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    /**
     * Exporte la liste officielle des enseignants au format PDF structuré.
     */
    fun exportTeachersListPdf(
        context: Context,
        teachers: List<com.example.data.model.Teacher>,
        subjects: List<com.example.data.model.Subject>
    ): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.WHITE
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), paint)

        // Bandeau En-tête Violet Académique
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#7C3AED") }
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 68f, headerPaint)
        val goldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#D97706") }
        canvas.drawRect(0f, 68f, PAGE_WIDTH.toFloat(), 72f, goldPaint)

        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 10f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("DIRECTION DES RESSOURCES PÉDAGOGIQUES", PAGE_WIDTH / 2f, 22f, paint)

        paint.textSize = 13f
        canvas.drawText("RÉPERTOIRE OFFICIEL DU CORPS PROFESSORAL", PAGE_WIDTH / 2f, 40f, paint)

        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("${teachers.size} enseignants actifs enregistrés", PAGE_WIDTH / 2f, 58f, paint)

        var currentY = 88f

        // En-tête du tableau
        val tableHeaderHeight = 22f
        val tableHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#1E293B") }
        canvas.drawRect(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + tableHeaderHeight, tableHeaderPaint)

        paint.color = Color.WHITE
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("ENSEIGNANT", MARGIN + 8f, currentY + 14f, paint)
        canvas.drawText("DISCIPLINE PRINCIPALE", MARGIN + 180f, currentY + 14f, paint)
        canvas.drawText("E-MAIL PROFESSIONNEL", MARGIN + 320f, currentY + 14f, paint)
        canvas.drawText("TÉLÉPHONE", MARGIN + 450f, currentY + 14f, paint)

        currentY += tableHeaderHeight

        val rowHeight = 24f
        val altRowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#FAF5FF") }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#E2E8F0"); strokeWidth = 0.5f }

        teachers.forEachIndexed { index, teacher ->
            val rowY = currentY
            if (index % 2 == 1) {
                canvas.drawRect(MARGIN, rowY, PAGE_WIDTH - MARGIN, rowY + rowHeight, altRowPaint)
            }
            canvas.drawLine(MARGIN, rowY + rowHeight, PAGE_WIDTH - MARGIN, rowY + rowHeight, linePaint)

            paint.textAlign = Paint.Align.LEFT

            // Nom
            paint.color = Color.parseColor("#0F172A")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 8.5f
            canvas.drawText(teacher.fullName, MARGIN + 8f, rowY + 15f, paint)

            // Matière
            paint.color = Color.parseColor("#7C3AED")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText(teacher.subjectName.ifBlank { "Générale" }, MARGIN + 180f, rowY + 15f, paint)

            // Email
            paint.color = Color.parseColor("#475569")
            canvas.drawText(teacher.email, MARGIN + 320f, rowY + 15f, paint)

            // Tel
            paint.color = Color.parseColor("#334155")
            canvas.drawText(teacher.phone.ifBlank { "--" }, MARGIN + 450f, rowY + 15f, paint)

            currentY += rowHeight
        }

        val tableBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; color = Color.parseColor("#CBD5E1"); strokeWidth = 1f }
        canvas.drawRect(MARGIN, 88f, PAGE_WIDTH - MARGIN, currentY, tableBorder)

        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#94A3B8"); textSize = 7.5f; textAlign = Paint.Align.CENTER }
        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("Document confidentiel généré le $dateStr • Système ERP Gestion École", PAGE_WIDTH / 2f, PAGE_HEIGHT - 16f, footerPaint)

        pdfDocument.finishPage(page)

        return try {
            val reportsDir = File(context.cacheDir, "reports").apply { if (!exists()) mkdirs() }
            val file = File(reportsDir, "corps_professoral_${System.currentTimeMillis()}.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    /**
     * Exporte le relevé général des notes et évaluations au format PDF structuré.
     */
    fun exportGradesListPdf(
        context: Context,
        grades: List<com.example.data.model.Grade>,
        students: Map<Long, com.example.data.model.Student>,
        subjects: Map<Long, com.example.data.model.Subject>,
        classes: Map<Long, com.example.data.model.SchoolClass>,
        period: String
    ): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.WHITE
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), paint)

        // Bandeau En-tête Bleu Sombre
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#0369A1") }
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 68f, headerPaint)
        val goldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#D97706") }
        canvas.drawRect(0f, 68f, PAGE_WIDTH.toFloat(), 72f, goldPaint)

        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 10f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("COMMISSION PÉDAGOGIQUE • SUIVI DES ÉVALUATIONS", PAGE_WIDTH / 2f, 22f, paint)

        paint.textSize = 13f
        canvas.drawText("RELEVÉ GÉNÉRAL DES NOTES & CONTRÔLES", PAGE_WIDTH / 2f, 40f, paint)

        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Période : $period • ${grades.size} évaluation(s) répertoriée(s)", PAGE_WIDTH / 2f, 58f, paint)

        var currentY = 88f

        // En-tête de tableau
        val tableHeaderHeight = 22f
        val tableHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#1E293B") }
        canvas.drawRect(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + tableHeaderHeight, tableHeaderPaint)

        paint.color = Color.WHITE
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("DATE", MARGIN + 8f, currentY + 14f, paint)
        canvas.drawText("ÉLÈVE", MARGIN + 70f, currentY + 14f, paint)
        canvas.drawText("CLASSE", MARGIN + 210f, currentY + 14f, paint)
        canvas.drawText("MATIÈRE", MARGIN + 270f, currentY + 14f, paint)
        canvas.drawText("TITRE", MARGIN + 370f, currentY + 14f, paint)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("NOTE /20", MARGIN + 490f, currentY + 14f, paint)

        currentY += tableHeaderHeight

        val rowHeight = 22f
        val altRowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#F0F9FF") }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#E2E8F0"); strokeWidth = 0.5f }

        grades.take(28).forEachIndexed { index, grade ->
            val rowY = currentY
            if (index % 2 == 1) {
                canvas.drawRect(MARGIN, rowY, PAGE_WIDTH - MARGIN, rowY + rowHeight, altRowPaint)
            }
            canvas.drawLine(MARGIN, rowY + rowHeight, PAGE_WIDTH - MARGIN, rowY + rowHeight, linePaint)

            paint.textAlign = Paint.Align.LEFT
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 8f
            paint.color = Color.parseColor("#475569")

            // Date
            canvas.drawText(grade.date, MARGIN + 8f, rowY + 14f, paint)

            // Élève
            val stu = students[grade.studentId]
            paint.color = Color.parseColor("#0F172A")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText((stu?.fullName ?: "Élève #${grade.studentId}").take(22), MARGIN + 70f, rowY + 14f, paint)

            // Classe
            val cls = classes[grade.classId]
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.color = Color.parseColor("#0369A1")
            canvas.drawText(cls?.name ?: "--", MARGIN + 210f, rowY + 14f, paint)

            // Matière
            val sub = subjects[grade.subjectId]
            paint.color = Color.parseColor("#334155")
            canvas.drawText((sub?.name ?: "--").take(16), MARGIN + 270f, rowY + 14f, paint)

            // Titre
            paint.color = Color.parseColor("#64748B")
            canvas.drawText(grade.title.take(18), MARGIN + 370f, rowY + 14f, paint)

            // Note
            paint.textAlign = Paint.Align.CENTER
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.color = if (grade.normalizedTo20 >= 14.0) Color.parseColor("#047857") else if (grade.normalizedTo20 >= 10.0) Color.parseColor("#1D4ED8") else Color.parseColor("#DC2626")
            canvas.drawText(String.format(Locale.US, "%.1f", grade.normalizedTo20), MARGIN + 490f, rowY + 14f, paint)

            currentY += rowHeight
        }

        val tableBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; color = Color.parseColor("#CBD5E1"); strokeWidth = 1f }
        canvas.drawRect(MARGIN, 88f, PAGE_WIDTH - MARGIN, currentY, tableBorder)

        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#94A3B8"); textSize = 7.5f; textAlign = Paint.Align.CENTER }
        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("Extrait certifié conforme délivré le $dateStr • ERP Gestion École", PAGE_WIDTH / 2f, PAGE_HEIGHT - 16f, footerPaint)

        pdfDocument.finishPage(page)

        return try {
            val reportsDir = File(context.cacheDir, "reports").apply { if (!exists()) mkdirs() }
            val file = File(reportsDir, "releve_notes_${System.currentTimeMillis()}.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    /**
     * Exporte le Journal d'Activité / Piste d'audit au format PDF structuré pour les administrateurs.
     */
    fun exportActivityLogsPdf(
        context: Context,
        logs: List<com.example.data.model.ActivityLog>
    ): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.WHITE
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), paint)

        // Bandeau En-tête Rouge/Gris Administration
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#334155") }
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 68f, headerPaint)
        val redPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#DC2626") }
        canvas.drawRect(0f, 68f, PAGE_WIDTH.toFloat(), 72f, redPaint)

        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 10f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("ADMINISTRATION SYSTÈME • SÉCURITÉ & AUDIT", PAGE_WIDTH / 2f, 22f, paint)

        paint.textSize = 13f
        canvas.drawText("JOURNAL D'ACTIVITÉ & TRAÇABILITÉ DES MODIFICATIONS", PAGE_WIDTH / 2f, 40f, paint)

        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("${logs.size} événements système répertoriés", PAGE_WIDTH / 2f, 58f, paint)

        var currentY = 88f

        val tableHeaderHeight = 22f
        val tableHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#0F172A") }
        canvas.drawRect(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + tableHeaderHeight, tableHeaderPaint)

        paint.color = Color.WHITE
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("DATE & HEURE", MARGIN + 8f, currentY + 14f, paint)
        canvas.drawText("UTILISATEUR & RÔLE", MARGIN + 120f, currentY + 14f, paint)
        canvas.drawText("TYPE", MARGIN + 260f, currentY + 14f, paint)
        canvas.drawText("DESCRIPTION DE L'ACTION", MARGIN + 320f, currentY + 14f, paint)

        currentY += tableHeaderHeight

        val rowHeight = 24f
        val altRowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#F8FAFC") }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#E2E8F0"); strokeWidth = 0.5f }

        logs.take(28).forEachIndexed { index, log ->
            val rowY = currentY
            if (index % 2 == 1) {
                canvas.drawRect(MARGIN, rowY, PAGE_WIDTH - MARGIN, rowY + rowHeight, altRowPaint)
            }
            canvas.drawLine(MARGIN, rowY + rowHeight, PAGE_WIDTH - MARGIN, rowY + rowHeight, linePaint)

            paint.textAlign = Paint.Align.LEFT
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 7.5f
            paint.color = Color.parseColor("#475569")

            canvas.drawText(log.timestamp, MARGIN + 8f, rowY + 15f, paint)

            paint.color = Color.parseColor("#0F172A")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("${log.userFullName} (${log.userRole})".take(24), MARGIN + 120f, rowY + 15f, paint)

            paint.color = when (log.actionType) {
                "GRADE" -> Color.parseColor("#2563EB")
                "ATTENDANCE" -> Color.parseColor("#059669")
                "PAYMENT" -> Color.parseColor("#D97706")
                "STUDENT" -> Color.parseColor("#7C3AED")
                else -> Color.parseColor("#DC2626")
            }
            canvas.drawText(log.actionType, MARGIN + 260f, rowY + 15f, paint)

            paint.color = Color.parseColor("#334155")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            val fullDesc = if (log.targetEntity.isNotBlank()) "${log.description} [${log.targetEntity}]" else log.description
            canvas.drawText(fullDesc.take(38), MARGIN + 320f, rowY + 15f, paint)

            currentY += rowHeight
        }

        val tableBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; color = Color.parseColor("#CBD5E1"); strokeWidth = 1f }
        canvas.drawRect(MARGIN, 88f, PAGE_WIDTH - MARGIN, currentY, tableBorder)

        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#94A3B8"); textSize = 7.5f; textAlign = Paint.Align.CENTER }
        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("Piste d'audit certifiée générée le $dateStr • Accès Administrateur", PAGE_WIDTH / 2f, PAGE_HEIGHT - 16f, footerPaint)

        pdfDocument.finishPage(page)

        return try {
            val reportsDir = File(context.cacheDir, "reports").apply { if (!exists()) mkdirs() }
            val file = File(reportsDir, "journal_activite_${System.currentTimeMillis()}.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    /**
     * Exporte le reçu officiel de caisse d'un paiement en FCFA au format PDF structuré.
     */
    fun exportPaymentReceiptPdf(
        context: Context,
        payment: com.example.data.model.Payment,
        student: com.example.data.model.Student?,
        schoolClass: com.example.data.model.SchoolClass?,
        schoolName: String = "Groupe Scolaire Excellence Cocody"
    ): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.WHITE
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), paint)

        // Bandeau En-tête Vert Émeraude / Bleu Sombre
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#047857") }
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 72f, headerPaint)
        val goldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#D97706") }
        canvas.drawRect(0f, 72f, PAGE_WIDTH.toFloat(), 76f, goldPaint)

        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 11f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(schoolName.uppercase(), PAGE_WIDTH / 2f, 24f, paint)

        paint.textSize = 14f
        canvas.drawText("REÇU OFFICIEL DE CAISSE & FACTURATION SCOLAIRE", PAGE_WIDTH / 2f, 44f, paint)

        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Année Scolaire ${payment.academicYear} • Enregistrement Sécurisé", PAGE_WIDTH / 2f, 62f, paint)

        var currentY = 96f

        // Bloc Récépissé & Date
        val metaBox = RectF(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + 44f)
        val metaBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#F8FAFC") }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = Color.parseColor("#E2E8F0")
            strokeWidth = 1f
        }
        canvas.drawRoundRect(metaBox, 6f, 6f, metaBg)
        canvas.drawRoundRect(metaBox, 6f, 6f, borderPaint)

        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 9f
        paint.color = Color.parseColor("#64748B")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("RÉCÉPISSÉ N°", MARGIN + 12f, currentY + 18f, paint)
        paint.textSize = 12f
        paint.color = Color.parseColor("#0F172A")
        canvas.drawText(payment.receiptNumber, MARGIN + 12f, currentY + 34f, paint)

        paint.textAlign = Paint.Align.RIGHT
        paint.textSize = 9f
        paint.color = Color.parseColor("#64748B")
        canvas.drawText("DATE D'ÉMISSION", PAGE_WIDTH - MARGIN - 12f, currentY + 18f, paint)
        paint.textSize = 12f
        paint.color = Color.parseColor("#0F172A")
        canvas.drawText(payment.date, PAGE_WIDTH - MARGIN - 12f, currentY + 34f, paint)

        currentY += 56f

        // Bloc Identité Élève
        val stuBox = RectF(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + 54f)
        val stuBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#F1F5F9") }
        canvas.drawRoundRect(stuBox, 6f, 6f, stuBg)
        canvas.drawRoundRect(stuBox, 6f, 6f, borderPaint)

        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 8.5f
        paint.color = Color.parseColor("#475569")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("DÉSIGNATION DE L'ÉLÈVE CONCERNÉ", MARGIN + 12f, currentY + 16f, paint)

        paint.textSize = 13f
        paint.color = Color.parseColor("#0F172A")
        canvas.drawText(student?.fullName ?: "Élève Inconnu", MARGIN + 12f, currentY + 32f, paint)

        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.parseColor("#334155")
        canvas.drawText("Matricule : ${student?.matricule ?: "N/A"}", MARGIN + 12f, currentY + 46f, paint)

        paint.textAlign = Paint.Align.RIGHT
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.parseColor("#1D4ED8")
        canvas.drawText("Classe : ${schoolClass?.name ?: "N/A"}", PAGE_WIDTH - MARGIN - 12f, currentY + 46f, paint)

        currentY += 66f

        // Tableau des Rubriques & Montants en FCFA
        val tableHeaderHeight = 22f
        val tableHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#047857") }
        canvas.drawRect(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + tableHeaderHeight, tableHeaderPaint)

        paint.color = Color.WHITE
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("DESIGNATION DU VERSEMENT", MARGIN + 12f, currentY + 14f, paint)
        canvas.drawText("MODE DE PAIEMENT", MARGIN + 230f, currentY + 14f, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("MONTANT PAYÉ (FCFA)", PAGE_WIDTH - MARGIN - 12f, currentY + 14f, paint)

        currentY += tableHeaderHeight

        // Ligne de détail
        val rowHeight = 28f
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E2E8F0")
            strokeWidth = 0.5f
        }
        canvas.drawLine(MARGIN, currentY + rowHeight, PAGE_WIDTH - MARGIN, currentY + rowHeight, linePaint)

        paint.textAlign = Paint.Align.LEFT
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9.5f
        paint.color = Color.parseColor("#0F172A")
        canvas.drawText(payment.type.label, MARGIN + 12f, currentY + 18f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 9f
        paint.color = Color.parseColor("#475569")
        canvas.drawText(payment.paymentMethod, MARGIN + 230f, currentY + 18f, paint)

        paint.textAlign = Paint.Align.RIGHT
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 11f
        paint.color = Color.parseColor("#047857")
        val amountStr = LocalizationUtil.formatFcfa(payment.amountPaid)
        canvas.drawText(amountStr, PAGE_WIDTH - MARGIN - 12f, currentY + 18f, paint)

        currentY += rowHeight + 14f

        // Récapitulatif Total, Versé, Solde Restant
        val recapBox = RectF(PAGE_WIDTH / 2f, currentY, PAGE_WIDTH - MARGIN, currentY + 74f)
        canvas.drawRoundRect(recapBox, 6f, 6f, metaBg)
        canvas.drawRoundRect(recapBox, 6f, 6f, borderPaint)

        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 9f
        paint.color = Color.parseColor("#64748B")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Montant Total Facturé :", PAGE_WIDTH / 2f + 12f, currentY + 18f, paint)
        canvas.drawText("Montant Versé / Encaissé :", PAGE_WIDTH / 2f + 12f, currentY + 38f, paint)
        canvas.drawText("Solde Restant Dû :", PAGE_WIDTH / 2f + 12f, currentY + 58f, paint)

        paint.textAlign = Paint.Align.RIGHT
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.parseColor("#0F172A")
        canvas.drawText(LocalizationUtil.formatFcfa(payment.amountTotal), PAGE_WIDTH - MARGIN - 12f, currentY + 18f, paint)

        paint.color = Color.parseColor("#047857")
        canvas.drawText(amountStr, PAGE_WIDTH - MARGIN - 12f, currentY + 38f, paint)

        paint.color = if (payment.remainingAmount > 0) Color.parseColor("#DC2626") else Color.parseColor("#16A34A")
        canvas.drawText(LocalizationUtil.formatFcfa(payment.remainingAmount), PAGE_WIDTH - MARGIN - 12f, currentY + 58f, paint)

        currentY += 90f

        // Cachet & Signature de Validation
        val signBox = RectF(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + 68f)
        canvas.drawRoundRect(signBox, 6f, 6f, metaBg)
        canvas.drawRoundRect(signBox, 6f, 6f, borderPaint)

        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.parseColor("#047857")
        canvas.drawText("SERVICE DE COMPTABILITÉ SCOLAIRE", MARGIN + 12f, currentY + 18f, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8.5f
        paint.color = Color.parseColor("#64748B")
        canvas.drawText("Mode de règlement : ${payment.paymentMethod}", MARGIN + 12f, currentY + 34f, paint)
        canvas.drawText("[ Paiement vérifié et certifié conforme ]", MARGIN + 12f, currentY + 50f, paint)

        paint.textAlign = Paint.Align.RIGHT
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.parseColor("#0F172A")
        canvas.drawText("Le Caissier / Chef Comptable", PAGE_WIDTH - MARGIN - 12f, currentY + 18f, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        paint.textSize = 8.5f
        paint.color = Color.parseColor("#64748B")
        canvas.drawText("Fatou Diallo", PAGE_WIDTH - MARGIN - 12f, currentY + 34f, paint)

        // Pied de page
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#94A3B8")
            textSize = 7.5f
            textAlign = Paint.Align.CENTER
        }
        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("Quittance officielle délivrée le $dateStr • ${schoolName} • Devise Officielle Franc CFA (FCFA)", PAGE_WIDTH / 2f, PAGE_HEIGHT - 16f, footerPaint)

        pdfDocument.finishPage(page)

        return try {
            val reportsDir = File(context.cacheDir, "reports").apply { if (!exists()) mkdirs() }
            val file = File(reportsDir, "recu_${payment.receiptNumber}.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    /**
     * Exporte la Carte Scolaire officielle d'un élève au format PDF (Badge / Format Tour de cou).
     */
    fun exportStudentCardPdf(
        context: Context,
        student: Student,
        schoolClass: SchoolClass?,
        school: School
    ): File? {
        val CARD_WIDTH = 280
        val CARD_HEIGHT = 440
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(CARD_WIDTH, CARD_HEIGHT, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 1. Fond de la carte
        paint.color = Color.WHITE
        canvas.drawRect(0f, 0f, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(), paint)

        // 2. Encoche / Fente pour cordon tour de cou (Lanyard hole)
        val holePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E2E8F0")
            style = Paint.Style.FILL
        }
        val holeStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#94A3B8")
            style = Paint.Style.STROKE
            strokeWidth = 1.2f
        }
        val holeRect = RectF(CARD_WIDTH / 2f - 24f, 6f, CARD_WIDTH / 2f + 24f, 16f)
        canvas.drawRoundRect(holeRect, 5f, 5f, holePaint)
        canvas.drawRoundRect(holeRect, 5f, 5f, holeStroke)

        // 3. Drapeau du Mali en ruban tricolore supérieur (Vert, Jaune, Rouge)
        val flagTop = 22f
        val flagHeight = 6f
        val stripeWidth = CARD_WIDTH / 3f

        paint.color = Color.parseColor("#16A34A") // Vert
        canvas.drawRect(0f, flagTop, stripeWidth, flagTop + flagHeight, paint)

        paint.color = Color.parseColor("#EAB308") // Jaune
        canvas.drawRect(stripeWidth, flagTop, stripeWidth * 2f, flagTop + flagHeight, paint)

        paint.color = Color.parseColor("#DC2626") // Rouge
        canvas.drawRect(stripeWidth * 2f, flagTop, CARD_WIDTH.toFloat(), flagTop + flagHeight, paint)

        // 4. En-tête Institutionnel
        val headerBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E3A8A") // Bleu Marine Institutionnel
        }
        canvas.drawRect(0f, flagTop + flagHeight, CARD_WIDTH.toFloat(), 95f, headerBg)

        // Ornements dorés
        val goldLine = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F59E0B")
        }
        canvas.drawRect(0f, 95f, CARD_WIDTH.toFloat(), 97f, goldLine)

        paint.color = Color.WHITE
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 8.5f
        canvas.drawText("RÉPUBLIQUE DU MALI", CARD_WIDTH / 2f, 40f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 6.5f
        paint.color = Color.parseColor("#FDE68A")
        canvas.drawText("Un Peuple • Un But • Une Foi", CARD_WIDTH / 2f, 49f, paint)

        paint.color = Color.WHITE
        paint.textSize = 7.5f
        canvas.drawText(school.academyName.ifBlank { "Académie d'Enseignement de Bamako" }.uppercase(), CARD_WIDTH / 2f, 60f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 10f
        canvas.drawText(school.name.uppercase(), CARD_WIDTH / 2f, 75f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 7f
        paint.color = Color.parseColor("#CBD5E1")
        canvas.drawText("CARTE D'IDENTITÉ SCOLAIRE • ${school.academicYear}", CARD_WIDTH / 2f, 88f, paint)

        // 5. Cadre Photo / Avatar de l'élève
        val photoLeft = 18f
        val photoTop = 108f
        val photoSize = 74f

        val photoBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = try {
                Color.parseColor(student.avatarColorHex)
            } catch (_: Exception) {
                Color.parseColor("#2563EB")
            }
        }
        val photoFrame = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = Color.parseColor("#F59E0B")
        }
        val photoRect = RectF(photoLeft, photoTop, photoLeft + photoSize, photoTop + photoSize)
        canvas.drawRoundRect(photoRect, 10f, 10f, photoBg)
        canvas.drawRoundRect(photoRect, 10f, 10f, photoFrame)

        // Initiales au centre de la photo
        paint.color = Color.WHITE
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 24f
        val initials = "${student.firstName.take(1)}${student.lastName.take(1)}".uppercase()
        canvas.drawText(initials, photoLeft + photoSize / 2f, photoTop + photoSize / 2f + 8f, paint)

        // 6. Informations Identité Élève
        val infoLeft = photoLeft + photoSize + 12f
        paint.textAlign = Paint.Align.LEFT
        paint.color = Color.parseColor("#0F172A")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 12f
        canvas.drawText("${student.lastName.uppercase()} ${student.firstName}", infoLeft, 124f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8.5f
        paint.color = Color.parseColor("#475569")
        canvas.drawText("Matricule :", infoLeft, 140f, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.parseColor("#1E3A8A")
        canvas.drawText(student.matricule, infoLeft + 48f, 140f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8.5f
        paint.color = Color.parseColor("#475569")
        canvas.drawText("Classe :", infoLeft, 154f, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.parseColor("#059669")
        canvas.drawText(schoolClass?.name ?: "Classe", infoLeft + 38f, 154f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8f
        paint.color = Color.parseColor("#64748B")
        canvas.drawText("Né(e) le : ${student.birthDate} (${student.gender})", infoLeft, 168f, paint)

        // 7. Statut Académie / Candidat Examen Officiel
        val statusTop = 192f
        val statusRect = RectF(14f, statusTop, CARD_WIDTH - 14f, statusTop + 36f)
        val statusBg = Paint(Paint.ANTI_ALIAS_FLAG)
        val statusBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        if (student.isExamCandidate) {
            if (student.academyExamNumber.isNotBlank()) {
                statusBg.color = Color.parseColor("#ECFDF5")
                statusBorder.color = Color.parseColor("#10B981")
                canvas.drawRoundRect(statusRect, 6f, 6f, statusBg)
                canvas.drawRoundRect(statusRect, 6f, 6f, statusBorder)

                paint.textAlign = Paint.Align.CENTER
                paint.color = Color.parseColor("#065F46")
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 8.5f
                canvas.drawText("✓ CANDIDAT OFFICIEL EXAMEN ACADÉMIE", CARD_WIDTH / 2f, statusTop + 14f, paint)

                paint.textSize = 9.5f
                paint.color = Color.parseColor("#047857")
                canvas.drawText("N° Table Académie : ${student.academyExamNumber}", CARD_WIDTH / 2f, statusTop + 28f, paint)
            } else {
                statusBg.color = Color.parseColor("#FFFBEB")
                statusBorder.color = Color.parseColor("#F59E0B")
                canvas.drawRoundRect(statusRect, 6f, 6f, statusBg)
                canvas.drawRoundRect(statusRect, 6f, 6f, statusBorder)

                paint.textAlign = Paint.Align.CENTER
                paint.color = Color.parseColor("#B45309")
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 8f
                canvas.drawText("⚠ CANDIDAT EXAMEN EN COURS D'ATTRIBUTION", CARD_WIDTH / 2f, statusTop + 14f, paint)

                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.textSize = 7f
                paint.color = Color.parseColor("#78350F")
                canvas.drawText("Numéro officiel en attente des listes de l'Académie", CARD_WIDTH / 2f, statusTop + 27f, paint)
            }
        } else {
            statusBg.color = Color.parseColor("#F1F5F9")
            statusBorder.color = Color.parseColor("#CBD5E1")
            canvas.drawRoundRect(statusRect, 6f, 6f, statusBg)
            canvas.drawRoundRect(statusRect, 6f, 6f, statusBorder)

            paint.textAlign = Paint.Align.CENTER
            paint.color = Color.parseColor("#334155")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 8.5f
            canvas.drawText("RÉGIME SCOLAIRE GÉNÉRAL INTERNE", CARD_WIDTH / 2f, statusTop + 16f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 7f
            paint.color = Color.parseColor("#64748B")
            canvas.drawText("Élève régulièrement inscrit en classe de ${schoolClass?.name ?: ""}", CARD_WIDTH / 2f, statusTop + 28f, paint)
        }

        // 8. Zone Code QR & Code-barres pour présence et appel rapide
        val qrTop = 238f
        val qrBox = RectF(14f, qrTop, CARD_WIDTH - 14f, qrTop + 104f)
        val qrBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F8FAFC")
        }
        val qrBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1f
            color = Color.parseColor("#E2E8F0")
        }
        canvas.drawRoundRect(qrBox, 8f, 8f, qrBg)
        canvas.drawRoundRect(qrBox, 8f, 8f, qrBorder)

        // Dessin d'une matrice QR Code stylisée
        val qrCodeSize = 64f
        val qrLeft = 24f
        val qrBoxTop = qrTop + 10f
        val blackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.BLACK }

        // Carrés de repère (Finder patterns)
        canvas.drawRect(qrLeft, qrBoxTop, qrLeft + 18f, qrBoxTop + 18f, blackPaint)
        canvas.drawRect(qrLeft + 46f, qrBoxTop, qrLeft + 64f, qrBoxTop + 18f, blackPaint)
        canvas.drawRect(qrLeft, qrBoxTop + 46f, qrLeft + 18f, qrBoxTop + 64f, blackPaint)

        // Pixels centraux
        val hash = (student.matricule + student.id).hashCode()
        for (i in 0 until 6) {
            for (j in 0 until 6) {
                if (((hash shr (i * 2 + j)) and 1) == 1) {
                    canvas.drawRect(qrLeft + 22f + i * 4f, qrBoxTop + 12f + j * 6f, qrLeft + 25f + i * 4f, qrBoxTop + 16f + j * 6f, blackPaint)
                }
            }
        }

        // Texte d'instruction du scan
        paint.textAlign = Paint.Align.LEFT
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 8.5f
        paint.color = Color.parseColor("#0F172A")
        canvas.drawText("SCANNER SCOLARIS", qrLeft + qrCodeSize + 10f, qrTop + 26f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 7f
        paint.color = Color.parseColor("#64748B")
        canvas.drawText("Appel, présence & contrôle", qrLeft + qrCodeSize + 10f, qrTop + 38f, paint)
        canvas.drawText("Code : ${student.effectiveQrData.take(20)}...", qrLeft + qrCodeSize + 10f, qrTop + 50f, paint)

        paint.textSize = 6.5f
        paint.color = Color.parseColor("#2563EB")
        canvas.drawText("Urgence : ${student.parentPhone}", qrLeft + qrCodeSize + 10f, qrTop + 66f, paint)

        // 9. Pied de la Carte & Signature du Directeur
        val footerTop = 352f
        val sealPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1.2f
            color = Color.parseColor("#1E3A8A")
        }
        val sealRadius = 24f
        val sealCenterX = 48f
        val sealCenterY = footerTop + 32f
        canvas.drawCircle(sealCenterX, sealCenterY, sealRadius, sealPaint)

        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 5f
        paint.color = Color.parseColor("#1E3A8A")
        canvas.drawText("RÉP. DU MALI", sealCenterX, sealCenterY - 10f, paint)
        canvas.drawText("★ SCEAU ★", sealCenterX, sealCenterY, paint)
        canvas.drawText(school.academicYear, sealCenterX, sealCenterY + 10f, paint)

        // Signature
        paint.textAlign = Paint.Align.RIGHT
        paint.color = Color.parseColor("#0F172A")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 7.5f
        canvas.drawText("Le Directeur de l'Établissement", CARD_WIDTH - 20f, footerTop + 20f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 7f
        paint.color = Color.parseColor("#475569")
        canvas.drawText(school.directorName.ifBlank { "Dr. Robert Kouassi" }, CARD_WIDTH - 20f, footerTop + 34f, paint)

        paint.textSize = 6f
        paint.color = Color.parseColor("#94A3B8")
        canvas.drawText("Signature & Cachet Officiels", CARD_WIDTH - 20f, footerTop + 46f, paint)

        // 10. Bordure extérieure sécurisée de la carte
        val cardBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
            color = Color.parseColor("#CBD5E1")
        }
        val outerRect = RectF(1f, 1f, CARD_WIDTH.toFloat() - 1f, CARD_HEIGHT.toFloat() - 1f)
        canvas.drawRoundRect(outerRect, 12f, 12f, cardBorder)

        pdfDocument.finishPage(page)

        return try {
            val fileName = "Carte_Scolaire_${student.matricule}_${student.lastName}.pdf"
            val file = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    /**
     * Exporte en lot toutes les cartes scolaires d'une classe sur un document multipage.
     */
    fun exportClassStudentCardsPdf(
        context: Context,
        students: List<Student>,
        classesMap: Map<Long, SchoolClass>,
        school: School
    ): File? {
        if (students.isEmpty()) return null

        val CARD_WIDTH = 280
        val CARD_HEIGHT = 440
        val pdfDocument = PdfDocument()

        for ((index, student) in students.withIndex()) {
            val pageInfo = PdfDocument.PageInfo.Builder(CARD_WIDTH, CARD_HEIGHT, index + 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            val schoolClass = classesMap[student.classId]

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            // Fond
            paint.color = Color.WHITE
            canvas.drawRect(0f, 0f, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(), paint)

            // Fente tour de cou
            val holeRect = RectF(CARD_WIDTH / 2f - 24f, 6f, CARD_WIDTH / 2f + 24f, 16f)
            val holePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#E2E8F0") }
            canvas.drawRoundRect(holeRect, 5f, 5f, holePaint)

            // Drapeau Mali
            val flagTop = 22f
            val stripeWidth = CARD_WIDTH / 3f
            paint.color = Color.parseColor("#16A34A")
            canvas.drawRect(0f, flagTop, stripeWidth, flagTop + 6f, paint)
            paint.color = Color.parseColor("#EAB308")
            canvas.drawRect(stripeWidth, flagTop, stripeWidth * 2f, flagTop + 6f, paint)
            paint.color = Color.parseColor("#DC2626")
            canvas.drawRect(stripeWidth * 2f, flagTop, CARD_WIDTH.toFloat(), flagTop + 6f, paint)

            // Bandeau
            val headerBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#1E3A8A") }
            canvas.drawRect(0f, 28f, CARD_WIDTH.toFloat(), 95f, headerBg)

            paint.color = Color.WHITE
            paint.textAlign = Paint.Align.CENTER
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 8f
            canvas.drawText("RÉPUBLIQUE DU MALI • ${school.academyName.uppercase()}", CARD_WIDTH / 2f, 44f, paint)
            paint.textSize = 10f
            canvas.drawText(school.name.uppercase(), CARD_WIDTH / 2f, 65f, paint)
            paint.textSize = 7f
            paint.color = Color.parseColor("#FDE68A")
            canvas.drawText("CARTE SCOLAIRE • ${school.academicYear}", CARD_WIDTH / 2f, 82f, paint)

            // Photo
            val photoLeft = 18f
            val photoTop = 108f
            val photoSize = 74f
            val photoBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = try { Color.parseColor(student.avatarColorHex) } catch (_: Exception) { Color.parseColor("#2563EB") }
            }
            canvas.drawRoundRect(RectF(photoLeft, photoTop, photoLeft + photoSize, photoTop + photoSize), 10f, 10f, photoBg)

            paint.color = Color.WHITE
            paint.textSize = 22f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("${student.firstName.take(1)}${student.lastName.take(1)}".uppercase(), photoLeft + photoSize / 2f, photoTop + photoSize / 2f + 8f, paint)

            // Nom et classe
            val infoLeft = photoLeft + photoSize + 12f
            paint.textAlign = Paint.Align.LEFT
            paint.color = Color.parseColor("#0F172A")
            paint.textSize = 11.5f
            canvas.drawText("${student.lastName.uppercase()} ${student.firstName}", infoLeft, 126f, paint)

            paint.textSize = 8.5f
            paint.color = Color.parseColor("#475569")
            canvas.drawText("Matricule : ${student.matricule}", infoLeft, 142f, paint)
            canvas.drawText("Classe : ${schoolClass?.name ?: "Classe"}", infoLeft, 156f, paint)
            canvas.drawText("Né(e) : ${student.birthDate}", infoLeft, 170f, paint)

            // Statut examen
            val statusTop = 194f
            val statusRect = RectF(14f, statusTop, CARD_WIDTH - 14f, statusTop + 34f)
            val statusBg = Paint(Paint.ANTI_ALIAS_FLAG)
            if (student.isExamCandidate) {
                statusBg.color = Color.parseColor("#ECFDF5")
                canvas.drawRoundRect(statusRect, 6f, 6f, statusBg)
                paint.textAlign = Paint.Align.CENTER
                paint.color = Color.parseColor("#065F46")
                paint.textSize = 8f
                canvas.drawText("CANDIDAT EXAMEN ACADÉMIE", CARD_WIDTH / 2f, statusTop + 14f, paint)
                paint.textSize = 9f
                val examNum = student.academyExamNumber.ifBlank { "En attente attribution Académie" }
                canvas.drawText("N° : $examNum", CARD_WIDTH / 2f, statusTop + 27f, paint)
            } else {
                statusBg.color = Color.parseColor("#F1F5F9")
                canvas.drawRoundRect(statusRect, 6f, 6f, statusBg)
                paint.textAlign = Paint.Align.CENTER
                paint.color = Color.parseColor("#334155")
                paint.textSize = 8f
                canvas.drawText("RÉGIME GÉNÉRAL STANDARD", CARD_WIDTH / 2f, statusTop + 20f, paint)
            }

            // QR Code info
            paint.textAlign = Paint.Align.LEFT
            paint.color = Color.BLACK
            paint.textSize = 8f
            canvas.drawText("QR CODE IDENTITÉ : ${student.effectiveQrData}", 16f, 255f, paint)

            // Bas de page
            paint.textAlign = Paint.Align.RIGHT
            paint.color = Color.parseColor("#0F172A")
            paint.textSize = 7f
            canvas.drawText("Le Directeur : ${school.directorName}", CARD_WIDTH - 16f, 400f, paint)

            pdfDocument.finishPage(page)
        }

        return try {
            val fileName = "Planche_Cartes_Scolaires_${school.code}_${System.currentTimeMillis()}.pdf"
            val file = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    /**
     * Exporte les annonces officielles de l'établissement au format PDF.
     */
    fun exportAnnouncementsPdf(
        context: Context,
        announcements: List<SchoolNotification>,
        school: School
    ): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Fond
        paint.color = Color.WHITE
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), paint)

        // Bandeau
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#1E3A8A") }
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 72f, headerPaint)

        paint.color = Color.WHITE
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 10f
        canvas.drawText("RÉPUBLIQUE DU MALI • MINISTÈRE DE L'ÉDUCATION NATIONALE", PAGE_WIDTH / 2f, 24f, paint)

        paint.textSize = 13f
        canvas.drawText(school.name.uppercase(), PAGE_WIDTH / 2f, 44f, paint)

        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("BULLETIN D'ANNONCES OFFICIELLES & COMMUNICATIONS DE LA SCOLARITÉ", PAGE_WIDTH / 2f, 60f, paint)

        var currentY = 100f
        paint.textAlign = Paint.Align.LEFT

        for (notif in announcements.take(8)) {
            val boxRect = RectF(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + 68f)
            val boxBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = when (notif.category) {
                    "NOTES", "EXAMEN" -> Color.parseColor("#FEF3C7")
                    "FINANCE", "PAIEMENT" -> Color.parseColor("#E0E7FF")
                    "VACANCES", "CALENDRIER" -> Color.parseColor("#DCFCE7")
                    else -> Color.parseColor("#F8FAFC")
                }
            }
            canvas.drawRoundRect(boxRect, 6f, 6f, boxBg)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 11f
            paint.color = Color.parseColor("#0F172A")
            canvas.drawText("● [${notif.category}] ${notif.title}", MARGIN + 12f, currentY + 22f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 8.5f
            paint.color = Color.parseColor("#475569")
            canvas.drawText(notif.message, MARGIN + 12f, currentY + 40f, paint)

            paint.textSize = 7.5f
            paint.color = Color.parseColor("#64748B")
            canvas.drawText("Date : ${notif.date} • Destiné à : ${notif.targetRole}", MARGIN + 12f, currentY + 56f, paint)

            currentY += 76f
        }

        // Signature bas
        paint.textAlign = Paint.Align.RIGHT
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 10f
        paint.color = Color.parseColor("#1E3A8A")
        canvas.drawText("Le Directeur : ${school.directorName}", PAGE_WIDTH - MARGIN, PAGE_HEIGHT - 40f, paint)

        pdfDocument.finishPage(page)

        return try {
            val fileName = "Annonces_Officielles_${school.code}_${System.currentTimeMillis()}.pdf"
            val file = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    /**
     * Lance l'impression d'un fichier PDF via le PrintManager Android.
     */
    fun printPdf(context: Context, file: File, jobName: String) {
        try {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? android.print.PrintManager
            if (printManager != null) {
                val printAdapter = object : android.print.PrintDocumentAdapter() {
                    override fun onLayout(
                        oldAttributes: android.print.PrintAttributes?,
                        newAttributes: android.print.PrintAttributes?,
                        cancellationSignal: android.os.CancellationSignal?,
                        callback: LayoutResultCallback?,
                        extras: android.os.Bundle?
                    ) {
                        if (cancellationSignal?.isCanceled == true) {
                            callback?.onLayoutCancelled()
                            return
                        }
                        val info = android.print.PrintDocumentInfo.Builder(file.name)
                            .setContentType(android.print.PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                            .build()
                        callback?.onLayoutFinished(info, true)
                    }

                    override fun onWrite(
                        pages: Array<out android.print.PageRange>?,
                        destination: android.os.ParcelFileDescriptor?,
                        cancellationSignal: android.os.CancellationSignal?,
                        callback: WriteResultCallback?
                    ) {
                        try {
                            val input = java.io.FileInputStream(file)
                            val output = java.io.FileOutputStream(destination?.fileDescriptor)
                            input.copyTo(output)
                            input.close()
                            output.close()
                            callback?.onWriteFinished(arrayOf(android.print.PageRange.ALL_PAGES))
                        } catch (e: Exception) {
                            callback?.onWriteFailed(e.message)
                        }
                    }
                }
                printManager.print(jobName, printAdapter, null)
            } else {
                shareOrViewPdf(context, file, jobName)
            }
        } catch (_: Exception) {
            shareOrViewPdf(context, file, jobName)
        }
    }

    /**
     * Ouvre ou partage le fichier PDF généré avec l'application de lecture PDF ou de partage du système.
     */
    fun shareOrViewPdf(context: Context, file: File, title: String) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, file)

            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(sendIntent, title).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(chooser)
            Toast.makeText(context, "PDF généré avec succès (${file.name})", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Erreur d'ouverture du PDF : ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
