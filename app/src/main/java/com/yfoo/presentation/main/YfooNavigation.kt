package com.yfoo.presentation.main

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.yfoo.presentation.chat.ChatScreen
import com.yfoo.presentation.liked.LikedScreen
import com.yfoo.presentation.profile.ProfileScreen
import com.yfoo.presentation.swipe.SwipeScreen
import com.yfoo.presentation.swipe.SwipeViewModel
import com.yfoo.presentation.utils.WindowSize

//TODO do we need it to be sealed class? Why not enum?
sealed class Screen(val route: String) {
    object Swipe : Screen("swipe")
    object Liked : Screen("liked")
    object Chat : Screen("chat")
    object Profile : Screen("profile")
    object Billing : Screen("billing")
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun YfooNavGraph(
    windowSize: WindowSize,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberAnimatedNavController(),
    startDestination: String = MainScreen.Swipe.route
) {
    AnimatedNavHost(navController, startDestination, modifier) {
        composable(MainScreen.Swipe.route) {
            val viewModel: SwipeViewModel = hiltViewModel()
            SwipeScreen(viewModel, windowSize)
        }
        composable(MainScreen.Liked.route) {
            LikedScreen()
        }
        composable(MainScreen.Chat.route) {
            ChatScreen()
        }
        composable(MainScreen.Profile.route) {
            ProfileScreen()
        }
    }
}

//TODO JetNews sample executes more actions in similar cases. Check if they are of any use
fun NavHostController.navigateToSwipe() = navigateToMainScreen(Screen.Swipe)
fun NavHostController.navigateToLiked() = navigateToMainScreen(Screen.Liked)
fun NavHostController.navigateToChat() = navigateToMainScreen(Screen.Chat)
fun NavHostController.navigateToProfile() = navigateToMainScreen(Screen.Profile)

private fun NavHostController.navigateToMainScreen(screen: Screen) {
    navigate(screen.route) {
        launchSingleTop = true
    }
}
