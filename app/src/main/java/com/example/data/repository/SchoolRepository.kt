package com.example.data.repository

import com.example.data.dao.SchoolDao
import com.example.data.model.Attendance
import com.example.data.model.Grade
import com.example.data.model.Payment
import com.example.data.model.SchoolClass
import com.example.data.model.SchoolNotification
import com.example.data.model.Student
import com.example.data.model.Subject
import com.example.data.model.Teacher
import com.example.data.model.TimetableSlot
import com.example.data.model.UserAccount
import kotlinx.coroutines.flow.Flow

class SchoolRepository(private val dao: SchoolDao) {

    // Classes
    val allClasses: Flow<List<SchoolClass>> = dao.getAllClasses()
    suspend fun getClassById(id: Long): SchoolClass? = dao.getClassById(id)
    suspend fun insertClass(schoolClass: SchoolClass): Long = dao.insertClass(schoolClass)
    suspend fun deleteClass(schoolClass: SchoolClass) = dao.deleteClass(schoolClass)

    // Students
    val allStudents: Flow<List<Student>> = dao.getAllStudents()
    fun getStudentsByClass(classId: Long): Flow<List<Student>> = dao.getStudentsByClass(classId)
    suspend fun getStudentById(id: Long): Student? = dao.getStudentById(id)
    suspend fun insertStudent(student: Student): Long = dao.insertStudent(student)
    suspend fun updateStudent(student: Student) = dao.updateStudent(student)
    suspend fun deleteStudent(student: Student) = dao.deleteStudent(student)

    // Subjects
    val allSubjects: Flow<List<Subject>> = dao.getAllSubjects()
    suspend fun getSubjectById(id: Long): Subject? = dao.getSubjectById(id)
    suspend fun insertSubject(subject: Subject): Long = dao.insertSubject(subject)

    // Teachers
    val allTeachers: Flow<List<Teacher>> = dao.getAllTeachers()

    // Grades
    val allGrades: Flow<List<Grade>> = dao.getAllGrades()
    fun getGradesByStudent(studentId: Long): Flow<List<Grade>> = dao.getGradesByStudent(studentId)
    fun getGradesByClass(classId: Long): Flow<List<Grade>> = dao.getGradesByClass(classId)
    suspend fun insertGrade(grade: Grade): Long = dao.insertGrade(grade)
    suspend fun insertGrades(grades: List<Grade>) = dao.insertGrades(grades)
    suspend fun deleteGrade(grade: Grade) = dao.deleteGrade(grade)

    // Attendance
    val allAttendance: Flow<List<Attendance>> = dao.getAllAttendance()
    fun getAttendanceByDateAndClass(date: String, classId: Long): Flow<List<Attendance>> =
        dao.getAttendanceByDateAndClass(date, classId)
    fun getAttendanceByStudent(studentId: Long): Flow<List<Attendance>> =
        dao.getAttendanceByStudent(studentId)
    suspend fun insertAttendance(attendance: Attendance): Long = dao.insertAttendance(attendance)
    suspend fun insertAttendanceList(list: List<Attendance>) = dao.insertAttendanceList(list)
    suspend fun updateAttendance(attendance: Attendance) = dao.updateAttendance(attendance)
    suspend fun deleteAttendance(attendance: Attendance) = dao.deleteAttendance(attendance)
    suspend fun deleteAttendanceBySession(classId: Long, date: String, timeSlot: String) =
        dao.deleteAttendanceBySession(classId, date, timeSlot)

    // Timetable
    val allTimetableSlots: Flow<List<TimetableSlot>> = dao.getAllTimetableSlots()
    fun getTimetableSlotsByClass(classId: Long): Flow<List<TimetableSlot>> =
        dao.getTimetableSlotsByClass(classId)
    suspend fun insertTimetableSlot(slot: TimetableSlot): Long = dao.insertTimetableSlot(slot)
    suspend fun deleteTimetableSlot(slot: TimetableSlot) = dao.deleteTimetableSlot(slot)

    // Users
    val allUsers: Flow<List<UserAccount>> = dao.getAllUsers()
    suspend fun getUserByUsername(username: String): UserAccount? = dao.getUserByUsername(username)
    suspend fun insertUser(user: UserAccount): Long = dao.insertUser(user)
    suspend fun updateUser(user: UserAccount) = dao.updateUser(user)
    suspend fun deleteUser(user: UserAccount) = dao.deleteUser(user)

