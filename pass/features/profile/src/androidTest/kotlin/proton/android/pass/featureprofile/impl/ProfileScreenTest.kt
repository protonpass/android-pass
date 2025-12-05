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

package proton.android.pass.featureprofile.impl

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.intent.rule.IntentsRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.autofill.api.AutofillStatus
import proton.android.pass.autofill.api.AutofillSupportedStatus
import proton.android.pass.autofill.fakes.FakeAutofillManager
import proton.android.pass.commonui.api.AppUrls.PASS_STORE
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.data.api.ItemCountSummary
import proton.android.pass.data.api.usecases.UpgradeInfo
import proton.android.pass.data.fakes.usecases.FakeObserveItemCount
import proton.android.pass.data.fakes.usecases.FakeObserveMFACount
import proton.android.pass.data.fakes.usecases.FakeObserveUpgradeInfo
import proton.android.pass.data.fakes.usecases.FakeObserveUserAccessData
import proton.android.pass.domain.Plan
import proton.android.pass.domain.PlanLimit
import proton.android.pass.domain.PlanType
import proton.android.pass.features.profile.PASS_IMPORT
import proton.android.pass.features.profile.ProfileNavigation
import proton.android.pass.features.profile.ProfileScreen
import proton.android.pass.features.profile.R
import proton.android.pass.features.profile.SHOW_RATING_OPTION
import proton.android.pass.test.CallChecker
import proton.android.pass.test.HiltComponentActivity
import proton.android.pass.test.TestConstants
import javax.inject.Inject
import proton.android.pass.composecomponents.impl.R as CompR

@HiltAndroidTest
class ProfileScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltComponentActivity>()

    @get:Rule(order = 2)
    val intentsRule = IntentsRule()

    @Inject
    lateinit var observeUpgradeInfo: FakeObserveUpgradeInfo

    @Inject
    lateinit var autofillManager: FakeAutofillManager

    @Inject
    lateinit var observeItemCount: FakeObserveItemCount

    @Inject
    lateinit var observeMfaCount: FakeObserveMFACount

    @Inject
    lateinit var observeUserAccessData: FakeObserveUserAccessData

    @Before
    fun setup() {
        hiltRule.inject()
        setupPlan(PlanType.Paid.Plus("", ""), true)
        autofillManager.emitStatus(AutofillSupportedStatus.Supported(AutofillStatus.EnabledByOurService))
        observeItemCount.sendResult(Result.success(ItemCountSummary.Initial))
        observeMfaCount.emitResult(0)
        observeUserAccessData.sendValue(null)
    }

    @Test
    fun onSettingsClickCalled() {
        val checker = CallChecker<Unit>()
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    ProfileScreen(
                        onNavigateEvent = {
                            if (it is ProfileNavigation.Settings) {
                                checker.call()
                            }
                        },
                        enterPinSuccess = false,
                        onClearPinSuccess = {}
                    )
                }
            }

            onNodeWithText(activity.getString(R.string.profile_option_feedback)).performScrollTo()
            onNodeWithText(activity.getString(R.string.profile_option_settings)).performClick()

            waitUntil { checker.isCalled }
        }
    }

    @Test
    fun showsUpgradeButtonIfPlanIsFreeAndUpgradeIsAvailable() {
        setupPlan(TestConstants.FreePlanType, true)

        val checker = CallChecker<Unit>()

        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    ProfileScreen(
                        onNavigateEvent = {
                            if (it == ProfileNavigation.Upgrade) {
                                checker.call()
                            }
                        },
                        enterPinSuccess = false,
                        onClearPinSuccess = {}
                    )
                }
            }

            onNodeWithText(activity.getString(CompR.string.upgrade)).assertExists().performClick()
            waitUntil { checker.isCalled }

        }
    }

    @Test
    fun doesNotShowUpgradeButtonIfPlanIsFreeAndUpgradeIsNotAvailable() {
        setupPlan(TestConstants.FreePlanType, false)
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    ProfileScreen(
                        onNavigateEvent = {},
                        enterPinSuccess = false,
                        onClearPinSuccess = {}
                    )
                }
            }

            onNodeWithText(activity.getString(CompR.string.upgrade)).assertDoesNotExist()
        }
    }

    @Test
    fun doesNotShowUpgradeButtonIfPlanIsPaidAndUpgradeIsAvailable() {
        setupPlan(PlanType.Paid.Plus("", ""), true)
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    ProfileScreen(
                        onNavigateEvent = {},
                        enterPinSuccess = false,
                        onClearPinSuccess = {}
                    )
                }
            }

            onNodeWithText(activity.getString(CompR.string.upgrade)).assertDoesNotExist()
        }
    }

    @Test
    fun onFeedbackClickCalled() {
        val checker = CallChecker<Unit>()

        composeTestRule.apply {
            setContent {
                PassTheme {
                    ProfileScreen(
                        onNavigateEvent = {
                            if (it is ProfileNavigation.Feedback) {
                                checker.call()
                            }
                        },
                        enterPinSuccess = false,
                        onClearPinSuccess = {}
                    )
                }
            }
            onNodeWithText("0.0.0").performScrollTo()
            onNodeWithText(activity.getString(R.string.profile_option_feedback)).performClick()

            waitUntil { checker.isCalled }
        }
    }

    @Test
    fun onRateAppClickCalled() {
        if (!SHOW_RATING_OPTION) return
        composeTestRule.apply {
            setContent {
                PassTheme {
                    ProfileScreen(
                        onNavigateEvent = {},
                        enterPinSuccess = false,
                        onClearPinSuccess = {}
                    )
                }
            }
            onNodeWithText("0.0.0").performScrollTo()
            onNodeWithText(activity.getString(R.string.profile_option_rating)).performClick()
        }

        intended(
            allOf(
                hasAction(Intent.ACTION_CHOOSER),
                hasExtra(
                    `is`(Intent.EXTRA_INTENT), allOf(
                        hasAction(ACTION_VIEW),
                        hasData(PASS_STORE)
                    )
                )
            )
        )
    }


    @Test
    fun onImportExportClickCalled() {
        composeTestRule.apply {
            setContent {
                PassTheme {
                    ProfileScreen(
                        onNavigateEvent = {},
                        enterPinSuccess = false,
                        onClearPinSuccess = {}
                    )
                }
            }
            onNodeWithText("0.0.0").performScrollTo()
            onNodeWithText(activity.getString(R.string.profile_option_import_export)).performClick()
        }

        intended(
            allOf(
                hasAction(Intent.ACTION_CHOOSER),
                hasExtra(
                    `is`(Intent.EXTRA_INTENT), allOf(
                        hasAction(ACTION_VIEW),
                        hasData(PASS_IMPORT)
                    )
                )
            )
        )
    }

    private fun setupPlan(planType: PlanType, isUpgradeAvailable: Boolean) {
        val plan = Plan(
            planType = planType,
            hideUpgrade = false,
            vaultLimit = PlanLimit.Unlimited,
            aliasLimit = PlanLimit.Unlimited,
            totpLimit = PlanLimit.Unlimited,
            updatedAt = 123
        )
        val upgradeInfo = UpgradeInfo(
            isUpgradeAvailable = isUpgradeAvailable,
            isSubscriptionAvailable = isUpgradeAvailable,
            plan = plan,
            totalVaults = 0,
            totalAlias = 0,
            totalTotp = 0
        )
        observeUpgradeInfo.setResult(upgradeInfo)
    }
}
