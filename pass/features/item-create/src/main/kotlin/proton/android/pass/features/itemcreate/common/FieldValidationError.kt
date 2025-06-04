/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.features.itemcreate.common

import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option

sealed interface ValidationError

sealed interface CommonFieldValidationError : ValidationError {
    data object BlankTitle : CommonFieldValidationError
}

sealed interface CustomFieldValidationError : ValidationError {
    data class EmptyField(val sectionIndex: Option<Int> = None, val index: Int) :
        CustomFieldValidationError

    data class InvalidTotp(val sectionIndex: Option<Int> = None, val index: Int) :
        CustomFieldValidationError
}

sealed interface LoginItemValidationError : ValidationError {
    @JvmInline
    value class InvalidUrl(val index: Int) : LoginItemValidationError
    data object InvalidPrimaryTotp : LoginItemValidationError
}

sealed interface CreditCardItemValidationError : ValidationError {
    data object InvalidExpirationDate : CreditCardItemValidationError
}

sealed interface AliasItemValidationError : ValidationError {
    data object BlankPrefix : AliasItemValidationError
    data object InvalidAliasContent : AliasItemValidationError
    data object NoMailboxes : AliasItemValidationError
}

sealed interface IdentityItemValidationError : ValidationError {
    @JvmInline
    value class PersonalDetailsInvalidTotp(val index: Int) : IdentityItemValidationError

    @JvmInline
    value class AddressDetailsInvalidTotp(val index: Int) : IdentityItemValidationError

    @JvmInline
    value class ContactDetailsInvalidTotp(val index: Int) : IdentityItemValidationError

    @JvmInline
    value class WorkDetailsInvalidTotp(val index: Int) : IdentityItemValidationError
}
