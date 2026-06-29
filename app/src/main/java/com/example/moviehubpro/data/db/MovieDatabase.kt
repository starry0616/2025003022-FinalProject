package com.example.moviehubpro.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.moviehubpro.data.db.entity.FavoriteMovieEntity
import com.example.moviehubpro.data.db.entity.MovieCacheEntity

@Database(entities = [FavoriteMovieEntity::class, MovieCacheEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
}
