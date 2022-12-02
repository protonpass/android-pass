package me.proton.pass.presentation.home

import androidx.compose.runtime.Stable

@Stable
enum class HomeFilterMode {
    AllItems,
    Logins,
    Aliases,
    Notes;
}
