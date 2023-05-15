package proton.android.pass.featureitemcreate.impl.bottomsheets.createitem

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class CreateItemBottomSheetUIStatePreviewProvider :
    PreviewParameterProvider<CreateItemBottomSheetUIState> {
    override val values: Sequence<CreateItemBottomSheetUIState>
        get() = sequenceOf(
            CreateItemBottomSheetUIState(
                shareId = null,
                createItemAliasUIState = CreateItemAliasUIState(
                    canUpgrade = true,
                    aliasCount = 5,
                    aliasLimit = 10
                )
            ),
            CreateItemBottomSheetUIState(
                shareId = null,
                createItemAliasUIState = CreateItemAliasUIState(
                    canUpgrade = true,
                    aliasCount = 10,
                    aliasLimit = 10
                )
            )
        )
}
