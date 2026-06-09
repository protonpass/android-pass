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

package proton.android.pass.features.itemcreate.login

import proton.android.pass.common.api.removeAccents
import proton.android.pass.data.api.usecases.popularservices.PopularService

internal const val POPULAR_SERVICE_SUGGESTIONS_LIMIT = 5

/**
 * Returns the popular services whose title starts with [query] (accent- and case-insensitive),
 * capped at [limit]. Prefix matching keeps short queries (e.g. "a", "fa") from returning too many
 * results. Empty only when the query is blank; an exact title match (e.g. "adobe") is still
 * returned so it shows as a suggestion. Dismissing the popup after the user picks a service is
 * handled by the caller, not here.
 */
internal fun matchPopularServices(
    query: String,
    services: List<PopularService>,
    limit: Int = POPULAR_SERVICE_SUGGESTIONS_LIMIT
): List<PopularService> {
    val trimmedQuery = query.trim()
    if (trimmedQuery.isEmpty()) return emptyList()

    val normalizedQuery = trimmedQuery.preprocess()
    return services
        .filter { it.title.preprocess().startsWith(normalizedQuery) }
        .take(limit)
}

private fun String.preprocess(): String = lowercase().removeAccents()
