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

package proton.android.pass.autofill

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BrowserListTest {

    @Test
    fun `does not contain developer or debug browser builds`() {
        val devBuilds = setOf(
            "com.android.htmlviewer",
            "com.brave.browser_dev",
            "com.brave.browser_nightly",
            "com.chrome.canary",
            "com.chrome.dev",
            "com.google.android.apps.chrome_dev",
            "com.kiwibrowser.browser.dev",
            "com.microsoft.emmx.canary",
            "com.microsoft.emmx.dev",
            "net.slions.fulguris.full.download.debug",
            "net.slions.fulguris.full.playstore.debug",
            "org.chromium.webview_shell",
            "org.mozilla.fenix.nightly",
            "org.mozilla.fennec_aurora"
        )
        assertThat(BROWSERS.intersect(devBuilds)).isEmpty()
        assertThat(BROWSERS).hasSize(80)
    }
}
