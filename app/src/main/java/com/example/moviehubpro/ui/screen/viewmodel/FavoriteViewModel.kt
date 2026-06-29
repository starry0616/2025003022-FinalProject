package com.example.moviehubpro.ui.screen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moviehubpro.data.repository.MovieRepo
import com.example.moviehubpro.model.Movie
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class FavoriteUiState(
    val movies: List<Movie> = emptyList(),
    val query: String = "",
    val filterYear: String = "",
    val sortByRating: Boolean = false,
    val selectedMovieIds: Set<Int> = emptySet(),
    val isSelectionMode: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class FavoriteViewModel(private val movieRepo: MovieRepo) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoriteUiState())
    val uiState: StateFlow<FavoriteUiState> = _uiState.asStateFlow()

    init {
        observeFavorites()
    }

    private fun observeFavorites() {
        _uiState.flatMapLatest { state ->
            if (state.sortByRating) {
                movieRepo.getFavoritesSortedByRating()
            } else {
                movieRepo.searchAndFilterFavorites(state.query, state.filterYear)
            }
        }.onEach { list ->
            _uiState.update { it.copy(movies = list) }
        }.launchIn(viewModelScope)
    }

    fun onQueryChange(newQuery: String) {
        _uiState.update { it.copy(query = newQuery) }
    }

    fun onYearChange(year: String) {
        _uiState.update { it.copy(filterYear = year) }
    }

    fun toggleSortByRating() {
        _uiState.update { it.copy(sortByRating = !it.sortByRating) }
    }

    fun toggleSelectionMode() {
        _uiState.update { it.copy(isSelectionMode = !it.isSelectionMode, selectedMovieIds = emptySet()) }
    }

    fun toggleMovieSelection(movieId: Int) {
        _uiState.update { state ->
            val newSelected = if (state.selectedMovieIds.contains(movieId)) {
                state.selectedMovieIds - movieId
            } else {
                state.selectedMovieIds + movieId
            }
            state.copy(selectedMovieIds = newSelected)
        }
    }

    fun deleteSelected() {
        val idsToDelete = _uiState.value.selectedMovieIds
        viewModelScope.launch {
            _uiState.value.movies.filter { idsToDelete.contains(it.id) }.forEach { movie ->
                movieRepo.toggleFavorite(movie)
            }
            _uiState.update { it.copy(isSelectionMode = false, selectedMovieIds = emptySet()) }
        }
    }

    fun removeFavorite(movie: Movie) {
        viewModelScope.launch {
            movieRepo.toggleFavorite(movie)
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            movieRepo.clearAllFavorites()
        }
    }
}
