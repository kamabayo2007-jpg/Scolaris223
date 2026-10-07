package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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

@Dao
interface SchoolDao {

    // === Classes ===
    @Query("SELECT * FROM classes ORDER BY name ASC")
    fun getAllClasses(): Flow<List<SchoolClass>>

    @Query("SELECT * FROM classes WHERE id = :id LIMIT 1")
    suspend fun getClassById(id: Long): SchoolClass?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(schoolClass: SchoolClass): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClasses(classes: List<SchoolClass>)

    @Delete
    suspend fun deleteClass(schoolClass: SchoolClass)

    // === Students ===
    @Query("SELECT * FROM students ORDER BY lastName ASC, firstName ASC")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE classId = :classId ORDER BY lastName ASC, firstName ASC")
    fun getStudentsByClass(classId: Long): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
    suspend fun getStudentById(id: Long): Student?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<Student>)

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)

    // === Subjects ===
    @Query("SELECT * FROM subjects ORDER BY name ASC")
    fun getAllSubjects(): Flow<List<Subject>>

    @Query("SELECT * FROM subjects WHERE id = :id LIMIT 1")
    suspend fun getSubjectById(id: Long): Subject?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: Subject): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<Subject>)

    // === Teachers ===
    @Query("SELECT * FROM teachers ORDER BY lastName ASC")
    fun getAllTeachers(): Flow<List<Teacher>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeachers(teachers: List<Teacher>)

    // === Grades ===
    @Query("SELECT * FROM grades ORDER BY date DESC, id DESC")
    fun getAllGrades(): Flow<List<Grade>>

    @Query("SELECT * FROM grades WHERE studentId = :studentId ORDER BY date DESC")
    fun getGradesByStudent(studentId: Long): Flow<List<Grade>>

    @Query("SELECT * FROM grades WHERE classId = :classId ORDER BY date DESC")
    fun getGradesByClass(classId: Long): Flow<List<Grade>>

    @Query("SELECT * FROM grades WHERE classId = :classId AND subjectId = :subjectId ORDER BY date DESC")
    fun getGradesByClassAndSubject(classId: Long, subjectId: Long): Flow<List<Grade>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrade(grade: Grade): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrades(grades: List<Grade>)

    @Delete
    suspend fun deleteGrade(grade: Grade)

    // === Attendance ===
    @Query("SELECT * FROM attendance ORDER BY date DESC, id DESC")
    fun getAllAttendance(): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE classId = :classId AND date = :date")
    fun getAttendanceByDateAndClass(date: String, classId: Long): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE studentId = :studentId ORDER BY date DESC")
    fun getAttendanceByStudent(studentId: Long): Flow<List<Attendance>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: Attendance): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceList(list: List<Attendance>)

    @Update
    suspend fun updateAttendance(attendance: Attendance)

    @Delete
    suspend fun deleteAttendance(attendance: Attendance)

    @Query("DELETE FROM attendance WHERE classId = :classId AND date = :date AND timeSlot = :timeSlot")
    suspend fun deleteAttendanceBySession(classId: Long, date: String, timeSlot: String)

    // === Timetable Slots ===
    @Query("SELECT * FROM timetable_slots ORDER BY dayOfWeek ASC, startTime ASC")
    fun getAllTimetableSlots(): Flow<List<TimetableSlot>>

    @Query("SELECT * FROM timetable_slots WHERE classId = :classId ORDER BY dayOfWeek ASC, startTime ASC")
    fun getTimetableSlotsByClass(classId: Long): Flow<List<TimetableSlot>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimetableSlot(slot: TimetableSlot): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimetableSlots(slots: List<TimetableSlot>)

    @Delete
    suspend fun deleteTimetableSlot(slot: TimetableSlot)

    // === Users ===
    @Query("SELECT * FROM users ORDER BY role ASC, fullName ASC")
    fun getAllUsers(): Flow<List<UserAccount>>

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserAccount): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserAccount>)

    @Update
    suspend fun updateUser(user: UserAccount)

    @Delete
    suspend fun deleteUser(user: UserAccount)

    // === Payments & Finances ===
    @Query("SELECT * FROM payments ORDER BY date DESC, id DESC")
    fun getAllPayments(): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE studentId = :studentId ORDER BY date DESC")
    fun getPaymentsByStudent(studentId: Long): Flow<List<Payment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayments(payments: List<Payment>)

    @Update
    suspend fun updatePayment(payment: Payment)

    @Delete
    suspend fun deletePayment(payment: Payment)

    // === Notifications ===
    @Query("SELECT * FROM notifications ORDER BY date DESC, id DESC")
    fun getAllNotifications(): Flow<List<SchoolNotification>>

    @Query("SELECT * FROM notifications WHERE targetRole = 'ALL' OR targetRole = :role ORDER BY date DESC, id DESC")
    fun getNotificationsForRole(role: String): Flow<List<SchoolNotification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: SchoolNotification): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<SchoolNotification>)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: Long)

    // === Journal d'Activité / Audit Logs ===
    @Query("SELECT * FROM activity_logs ORDER BY id DESC")
    fun getAllActivityLogs(): Flow<List<com.example.data.model.ActivityLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivityLog(log: com.example.data.model.ActivityLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivityLogs(logs: List<com.example.data.model.ActivityLog>)

    // === Multi-Écoles & Tenants ===
    @Query("SELECT * FROM schools ORDER BY name ASC")
    fun getAllSchools(): Flow<List<com.example.data.model.School>>

    @Query("SELECT * FROM schools WHERE id = :id LIMIT 1")
    suspend fun getSchoolById(id: Long): com.example.data.model.School?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchool(school: com.example.data.model.School): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchools(schools: List<com.example.data.model.School>)

    @Update
    suspend fun updateSchool(school: com.example.data.model.School)

    @Delete
    suspend fun deleteSchool(school: com.example.data.model.School)

    // === Souscriptions Écoles (Inscriptions & Abonnements Annuels) ===
    @Query("SELECT * FROM school_subscriptions ORDER BY id DESC")
    fun getAllSchoolSubscriptions(): Flow<List<com.example.data.model.SchoolSubscription>>

    @Query("SELECT * FROM school_subscriptions WHERE schoolId = :schoolId ORDER BY id DESC")
    fun getSubscriptionsBySchool(schoolId: Long): Flow<List<com.example.data.model.SchoolSubscription>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchoolSubscription(subscription: com.example.data.model.SchoolSubscription): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchoolSubscriptions(subscriptions: List<com.example.data.model.SchoolSubscription>)

    // === Calendrier Scolaire Annuel (Dates clés, Trimestres, Vacances, Examens) ===
    @Query("SELECT * FROM academic_calendar_events ORDER BY startDate ASC")
    fun getAllCalendarEvents(): Flow<List<com.example.data.model.AcademicCalendarEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalendarEvent(event: com.example.data.model.AcademicCalendarEvent): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalendarEvents(events: List<com.example.data.model.AcademicCalendarEvent>)

    @Delete
    suspend fun deleteCalendarEvent(event: com.example.data.model.AcademicCalendarEvent)

    // === Suivi des Paiements & Salaires des Professeurs ===
    @Query("SELECT * FROM teacher_payments ORDER BY id DESC")
    fun getAllTeacherPayments(): Flow<List<com.example.data.model.TeacherPayment>>

    @Query("SELECT * FROM teacher_payments WHERE teacherId = :teacherId ORDER BY id DESC")
    fun getTeacherPaymentsByTeacher(teacherId: Long): Flow<List<com.example.data.model.TeacherPayment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacherPayment(payment: com.example.data.model.TeacherPayment): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacherPayments(payments: List<com.example.data.model.TeacherPayment>)

    @Update
    suspend fun updateTeacherPayment(payment: com.example.data.model.TeacherPayment)

    @Delete
    suspend fun deleteTeacherPayment(payment: com.example.data.model.TeacherPayment)

    // === Publication des Notes par la Scolarité ===
    @Query("UPDATE grades SET isPublished = 1 WHERE classId = :classId AND period = :period")
    suspend fun publishGradesForClassAndPeriod(classId: Long, period: String)

    @Query("UPDATE grades SET isPublished = 1 WHERE id = :gradeId")
    suspend fun publishGrade(gradeId: Long)

    // === Validation des Paiements Locaux par la Scolarité ===
    @Query("UPDATE payments SET isValidatedByScolarite = 1, status = 'PAYE', validatedBy = :validatedBy, validationDate = :validationDate WHERE id = :paymentId")
    suspend fun validateLocalPayment(paymentId: Long, validatedBy: String, validationDate: String)

    // === Nettoyage & Remise à Zéro des Données de Test ===
    @Query("DELETE FROM grades")
    suspend fun clearAllGrades()

    @Query("DELETE FROM attendance")
    suspend fun clearAllAttendance()

    @Query("DELETE FROM payments")
    suspend fun clearAllPayments()

    @Query("DELETE FROM teacher_payments")
    suspend fun clearAllTeacherPayments()

    @Query("DELETE FROM notifications")
    suspend fun clearAllNotifications()

    @Query("DELETE FROM students")
    suspend fun clearAllStudents()

    @Query("DELETE FROM timetable_slots")
    suspend fun clearAllTimetableSlots()
}
