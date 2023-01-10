package proton.android.pass.autofill

import android.app.PendingIntent
import android.service.autofill.InlinePresentation
import android.widget.RemoteViews
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option

data class DatasetBuilderOptions(
    val authenticateView: Option<RemoteViews> = None,
    val inlinePresentation: Option<InlinePresentation> = None,
    val pendingIntent: Option<PendingIntent> = None
)
