package proton.android.pass.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import me.proton.core.user.domain.entity.User
import proton.android.pass.commonuimodels.api.ShareUiModelWithItemCount
import proton.android.pass.data.api.ItemCountSummary
import proton.pass.domain.ShareId

sealed class ItemTypeSection {
    abstract val shareId: ShareId?
}
sealed interface NavigationDrawerSection {
    data class AllItems(override val shareId: ShareId? = null) :
        ItemTypeSection(), NavigationDrawerSection

    data class Logins(override val shareId: ShareId? = null) :
        ItemTypeSection(), NavigationDrawerSection

    data class Aliases(override val shareId: ShareId? = null) :
        ItemTypeSection(), NavigationDrawerSection

    data class Notes(override val shareId: ShareId? = null) :
        ItemTypeSection(), NavigationDrawerSection

    object Settings : NavigationDrawerSection
    object Trash : NavigationDrawerSection
}

@Immutable
data class DrawerUiState(
    @StringRes val appNameResId: Int,
    val closeOnBackEnabled: Boolean = true,
    val closeOnActionEnabled: Boolean = true,
    val currentUser: User? = null,
    val selectedSection: NavigationDrawerSection? = null,
    val itemCountSummary: ItemCountSummary = ItemCountSummary.Initial,
    val shares: List<ShareUiModelWithItemCount> = emptyList(),
    val trashedItemCount: Long = 0
)
