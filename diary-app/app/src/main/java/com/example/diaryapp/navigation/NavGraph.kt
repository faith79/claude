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
import com.example.diaryapp.ui.memo.MemoDetailScreen
import com.example.diaryapp.ui.memo.MemoEditorScreen
import com.example.diaryapp.ui.settings.SettingsScreen
import com.example.diaryapp.ui.tools.LadderGameScreen
import com.example.diaryapp.viewmodel.AuthViewModel
import com.example.diaryapp.viewmodel.DiaryViewModel
import com.example.diaryapp.viewmodel.MemoViewModel
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
            // Design Ref: diary-ux-fixes §SC-02 — Activity 스코프 VM 공유
            val activity = LocalContext.current as ComponentActivity
            val diaryViewModel: DiaryViewModel = hiltViewModel(activity)
            val memoViewModel: MemoViewModel = hiltViewModel(activity)
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
                },
                // Design Ref: tab-memo-fullscreen — 전체화면 에디터 네비게이션
                onAddMemo  = { navController.navigate(Screen.MemoEditor.createRoute()) },
                // Design Ref: memo-todo-detail §5 — 클릭 시 상세화면으로 이동
                onEditMemo = { id -> navController.navigate(Screen.MemoDetail.createRoute(id)) },
                // Design Ref: tools-tab-ladder-game — 도구모음 탭 사다리 게임 네비게이션
                onNavigateToLadder = { navController.navigate(Screen.LadderGame.route) },
                diaryViewModel = diaryViewModel,
                memoViewModel  = memoViewModel
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
                // Design Ref: diary-ux-fixes §SC-02 — 저장 후 항상 Home(달력)으로 복귀
                onSaved = { navController.popBackStack(Screen.Home.route, inclusive = false) },
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

        // Design Ref: tab-memo-fullscreen — 전체화면 메모 에디터
        composable(
            route = Screen.MemoEditor.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType; defaultValue = "" })
        ) { backStack ->
            val id = backStack.arguments?.getString("id") ?: ""
            val activity = LocalContext.current as ComponentActivity
            val memoViewModel: MemoViewModel = hiltViewModel(activity)
            MemoEditorScreen(
                memoId       = id,
                onBack       = { navController.popBackStack() },
                memoViewModel = memoViewModel
            )
        }

        // Design Ref: tools-tab-ladder-game — 사다리 게임 전체화면
        composable(Screen.LadderGame.route) {
            LadderGameScreen(onBack = { navController.popBackStack() })
        }

        // Design Ref: memo-todo-detail §5 — 메모/TODO 상세 화면
        composable(
            route = Screen.MemoDetail.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStack ->
            val id = backStack.arguments?.getString("id") ?: return@composable
            val activity = LocalContext.current as ComponentActivity
            val memoViewModel: MemoViewModel = hiltViewModel(activity)
            MemoDetailScreen(
                memoId = id,
                onBack = { navController.popBackStack() },
                onEdit = { editId -> navController.navigate(Screen.MemoEditor.createRoute(editId)) },
                onDelete = { navController.popBackStack() },
                memoViewModel = memoViewModel
            )
        }
    }
}
