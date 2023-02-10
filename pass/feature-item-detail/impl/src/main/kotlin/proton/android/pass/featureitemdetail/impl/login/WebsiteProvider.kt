package proton.android.pass.featureitemdetail.impl.login

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class WebsiteProvider : PreviewParameterProvider<List<String>> {
    override val values: Sequence<List<String>>
        get() = sequenceOf(
            emptyList(),
            listOf("http://test.local"),
            listOf("http://test.local", "http://other.local")
        )
}
