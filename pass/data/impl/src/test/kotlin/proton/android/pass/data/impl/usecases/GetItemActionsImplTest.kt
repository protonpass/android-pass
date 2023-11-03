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

package proton.android.pass.data.impl.usecases

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import proton.android.pass.data.api.usecases.ItemActions
import proton.android.pass.data.api.usecases.capabilities.CanShareVaultStatus
import proton.android.pass.data.fakes.usecases.TestCanShareVault
import proton.android.pass.data.fakes.usecases.TestGetItemById
import proton.android.pass.data.fakes.usecases.TestGetUserPlan
import proton.android.pass.data.fakes.usecases.TestObserveVaults
import proton.android.pass.test.domain.TestItem
import proton.pass.domain.ItemId
import proton.pass.domain.ItemState
import proton.pass.domain.Plan
import proton.pass.domain.PlanLimit
import proton.pass.domain.PlanType
import proton.pass.domain.ShareId
import proton.pass.domain.ShareRole
import proton.pass.domain.Vault

class GetItemActionsImplTest {

    private lateinit var instance: GetItemActionsImpl
    private lateinit var getItemById: TestGetItemById
    private lateinit var observeUserPlan: TestGetUserPlan
    private lateinit var canShareVault: TestCanShareVault
    private lateinit var observeVaults: TestObserveVaults

    @Before
    fun setup() {
        getItemById = TestGetItemById()
        observeUserPlan = TestGetUserPlan()
        canShareVault = TestCanShareVault()
        observeVaults = TestObserveVaults()

        setDefaultState()

        instance = GetItemActionsImpl(
            getItemById = getItemById,
            observeUserPlan = observeUserPlan,
            canShareVault = canShareVault,
            observeVaults = observeVaults,
        )
    }

    @Test
    fun `can perform all actions if vault is owned and plan is paid`() = runTest {
        val res = instance.invoke(shareId = SHARE_ID, itemId = ItemId(""))
        val expected = ItemActions(
            canShare = CanShareVaultStatus.CanShare(1),
            canEdit = ItemActions.CanEditActionState.Enabled,
            canMoveToOtherVault = ItemActions.CanMoveToOtherVaultState.Enabled,
            canMoveToTrash = true,
            canRestoreFromTrash = false,
            canDelete = false
        )
        assertThat(res).isEqualTo(expected)
    }

    @Test
    fun `cannot share if canShareVault returns so`() = runTest {
        for (
            reason in listOf(
                CanShareVaultStatus.CannotShareReason.NotEnoughPermissions,
                CanShareVaultStatus.CannotShareReason.NotEnoughInvites
            )
        ) {
            val canShareResult = CanShareVaultStatus.CannotShare(reason)
            canShareVault.setResult(canShareResult)

            val res = instance.invoke(shareId = SHARE_ID, itemId = ItemId(""))
            assertThat(res.canShare).isEqualTo(canShareResult)
        }
    }

    @Test
    fun `can edit if downgraded but vault owned and enough permissions`() = runTest {
        val vaults = listOf(
            generateVault(shareId = SHARE_ID, owned = true, role = ShareRole.Admin),
            generateVault(owned = false, role = ShareRole.Read),
            generateVault(owned = false, role = ShareRole.Read)
        )
        observeVaults.sendResult(Result.success(vaults))

        val res = instance.invoke(shareId = SHARE_ID, itemId = ItemId(""))
        assertThat(res.canEdit).isEqualTo(ItemActions.CanEditActionState.Enabled)
    }

    @Test
    fun `cannot edit if not enough permissions`() = runTest {
        val vaults = listOf(
            generateVault(shareId = SHARE_ID, owned = false, role = ShareRole.Read),
            generateVault(owned = true, role = ShareRole.Admin)
        )
        observeVaults.sendResult(Result.success(vaults))

        val res = instance.invoke(shareId = SHARE_ID, itemId = ItemId(""))
        val expected = ItemActions.CanEditActionState.Disabled(
            ItemActions.CanEditActionState.CanEditDisabledReason.NotEnoughPermission
        )
        assertThat(res.canEdit).isEqualTo(expected)
    }

    @Test
    fun `cannot edit if downgraded`() = runTest {
        setPlan(planType = PlanType.Free("", ""))
        val vaults = listOf(
            generateVault(shareId = SHARE_ID, owned = true, role = ShareRole.Read),
            generateVault(owned = true, role = ShareRole.Admin)
        )
        observeVaults.sendResult(Result.success(vaults))

        val res = instance.invoke(shareId = SHARE_ID, itemId = ItemId(""))
        val expected = ItemActions.CanEditActionState.Disabled(
            ItemActions.CanEditActionState.CanEditDisabledReason.Downgraded
        )
        assertThat(res.canEdit).isEqualTo(expected)
    }

    @Test
    fun `cannot edit if item is in trash`() = runTest {
        setItem(state = ItemState.Trashed)
        val res = instance.invoke(shareId = SHARE_ID, itemId = ItemId(""))
        val expected = ItemActions.CanEditActionState.Disabled(
            ItemActions.CanEditActionState.CanEditDisabledReason.ItemInTrash
        )
        assertThat(res.canEdit).isEqualTo(expected)
    }

