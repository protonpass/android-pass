/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.features.itemcreate.alias

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.commonrust.api.AliasPrefixError
import proton.android.pass.commonrust.api.AliasPrefixValidator
import proton.android.pass.domain.ItemContents
import proton.android.pass.features.itemcreate.common.AliasItemValidationError
import proton.android.pass.features.itemcreate.common.CommonFieldValidationError
import proton.android.pass.features.itemcreate.common.UICustomFieldContent
import proton.android.pass.features.itemcreate.common.ValidationError
import proton.android.pass.log.api.PassLogger

@Parcelize
@Immutable
data class AliasItemFormState(
    val title: String = "",
    val prefix: String = "",
    val note: String = "",
    val mailboxTitle: String = "",
    val aliasOptions: AliasOptionsUiModel = AliasOptionsUiModel(emptyList(), emptyList()),
    val selectedSuffix: AliasSuffixUiModel? = null,
    val selectedMailboxes: Set<AliasMailboxUiModel> = emptySet(),
    val aliasToBeCreated: String? = null,
    val slNote: String? = null,
    val senderName: String? = null,
    val customFields: List<UICustomFieldContent>
) : Parcelable {

    fun validate(allowEmptyTitle: Boolean, aliasPrefixValidator: AliasPrefixValidator): Set<ValidationError> {
        val mutableSet = mutableSetOf<ValidationError>()

        if (!allowEmptyTitle) {
            if (title.isBlank()) mutableSet.add(CommonFieldValidationError.BlankTitle)
        }

        aliasPrefixValidator.validate(prefix).onFailure {
            if (it is AliasPrefixError) {
                mutableSet.add(it.toError())
            } else {
                PassLogger.w(TAG, "Error validating alias prefix")
                PassLogger.w(TAG, it)
            }
        }

        if (selectedMailboxes.isEmpty()) mutableSet.add(AliasItemValidationError.NoMailboxes)

        return mutableSet.toSet()
    }

    internal fun toItemContents(): ItemContents.Alias = ItemContents.Alias(
        title = title,
        note = note,
        aliasEmail = aliasToBeCreated.orEmpty(),
        customFields = customFields.map(UICustomFieldContent::toCustomFieldContent)
    )

    companion object {
        private const val TAG = "AliasItemFormState"
        const val MAX_PREFIX_LENGTH: Int = 40

        fun default(title: Option<String>): AliasItemFormState = when (title) {
            None -> AliasItemFormState(customFields = emptyList())
            is Some -> AliasItemFormState(
                title = title.value,
                prefix = AliasUtils.formatAlias(title.value),
                customFields = emptyList()
            )
        }
    }
}

fun AliasPrefixError.toError(): AliasItemValidationError = when (this) {
    AliasPrefixError.DotAtTheBeginning -> AliasItemValidationError.InvalidAliasContent
    AliasPrefixError.DotAtTheEnd -> AliasItemValidationError.InvalidAliasContent
    AliasPrefixError.InvalidCharacter -> AliasItemValidationError.InvalidAliasContent
    AliasPrefixError.PrefixEmpty -> AliasItemValidationError.BlankPrefix
    AliasPrefixError.PrefixTooLong -> AliasItemValidationError.InvalidAliasContent
    AliasPrefixError.TwoConsecutiveDots -> AliasItemValidationError.InvalidAliasContent
}
