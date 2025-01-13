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

private const val THEME_SYSTEM = 1
private const val THEME_LIGHT = 2
private const val THEME_DARK = 3

enum class ThemePreference {
    Light,
    Dark,
    System;

    fun value(): Int = when (this) {
        System -> THEME_SYSTEM
        Light -> THEME_LIGHT
        Dark -> THEME_DARK
    }

    companion object {
        fun from(value: Int): ThemePreference = when (value) {
            THEME_SYSTEM -> System
            THEME_LIGHT -> Light
            THEME_DARK -> Dark
            else -> System
        }
    }
}
