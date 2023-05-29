package proton.android.pass.autofill.ui.previewproviders

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.datetime.Clock
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.pass.domain.HiddenState
import proton.pass.domain.ItemContents
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

class SuggestionsPreviewProvider : PreviewParameterProvider<SuggestionsInput> {
    override val values: Sequence<SuggestionsInput>
        get() = sequence {
            for (showUpgrade in listOf(true, false)) {
                yield(
                    SuggestionsInput(
                        items = listOf(
                            item("item 1", "some username"),
                            item("item 2", "other username")
                        ),
                        showUpgradeMessage = showUpgrade,
                        canUpgrade = false
                    )
                )
            }
            yield(
                SuggestionsInput(
                    items = listOf(
                        item("item 1", "some username"),
                        item("item 2", "other username")
                    ),
                    showUpgradeMessage = true,
                    canUpgrade = true
                )
            )
        }


    private fun item(name: String, username: String): ItemUiModel =
        ItemUiModel(
            id = ItemId(name),
            shareId = ShareId(name),
            contents = ItemContents.Login(
                title = name,
                note = "",
                username = username,
                password = HiddenState.Concealed(""),
                urls = emptyList(),
                packageInfoSet = emptySet(),
                primaryTotp = HiddenState.Concealed(""),
                customFields = emptyList()
            ),
            state = 0,
            createTime = Clock.System.now(),
            modificationTime = Clock.System.now(),
            lastAutofillTime = Clock.System.now(),
        )
}

data class SuggestionsInput(
    val items: List<ItemUiModel>,
    val showUpgradeMessage: Boolean,
    val canUpgrade: Boolean
)
