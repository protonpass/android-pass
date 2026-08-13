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

package proton.android.pass.features.item.details.detailmenu.presentation

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import proton.android.pass.common.api.None
import proton.android.pass.common.api.some
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemAction
import proton.android.pass.data.api.repositories.CompromisedPasswordItem
import proton.android.pass.data.fakes.usecases.FakeGetUserPlan
import proton.android.pass.data.fakes.usecases.compromisedpassword.FakeObserveCompromisedPasswords
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemFlag
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.Plan
import proton.android.pass.domain.PlanLimit
import proton.android.pass.domain.PlanType
import proton.android.pass.preferences.FakeFeatureFlagsPreferenceRepository
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.securitycenter.api.passwords.DuplicatedPasswordReport
import proton.android.pass.securitycenter.api.passwords.InsecurePasswordsReport
import proton.android.pass.securitycenter.api.passwords.Missing2faReport
import proton.android.pass.securitycenter.fakes.passwords.FakeDuplicatedPasswordChecker
import proton.android.pass.securitycenter.fakes.passwords.FakeInsecurePasswordChecker
import proton.android.pass.securitycenter.fakes.passwords.FakeMissing2faChecker
import proton.android.pass.test.domain.ItemTestFactory

internal class ItemDetailsMenuMonitoringTest {

    private lateinit var insecurePasswordChecker: FakeInsecurePasswordChecker

    private lateinit var duplicatedPasswordChecker: FakeDuplicatedPasswordChecker

    private lateinit var missingTfaChecker: FakeMissing2faChecker

    private lateinit var observeCompromisedPasswords: FakeObserveCompromisedPasswords

    private lateinit var getUserPlan: FakeGetUserPlan

    private lateinit var featureFlags: FakeFeatureFlagsPreferenceRepository

    private lateinit var resolveTriggeredMonitorChecks: ResolveTriggeredMonitorChecks

    @Before
    fun setup() {
        insecurePasswordChecker = FakeInsecurePasswordChecker()
        duplicatedPasswordChecker = FakeDuplicatedPasswordChecker()
        missingTfaChecker = FakeMissing2faChecker()
        observeCompromisedPasswords = FakeObserveCompromisedPasswords()
        getUserPlan = FakeGetUserPlan()
        featureFlags = FakeFeatureFlagsPreferenceRepository()
        resolveTriggeredMonitorChecks = ResolveTriggeredMonitorChecks(
            insecurePasswordChecker = insecurePasswordChecker,
            duplicatedPasswordChecker = duplicatedPasswordChecker,
            missingTfaChecker = missingTfaChecker,
            observeCompromisedPasswords = observeCompromisedPasswords,
            getUserPlan = getUserPlan,
            featureFlagsPreferencesRepository = featureFlags
        )
    }

    @Test
    fun `no warning and item is not excluded - Exclude from monitoring button`() = runTest {
        val item = itemWith()

        assertThat(showsIncludeButton(item)).isFalse()
    }

    @Test
    fun `no warning and item is excluded - Include from monitoring button`() = runTest {
        val item = itemWith(ItemFlag.SkipHealthCheck)

        assertThat(showsIncludeButton(item)).isTrue()
    }

    @Test
    fun `opened from anywhere but excluded items with at least one warning - always exclude`() = runTest {
        val item = itemWith()
        fireWeakPassword(item)
        fireCompromisedPassword(item)

        assertThat(showsIncludeButton(item)).isFalse()
    }

    @Test
    fun `opened from the excluded items section with at least one ignored warning - always include`() = runTest {
        val item = itemWith(ItemFlag.SkipWeakPasswordCheck)
        fireWeakPassword(item)
        fireMissingTwoFa(item)

        assertThat(showsIncludeButton(item, isOpenedFromExcludedSection = true)).isTrue()
    }

