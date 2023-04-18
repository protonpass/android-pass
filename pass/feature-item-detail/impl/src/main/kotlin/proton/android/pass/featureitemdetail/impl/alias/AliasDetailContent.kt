package proton.android.pass.featureitemdetail.impl.alias

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.PersistentList
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.featureitemdetail.impl.common.MoreInfo
import proton.android.pass.featureitemdetail.impl.common.MoreInfoUiState
import proton.android.pass.featureitemdetail.impl.common.NoteSection
import proton.pass.domain.AliasMailbox
import proton.pass.domain.ItemType

@Composable
fun AliasDetailContent(
    modifier: Modifier = Modifier,
    itemUiModel: ItemUiModel,
    moreInfoUiState: MoreInfoUiState,
    mailboxes: PersistentList<AliasMailbox>,
    isLoading: Boolean,
    onCopyAlias: (String) -> Unit,
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AliasTitle(
            modifier = Modifier.padding(0.dp, 12.dp),
            title = itemUiModel.name
        )
        AliasSection(
            alias = (itemUiModel.itemType as ItemType.Alias).aliasEmail,
            mailboxes = mailboxes,
            isLoading = isLoading,
            onCopyAlias = onCopyAlias
        )
        NoteSection(
            text = itemUiModel.note,
            accentColor = PassTheme.colors.aliasInteractionNorm
        )
        MoreInfo(moreInfoUiState = moreInfoUiState)
    }
}

