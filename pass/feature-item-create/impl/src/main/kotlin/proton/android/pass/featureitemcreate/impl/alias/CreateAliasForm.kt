package proton.android.pass.featureitemcreate.impl.alias

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.container.InfoBanner
import proton.android.pass.composecomponents.impl.form.SimpleNoteSection
import proton.android.pass.composecomponents.impl.form.TitleVaultSelectionSection
import proton.android.pass.featureitemcreate.impl.R
import proton.pass.domain.VaultWithItemCount

@Composable
internal fun CreateAliasForm(
    modifier: Modifier = Modifier,
    aliasItem: AliasItem,
    selectedVault: VaultWithItemCount?,
    isCreateMode: Boolean,
    onTitleRequiredError: Boolean,
    onAliasRequiredError: Boolean,
    onInvalidAliasError: Boolean,
    isEditAllowed: Boolean,
    isLoading: Boolean,
    showVaultSelector: Boolean,
    showUpgrade: Boolean,
    onTitleChange: (String) -> Unit,
    onPrefixChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onSuffixClick: () -> Unit,
    onMailboxClick: () -> Unit,
    onVaultSelectorClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AnimatedVisibility(visible = showUpgrade) {
            InfoBanner(
                backgroundColor = PassTheme.colors.aliasInteractionNormMinor1,
                text = stringResource(R.string.create_alias_content_limit_banner)
            )
        }
        TitleVaultSelectionSection(
            titleValue = aliasItem.title,
            onTitleChanged = onTitleChange,
            onTitleRequiredError = onTitleRequiredError,
            enabled = isEditAllowed,
            showVaultSelector = showVaultSelector,
            vaultName = selectedVault?.vault?.name,
            vaultIcon = selectedVault?.vault?.icon,
            vaultColor = selectedVault?.vault?.color,
            onVaultClicked = onVaultSelectorClick
        )
        if (isCreateMode) {
            CreateAliasSection(
                state = aliasItem,
                onChange = onPrefixChange,
                onSuffixClick = onSuffixClick,
                canEdit = isEditAllowed,
                canSelectSuffix = aliasItem.aliasOptions.suffixes.size > 1,
                onAliasRequiredError = onAliasRequiredError,
                onInvalidAliasError = onInvalidAliasError
            )
        } else {
            DisplayAliasSection(
                state = aliasItem,
                isLoading = isLoading
            )
        }
        MailboxSection(
            isBottomSheet = false,
            mailboxes = aliasItem.mailboxes,
            isCreateMode = isCreateMode,
            isEditAllowed = isEditAllowed && aliasItem.mailboxes.size > 1,
            isLoading = isLoading,
            onMailboxClick = onMailboxClick
        )
        SimpleNoteSection(
            value = aliasItem.note,
            enabled = isEditAllowed,
            onChange = onNoteChange
        )
    }
}


