package proton.android.pass.featureitemdetail.impl.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.item.LinkedAppsListSection
import proton.android.pass.featureitemdetail.impl.common.MoreInfo
import proton.android.pass.featureitemdetail.impl.common.MoreInfoUiState
import proton.android.pass.featureitemdetail.impl.common.NoteSection

@Composable
fun LoginContent(
    modifier: Modifier = Modifier,
    state: LoginDetailUiState,
    moreInfoUiState: MoreInfoUiState,
    onTogglePasswordClick: () -> Unit,
    onUsernameClick: () -> Unit,
    onCopyPasswordClick: () -> Unit,
    onWebsiteClicked: (String) -> Unit,
    onWebsiteLongClicked: (String) -> Unit,
    onCopyTotpClick: (String) -> Unit
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LoginTitle(
            title = state.title,
            website = state.websites.firstOrNull(),
            packageName = state.packageInfoSet.firstOrNull()?.packageName
        )
        MainLoginSection(
            state = state,
            onUsernameClick = onUsernameClick,
            onTogglePasswordClick = onTogglePasswordClick,
            onCopyPasswordClick = onCopyPasswordClick,
            onCopyTotpClick = onCopyTotpClick
        )
        WebsiteSection(
            websites = state.websites,
            onWebsiteClicked = onWebsiteClicked,
            onWebsiteLongClicked = onWebsiteLongClicked
        )
        NoteSection(
            text = state.note,
            accentColor = PassTheme.colors.accentPurpleOpaque
        )
        LinkedAppsListSection(
            packageInfoUiSet = state.packageInfoSet,
            isEditable = false,
            onLinkedAppDelete = {}
        )
        MoreInfo(moreInfoUiState = moreInfoUiState)
    }
}
