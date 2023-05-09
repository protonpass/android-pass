package proton.android.pass.featureitemcreate.impl.alias.mailboxes

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import proton.android.pass.featureitemcreate.impl.alias.AliasMailboxUiModel
import proton.android.pass.featureitemcreate.impl.alias.SelectedAliasMailboxUiModel

class SelectMailboxesUiStatePreviewProvider : PreviewParameterProvider<SelectMailboxesUiState> {
    override val values: Sequence<SelectMailboxesUiState>
        get() = sequenceOf(
            SelectMailboxesUiState(
                mailboxes = listOf(
                    SelectedAliasMailboxUiModel(
                        selected = false,
                        model = AliasMailboxUiModel(
                            id = 1,
                            email = "eric.norbert@proton.me"
                        )
                    ),
                    SelectedAliasMailboxUiModel(
                        selected = false,
                        model = AliasMailboxUiModel(
                            id = 2,
                            email = "eric.work@proton.me"
                        )
                    )
                ),
                canApply = IsButtonEnabled.from(false),
                showUpgrade = false
            ),
            SelectMailboxesUiState(
                mailboxes = listOf(
                    SelectedAliasMailboxUiModel(
                        selected = true,
                        model = AliasMailboxUiModel(
                            id = 1,
                            email = "eric.norbert@proton.me"
                        )
                    ),
                    SelectedAliasMailboxUiModel(
                        selected = true,
                        model = AliasMailboxUiModel(
                            id = 2,
                            email = "eric.work@proton.me"
                        )
                    )
                ),
                canApply = IsButtonEnabled.from(true),
                showUpgrade = false
            ),
            SelectMailboxesUiState(
                mailboxes = listOf(
                    SelectedAliasMailboxUiModel(
                        selected = false,
                        model = AliasMailboxUiModel(
                            id = 1,
                            email = "eric.norbert@proton.me"
                        )
                    ),
                    SelectedAliasMailboxUiModel(
                        selected = false,
                        model = AliasMailboxUiModel(
                            id = 2,
                            email = "eric.work@proton.me"
                        )
                    )
                ),
                canApply = IsButtonEnabled.from(false),
                showUpgrade = true
            )
        )
}
