/*
 * Copyright (c) 2022 Proton Technologies AG
 * This file is part of Proton Technologies AG and Proton Mail.
 *
 * Proton Mail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Mail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Mail. If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.initializer

import android.content.Context
import androidx.startup.AppInitializer
import androidx.startup.Initializer
import me.proton.core.humanverification.presentation.HumanVerificationInitializer

class MainInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        // No-op needed
    }

    override fun dependencies() = listOf(
        EventManagerInitializer::class.java,
        HumanVerificationInitializer::class.java,
    )

    companion object {

        fun init(appContext: Context) {
            with(AppInitializer.getInstance(appContext)) {
                // WorkManager need to be initialized before any other dependant initializer.
                initializeComponent(WorkManagerInitializer::class.java)
                initializeComponent(MainInitializer::class.java)
            }
        }
    }
}
