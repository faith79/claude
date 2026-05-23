package com.example.diaryapp.navigation

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
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
import com.example.diaryapp.viewmodel.DiaryViewModel
import com.example.diaryapp.viewmodel.SettingsViewModel

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
                onEditDiary = { date, id ->
                    navController.navigate(Screen.DiaryEditor.createRoute(date, id))
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
            // Design Ref: joyary-upgrade-v10 — Activity 스코프 공유 ViewModel
            // Detail·Editor가 동일 인스턴스를 관찰해야 saveDiary force-refresh가 Detail에 전달됨
            val activity = LocalContext.current as ComponentActivity
            val diaryViewModel: DiaryViewModel = hiltViewModel(activity)
            DiaryDetailScreen(
                date = date,
                onEdit = { d, id ->
                    navController.navigate(Screen.DiaryEditor.createRoute(d, id))
                },
                onBack = { navController.popBackStack() },
                onDeleted = { navController.popBackStack() },
                // Design Ref: §5.3 — 빈 날 '일기 쓰기' 버튼 → DiaryEditor 이동 (FR-10)
                onAddDiary = { d ->
                    navController.navigate(Screen.DiaryEditor.createRoute(d))
                },
                diaryViewModel = diaryViewModel
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
            // Design Ref: joyary-upgrade-v10 — Activity 스코프 공유 ViewModel (DiaryDetail과 동일 인스턴스)
            val activity = LocalContext.current as ComponentActivity
            val diaryViewModel: DiaryViewModel = hiltViewModel(activity)
            DiaryEditorScreen(
                date = date,
                existingId = id,
                onSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
                diaryViewModel = diaryViewModel
            )
        }

        composable(Screen.Settings.route) {
            // Activity 스코프 ViewModel을 명시적으로 주입 — MainActivity와 동일한 인스턴스 공유
            val activity = LocalContext.current as ComponentActivity
            val settingsViewModel: SettingsViewModel = hiltViewModel(activity)
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                settingsViewModel = settingsViewModel
            )
        }
    }
}
