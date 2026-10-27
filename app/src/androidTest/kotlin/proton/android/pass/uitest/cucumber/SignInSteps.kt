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

package proton.android.pass.uitest.cucumber

import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import dagger.hilt.android.testing.HiltAndroidTest
import io.cucumber.java.After
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.auth.domain.feature.IsLoginTwoStepEnabled
import me.proton.core.auth.domain.testing.LoginTestHelper
import me.proton.core.auth.test.robot.AddAccountRobot
import me.proton.core.auth.test.robot.login.LoginRobot
import me.proton.core.auth.test.robot.signup.CongratsRobot
import me.proton.core.auth.test.robot.signup.SignupExternal
import me.proton.core.humanverification.test.robot.HvCodeRobot
import me.proton.core.network.domain.client.ExtraHeaderProvider
import me.proton.core.test.android.robots.auth.login.TwoFaRobot
import me.proton.core.test.quark.data.User
import me.proton.core.util.kotlin.random
import me.proton.test.fusion.FusionConfig
import org.junit.Assume.assumeTrue
import proton.android.pass.features.home.HomeScreenTestTag
import proton.android.pass.uitest.TestUsers
import proton.android.pass.uitest.robot.HomeRobot
import proton.android.pass.uitest.robot.TIMEOUT_MILLIS
import proton.android.pass.uitest.robot.string
import java.nio.ByteBuffer
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import proton.android.pass.composecomponents.impl.R as CompR
import proton.android.pass.features.home.R as HomeR

@HiltAndroidTest
class SignInSteps {

    @Inject
    lateinit var loginTestHelper: LoginTestHelper

    @Inject
    lateinit var accountManager: AccountManager

    @Inject
    lateinit var isLoginTwoStepEnabled: IsLoginTwoStepEnabled

    @Inject
    lateinit var extraHeaderProvider: ExtraHeaderProvider

    private var acceptsExternalAccounts = false

    private var secondFactorMethod: String = OTP_METHOD

    private val composeRule
        get() = FusionConfig.Compose.testRule.get()

    @Given("I am signed out")
    fun signedOut() {
        loginTestHelper.logoutAll()
        AddAccountRobot.uiElementsDisplayed()
    }

    @Given("I am a new user on the sign-up screen")
    fun newUserOnSignUpScreen() {
        signedOut()
        extraHeaderProvider.addHeaders(ACCEPT_EXTERNAL_HEADER to "true")
        acceptsExternalAccounts = true
        AddAccountRobot.clickSignUp()
        SignupExternal.robotDisplayed()
    }

    @After
    fun restoreHeaders() {
        if (acceptsExternalAccounts) {
            extraHeaderProvider.removeFirst(ACCEPT_EXTERNAL_HEADER)
            acceptsExternalAccounts = false
        }
    }

    @Given("I have an account with a valid password")
    fun accountWithValidPassword() {
        check(TestUsers.totp.name.isNotEmpty()) { "No test account configured" }
    }

    @Given("two-factor authentication is enabled on the account")
    fun twoFactorEnabled() {
        check(TestUsers.totp.twoFa.isNotEmpty()) { "The test account has no second factor secret" }
    }

    @Given("my account has enabled {} as a second factor")
    fun accountSecondFactor(method: String) {
        secondFactorMethod = method
        assumeTrue("Cannot drive a $method on an emulator", method == OTP_METHOD)
    }

    @When("I sign up with a new Proton address and a valid password")
    fun signUp() {
        val username = "test-${String.random(USERNAME_LENGTH)}"
        SignupExternal
            .clickSwitch()
            .apply { robotDisplayed() }
            .fillEmail(username)
            .clickNext()
            .fillAndClickNext(String.random(PASSWORD_LENGTH))
            .skip()
            .skipConfirm()
        fillInternalHumanVerification(username)
        HvCodeRobot.clickVerify()
        CongratsRobot.uiElementsDisplayed()
        CongratsRobot.clickStart()
    }

    @When("I sign in with the account and its password")
    fun signIn() {
        AddAccountRobot.clickSignIn()
        LoginRobot.login(TestUsers.totp, runBlocking { isLoginTwoStepEnabled() })
    }

