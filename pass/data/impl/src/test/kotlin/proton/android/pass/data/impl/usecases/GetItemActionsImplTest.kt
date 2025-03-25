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
import proton.android.pass.data.api.usecases.capabilities.CanShareShareStatus
import proton.android.pass.data.fakes.usecases.FakeGetItemById
import proton.android.pass.data.fakes.usecases.TestCanShareShare
import proton.android.pass.data.fakes.usecases.TestGetUserPlan
import proton.android.pass.data.fakes.usecases.TestObserveAllShares
import proton.android.pass.data.fakes.usecases.organizations.FakeObserveOrganizationSharingPolicy
import proton.android.pass.data.fakes.usecases.shares.FakeObserveShare
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.Plan
import proton.android.pass.domain.PlanLimit
import proton.android.pass.domain.PlanType
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareRole
import proton.android.pass.domain.organizations.OrganizationItemShareMode
import proton.android.pass.domain.organizations.OrganizationSecureLinkMode
import proton.android.pass.domain.organizations.OrganizationSharingPolicy
import proton.android.pass.test.domain.TestItem
import proton.android.pass.test.domain.TestShare

internal class GetItemActionsImplTest {

    private lateinit var instance: GetItemActionsImpl
    private lateinit var getItemById: FakeGetItemById
    private lateinit var observeUserPlan: TestGetUserPlan
    private lateinit var canShareVault: TestCanShareShare
    private lateinit var observeShare: FakeObserveShare
    private lateinit var observeAllShares: TestObserveAllShares
    private lateinit var observeOrganizationSharingPolicy: FakeObserveOrganizationSharingPolicy

    @Before
    fun setup() {
        getItemById = FakeGetItemById()
        observeUserPlan = TestGetUserPlan()
        canShareVault = TestCanShareShare()
        observeShare = FakeObserveShare()
        observeAllShares = TestObserveAllShares()
        observeOrganizationSharingPolicy = FakeObserveOrganizationSharingPolicy()

        setDefaultState()

        instance = GetItemActionsImpl(
            getItemById = getItemById,
            observeUserPlan = observeUserPlan,
            canShareShare = canShareVault,
            observeShare = observeShare,
            observeAllShares = observeAllShares,
            observeOrganizationSharingPolicy = observeOrganizationSharingPolicy
        )
    }

    @Test
    fun `can perform all actions if vault is owned and plan is paid`() = runTest {
        val share = TestShare.Vault.create(id = SHARE_ID.id)
        observeShare.emitValue(share)

        val res = instance.invoke(shareId = SHARE_ID, itemId = ItemId(""))
        val expected = ItemActions(
            canShare = CanShareShareStatus.CanShare(1),
            canEdit = ItemActions.CanEditActionState.Enabled,
            canMoveToOtherVault = ItemActions.CanMoveToOtherVaultState.Enabled,
            canMoveToTrash = true,
            canRestoreFromTrash = false,
            canDelete = false,
            canUseOptions = true
        )
        assertThat(res).isEqualTo(expected)
    }

    @Test
    fun `cannot share if canShareVault returns so`() = runTest {
        val reasons = listOf(
            CanShareShareStatus.CannotShareReason.NotEnoughPermissions,
            CanShareShareStatus.CannotShareReason.NotEnoughInvites
        )
        for (reason in reasons) {
            val canShareResult = CanShareShareStatus.CannotShare(reason)
            canShareVault.setResult(canShareResult)

            val res = instance.invoke(shareId = SHARE_ID, itemId = ItemId(""))
            assertThat(res.canShare).isEqualTo(canShareResult)
        }
    }

    @Test
    fun `can edit if downgraded but vault owned and enough permissions`() = runTest {
        val vaultShare = TestShare.Vault.create(
            id = SHARE_ID.id,
            isOwner = true,
            shareRole = ShareRole.Admin
        )
        val vaultShares = listOf(
            vaultShare,
            TestShare.Vault.create(isOwner = false, shareRole = ShareRole.Read),
            TestShare.Vault.create(isOwner = false, shareRole = ShareRole.Read)
        )
        observeShare.emitValue(vaultShare)
        observeAllShares.sendResult(Result.success(vaultShares))

        val res = instance.invoke(shareId = SHARE_ID, itemId = ItemId(""))

        assertThat(res.canEdit).isEqualTo(ItemActions.CanEditActionState.Enabled)
    }

    @Test
    fun `cannot edit if not enough permissions`() = runTest {
        val vaultShare = TestShare.Vault.create(
            id = SHARE_ID.id,
            isOwner = false,
            shareRole = ShareRole.Read
        )
        val vaultShares = listOf(
            vaultShare,
            TestShare.Vault.create(isOwner = true, shareRole = ShareRole.Admin)
        )
        val expected = ItemActions.CanEditActionState.Disabled(
            ItemActions.CanEditActionState.CanEditDisabledReason.NotEnoughPermission
        )
        observeShare.emitValue(vaultShare)
        observeAllShares.sendResult(Result.success(vaultShares))

        val res = instance.invoke(shareId = SHARE_ID, itemId = ItemId(""))

        assertThat(res.canEdit).isEqualTo(expected)
    }

