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

package proton.android.pass.appconfig.api

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import proton.android.pass.appconfig.api.BuildFlavor.Companion.passStoreUrl

class BuildFlavorPassStoreUrlTest {

    @Test
    fun `fdroid points to the f-droid listing`() {
        assertThat(BuildFlavor.Fdroid(BuildEnv.PROD).passStoreUrl())
            .isEqualTo("https://f-droid.org/packages/proton.android.pass.fdroid/")
    }

    @Test
    fun `quest points to the meta horizon listing`() {
        assertThat(BuildFlavor.Quest(BuildEnv.PROD).passStoreUrl())
            .isEqualTo(
                "https://www.meta.com/en-gb/experiences/" +
                    "proton-pass-password-manager/25447164831535276/"
            )
    }

    @Test
    fun `play points to the play store listing`() {
        assertThat(BuildFlavor.Play(BuildEnv.PROD).passStoreUrl())
            .isEqualTo("https://play.google.com/store/apps/details?id=proton.android.pass")
    }

    @Test
    fun `every other flavor falls back to the play store listing`() {
        val others = listOf(
            BuildFlavor.Dev(BuildEnv.BLACK),
            BuildFlavor.Alpha(BuildEnv.PROD),
            BuildFlavor.Nogms(BuildEnv.PROD)
        )
        others.forEach { flavor ->
            assertThat(flavor.passStoreUrl()).isEqualTo(PASS_PLAY_STORE_URL)
        }
    }
}
