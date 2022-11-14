package me.proton.pass.presentation.create.login

import me.proton.pass.domain.entity.PackageName

data class InitialCreateLoginUiState(
    val title: String? = null,
    val username: String? = null,
    val password: String? = null,
    val url: String? = null,
    val packageName: PackageName? = null
)
