package com.example.moviehubpro.ui.screen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moviehubpro.data.datastore.AppDataStore
import com.example.moviehubpro.data.repository.MovieCategory
import com.example.moviehubpro.data.repository.MovieRepo
import com.example.moviehubpro.util.NetworkMonitor
import com.example.moviehubpro.model.Movie
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class HomeUiState(
    val movies: List<Movie> = emptyList(),
    val searchResults: List<Movie> = emptyList(),
    val selectedCategory: MovieCategory = MovieCategory.POPULAR,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isNextPageLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val searchHistory: List<String> = emptyList(),
    val isOnline: Boolean = true,
    // 分类筛选字段
    val selectedGenre: String = "全部",
    val selectedRegion: String = "全部",
    val selectedYear: String = "全部",
    val filteredMovies: List<Movie> = emptyList()
)

class HomeViewModel(
    private val movieRepo: MovieRepo,
    private val appDataStore: AppDataStore,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var currentPage = 1
    private var isLastPage = false
    
    private var currentSearchPage = 1
    private var isLastSearchPage = false
    
    private var filterJob: Job? = null
    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            appDataStore.searchHistoryFlow.collect { history ->
                _uiState.update { it.copy(searchHistory = history) }
            }
        }
        viewModelScope.launch {
            networkMonitor.isOnline.collect { online ->
                _uiState.update { it.copy(isOnline = online) }
            }
        }
        loadMovies()
    }

    fun setCategory(category: MovieCategory) {
        if (_uiState.value.selectedCategory == category) return
        _uiState.update { it.copy(selectedCategory = category) }
        
        currentPage = 1
        isLastPage = false
        
        if (category != MovieCategory.CATEGORY) {
            _uiState.update { it.copy(movies = emptyList()) }
            loadMovies()
        } else {
            _uiState.update { it.copy(filteredMovies = emptyList()) }
            triggerDiscover()
        }
    }

    fun refresh() {
        currentPage = 1
        isLastPage = false
        if (_uiState.value.selectedCategory == MovieCategory.CATEGORY) {
            triggerDiscover(isRefresh = true)
        } else {
            loadMovies(isRefresh = true)
        }
    }

    fun loadNextPage() {
        if (_uiState.value.isNextPageLoading) return
        
        if (_uiState.value.searchQuery.isNotEmpty()) {
            if (isLastSearchPage) return
            currentSearchPage++
            loadSearchResults(isNextPage = true)
        } else {
            if (isLastPage) return
            currentPage++
            if (_uiState.value.selectedCategory == MovieCategory.CATEGORY) {
                triggerDiscover(isNextPage = true)
            } else {
                loadMovies(isNextPage = true)
            }
        }
    }

    private fun loadMovies(isRefresh: Boolean = false, isNextPage: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { 
                when {
                    isRefresh -> it.copy(isRefreshing = true, error = null)
                    isNextPage -> it.copy(isNextPageLoading = true)
                    else -> it.copy(isLoading = true, error = null)
                }
            }

            val result = movieRepo.getMovies(_uiState.value.selectedCategory, currentPage)
            result.onSuccess { list ->
                if (list.isEmpty()) isLastPage = true
                _uiState.update { state ->
                    state.copy(
                        movies = if (isRefresh) list else (state.movies + list).distinctBy { it.id },
                        isLoading = false,
                        isRefreshing = false,
                        isNextPageLoading = false
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message, isLoading = false, isRefreshing = false, isNextPageLoading = false) }
            }
        }
    }

    // --- 全库筛选逻辑 (Discover API) ---

    fun updateFilters(genre: String? = null, region: String? = null, year: String? = null) {
        _uiState.update { state ->
            state.copy(
                selectedGenre = genre ?: state.selectedGenre,
                selectedRegion = region ?: state.selectedRegion,
                selectedYear = year ?: state.selectedYear
            )
        }
        currentPage = 1
        isLastPage = false
        triggerDiscover()
    }

    private fun triggerDiscover(isRefresh: Boolean = false, isNextPage: Boolean = false) {
        filterJob?.cancel()
        filterJob = viewModelScope.launch {
            if (!isNextPage) delay(300) // 防抖
            
            _uiState.update { 
                when {
                    isRefresh -> it.copy(isRefreshing = true)
                    isNextPage -> it.copy(isNextPageLoading = true)
                    else -> it.copy(isLoading = true)
                }
            }

            val state = _uiState.value
            val result = movieRepo.discoverMovies(
                genreId = getGenreId(state.selectedGenre),
                langCode = getLangCode(state.selectedRegion),
                year = getSpecificYear(state.selectedYear),
                page = currentPage
            )

            result.onSuccess { list ->
                if (list.isEmpty()) isLastPage = true
                _uiState.update { 
                    it.copy(
                        filteredMovies = if (isNextPage) it.filteredMovies + list else list,
                        isLoading = false,
                        isRefreshing = false,
                        isNextPageLoading = false
                    )
                }
            }.onFailure {
                _uiState.update { it.copy(isLoading = false, isRefreshing = false, isNextPageLoading = false) }
            }
        }
    }

    private fun getGenreId(name: String): Int? = when(name) {
        "科幻" -> 878; "悬疑" -> 9648; "喜剧" -> 35; "动作" -> 28; "爱情" -> 10749; "恐怖" -> 27; else -> null
    }

    private fun getLangCode(name: String): String? = when(name) {
        "华语" -> "zh"; "欧美" -> "en"; "日韩" -> "ja"; else -> null
    }

    private fun getSpecificYear(name: String): Int? = when(name) {
        "2020年代" -> 2024; "2010年代" -> 2015; "2000年代" -> 2005; "90年代" -> 1995; else -> null
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        if (query.isBlank()) {
            searchJob?.cancel()
            _uiState.update { it.copy(searchResults = emptyList()) }
            return
        }
        
        currentSearchPage = 1
        isLastSearchPage = false
        
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)
            loadSearchResults(isNextPage = false)
        }
    }
    
    private fun loadSearchResults(isNextPage: Boolean) {
        viewModelScope.launch {
            if (isNextPage) {
                _uiState.update { it.copy(isNextPageLoading = true) }
            } else {
                _uiState.update { it.copy(isLoading = true) }
            }
            
            val result = movieRepo.searchMovies(_uiState.value.searchQuery, currentSearchPage)
            result.onSuccess { list ->
                if (list.isEmpty()) isLastSearchPage = true
                _uiState.update { state ->
                    state.copy(
                        searchResults = if (isNextPage) (state.searchResults + list).distinctBy { it.id } else list,
                        isLoading = false,
                        isNextPageLoading = false
                    )
                }
                if (!isNextPage && list.isNotEmpty()) {
                    appDataStore.addSearchHistory(_uiState.value.searchQuery)
                }
            }.onFailure {
                _uiState.update { it.copy(isLoading = false, isNextPageLoading = false) }
            }
        }
    }
}
