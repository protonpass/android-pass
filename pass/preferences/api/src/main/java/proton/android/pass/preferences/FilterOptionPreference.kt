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

package proton.android.pass.preferences

const val FILTER_ALL = 1
const val FILTER_LOGIN = 2
const val FILTER_ALIAS = 3
const val FILTER_NOTE = 4
const val FILTER_CREDIT_CARD = 5
const val FILTER_IDENTITY = 6
const val FILTER_LOGIN_MFA = 7
const val FILTER_SHARED_WITH_ME = 8
const val FILTER_SHARED_BY_ME = 9
const val FILTER_CUSTOM_ITEM = 10

enum class FilterOptionPreference(val value: Int) {
    All(FILTER_ALL),
    Login(FILTER_LOGIN),
    Alias(FILTER_ALIAS),
    Note(FILTER_NOTE),
    CreditCard(FILTER_CREDIT_CARD),
    Identity(FILTER_IDENTITY),
    Custom(FILTER_CUSTOM_ITEM),
    LoginMFA(FILTER_LOGIN_MFA),
    SharedWithMe(FILTER_SHARED_WITH_ME),
    SharedByMe(FILTER_SHARED_BY_ME);

    companion object {

        fun fromValue(value: Int): FilterOptionPreference = when (value) {
            FILTER_ALL -> All
            FILTER_LOGIN -> Login
            FILTER_ALIAS -> Alias
            FILTER_NOTE -> Note
            FILTER_CREDIT_CARD -> CreditCard
            FILTER_IDENTITY -> Identity
            FILTER_CUSTOM_ITEM -> Custom
            FILTER_LOGIN_MFA -> LoginMFA
            FILTER_SHARED_WITH_ME -> SharedWithMe
            FILTER_SHARED_BY_ME -> SharedByMe
            else -> All
        }

    }

}