    @When("^I complete two-factor verification with (.+)$")
    fun completeTwoFactor(input: String) {
        val code = when (input) {
            "a valid $OTP_METHOD" -> totpCode(TestUsers.totp, offsetSteps = 0)
            "an incorrect one-time code" -> INCORRECT_CODE
            "an expired one-time code" -> totpCode(TestUsers.totp, offsetSteps = -EXPIRED_STEPS)
            else -> error("Unsupported two-factor input: $input")
        }
        TwoFaRobot().setSecondFactorInput(code).authenticate<TwoFaRobot>()
    }

    @Then("I can unlock the app")
    fun canUnlockApp() {
        composeRule.waitUntil(TIMEOUT_MILLIS) {
            composeRule.onAllNodes(hasTestTag(HomeScreenTestTag.SCREEN), useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    @Then("I am not signed in")
    fun notSignedIn() {
        val readyAccounts = runBlocking { accountManager.getAccounts().first() }
            .filter { it.state == AccountState.Ready }
        check(readyAccounts.isEmpty()) { "An account is signed in" }
    }

    @Then("I see an error that the verification failed")
    fun verificationFailedErrorShown() {
        TwoFaRobot().verify { formElementsDisplayed() }
        val errorShown = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            .wait(Until.hasObject(By.text(VERIFICATION_FAILED_MESSAGE)), TIMEOUT_MILLIS)
        check(errorShown) { "Verification error \"$VERIFICATION_FAILED_MESSAGE\" was not displayed" }
    }

    @Then("I can try the verification again")
    fun canRetryVerification() {
        TwoFaRobot().verify { formElementsDisplayed() }
    }

    @Then("I see the vault {string}")
    fun seeVault(vaultName: String) {
        HomeRobot.openDrawer(VAULT_CREATION_TIMEOUT_MILLIS).assertVaultShown(vaultName)
    }

    @Then("the vault {string} contains no items")
    fun vaultContainsNoItems(vaultName: String) {
        check(vaultName.isNotBlank())
        val title = string(HomeR.string.home_empty_vault_title)
        composeRule.waitUntil(TIMEOUT_MILLIS) {
            composeRule.onAllNodes(hasText(title), useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Then("I see the option to create a new item")
    fun createItemOptionShown() {
        val title = string(CompR.string.bottom_bar_add_item_icon_content_description)
        composeRule.waitUntil(TIMEOUT_MILLIS) {
            composeRule.onAllNodes(hasContentDescription(title), useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    private fun totpCode(user: User, offsetSteps: Long): String {
        val counter = System.currentTimeMillis() / MILLIS_PER_STEP + offsetSteps
        val message = ByteBuffer.allocate(Long.SIZE_BYTES).putLong(counter).array()
        val key = SecretKeySpec(decodeBase32(user.twoFa), HMAC_ALGORITHM)
        val mac = Mac.getInstance(HMAC_ALGORITHM).apply { init(key) }
        val hash = mac.doFinal(message)
        val offset = hash.last().toInt() and 0x0F
        val binary = ByteBuffer.wrap(hash, offset, Int.SIZE_BYTES).int and Int.MAX_VALUE
        return (binary % CODE_MODULUS).toString().padStart(CODE_DIGITS, '0')
    }

    private fun decodeBase32(secret: String): ByteArray {
        val bits = secret.uppercase()
            .filter { it in BASE32_ALPHABET }
            .map { BASE32_ALPHABET.indexOf(it).toString(2).padStart(BASE32_BITS, '0') }
            .joinToString("")
        return bits.chunked(Byte.SIZE_BITS)
            .filter { it.length == Byte.SIZE_BITS }
            .map { it.toInt(2).toByte() }
            .toByteArray()
    }

    private companion object {
        const val ACCEPT_EXTERNAL_HEADER = "X-Accept-ExtAcc"
        const val USERNAME_LENGTH = 15L
        const val OTP_METHOD = "OTP"
        const val INCORRECT_CODE = "000000"
        const val VERIFICATION_FAILED_MESSAGE = "Incorrect login credentials. Please try again."
        const val PASSWORD_LENGTH = 12L
        const val VAULT_CREATION_TIMEOUT_MILLIS = 90_000L
        const val MILLIS_PER_STEP = 30_000L
        const val EXPIRED_STEPS = 10L
        const val HMAC_ALGORITHM = "HmacSHA1"
        const val CODE_DIGITS = 6
        const val CODE_MODULUS = 1_000_000
        const val BASE32_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
        const val BASE32_BITS = 5
    }
}
