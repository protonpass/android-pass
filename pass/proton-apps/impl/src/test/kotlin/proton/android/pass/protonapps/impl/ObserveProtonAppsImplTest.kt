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

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test
import proton.android.pass.appconfig.api.BuildEnv
import proton.android.pass.appconfig.api.BuildFlavor
import proton.android.pass.appconfig.fakes.FakeAppConfig
import proton.android.pass.protonapps.api.ProtonApp
import proton.android.pass.protonapps.api.ProtonAppType

class ObserveProtonAppsImplTest {

    @Test
    fun `non-fdroid flavor emits all entries sorted by position`() = runTest {
        val observer = ObserveProtonAppsImpl(
            appInstalledChecker = FakeAppInstalledChecker(),
            appConfig = FakeAppConfig().apply { setFlavor(BuildFlavor.Play(BuildEnv.PROD)) }
        )

        observer.invoke().test {
            val apps = awaitItem()
            assertThat(apps.map(ProtonApp::type)).containsExactly(
                ProtonAppType.Vpn,
                ProtonAppType.Mail,
                ProtonAppType.Drive,
                ProtonAppType.Lumo,
                ProtonAppType.Meet
            ).inOrder()
            awaitComplete()
        }
    }

    @Test
    fun `fdroid flavor filters out entries without fdroid fallback url`() = runTest {
        val observer = ObserveProtonAppsImpl(
            appInstalledChecker = FakeAppInstalledChecker(),
            appConfig = FakeAppConfig().apply { setFlavor(BuildFlavor.Fdroid(BuildEnv.PROD)) }
        )

        observer.invoke().test {
            val apps = awaitItem()
            assertThat(apps.map(ProtonApp::type)).containsExactly(
                ProtonAppType.Vpn,
                ProtonAppType.Drive,
                ProtonAppType.Lumo
            ).inOrder()
            awaitComplete()
        }
    }

    @Test
    fun `isInstalled reflects installed checker result`() = runTest {
        val allInstalled = FakeAppInstalledChecker(installed = ProtonAppType.entries.map { it.id }.toSet())
        val observer = ObserveProtonAppsImpl(
            appInstalledChecker = allInstalled,
            appConfig = FakeAppConfig().apply { setFlavor(BuildFlavor.Play(BuildEnv.PROD)) }
        )

        observer.invoke().test {
            val apps = awaitItem()
            assertThat(apps.all { it.isInstalled }).isTrue()
            awaitComplete()
        }
    }
}
