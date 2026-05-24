package com.example.diaryapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.diaryapp.navigation.NavGraph
import com.example.diaryapp.navigation.Screen
import com.example.diaryapp.ui.theme.AppThemeTemplates
import com.example.diaryapp.ui.theme.DiaryAppTheme
import com.example.diaryapp.ui.theme.LocalThemeColors
import com.example.diaryapp.viewmodel.AuthViewModel
import com.example.diaryapp.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Design Ref: joyary-upgrade-v4 §3.2 — templateIndex로 colorScheme + themeColors 동적 주입 (FR-03,FR-04)
            // Design Ref: joyary-upgrade-v5 §2.1 — diaryBg, weekdayColor override via copy() (KD-02)
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val templateIndex by settingsViewModel.selectedTemplateIndex.collectAsStateWithLifecycle()
            val template = AppThemeTemplates.getOrElse(templateIndex) { AppThemeTemplates[0] }
            val weekday by settingsViewModel.weekdayColor.collectAsStateWithLifecycle()
            // Design Ref: joyary-ux-improvements §FR-04 — 에디터 배경색 사용자 지정값 사용
            val diaryBg by settingsViewModel.diaryBgColor.collectAsStateWithLifecycle()

            DiaryAppTheme(colorScheme = template.colorScheme) {
                CompositionLocalProvider(
                    LocalThemeColors provides template.themeColors.copy(
                        diaryBg = diaryBg,
                        weekdayColor = weekday
                    )
                ) {
                    val navController = rememberNavController()
                    val authViewModel: AuthViewModel = hiltViewModel()
                    val start = remember {
                        if (authViewModel.isLoggedIn) Screen.Home.route else Screen.Login.route
                    }
                    NavGraph(navController = navController, startDestination = start)
                }
            }
        }
    }
}
