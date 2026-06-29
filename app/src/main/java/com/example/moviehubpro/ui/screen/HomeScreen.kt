package com.example.moviehubpro.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.moviehubpro.R
import com.example.moviehubpro.model.Movie
import com.example.moviehubpro.ui.screen.components.*
import com.example.moviehubpro.ui.screen.viewmodel.HomeViewModel
import com.example.moviehubpro.ui.screen.viewmodel.HomeUiState
import com.example.moviehubpro.util.Constants
import com.example.moviehubpro.data.repository.MovieCategory
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    vm: HomeViewModel,
    isDarkMode: Boolean,
    onToggleDark: (Boolean) -> Unit,
    onMovieClick: (Movie) -> Unit
) {
    val uiState by vm.uiState.collectAsState()
    var isSearching by remember { mutableStateOf(false) }
    
    // 统一处理滚动加载
    val listState = rememberLazyListState()
    val gridState = androidx.compose.foundation.lazy.grid.rememberLazyGridState()

    val pullToRefreshState = rememberPullToRefreshState()
    LaunchedEffect(pullToRefreshState.isRefreshing) {
        if (pullToRefreshState.isRefreshing) vm.refresh()
    }
    LaunchedEffect(uiState.isRefreshing) {
        if (!uiState.isRefreshing) pullToRefreshState.endRefresh()
    }

    val shouldLoadMore = remember {
        derivedStateOf {
            if (uiState.selectedCategory == MovieCategory.CATEGORY) {
                val totalItems = gridState.layoutInfo.totalItemsCount
                val lastVisibleItem = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                lastVisibleItem >= totalItems - 2 && totalItems > 0 && !uiState.isNextPageLoading
            } else {
                val totalItems = listState.layoutInfo.totalItemsCount
                val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                lastVisibleItem >= totalItems - 2 && totalItems > 0 && !uiState.isNextPageLoading
            }
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && !uiState.isRefreshing) {
            vm.loadNextPage()
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("MovieHub Pro", fontWeight = FontWeight.Black) },
                    actions = {
                        IconButton(onClick = { onToggleDark(!isDarkMode) }) {
                            Icon(if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode, null)
                        }
                        IconButton(onClick = {
                            isSearching = !isSearching
                            if (!isSearching) vm.onSearchQueryChange("")
                        }) {
                            Icon(if (isSearching) Icons.Default.Close else Icons.Default.Search, null)
                        }
                    }
                )

                if (!isSearching) {
                    ScrollableTabRow(
                        selectedTabIndex = uiState.selectedCategory.ordinal,
                        edgePadding = 16.dp,
                        containerColor = MaterialTheme.colorScheme.surface,
                        divider = {}
                    ) {
                        MovieCategory.entries.forEach { category ->
                            Tab(
                                selected = uiState.selectedCategory == category,
                                onClick = { vm.setCategory(category) },
                                text = {
                                    Text(
                                        when(category) {
                                            MovieCategory.POPULAR -> "热门"
                                            MovieCategory.UPCOMING -> "上映"
                                            MovieCategory.TOP_RATED -> "高分"
                                            MovieCategory.CATEGORY -> "分类"
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize().nestedScroll(pullToRefreshState.nestedScrollConnection)) {
            
            Column(modifier = Modifier.fillMaxSize()) {
                // 搜索栏集成在内容顶部
                AnimatedVisibility(visible = isSearching) {
                    SearchBar(
                        query = uiState.searchQuery,
                        onQueryChange = { vm.onSearchQueryChange(it) },
                        history = uiState.searchHistory,
                        onHistoryClick = { vm.onSearchQueryChange(it) },
                        onBack = { 
                            isSearching = false
                            vm.onSearchQueryChange("")
                        }
                    )
                }

                if (uiState.selectedCategory == MovieCategory.CATEGORY) {
                    CategoryFilterView(vm, uiState, gridState, onMovieClick)
                } else {
                    MainListView(uiState, listState, isSearching, { isSearching = it }, vm, onMovieClick)
                }
            }

            PullToRefreshContainer(
                state = pullToRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun CategoryFilterView(
    vm: HomeViewModel, 
    uiState: HomeUiState, 
    gridState: androidx.compose.foundation.lazy.grid.LazyGridState,
    onMovieClick: (Movie) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        FilterPanel(
            selectedGenre = uiState.selectedGenre,
            selectedRegion = uiState.selectedRegion,
            selectedYear = uiState.selectedYear,
            onFilterChange = { genre, region, year ->
                vm.updateFilters(genre, region, year)
            }
        )
        
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

        val movies = uiState.filteredMovies
        
        if (uiState.isLoading && movies.isEmpty()) {
            ShimmerLoading()
        } else if (movies.isEmpty()) {
            EmptyView("没有找到符合条件的电影", icon = Icons.Default.FilterListOff)
        } else {
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(movies, key = { it.id }) { movie ->
                    MovieGridItem(movie, onMovieClick)
                }
                
                if (uiState.isNextPageLoading) {
                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(32.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterPanel(
    selectedGenre: String,
    selectedRegion: String,
    selectedYear: String,
    onFilterChange: (genre: String?, region: String?, year: String?) -> Unit
) {
    val genres = listOf("全部", "科幻", "悬疑", "喜剧", "动作", "爱情", "恐怖")
    val regions = listOf("全部", "华语", "欧美", "日韩", "其它")
    val years = listOf("全部", "2020年代", "2010年代", "2000年代", "90年代")

    Column(modifier = Modifier.padding(top = 8.dp)) {
        FilterRow("类型", genres, selectedGenre) { onFilterChange(it, null, null) }
        FilterRow("地区", regions, selectedRegion) { onFilterChange(null, it, null) }
        FilterRow("年份", years, selectedYear) { onFilterChange(null, null, it) }
    }
}

@Composable
fun FilterRow(label: String, options: List<String>, selected: String, onSelect: (String) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, modifier = Modifier.padding(start = 16.dp, end = 8.dp), fontSize = 12.sp, color = Color.Gray)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(horizontal = 8.dp)) {
            items(options) { option ->
                val isSelected = option == selected
                SuggestionChip(
                    onClick = { onSelect(option) },
                    label = { Text(option, fontSize = 12.sp) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        labelColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                    ),
                    border = if (isSelected) null else SuggestionChipDefaults.suggestionChipBorder(enabled = true),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    }
}

@Composable
fun MovieGridItem(
    movie: Movie, 
    onClick: (Movie) -> Unit
) {
    Column(modifier = Modifier.clickable { onClick(movie) }) {
        Card(
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            AsyncImage(
                model = "${Constants.POSTER_BASE_URL}/${Constants.POSTER_W500}${movie.posterPath}",
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(movie.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Star, null, modifier = Modifier.size(12.dp), tint = Color(0xFFFFD700))
            Text(" ${String.format(Locale.US, "%.1f", movie.voteAverage)}", fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainListView(
    uiState: HomeUiState, 
    listState: androidx.compose.foundation.lazy.LazyListState, 
    isSearching: Boolean, 
    onSearchToggle: (Boolean) -> Unit,
    vm: HomeViewModel, 
    onMovieClick: (Movie) -> Unit
) {
    val allMovies = if (uiState.searchQuery.isNotEmpty()) uiState.searchResults else uiState.movies
    LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
        if (allMovies.isNotEmpty() && !isSearching && !uiState.isRefreshing) {
            item(key = "hero_banner") {
                HeroBanner(movies = allMovies.take(5), onMovieClick = onMovieClick)
            }
            item(key = "category_pills") {
                CategoryPillsRow(onCategoryClick = { category ->
                    // 修复：不再进入搜索，而是切换到「分类」频道并自动选中类型
                    vm.setCategory(MovieCategory.CATEGORY)
                    vm.updateFilters(genre = category.name)
                })
            }
            item(key = "trending_title") { SectionTitle("本周趋势") }
            item(key = "trending_row") { TrendingRow(movies = allMovies.drop(5).take(10), onMovieClick = onMovieClick) }
            item(key = "main_list_title") {
                SectionTitle(when(uiState.selectedCategory) {
                    MovieCategory.POPULAR -> "全网热门"
                    MovieCategory.UPCOMING -> "近期上映"
                    MovieCategory.TOP_RATED -> "高分经典"
                    else -> ""
                })
            }
        }
        items(allMovies, key = { it.id }) { movie ->
            MovieCard(
                movie = movie, 
                onClick = { onMovieClick(movie) }
            )
        }
        
        if (uiState.isNextPageLoading) {
            item(key = "loading_indicator") {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HeroBanner(movies: List<Movie>, onMovieClick: (Movie) -> Unit) {
    if (movies.isEmpty()) return
    val pagerState = rememberPagerState(pageCount = { movies.size })
    Box(modifier = Modifier.fillMaxWidth().height(220.dp).padding(16.dp)) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp))) { page ->
            val movie = movies.getOrNull(page) ?: return@HorizontalPager
            Box(modifier = Modifier.fillMaxSize().clickable { onMovieClick(movie) }) {
                AsyncImage(
                    model = "${Constants.POSTER_BASE_URL}/${Constants.POSTER_W500}${movie.backdropPath ?: movie.posterPath}",
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)), startY = 300f)))
                Text(movie.title, modifier = Modifier.align(Alignment.BottomStart).padding(16.dp), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
        Row(modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(movies.size) { iteration ->
                val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.5f)
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(color))
            }
        }
    }
}

@Composable
fun CategoryPillsRow(onCategoryClick: (MovieCategoryItem) -> Unit) {
    val categories = listOf(
        MovieCategoryItem(28, "动作", "🎬"),
        MovieCategoryItem(27, "恐怖", "👻"),
        MovieCategoryItem(10749, "爱情", "❤️"),
        MovieCategoryItem(35, "喜剧", "😂"),
        MovieCategoryItem(878, "科幻", "🚀"),
        MovieCategoryItem(16, "动画", "🧸"),
        MovieCategoryItem(80, "犯罪", "🚔"),
        MovieCategoryItem(18, "剧情", "🎭")
    )
    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(vertical = 8.dp)) {
        items(categories) { category ->
            Surface(onClick = { onCategoryClick(category) }, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), shape = RoundedCornerShape(24.dp)) {
                Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(category.emoji, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(category.name, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(title, modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp), fontSize = 20.sp, fontWeight = FontWeight.Black)
}

@Composable
fun TrendingRow(movies: List<Movie>, onMovieClick: (Movie) -> Unit) {
    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(movies, key = { it.id }) { movie ->
            Card(modifier = Modifier.width(120.dp).clickable { onMovieClick(movie) }, shape = RoundedCornerShape(12.dp)) {
                Box {
                    AsyncImage(model = "${Constants.POSTER_BASE_URL}/${Constants.POSTER_W342}${movie.posterPath}", contentDescription = null, modifier = Modifier.height(180.dp).fillMaxWidth(), contentScale = ContentScale.Crop)
                }
            }
        }
    }
}

@Composable
fun MovieCard(
    movie: Movie, 
    modifier: Modifier = Modifier, 
    onClick: () -> Unit
) {
    Card(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).clickable { onClick() }, shape = RoundedCornerShape(16.dp)) {
        Row(modifier = Modifier.padding(12.dp)) {
            AsyncImage(
                model = "${Constants.POSTER_BASE_URL}/${Constants.POSTER_W500}${movie.posterPath}", 
                contentDescription = null, 
                modifier = Modifier
                    .size(100.dp, 150.dp)
                    .clip(RoundedCornerShape(12.dp)), 
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(start = 16.dp).fillMaxWidth()) {
                Text(movie.title, fontWeight = FontWeight.Bold, fontSize = 18.sp, maxLines = 1)
                RatingBar(movie.voteAverage)
                Text(movie.overview, fontSize = 13.sp, maxLines = 3, overflow = TextOverflow.Ellipsis, color = Color.Gray)
            }
        }
    }
}

@Composable
fun RatingBar(rating: Double) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        repeat(5) { index ->
            val color = if (index < (rating / 2).toInt()) Color(0xFFFFD700) else Color.LightGray
            Icon(Icons.Default.Star, null, modifier = Modifier.size(14.dp), tint = color)
        }
        Text("  ${String.format(Locale.US, "%.1f", rating)}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit, history: List<String>, onHistoryClick: (String) -> Unit, onBack: () -> Unit) {
    Column(modifier = Modifier.padding(8.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("搜索全网电影...") },
            leadingIcon = { 
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null) 
                }
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Clear, null)
                    }
                }
            },
            shape = MaterialTheme.shapes.medium
        )
        if (history.isNotEmpty() && query.isEmpty()) {
            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.History, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                Text(" 最近搜索", fontSize = 12.sp, color = Color.Gray)
            }
            Row(modifier = Modifier.padding(horizontal = 8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                history.take(3).forEach { item ->
                    SuggestionChip(
                        onClick = { onHistoryClick(item) },
                        label = { Text(item) }
                    )
                }
            }
        }
    }
}

data class MovieCategoryItem(val id: Int, val name: String, val emoji: String)
