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

package proton.android.pass.protonapps.impl

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import proton.android.pass.appconfig.api.BuildEnv
import proton.android.pass.appconfig.api.BuildFlavor
import proton.android.pass.appconfig.fakes.FakeAppConfig
import proton.android.pass.protonapps.api.ProtonApp
import proton.android.pass.protonapps.api.ProtonAppType
import proton.android.pass.protonapps.api.usecases.OpenResult

class OpenProtonAppImplTest {

    @Test
    fun `installed app with launch intent returns OpenedApp and starts the launch intent`() {
        val launchIntent = Intent()
        val pm = FakePackageManager().apply {
            launchIntentFactory = { name -> if (name == ProtonAppType.Vpn.id) launchIntent else null }
        }
        val context = RecordingContext(pm)
        val opener = OpenProtonAppImpl(
            context = context,
            appConfig = FakeAppConfig().apply { setFlavor(BuildFlavor.Play(BuildEnv.PROD)) }
        )

        val result = opener.invoke(ProtonApp(type = ProtonAppType.Vpn, isInstalled = true))

        assertThat(result).isEqualTo(OpenResult.OpenedApp)
        assertThat(context.startActivityCallCount).isEqualTo(1)
        assertThat(context.startedIntents.single()).isSameInstanceAs(launchIntent)
    }

    @Test
    fun `non-installed on non-fdroid opens play store`() {
        val pm = FakePackageManager()
        val context = RecordingContext(pm)
        val opener = OpenProtonAppImpl(
            context = context,
            appConfig = FakeAppConfig().apply { setFlavor(BuildFlavor.Play(BuildEnv.PROD)) }
        )

        val result = opener.invoke(ProtonApp(type = ProtonAppType.Mail, isInstalled = false))

        assertThat(result).isEqualTo(OpenResult.OpenedPlayStore)
        assertThat(context.startActivityCallCount).isEqualTo(1)
    }

    @Test
    fun `non-installed on non-fdroid falls back to web when play store unavailable`() {
        val pm = FakePackageManager()
        val context = RecordingContext(pm, throwOnFirstStart = true)
        val opener = OpenProtonAppImpl(
            context = context,
            appConfig = FakeAppConfig().apply { setFlavor(BuildFlavor.Play(BuildEnv.PROD)) }
        )

        val result = opener.invoke(ProtonApp(type = ProtonAppType.Mail, isInstalled = false))

        assertThat(result).isEqualTo(OpenResult.OpenedWebFallback)
        assertThat(context.startActivityCallCount).isEqualTo(2)
    }

    @Test
    fun `non-installed on fdroid opens web fallback url`() {
        val pm = FakePackageManager()
        val context = RecordingContext(pm)
        val opener = OpenProtonAppImpl(
            context = context,
            appConfig = FakeAppConfig().apply { setFlavor(BuildFlavor.Fdroid(BuildEnv.PROD)) }
        )

        val result = opener.invoke(ProtonApp(type = ProtonAppType.Vpn, isInstalled = false))

        assertThat(result).isEqualTo(OpenResult.OpenedWebFallback)
        assertThat(context.startActivityCallCount).isEqualTo(1)
    }

    private class RecordingContext(
        private val pm: PackageManager,
        private val throwOnFirstStart: Boolean = false
    ) : ContextWrapper(null) {
        val startedIntents: MutableList<Intent> = mutableListOf()
        var startActivityCallCount: Int = 0
            private set

        override fun getPackageManager(): PackageManager = pm
        override fun getApplicationContext(): Context = this

        override fun startActivity(intent: Intent) {
            startActivityCallCount += 1
            if (throwOnFirstStart && startActivityCallCount == 1) {
                throw ActivityNotFoundException("play store unavailable")
            }
            startedIntents += intent
        }

        override fun startActivity(intent: Intent, options: android.os.Bundle?) = startActivity(intent)
    }
}
