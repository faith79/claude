package com.example.diaryapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.diaryapp.ui.auth.LoginScreen
import com.example.diaryapp.ui.auth.SignUpScreen
import com.example.diaryapp.ui.diary.DiaryDetailScreen
import com.example.diaryapp.ui.diary.DiaryEditorScreen
import com.example.diaryapp.ui.home.HomeScreen
import com.example.diaryapp.ui.settings.SettingsScreen
import com.example.diaryapp.viewmodel.AuthViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(navController = navController, startDestination = startDestination) {

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) }
            )
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onDateSelected = { date ->
                    navController.navigate(Screen.DiaryDetail.createRoute(date))
                },
                onAddDiary = { date ->
                    navController.navigate(Screen.DiaryEditor.createRoute(date))
                },
                onSettings = { navController.navigate(Screen.Settings.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.DiaryDetail.route,
            arguments = listOf(navArgument("date") { type = NavType.StringType })
        ) { backStack ->
            val date = backStack.arguments?.getString("date") ?: return@composable
            DiaryDetailScreen(
                date = date,
                onEdit = { d, id ->
                    navController.navigate(Screen.DiaryEditor.createRoute(d, id))
                },
                onBack = { navController.popBackStack() },
                onDeleted = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.DiaryEditor.route,
            arguments = listOf(
                navArgument("date") { type = NavType.StringType },
                navArgument("id") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStack ->
            val date = backStack.arguments?.getString("date") ?: return@composable
            val id = backStack.arguments?.getString("id") ?: ""
            DiaryEditorScreen(
                date = date,
                existingId = id,
                onSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
