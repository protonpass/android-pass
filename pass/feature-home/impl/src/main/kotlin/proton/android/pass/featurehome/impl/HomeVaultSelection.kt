package proton.android.pass.featurehome.impl

import androidx.compose.runtime.Stable
import proton.pass.domain.ShareId

@Stable
sealed class HomeVaultSelection {
    object AllVaults : HomeVaultSelection()
    data class Vault(val shareId: ShareId) : HomeVaultSelection()
}
