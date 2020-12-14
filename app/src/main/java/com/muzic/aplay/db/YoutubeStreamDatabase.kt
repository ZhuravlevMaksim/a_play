package com.muzic.aplay.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [YoutubeStream::class], version = 1, exportSchema = false)
abstract class YoutubeStreamDatabase : RoomDatabase() {
    abstract fun dao(): YoutubeStreamRepository

    companion object {
        @Volatile
        private var INSTANCE: YoutubeStreamDatabase? = null

        fun get(context: Context): YoutubeStreamDatabase {
            if (INSTANCE != null) return INSTANCE!!
            return synchronized(this){
                if (INSTANCE != null) return@synchronized INSTANCE!!
                INSTANCE = Room.databaseBuilder(context.applicationContext, YoutubeStreamDatabase::class.java, "youtube_stream_db").build()
                INSTANCE!!
            }
        }
    }
}