package com.example.moviehubpro.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.moviehubpro.model.Movie

/**
 * 收藏电影表
 */
@Entity(tableName = "favorite_movies")
data class FavoriteMovieEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String,
    val voteAverage: Double,
    val originalLanguage: String = "en",
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 网络离线缓存表
 */
@Entity(tableName = "movie_cache")
data class MovieCacheEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String,
    val voteAverage: Double,
    val originalLanguage: String = "en",
    val cacheType: String
)

// 数据转换扩展函数
fun FavoriteMovieEntity.toDomain() = Movie(id, title, overview, posterPath, backdropPath, releaseDate, voteAverage, emptyList(), originalLanguage, true)
fun MovieCacheEntity.toDomain() = Movie(id, title, overview, posterPath, backdropPath, releaseDate, voteAverage, emptyList(), originalLanguage, false)
fun Movie.toFavoriteEntity() = FavoriteMovieEntity(id, title, overview, posterPath, backdropPath, releaseDate, voteAverage, originalLanguage)
fun Movie.toCacheEntity(type: String) = MovieCacheEntity(id, title, overview, posterPath, backdropPath, releaseDate, voteAverage, originalLanguage, type)
