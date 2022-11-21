package me.proton.pass.presentation.create.alias

import androidx.compose.runtime.Stable

@Stable
sealed interface AliasBottomSheetType {
    object Suffix : AliasBottomSheetType
    object Mailbox : AliasBottomSheetType
}
