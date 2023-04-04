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
