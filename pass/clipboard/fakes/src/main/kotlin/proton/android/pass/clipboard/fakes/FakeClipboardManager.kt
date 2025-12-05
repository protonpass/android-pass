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

package proton.android.pass.clipboard.fakes

import proton.android.pass.clipboard.api.ClipboardManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeClipboardManager @Inject constructor() : ClipboardManager {

    private var contents: String = ""

    fun getContents() = contents

    override fun copyToClipboard(text: String, isSecure: Boolean) {
        contents = text
    }

    override fun clearClipboard() {
        contents = ""
    }

    override fun getClipboardContent(): Result<String> = Result.success("")
}
