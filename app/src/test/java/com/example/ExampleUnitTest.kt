package com.example

import com.example.data.model.AttendanceStatus
import com.example.data.model.Grade
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testGradeNormalizationAndWeightedAverage() {
        val grade1 = Grade(
            studentId = 1, subjectId = 1, classId = 1,
            title = "Contrôle", gradeValue = 16.0, outOf = 20.0,
            coefficient = 2.0, period = "T1", date = "2026-09-01"
        )
        val grade2 = Grade(
            studentId = 1, subjectId = 2, classId = 1,
            title = "Devoir", gradeValue = 8.0, outOf = 10.0, // Should normalize to 16.0/20
            coefficient = 1.0, period = "T1", date = "2026-09-02"
        )

        assertEquals(16.0, grade1.normalizedTo20, 0.001)
        assertEquals(16.0, grade2.normalizedTo20, 0.001)

        val weightedSum = (grade1.normalizedTo20 * grade1.coefficient) + (grade2.normalizedTo20 * grade2.coefficient)
        val coeffSum = grade1.coefficient + grade2.coefficient
        val average = weightedSum / coeffSum

        assertEquals(16.0, average, 0.001)
    }

    @Test
    fun testAttendanceStatuses() {
        val statuses = listOf(
            AttendanceStatus.PRESENT,
            AttendanceStatus.LATE,
            AttendanceStatus.ABSENT_JUSTIFIED,
            AttendanceStatus.ABSENT_UNJUSTIFIED
        )
        assertEquals(4, statuses.size)
    }

    @Test
    fun testTeacherMultiGradeEntryCalculation() {
        // Un élève recevant plusieurs notes dans une même matière
        val mathSubjectId = 101L
        val studentId = 202L
        val classId = 1L

        val gradeControl = Grade(
            studentId = studentId, subjectId = mathSubjectId, classId = classId,
            title = "Contrôle N°1", gradeValue = 14.0, outOf = 20.0,
            coefficient = 2.0, period = "Trimestre 1", date = "2026-09-20"
        )
        val gradeOral = Grade(
            studentId = studentId, subjectId = mathSubjectId, classId = classId,
            title = "Interrogation orale", gradeValue = 9.0, outOf = 10.0, // Normalisé 18/20
            coefficient = 1.0, period = "Trimestre 1", date = "2026-09-22"
        )
        val gradeDM = Grade(
            studentId = studentId, subjectId = mathSubjectId, classId = classId,
            title = "Devoir maison", gradeValue = 16.0, outOf = 20.0,
            coefficient = 0.5, period = "Trimestre 1", date = "2026-09-25"
        )

        val batchGrades = listOf(gradeControl, gradeOral, gradeDM)
        assertEquals(3, batchGrades.size)

        // Calcul de la moyenne pondérée de l'élève pour la matière
        val totalWeighted = batchGrades.sumOf { it.normalizedTo20 * it.coefficient }
        val totalCoeff = batchGrades.sumOf { it.coefficient } // 2.0 + 1.0 + 0.5 = 3.5
        val studentSubjectAvg = totalWeighted / totalCoeff
        // (14.0 * 2.0 + 18.0 * 1.0 + 16.0 * 0.5) / 3.5 = (28.0 + 18.0 + 8.0) / 3.5 = 54.0 / 3.5 = 15.4285...
        assertEquals(15.428, studentSubjectAvg, 0.01)
    }

    @Test
    fun testUserRoleAccounts() {
        val adminRole = com.example.data.model.UserRole.ADMIN
        val profRole = com.example.data.model.UserRole.PROFESSEUR
        val eleveRole = com.example.data.model.UserRole.ELEVE
        val parentRole = com.example.data.model.UserRole.PARENT
        val comptaRole = com.example.data.model.UserRole.COMPTABLE

        assertEquals("Administrateur", adminRole.label)
        assertEquals("Professeur", profRole.label)
        assertEquals("Élève", eleveRole.label)
        assertEquals("Parent d'Élève", parentRole.label)
        assertEquals("Comptable", comptaRole.label)
    }

    @Test
    fun testPaymentCalculationsAndRemainingDebt() {
        val paymentFull = com.example.data.model.Payment(
            studentId = 1,
            receiptNumber = "REC-2026-001",
            type = com.example.data.model.PaymentType.INSCRIPTION,
            amountTotal = 150.0,
            amountPaid = 150.0,
            date = "2026-09-01",
            status = com.example.data.model.PaymentStatus.PAYE
        )
        assertEquals(0.0, paymentFull.remainingAmount, 0.001)

        val paymentPartial = com.example.data.model.Payment(
            studentId = 2,
            receiptNumber = "REC-2026-002",
            type = com.example.data.model.PaymentType.SCOLARITE,
            amountTotal = 650.0,
            amountPaid = 300.0,
            date = "2026-09-15",
            status = com.example.data.model.PaymentStatus.PARTIEL
        )
        assertEquals(350.0, paymentPartial.remainingAmount, 0.001)
    }

    @Test
    fun testBulletinRankingAndAverage() {
        val student1Grades = listOf(16.0, 18.0, 14.0)
        val student2Grades = listOf(12.0, 13.0, 11.0)
        val student3Grades = listOf(19.0, 19.5, 18.5)

        val avg1 = student1Grades.average()
        val avg2 = student2Grades.average()
        val avg3 = student3Grades.average()

        val ranking = listOf(Pair(1, avg1), Pair(2, avg2), Pair(3, avg3))
            .sortedByDescending { it.second }

        assertEquals(3, ranking[0].first) // student 3 is 1st
        assertEquals(1, ranking[1].first) // student 1 is 2nd
        assertEquals(2, ranking[2].first) // student 2 is 3rd
    }

    @Test
    fun testGlobalSimultaneousSearch() {
        val students = listOf(
            com.example.data.model.Student(id = 1, firstName = "Lucas", lastName = "Moreau", matricule = "MAT-2026-001", classId = 1, gender = "M"),
            com.example.data.model.Student(id = 2, firstName = "Emma", lastName = "Bernard", matricule = "MAT-2026-002", classId = 2, gender = "F")
        )
        val teachers = listOf(
            com.example.data.model.Teacher(id = 1, firstName = "Michel", lastName = "Bernard", email = "bernard@ecole.fr", subjectName = "Mathématiques", phone = "0601020304"),
            com.example.data.model.Teacher(id = 2, firstName = "Claire", lastName = "Dubois", email = "dubois@ecole.fr", subjectName = "Français", phone = "0602030405")
        )
        val classes = listOf(
            com.example.data.model.SchoolClass(id = 1, name = "6ème A", level = "Collège", mainTeacher = "M. Bernard", room = "Salle 101"),
            com.example.data.model.SchoolClass(id = 2, name = "5ème B", level = "Collège", mainTeacher = "Mme Dubois", room = "Salle 102")
        )

        // Searching "Bernard": should find student "Emma Bernard", teacher "M. Bernard", and class "6ème A" (mainTeacher = "M. Bernard")
        val query = "Bernard"
        val matchedStudents = students.filter { it.fullName.contains(query, ignoreCase = true) }
        val matchedTeachers = teachers.filter { it.fullName.contains(query, ignoreCase = true) }
        val matchedClasses = classes.filter { it.mainTeacher.contains(query, ignoreCase = true) }

        assertEquals(1, matchedStudents.size)
        assertEquals("Emma Bernard", matchedStudents[0].fullName)

        assertEquals(1, matchedTeachers.size)
        assertEquals("Michel Bernard", matchedTeachers[0].fullName)

        assertEquals(1, matchedClasses.size)
        assertEquals("6ème A", matchedClasses[0].name)
    }
}
