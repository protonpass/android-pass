/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.features.vault.bottomsheet

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.commonui.fakes.FakeSavedStateHandleProvider
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.fakes.context.FakeEncryptionContext
import proton.android.pass.crypto.fakes.context.FakeEncryptionContextProvider
import proton.android.pass.data.api.errors.CannotCreateMoreVaultsError
import proton.android.pass.data.api.repositories.MigrateItemsResult
import proton.android.pass.data.fakes.usecases.FakeCreateVault
import proton.android.pass.data.fakes.usecases.FakeDeleteVault
import proton.android.pass.data.fakes.usecases.FakeMigrateItems
import proton.android.pass.data.fakes.usecases.FakeObserveUpgradeInfo
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.Plan
import proton.android.pass.domain.PlanLimit
import proton.android.pass.domain.ShareColor
import proton.android.pass.domain.ShareIcon
import proton.android.pass.domain.ShareId
import proton.android.pass.features.vault.VaultSnackbarMessage
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.notifications.fakes.FakeSnackbarDispatcher
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.TestConstants
import proton.android.pass.test.domain.ItemTestFactory
import proton.android.pass.test.domain.ShareTestFactory

class CreateVaultViewModelTest {

    @get:Rule
    val dispatcher = MainDispatcherRule()

    private lateinit var instance: CreateVaultViewModel
    private lateinit var snackbar: FakeSnackbarDispatcher
    private lateinit var createVault: FakeCreateVault
    private lateinit var deleteVault: FakeDeleteVault
    private lateinit var migrateItem: FakeMigrateItems
    private lateinit var getUpgradeInfo: FakeObserveUpgradeInfo
    private lateinit var savedState: FakeSavedStateHandleProvider

    @Before
    fun setup() {
        snackbar = FakeSnackbarDispatcher()
        createVault = FakeCreateVault()
        deleteVault = FakeDeleteVault()
        migrateItem = FakeMigrateItems()
        getUpgradeInfo = FakeObserveUpgradeInfo().apply {
            setResult(FakeObserveUpgradeInfo.DEFAULT)
        }
        savedState = FakeSavedStateHandleProvider().apply {
            get()[CreateVaultNextActionNavArgId.key] = CreateVaultNextAction.NEXT_ACTION_DONE
        }
        createViewModel()
    }

    @Test
    fun `emits initial state`() = runTest {
        instance.state.test {
            assertThat(awaitItem()).isEqualTo(BaseVaultUiState.Initial)
        }
    }

    @Test
    fun `holds name changes`() = runTest {
        val name = "test"
        instance.onNameChange(name)
        instance.state.test {
            assertThat(awaitItem().name).isEqualTo(name)
        }
    }

    @Test
    fun `holds icon changes`() = runTest {
        val icon = ShareIcon.Icon8
        instance.onIconChange(icon)
        instance.state.test {
            assertThat(awaitItem().icon).isEqualTo(icon)
        }
    }

    @Test
    fun `holds color changes`() = runTest {
        val color = ShareColor.Color7
        instance.onColorChange(color)
        instance.state.test {
            assertThat(awaitItem().color).isEqualTo(color)
        }
    }

    @Test
    fun `entering and clearing the text disables button and shows error`() = runTest {
        instance.onNameChange("name")
        instance.onNameChange("")
        instance.state.test {
            val item = awaitItem()
            assertThat(item.isCreateButtonEnabled).isEqualTo(IsButtonEnabled.Disabled)
            assertThat(item.isTitleRequiredError).isTrue()
        }
    }

    @Test
    fun `displays error snackbar on createVault error`() = runTest {
        instance.onNameChange("name")

        createVault.setResult(Result.failure(IllegalStateException("test")))
        instance.onCreateClick()
        instance.state.test {
            val item = awaitItem()
            assertThat(item.isVaultCreatedEvent).isEqualTo(IsVaultCreatedEvent.Unknown)
            assertThat(item.isLoading).isEqualTo(IsLoadingState.NotLoading)

            val message = snackbar.snackbarMessage.first().value()
            assertThat(message).isNotNull()
            assertThat(message).isEqualTo(VaultSnackbarMessage.CreateVaultError)
        }
    }

    @Test
    fun `displays proper error snackbar on cannotCreateMoreVaults`() = runTest {
        instance.onNameChange("name")

        createVault.setResult(Result.failure(CannotCreateMoreVaultsError()))
        instance.onCreateClick()
        instance.state.test {
            val item = awaitItem()
            assertThat(item.isVaultCreatedEvent).isEqualTo(IsVaultCreatedEvent.Unknown)
            assertThat(item.isLoading).isEqualTo(IsLoadingState.NotLoading)

            val message = snackbar.snackbarMessage.first().value()
            assertThat(message).isNotNull()
            assertThat(message).isEqualTo(VaultSnackbarMessage.CannotCreateMoreVaultsError)
        }
    }

