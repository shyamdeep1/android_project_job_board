package com.example.android_project.common.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [JobEntity::class, ApplicationEntity::class], version = 3, exportSchema = false)
abstract class JobBoardDatabase : RoomDatabase() {
    abstract fun jobDao(): JobDao
    abstract fun applicationDao(): ApplicationDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `applications` (
                        `id` TEXT NOT NULL,
                        `applicantName` TEXT NOT NULL,
                        `appliedRole` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `timeLabel` TEXT NOT NULL,
                        `applicantUid` TEXT,
                        `applicantEmail` TEXT,
                        `jobId` TEXT,
                        `createdAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_applications_jobId` ON `applications` (`jobId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_applications_applicantUid` ON `applications` (`applicantUid`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_applications_applicantEmail` ON `applications` (`applicantEmail`)")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `applications` ADD COLUMN `resumeUrl` TEXT")
            }
        }

        @Volatile
        private var INSTANCE: JobBoardDatabase? = null

        fun getInstance(context: Context): JobBoardDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    JobBoardDatabase::class.java,
                    "job_board.db"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
