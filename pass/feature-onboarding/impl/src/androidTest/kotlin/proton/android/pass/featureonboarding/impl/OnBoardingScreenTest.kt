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

package proton.android.pass.featureonboarding.impl

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.IntSize
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.autofill.api.AutofillStatus
import proton.android.pass.autofill.api.AutofillSupportedStatus
import proton.android.pass.autofill.fakes.TestAutofillManager
import proton.android.pass.biometry.BiometryStatus
import proton.android.pass.biometry.TestBiometryManager
import proton.android.pass.data.fakes.usecases.TestObserveUserAccessData
import proton.android.pass.domain.UserAccessData
import proton.android.pass.test.CallChecker
import proton.android.pass.test.HiltComponentActivity
import javax.inject.Inject

@HiltAndroidTest
class OnBoardingScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltComponentActivity>()

    private val notNowButtonMatcher by lazy {
        hasText(composeTestRule.activity.resources.getString(R.string.on_boarding_skip))
    }

    private val fingerprintMatcher by lazy {
        hasText(composeTestRule.activity.resources.getString(R.string.on_boarding_fingerprint_button))
    }

    private val startMatcher by lazy {
        hasText(composeTestRule.activity.resources.getString(R.string.on_boarding_last_page_button))
    }

    @Inject
    lateinit var autofillManager: TestAutofillManager

    @Inject
    lateinit var biometryManager: TestBiometryManager

    @Inject
    lateinit var observeUserAccessData: TestObserveUserAccessData

    @Before
    fun setup() {
        hiltRule.inject()
        observeUserAccessData.sendValue(UserAccessData(0, 0, false, false, false))
    }

    @Test
    fun noAutofillNoBiometry() {
        autofillManager.emitStatus(AutofillSupportedStatus.Unsupported)
        biometryManager.setBiometryStatus(BiometryStatus.NotAvailable)

        val checker = CallChecker<Unit>()
        composeTestRule.setContent {
            OnBoardingScreen(
                onBoardingShown = { checker.call() },
            )
        }

        composeTestRule
            .onAllNodes(startMatcher)[0]
            .performClick()

        composeTestRule.waitUntil { checker.isCalled }
    }

    @Test
    fun yesAutofillNoBiometry() {
        autofillManager.emitStatus(AutofillSupportedStatus.Supported(AutofillStatus.EnabledByOtherService))
        biometryManager.setBiometryStatus(BiometryStatus.NotAvailable)

        val checker = CallChecker<Unit>()
        composeTestRule.setContent {
            OnBoardingScreen(
                onBoardingShown = { checker.call() },
            )
        }

        composeTestRule
            .onAllNodes(notNowButtonMatcher)[0]
            .performClick()

        composeTestRule.waitUntil {
            composeTestRule
                .onNode(startMatcher)
                .assertIsDisplayed()
                .fetchSemanticsNode()
                .size != IntSize.Zero
        }

        composeTestRule
            .onAllNodes(startMatcher)[0]
            .performClick()

        composeTestRule.waitUntil { checker.isCalled }
    }

    @Test
    fun noAutofillYesBiometry() {
        autofillManager.emitStatus(AutofillSupportedStatus.Unsupported)
        biometryManager.setBiometryStatus(BiometryStatus.CanAuthenticate)

        val checker = CallChecker<Unit>()
        composeTestRule.setContent {
            OnBoardingScreen(
                onBoardingShown = { checker.call() },
            )
        }

        composeTestRule
            .onAllNodes(notNowButtonMatcher)[0]
            .performClick()

        composeTestRule.waitUntil {
            composeTestRule
                .onNode(startMatcher)
                .assertIsDisplayed()
                .fetchSemanticsNode()
                .size != IntSize.Zero
        }

        composeTestRule
            .onAllNodes(startMatcher)[0]
            .performClick()

        composeTestRule.waitUntil { checker.isCalled }
    }

    @Test
    fun yesAutofillYesBiometry() {
        autofillManager.emitStatus(AutofillSupportedStatus.Supported(AutofillStatus.EnabledByOtherService))
        biometryManager.setBiometryStatus(BiometryStatus.CanAuthenticate)

        val checker = CallChecker<Unit>()
        composeTestRule.setContent {
            OnBoardingScreen(
                onBoardingShown = { checker.call() },
            )
        }

        composeTestRule
            .onAllNodes(notNowButtonMatcher)[0]
            .performClick()
        composeTestRule.waitUntil {
            composeTestRule
                .onNode(fingerprintMatcher)
                .assertIsDisplayed()
                .fetchSemanticsNode()
                .size != IntSize.Zero
        }
        composeTestRule
            .onAllNodes(notNowButtonMatcher)[0]
            .performClick()
        composeTestRule.waitUntil {
            composeTestRule
                .onNode(startMatcher)
                .assertIsDisplayed()
                .fetchSemanticsNode()
                .size != IntSize.Zero
        }
        composeTestRule.onNode(startMatcher).performClick()
        composeTestRule.waitUntil { checker.isCalled }
    }
}
