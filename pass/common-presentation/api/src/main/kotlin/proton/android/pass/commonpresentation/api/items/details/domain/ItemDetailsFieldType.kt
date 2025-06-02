/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.commonpresentation.api.items.details.domain

import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId

sealed interface ItemDetailsFieldType {

    sealed interface PlainCopyable : ItemDetailsFieldType {

        val text: String

        data class Alias(override val text: String) : PlainCopyable

        data class BirthDate(override val text: String) : PlainCopyable

        data class CardNumber(override val text: String) : PlainCopyable

        data class City(override val text: String) : PlainCopyable

        data class Company(override val text: String) : PlainCopyable

        data class CountryOrRegion(override val text: String) : PlainCopyable

        data class County(override val text: String) : PlainCopyable

        data class CustomField(override val text: String) : PlainCopyable

        data class Email(override val text: String) : PlainCopyable

        data class Facebook(override val text: String) : PlainCopyable

        data class FirstName(override val text: String) : PlainCopyable

        data class Floor(override val text: String) : PlainCopyable

        data class FullName(override val text: String) : PlainCopyable

        data class Gender(override val text: String) : PlainCopyable

        data class Instagram(override val text: String) : PlainCopyable

        data class LastName(override val text: String) : PlainCopyable

        data class LicenseNumber(override val text: String) : PlainCopyable

        data class LinkedIn(override val text: String) : PlainCopyable

        data class MiddleName(override val text: String) : PlainCopyable

        data class Occupation(override val text: String) : PlainCopyable

        data class Organization(override val text: String) : PlainCopyable

        data class PassportNumber(override val text: String) : PlainCopyable

        data class PhoneNumber(override val text: String) : PlainCopyable

        data class Reddit(override val text: String) : PlainCopyable

        data class StateOrProvince(override val text: String) : PlainCopyable

        data class StreetAddress(override val text: String) : PlainCopyable

        data class TotpCode(override val text: String) : PlainCopyable

        data class Username(override val text: String) : PlainCopyable

        data class Website(override val text: String) : PlainCopyable

        data class XHandle(override val text: String) : PlainCopyable

        data class Yahoo(override val text: String) : PlainCopyable

        data class ZipOrPostalCode(override val text: String) : PlainCopyable

        data class PublicKey(override val text: String) : PlainCopyable

        data class SSID(override val text: String) : PlainCopyable
    }

    sealed interface HiddenCopyable : ItemDetailsFieldType {

        val hiddenState: HiddenState

        data class CustomField(override val hiddenState: HiddenState, val index: Int) :
            HiddenCopyable {
            override fun equals(other: Any?): Boolean = other is CustomField && this.index == other.index

            override fun hashCode(): Int = index.hashCode()
        }

        data class Cvv(override val hiddenState: HiddenState) : HiddenCopyable {
            override fun equals(other: Any?) = other is Cvv
            override fun hashCode() = javaClass.hashCode()
        }

        data class Password(override val hiddenState: HiddenState) : HiddenCopyable {
            override fun equals(other: Any?) = other is Password
            override fun hashCode() = javaClass.hashCode()
        }

        data class Pin(override val hiddenState: HiddenState) : HiddenCopyable {
            override fun equals(other: Any?) = other is Pin
            override fun hashCode() = javaClass.hashCode()
        }

        data class PrivateKey(override val hiddenState: HiddenState) : HiddenCopyable {
            override fun equals(other: Any?) = other is PrivateKey
            override fun hashCode() = javaClass.hashCode()
        }

        data class SocialSecurityNumber(override val hiddenState: HiddenState) : HiddenCopyable {
            override fun equals(other: Any?) = other is SocialSecurityNumber
            override fun hashCode() = javaClass.hashCode()
        }

    }

    sealed interface LoginItemAction : ItemDetailsFieldType
    sealed interface NoteItemAction : ItemDetailsFieldType
    sealed interface CreditCardItemAction : ItemDetailsFieldType
    sealed interface AliasItemAction : ItemDetailsFieldType {
        data class CreateLoginFromAlias(
            val alias: String,
            val shareId: ShareId
        ) : AliasItemAction

        data class ToggleAlias(
            val shareId: ShareId,
            val itemId: ItemId,
            val value: Boolean
        ) : AliasItemAction

        data object ContactBanner : AliasItemAction
        data class ContactSection(val shareId: ShareId, val itemId: ItemId) : AliasItemAction
    }

    sealed interface IdentityItemAction : ItemDetailsFieldType
    sealed interface CustomItemAction : ItemDetailsFieldType
    sealed interface WifiNetworkItemAction : ItemDetailsFieldType
    sealed interface SSHKeyItemAction : ItemDetailsFieldType

}
