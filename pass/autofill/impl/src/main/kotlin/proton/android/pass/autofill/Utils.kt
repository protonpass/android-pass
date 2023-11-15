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

package proton.android.pass.autofill

import android.app.assist.AssistStructure
import android.service.autofill.FillContext
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.data.api.url.UrlSanitizer

object Utils {

    fun getApplicationPackageName(windowNode: AssistStructure.WindowNode): String {
        val wholePackageName = windowNode.title
        val packageComponents = wholePackageName.split("/")
        return packageComponents.first()
    }

    fun getWindowNodes(fillContexts: List<FillContext>): List<AssistStructure.WindowNode> {
        val fillContext = fillContexts
            .lastOrNull { !it.structure.activityComponent.className.contains("PopupWindow") }
            ?: return emptyList()
        val structure: AssistStructure = fillContext.structure
        return if (structure.windowNodeCount > 0)
            (0 until structure.windowNodeCount).map { structure.getWindowNodeAt(it) } else
            emptyList()
    }

    fun getTitle(
        urlOption: Option<String>,
        appNameOption: Option<String>
    ): String = when (urlOption) {
        None -> when (appNameOption) {
            None -> ""
            is Some -> appNameOption.value() ?: ""
        }

        is Some -> UrlSanitizer.getDomain(urlOption.value).getOrDefault("")
    }
}
