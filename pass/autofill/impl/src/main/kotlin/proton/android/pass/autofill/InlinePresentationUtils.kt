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

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.autofill.InlinePresentation
import android.widget.inline.InlinePresentationSpec
import androidx.annotation.RequiresApi
import androidx.autofill.inline.v1.InlineSuggestionUi
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.ellipsize

object InlinePresentationUtils {

    private const val TITLE_LENGTH = 20

    @SuppressLint("RestrictedApi")
    @RequiresApi(Build.VERSION_CODES.R)
    internal fun create(
        title: String,
        subtitle: Option<String> = None,
        inlinePresentationSpec: InlinePresentationSpec,
        icon: Option<Icon> = None,
        pendingIntent: PendingIntent
    ): InlinePresentation {
        val builder = InlineSuggestionUi.newContentBuilder(pendingIntent)
        builder.setContentDescription(title)
        builder.setTitle(title.ellipsize(TITLE_LENGTH))
        if (subtitle is Some) {
            builder.setSubtitle(subtitle.value)
        }
        if (icon is Some) {
            builder.setStartIcon(icon.value)
        }
        return InlinePresentation(builder.build().slice, inlinePresentationSpec, false)
    }

    @SuppressLint("RestrictedApi")
    @RequiresApi(Build.VERSION_CODES.R)
    internal fun createPinned(
        icon: Icon,
        contentDescription: String,
        inlinePresentationSpec: InlinePresentationSpec,
        pendingIntent: PendingIntent
    ): InlinePresentation {
        val builder = InlineSuggestionUi.newContentBuilder(pendingIntent)
        builder.setContentDescription(contentDescription)
        builder.setStartIcon(icon)
        return InlinePresentation(builder.build().slice, inlinePresentationSpec, true)
    }


}
