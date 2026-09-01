package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.ContainerDao
import com.example.data.local.entity.ContainerAppEntity
import com.example.data.local.entity.ContainerSystemEntity
import com.example.data.local.entity.ConversionJobEntity
import com.example.data.local.entity.SupervisorLogEntity

@Database(
    entities = [
        ContainerSystemEntity::class,
        ContainerAppEntity::class,
        ConversionJobEntity::class,
        SupervisorLogEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun containerDao(): ContainerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "osdockbox_containers.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
