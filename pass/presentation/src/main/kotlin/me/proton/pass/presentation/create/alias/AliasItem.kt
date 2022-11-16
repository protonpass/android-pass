package me.proton.pass.presentation.create.alias

import androidx.compose.runtime.Immutable
import me.proton.pass.domain.AliasOptions
import me.proton.pass.domain.AliasSuffix

@Immutable
data class AliasItem(
    val title: String = "",
    val alias: String = "",
    val note: String = "",
    val mailboxTitle: String = "",
    val aliasOptions: AliasOptions = AliasOptions(emptyList(), emptyList()),
    val selectedSuffix: AliasSuffix? = null,
    val mailboxes: List<AliasMailboxUiModel> = emptyList(),
    val isMailboxListApplicable: Boolean = false,
    val aliasToBeCreated: String? = null
) {

    fun validate(): Set<AliasItemValidationErrors> {
        val mutableSet = mutableSetOf<AliasItemValidationErrors>()
        if (title.isBlank()) mutableSet.add(AliasItemValidationErrors.BlankTitle)
        if (alias.isBlank()) mutableSet.add(AliasItemValidationErrors.BlankAlias)
        if (!areAllAliasCharactersValid()) mutableSet.add(AliasItemValidationErrors.InvalidAliasContent)
        if (mailboxes.count { it.selected } == 0) mutableSet.add(AliasItemValidationErrors.NoMailboxes)
        return mutableSet.toSet()
    }

    private fun areAllAliasCharactersValid(): Boolean =
        alias.all { it.isLetterOrDigit() || ALLOWED_SPECIAL_CHARACTERS.contains(it) }


    companion object {
        val Empty = AliasItem()
        private val ALLOWED_SPECIAL_CHARACTERS: List<Char> = listOf('_', '-', '.')
    }
}

sealed interface AliasItemValidationErrors {
    object BlankTitle : AliasItemValidationErrors
    object BlankAlias : AliasItemValidationErrors
    object InvalidAliasContent : AliasItemValidationErrors
    object NoMailboxes : AliasItemValidationErrors
}
