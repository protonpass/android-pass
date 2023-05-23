package proton.android.pass.featurevault.impl.bottomsheet.select

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetTitle
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetVaultRow
import proton.android.pass.composecomponents.impl.bottomsheet.withDividers
import proton.android.pass.composecomponents.impl.container.InfoBanner
import proton.android.pass.feature.vault.impl.R
import proton.pass.domain.ShareColor
import proton.pass.domain.ShareIcon
import proton.pass.domain.ShareId
import proton.pass.domain.Vault
import proton.pass.domain.VaultWithItemCount
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
fun SelectVaultBottomsheetContent(
    modifier: Modifier = Modifier,
    state: SelectVaultUiState.Success,
    onVaultClick: (ShareId) -> Unit,
    onUpgrade: () -> Unit
) {
    Column(
        modifier = modifier.bottomSheet(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (state.canUpgrade) {
            InfoBanner(
                modifier = Modifier.padding(16.dp, 0.dp),
                backgroundColor = PassTheme.colors.interactionNormMinor1,
                text = buildAnnotatedString {
                    append(stringResource(R.string.bottomsheet_cannot_select_not_primary_vault))
                    append(' ')
                    withStyle(
                        style = SpanStyle(
                            textDecoration = TextDecoration.Underline,
                            color = PassTheme.colors.loginInteractionNormMajor2
                        )
                    ) {
                        append(stringResource(CompR.string.action_upgrade_now))
                    }
                },
                onClick = onUpgrade
            )
        } else {
            BottomSheetTitle(title = stringResource(R.string.vault_title))
        }

        BottomSheetItemList(
            items = state.vaults
                .map {
                    val isSelected = it.vault.shareId == state.selected.vault.shareId
                    val enabled = if (state.canUpgrade) {
                        isSelected
                    } else {
                        true
                    }
                    BottomSheetVaultRow(
                        vault = it,
                        isSelected = isSelected,
                        enabled = enabled,
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
    val selectedVault = VaultWithItemCount(
        vault = Vault(
            shareId = ShareId("123"),
            name = "vault 1",
            isPrimary = false
        ),
        activeItemCount = 12,
        trashedItemCount = 0,
    )
    PassTheme(isDark = isDark) {
        Surface {
            SelectVaultBottomsheetContent(
                state = SelectVaultUiState.Success(
                    vaults = persistentListOf(
                        selectedVault,
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
                    selected = selectedVault,
                    canUpgrade = false
                ),
                onVaultClick = {},
                onUpgrade = {}
            )
        }
    }
}

