package proton.android.pass.featureitemcreate.impl.alias

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize
import proton.android.pass.featureitemcreate.impl.alias.AliasUtils.areAllAliasCharactersValid

@Parcelize
@Immutable
data class AliasItem(
    val title: String = "",
    val prefix: String = "",
    val note: String = "",
    val mailboxTitle: String = "",
    val aliasOptions: AliasOptionsUiModel = AliasOptionsUiModel(emptyList(), emptyList()),
    val selectedSuffix: AliasSuffixUiModel? = null,
    val mailboxes: List<SelectedAliasMailboxUiModel> = emptyList(),
    val aliasToBeCreated: String? = null
) : Parcelable {

    fun validate(allowEmptyTitle: Boolean): Set<AliasItemValidationErrors> {
        val mutableSet = mutableSetOf<AliasItemValidationErrors>()
        if (!allowEmptyTitle) {
            if (title.isBlank()) mutableSet.add(AliasItemValidationErrors.BlankTitle)
        }

        if (prefix.isBlank()) mutableSet.add(AliasItemValidationErrors.BlankPrefix)

        if (prefix.startsWith(".")) mutableSet.add(AliasItemValidationErrors.InvalidAliasContent)

        if (prefix.endsWith(".")) mutableSet.add(AliasItemValidationErrors.InvalidAliasContent)

        if (prefix.contains("..")) mutableSet.add(AliasItemValidationErrors.InvalidAliasContent)

        if (prefix.length > MAX_PREFIX_LENGTH) mutableSet.add(AliasItemValidationErrors.InvalidAliasContent)

        if (!areAllAliasCharactersValid(prefix)) mutableSet.add(AliasItemValidationErrors.InvalidAliasContent)

        if (mailboxes.count { it.selected } == 0) mutableSet.add(AliasItemValidationErrors.NoMailboxes)

        return mutableSet.toSet()
    }

    companion object {
        const val MAX_PREFIX_LENGTH: Int = 40

        val Empty = AliasItem()
    }
}

sealed interface AliasItemValidationErrors {
    object BlankTitle : AliasItemValidationErrors
    object BlankPrefix : AliasItemValidationErrors
    object InvalidAliasContent : AliasItemValidationErrors
    object NoMailboxes : AliasItemValidationErrors
}
