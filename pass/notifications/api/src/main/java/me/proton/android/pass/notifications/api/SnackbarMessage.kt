package me.proton.android.pass.notifications.api

interface SnackbarMessage {
    val id: Int
    val type: SnackbarType
    val isClipboard: Boolean
}

enum class SnackbarType {
    SUCCESS, WARNING, ERROR, NORM
}

