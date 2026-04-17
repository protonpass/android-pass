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

package proton.android.pass.features.itemcreate.login.autofillsuggestions

import androidx.navigation.NavType
import proton.android.pass.domain.AutofillUrlMode
import proton.android.pass.navigation.api.NavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.NavParamEncoder

const val AUTOFILL_URL_INDEX_KEY = "autofill_url_index"
const val AUTOFILL_URL_MODE_KEY = "autofill_url_mode"

private const val AUTOFILL_URL_INDEX_ARG = "autofillUrlIndex"
private const val AUTOFILL_ENCODED_URL_ARG = "autofillEncodedUrl"
private const val AUTOFILL_CURRENT_MODE_ARG = "autofillCurrentMode"

internal const val ARG_AUTOFILL_URL_INDEX = AUTOFILL_URL_INDEX_ARG
internal const val ARG_AUTOFILL_ENCODED_URL = AUTOFILL_ENCODED_URL_ARG
internal const val ARG_AUTOFILL_CURRENT_MODE = AUTOFILL_CURRENT_MODE_ARG

private object AutofillUrlIndexArg : NavArgId {
    override val key = AUTOFILL_URL_INDEX_ARG
    override val navType = NavType.IntType
}

private object AutofillEncodedUrlArg : NavArgId {
    override val key = AUTOFILL_ENCODED_URL_ARG
    override val navType = NavType.StringType
}

private object AutofillCurrentModeArg : NavArgId {
    override val key = AUTOFILL_CURRENT_MODE_ARG
    override val navType = NavType.StringType
}

object AutofillUrlSuggestionsNavItem : NavItem(
    baseRoute = "login/autofill_suggestions",
    navArgIds = listOf(AutofillUrlIndexArg, AutofillEncodedUrlArg, AutofillCurrentModeArg)
) {
    fun createNavRoute(
        url: String,
        urlIndex: Int,
        mode: AutofillUrlMode
    ): String = "$baseRoute/$urlIndex/${NavParamEncoder.encode(url.ifBlank { " " })}/${mode.name}"
}