    @Test
    fun `displays success snackbar on createVault success`() = runTest {
        instance.onNameChange("name")

        createVault.setResult(Result.success(ShareTestFactory.Vault.create()))
        instance.onCreateClick()
        instance.state.test {
            val item = awaitItem()
            assertThat(item.isVaultCreatedEvent).isEqualTo(IsVaultCreatedEvent.Created)
            assertThat(item.isLoading).isEqualTo(IsLoadingState.NotLoading)

            val message = snackbar.snackbarMessage.first().value()
            assertThat(message).isNotNull()
            assertThat(message).isEqualTo(VaultSnackbarMessage.CreateVaultSuccess)
        }
    }

    @Test
    fun `does not display upgrade ui if vault limit not reached`() = runTest {
        getUpgradeInfo.setResult(
            FakeObserveUpgradeInfo.DEFAULT.copy(
                isUpgradeAvailable = true,
                plan = Plan(
                    planType = TestConstants.FreePlanType,
                    vaultLimit = PlanLimit.Limited(2),
                    aliasLimit = PlanLimit.Limited(0),
                    totpLimit = PlanLimit.Limited(0),
                    updatedAt = 0,
                    hideUpgrade = false
                ),
                totalVaults = 1
            )
        )
        instance.createState.test {
            val item = awaitItem()
            assertThat(item.displayNeedUpgrade).isFalse()
        }
    }

    @Test
    fun `does not display upgrade ui if vault limit reached but upgrade unavailable`() = runTest {
        getUpgradeInfo.setResult(
            FakeObserveUpgradeInfo.DEFAULT.copy(
                isUpgradeAvailable = false,
                plan = Plan(
                    planType = TestConstants.FreePlanType,
                    vaultLimit = PlanLimit.Limited(1),
                    aliasLimit = PlanLimit.Limited(0),
                    totpLimit = PlanLimit.Limited(0),
                    updatedAt = 0,
                    hideUpgrade = false
                ),
                totalVaults = 1
            )
        )
        instance.createState.test {
            val item = awaitItem()
            assertThat(item.displayNeedUpgrade).isFalse()
        }
    }

    @Test
    fun `displays upgrade ui if plan is free`() = runTest {
        getUpgradeInfo.setResult(
            FakeObserveUpgradeInfo.DEFAULT.copy(
                isUpgradeAvailable = true,
                plan = Plan(
                    planType = TestConstants.FreePlanType,
                    vaultLimit = PlanLimit.Limited(1),
                    aliasLimit = PlanLimit.Limited(0),
                    totpLimit = PlanLimit.Limited(0),
                    updatedAt = 0,
                    hideUpgrade = false
                ),
                totalVaults = 1
            )
        )
        instance.createState.test {
            val item = awaitItem()
            assertThat(item.displayNeedUpgrade).isTrue()
        }
    }

    @Test
    fun `on next ShareVault check vault is created and item is migrated`() = runTest {
        setNextShareVault()
        createViewModel()

        createVault.setResult(Result.success(ShareTestFactory.Vault.create(id = NEW_SHARE_ID)))
        migrateItem.setResult(Result.success(MigrateItemsResult.AllMigrated(listOf(ItemTestFactory.create()))))

        instance.onNameChange("name")
        instance.onCreateClick()

        val createVaultMemory = createVault.memory()
        assertThat(createVaultMemory.size).isEqualTo(1)

        val migrateItemMemory = migrateItem.memory()
        val expectedMigrateItem = FakeMigrateItems.Payload(
            items = mapOf(ShareId(SHARE_ID) to listOf(ItemId(ITEM_ID))),
            destinationShare = ShareId(NEW_SHARE_ID)
        )
        assertThat(migrateItemMemory).isEqualTo(listOf(expectedMigrateItem))

        val deleteVaultMemory = deleteVault.memory()
        assertThat(deleteVaultMemory).isEmpty()

        instance.createState.test {
            val item = awaitItem()
            val event = item.base.isVaultCreatedEvent
            assertThat(event).isInstanceOf(IsVaultCreatedEvent.CreatedAndMoveToShare::class.java)

            val castedEvent = event as IsVaultCreatedEvent.CreatedAndMoveToShare
            assertThat(castedEvent.shareId).isEqualTo(ShareId(NEW_SHARE_ID))
        }
    }

