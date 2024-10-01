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

package proton.android.pass.composecomponents.impl.utils

import androidx.compose.runtime.Composable
import java.util.Locale

private const val ONE_MINUTE_IN_SECONDS = 60

@Composable
fun passTimerText(seconds: Int, locale: Locale = Locale.getDefault()): String = String.format(
    locale = locale,
    format = "%01d:%02d",
    seconds / ONE_MINUTE_IN_SECONDS, seconds % ONE_MINUTE_IN_SECONDS
)