    @Test
    fun `all alerts are ignored - the button is include from any view`() = runTest {
        val item = itemWith(ItemFlag.SkipWeakPasswordCheck, ItemFlag.SkipReusedPasswordCheck)
        fireWeakPassword(item)
        fireReusedPassword()

        assertThat(showsIncludeButton(item)).isTrue()
        assertThat(showsIncludeButton(item, isOpenedFromExcludedSection = true)).isTrue()
    }

    @Test
    fun `all alerts are resumed - the button is exclude from any view`() = runTest {
        val item = itemWith()
        fireWeakPassword(item)
        fireReusedPassword()

        assertThat(showsIncludeButton(item)).isFalse()
        assertThat(showsIncludeButton(item, isOpenedFromExcludedSection = true)).isFalse()
    }

    @Test
    fun `excluded item with one resumed parameter - the warning shows again and rule 4 applies`() = runTest {
        val item = itemWith(ItemFlag.SkipHealthCheck, ItemFlag.SkipReusedPasswordCheck)
        fireWeakPassword(item)
        fireReusedPassword()

        assertThat(resolveTriggeredMonitorChecks(item))
            .containsExactly(ItemFlag.SkipWeakPasswordCheck)
        assertThat(showsIncludeButton(item, isOpenedFromExcludedSection = true)).isTrue()
        assertThat(showsIncludeButton(item)).isFalse()
    }

    @Test
    fun `item not excluded with one ignored check - the button is still exclude from monitoring`() = runTest {
        val item = itemWith(ItemFlag.SkipWeakPasswordCheck)
        fireWeakPassword(item)
        fireReusedPassword()

        assertThat(showsIncludeButton(item)).isFalse()
    }

    @Test
    fun `password was set to secure on an excluded item - all warnings are gone and the item stays excluded`() =
        runTest {
            val item = itemWith(ItemFlag.SkipHealthCheck, ItemFlag.SkipWeakPasswordCheck)

            assertThat(resolveTriggeredMonitorChecks(item)).isEmpty()
            assertThat(showsIncludeButton(item)).isTrue()
        }

    private suspend fun showsIncludeButton(item: Item, isOpenedFromExcludedSection: Boolean = false): Boolean =
        ItemDetailsMenuState(
            action = BottomSheetItemAction.None,
            event = ItemDetailsMenuEvent.Idle,
            itemOption = item.some(),
            itemActionsOption = None,
            shareOption = None,
            triggeredChecks = resolveTriggeredMonitorChecks(item),
            isOpenedFromExcludedSection = isOpenedFromExcludedSection
        ).isItemExcludedFromMonitoring

    private fun fireWeakPassword(item: Item) {
        insecurePasswordChecker.setResult(
            InsecurePasswordsReport(weakPasswordItems = listOf(item), vulnerablePasswordItems = emptyList())
        )
    }

    private fun fireReusedPassword() {
        duplicatedPasswordChecker.setResult(
            DuplicatedPasswordReport(duplicatedPasswordItems = setOf(itemWith(id = "duplicate")))
        )
    }

    private fun fireMissingTwoFa(item: Item) {
        missingTfaChecker.setResult(Missing2faReport(items = listOf(item)))
    }

    private fun fireCompromisedPassword(item: Item) {
        featureFlags.set(FeatureFlag.PASS_COMPROMISED_PASSWORDS, true)
        getUserPlan.setResult(Result.success(PAID_PLAN))
        observeCompromisedPasswords.emit(
            listOf(CompromisedPasswordItem(shareId = item.shareId, itemId = item.id))
        )
    }

    private fun itemWith(vararg flags: ItemFlag, id: String = "item-123"): Item = ItemTestFactory.createLogin(
        itemId = ItemId(id),
        flags = flags.sumOf { flag -> flag.value }
    )

    private companion object {

        private val PAID_PLAN = Plan(
            planType = PlanType.Paid.Plus(name = "plus", displayName = "Proton Plus"),
            hideUpgrade = false,
            vaultLimit = PlanLimit.Unlimited,
            aliasLimit = PlanLimit.Unlimited,
            totpLimit = PlanLimit.Unlimited,
            updatedAt = 0L
        )

    }
}
