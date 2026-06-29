package com.example.moviehubpro.data.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1,
        @Query("language") language: String = "zh-CN"
    ): Response<MovieResponseDto>

    @GET("movie/upcoming")
    suspend fun getUpcomingMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1,
        @Query("language") language: String = "zh-CN"
    ): Response<MovieResponseDto>

    @GET("movie/top_rated")
    suspend fun getTopRatedMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1,
        @Query("language") language: String = "zh-CN"
    ): Response<MovieResponseDto>

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("language") language: String = "zh-CN"
    ): Response<MovieResponseDto>

    @GET("discover/movie")
    suspend fun discoverMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1,
        @Query("with_genres") genreId: String? = null,
        @Query("with_original_language") languageCode: String? = null,
        @Query("primary_release_year") year: Int? = null,
        @Query("language") language: String = "zh-CN",
        @Query("sort_by") sortBy: String = "popularity.desc"
    ): Response<MovieResponseDto>
}
