package com.muzic.aplay.db

import android.content.Context
import androidx.room.*
import java.util.*

@Database(entities = [YoutubeStream::class, Podcast::class, Episode::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class YoutubeStreamDatabase : RoomDatabase() {
    abstract fun youtubeDao(): YoutubeStreamDao
    abstract fun podcastDao(): PodcastDao

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

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return if (value == null) null else Date(value)
    }
    @TypeConverter
    fun toTimestamp(date: Date?): Long? {
        return (date?.time)
    }
}