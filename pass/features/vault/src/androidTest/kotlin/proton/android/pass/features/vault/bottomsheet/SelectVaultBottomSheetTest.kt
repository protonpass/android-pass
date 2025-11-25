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

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.fakes.TestSavedStateHandleProvider
import proton.android.pass.data.api.usecases.UpgradeInfo
import proton.android.pass.data.fakes.usecases.TestCanCreateItemInVault
import proton.android.pass.data.fakes.usecases.TestCanPerformPaidAction
import proton.android.pass.data.fakes.usecases.TestObserveUpgradeInfo
import proton.android.pass.data.fakes.usecases.TestObserveVaultsWithItemCount
import proton.android.pass.domain.Plan
import proton.android.pass.domain.PlanLimit
import proton.android.pass.domain.PlanType
import proton.android.pass.domain.ShareFlags
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareRole
import proton.android.pass.domain.Vault
import proton.android.pass.domain.VaultId
import proton.android.pass.domain.VaultWithItemCount
import proton.android.pass.features.vault.R
import proton.android.pass.features.vault.VaultNavigation
import proton.android.pass.features.vault.bottomsheet.select.SelectVaultBottomsheet
import proton.android.pass.features.vault.bottomsheet.select.SelectedVaultArg
import proton.android.pass.test.CallChecker
import proton.android.pass.test.HiltComponentActivity
import proton.android.pass.test.TestConstants
import proton.android.pass.test.waitUntilExists
import java.util.Date
import javax.inject.Inject
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@HiltAndroidTest
class SelectVaultBottomSheetTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltComponentActivity>()


    @Inject
    lateinit var observeVaultsWithItemCount: TestObserveVaultsWithItemCount

    @Inject
    lateinit var observeUpgradeInfo: TestObserveUpgradeInfo

    @Inject
    lateinit var savedStateHandle: TestSavedStateHandleProvider

    @Inject
    lateinit var canPerformPaidAction: TestCanPerformPaidAction

    @Inject
    lateinit var canCreateItemInVault: TestCanCreateItemInVault

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun canSelectVaultWithFreePlanAndOneVault() {
        setupPlan(true, TestConstants.FreePlanType)
        setupVaults(1)

        val checker = CallChecker<ShareId>()
        composeTestRule.apply {
            setContent {
                PassTheme {
                    SelectVaultBottomsheet(
                        onNavigate = {
                            if (it is VaultNavigation.VaultSelected) {
                                checker.call(it.shareId)
                            }
                        }
                    )
                }
            }

            val vaultRow = hasText(vaultNameForIndex(0))
            waitUntilExists(vaultRow)
            onNode(vaultRow).performClick()
            waitUntil { checker.isCalled }
        }

        assertEquals(shareIdForIndex(0), checker.memory)
    }

    @Test
    fun canSelectPrimaryVaultWithFreePlanAndMultipleVaults() {
        val primaryIndex = 1
        setupPlan(true, TestConstants.FreePlanType)
        setupVaults(3, primaryIndex = primaryIndex)

        val checker = CallChecker<ShareId>()
        composeTestRule.apply {
            setContent {
                PassTheme {
                    SelectVaultBottomsheet(
                        onNavigate = {
                            if (it is VaultNavigation.VaultSelected) {
                                checker.call(it.shareId)
                            }
                        }
                    )
                }
            }

            val enabledVaultRow = hasText(vaultNameForIndex(primaryIndex))
            waitUntilExists(enabledVaultRow)
            onNode(enabledVaultRow).performClick()

            waitUntil { checker.isCalled }
        }

        assertEquals(shareIdForIndex(1), checker.memory)
    }

    @Test
    fun cannotSelectOtherVaultWithFreePlanAndMultipleVaults() {
        setupPlan(true, TestConstants.FreePlanType)
        setupVaults(3, primaryIndex = 1)
        canCreateItemInVault.setResult(shareIdForIndex(0), false)
        canCreateItemInVault.setResult(shareIdForIndex(1), true)
        canCreateItemInVault.setResult(shareIdForIndex(2), false)

        val checker = CallChecker<ShareId>()
        composeTestRule.apply {
            setContent {
                PassTheme {
                    SelectVaultBottomsheet(
                        onNavigate = {
                            if (it is VaultNavigation.VaultSelected) {
                                checker.call(it.shareId)
                            }
                        }
                    )
                }
            }

            val disabledVaultRow = hasText(vaultNameForIndex(0))
            waitUntilExists(disabledVaultRow)
            onNode(disabledVaultRow).assertHasNoClickAction()

            assertFalse(checker.isCalled)
        }

    }

    @Test
    fun canGoToUpgradeInFreePlan() {
        setupPlan(true, TestConstants.FreePlanType)
        setupVaults(1)

        val checker = CallChecker<Unit>()
        composeTestRule.apply {
            setContent {
                PassTheme {
                    SelectVaultBottomsheet(
                        onNavigate = {
                            if (it == VaultNavigation.Upgrade) {
                                checker.call()
                            }
                        }
                    )
                }
            }

            val text = activity.getString(R.string.bottomsheet_cannot_select_not_primary_vault)
            val node = hasText(text, substring = true)
            waitUntilExists(node)
            onNode(node).performClick()

            waitUntil { checker.isCalled }
        }
    }

    @Test
    fun canSelectOtherVaultInPaidPlan() {
        setupPlan(true, PlanType.Paid.Plus("", ""))
        setupVaults(3, primaryIndex = 1)

        val checker = CallChecker<ShareId>()
        composeTestRule.apply {
            setContent {
                PassTheme {
                    SelectVaultBottomsheet(
                        onNavigate = {
                            if (it is VaultNavigation.VaultSelected) {
                                checker.call(it.shareId)
                            }
                        }
                    )
                }
            }

            val otherVaultRow = hasText(vaultNameForIndex(0))
            waitUntilExists(otherVaultRow)
            onNode(otherVaultRow).assertHasClickAction().performClick()
            waitUntil { checker.isCalled }
        }

        assertEquals(shareIdForIndex(0), checker.memory)
    }

    @Test
    fun cannotSelectReadOnlyVault() {
        setupPlan(true, PlanType.Paid.Plus("", ""))
        val vaults = listOf(
            VaultWithItemCount(
                vault = Vault(
                    userId = UserId(""),
                    shareId = shareIdForIndex(0),
                    vaultId = VaultId("vault-id"),
                    name = vaultNameForIndex(0),
                    createTime = Date(),
                    shareFlags = ShareFlags(0)
                ),
                activeItemCount = 1,
                trashedItemCount = 1
            ),
            VaultWithItemCount(
                vault = Vault(
                    userId = UserId(""),
                    shareId = shareIdForIndex(1),
                    vaultId = VaultId("vault-id-1"),
                    name = vaultNameForIndex(1),
                    role = ShareRole.Read,
                    createTime = Date(),
                    shareFlags = ShareFlags(0)
                ),
                activeItemCount = 1,
                trashedItemCount = 1
            )
        )
        savedStateHandle.get()[SelectedVaultArg.key] = shareIdForIndex(0).id
        observeVaultsWithItemCount.sendResult(Result.success(vaults))
        canCreateItemInVault.setResult(shareIdForIndex(0), true)
        canCreateItemInVault.setResult(shareIdForIndex(1), false)


        composeTestRule.apply {
            setContent {
                PassTheme {
                    SelectVaultBottomsheet(
                        onNavigate = {}
                    )
                }
            }

            val otherVaultRow = hasText(vaultNameForIndex(1))
            waitUntilExists(otherVaultRow)
            onNode(otherVaultRow).assertHasNoClickAction()
        }
    }

    private fun setupVaults(count: Int, primaryIndex: Int = 0) {
        val vaults = (0 until count).map {
            VaultWithItemCount(
                vault = Vault(
                    userId = UserId(""),
                    shareId = shareIdForIndex(it),
                    vaultId = VaultId("vault-id-$it"),
                    name = vaultNameForIndex(it),
                    createTime = Date(),
                    shareFlags = ShareFlags(0)
                ),
                activeItemCount = 1,
                trashedItemCount = 1
            )
        }

        savedStateHandle.get()[SelectedVaultArg.key] = shareIdForIndex(primaryIndex).id
        observeVaultsWithItemCount.sendResult(Result.success(vaults))
    }

    private fun setupPlan(canUpgrade: Boolean, planType: PlanType) {
        canPerformPaidAction.setResult(planType != TestConstants.FreePlanType)
        observeUpgradeInfo.setResult(
            UpgradeInfo(
                isUpgradeAvailable = canUpgrade,
                isSubscriptionAvailable = canUpgrade,
                plan = Plan(
                    planType = planType,
                    hideUpgrade = false,
                    vaultLimit = PlanLimit.Unlimited,
                    aliasLimit = PlanLimit.Unlimited,
                    totpLimit = PlanLimit.Unlimited,
                    updatedAt = 123
                ),
                totalVaults = 1,
                totalAlias = 1,
                totalTotp = 1
            )
        )
    }

    private fun vaultNameForIndex(index: Int) = "Vault$index"
    private fun shareIdForIndex(index: Int) = ShareId("share-$index")

}
