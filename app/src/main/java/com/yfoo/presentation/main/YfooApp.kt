package com.yfoo.presentation.main

import androidx.annotation.StringRes
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.yfoo.R
import com.yfoo.presentation.YfooStatusBar
import com.yfoo.presentation.liked.LikedScreen
import com.yfoo.presentation.swipe.SwipeScreen
import com.yfoo.presentation.swipe.SwipeViewModel
import com.yfoo.presentation.theme.YfooTheme
import com.yfoo.presentation.utils.WindowSize
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

enum class MainScreen(val route: String, @StringRes val nameRes: Int, val icon: ImageVector) {
    Swipe(Screen.Swipe.route, R.string.swipe, Icons.Filled.Explore),
    Liked(Screen.Liked.route, R.string.liked, Icons.Filled.Favorite),
    Chat(Screen.Chat.route, R.string.chat, Icons.Filled.ChatBubble),
    Profile(Screen.Profile.route, R.string.profile, Icons.Filled.Person);
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun YfooApp(
    windowSize: WindowSize,
) {
    val mainScreens = remember { MainScreen.values() }

    val viewModel: MainViewModel = hiltViewModel()
    val navigationClickHandler: (MainScreen) -> Unit = remember {
        { screen ->
            when (screen) {
                MainScreen.Swipe -> viewModel.onAction(MainUiAction.SwipeClick)
                MainScreen.Liked -> viewModel.onAction(MainUiAction.LikedClick)
                MainScreen.Chat -> viewModel.onAction(MainUiAction.ChatClick)
                MainScreen.Profile -> viewModel.onAction(MainUiAction.ProfileClick)
            }
        }
    }

    val navController = rememberAnimatedNavController()

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: mainScreens.first().route

    val isCompactScreen = windowSize == WindowSize.Compact

    LaunchedEffect("key") { //TODO another name for key
        viewModel.oneShotEvents
            .onEach {
                when (it) {
                    MainOneShotEvent.NavigateToSwipe -> navController.navigateToSwipe()
                    MainOneShotEvent.NavigateToLiked -> navController.navigateToLiked()
                    MainOneShotEvent.NavigateToChat -> navController.navigateToChat()
                    MainOneShotEvent.NavigateToProfile -> navController.navigateToProfile()
                }
            }
            .collect()
    }

    YfooTheme {
        YfooStatusBar()
        Scaffold(
            bottomBar = {
                if (isCompactScreen) {
                    AppBottomNavigation(
                        screens = mainScreens,
                        currentRoute = currentRoute,
                        onRouteClick = navigationClickHandler,
                    )
                }
            },
        ) {
            Row(Modifier.fillMaxSize()) {
                if (!isCompactScreen) {
                    AppNavigationRail(
                        screens = mainScreens,
                        currentRoute = currentRoute,
                        onRouteClick = navigationClickHandler,
                    )
                }
                YfooNavGraph(
                    windowSize = windowSize,
                    navController = navController,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
            }
        }
    }
}

@Composable
private fun AppBottomNavigation(
    screens: Array<MainScreen>,
    currentRoute: String,
    onRouteClick: (MainScreen) -> Unit,
    modifier: Modifier = Modifier,
) {
    BottomNavigation(modifier) {
        screens.forEach { screen ->
            BottomNavigationItem(
                selected = screen.route == currentRoute,
                onClick = { onRouteClick(screen) },
                icon = { Icon(imageVector = screen.icon, contentDescription = null) },
                label = { Text(stringResource(screen.nameRes)) },
            )
        }
    }
}

@Composable
private fun AppNavigationRail(
    screens: Array<MainScreen>,
    currentRoute: String,
    onRouteClick: (MainScreen) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationRail(modifier) {
        screens.forEach { screen ->
            NavigationRailItem(
                selected = screen.route == currentRoute,
                onClick = { onRouteClick(screen) },
                icon = { Icon(imageVector = screen.icon, contentDescription = null) },
                label = { Text(stringResource(screen.nameRes)) },
            )
        }
    }
}

