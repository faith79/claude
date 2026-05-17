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
import com.example.diaryapp.ui.theme.DiaryAppTheme
import com.example.diaryapp.ui.theme.LocalThemeColors
import com.example.diaryapp.ui.theme.ThemeColors
import com.example.diaryapp.viewmodel.AuthViewModel
import com.example.diaryapp.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Design Ref: joyary-upgrade-v3 §5.1 — LocalThemeColors 최상위 provide (FR-06)
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val calendarBg by settingsViewModel.calendarBgColor.collectAsStateWithLifecycle()
            val appBg      by settingsViewModel.appBgColor.collectAsStateWithLifecycle()
            val todayBg    by settingsViewModel.todayBgColor.collectAsStateWithLifecycle()

            DiaryAppTheme {
                CompositionLocalProvider(
                    LocalThemeColors provides ThemeColors(calendarBg, appBg, todayBg)
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
