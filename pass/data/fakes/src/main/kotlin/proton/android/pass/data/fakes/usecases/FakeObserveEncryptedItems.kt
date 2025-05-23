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

package proton.android.pass.data.fakes.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.FlowUtils.testFlow
import proton.android.pass.common.api.None
import proton.android.pass.crypto.fakes.context.TestEncryptionContext
import proton.android.pass.crypto.fakes.context.TestEncryptionContextProvider
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveEncryptedItems
import proton.android.pass.datamodels.api.serializeToProto
import proton.android.pass.domain.AddressDetailsContent
import proton.android.pass.domain.ContactDetailsContent
import proton.android.pass.domain.CreditCardType
import proton.android.pass.domain.Flags
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemEncrypted
import proton.android.pass.domain.ItemFlag
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.PersonalDetailsContent
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareSelection
import proton.android.pass.domain.ShareType
import proton.android.pass.domain.WorkDetailsContent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeObserveEncryptedItems @Inject constructor() : ObserveEncryptedItems {

    private val fallback: MutableSharedFlow<Result<List<ItemEncrypted>>> = testFlow()
    private val flowsMap = mutableMapOf<Params, MutableSharedFlow<List<ItemEncrypted>>>()

    fun emitValue(value: List<ItemEncrypted>) {
        fallback.tryEmit(Result.success(value))
    }

    fun emit(params: Params, value: List<ItemEncrypted>) {
        flowsMap[params] = flowsMap[params] ?: testFlow()
        flowsMap[params]?.tryEmit(value)
    }

    fun sendException(exception: Exception) {
        fallback.tryEmit(Result.failure(exception))
    }

    override fun invoke(
        selection: ShareSelection,
        itemState: ItemState?,
        filter: ItemTypeFilter,
        userId: UserId?,
        itemFlags: Map<ItemFlag, Boolean>
    ): Flow<List<ItemEncrypted>> {
        val params = Params(
            selection = selection,
            itemState = itemState,
            filter = filter,
            userId = userId,
            itemFlags = itemFlags
        )
        val flow = flowsMap[params]
        return flow ?: fallback.map { it.getOrThrow() }
    }

    data class DefaultValues(
        val login: ItemEncrypted,
        val alias: ItemEncrypted,
        val note: ItemEncrypted
    ) {
        fun asList(): List<ItemEncrypted> = listOf(login, alias, note)
    }

    companion object {

        val defaultValues: DefaultValues = DefaultValues(
            createLogin(itemId = ItemId("login")),
            createAlias(itemId = ItemId("alias")),
            createNote(itemId = ItemId("note"))
        )

        fun createItem(
            shareId: ShareId = ShareId("share-123"),
            itemId: ItemId = ItemId("item-123"),
            aliasEmail: String? = null,
            flags: Int = 0,
            itemContents: ItemContents
        ): ItemEncrypted {
            val now = Clock.System.now()
            val asProto = itemContents.serializeToProto(
                itemUuid = "123",
                encryptionContext = TestEncryptionContext
            )
            return TestEncryptionContextProvider().withEncryptionContext {
                ItemEncrypted(
                    id = itemId,
                    userId = UserId("user-id"),
                    revision = 1,
                    shareId = shareId,
                    title = encrypt(itemContents.title),
                    note = encrypt(itemContents.note),
                    content = encrypt(asProto.toByteArray()),
                    aliasEmail = aliasEmail,
                    state = ItemState.Active.value,
                    modificationTime = now,
                    createTime = now,
                    lastAutofillTime = None,
                    isPinned = false,
                    pinTime = None,
                    flags = Flags(flags),
                    shareCount = 0,
                    shareType = ShareType.Vault
                )
            }
        }

        fun createLogin(
            shareId: ShareId = ShareId("share-123"),
            itemId: ItemId = ItemId("item-123"),
            title: String = "login-item",
            email: String = "user@email.com",
            username: String = "username",
            password: String = "",
            primaryTotp: String = "",
            note: String = "note"
        ): ItemEncrypted = createItem(
            shareId = shareId,
            itemId = itemId,
            itemContents = ItemContents.Login(
                title = title,
                note = note,
                itemEmail = email,
                itemUsername = username,
                password = HiddenState.Concealed(TestEncryptionContext.encrypt(password)),
                urls = emptyList(),
                packageInfoSet = emptySet(),
                primaryTotp = HiddenState.Revealed(
                    TestEncryptionContext.encrypt(primaryTotp),
                    primaryTotp
                ),
                customFields = emptyList(),
                passkeys = emptyList()
            )
        )

        fun createAlias(
            shareId: ShareId = ShareId("share-123"),
            itemId: ItemId = ItemId("item-123"),
            title: String = "alias-item",
            alias: String = "some.alias@domain.test",
            note: String = "note",
            flags: Int = 0
        ) = createItem(
            shareId = shareId,
            itemId = itemId,
            aliasEmail = alias,
            flags = flags,
            itemContents = ItemContents.Alias(
                title = title,
                note = note,
                aliasEmail = alias,
                customFields = emptyList()
            )
        )

        fun createNote(
            shareId: ShareId = ShareId("share-123"),
            itemId: ItemId = ItemId("item-123"),
            title: String = "note-item",
            note: String = "note"
        ) = createItem(
            shareId = shareId,
            itemId = itemId,
            itemContents = ItemContents.Note(
                title = title,
                note = note,
                customFields = emptyList()
            )
        )

        fun createCreditCard(
            shareId: ShareId = ShareId("share-123"),
            itemId: ItemId = ItemId("item-123"),
            title: String = "note-item",
            note: String = "note",
            holder: String = "Card holder",
            number: String = "1234123412341234",
            pin: String = "0000",
            verificationNumber: String = "000",
            expirationDate: String = "2030-01"
        ) = createItem(
            shareId = shareId,
            itemId = itemId,
            itemContents = ItemContents.CreditCard(
                title = title,
                note = note,
                cardHolder = holder,
                type = CreditCardType.Other,
                number = number,
                cvv = if (verificationNumber.isBlank()) {
                    HiddenState.Empty(TestEncryptionContext.encrypt(verificationNumber))
                } else {
                    HiddenState.Concealed(TestEncryptionContext.encrypt(verificationNumber))
                },
                pin = if (pin.isBlank()) {
                    HiddenState.Empty(TestEncryptionContext.encrypt(pin))
                } else {
                    HiddenState.Concealed(TestEncryptionContext.encrypt(pin))
                },
                expirationDate = expirationDate,
                customFields = emptyList()
            )
        )

        fun createIdentity(
            shareId: ShareId = ShareId("share-123"),
            itemId: ItemId = ItemId("item-123"),
            fullName: String = "John Doe"
        ) = createItem(
            shareId = shareId,
            itemId = itemId,
            itemContents = ItemContents.Identity(
                title = "Identity",
                note = "note",
                personalDetailsContent = PersonalDetailsContent.EMPTY.copy(
                    fullName = fullName,
                    firstName = "First name",
                    middleName = "Middle name",
                    lastName = "Last name",
                    phoneNumber = "1234567890"
                ),
                addressDetailsContent = AddressDetailsContent.EMPTY.copy(
                    streetAddress = "123 Main St",
                    zipOrPostalCode = "12345",
                    city = "City",
                    countryOrRegion = "Country",
                    organization = "Organization"
                ),
                contactDetailsContent = ContactDetailsContent.EMPTY,
                workDetailsContent = WorkDetailsContent.EMPTY,
                extraSectionContentList = emptyList(),
                customFields = emptyList()
            )
        )
    }

    data class Params(
        val selection: ShareSelection = ShareSelection.AllShares,
        val itemState: ItemState? = ItemState.Active,
        val filter: ItemTypeFilter = ItemTypeFilter.All,
        val userId: UserId? = null,
        val itemFlags: Map<ItemFlag, Boolean> = emptyMap()
    )
}
