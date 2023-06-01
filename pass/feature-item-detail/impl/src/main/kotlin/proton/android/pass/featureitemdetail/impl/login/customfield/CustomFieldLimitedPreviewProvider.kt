package proton.android.pass.featureitemdetail.impl.login.customfield

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.featureitemdetail.impl.login.CustomFieldUiContent

class CustomFieldLimitedPreviewProvider : PreviewParameterProvider<CustomFieldUiContent.Limited> {
    override val values: Sequence<CustomFieldUiContent.Limited>
        get() = sequenceOf(
            CustomFieldUiContent.Limited.Totp(label = "a totp field"),
            CustomFieldUiContent.Limited.Text(label = "a text field"),
            CustomFieldUiContent.Limited.Hidden(label = "a hidden field"),
        )
}

class ThemedCFLimitedPRovider :
    ThemePairPreviewProvider<CustomFieldUiContent.Limited>(CustomFieldLimitedPreviewProvider())
