package me.proton.pass.autofill.ui.autofill.select

import androidx.compose.runtime.Immutable
import me.proton.pass.common.api.Option
import me.proton.pass.domain.entity.PackageName

@Immutable
data class SelectItemInitialState(
    val packageName: PackageName,
    val webDomain: Option<String>
)
