package com.muzic.aplay.db

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

@Database(entities = [YoutubeStream::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class YoutubeStreamDatabase : RoomDatabase() {
    abstract fun youtubeDao(): YoutubeStreamDao

    companion object {
        @Volatile
        private var INSTANCE: YoutubeStreamDatabase? = null

        fun get(context: Context): YoutubeStreamDatabase {
            if (INSTANCE != null) return INSTANCE!!
            return synchronized(this) {
                if (INSTANCE != null) return@synchronized INSTANCE!!
                INSTANCE = Room.databaseBuilder(context.applicationContext, YoutubeStreamDatabase::class.java, "youtube_stream_db").build()
                INSTANCE!!
            }
        }
    }
}

@Dao
interface YoutubeStreamDao {

    @Query("SELECT * FROM youtube_stream_table ORDER BY id ASC")
    fun getAllData(): LiveData<List<YoutubeStream>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(stream: YoutubeStream)

    @Delete
    fun delete(stream: YoutubeStream)

}

@Entity(tableName = "youtube_stream_table")
data class YoutubeStream(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var uid: String,
    var url: String?,
    var title: String,
    var contentLength: String,
    var mimeType: String,
    var fileName: String,
    var update: Long = System.currentTimeMillis()
) {
    fun details(): String {
        return "${toRegex.find(mimeType)?.destructured?.component1()}::" + String.format("%.2f Mb", contentLength.toDouble() / 1024 / 1024)
    }
}

val toRegex = "\"(.+)\"".toRegex()
