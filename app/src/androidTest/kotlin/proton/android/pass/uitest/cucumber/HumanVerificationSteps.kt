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

import android.app.UiAutomation
import android.os.ParcelFileDescriptor
import android.view.KeyEvent
import android.webkit.WebView
import androidx.test.espresso.web.model.Atom
import androidx.test.espresso.web.model.SimpleAtom
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.DriverAtoms.getText
import androidx.test.espresso.web.webdriver.DriverAtoms.webClick
import androidx.test.espresso.web.webdriver.Locator
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Assert.assertNotNull

private const val WEB_VIEW_TIMEOUT_MILLIS = 60_000L
private const val WEB_ELEMENT_TIMEOUT_MILLIS = 20_000L
private const val WEB_ELEMENT_POLL_MILLIS = 500L
private const val VERIFY_XPATH = "//button[text()=\"Verify\"]"
private const val HV_CODE = "666666"
private const val HV_EMAIL_DOMAIN = "passmail.net"
private const val FOCUS_SCRIPT = "function(elem) {\nelem.focus();\n}"

fun <T> awaitWebElement(
    xpath: String,
    action: Atom<T>,
    timeoutMillis: Long = WEB_ELEMENT_TIMEOUT_MILLIS
) {
    val deadline = System.currentTimeMillis() + timeoutMillis
    while (true) {
        val failure = runCatching {
            onWebView()
                .withElement(findElement(Locator.XPATH, xpath))
                .perform(action)
        }.fold(onSuccess = { return }, onFailure = { it })
        if (System.currentTimeMillis() >= deadline) {
            throw AssertionError("Web element $xpath not usable within ${timeoutMillis / 1000} s", failure)
        }
        Thread.sleep(WEB_ELEMENT_POLL_MILLIS)
    }
}

fun fillInternalHumanVerification(username: String) {
    val instrumentation = InstrumentationRegistry.getInstrumentation()
    val device = UiDevice.getInstance(instrumentation)
    val uiAutomation = instrumentation.uiAutomation

    assertNotNull(
        "Human verification WebView not visible within ${WEB_VIEW_TIMEOUT_MILLIS / 1000} s",
        device.wait(Until.findObject(By.clazz(WebView::class.java)), WEB_VIEW_TIMEOUT_MILLIS)
    )
    onWebView().forceJavascriptEnabled()
    awaitWebElement("//*[@id=\"label_1\"]", webClick())
    repeat(2) { device.pressKeyCode(KeyEvent.KEYCODE_TAB) }
    awaitWebElement("//*[@id=\"email\"]", SimpleAtom(FOCUS_SCRIPT))
    uiAutomation.runShell("input text $username@$HV_EMAIL_DOMAIN")
    awaitWebElement("//button[text()=\"Get verification code\"]", webClick())
    awaitWebElement(VERIFY_XPATH, getText())
    uiAutomation.runShell("input text $HV_CODE")
    awaitWebElement(VERIFY_XPATH, webClick())
}

private fun UiAutomation.runShell(command: String) {
    ParcelFileDescriptor.AutoCloseInputStream(executeShellCommand(command)).use { it.readBytes() }
}
