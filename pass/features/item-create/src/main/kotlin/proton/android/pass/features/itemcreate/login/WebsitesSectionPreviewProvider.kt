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

package proton.android.pass.features.itemcreate.login

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

class WebsitesSectionPreviewProvider(val withErrors: Boolean = false) :
    PreviewParameterProvider<WebsitesPreviewParameter> {
    override val values: Sequence<WebsitesPreviewParameter>
        get() = sequence {
            for (isEditAllowed in listOf(true, false)) {
                for (websiteList in websites) {
                    yield(
                        WebsitesPreviewParameter(
                            websites = websiteList,
                            websitesWithErrors = if (withErrors) {
                                websiteList.indices.toImmutableList()
                            } else {
                                persistentListOf()
                            },
                            isEditAllowed = isEditAllowed
                        )
                    )
                }
            }
        }

    private val websites = listOf(
        persistentListOf(),
        persistentListOf("https://one.website"),
        persistentListOf("https://one.website", "https://two.websites")
    )
}

data class WebsitesPreviewParameter(
    val websites: ImmutableList<String>,
    val websitesWithErrors: ImmutableList<Int>,
    val isEditAllowed: Boolean
)
