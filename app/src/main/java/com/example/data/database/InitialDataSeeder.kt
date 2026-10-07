package com.example.data.database

import com.example.data.dao.SchoolDao
import com.example.data.model.Attendance
import com.example.data.model.AttendanceStatus
import com.example.data.model.Grade
import com.example.data.model.MobilePaymentMethods
import com.example.data.model.Payment
import com.example.data.model.PaymentStatus
import com.example.data.model.PaymentType
import com.example.data.model.School
import com.example.data.model.SchoolClass
import com.example.data.model.SchoolNotification
import com.example.data.model.SchoolSubscription
import com.example.data.model.SchoolSubscriptionStatus
import com.example.data.model.Student
import com.example.data.model.Subject
import com.example.data.model.SubscriptionFeeType
import com.example.data.model.Teacher
import com.example.data.model.TimetableSlot
import com.example.data.model.UserAccount
import com.example.data.model.UserRole
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object InitialDataSeeder {

    suspend fun populateDatabase(dao: SchoolDao) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // 0. Établissements Scolaires (Multi-Écoles / Multi-Tenant)
        val initialSchools = listOf(
            School(
                id = 1L,
                name = "Groupe Scolaire Excellence de Bamako",
                code = "GSE-BKO",
                city = "Bamako, Badalabougou",
                country = "Mali",
                phone = "+223 76 48 92 10",
                email = "contact@excellence-bamako.ml",
                registrationFee = 100000.0,
                annualSubscriptionFee = 500000.0,
                subscriptionStatus = SchoolSubscriptionStatus.ACTIVE,
                subscriptionExpiry = "2027-09-30",
                bannerColor = "#1E3A8A",
                academicYear = "2025-2026",
                directorName = "Dr. Robert Kouassi",
                officialMotto = "Un Peuple - Un But - Une Foi",
                currency = "FCFA",
                academyName = "Académie d'Enseignement de Bamako Rive Droite",
                countrySeal = "🇲🇱 République du Mali — Un Peuple, Un But, Une Foi",
                ministryName = "Ministère de l'Éducation Nationale du Mali"
            ),
            School(
                id = 2L,
                name = "Lycée Moderne de Dakar",
                code = "LMD-DKR",
                city = "Dakar, Fann",
                country = "Sénégal",
                phone = "+221 77 654 32 10",
                email = "direction@lycee-dakar.sn",
                registrationFee = 100000.0,
                annualSubscriptionFee = 500000.0,
                subscriptionStatus = SchoolSubscriptionStatus.ACTIVE,
                subscriptionExpiry = "2027-10-15",
                bannerColor = "#0F766E"
            ),
            School(
                id = 3L,
                name = "Collège International l'Avenir",
                code = "CIA-BKO",
                city = "Bamako, ACI 2000",
                country = "Mali",
                phone = "+223 66 12 34 56",
                email = "info@avenir-bamako.ml",
                registrationFee = 100000.0,
                annualSubscriptionFee = 500000.0,
                subscriptionStatus = SchoolSubscriptionStatus.PENDING_RENEWAL,
                subscriptionExpiry = "2026-11-01",
                bannerColor = "#D97706"
            )
        )
        dao.insertSchools(initialSchools)

        // 0.1 Souscriptions Initiales des Écoles (Frais d'Adhésion & Abonnement Annuel en FCFA)
        val initialSubscriptions = listOf(
            SchoolSubscription(
                schoolId = 1L,
                feeType = SubscriptionFeeType.INSCRIPTION,
                amount = 100000.0,
                paymentMethod = MobilePaymentMethods.ORANGE_MONEY,
                phoneNumber = "+225 07 48 92 10 33",
                transactionReference = "OM-SUB-2025-0912",
                paymentDate = "2025-09-01",
                validUntil = "2026-09-01",
                status = "VALIDE",
                notes = "Frais d'adhésion initiale plateforme réglés par Orange Money"
            ),
            SchoolSubscription(
                schoolId = 1L,
                feeType = SubscriptionFeeType.ABONNEMENT_ANNUEL,
                amount = 500000.0,
                paymentMethod = MobilePaymentMethods.WAVE,
                phoneNumber = "+225 07 48 92 10 33",
                transactionReference = "WAVE-SUB-2026-0045",
                paymentDate = "2026-09-02",
                validUntil = "2027-09-30",
                status = "VALIDE",
                notes = "Abonnement annuel 2026-2027 acquitté via Wave"
            ),
            SchoolSubscription(
                schoolId = 2L,
                feeType = SubscriptionFeeType.INSCRIPTION,
                amount = 100000.0,
                paymentMethod = MobilePaymentMethods.MOOV_MONEY,
                phoneNumber = "+221 77 654 32 10",
                transactionReference = "MOOV-SUB-2025-8812",
                paymentDate = "2025-10-01",
                validUntil = "2026-10-01",
                status = "VALIDE",
                notes = "Adhésion initiale réglée via Moov Money"
            ),
            SchoolSubscription(
                schoolId = 2L,
                feeType = SubscriptionFeeType.ABONNEMENT_ANNUEL,
                amount = 500000.0,
                paymentMethod = MobilePaymentMethods.WAVE,
                phoneNumber = "+221 77 654 32 10",
                transactionReference = "WAVE-SUB-2026-1102",
                paymentDate = "2026-10-02",
                validUntil = "2027-10-15",
                status = "VALIDE",
                notes = "Renouvellement annuel 2026-2027"
            )
        )
        dao.insertSchoolSubscriptions(initialSubscriptions)

        // 1. Classes
        val classes = listOf(
            SchoolClass(name = "Terminale S1", level = "Lycée", academicYear = "2025-2026", room = "Salle 302", mainTeacher = "M. Bernard"),
            SchoolClass(name = "3ème A", level = "Collège", academicYear = "2025-2026", room = "Salle 104", mainTeacher = "Mme Rousseau"),
            SchoolClass(name = "1ère Spé Maths", level = "Lycée", academicYear = "2025-2026", room = "Salle 208", mainTeacher = "M. Martin"),
            SchoolClass(name = "6ème B", level = "Collège", academicYear = "2025-2026", room = "Salle 012", mainTeacher = "Mme Lambert")
        )
        dao.insertClasses(classes)

        // 2. Subjects
        val subjects = listOf(
            Subject(name = "Mathématiques", code = "MATH", coefficient = 4.0, colorHex = "#1D4ED8", iconName = "calculate"),
            Subject(name = "Français / Littérature", code = "FRAN", coefficient = 4.0, colorHex = "#7C3AED", iconName = "menu_book"),
            Subject(name = "Histoire - Géographie", code = "HGEO", coefficient = 3.0, colorHex = "#B45309", iconName = "public"),
            Subject(name = "Physique - Chimie", code = "PHYS", coefficient = 3.0, colorHex = "#047857", iconName = "science"),
            Subject(name = "Sciences & Vie de la Terre", code = "SVT", coefficient = 2.0, colorHex = "#059669", iconName = "eco"),
            Subject(name = "Anglais (LV1)", code = "ANGL", coefficient = 2.0, colorHex = "#C026D3", iconName = "language"),
            Subject(name = "Philosophie", code = "PHIL", coefficient = 3.0, colorHex = "#DC2626", iconName = "psychology"),
            Subject(name = "Éducation Physique & Sportive", code = "EPS", coefficient = 2.0, colorHex = "#EA580C", iconName = "sports_soccer")
        )
        dao.insertSubjects(subjects)

        // 3. Teachers
        val teachers = listOf(
            Teacher(firstName = "Alain", lastName = "Bernard", email = "a.bernard@lycee.fr", phone = "+225 07 12 34 56", subjectName = "Mathématiques"),
            Teacher(firstName = "Sophie", lastName = "Rousseau", email = "s.rousseau@college.fr", phone = "+225 07 23 45 67", subjectName = "Français"),
            Teacher(firstName = "Thomas", lastName = "Martin", email = "t.martin@lycee.fr", phone = "+225 07 34 56 78", subjectName = "Physique - Chimie"),
            Teacher(firstName = "Claire", lastName = "Dubois", email = "c.dubois@lycee.fr", phone = "+225 07 45 67 89", subjectName = "Histoire - Géographie"),
            Teacher(firstName = "David", lastName = "Lambert", email = "d.lambert@college.fr", phone = "+225 07 56 78 90", subjectName = "Anglais (LV1)")
        )
        dao.insertTeachers(teachers)

        // 4. Students for Terminale S1 (classId = 1) and 3ème A (classId = 2)
        val students = listOf(
            Student(
                classId = 1,
                firstName = "Lucas",
                lastName = "Moreau",
                matricule = "TS1-001",
                gender = "M",
                birthDate = "15/04/2008",
                parentEmail = "moreau.famille@email.fr",
                parentPhone = "+223 76 98 76 54",
                avatarColorHex = "#1D4ED8",
                isExamCandidate = true,
                academyExamNumber = "BAC-BKO-2026-8841",
                barcodeOrQrData = "SCOLARIS:ML:TS1-001:LUCAS_MOREAU"
            ),
            Student(
                classId = 1,
                firstName = "Emma",
                lastName = "Bernard",
                matricule = "TS1-002",
                gender = "F",
                birthDate = "22/09/2008",
                parentEmail = "bernard.famille@email.fr",
                parentPhone = "+223 76 87 65 43",
                avatarColorHex = "#9333EA",
                isExamCandidate = true,
                academyExamNumber = "", // En attente attribution officielle Académie
                barcodeOrQrData = "SCOLARIS:ML:TS1-002:EMMA_BERNARD"
            ),
            Student(
                classId = 1,
                firstName = "Alexandre",
                lastName = "Petit",
                matricule = "TS1-003",
                gender = "M",
                birthDate = "03/11/2007",
                parentEmail = "petit.famille@email.fr",
                parentPhone = "+223 76 76 54 32",
                avatarColorHex = "#059669",
                isExamCandidate = true,
                academyExamNumber = "BAC-BKO-2026-8842",
                barcodeOrQrData = "SCOLARIS:ML:TS1-003:ALEX_PETIT"
            ),
            Student(
                classId = 1,
                firstName = "Chloé",
                lastName = "Martin",
                matricule = "TS1-004",
                gender = "F",
                birthDate = "18/02/2008",
                parentEmail = "martin.famille@email.fr",
                parentPhone = "+223 76 65 43 21",
                avatarColorHex = "#D97706",
                isExamCandidate = true,
                academyExamNumber = "", // En attente résultats/attribution académie
                barcodeOrQrData = "SCOLARIS:ML:TS1-004:CHLOE_MARTIN"
            ),
            Student(
                classId = 1,
                firstName = "Maxime",
                lastName = "Fournier",
                matricule = "TS1-005",
                gender = "M",
                birthDate = "30/06/2008",
                parentEmail = "fournier.famille@email.fr",
                parentPhone = "+223 76 54 32 10",
                avatarColorHex = "#DC2626",
                isExamCandidate = false,
                barcodeOrQrData = "SCOLARIS:ML:TS1-005:MAXIME_FOURNIER"
            ),
            Student(
                classId = 1,
                firstName = "Sarah",
                lastName = "Lefebvre",
                matricule = "TS1-006",
                gender = "F",
                birthDate = "12/08/2008",
                parentEmail = "lefebvre.famille@email.fr",
                parentPhone = "+223 76 43 21 09",
                avatarColorHex = "#0284C7",
                isExamCandidate = false,
                barcodeOrQrData = "SCOLARIS:ML:TS1-006:SARAH_LEFEBVRE"
            ),
            Student(
                classId = 1,
                firstName = "Hugo",
                lastName = "Roux",
                matricule = "TS1-007",
                gender = "M",
                birthDate = "25/12/2007",
                parentEmail = "roux.famille@email.fr",
                parentPhone = "+223 76 32 10 98",
                avatarColorHex = "#4F46E5",
                isExamCandidate = true,
                academyExamNumber = "BAC-BKO-2026-8845",
                barcodeOrQrData = "SCOLARIS:ML:TS1-007:HUGO_ROUX"
            ),
            Student(
                classId = 1,
                firstName = "Léa",
                lastName = "Garcia",
                matricule = "TS1-008",
                gender = "F",
                birthDate = "09/01/2008",
                parentEmail = "garcia.famille@email.fr",
                parentPhone = "+223 76 21 09 87",
                avatarColorHex = "#DB2777",
                isExamCandidate = false,
                barcodeOrQrData = "SCOLARIS:ML:TS1-008:LEA_GARCIA"
            ),

            Student(
                classId = 2,
                firstName = "Antoine",
                lastName = "Dubois",
                matricule = "3A-001",
                gender = "M",
                birthDate = "14/05/2011",
                parentEmail = "dubois.famille@email.fr",
                parentPhone = "+223 66 11 22 33",
                avatarColorHex = "#2563EB",
                isExamCandidate = true,
                academyExamNumber = "DEF-BKO-2026-1044",
                barcodeOrQrData = "SCOLARIS:ML:3A-001:ANTOINE_DUBOIS"
            ),
            Student(
                classId = 2,
                firstName = "Camille",
                lastName = "Fontaine",
                matricule = "3A-002",
                gender = "F",
                birthDate = "28/10/2011",
                parentEmail = "fontaine.famille@email.fr",
                parentPhone = "+223 66 22 33 44",
                avatarColorHex = "#7C3AED",
                isExamCandidate = true,
                academyExamNumber = "", // En attente attribution
                barcodeOrQrData = "SCOLARIS:ML:3A-002:CAMILLE_FONTAINE"
            ),
            Student(
                classId = 2,
                firstName = "Jules",
                lastName = "Riviere",
                matricule = "3A-003",
                gender = "M",
                birthDate = "05/03/2011",
                parentEmail = "riviere.famille@email.fr",
                parentPhone = "+223 66 33 44 55",
                avatarColorHex = "#059669",
                isExamCandidate = false,
                barcodeOrQrData = "SCOLARIS:ML:3A-003:JULES_RIVIERE"
            ),
            Student(
                classId = 2,
                firstName = "Inès",
                lastName = "Leroy",
                matricule = "3A-004",
                gender = "F",
                birthDate = "19/07/2011",
                parentEmail = "leroy.famille@email.fr",
                parentPhone = "+223 66 44 55 66",
                avatarColorHex = "#EA580C",
                isExamCandidate = true,
                academyExamNumber = "DEF-BKO-2026-1045",
                barcodeOrQrData = "SCOLARIS:ML:3A-004:INES_LEROY"
            )
        )
        dao.insertStudents(students)

        // 5. Grades
        val grades = listOf(
            Grade(studentId = 1, subjectId = 1, classId = 1, title = "DS 1 - Analyse & Suites", gradeValue = 16.5, outOf = 20.0, coefficient = 2.0, period = "Trimestre 1", date = "2026-09-15", comment = "Très bon raisonnement."),
            Grade(studentId = 1, subjectId = 1, classId = 1, title = "Interrogation Fonctions", gradeValue = 18.0, outOf = 20.0, coefficient = 1.0, period = "Trimestre 1", date = "2026-09-22", comment = "Parfait."),
            Grade(studentId = 1, subjectId = 2, classId = 1, title = "Dissertation Littéraire", gradeValue = 14.0, outOf = 20.0, coefficient = 2.0, period = "Trimestre 1", date = "2026-09-18", comment = "Bonne analyse stylistique."),
            Grade(studentId = 1, subjectId = 3, classId = 1, title = "Contrôle Guerre Froide", gradeValue = 15.0, outOf = 20.0, coefficient = 1.5, period = "Trimestre 1", date = "2026-09-12", comment = "Solide maîtrise des repères."),
            Grade(studentId = 1, subjectId = 4, classId = 1, title = "TP Mécanique de Newton", gradeValue = 17.0, outOf = 20.0, coefficient = 1.0, period = "Trimestre 1", date = "2026-09-20", comment = "Excellente rigueur expérimentale."),

            Grade(studentId = 2, subjectId = 1, classId = 1, title = "DS 1 - Analyse & Suites", gradeValue = 18.5, outOf = 20.0, coefficient = 2.0, period = "Trimestre 1", date = "2026-09-15", comment = "Excellent travail."),
            Grade(studentId = 2, subjectId = 2, classId = 1, title = "Dissertation Littéraire", gradeValue = 17.0, outOf = 20.0, coefficient = 2.0, period = "Trimestre 1", date = "2026-09-18", comment = "Très bien rédigé."),
            Grade(studentId = 2, subjectId = 4, classId = 1, title = "TP Mécanique de Newton", gradeValue = 19.0, outOf = 20.0, coefficient = 1.0, period = "Trimestre 1", date = "2026-09-20", comment = "Remarquable."),

            Grade(studentId = 3, subjectId = 1, classId = 1, title = "DS 1 - Analyse & Suites", gradeValue = 11.0, outOf = 20.0, coefficient = 2.0, period = "Trimestre 1", date = "2026-09-15", comment = "Des erreurs de calcul."),
            Grade(studentId = 3, subjectId = 2, classId = 1, title = "Dissertation Littéraire", gradeValue = 12.5, outOf = 20.0, coefficient = 2.0, period = "Trimestre 1", date = "2026-09-18", comment = "Ensemble convenable."),
            Grade(studentId = 3, subjectId = 3, classId = 1, title = "Contrôle Guerre Froide", gradeValue = 13.5, outOf = 20.0, coefficient = 1.5, period = "Trimestre 1", date = "2026-09-12", comment = "Assez bien."),

            Grade(studentId = 4, subjectId = 1, classId = 1, title = "DS 1 - Analyse & Suites", gradeValue = 14.5, outOf = 20.0, coefficient = 2.0, period = "Trimestre 1", date = "2026-09-15", comment = "Bon investissement."),
            Grade(studentId = 4, subjectId = 6, classId = 1, title = "Expression Orale B2", gradeValue = 16.0, outOf = 20.0, coefficient = 1.0, period = "Trimestre 1", date = "2026-09-21", comment = "Fluide et spontané."),

            Grade(studentId = 9, subjectId = 1, classId = 2, title = "Contrôle Théorème de Thalès", gradeValue = 15.5, outOf = 20.0, coefficient = 2.0, period = "Trimestre 1", date = "2026-09-16", comment = "Très bien appliqué."),
            Grade(studentId = 9, subjectId = 2, classId = 2, title = "Dictée & Questions", gradeValue = 13.0, outOf = 20.0, coefficient = 1.5, period = "Trimestre 1", date = "2026-09-19", comment = "Attention aux accords."),
            Grade(studentId = 10, subjectId = 1, classId = 2, title = "Contrôle Théorème de Thalès", gradeValue = 17.5, outOf = 20.0, coefficient = 2.0, period = "Trimestre 1", date = "2026-09-16", comment = "Très satisfaisant.")
        )
        dao.insertGrades(grades)

        // 6. Attendance
        val attendances = listOf(
            Attendance(studentId = 1, classId = 1, date = todayStr, timeSlot = "08:00 - 10:00", status = AttendanceStatus.PRESENT),
            Attendance(studentId = 2, classId = 1, date = todayStr, timeSlot = "08:00 - 10:00", status = AttendanceStatus.PRESENT),
            Attendance(studentId = 3, classId = 1, date = todayStr, timeSlot = "08:00 - 10:00", status = AttendanceStatus.PRESENT),
            Attendance(studentId = 4, classId = 1, date = todayStr, timeSlot = "08:00 - 10:00", status = AttendanceStatus.LATE, reason = "Embouteillage sur le boulevard", remarks = "Arrivé à 08h20"),
            Attendance(studentId = 5, classId = 1, date = todayStr, timeSlot = "08:00 - 10:00", status = AttendanceStatus.ABSENT_UNJUSTIFIED, reason = "", remarks = "Non justifié"),
            Attendance(studentId = 6, classId = 1, date = todayStr, timeSlot = "08:00 - 10:00", status = AttendanceStatus.ABSENT_JUSTIFIED, reason = "Rendez-vous médical", justifiedDate = todayStr, remarks = "Certificat médical transmis"),
            Attendance(studentId = 7, classId = 1, date = todayStr, timeSlot = "08:00 - 10:00", status = AttendanceStatus.PRESENT),
            Attendance(studentId = 8, classId = 1, date = todayStr, timeSlot = "08:00 - 10:00", status = AttendanceStatus.PRESENT),

            Attendance(studentId = 1, classId = 1, date = "2026-09-22", timeSlot = "10:00 - 12:00", status = AttendanceStatus.PRESENT),
            Attendance(studentId = 1, classId = 1, date = "2026-09-20", timeSlot = "08:00 - 10:00", status = AttendanceStatus.LATE, reason = "Panne de transport"),
            Attendance(studentId = 1, classId = 1, date = "2026-09-18", timeSlot = "14:00 - 16:00", status = AttendanceStatus.PRESENT),
            Attendance(studentId = 2, classId = 1, date = "2026-09-22", timeSlot = "10:00 - 12:00", status = AttendanceStatus.PRESENT)
        )
        dao.insertAttendanceList(attendances)

        // 7. Timetable Slots (Créés et gérés exclusivement par la Scolarité)
        val slots = listOf(
            TimetableSlot(classId = 1, subjectId = 1, teacherName = "Alain Bernard", teacherId = 1L, dayOfWeek = 1, startTime = "08:00", endTime = "10:00", room = "Salle 302"),
            TimetableSlot(classId = 1, subjectId = 2, teacherName = "Sophie Rousseau", teacherId = 2L, dayOfWeek = 1, startTime = "10:15", endTime = "12:15", room = "Salle 302"),
            TimetableSlot(classId = 1, subjectId = 3, teacherName = "Claire Dubois", teacherId = 4L, dayOfWeek = 1, startTime = "13:30", endTime = "15:30", room = "Salle 104"),
            TimetableSlot(classId = 1, subjectId = 4, teacherName = "Thomas Martin", teacherId = 3L, dayOfWeek = 2, startTime = "08:00", endTime = "10:00", room = "Labo Phys 1"),
            TimetableSlot(classId = 1, subjectId = 1, teacherName = "Alain Bernard", teacherId = 1L, dayOfWeek = 2, startTime = "10:15", endTime = "12:15", room = "Salle 302"),
            TimetableSlot(classId = 1, subjectId = 6, teacherName = "David Lambert", teacherId = 5L, dayOfWeek = 3, startTime = "08:00", endTime = "10:00", room = "Salle 208"),
            TimetableSlot(classId = 1, subjectId = 5, teacherName = "Thomas Martin", teacherId = 3L, dayOfWeek = 4, startTime = "08:00", endTime = "10:00", room = "Labo SVT"),
            TimetableSlot(classId = 1, subjectId = 7, teacherName = "Alain Bernard", teacherId = 1L, dayOfWeek = 4, startTime = "10:15", endTime = "12:15", room = "Salle 302"),
            TimetableSlot(classId = 1, subjectId = 8, teacherName = "David Lambert", teacherId = 5L, dayOfWeek = 5, startTime = "08:00", endTime = "10:00", room = "Gymnase"),
            TimetableSlot(classId = 1, subjectId = 1, teacherName = "Alain Bernard", teacherId = 1L, dayOfWeek = 5, startTime = "13:30", endTime = "15:00", room = "Salle 302")
        )
        dao.insertTimetableSlots(slots)

        // 8. Users for all roles (including Super Admin)
        val users = listOf(
            UserAccount(
                username = "superadmin",
                passwordHash = "Super@123456",
                fullName = "Ing. Amadou Diallo (Super Admin)",
                email = "superadmin@erp-ecole.cloud",
                role = UserRole.SUPER_ADMIN,
                avatarColorHex = "#0F172A"
            ),
            UserAccount(
                username = "admin",
                passwordHash = "Admin@123456",
                fullName = "Dr. Robert Kouassi",
                email = "admin@erp-ecole.edu",
                role = UserRole.ADMIN,
                avatarColorHex = "#DC2626"
            ),
            UserAccount(
                username = "direction",
                passwordHash = "Direction@123456",
                fullName = "Mme Hélène Mercier",
                email = "direction@erp-ecole.edu",
                role = UserRole.DIRECTION,
                avatarColorHex = "#7C3AED"
            ),
            UserAccount(
                username = "prof.bernard",
                passwordHash = "Prof@123456",
                fullName = "Alain Bernard",
                email = "a.bernard@lycee.fr",
                role = UserRole.PROFESSEUR,
                linkedTeacherId = 1,
                avatarColorHex = "#2563EB"
            ),
            UserAccount(
                username = "lucas.moreau",
                passwordHash = "Eleve@123456",
                fullName = "Lucas Moreau",
                email = "moreau.famille@email.fr",
                role = UserRole.ELEVE,
                linkedStudentId = 1,
                avatarColorHex = "#059669"
            ),
            UserAccount(
                username = "parent.moreau",
                passwordHash = "Parent@123456",
                fullName = "Jean-Pierre Moreau (Parent)",
                email = "moreau.famille@email.fr",
                role = UserRole.PARENT,
                linkedStudentId = 1,
                linkedStudentIds = "1,9", // Parent avec deux enfants : Lucas Moreau (Terminale S1) et Antoine Dubois (3ème A)
                avatarColorHex = "#D97706"
            ),
            UserAccount(
                username = "comptable",
                passwordHash = "Compta@123456",
                fullName = "Fatou Diallo",
                email = "comptabilite@erp-ecole.edu",
                role = UserRole.COMPTABLE,
                avatarColorHex = "#0891B2"
            )
        )
        dao.insertUsers(users)

        // 9. Payments & School Fees in Franc CFA (FCFA) with Orange Money, Moov Money, Wave, etc.
        val payments = listOf(
            Payment(
                studentId = 1,
                receiptNumber = "REC-2026-001",
                type = PaymentType.INSCRIPTION,
                amountTotal = 50000.0,
                amountPaid = 50000.0,
                date = "2026-09-01",
                paymentMethod = MobilePaymentMethods.ORANGE_MONEY,
                mobilePhoneNumber = "+225 07 48 92 10 33",
                status = PaymentStatus.PAYE,
                remarks = "Frais d'inscription réglés via Orange Money (Réf: OM-883492)"
            ),
            Payment(
                studentId = 1,
                receiptNumber = "REC-2026-015",
                type = PaymentType.SCOLARITE,
                amountTotal = 250000.0,
                amountPaid = 250000.0,
                date = "2026-09-05",
                paymentMethod = MobilePaymentMethods.WAVE,
                mobilePhoneNumber = "+225 07 48 92 10 33",
                status = PaymentStatus.PAYE,
                remarks = "Scolarité Trimestre 1 acquittée via Wave (Réf: WV-992104)"
            ),
            Payment(
                studentId = 1,
                receiptNumber = "REC-2026-028",
                type = PaymentType.CANTINE,
                amountTotal = 45000.0,
                amountPaid = 45000.0,
                date = "2026-09-10",
                paymentMethod = MobilePaymentMethods.MOOV_MONEY,
                mobilePhoneNumber = "+225 05 12 34 56 78",
                status = PaymentStatus.PAYE,
                remarks = "Restauration scolaire Trimestre 1 réglée par Moov Money"
            ),
            Payment(
                studentId = 2,
                receiptNumber = "REC-2026-002",
                type = PaymentType.INSCRIPTION,
                amountTotal = 50000.0,
                amountPaid = 50000.0,
                date = "2026-09-01",
                paymentMethod = MobilePaymentMethods.WAVE,
                mobilePhoneNumber = "+225 07 87 65 43 21",
                status = PaymentStatus.PAYE,
                remarks = "Inscription réglée par Wave"
            ),
            Payment(
                studentId = 2,
                receiptNumber = "REC-2026-018",
                type = PaymentType.SCOLARITE,
                amountTotal = 250000.0,
                amountPaid = 250000.0,
                date = "2026-09-06",
                paymentMethod = MobilePaymentMethods.ORANGE_MONEY,
                mobilePhoneNumber = "+225 07 87 65 43 21",
                status = PaymentStatus.PAYE,
                remarks = "Scolarité T1 via Orange Money"
            ),
            Payment(
                studentId = 3,
                receiptNumber = "REC-2026-005",
                type = PaymentType.INSCRIPTION,
                amountTotal = 50000.0,
                amountPaid = 50000.0,
                date = "2026-09-02",
                paymentMethod = MobilePaymentMethods.ESPECES,
                status = PaymentStatus.PAYE,
                remarks = "Espèces au guichet comptabilité"
            ),
            Payment(
                studentId = 3,
                receiptNumber = "REC-2026-022",
                type = PaymentType.SCOLARITE,
                amountTotal = 250000.0,
                amountPaid = 125000.0,
                date = "2026-09-08",
                paymentMethod = MobilePaymentMethods.ORANGE_MONEY,
                mobilePhoneNumber = "+225 07 76 54 32 10",
                status = PaymentStatus.PARTIEL,
                remarks = "1ère tranche scolarité réglée par Orange Money - Solde restant 125 000 FCFA"
            ),
            Payment(
                studentId = 4,
                receiptNumber = "REC-2026-008",
                type = PaymentType.INSCRIPTION,
                amountTotal = 50000.0,
                amountPaid = 50000.0,
                date = "2026-09-02",
                paymentMethod = MobilePaymentMethods.MOOV_MONEY,
                mobilePhoneNumber = "+225 05 65 43 21 09",
                status = PaymentStatus.PAYE,
                remarks = "Inscription Moov Money validée"
            ),
            Payment(
                studentId = 4,
                receiptNumber = "REC-2026-035",
                type = PaymentType.SCOLARITE,
                amountTotal = 250000.0,
                amountPaid = 250000.0,
                date = "2026-09-12",
                paymentMethod = MobilePaymentMethods.WAVE,
                mobilePhoneNumber = "+225 05 65 43 21 09",
                status = PaymentStatus.PAYE
            ),
            Payment(
                studentId = 5,
                receiptNumber = "REC-2026-011",
                type = PaymentType.INSCRIPTION,
                amountTotal = 50000.0,
                amountPaid = 50000.0,
                date = "2026-09-03",
                paymentMethod = MobilePaymentMethods.ESPECES,
                isLocalPayment = true,
                isValidatedByScolarite = true,
                validatedBy = "Dr. Robert Kouassi (Scolarité)",
                validationDate = "2026-09-03",
                status = PaymentStatus.PAYE
            ),
            Payment(
                studentId = 5,
                receiptNumber = "REC-2026-040",
                type = PaymentType.SCOLARITE,
                amountTotal = 250000.0,
                amountPaid = 0.0,
                date = "2026-09-15",
                paymentMethod = MobilePaymentMethods.ORANGE_MONEY,
                mobilePhoneNumber = "+225 07 54 32 10 98",
                status = PaymentStatus.EN_ATTENTE,
                remarks = "Relance envoyée aux parents par SMS/WhatsApp le 20/09"
            ),
            Payment(
                studentId = 6,
                receiptNumber = "REC-2026-048",
                type = PaymentType.SCOLARITE,
                amountTotal = 150000.0,
                amountPaid = 150000.0,
                date = todayStr,
                paymentMethod = MobilePaymentMethods.ESPECES,
                isLocalPayment = true,
                isValidatedByScolarite = false, // En attente de validation par la Scolarité
                status = PaymentStatus.PARTIEL,
                remarks = "Versement espèces déposé au guichet ce matin — En attente de validation comptable"
            )
        )
        dao.insertPayments(payments)

        // 10. School Notifications & Annonces Catégorisées
        val notifications = listOf(
            SchoolNotification(
                title = "Publication Officielle des Notes DS1",
                message = "La Scolarité a officiellement validé et publié les relevés de notes du Devoir Surveillé N°1 de Mathématiques.",
                targetRole = "ALL",
                category = "NOTES_RESULTATS",
                date = "2026-09-22",
                type = "GRADE"
            ),
            SchoolNotification(
                title = "Paiement Mobile Money Activé",
                message = "Les règlements par Orange Money, Moov Money et Wave sont désormais instantanément confirmés et sécurisés.",
                targetRole = "ALL",
                category = "PAIEMENT_SCOLARITE",
                date = "2026-09-21",
                type = "PAYMENT"
            ),
            SchoolNotification(
                title = "Virement des Honoraires & Salaires du mois",
                message = "Les ordres de virement des salaires du corps professoral pour le mois en cours ont été émis.",
                targetRole = "PROFESSEUR",
                category = "PAIE_PROFESSEUR",
                date = "2026-09-28",
                type = "FINANCE"
            ),
            SchoolNotification(
                title = "Calendrier Scolaire Annuel : Vacances de Toussaint",
                message = "Les congés de Toussaint débuteront le vendredi 24 octobre après les cours et la reprise aura lieu le lundi 3 novembre.",
                targetRole = "ALL",
                category = "CALENDRIER",
                date = "2026-09-25",
                type = "EVENT"
            ),
            SchoolNotification(
                title = "Conseil de Classe du 1er Trimestre",
                message = "Le planning des conseils de classe du premier trimestre est disponible au secrétariat de scolarité.",
                targetRole = "ALL",
                category = "ADMINISTRATIF",
                date = "2026-09-20",
                type = "ANNOUNCEMENT"
            ),
            SchoolNotification(
                title = "Souscription Plateforme Active",
                message = "L'abonnement annuel de l'établissement est actif et garanti jusqu'au 30/09/2027.",
                targetRole = "ADMIN",
                category = "ADMINISTRATIF",
                date = todayStr,
                type = "INFO"
            )
        )
        dao.insertNotifications(notifications)

        // 10.1 Calendrier Scolaire Annuel (Dates Clés, Trimestres, Vacances, Examens)
        val calendarEvents = listOf(
            com.example.data.model.AcademicCalendarEvent(
                schoolId = 1L,
                title = "Rentrée Scolaire des Classes",
                category = "TRIMESTRE",
                startDate = "2025-09-08",
                endDate = "2025-09-08",
                description = "Accueil solennel des élèves, remise des emplois du temps hebdomadaires",
                academicYear = "2025-2026"
            ),
            com.example.data.model.AcademicCalendarEvent(
                schoolId = 1L,
                title = "Congés de Toussaint",
                category = "VACANCES",
                startDate = "2025-10-24",
                endDate = "2025-11-03",
                description = "Interruption des cours pour tous les cycles collège et lycée",
                academicYear = "2025-2026"
            ),
            com.example.data.model.AcademicCalendarEvent(
                schoolId = 1L,
                title = "Examens et Devoirs Surveillés du 1er Trimestre",
                category = "EXAMEN",
                startDate = "2025-11-24",
                endDate = "2025-11-28",
                description = "Semaine bloquée d'évaluations trimestrielles pour toutes les filières",
                academicYear = "2025-2026"
            ),
            com.example.data.model.AcademicCalendarEvent(
                schoolId = 1L,
                title = "Fin du 1er Trimestre & Remise des Bulletins",
                category = "TRIMESTRE",
                startDate = "2025-12-12",
                endDate = "2025-12-12",
                description = "Arrêt officiel des notes et publication par la Scolarité",
                academicYear = "2025-2026"
            ),
            com.example.data.model.AcademicCalendarEvent(
                schoolId = 1L,
                title = "Vacances de Noël et du Nouvel An",
                category = "VACANCES",
                startDate = "2025-12-19",
                endDate = "2026-01-05",
                description = "Fermeture administrative et pédagogique des établissements",
                academicYear = "2025-2026"
            ),
            com.example.data.model.AcademicCalendarEvent(
                schoolId = 1L,
                title = "Reprise des cours & Début du 2ème Trimestre",
                category = "TRIMESTRE",
                startDate = "2026-01-05",
                endDate = "2026-01-05",
                description = "Démarrage des programmes pédagogiques du T2",
                academicYear = "2025-2026"
            ),
            com.example.data.model.AcademicCalendarEvent(
                schoolId = 1L,
                title = "Examens Blancs Régionaux (Bac & BEPC)",
                category = "EXAMEN",
                startDate = "2026-02-16",
                endDate = "2026-02-20",
                description = "Épreuves en conditions réelles d'examen pour les classes d'examen",
                academicYear = "2025-2026"
            ),
            com.example.data.model.AcademicCalendarEvent(
                schoolId = 1L,
                title = "Vacances de Février / Mi-parcours",
                category = "VACANCES",
                startDate = "2026-02-20",
                endDate = "2026-03-02",
                description = "Repos de mi-trimestre pour le corps professoral et les élèves",
                academicYear = "2025-2026"
            ),
            com.example.data.model.AcademicCalendarEvent(
                schoolId = 1L,
                title = "Fin du 2ème Trimestre",
                category = "TRIMESTRE",
                startDate = "2026-03-27",
                endDate = "2026-03-27",
                description = "Validation des notes et conseils de classe du T2",
                academicYear = "2025-2026"
            ),
            com.example.data.model.AcademicCalendarEvent(
                schoolId = 1L,
                title = "Vacances de Pâques",
                category = "VACANCES",
                startDate = "2026-04-03",
                endDate = "2026-04-14",
                description = "Congés de Pâques pour toute la communauté scolaire",
                academicYear = "2025-2026"
            ),
            com.example.data.model.AcademicCalendarEvent(
                schoolId = 1L,
                title = "Épreuves Officielles & Clôture de l'Année Scolaire",
                category = "EXAMEN",
                startDate = "2026-06-15",
                endDate = "2026-06-26",
                description = "Sessions du Baccalauréat et proclamations des résultats",
                academicYear = "2025-2026"
            ),
            com.example.data.model.AcademicCalendarEvent(
                schoolId = 1L,
                title = "Grandes Vacances Scolaires",
                category = "VACANCES",
                startDate = "2026-07-03",
                endDate = "2026-09-07",
                description = "Fin de l'année scolaire 2025-2026",
                academicYear = "2025-2026"
            )
        )
        dao.insertCalendarEvents(calendarEvents)

        // 10.2 Suivi des Paiements & Salaires des Enseignants
        val teacherPayments = listOf(
            com.example.data.model.TeacherPayment(
                schoolId = 1L,
                teacherId = 1L,
                teacherName = "Alain Bernard",
                periodMonth = "Septembre 2025",
                academicYear = "2025-2026",
                baseSalary = 350000.0,
                bonusAmount = 25000.0,
                deductions = 0.0,
                netAmount = 375000.0,
                paymentDate = "2025-09-28",
                paymentMethod = MobilePaymentMethods.VIREMENT,
                transactionRef = "VIR-ENS-2025-0901",
                status = "PAYE",
                notes = "Salaire de base + prime de responsabilité titulaire Terminale S1"
            ),
            com.example.data.model.TeacherPayment(
                schoolId = 1L,
                teacherId = 1L,
                teacherName = "Alain Bernard",
                periodMonth = "Octobre 2025",
                academicYear = "2025-2026",
                baseSalary = 350000.0,
                bonusAmount = 0.0,
                deductions = 0.0,
                netAmount = 350000.0,
                paymentDate = "2025-10-28",
                paymentMethod = MobilePaymentMethods.ORANGE_MONEY,
                transactionRef = "OM-ENS-2025-1014",
                status = "PAYE",
                notes = "Virement Mobile Money Orange validé"
            ),
            com.example.data.model.TeacherPayment(
                schoolId = 1L,
                teacherId = 2L,
                teacherName = "Sophie Rousseau",
                periodMonth = "Octobre 2025",
                academicYear = "2025-2026",
                baseSalary = 320000.0,
                bonusAmount = 15000.0,
                deductions = 0.0,
                netAmount = 335000.0,
                paymentDate = "2025-10-28",
                paymentMethod = MobilePaymentMethods.WAVE,
                transactionRef = "WV-ENS-2025-1022",
                status = "PAYE",
                notes = "Règlement salaire via Wave Business"
            ),
            com.example.data.model.TeacherPayment(
                schoolId = 1L,
                teacherId = 3L,
                teacherName = "Thomas Martin",
                periodMonth = "Octobre 2025",
                academicYear = "2025-2026",
                baseSalary = 340000.0,
                bonusAmount = 20000.0,
                deductions = 0.0,
                netAmount = 360000.0,
                paymentDate = "2025-10-28",
                paymentMethod = MobilePaymentMethods.VIREMENT,
                transactionRef = "VIR-ENS-2025-1033",
                status = "PAYE",
                notes = "Salaire mensuel Physique-Chimie"
            ),
            com.example.data.model.TeacherPayment(
                schoolId = 1L,
                teacherId = 4L,
                teacherName = "Claire Dubois",
                periodMonth = "Octobre 2025",
                academicYear = "2025-2026",
                baseSalary = 310000.0,
                bonusAmount = 0.0,
                deductions = 0.0,
                netAmount = 310000.0,
                paymentDate = "2025-10-29",
                paymentMethod = MobilePaymentMethods.MOOV_MONEY,
                transactionRef = "MOOV-ENS-2025-1044",
                status = "PAYE",
                notes = "Paiement honoraires Histoire-Géo via Moov Money"
            ),
            com.example.data.model.TeacherPayment(
                schoolId = 1L,
                teacherId = 5L,
                teacherName = "David Lambert",
                periodMonth = "Octobre 2025",
                academicYear = "2025-2026",
                baseSalary = 300000.0,
                bonusAmount = 0.0,
                deductions = 0.0,
                netAmount = 300000.0,
                paymentDate = todayStr,
                paymentMethod = MobilePaymentMethods.ESPECES,
                transactionRef = "ESP-CAISSE-089",
                status = "EN_ATTENTE",
                notes = "Chèque / Espèces prêt en caisse comptabilité — En attente d'émargement"
            )
        )
        dao.insertTeacherPayments(teacherPayments)

        // 11. Journal d'Activité / Audit Logs (Chronologique)
        val initialLogs = listOf(
            com.example.data.model.ActivityLog(
                timestamp = "Aujourd'hui à 11:35",
                actionType = "GRADE",
                description = "Saisie des notes du Contrôle Écrit N°1 en Mathématiques",
                userFullName = "Alain Bernard",
                userRole = "PROFESSEUR",
                targetEntity = "Classe 6ème A",
                details = "24 notes enregistrées (Moyenne : 14.8/20)"
            ),
            com.example.data.model.ActivityLog(
                timestamp = "Aujourd'hui à 10:10",
                actionType = "PAYMENT",
                description = "Encaissement scolarité via Wave Mobile Money",
                userFullName = "Fatou Diallo",
                userRole = "COMPTABLE",
                targetEntity = "Élève Lucas Moreau",
                details = "Montant : 250 000 FCFA (Réf: WV-992104)"
            ),
            com.example.data.model.ActivityLog(
                timestamp = "Hier à 16:40",
                actionType = "SECURITY",
                description = "Connexion Super Administrateur & Audit Multi-Écoles",
                userFullName = "Ing. Amadou Diallo",
                userRole = "SUPER_ADMIN",
                targetEntity = "Plateforme Centrale",
                details = "Contrôle global des 3 établissements scolaires"
            ),
            com.example.data.model.ActivityLog(
                timestamp = "Hier à 14:20",
                actionType = "STUDENT",
                description = "Mise à jour du dossier administratif",
                userFullName = "Dr. Robert Kouassi",
                userRole = "ADMINISTRATEUR",
                targetEntity = "Élève Emma Bernard",
                details = "Coordonnées des parents et numéro Orange Money validés"
            )
        )
        dao.insertActivityLogs(initialLogs)
    }
}
