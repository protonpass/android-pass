package proton.android.pass.featureitemdetail.impl.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.toImmutableSet
import proton.android.pass.commonui.api.PassColors
import proton.android.pass.featureitemdetail.impl.common.MoreInfo
import proton.android.pass.featureitemdetail.impl.common.NoteSection

@Composable
fun LoginContent(
    modifier: Modifier = Modifier,
    state: LoginDetailUiState,
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
        LoginTitle(title = state.title)
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
            accentColor = PassColors.PurpleAccent
        )
        LinkedAppsListSection(
            linkedAppsSet = state.packageNames.toImmutableSet(),
            isEditable = false,
            onLinkedAppDelete = {}
        )
        MoreInfo()
    }
}
