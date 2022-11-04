package me.proton.android.pass.notifications.api

interface SnackbarMessage {
    val id: Int
    val type: SnackbarType
}

enum class SnackbarType {
    SUCCESS, WARNING, ERROR, NORM
}

