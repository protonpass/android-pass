package me.proton.pass.autofill

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import me.proton.pass.autofill.entities.AutofillData
import me.proton.pass.autofill.ui.autofill.AutofillActivity

object PendingIntentUtils {
    private val autofillPendingIntentFlags: Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
        } else {
            PendingIntent.FLAG_CANCEL_CURRENT
        }

    internal fun getOpenAppPendingIntent(
        context: Context,
        autofillData: AutofillData
    ): PendingIntent = PendingIntent.getActivity(
        context,
        AutofillActivity.REQUEST_CODE,
        AutofillActivity.newIntent(context, autofillData),
        autofillPendingIntentFlags
    )

    internal fun getEmptyPendingIntent(context: Context) =
        PendingIntent.getService(
            context,
            0,
            Intent(),
            autofillPendingIntentFlags
        )
}
