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
    object MemoEditor : Screen("memo_editor?id={id}") {
        fun createRoute(id: String = "") = "memo_editor?id=$id"
    }
    // Design Ref: memo-todo-detail §1 — 상세보기 전용 화면
    object MemoDetail : Screen("memo_detail/{id}") {
        fun createRoute(id: String) = "memo_detail/$id"
    }
}
