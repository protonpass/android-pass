/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.preferences

private const val CLIPBOARD_NEVER = 1
private const val CLIPBOARD_60S = 2
private const val CLIPBOARD_180S = 3

enum class ClearClipboardPreference {
    Never,
    S60,
    S180;

    fun value(): Int =
        when (this) {
            Never -> CLIPBOARD_NEVER
            S60 -> CLIPBOARD_60S
            S180 -> CLIPBOARD_180S
        }

    companion object {
        fun from(value: Int): ClearClipboardPreference =
            when (value) {
                CLIPBOARD_NEVER -> Never
                CLIPBOARD_60S -> S60
                CLIPBOARD_180S -> S180
                else -> S60
            }
    }
}
