package me.proton.android.pass.featureauth.impl

sealed interface AuthStatus {
    object Pending : AuthStatus
    object Success : AuthStatus
    object Failed : AuthStatus
    object Canceled : AuthStatus
}
