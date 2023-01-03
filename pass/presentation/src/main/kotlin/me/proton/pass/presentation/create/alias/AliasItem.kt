package me.proton.pass.presentation.create.alias

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize
import me.proton.pass.presentation.create.alias.AliasUtils.areAllAliasCharactersValid

@Parcelize
@Immutable
data class AliasItem(
    val title: String = "",
    val alias: String = "",
    val note: String = "",
    val mailboxTitle: String = "",
    val aliasOptions: AliasOptionsUiModel = AliasOptionsUiModel(emptyList(), emptyList()),
    val selectedSuffix: AliasSuffixUiModel? = null,
    val mailboxes: List<SelectedAliasMailboxUiModel> = emptyList(),
    val aliasToBeCreated: String? = null
) : Parcelable {

    fun validate(): Set<AliasItemValidationErrors> {
        val mutableSet = mutableSetOf<AliasItemValidationErrors>()
        if (title.isBlank()) mutableSet.add(AliasItemValidationErrors.BlankTitle)

        if (alias.isBlank()) mutableSet.add(AliasItemValidationErrors.BlankAlias)

        if (alias.startsWith(".")) mutableSet.add(AliasItemValidationErrors.InvalidAliasContent)

        if (alias.endsWith(".")) mutableSet.add(AliasItemValidationErrors.InvalidAliasContent)

        if (alias.contains("..")) mutableSet.add(AliasItemValidationErrors.InvalidAliasContent)

        if (!areAllAliasCharactersValid(alias)) mutableSet.add(AliasItemValidationErrors.InvalidAliasContent)

        if (mailboxes.count { it.selected } == 0) mutableSet.add(AliasItemValidationErrors.NoMailboxes)

        return mutableSet.toSet()
    }

    companion object {
        val Empty = AliasItem()
    }
}

sealed interface AliasItemValidationErrors {
    object BlankTitle : AliasItemValidationErrors
    object BlankAlias : AliasItemValidationErrors
    object InvalidAliasContent : AliasItemValidationErrors
    object NoMailboxes : AliasItemValidationErrors
}
