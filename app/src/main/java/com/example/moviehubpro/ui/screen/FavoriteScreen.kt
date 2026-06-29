package com.example.moviehubpro.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.moviehubpro.R
import com.example.moviehubpro.model.Movie
import com.example.moviehubpro.ui.screen.components.EmptyView
import com.example.moviehubpro.ui.screen.viewmodel.FavoriteViewModel
import com.example.moviehubpro.util.Constants
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FavoriteScreen(
    vm: FavoriteViewModel,
    onBack: () -> Unit,
    onMovieClick: (Movie) -> Unit
) {
    val uiState by vm.uiState.collectAsState()
    var showFilterDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(if (uiState.isSelectionMode) "选择影片" else "我的收藏", fontWeight = FontWeight.Black)
                        if (!uiState.isSelectionMode) {
                            Text("${uiState.movies.size} 部影片", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        } else {
                            Text("已选 ${uiState.selectedMovieIds.size} 部", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.isSelectionMode) vm.toggleSelectionMode() else onBack()
                    }) {
                        Icon(if (uiState.isSelectionMode) Icons.Default.Close else Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    if (uiState.isSelectionMode) {
                        IconButton(onClick = { if (uiState.selectedMovieIds.isNotEmpty()) showDeleteConfirm = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "删除已选", tint = Color.Red)
                        }
                    } else {
                        IconButton(onClick = { showFilterDialog = true }) {
                            Icon(Icons.AutoMirrored.Filled.List, contentDescription = "筛选")
                        }
                        IconButton(onClick = { vm.toggleSelectionMode() }) {
                            Icon(Icons.Default.Edit, contentDescription = "管理")
                        }
                    }
                }
            )
        }
    ) { pad ->
        Column(modifier = Modifier.padding(pad).fillMaxSize()) {
            // 搜索框优化：大圆角与对齐
            OutlinedTextField(
                value = uiState.query,
                onValueChange = { vm.onQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("搜索本地收藏...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (uiState.query.isNotEmpty()) {
                        IconButton(onClick = { vm.onQueryChange("") }) {
                            Icon(Icons.Default.Clear, null)
                        }
                    }
                },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )

            if (uiState.movies.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        EmptyView(if (uiState.query.isEmpty()) "暂无收藏电影" else "未找到匹配影片", icon = Icons.Default.FavoriteBorder)
                        if (uiState.query.isEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = onBack) { Text("去发现好片") }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(uiState.movies, key = { it.id }) { movie ->
                        val isSelected = uiState.selectedMovieIds.contains(movie.id)
                        FavoriteItemRow(
                            movie = movie,
                            isSelected = isSelected,
                            isSelectionMode = uiState.isSelectionMode,
                            onToggleSelection = { vm.toggleMovieSelection(movie.id) },
                            onDelete = { vm.removeFavorite(movie) },
                            onClick = { 
                                if (uiState.isSelectionMode) vm.toggleMovieSelection(movie.id) 
                                else onMovieClick(movie) 
                            }
                        )
                    }
                }
            }
        }
    }

    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            title = { Text("本地筛选") },
            text = {
                Column {
                    Text("按年份筛选", style = MaterialTheme.typography.labelMedium)
                    OutlinedTextField(
                        value = uiState.filterYear,
                        onValueChange = { vm.onYearChange(it) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        placeholder = { Text("如: 2024") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = uiState.sortByRating, onCheckedChange = { vm.toggleSortByRating() })
                        Text("按评分高低排序")
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showFilterDialog = false }) { Text("完成") } }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("批量删除") },
            text = { Text("确定要删除已选中的 ${uiState.selectedMovieIds.size} 部影片吗？") },
            confirmButton = {
                Button(
                    onClick = { vm.deleteSelected(); showDeleteConfirm = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("确定删除")
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("取消") } }
        )
    }
}

@Composable
fun FavoriteItemRow(
    movie: Movie,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onToggleSelection: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelectionMode) {
                Checkbox(checked = isSelected, onCheckedChange = { onToggleSelection() })
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            AsyncImage(
                model = "${Constants.POSTER_BASE_URL}/${Constants.POSTER_W342}${movie.posterPath}",
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp, 90.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.ic_launcher_background)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, modifier = Modifier.size(14.dp), tint = Color(0xFFFFD700))
                    Text(
                        text = " ${String.format(Locale.US, "%.1f", movie.voteAverage)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = movie.releaseDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }

            if (!isSelectionMode) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.DeleteOutline, null, tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
                }
            }
        }
    }
}
