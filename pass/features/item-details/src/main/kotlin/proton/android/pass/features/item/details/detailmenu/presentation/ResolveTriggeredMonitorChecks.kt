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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.api.usecases.compromisedpassword.ObserveCompromisedPasswords
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemFlag
import proton.android.pass.domain.ItemType
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import proton.android.pass.securitycenter.api.SecurityCheck
import proton.android.pass.securitycenter.api.isCheckExcluded
import proton.android.pass.securitycenter.api.passwords.DuplicatedPasswordChecker
import proton.android.pass.securitycenter.api.passwords.InsecurePasswordChecker
import proton.android.pass.securitycenter.api.passwords.MissingTfaChecker
import javax.inject.Inject

class ResolveTriggeredMonitorChecks @Inject constructor(
    private val insecurePasswordChecker: InsecurePasswordChecker,
    private val duplicatedPasswordChecker: DuplicatedPasswordChecker,
    private val missingTfaChecker: MissingTfaChecker,
    private val observeCompromisedPasswords: ObserveCompromisedPasswords,
    private val getUserPlan: GetUserPlan,
    private val featureFlagsPreferencesRepository: FeatureFlagsPreferencesRepository
) {

    private val compromisedPasswordsEnabledFlow: Flow<Boolean> =
        featureFlagsPreferencesRepository[FeatureFlag.PASS_COMPROMISED_PASSWORDS]

    suspend operator fun invoke(item: Item): Set<ItemFlag> = buildSet {
        if (item.itemType !is ItemType.Login) return emptySet()
        if (item.isCheckTriggered(SecurityCheck.WeakPassword) {
                insecurePasswordChecker(listOf(item)).hasInsecurePasswords
            }
        ) {
            add(ItemFlag.SkipWeakPasswordCheck)
        }
        if (item.isCheckTriggered(SecurityCheck.ReusedPassword) {
                duplicatedPasswordChecker(item).hasDuplications
            }
        ) {
            add(ItemFlag.SkipReusedPasswordCheck)
        }
        if (item.isCheckTriggered(SecurityCheck.Missing2fa) {
                missingTfaChecker(listOf(item)).isMissingTwoFa
            }
        ) {
            add(ItemFlag.Skip2FACheck)
        }
        if (item.isCheckTriggered(SecurityCheck.CompromisedPassword) { isPasswordCompromised(item) }) {
            add(ItemFlag.SkipCompromisedPasswordCheck)
        }
    }

    private suspend fun Item.isCheckTriggered(check: SecurityCheck, fires: suspend () -> Boolean): Boolean =
        !isCheckExcluded(check) && fires()

    private suspend fun isPasswordCompromised(item: Item): Boolean {
        val isAllowed = compromisedPasswordsEnabledFlow.first() && getUserPlan().first().isPaidPlan

        return isAllowed && observeCompromisedPasswords(item.userId, item.shareId, item.id).first()
    }

}
