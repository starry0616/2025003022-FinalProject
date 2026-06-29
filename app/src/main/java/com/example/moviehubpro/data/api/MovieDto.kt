package com.example.moviehubpro.data.api

import com.example.moviehubpro.model.Movie
import com.google.gson.annotations.SerializedName

/**
 * TMDB 网络响应 DTO
 */
data class MovieResponseDto(
    val results: List<MovieDto>
)

data class MovieDto(
    val id: Int,
    val title: String,
    val overview: String,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?, // 解析背景图
    @SerializedName("release_date") val releaseDate: String?,
    @SerializedName("vote_average") val voteAverage: Double,
    @SerializedName("genre_ids") val genreIds: List<Int>?,
    @SerializedName("original_language") val originalLanguage: String?
)

// 网络对象转业务对象
fun MovieDto.toDomain() = Movie(
    id = id,
    title = title,
    overview = overview,
    posterPath = posterPath,
    backdropPath = backdropPath,
    releaseDate = releaseDate ?: "未知",
    voteAverage = voteAverage,
    genreIds = genreIds ?: emptyList(),
    originalLanguage = originalLanguage ?: "en"
)
