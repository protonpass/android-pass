package proton.android.pass.autofill

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import proton.android.pass.autofill.entities.AutofillData
import proton.android.pass.autofill.ui.autofill.AutofillActivity
import proton.android.pass.autofill.ui.autofill.inlinesuggestions.InlineSuggestionsNoUiActivity
import proton.android.pass.common.api.toOption
import proton.pass.domain.Item

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

    internal fun getInlineSuggestionPendingIntent(
        context: Context,
        autofillData: AutofillData,
        item: Item,
        shouldAuthenticate: Boolean,
        intentRequestCode: Int,
    ): PendingIntent = PendingIntent.getActivity(
        context,
        intentRequestCode,
        getInlineSuggestionPendingIntent(context, autofillData, item, shouldAuthenticate),
        autofillPendingIntentFlags
    )

    private fun getInlineSuggestionPendingIntent(
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
