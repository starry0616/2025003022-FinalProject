package com.example.moviehubpro.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.moviehubpro.R
import com.example.moviehubpro.model.Movie
import com.example.moviehubpro.ui.screen.components.shimmer
import com.example.moviehubpro.ui.screen.viewmodel.DetailViewModel
import com.example.moviehubpro.util.Constants
import java.util.Locale

@Composable
fun DetailScreen(
    movie: Movie,
    vm: DetailViewModel,
    onBack: () -> Unit,
    onMovieClick: (Movie) -> Unit
) {
    val uiState by vm.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    
    val scale by animateFloatAsState(
        targetValue = if (uiState.isFavorite) 1.2f else 1f, 
        animationSpec = spring(Spring.DampingRatioHighBouncy),
        label = "favScale"
    )

    LaunchedEffect(movie.id) { vm.checkFavoriteStatus(movie.id) }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        
        // 1. Hero Backdrop (沉浸式背景)
        Box(modifier = Modifier.fillMaxWidth().height(400.dp)) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data("${Constants.POSTER_BASE_URL}/${Constants.POSTER_W500}${movie.backdropPath ?: movie.posterPath}")
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().blur(if (scrollState.value > 100) 10.dp else 0.dp),
                contentScale = ContentScale.Crop
            )
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.4f),
                                Color.Transparent,
                                MaterialTheme.colorScheme.background.copy(alpha = 0.9f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            )
        }

        // 2. Content Layer
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(260.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Surface(
                    modifier = Modifier
                        .width(130.dp)
                        .height(190.dp)
                        .shadow(12.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data("${Constants.POSTER_BASE_URL}/${Constants.POSTER_W500}${movie.posterPath}")
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.shimmer()
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                    Surface(
                        color = Color(0xFFFFD700),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "IMDb ${String.format(Locale.US, "%.1f", movie.voteAverage)}",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = movie.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 28.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${movie.releaseDate} • ${translateLang(movie.originalLanguage)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { 
                        val intent = android.content.Intent(
                            android.content.Intent.ACTION_VIEW, 
                            android.net.Uri.parse("https://www.youtube.com/results?search_query=${movie.title}+trailer")
                        )
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.PlayArrow, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("观看预告")
                }
                
                OutlinedButton(
                    onClick = { vm.toggleFavorite(movie) },
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        if (uiState.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        null,
                        tint = if (uiState.isFavorite) Color.Red else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.scale(scale)
                    )
                }
            }

            DetailSectionTitle("剧情简介")
            Text(
                text = if (movie.overview.isNullOrBlank()) "暂无该电影的详细剧情介绍。" else movie.overview,
                modifier = Modifier.padding(horizontal = 24.dp),
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 24.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            DetailSectionTitle("相关推荐")
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.similarMovies, key = { it.id }) { sim ->
                    SimilarMovieItem(sim, context, onMovieClick)
                }
            }
        }

        DetailTopBar(onBack = onBack, movieTitle = movie.title, scrollState = scrollState)
    }
}

@Composable
fun DetailTopBar(onBack: () -> Unit, movieTitle: String, scrollState: ScrollState) {
    val alpha by animateFloatAsState(
        targetValue = if (scrollState.value > 300) 1f else 0f,
        label = "TopBarAlpha"
    )

    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = alpha),
        modifier = Modifier.fillMaxWidth().statusBarsPadding()
    ) {
        Row(
            modifier = Modifier.height(64.dp).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.background(
                    if (alpha < 0.5f) Color.Black.copy(alpha = 0.3f) else Color.Transparent,
                    CircleShape
                )
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
            }
            
            if (alpha > 0.8f) {
                Text(
                    text = movieTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun DetailSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(24.dp)
    )
}

@Composable
fun SimilarMovieItem(movie: Movie, context: android.content.Context, onClick: (Movie) -> Unit) {
    Card(
        modifier = Modifier.width(110.dp).clickable { onClick(movie) },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data("${Constants.POSTER_BASE_URL}/${Constants.POSTER_W342}${movie.posterPath}")
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier.height(160.dp),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.ic_launcher_background)
        )
    }
}

fun translateLang(lang: String): String {
    return when (lang.lowercase()) {
        "zh", "cn" -> "华语"
        "en" -> "欧美"
        "ja" -> "日本"
        "ko" -> "韩国"
        "hi" -> "印度"
        "fr" -> "法国"
        "de" -> "德国"
        "it" -> "意大利"
        "es" -> "西班牙"
        else -> lang.uppercase()
    }
}
