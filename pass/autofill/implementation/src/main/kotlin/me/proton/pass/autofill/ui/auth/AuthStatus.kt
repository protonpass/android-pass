package me.proton.pass.autofill.ui.auth

sealed interface AuthStatus {
    object Pending : AuthStatus
    object Success : AuthStatus
    object Failed : AuthStatus
    object Canceled : AuthStatus
}
