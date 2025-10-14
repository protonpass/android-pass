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

package proton.android.pass.data.impl.utils

import android.content.Context
import coil.ImageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ImagePreloaderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ImagePreloader {

    override fun preloadImages(imageUrls: Set<String>) {
        val imageLoader = ImageLoader.Builder(context)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build()
        imageUrls.forEach { url ->
            val request = ImageRequest.Builder(context)
                .data(url)
                .build()
            imageLoader.enqueue(request)
        }
    }
}
