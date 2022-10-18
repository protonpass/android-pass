package me.proton.core.pass.presentation.create.login

sealed interface LoginSnackbarMessages {
    object EmptyShareIdError : LoginSnackbarMessages
    object CreationError : LoginSnackbarMessages
}
