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

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import proton.android.pass.autofill.entities.AutofillData
import proton.android.pass.autofill.ui.autofill.AutofillActivity
import proton.android.pass.autofill.ui.autofill.inlinesuggestions.InlineSuggestionsNoUiActivity
import proton.android.pass.common.api.toOption
import proton.android.pass.domain.Item

object PendingIntentUtils {
    private val autofillPendingIntentFlags: Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
        } else {
            PendingIntent.FLAG_CANCEL_CURRENT
        }

    internal fun getOpenAppPendingIntent(
        context: Context,
        autofillData: AutofillData,
        intentRequestCode: Int,
    ): PendingIntent = PendingIntent.getActivity(
        context,
        intentRequestCode,
        AutofillActivity.newIntent(context, autofillData),
        autofillPendingIntentFlags
    )

    internal fun getSuggestionPendingIntent(
        context: Context,
        autofillData: AutofillData,
        item: Item,
        shouldAuthenticate: Boolean,
        intentRequestCode: Int,
    ): PendingIntent = PendingIntent.getActivity(
        context,
        intentRequestCode,
        getSuggestionPendingIntent(context, autofillData, item, shouldAuthenticate),
        autofillPendingIntentFlags
    )

    private fun getSuggestionPendingIntent(
        context: Context,
        autofillData: AutofillData,
        item: Item,
        shouldAuthenticate: Boolean
    ) = if (shouldAuthenticate) {
        AutofillActivity.newIntent(context, autofillData, item.toOption())
    } else {
        InlineSuggestionsNoUiActivity.newIntent(context, autofillData, item)
    }

    internal fun getLongPressInlinePendingIntent(context: Context) =
        PendingIntent.getService(
            context,
            0,
            Intent(),
            autofillPendingIntentFlags
        )
}
