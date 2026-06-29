package com.example.moviehubpro.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.moviehubpro.model.Movie
import com.example.moviehubpro.ui.screen.*
import com.example.moviehubpro.ui.screen.viewmodel.*

object ScreenRoute {
    const val HOME = "home"
    const val DETAIL = "detail"
    const val FAVORITE = "favorite"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    homeVm: HomeViewModel,
    detailVm: DetailViewModel,
    favoriteVm: FavoriteViewModel,
    settingsVm: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = ScreenRoute.HOME,
        modifier = modifier,
        enterTransition = { fadeIn(animationSpec = tween(500)) + slideInHorizontally() },
        exitTransition = { fadeOut(animationSpec = tween(500)) + slideOutHorizontally() }
    ) {
        composable(ScreenRoute.HOME) {
            HomeScreen(
                vm = homeVm,
                isDarkMode = settingsVm.isDarkMode.collectAsState().value,
                onToggleDark = { settingsVm.toggleDarkMode(it) },
                onMovieClick = { movie ->
                    navController.currentBackStackEntry?.savedStateHandle?.set("movie", movie)
                    navController.navigate(ScreenRoute.DETAIL)
                }
            )
        }

        composable(ScreenRoute.DETAIL) {
            val movie = navController.previousBackStackEntry?.savedStateHandle?.get<Movie>("movie")
            movie?.let {
                DetailScreen(
                    movie = it,
                    vm = detailVm,
                    onBack = { navController.popBackStack() },
                    onMovieClick = { nextMovie ->
                        navController.currentBackStackEntry?.savedStateHandle?.set("movie", nextMovie)
                        navController.navigate(ScreenRoute.DETAIL)
                    }
                )
            }
        }

        composable(ScreenRoute.FAVORITE) {
            FavoriteScreen(
                vm = favoriteVm,
                onBack = { navController.popBackStack() },
                onMovieClick = { movie ->
                    navController.currentBackStackEntry?.savedStateHandle?.set("movie", movie)
                    navController.navigate(ScreenRoute.DETAIL)
                }
            )
        }
    }
}
