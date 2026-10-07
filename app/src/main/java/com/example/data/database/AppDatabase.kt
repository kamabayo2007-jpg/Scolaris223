package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        com.example.data.model.School::class,
        com.example.data.model.SchoolSubscription::class,
        SchoolClass::class,
        Student::class,
        Subject::class,
        Teacher::class,
        Grade::class,
        Attendance::class,
        TimetableSlot::class,
        UserAccount::class,
        Payment::class,
        SchoolNotification::class,
        com.example.data.model.ActivityLog::class,
        com.example.data.model.AcademicCalendarEvent::class,
        com.example.data.model.TeacherPayment::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun schoolDao(): SchoolDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "edugestion_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        InitialDataSeeder.populateDatabase(database.schoolDao())
                    }
                }
            }
        }
    }
}
