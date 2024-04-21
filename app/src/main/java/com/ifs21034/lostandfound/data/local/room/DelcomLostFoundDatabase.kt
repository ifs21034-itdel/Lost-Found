package com.ifs21034.lostandfound.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ifs21034.lostandfound.data.local.entity.DelcomLostFoundEntity

@Database(entities = [DelcomLostFoundEntity::class], version = 1, exportSchema = false)
abstract class DelcomLostFoundDatabase : RoomDatabase() {
    abstract fun delcomLostFoundDao(): IDelcomLostFoundDao
    companion object {
        private const val Database_NAME = "DelcomLostFound.db"
        @Volatile
        private var INSTANCE: DelcomLostFoundDatabase? = null
        @JvmStatic
        fun getInstance(context: Context): DelcomLostFoundDatabase {
            if (INSTANCE == null) {
                synchronized(DelcomLostFoundDatabase::class.java) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        DelcomLostFoundDatabase::class.java,
                        Database_NAME
                    ).build()
                }
            }
            return INSTANCE as DelcomLostFoundDatabase
        }
    }
}