package com.example.diaryapp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.diaryapp.data.repository.AuthRepository
import com.example.diaryapp.navigation.NavGraph
import com.example.diaryapp.navigation.Screen
import com.example.diaryapp.ui.theme.AppThemeTemplates
import com.example.diaryapp.ui.theme.DiaryAppTheme
import com.example.diaryapp.ui.theme.LocalThemeColors
import com.example.diaryapp.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    // Design Ref: joyary-login-biometric §1.2 — onCreate 에서 한 번만 signOut (recompose 영향 없음)
    @Inject lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Design Ref: joyary-login-biometric §1.1 — 앱 실행 시 Firebase 세션 항상 종료 (FR-01)
        authRepository.signOutImmediate()
        enableEdgeToEdge()
        setContent {
            // Design Ref: joyary-upgrade-v4 §3.2 — templateIndex로 colorScheme + themeColors 동적 주입 (FR-03,FR-04)
            // Design Ref: joyary-upgrade-v5 §2.1 — diaryBg, weekdayColor override via copy() (KD-02)
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val templateIndex by settingsViewModel.selectedTemplateIndex.collectAsStateWithLifecycle()
            val template = AppThemeTemplates.getOrElse(templateIndex) { AppThemeTemplates[0] }
            val weekday by settingsViewModel.weekdayColor.collectAsStateWithLifecycle()
            // Design Ref: diary-editor-bg-setting §FR-04 — 글쓰기 배경색 독립 설정값 사용
            val diaryBg by settingsViewModel.diaryBgColor.collectAsStateWithLifecycle()

            DiaryAppTheme(colorScheme = template.colorScheme) {
                CompositionLocalProvider(
                    LocalThemeColors provides template.themeColors.copy(
                        diaryBg = diaryBg,
                        weekdayColor = weekday
                    )
                ) {
                    val navController = rememberNavController()
                    NavGraph(navController = navController, startDestination = Screen.Login.route)
                }
            }
        }
    }
}
