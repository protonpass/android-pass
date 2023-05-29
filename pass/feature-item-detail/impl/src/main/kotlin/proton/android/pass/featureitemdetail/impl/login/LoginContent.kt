package proton.android.pass.featureitemdetail.impl.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentSet
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.commonuimodels.api.PackageInfoUi
import proton.android.pass.composecomponents.impl.item.LinkedAppsListSection
import proton.android.pass.featureitemdetail.impl.common.MoreInfo
import proton.android.pass.featureitemdetail.impl.common.MoreInfoUiState
import proton.android.pass.featureitemdetail.impl.common.NoteSection
import proton.pass.domain.ItemContents
import proton.pass.domain.Vault

@Composable
fun LoginContent(
    modifier: Modifier = Modifier,
    itemUiModel: ItemUiModel,
    vault: Vault?,
    passwordState: PasswordState,
    totpUiState: TotpUiState?,
    moreInfoUiState: MoreInfoUiState,
    showViewAlias: Boolean,
    canLoadExternalImages: Boolean,
    onTogglePasswordClick: () -> Unit,
    onUsernameClick: () -> Unit,
    onGoToAliasClick: () -> Unit,
    onCopyPasswordClick: () -> Unit,
    onWebsiteClicked: (String) -> Unit,
    onWebsiteLongClicked: (String) -> Unit,
    onCopyTotpClick: (String) -> Unit,
    onUpgradeClick: () -> Unit
) {
    val contents = itemUiModel.contents as ItemContents.Login
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LoginTitle(
            modifier = Modifier.padding(0.dp, 12.dp),
            title = itemUiModel.contents.title,
            vault = vault,
            website = contents.urls.firstOrNull(),
            packageName = contents.packageInfoSet.minByOrNull { it.packageName.value }?.packageName?.value,
            canLoadExternalImages = canLoadExternalImages
        )
        MainLoginSection(
            username = contents.username,
            passwordState = passwordState,
            totpUiState = totpUiState,
            showViewAlias = showViewAlias,
            onUsernameClick = onUsernameClick,
            onGoToAliasClick = onGoToAliasClick,
            onTogglePasswordClick = onTogglePasswordClick,
            onCopyPasswordClick = onCopyPasswordClick,
            onCopyTotpClick = onCopyTotpClick,
            onUpgradeClick = onUpgradeClick
        )
        WebsiteSection(
            websites = contents.urls.toPersistentList(),
            onWebsiteClicked = onWebsiteClicked,
            onWebsiteLongClicked = onWebsiteLongClicked
        )
        NoteSection(
            text = itemUiModel.contents.note,
            accentColor = PassTheme.colors.loginInteractionNorm
        )
        LinkedAppsListSection(
            packageInfoUiSet = contents.packageInfoSet.map { PackageInfoUi(it) }.toPersistentSet(),
            isEditable = false,
            onLinkedAppDelete = {}
        )
        MoreInfo(moreInfoUiState = moreInfoUiState)
    }
}
