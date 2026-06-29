package com.example.moviehubpro.data.repository

import com.example.moviehubpro.data.api.ApiService
import com.example.moviehubpro.data.api.toDomain
import com.example.moviehubpro.data.db.MovieDao
import com.example.moviehubpro.data.db.entity.toCacheEntity
import com.example.moviehubpro.data.db.entity.toDomain as entityToDomain
import com.example.moviehubpro.data.db.entity.toFavoriteEntity
import com.example.moviehubpro.model.Movie
import com.example.moviehubpro.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

enum class MovieCategory { POPULAR, UPCOMING, TOP_RATED, CATEGORY }

class MovieRepo(
    private val apiService: ApiService,
    private val movieDao: MovieDao
) {
    suspend fun getMovies(category: MovieCategory, page: Int = 1): Result<List<Movie>> {
        return try {
            val response = when(category) {
                MovieCategory.POPULAR -> apiService.getPopularMovies(Constants.API_KEY, page)
                MovieCategory.UPCOMING -> apiService.getUpcomingMovies(Constants.API_KEY, page)
                MovieCategory.TOP_RATED -> apiService.getTopRatedMovies(Constants.API_KEY, page)
                MovieCategory.CATEGORY -> return Result.success(emptyList()) // 分类筛选暂不走此接口
            }
            
            if (response.isSuccessful) {
                val movies = response.body()?.results?.map { it.toDomain() } ?: emptyList()
                if (page == 1) {
                    movieDao.clearCacheByType(category.name)
                    movieDao.insertCache(movies.map { it.toCacheEntity(category.name) })
                }
                Result.success(movies)
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            val cached = movieDao.getCacheByType(category.name).map { it.entityToDomain() }
            if (cached.isNotEmpty()) Result.success(cached) else Result.success(getMockMovies())
        }
    }

    suspend fun searchMovies(query: String, page: Int = 1): Result<List<Movie>> {
        if (query.isBlank()) return Result.success(emptyList())
        return try {
            val response = apiService.searchMovies(Constants.API_KEY, query, page)
            if (response.isSuccessful) {
                Result.success(response.body()?.results?.map { it.toDomain() } ?: emptyList())
            } else {
                throw Exception()
            }
        } catch (e: Exception) {
            if (page > 1) return Result.success(emptyList())
            // 增强：离线时搜索本地收藏
            val localMatch = movieDao.getAllFavorites().first()
                .map { it.entityToDomain() }
                .filter { it.title.contains(query, ignoreCase = true) }
            
            if (localMatch.isNotEmpty()) Result.success(localMatch) 
            else Result.success(getMockMovies().filter { it.title.contains(query, ignoreCase = true) })
        }
    }

    suspend fun discoverMovies(
        genreId: Int?,
        langCode: String?,
        year: Int?,
        page: Int = 1
    ): Result<List<Movie>> {
        return try {
            val response = apiService.discoverMovies(
                apiKey = Constants.API_KEY,
                genreId = genreId?.toString(),
                languageCode = langCode,
                year = year,
                page = page
            )
            if (response.isSuccessful) {
                Result.success(response.body()?.results?.map { it.toDomain() } ?: emptyList())
            } else {
                Result.failure(Exception("Discovery Error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAllFavorites(): Flow<List<Movie>> = movieDao.getAllFavorites().map { list ->
        list.map { it.entityToDomain() }
    }

    fun getFavoritesSortedByRating(): Flow<List<Movie>> = movieDao.getFavoritesSortedByRating().map { list ->
        list.map { it.entityToDomain() }
    }

    fun searchAndFilterFavorites(query: String, year: String): Flow<List<Movie>> = 
        movieDao.searchFavorites(query, year).map { list ->
            list.map { it.entityToDomain() }
        }

    suspend fun toggleFavorite(movie: Movie) {
        if (movieDao.isMovieFavorite(movie.id)) {
            movieDao.deleteFavorite(movie.toFavoriteEntity())
        } else {
            movieDao.insertFavorite(movie.toFavoriteEntity())
        }
    }

    suspend fun isFavorite(movieId: Int): Boolean = movieDao.isMovieFavorite(movieId)

    suspend fun clearAllFavorites() = movieDao.clearAllFavorites()

    private fun getMockMovies() = listOf(
        Movie(1, "模拟: 肖申克的救赎", "希望是件好东西。", null, null, "1994", 9.3, listOf(18), "en"),
        Movie(2, "模拟: 泰坦尼克号", "真爱永恒。", null, null, "1997", 8.5, listOf(18, 10749), "en")
    )
}
