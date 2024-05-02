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
import kotlinx.datetime.Clock
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.FlowUtils.testFlow
import proton.android.pass.common.api.None
import proton.android.pass.crypto.fakes.context.TestEncryptionContext
import proton.android.pass.crypto.fakes.context.TestEncryptionContextProvider
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveItems
import proton.android.pass.data.api.usecases.items.ItemIsBreachedFilter
import proton.android.pass.data.api.usecases.items.ItemSecurityCheckFilter
import proton.android.pass.datamodels.api.fromParsed
import proton.android.pass.datamodels.api.serializeToProto
import proton.android.pass.domain.CreditCardType
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareSelection
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestObserveItems @Inject constructor() : ObserveItems {

    private val fallback: MutableSharedFlow<List<Item>> = testFlow()
    private val flowsMap = mutableMapOf<Params, MutableSharedFlow<List<Item>>>()

    fun emitValue(value: List<Item>) {
        fallback.tryEmit(value)
    }

    fun emit(params: Params, value: List<Item>) {
        flowsMap[params] = flowsMap[params] ?: testFlow()
        flowsMap[params]?.tryEmit(value)
    }

    override fun invoke(
        selection: ShareSelection,
        itemState: ItemState?,
        filter: ItemTypeFilter,
        userId: UserId?,
        securityCheckFilter: ItemSecurityCheckFilter,
        isBreachedFilter: ItemIsBreachedFilter
    ): Flow<List<Item>> = flowsMap[
        Params(
            selection = selection,
            itemState = itemState,
            filter = filter,
            userId = userId,
            securityCheckFilter = securityCheckFilter,
            isBreachedFilter = isBreachedFilter
        )
    ] ?: fallback

    data class DefaultValues(
        val login: Item,
        val alias: Item,
        val note: Item
    ) {
        fun asList(): List<Item> = listOf(login, alias, note)
    }

    companion object {

        val defaultValues = DefaultValues(
            createLogin(itemId = ItemId("login")),
            createAlias(itemId = ItemId("alias")),
            createNote(itemId = ItemId("note"))
        )

        fun createItem(
            shareId: ShareId = ShareId("share-123"),
            itemId: ItemId = ItemId("item-123"),
            aliasEmail: String? = null,
            itemContents: ItemContents
        ): Item {
            val now = Clock.System.now()
            val asProto = itemContents.serializeToProto(
                itemUuid = "123",
                encryptionContext = TestEncryptionContext
            )
            return TestEncryptionContextProvider().withEncryptionContext {
                Item(
                    id = itemId,
                    itemUuid = "",
                    revision = 1,
                    shareId = shareId,
                    itemType = ItemType.fromParsed(this, asProto, aliasEmail),
                    title = encrypt(itemContents.title),
                    note = encrypt(itemContents.note),
                    content = encrypt(asProto.toByteArray()),
                    packageInfoSet = emptySet(),
                    state = ItemState.Active.value,
                    modificationTime = now,
                    createTime = now,
                    lastAutofillTime = None,
                    isPinned = false,
                    flags = 0
                )
            }
        }

        fun createLogin(
            shareId: ShareId = ShareId("share-123"),
            itemId: ItemId = ItemId("item-123"),
            title: String = "login-item",
            username: String = "username",
            password: String = "",
            primaryTotp: String = "",
            note: String = "note"
        ): Item = createItem(
            shareId = shareId,
            itemId = itemId,
            itemContents = ItemContents.Login(
                title = title,
                note = note,
                username = username,
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
            note: String = "note"
        ) = createItem(
            shareId = shareId,
            itemId = itemId,
            aliasEmail = alias,
            itemContents = ItemContents.Alias(
                title = title,
                note = note,
                aliasEmail = alias
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
                note = note
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
                expirationDate = expirationDate
            )
        )
    }

    data class Params(
        val selection: ShareSelection = ShareSelection.AllShares,
        val itemState: ItemState? = ItemState.Active,
        val filter: ItemTypeFilter = ItemTypeFilter.All,
        val userId: UserId? = null,
        val securityCheckFilter: ItemSecurityCheckFilter = ItemSecurityCheckFilter.All,
        val isBreachedFilter: ItemIsBreachedFilter = ItemIsBreachedFilter.NotBreached
    )
}