    @Test
    fun `on next ShareVault if vault is not created item is not migrated`() = runTest {
        setNextShareVault()
        createViewModel()

        createVault.setResult(Result.failure(IllegalStateException("test")))

        instance.onNameChange("name")
        instance.onCreateClick()

        val createVaultMemory = createVault.memory()
        assertThat(createVaultMemory.size).isEqualTo(1)

        val migrateItemMemory = migrateItem.memory()
        assertThat(migrateItemMemory).isEmpty()

        val deleteVaultMemory = deleteVault.memory()
        assertThat(deleteVaultMemory).isEmpty()

        assertThat(
            snackbar.snackbarMessage.first().value()!!
        ).isEqualTo(VaultSnackbarMessage.CreateVaultError)
    }

    @Test
    fun `on next ShareVault if item migration fails new vault is deleted`() = runTest {
        setNextShareVault()
        createViewModel()

        createVault.setResult(Result.success(ShareTestFactory.Vault.create(id = NEW_SHARE_ID)))
        migrateItem.setResult(Result.failure(IllegalStateException("test")))
        deleteVault.setResult(Result.success(Unit))

        instance.onNameChange("name")
        instance.onCreateClick()

        val createVaultMemory = createVault.memory()
        assertThat(createVaultMemory.size).isEqualTo(1)

        val migrateItemMemory = migrateItem.memory()
        val expectedMigrateItem = FakeMigrateItems.Payload(
            items = mapOf(ShareId(SHARE_ID) to listOf(ItemId(ITEM_ID))),
            destinationShare = ShareId(NEW_SHARE_ID)
        )
        assertThat(migrateItemMemory).isEqualTo(listOf(expectedMigrateItem))

        val deleteVaultMemory = deleteVault.memory()
        assertThat(deleteVaultMemory).isEqualTo(listOf(ShareId(NEW_SHARE_ID)))

        assertThat(
            snackbar.snackbarMessage.first().value()!!
        ).isEqualTo(VaultSnackbarMessage.CreateVaultError)
    }

    @Test
    fun `on next ShareVault if item migration fails and deletevault fails it does not crash`() = runTest {
        setNextShareVault()
        createViewModel()

        createVault.setResult(Result.success(ShareTestFactory.Vault.create(id = NEW_SHARE_ID)))
        migrateItem.setResult(Result.failure(IllegalStateException("test")))
        deleteVault.setResult(Result.failure(IllegalStateException("test")))

        instance.onNameChange("name")
        instance.onCreateClick()

        val createVaultMemory = createVault.memory()
        assertThat(createVaultMemory.size).isEqualTo(1)

        val migrateItemMemory = migrateItem.memory()
        val expectedMigrateItem = FakeMigrateItems.Payload(
            items = mapOf(ShareId(SHARE_ID) to listOf(ItemId(ITEM_ID))),
            destinationShare = ShareId(NEW_SHARE_ID)
        )
        assertThat(migrateItemMemory).isEqualTo(listOf(expectedMigrateItem))

        val deleteVaultMemory = deleteVault.memory()
        assertThat(deleteVaultMemory).isEqualTo(listOf(ShareId(NEW_SHARE_ID)))

        assertThat(
            snackbar.snackbarMessage.first().value()!!
        ).isEqualTo(VaultSnackbarMessage.CreateVaultError)
    }

    @Test
    fun `preserves leading space but not end space`() = runTest {
        instance.onNameChange(" name ")
        instance.onCreateClick()

        val memory = createVault.memory()
        assertThat(memory.size).isEqualTo(1)

        val item = memory.first()
        val name = FakeEncryptionContext.decrypt(item.vault.name)
        assertThat(name).isEqualTo(" name")
    }

    private fun setNextShareVault() {
        savedState.get().apply {
            set(CreateVaultNextActionNavArgId.key, CreateVaultNextAction.NEXT_ACTION_SHARE)
            set(CommonOptionalNavArgId.ShareId.key, SHARE_ID)
            set(CommonOptionalNavArgId.ItemId.key, ITEM_ID)
        }
    }

    private fun createViewModel() {
        instance = CreateVaultViewModel(
            snackbarDispatcher = snackbar,
            createVault = createVault,
            deleteVault = deleteVault,
            encryptionContextProvider = FakeEncryptionContextProvider(),
            savedStateHandleProvider = savedState,
            migrateItems = migrateItem,
            observeUpgradeInfo = getUpgradeInfo
        )
    }

    companion object {
        const val NEW_SHARE_ID = "CreateVaultViewModelTest-NewShareID"
        const val SHARE_ID = "CreateVaultViewModelTest-ShareID"
        const val ITEM_ID = "CreateVaultViewModelTest-ItemID"
    }
}
