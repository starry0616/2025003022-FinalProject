package com.example.moviehubpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.moviehubpro.navigation.AppNavGraph
import com.example.moviehubpro.navigation.ScreenRoute
import com.example.moviehubpro.ui.screen.viewmodel.*
import ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val app = application as MovieApplication
            val container = app.container

            val settingsVm: SettingsViewModel = viewModel { SettingsViewModel(container.appDataStore) }
            val homeVm: HomeViewModel = viewModel { HomeViewModel(container.movieRepo, container.appDataStore, container.networkMonitor) }
            val detailVm: DetailViewModel = viewModel { DetailViewModel(container.movieRepo) }
            val favoriteVm: FavoriteViewModel = viewModel { FavoriteViewModel(container.movieRepo) }

            val isDark by settingsVm.isDarkMode.collectAsState()

            AppTheme(darkTheme = isDark) {
                MainContent(homeVm, detailVm, favoriteVm, settingsVm)
            }
        }
    }
}

@Composable
fun MainContent(
    homeVm: HomeViewModel,
    detailVm: DetailViewModel,
    favoriteVm: FavoriteViewModel,
    settingsVm: SettingsViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val items = listOf(
        BottomNavItem("首页", ScreenRoute.HOME, Icons.Default.Home),
        BottomNavItem("收藏", ScreenRoute.FAVORITE, Icons.Default.Favorite)
    )

    // 只在首页和收藏页显示底栏，详情页不显示
    val showBottomBar = currentDestination?.route in listOf(ScreenRoute.HOME, ScreenRoute.FAVORITE)

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    items.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = androidx.compose.ui.graphics.Color.Gray,
                                unselectedTextColor = androidx.compose.ui.graphics.Color.Gray,
                                indicatorColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        AppNavGraph(
            navController = navController,
            homeVm = homeVm,
            detailVm = detailVm,
            favoriteVm = favoriteVm,
            settingsVm = settingsVm,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

data class BottomNavItem(val label: String, val route: String, val icon: ImageVector)
