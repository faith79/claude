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
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val templateIndex by settingsViewModel.selectedTemplateIndex.collectAsStateWithLifecycle()
            val template = AppThemeTemplates.getOrElse(templateIndex) { AppThemeTemplates[0] }

            DiaryAppTheme(colorScheme = template.colorScheme) {
                CompositionLocalProvider(
                    LocalThemeColors provides template.themeColors
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
