package com.rogue.brainrottracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [UsageEntity::class], version = 1, exportSchema = false)
abstract  class AppDatabase : RoomDatabase() {

    abstract fun usageDao(): UsageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "brainrot_database"
                ).build()
                INSTANCE=instance
                instance
            }
        }
    }

}