    @Test
    fun `cannot edit if downgraded`() = runTest {
        val vaultShare = TestShare.Vault.create(
            id = SHARE_ID.id,
            isOwner = true,
            shareRole = ShareRole.Read
        )
        val vaultShares = listOf(
            vaultShare,
            TestShare.Vault.create(isOwner = true, shareRole = ShareRole.Admin)
        )
        val expected = ItemActions.CanEditActionState.Disabled(
            ItemActions.CanEditActionState.CanEditDisabledReason.Downgraded
        )
        setPlan(planType = PlanType.Free("", ""))
        observeShare.emitValue(vaultShare)
        observeAllShares.sendResult(Result.success(vaultShares))

        val res = instance.invoke(shareId = SHARE_ID, itemId = ItemId(""))

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
        val vaultShare = TestShare.Vault.create(
            id = SHARE_ID.id,
            isOwner = true,
            shareRole = ShareRole.Admin
        )
        val vaultShares = listOf(vaultShare)
        val expected = ItemActions.CanMoveToOtherVaultState.Disabled(
            ItemActions.CanMoveToOtherVaultState.CanMoveToOtherVaultDisabledReason.NoVaultToMoveToAvailable
        )
        observeShare.emitValue(vaultShare)
        observeAllShares.sendResult(Result.success(vaultShares))

        val res = instance.invoke(shareId = SHARE_ID, itemId = ItemId(""))

        assertThat(res.canMoveToOtherVault).isEqualTo(expected)
    }

    @Test
    fun `cannot move to another vault if there is only 1 vault where can create`() = runTest {
        val vaultShare = TestShare.Vault.create(
            id = SHARE_ID.id,
            isOwner = true,
            shareRole = ShareRole.Admin
        )
        val vaultShares = listOf(
            vaultShare,
            TestShare.Vault.create(isOwner = true, shareRole = ShareRole.Read)
        )
        val expected = ItemActions.CanMoveToOtherVaultState.Disabled(
            ItemActions.CanMoveToOtherVaultState.CanMoveToOtherVaultDisabledReason.NoVaultToMoveToAvailable
        )
        observeShare.emitValue(vaultShare)
        observeAllShares.sendResult(Result.success(vaultShares))

        val res = instance.invoke(shareId = SHARE_ID, itemId = ItemId(""))

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
        val vaultShare = TestShare.Vault.create(
            id = SHARE_ID.id,
            isOwner = true,
            shareRole = ShareRole.Read
        )
        val vaultShares = listOf(
            vaultShare,
            TestShare.Vault.create(isOwner = true, shareRole = ShareRole.Admin)
        )
        observeShare.emitValue(vaultShare)
        observeAllShares.sendResult(Result.success(vaultShares))

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
        val vaultShare = TestShare.Vault.create(
            id = SHARE_ID.id,
            isOwner = true,
            shareRole = ShareRole.Read
        )
        val vaultShares = listOf(
            vaultShare,
            TestShare.Vault.create(isOwner = true, shareRole = ShareRole.Admin)
        )
        observeShare.emitValue(vaultShare)
        observeAllShares.sendResult(Result.success(vaultShares))

        val res = instance.invoke(shareId = SHARE_ID, itemId = ItemId(""))

        assertThat(res.canMoveToTrash).isFalse()
    }

    @Test
    fun `cannot restore from trash if is read only`() = runTest {
        val vaultShare = TestShare.Vault.create(
            id = SHARE_ID.id,
            isOwner = true,
            shareRole = ShareRole.Read
        )
        val vaultShares = listOf(
            vaultShare,
            TestShare.Vault.create(isOwner = true, shareRole = ShareRole.Admin)
        )
        observeShare.emitValue(vaultShare)
        observeAllShares.sendResult(Result.success(vaultShares))

        val res = instance.invoke(shareId = SHARE_ID, itemId = ItemId(""))

        assertThat(res.canRestoreFromTrash).isFalse()
    }

    @Test
    fun `can delete if item is trashed`() = runTest {
        setItem(state = ItemState.Trashed)
        val res = instance.invoke(shareId = SHARE_ID, itemId = ItemId(""))
        assertThat(res.canDelete).isTrue()
    }

    @Test
    fun `cannot delete if item is trashed but is read only`() = runTest {
        val vaultShare = TestShare.Vault.create(
            id = SHARE_ID.id,
            isOwner = true,
            shareRole = ShareRole.Read
        )
        val vaultShares = listOf(
            vaultShare,
            TestShare.Vault.create(isOwner = true, shareRole = ShareRole.Admin)
        )
        observeShare.emitValue(vaultShare)
        observeAllShares.sendResult(Result.success(vaultShares))
        setItem(state = ItemState.Trashed)

        val res = instance.invoke(shareId = SHARE_ID, itemId = ItemId(""))

        assertThat(res.canDelete).isFalse()
    }

    private fun setDefaultState() {
        setItem()
        setPlan(planType = PlanType.Paid.Plus("", ""))
        canShareVault.setResult(CanShareShareStatus.CanShare(1))

        val vaultShare = TestShare.Vault.create(
            id = SHARE_ID.id,
            isOwner = true,
            shareRole = ShareRole.Admin
        )
        val vaultShares = listOf(
            vaultShare,
            TestShare.Vault.create(isOwner = true, shareRole = ShareRole.Admin)
        )
        observeShare.emitValue(vaultShare)
        observeAllShares.sendResult(Result.success(vaultShares))

        val organizationSharingPolicy = OrganizationSharingPolicy(
            itemShareMode = OrganizationItemShareMode.Enabled,
            secureLinkMode = OrganizationSecureLinkMode.Enabled
        )
        observeOrganizationSharingPolicy.emitValue(organizationSharingPolicy)
    }

    private fun setItem(state: ItemState = ItemState.Active) {
        getItemById.emit(Result.success(TestItem.create().copy(state = state.value)))
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

    private companion object {

        private val SHARE_ID = ShareId("TestShareId")

    }

}
