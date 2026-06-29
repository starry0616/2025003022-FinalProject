package com.example.moviehubpro.ui.screen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moviehubpro.data.repository.MovieCategory
import com.example.moviehubpro.data.repository.MovieRepo
import com.example.moviehubpro.model.Movie
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DetailUiState(
    val isFavorite: Boolean = false,
    val similarMovies: List<Movie> = emptyList()
)

class DetailViewModel(private val movieRepo: MovieRepo) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    fun checkFavoriteStatus(movieId: Int) {
        viewModelScope.launch {
            val isFav = movieRepo.isFavorite(movieId)
            _uiState.update { it.copy(isFavorite = isFav) }
            
            // 修正：使用统一的 getMovies 接口获取推荐
            val result = movieRepo.getMovies(MovieCategory.POPULAR)
            result.onSuccess { list ->
                _uiState.update { it.copy(similarMovies = list.shuffled().take(5)) }
            }
        }
    }

    fun toggleFavorite(movie: Movie) {
        viewModelScope.launch {
            movieRepo.toggleFavorite(movie)
            checkFavoriteStatus(movie.id)
        }
    }
}
