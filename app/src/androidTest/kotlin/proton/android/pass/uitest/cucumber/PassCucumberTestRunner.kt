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

import android.app.Application
import android.content.Context
import androidx.work.testing.WorkManagerTestInitHelper
import dagger.hilt.android.testing.HiltTestApplication
import io.cucumber.android.runner.CucumberAndroidJUnitRunner

class PassCucumberTestRunner : CucumberAndroidJUnitRunner() {

    override fun newApplication(cl: ClassLoader?, className: String?, context: Context?): Application =
        super.newApplication(cl, HiltTestApplication::class.java.name, context)

    override fun callApplicationOnCreate(app: Application?) {
        super.callApplicationOnCreate(app)
        app?.let { WorkManagerTestInitHelper.initializeTestWorkManager(it) }
    }
}
