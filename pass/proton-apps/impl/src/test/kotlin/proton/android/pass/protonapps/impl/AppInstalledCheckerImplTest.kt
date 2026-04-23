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

import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AppInstalledCheckerImplTest {

    @Test
    fun `check returns true when package is found`() {
        val fakePackageManager = FakePackageManager(installedPackages = setOf("com.example.installed"))
        val context = FakeContextWithPm(fakePackageManager)
        val checker = AppInstalledCheckerImpl(context)

        val result = checker.check("com.example.installed")

        assertThat(result).isTrue()
    }

    @Test
    fun `check returns false when PackageManager throws NameNotFoundException`() {
        val fakePackageManager = FakePackageManager(installedPackages = emptySet())
        val context = FakeContextWithPm(fakePackageManager)
        val checker = AppInstalledCheckerImpl(context)

        val result = checker.check("com.example.missing")

        assertThat(result).isFalse()
    }

    private class FakeContextWithPm(private val pm: PackageManager) : ContextWrapper(null) {
        override fun getPackageManager(): PackageManager = pm
        override fun getApplicationContext(): Context = this
    }
}
