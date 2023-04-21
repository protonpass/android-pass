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

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun noAutofillNoBiometry() {
        autofillManager.emitStatus(AutofillSupportedStatus.Unsupported)
        biometryManager.setBiometryStatus(BiometryStatus.NotAvailable)

        val checker = CallChecker<Unit>()
        composeTestRule.setContent {
            OnBoardingScreen(
                onBoardingShown = { checker.call() }
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
                onBoardingShown = { checker.call() }
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
                onBoardingShown = { checker.call() }
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
                onBoardingShown = { checker.call() }
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
