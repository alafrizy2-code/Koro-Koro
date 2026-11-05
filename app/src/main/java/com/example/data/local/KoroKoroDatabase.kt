package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        FavoriteSongEntity::class,
        HistorySongEntity::class,
        PlaylistEntity::class,
        PlaylistSongEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class KoroKoroDatabase : RoomDatabase() {
    abstract fun karaokeDao(): KaraokeDao

    companion object {
        @Volatile
        private var INSTANCE: KoroKoroDatabase? = null

        fun getInstance(context: Context): KoroKoroDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KoroKoroDatabase::class.java,
                    "korokoro_tv_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
