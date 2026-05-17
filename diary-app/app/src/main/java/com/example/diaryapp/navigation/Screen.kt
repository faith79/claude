package com.example.diaryapp.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object Home : Screen("home")
    object DiaryDetail : Screen("diary_detail/{date}") {
        fun createRoute(date: String) = "diary_detail/$date"
    }
    object DiaryEditor : Screen("diary_editor?date={date}&id={id}") {
        fun createRoute(date: String, id: String = "") = "diary_editor?date=$date&id=$id"
    }
    object Settings : Screen("settings")
}
