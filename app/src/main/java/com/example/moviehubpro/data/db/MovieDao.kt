package com.example.moviehubpro.data.db

import androidx.room.*
import com.example.moviehubpro.data.db.entity.FavoriteMovieEntity
import com.example.moviehubpro.data.db.entity.MovieCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {

    // --- 收藏相关 (FavoriteMovieEntity) ---
    
    @Query("SELECT * FROM favorite_movies ORDER BY createdAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteMovieEntity>>

    // 复杂查询 ①：按评分从高到低排序
    @Query("SELECT * FROM favorite_movies ORDER BY voteAverage DESC")
    fun getFavoritesSortedByRating(): Flow<List<FavoriteMovieEntity>>

    // 复杂查询 ②：搜索收藏并按日期筛选 (模糊匹配)
    @Query("SELECT * FROM favorite_movies WHERE title LIKE '%' || :query || '%' AND releaseDate LIKE :year || '%'")
    fun searchFavorites(query: String, year: String): Flow<List<FavoriteMovieEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(movie: FavoriteMovieEntity)

    @Delete
    suspend fun deleteFavorite(movie: FavoriteMovieEntity)

    @Query("DELETE FROM favorite_movies")
    suspend fun clearAllFavorites()

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_movies WHERE id = :id)")
    suspend fun isMovieFavorite(id: Int): Boolean

    // --- 缓存相关 (MovieCacheEntity) ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCache(movies: List<MovieCacheEntity>) // 批量插入优化性能

    @Query("SELECT * FROM movie_cache WHERE cacheType = :type")
    suspend fun getCacheByType(type: String): List<MovieCacheEntity>

    @Query("DELETE FROM movie_cache WHERE cacheType = :type")
    suspend fun clearCacheByType(type: String)
}
