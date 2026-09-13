/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.features.item.details.detail.presentation.handlers

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import proton.android.pass.commonuimodels.api.attachments.AttachmentsState
import proton.android.pass.commonuimodels.api.items.DetailEvent
import proton.android.pass.commonuimodels.api.items.ItemDetailState
import proton.android.pass.crypto.fakes.context.FakeEncryptionContextProvider
import proton.android.pass.data.fakes.usecases.FakeCanDisplayTotp
import proton.android.pass.data.fakes.usecases.FakeChangeAliasStatus
import proton.android.pass.data.fakes.usecases.FakeObserveAliasDetails
import proton.android.pass.data.fakes.usecases.FakeObserveItemById
import proton.android.pass.data.fakes.usecases.aliascontact.FakeObserveAliasContacts
import proton.android.pass.data.fakes.usecases.folders.FakeGetFolderHierarchy
import proton.android.pass.domain.ItemFlag
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.preferences.FakePreferenceRepository
import proton.android.pass.test.domain.ItemTestFactory
import proton.android.pass.test.domain.ShareTestFactory
import proton.android.pass.totp.fakes.FakeObserveTotpFromUri

internal class AliasItemDetailsHandlerObserverImplTest {

    private lateinit var observeItemById: FakeObserveItemById
    private lateinit var handler: AliasItemDetailsHandlerObserverImpl

    private val shareId = ShareId("share-id")
    private val itemId = ItemId("item-id")

    @Before
    fun setUp() {
        observeItemById = FakeObserveItemById()
        handler = AliasItemDetailsHandlerObserverImpl(
            encryptionContextProvider = FakeEncryptionContextProvider(),
            observeTotpFromUri = FakeObserveTotpFromUri(),
            getFolderHierarchy = FakeGetFolderHierarchy(),
            canDisplayTotp = FakeCanDisplayTotp(),
            observeItemById = observeItemById,
            observeAliasDetails = FakeObserveAliasDetails(),
            observeAliasContacts = FakeObserveAliasContacts(),
            userPreferencesRepository = FakePreferenceRepository(),
            changeAliasStatus = FakeChangeAliasStatus()
        )
    }

    @Test
    fun `WHEN observed alias status changes THEN item details state is updated`() = runTest {
        // GIVEN an enabled alias is already loaded (no AliasDisabled flag → flags = 0)
        val enabledItem = ItemTestFactory.createAlias(
            shareId = shareId,
            itemId = itemId,
            flags = 0
        )
        val disabledItem = ItemTestFactory.createAlias(
            shareId = shareId,
            itemId = itemId,
            flags = ItemFlag.AliasDisabled.value
        )

        handler.observe(
            share = ShareTestFactory.Item.create(id = shareId.id),
            item = enabledItem,
            attachmentsState = AttachmentsState.Initial,
            savedStateEntries = emptyMap(),
            detailEvent = DetailEvent.Idle
        )
            .filterIsInstance<ItemDetailState.Alias>()
            .map { it.itemContents.isEnabled }
            .distinctUntilChanged()
            .test {
                assertThat(awaitItem()).isTrue()

                // WHEN the observed item is disabled
                observeItemById.emitValue(Result.success(disabledItem))
                // THEN the details state reports the alias as disabled
                assertThat(awaitItem()).isFalse()

                // WHEN the observed item is enabled again
                observeItemById.emitValue(Result.success(enabledItem))
                // THEN the details state reports the alias as enabled
                assertThat(awaitItem()).isTrue()

                cancelAndIgnoreRemainingEvents()
            }
    }
}