    // Payments
    val allPayments: Flow<List<Payment>> = dao.getAllPayments()
    fun getPaymentsByStudent(studentId: Long): Flow<List<Payment>> = dao.getPaymentsByStudent(studentId)
    suspend fun insertPayment(payment: Payment): Long = dao.insertPayment(payment)
    suspend fun updatePayment(payment: Payment) = dao.updatePayment(payment)
    suspend fun deletePayment(payment: Payment) = dao.deletePayment(payment)

    // Notifications
    val allNotifications: Flow<List<SchoolNotification>> = dao.getAllNotifications()
    fun getNotificationsForRole(role: String): Flow<List<SchoolNotification>> =
        dao.getNotificationsForRole(role)
    suspend fun insertNotification(notification: SchoolNotification): Long = dao.insertNotification(notification)
    suspend fun markNotificationAsRead(id: Long) = dao.markNotificationAsRead(id)

    // Activity Logs / Audit
    val allActivityLogs: Flow<List<com.example.data.model.ActivityLog>> = dao.getAllActivityLogs()
    suspend fun insertActivityLog(log: com.example.data.model.ActivityLog): Long = dao.insertActivityLog(log)
    suspend fun insertActivityLogs(logs: List<com.example.data.model.ActivityLog>) = dao.insertActivityLogs(logs)

    // Multi-Écoles & Tenants
    val allSchools: Flow<List<com.example.data.model.School>> = dao.getAllSchools()
    suspend fun getSchoolById(id: Long): com.example.data.model.School? = dao.getSchoolById(id)
    suspend fun insertSchool(school: com.example.data.model.School): Long = dao.insertSchool(school)
    suspend fun updateSchool(school: com.example.data.model.School) = dao.updateSchool(school)
    suspend fun deleteSchool(school: com.example.data.model.School) = dao.deleteSchool(school)

    // Souscriptions Écoles
    val allSchoolSubscriptions: Flow<List<com.example.data.model.SchoolSubscription>> = dao.getAllSchoolSubscriptions()
    fun getSubscriptionsBySchool(schoolId: Long): Flow<List<com.example.data.model.SchoolSubscription>> = dao.getSubscriptionsBySchool(schoolId)
    suspend fun insertSchoolSubscription(subscription: com.example.data.model.SchoolSubscription): Long = dao.insertSchoolSubscription(subscription)

    // Calendrier Scolaire Annuel
    val allCalendarEvents: Flow<List<com.example.data.model.AcademicCalendarEvent>> = dao.getAllCalendarEvents()
    suspend fun insertCalendarEvent(event: com.example.data.model.AcademicCalendarEvent): Long = dao.insertCalendarEvent(event)
    suspend fun deleteCalendarEvent(event: com.example.data.model.AcademicCalendarEvent) = dao.deleteCalendarEvent(event)

    // Suivi Salaires & Paiements Enseignants
    val allTeacherPayments: Flow<List<com.example.data.model.TeacherPayment>> = dao.getAllTeacherPayments()
    fun getTeacherPaymentsByTeacher(teacherId: Long): Flow<List<com.example.data.model.TeacherPayment>> = dao.getTeacherPaymentsByTeacher(teacherId)
    suspend fun insertTeacherPayment(payment: com.example.data.model.TeacherPayment): Long = dao.insertTeacherPayment(payment)
    suspend fun updateTeacherPayment(payment: com.example.data.model.TeacherPayment) = dao.updateTeacherPayment(payment)
    suspend fun deleteTeacherPayment(payment: com.example.data.model.TeacherPayment) = dao.deleteTeacherPayment(payment)

    // Publication des Notes par la Scolarité
    suspend fun publishGradesForClassAndPeriod(classId: Long, period: String) = dao.publishGradesForClassAndPeriod(classId, period)
    suspend fun publishGrade(gradeId: Long) = dao.publishGrade(gradeId)

    // Validation des Paiements Locaux par la Scolarité
    suspend fun validateLocalPayment(paymentId: Long, validatedBy: String, validationDate: String) =
        dao.validateLocalPayment(paymentId, validatedBy, validationDate)

    // Nettoyage des données de test
    suspend fun clearTestData(clearStudents: Boolean = false) {
        dao.clearAllGrades()
        dao.clearAllAttendance()
        dao.clearAllPayments()
        dao.clearAllTeacherPayments()
        dao.clearAllNotifications()
        if (clearStudents) {
            dao.clearAllStudents()
        }
    }
}
