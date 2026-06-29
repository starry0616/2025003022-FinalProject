package com.example.moviehubpro.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 业务 Domain 模型
 */
@Parcelize
data class Movie(
    val id: Int,
    val title: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String,
    val voteAverage: Double,
    val genreIds: List<Int> = emptyList(),
    val originalLanguage: String = "en",
    val isFavorite: Boolean = false
) : Parcelable
