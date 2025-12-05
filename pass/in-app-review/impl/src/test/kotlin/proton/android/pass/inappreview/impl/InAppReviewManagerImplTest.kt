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

package proton.android.pass.inappreview.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.Before
import org.junit.Test
import proton.android.pass.data.fakes.usecases.FakeGetUserPlan
import proton.android.pass.domain.Plan
import proton.android.pass.domain.PlanLimit
import proton.android.pass.domain.PlanType
import proton.android.pass.inappreview.api.InAppReviewManager
import proton.android.pass.inappreview.impl.InAppReviewManagerImpl.Companion.FREE_USER_ITEM_AUTOFILL_TRIGGER
import proton.android.pass.inappreview.impl.InAppReviewManagerImpl.Companion.FREE_USER_ITEM_CREATED_TRIGGER
import proton.android.pass.inappreview.impl.InAppReviewManagerImpl.Companion.PAID_USER_ITEM_AUTOFILL_TRIGGER
import proton.android.pass.inappreview.impl.InAppReviewManagerImpl.Companion.PAID_USER_ITEM_CREATED_TRIGGER
import proton.android.pass.inappreview.impl.InAppReviewManagerImpl.Companion.TIMES_USED
import proton.android.pass.preferences.AppUsageConfig
import proton.android.pass.preferences.FakeInternalSettingsRepository

class InAppReviewManagerImplTest {

    private lateinit var getUserPlan: FakeGetUserPlan
    private lateinit var internalSettingsRepository: FakeInternalSettingsRepository
    private lateinit var inAppReviewManager: InAppReviewManager

    @Before
    fun setUp() {
        getUserPlan = FakeGetUserPlan()
        internalSettingsRepository = FakeInternalSettingsRepository()
        inAppReviewManager = InAppReviewManagerImpl(
            getUserPlan = getUserPlan,
            internalSettingsRepository = internalSettingsRepository
        )
    }

    @Test
    fun `shouldRequestReview returns false when conditions are not met for free user`() = runTest {
        inAppReviewManager.shouldRequestReview().test {
            assertThat(awaitItem()).isFalse()
        }
    }

    @Test
    fun `shouldRequestReview returns false when conditions are not met for paid user`() = runTest {
        setPaidPlan()

        inAppReviewManager.shouldRequestReview().test {
            assertThat(awaitItem()).isFalse()
        }
    }

    @Test
    fun `shouldRequestReview returns false when review has been already triggered`() = runTest {
        internalSettingsRepository.setInAppReviewTriggered(true)

        inAppReviewManager.shouldRequestReview().test {
            assertThat(awaitItem()).isFalse()
        }
    }

    @Test
    fun `shouldRequestReview returns true when item created is reached for free user`() = runTest {
        internalSettingsRepository.setItemCreateCount(FREE_USER_ITEM_CREATED_TRIGGER)

        inAppReviewManager.shouldRequestReview().test {
            assertThat(awaitItem()).isTrue()
        }
    }

    @Test
    fun `shouldRequestReview returns true when item created is reached for paid user`() = runTest {
        internalSettingsRepository.setItemCreateCount(PAID_USER_ITEM_CREATED_TRIGGER)
        setPaidPlan()

        inAppReviewManager.shouldRequestReview().test {
            assertThat(awaitItem()).isTrue()
        }
    }

    @Test
    fun `shouldRequestReview returns true when item autofill is reached for free user`() = runTest {
        internalSettingsRepository.setItemAutofillCount(FREE_USER_ITEM_AUTOFILL_TRIGGER)

        inAppReviewManager.shouldRequestReview().test {
            assertThat(awaitItem()).isTrue()
        }
    }

    @Test
    fun `shouldRequestReview returns true when item autofill is reached for paid user`() = runTest {
        internalSettingsRepository.setItemAutofillCount(PAID_USER_ITEM_AUTOFILL_TRIGGER)
        setPaidPlan()

        inAppReviewManager.shouldRequestReview().test {
            assertThat(awaitItem()).isTrue()
        }
    }

    @Test
    fun `shouldRequestReview returns true when items launched is reached `() = runTest {
        internalSettingsRepository.setAppUsage(AppUsageConfig(TIMES_USED, Clock.System.now()))

        inAppReviewManager.shouldRequestReview().test {
            assertThat(awaitItem()).isTrue()
        }
    }

    private fun setPaidPlan() {
        getUserPlan.setResult(
            Result.success(
                Plan(
                    planType = PlanType.Paid.Plus("", ""),
                    hideUpgrade = false,
                    vaultLimit = PlanLimit.Limited(10),
                    aliasLimit = PlanLimit.Limited(10),
                    totpLimit = PlanLimit.Limited(10),
                    updatedAt = 0
                )
            )
        )
    }
}
