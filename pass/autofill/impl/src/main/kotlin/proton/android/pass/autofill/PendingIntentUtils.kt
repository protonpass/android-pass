package proton.android.pass.autofill

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import proton.android.pass.autofill.entities.AutofillData
import proton.android.pass.autofill.ui.autofill.AutofillActivity

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

    internal fun getLongPressInlinePendingIntent(context: Context) =
        PendingIntent.getService(
            context,
            0,
            Intent(),
            autofillPendingIntentFlags
        )
}
