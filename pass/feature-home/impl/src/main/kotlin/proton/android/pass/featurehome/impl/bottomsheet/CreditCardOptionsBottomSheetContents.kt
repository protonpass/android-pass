package proton.android.pass.featurehome.impl.bottomsheet

import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.toPersistentList
import kotlinx.datetime.Clock
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.domain.entity.UserId
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemAction
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemIcon
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemRow
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemSubtitle
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.composecomponents.impl.bottomsheet.pin
import proton.android.pass.composecomponents.impl.bottomsheet.unpin
import proton.android.pass.composecomponents.impl.bottomsheet.viewHistory
import proton.android.pass.composecomponents.impl.bottomsheet.withDividers
import proton.android.pass.composecomponents.impl.item.icon.CreditCardIcon
import proton.android.pass.domain.CreditCardType
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.featurehome.impl.R

@ExperimentalMaterialApi
@Composable
fun CreditCardOptionsBottomSheetContents(
    modifier: Modifier = Modifier,
    itemUiModel: ItemUiModel,
    isRecentSearch: Boolean = false,
    isFreePlan: Boolean,
    canUpdate: Boolean,
    onCopyNumber: (String) -> Unit,
    onCopyCvv: (EncryptedString) -> Unit,
    action: BottomSheetItemAction,
    onPinned: (ShareId, ItemId) -> Unit,
    onUnpinned: (ShareId, ItemId) -> Unit,
    onViewHistory: (ShareId, ItemId) -> Unit,
    onEdit: (ShareId, ItemId) -> Unit,
    onMoveToTrash: (ItemUiModel) -> Unit,
    onRemoveFromRecentSearch: (ShareId, ItemId) -> Unit
) {
    val contents = itemUiModel.contents as ItemContents.CreditCard

    Column(modifier.bottomSheet()) {
        BottomSheetItemRow(
            title = { BottomSheetItemTitle(text = contents.title) },
            subtitle = if (contents.cardHolder.isEmpty()) {
                null
            } else {
                { BottomSheetItemSubtitle(text = contents.cardHolder) }
            },
            leftIcon = { CreditCardIcon() }
        )

        val bottomSheetItems = mutableListOf(
            copyNumber { onCopyNumber(contents.number) },
            copyCvv { onCopyCvv(contents.cvv.encrypted) }
        ).apply {
            if (itemUiModel.isPinned) {
                add(unpin(action) { onUnpinned(itemUiModel.shareId, itemUiModel.id) })
            } else {
                add(pin(action) { onPinned(itemUiModel.shareId, itemUiModel.id) })
            }

            add(viewHistory(isFreePlan) { onViewHistory(itemUiModel.shareId, itemUiModel.id) })

            if (canUpdate) {
                add(edit(itemUiModel, onEdit))
                add(moveToTrash(itemUiModel, onMoveToTrash))
            }

            if (isRecentSearch) {
                add(removeFromRecentSearch(itemUiModel, onRemoveFromRecentSearch))
            }
        }

        BottomSheetItemList(
            items = bottomSheetItems.withDividers().toPersistentList()
        )
    }
}

@Composable
private fun copyNumber(onClick: () -> Unit) = copyItem(
    stringResource(id = R.string.bottomsheet_copy_number),
    onClick
)

@Composable
private fun copyCvv(onClick: () -> Unit) = copyItem(
    stringResource(id = R.string.bottomsheet_copy_cvv),
    onClick
)

private fun copyItem(text: String, onClick: () -> Unit): BottomSheetItem = object : BottomSheetItem {
    override val title: @Composable () -> Unit
        get() = { BottomSheetItemTitle(text = text) }
    override val subtitle: (@Composable () -> Unit)?
        get() = null
    override val leftIcon: (@Composable () -> Unit)
        get() = { BottomSheetItemIcon(iconId = R.drawable.ic_squares) }
    override val endIcon: (@Composable () -> Unit)?
        get() = null
    override val onClick = onClick
    override val isDivider = false
}


@Suppress("FunctionMaxLength")
@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun CreditCardOptionsBottomSheetContentsPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    PassTheme(isDark = input.first) {
        Surface {
            CreditCardOptionsBottomSheetContents(
                itemUiModel = ItemUiModel(
                    id = ItemId(id = ""),
                    userId = UserId(id = ""),
                    shareId = ShareId(id = ""),
                    contents = ItemContents.CreditCard(
                        title = "A credit card",
                        note = "Credit card note",
                        cardHolder = "Card holder",
                        type = CreditCardType.Visa,
                        number = "1234123412341234",
                        cvv = HiddenState.Concealed(""),
                        pin = HiddenState.Concealed(""),
                        expirationDate = "2030-01"

                    ),
                    state = 0,
                    createTime = Clock.System.now(),
                    modificationTime = Clock.System.now(),
                    lastAutofillTime = Clock.System.now(),
                    isPinned = false,
                    revision = 1,
                    shareCount = 0,
                    isOwner = true
                ),
                isRecentSearch = input.second,
                onCopyNumber = {},
                onCopyCvv = {},
                action = BottomSheetItemAction.None,
                onPinned = { _, _ -> },
                onUnpinned = { _, _ -> },
                onViewHistory = { _, _ -> },
                onEdit = { _, _ -> },
                onMoveToTrash = {},
                onRemoveFromRecentSearch = { _, _ -> },
                isFreePlan = input.second,
                canUpdate = true
            )
        }
    }
}
