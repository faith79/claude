package com.example.diaryapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.example.diaryapp.navigation.NavGraph
import com.example.diaryapp.navigation.Screen
import com.example.diaryapp.ui.theme.DiaryAppTheme
import com.example.diaryapp.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DiaryAppTheme {
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
