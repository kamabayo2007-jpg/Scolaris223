package com.example.util

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.Attendance
import com.example.data.model.AttendanceStatus
import com.example.data.model.BackupMetadata
import com.example.data.model.Grade
import com.example.data.model.Payment
import com.example.data.model.PaymentStatus
import com.example.data.model.PaymentType
import com.example.data.model.School
import com.example.data.model.SchoolBackupData
import com.example.data.model.SchoolClass
import com.example.data.model.SchoolSubscription
import com.example.data.model.SchoolSubscriptionStatus
import com.example.data.model.Student
import com.example.data.model.Subject
import com.example.data.model.Teacher
import com.example.data.model.TimetableSlot
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BackupRestoreUtil {

    /**
     * Génère la chaîne JSON complète et structurée d'un établissement
     */
    fun createBackupJson(data: SchoolBackupData): String {
        val root = JSONObject()

        // Métadonnées
        val meta = JSONObject().apply {
            put("version", data.metadata.version)
            put("schoolId", data.metadata.schoolId)
            put("schoolCode", data.metadata.schoolCode)
            put("schoolName", data.metadata.schoolName)
            put("dateExport", data.metadata.dateExport)
            put("totalStudents", data.metadata.totalStudents)
            put("totalPayments", data.metadata.totalPayments)
            put("systemSignature", data.metadata.systemSignature)
        }
        root.put("metadata", meta)

        // École
        val schoolObj = JSONObject().apply {
            put("id", data.school.id)
            put("name", data.school.name)
            put("code", data.school.code)
            put("city", data.school.city)
            put("country", data.school.country)
            put("phone", data.school.phone)
            put("email", data.school.email)
            put("registrationFee", data.school.registrationFee)
            put("annualSubscriptionFee", data.school.annualSubscriptionFee)
            put("subscriptionStatus", data.school.subscriptionStatus.name)
            put("subscriptionExpiry", data.school.subscriptionExpiry)
            put("bannerColor", data.school.bannerColor)
        }
        root.put("school", schoolObj)

        // Classes
        val classesArr = JSONArray()
        data.classes.forEach { c ->
            val cObj = JSONObject().apply {
                put("id", c.id)
                put("schoolId", c.schoolId)
                put("name", c.name)
                put("level", c.level)
                put("academicYear", c.academicYear)
                put("room", c.room)
                put("mainTeacher", c.mainTeacher)
            }
            classesArr.put(cObj)
        }
        root.put("classes", classesArr)

        // Élèves
        val studentsArr = JSONArray()
        data.students.forEach { s ->
            val sObj = JSONObject().apply {
                put("id", s.id)
                put("schoolId", s.schoolId)
                put("classId", s.classId)
                put("matricule", s.matricule)
                put("firstName", s.firstName)
                put("lastName", s.lastName)
                put("gender", s.gender)
                put("birthDate", s.birthDate)
                put("parentPhone", s.parentPhone)
                put("parentEmail", s.parentEmail)
                put("avatarColorHex", s.avatarColorHex)
            }
            studentsArr.put(sObj)
        }
        root.put("students", studentsArr)

        // Matières
        val subjectsArr = JSONArray()
        data.subjects.forEach { sub ->
            val subObj = JSONObject().apply {
                put("id", sub.id)
                put("schoolId", sub.schoolId)
                put("name", sub.name)
                put("code", sub.code)
                put("coefficient", sub.coefficient)
                put("colorHex", sub.colorHex)
            }
            subjectsArr.put(subObj)
        }
        root.put("subjects", subjectsArr)

        // Professeurs
        val teachersArr = JSONArray()
        data.teachers.forEach { t ->
            val tObj = JSONObject().apply {
                put("id", t.id)
                put("schoolId", t.schoolId)
                put("firstName", t.firstName)
                put("lastName", t.lastName)
                put("email", t.email)
                put("phone", t.phone)
                put("subjectName", t.subjectName)
            }
            teachersArr.put(tObj)
        }
        root.put("teachers", teachersArr)

        // Notes
        val gradesArr = JSONArray()
        data.grades.forEach { g ->
            val gObj = JSONObject().apply {
                put("id", g.id)
                put("schoolId", g.schoolId)
                put("studentId", g.studentId)
                put("subjectId", g.subjectId)
                put("classId", g.classId)
                put("title", g.title)
                put("gradeValue", g.gradeValue)
                put("outOf", g.outOf)
                put("coefficient", g.coefficient)
                put("period", g.period)
                put("date", g.date)
                put("comment", g.comment)
            }
            gradesArr.put(gObj)
        }
        root.put("grades", gradesArr)

        // Assiduités
        val attArr = JSONArray()
        data.attendances.forEach { a ->
            val aObj = JSONObject().apply {
                put("id", a.id)
                put("schoolId", a.schoolId)
                put("studentId", a.studentId)
                put("classId", a.classId)
                put("date", a.date)
                put("timeSlot", a.timeSlot)
                put("status", a.status.name)
                put("reason", a.reason)
                put("justifiedDate", a.justifiedDate)
                put("remarks", a.remarks)
            }
            attArr.put(aObj)
        }
        root.put("attendances", attArr)

        // Paiements (avec montant en Franc CFA et mode de paiement)
        val paymentsArr = JSONArray()
        data.payments.forEach { p ->
            val pObj = JSONObject().apply {
                put("id", p.id)
                put("schoolId", p.schoolId)
                put("studentId", p.studentId)
                put("receiptNumber", p.receiptNumber)
                put("type", p.type.name)
                put("amountTotal", p.amountTotal)
                put("amountPaid", p.amountPaid)
                put("date", p.date)
                put("paymentMethod", p.paymentMethod)
                put("mobilePhoneNumber", p.mobilePhoneNumber)
                put("isLocalPayment", p.isLocalPayment)
                put("status", p.status.name)
                put("remarks", p.remarks)
                put("academicYear", p.academicYear)
            }
            paymentsArr.put(pObj)
        }
        root.put("payments", paymentsArr)

        return root.toString(2)
    }

    /**
     * Sauvegarde la chaîne JSON dans un fichier sur l'appareil
     */
    fun saveBackupToFile(context: Context, jsonString: String, schoolCode: String): File? {
        return try {
            val backupDir = File(context.filesDir, "backups").apply { if (!exists()) mkdirs() }
            val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(backupDir, "sauvegarde_${schoolCode.lowercase()}_$dateStr.json")

            val outputStream = FileOutputStream(file)
            outputStream.write(jsonString.toByteArray(Charsets.UTF_8))
            outputStream.flush()
            outputStream.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Partage ou exporte le fichier de sauvegarde (pour stockage sur ordinateur, clé USB, Google Drive, email)
     */
    fun shareBackupFile(context: Context, file: File) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, file)

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Sauvegarde ERP École - ${file.name}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(intent, "Sauvegarder les données de l'école sur machine/cloud").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            Toast.makeText(context, "Fichier de sauvegarde exporté avec succès (${file.name})", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Erreur export sauvegarde : ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Parse et valide un fichier de sauvegarde JSON pour restauration
     */
    fun parseBackupJson(jsonString: String): SchoolBackupData? {
        return try {
            val root = JSONObject(jsonString)
            val metaObj = root.getJSONObject("metadata")
            val schoolObj = root.getJSONObject("school")

            val metadata = BackupMetadata(
                version = metaObj.optString("version", "1.0.0"),
                schoolId = metaObj.optLong("schoolId", 1L),
                schoolCode = metaObj.optString("schoolCode", "GSE"),
                schoolName = metaObj.optString("schoolName", "École"),
                dateExport = metaObj.optString("dateExport", ""),
                totalStudents = metaObj.optInt("totalStudents", 0),
                totalPayments = metaObj.optInt("totalPayments", 0),
                systemSignature = metaObj.optString("systemSignature", "ERP_GE_SECURE_BACKUP")
            )

            val school = School(
                id = schoolObj.optLong("id", 1L),
                name = schoolObj.getString("name"),
                code = schoolObj.getString("code"),
                city = schoolObj.optString("city", "Abidjan"),
                country = schoolObj.optString("country", "Côte d'Ivoire"),
                phone = schoolObj.optString("phone", ""),
                email = schoolObj.optString("email", ""),
                registrationFee = schoolObj.optDouble("registrationFee", 250000.0),
                annualSubscriptionFee = schoolObj.optDouble("annualSubscriptionFee", 500000.0),
                subscriptionStatus = try {
                    SchoolSubscriptionStatus.valueOf(schoolObj.optString("subscriptionStatus", "ACTIVE"))
                } catch (_: Exception) {
                    SchoolSubscriptionStatus.ACTIVE
                },
                subscriptionExpiry = schoolObj.optString("subscriptionExpiry", "2027-12-31"),
                bannerColor = schoolObj.optString("bannerColor", "#1E3A8A")
            )

            // Classes
            val classes = mutableListOf<SchoolClass>()
            val classesArr = root.optJSONArray("classes") ?: JSONArray()
            for (i in 0 until classesArr.length()) {
                val c = classesArr.getJSONObject(i)
                classes.add(
                    SchoolClass(
                        id = c.optLong("id", 0L),
                        schoolId = c.optLong("schoolId", school.id),
                        name = c.getString("name"),
                        level = c.optString("level", "Général"),
                        academicYear = c.optString("academicYear", "2025-2026"),
                        room = c.optString("room", ""),
                        mainTeacher = c.optString("mainTeacher", "")
                    )
                )
            }

            // Élèves
            val students = mutableListOf<Student>()
            val studentsArr = root.optJSONArray("students") ?: JSONArray()
            for (i in 0 until studentsArr.length()) {
                val s = studentsArr.getJSONObject(i)
                students.add(
                    Student(
                        id = s.optLong("id", 0L),
                        schoolId = s.optLong("schoolId", school.id),
                        classId = s.optLong("classId", 1L),
                        matricule = s.getString("matricule"),
                        firstName = s.getString("firstName"),
                        lastName = s.getString("lastName"),
                        gender = s.optString("gender", "M"),
                        birthDate = s.optString("birthDate", "2008-01-01"),
                        parentPhone = s.optString("parentPhone", ""),
                        parentEmail = s.optString("parentEmail", ""),
                        avatarColorHex = s.optString("avatarColorHex", "#1E3A8A")
                    )
                )
            }

            // Matières
            val subjects = mutableListOf<Subject>()
            val subjectsArr = root.optJSONArray("subjects") ?: JSONArray()
            for (i in 0 until subjectsArr.length()) {
                val sub = subjectsArr.getJSONObject(i)
                subjects.add(
                    Subject(
                        id = sub.optLong("id", 0L),
                        schoolId = sub.optLong("schoolId", school.id),
                        name = sub.getString("name"),
                        code = sub.getString("code"),
                        coefficient = sub.optDouble("coefficient", 1.0),
                        colorHex = sub.optString("colorHex", "#2563EB")
                    )
                )
            }

            // Professeurs
            val teachers = mutableListOf<Teacher>()
            val teachersArr = root.optJSONArray("teachers") ?: JSONArray()
            for (i in 0 until teachersArr.length()) {
                val t = teachersArr.getJSONObject(i)
                teachers.add(
                    Teacher(
                        id = t.optLong("id", 0L),
                        schoolId = t.optLong("schoolId", school.id),
                        firstName = t.getString("firstName"),
                        lastName = t.getString("lastName"),
                        email = t.optString("email", ""),
                        phone = t.optString("phone", ""),
                        subjectName = t.optString("subjectName", "")
                    )
                )
            }

            // Notes
            val grades = mutableListOf<Grade>()
            val gradesArr = root.optJSONArray("grades") ?: JSONArray()
            for (i in 0 until gradesArr.length()) {
                val g = gradesArr.getJSONObject(i)
                grades.add(
                    Grade(
                        id = g.optLong("id", 0L),
                        schoolId = g.optLong("schoolId", school.id),
                        studentId = g.getLong("studentId"),
                        subjectId = g.getLong("subjectId"),
                        classId = g.getLong("classId"),
                        title = g.optString("title", "Évaluation"),
                        gradeValue = g.getDouble("gradeValue"),
                        outOf = g.optDouble("outOf", 20.0),
                        coefficient = g.optDouble("coefficient", 1.0),
                        period = g.optString("period", "Trimestre 1"),
                        date = g.optString("date", "2025-10-01"),
                        comment = g.optString("comment", "")
                    )
                )
            }

            // Assiduités
            val attendances = mutableListOf<Attendance>()
            val attArr = root.optJSONArray("attendances") ?: JSONArray()
            for (i in 0 until attArr.length()) {
                val a = attArr.getJSONObject(i)
                attendances.add(
                    Attendance(
                        id = a.optLong("id", 0L),
                        schoolId = a.optLong("schoolId", school.id),
                        studentId = a.getLong("studentId"),
                        classId = a.getLong("classId"),
                        date = a.getString("date"),
                        timeSlot = a.optString("timeSlot", "08:00 - 10:00"),
                        status = try {
                            AttendanceStatus.valueOf(a.getString("status"))
                        } catch (_: Exception) {
                            AttendanceStatus.PRESENT
                        },
                        reason = a.optString("reason", ""),
                        justifiedDate = a.optString("justifiedDate", ""),
                        remarks = a.optString("remarks", "")
                    )
                )
            }

            // Paiements
            val payments = mutableListOf<Payment>()
            val paymentsArr = root.optJSONArray("payments") ?: JSONArray()
            for (i in 0 until paymentsArr.length()) {
                val p = paymentsArr.getJSONObject(i)
                payments.add(
                    Payment(
                        id = p.optLong("id", 0L),
                        schoolId = p.optLong("schoolId", school.id),
                        studentId = p.getLong("studentId"),
                        receiptNumber = p.getString("receiptNumber"),
                        type = try {
                            PaymentType.valueOf(p.getString("type"))
                        } catch (_: Exception) {
                            PaymentType.SCOLARITE
                        },
                        amountTotal = p.getDouble("amountTotal"),
                        amountPaid = p.getDouble("amountPaid"),
                        date = p.getString("date"),
                        paymentMethod = p.optString("paymentMethod", "Orange Money"),
                        mobilePhoneNumber = p.optString("mobilePhoneNumber", ""),
                        isLocalPayment = p.optBoolean("isLocalPayment", false),
                        status = try {
                            PaymentStatus.valueOf(p.getString("status"))
                        } catch (_: Exception) {
                            PaymentStatus.PAYE
                        },
                        remarks = p.optString("remarks", ""),
                        academicYear = p.optString("academicYear", "2025-2026")
                    )
                )
            }

            SchoolBackupData(
                metadata = metadata,
                school = school,
                classes = classes,
                students = students,
                subjects = subjects,
                teachers = teachers,
                grades = grades,
                attendances = attendances,
                payments = payments,
                timetableSlots = emptyList(),
                subscriptions = emptyList()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
