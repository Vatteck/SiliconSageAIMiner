package com.siliconsage.miner.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [GameState::class, Upgrade::class], version = 13, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                try {
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "silicon_sage_db"
                    )
                    .fallbackToDestructiveMigration(true) // v2.8.0: Auto-reset on schema mismatch
                    .build()
                    INSTANCE = instance
                    instance
                } catch (e: Exception) {
                    // Nuclear option: delete and recreate
                    context.deleteDatabase("silicon_sage_db")
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "silicon_sage_db"
                    )
                    .fallbackToDestructiveMigration(true)
                    .build()
                    INSTANCE = instance
                    instance
                }
            }
        }
    }
}