    @Test
    fun `cannot move to another vault if there is only 1 vault`() = runTest {
        val vaults = listOf(
            generateVault(shareId = SHARE_ID, owned = true, role = ShareRole.Admin)
        )
        observeVaults.sendResult(Result.success(vaults))

        val res = instance.invoke(shareId = SHARE_ID, itemId = ItemId(""))
        val expected = ItemActions.CanMoveToOtherVaultState.Disabled(
            ItemActions.CanMoveToOtherVaultState.CanMoveToOtherVaultDisabledReason.NoVaultToMoveToAvailable
        )
        assertThat(res.canMoveToOtherVault).isEqualTo(expected)
    }

    @Test
    fun `cannot move to another vault if there is only 1 vault where can create`() = runTest {
        val vaults = listOf(
            generateVault(shareId = SHARE_ID, owned = true, role = ShareRole.Admin),
            generateVault(owned = true, role = ShareRole.Read)
        )
        observeVaults.sendResult(Result.success(vaults))

        val res = instance.invoke(shareId = SHARE_ID, itemId = ItemId(""))
        val expected = ItemActions.CanMoveToOtherVaultState.Disabled(
            ItemActions.CanMoveToOtherVaultState.CanMoveToOtherVaultDisabledReason.NoVaultToMoveToAvailable
        )
        assertThat(res.canMoveToOtherVault).isEqualTo(expected)
    }

    @Test
    fun `cannot move to another vault if item is in trash`() = runTest {
        setItem(state = ItemState.Trashed)
        val res = instance.invoke(shareId = SHARE_ID, itemId = ItemId(""))
        val expected = ItemActions.CanMoveToOtherVaultState.Disabled(
            ItemActions.CanMoveToOtherVaultState.CanMoveToOtherVaultDisabledReason.ItemInTrash
        )
        assertThat(res.canMoveToOtherVault).isEqualTo(expected)
    }

    @Test
    fun `can move to another vault if vault is read only but owned (downgraded)`() = runTest {
        val vaults = listOf(
            generateVault(shareId = SHARE_ID, owned = true, role = ShareRole.Read),
            generateVault(owned = true, role = ShareRole.Admin)
        )
        observeVaults.sendResult(Result.success(vaults))

        val res = instance.invoke(shareId = SHARE_ID, itemId = ItemId(""))
        assertThat(res.canMoveToOtherVault).isEqualTo(ItemActions.CanMoveToOtherVaultState.Enabled)
    }

    @Test
    fun `cannot move to trash if item is already in trash`() = runTest {
        setItem(state = ItemState.Trashed)
        val res = instance.invoke(shareId = SHARE_ID, itemId = ItemId(""))
        assertThat(res.canMoveToTrash).isFalse()
    }

    @Test
    fun `cannot move to trash if is read only`() = runTest {
        val vaults = listOf(
            generateVault(shareId = SHARE_ID, owned = true, role = ShareRole.Read),
            generateVault(owned = true, role = ShareRole.Admin)
        )
        observeVaults.sendResult(Result.success(vaults))

        val res = instance.invoke(shareId = SHARE_ID, itemId = ItemId(""))
        assertThat(res.canMoveToTrash).isFalse()
    }

    @Test
    fun `cannot restore from trash if is read only`() = runTest {
        val vaults = listOf(
            generateVault(shareId = SHARE_ID, owned = true, role = ShareRole.Read),
            generateVault(owned = true, role = ShareRole.Admin)
        )
        observeVaults.sendResult(Result.success(vaults))

        val res = instance.invoke(shareId = SHARE_ID, itemId = ItemId(""))
        assertThat(res.canMoveToTrash).isFalse()
    }

    @Test
    fun `can delete if item is trashed`() = runTest {
        setItem(state = ItemState.Trashed)
        val res = instance.invoke(shareId = SHARE_ID, itemId = ItemId(""))
        assertThat(res.canDelete).isTrue()
    }

    @Test
    fun `cannot delete if item is trashed but is read only`() = runTest {
        setItem(state = ItemState.Trashed)
        val vaults = listOf(
            generateVault(shareId = SHARE_ID, owned = true, role = ShareRole.Read),
            generateVault(owned = true, role = ShareRole.Admin)
        )
        observeVaults.sendResult(Result.success(vaults))

        val res = instance.invoke(shareId = SHARE_ID, itemId = ItemId(""))
        assertThat(res.canDelete).isFalse()
    }

    private fun setDefaultState() {
        setItem()
        setPlan(planType = PlanType.Paid("", ""))
        canShareVault.setResult(CanShareVaultStatus.CanShare(1))

        val vaults = listOf(
            generateVault(shareId = SHARE_ID, owned = true, role = ShareRole.Admin),
            generateVault(owned = true, role = ShareRole.Admin)
        )
        observeVaults.sendResult(Result.success(vaults))
    }

    private fun generateVault(
        shareId: ShareId = ShareId("ShareId123"),
        owned: Boolean,
        role: ShareRole
    ) = Vault(
        name = "testVault",
        shareId = shareId,
        isOwned = owned,
        role = role
    )

    private fun setItem(state: ItemState = ItemState.Active) {
        getItemById.emitValue(Result.success(TestItem.create().copy(state = state.value)))
    }

    private fun setPlan(planType: PlanType) {
        observeUserPlan.setResult(
            Result.success(
                Plan(
                    planType = planType,
                    hideUpgrade = false,
                    vaultLimit = PlanLimit.Unlimited,
                    aliasLimit = PlanLimit.Unlimited,
                    totpLimit = PlanLimit.Unlimited,
                    updatedAt = 0
                )
            )
        )
    }

    companion object {
        private val SHARE_ID = ShareId("TestShareId")
    }
}
