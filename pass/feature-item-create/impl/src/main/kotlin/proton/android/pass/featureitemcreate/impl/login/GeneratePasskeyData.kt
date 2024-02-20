/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.featureitemcreate.impl.login

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.some

data class GeneratePasskeyData(
    val origin: String,
    val request: String
)

val GeneratePasskeyDataStateSaver: Saver<Option<GeneratePasskeyData>, Any> = run {
    val origin = "origin"
    val request = "request"
    mapSaver(
        save = {
            when (it) {
                is Some -> mapOf(
                    origin to it.value.origin,
                    request to it.value.request
                )
                else -> emptyMap()
            }
        },
        restore = { values ->
            if (values.isNotEmpty()) {
                if (values[origin] != null && values[request] != null) {
                    GeneratePasskeyData(
                        origin = values[origin] as String,
                        request = values[request] as String
                    ).some()
                } else {
                    None
                }
            } else {
                None
            }
        }
    )
}
