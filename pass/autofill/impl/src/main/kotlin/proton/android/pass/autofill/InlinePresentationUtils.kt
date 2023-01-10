package proton.android.pass.autofill

import android.annotation.SuppressLint
import android.app.PendingIntent
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
        pendingIntent: PendingIntent
    ): InlinePresentation {
        val builder = InlineSuggestionUi.newContentBuilder(pendingIntent)
        builder.setContentDescription(title)
        builder.setTitle(title.ellipsize(TITLE_LENGTH))
        if (subtitle is Some) {
            builder.setSubtitle(subtitle.value)
        }
        return InlinePresentation(builder.build().slice, inlinePresentationSpec, false)
    }
}
