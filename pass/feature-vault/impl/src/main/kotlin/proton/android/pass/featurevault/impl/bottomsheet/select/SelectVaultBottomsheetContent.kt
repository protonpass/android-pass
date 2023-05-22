package proton.android.pass.featurevault.impl.bottomsheet.select

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetTitle
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetVaultRow
import proton.android.pass.composecomponents.impl.bottomsheet.withDividers
import proton.android.pass.feature.vault.impl.R
import proton.pass.domain.ShareColor
import proton.pass.domain.ShareIcon
import proton.pass.domain.ShareId
import proton.pass.domain.Vault
import proton.pass.domain.VaultWithItemCount

@Composable
fun SelectVaultBottomsheetContent(
    modifier: Modifier = Modifier,
    shareList: ImmutableList<VaultWithItemCount>,
    selectedShareId: ShareId,
    onVaultClick: (ShareId) -> Unit
) {
    Column(
        modifier = modifier.bottomSheet(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BottomSheetTitle(title = stringResource(R.string.vault_title))
        BottomSheetItemList(
            items = shareList
                .map {
                    BottomSheetVaultRow(
                        vault = it,
                        isSelected = it.vault.shareId == selectedShareId,
                        onVaultClick = onVaultClick
                    )
                }
                .withDividers()
                .toPersistentList()
        )
    }
}

@Preview
@Composable
fun SelectVaultBottomsheetContentPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    val shareId = "123"
    PassTheme(isDark = isDark) {
        Surface {
            SelectVaultBottomsheetContent(
                shareList = persistentListOf(
                    VaultWithItemCount(
                        vault = Vault(
                            shareId = ShareId(shareId),
                            name = "vault 1",
                            isPrimary = false
                        ),
                        activeItemCount = 12,
                        trashedItemCount = 0,
                    ),
                    VaultWithItemCount(
                        vault = Vault(
                            shareId = ShareId("other"),
                            name = "vault 2",
                            color = ShareColor.Color2,
                            icon = ShareIcon.Icon2,
                            isPrimary = false
                        ),
                        activeItemCount = 1,
                        trashedItemCount = 0,
                    )
                ),
                selectedShareId = ShareId(shareId),
                onVaultClick = {}
            )
        }
    }
}

