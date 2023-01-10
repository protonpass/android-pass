package proton.android.pass.autofill.ui.autofill.select

import androidx.compose.runtime.Immutable
import proton.android.pass.common.api.Option
import proton.pass.domain.entity.PackageName

@Immutable
data class SelectItemInitialState(
    val packageName: PackageName,
    val webDomain: Option<String>
)
