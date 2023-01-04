package me.proton.android.pass.composecomponents.impl.topbar

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class SearchTopBarPreviewProvider :
    PreviewParameterProvider<SearchTopBarData> {
    override val values: Sequence<SearchTopBarData>
        get() = sequenceOf(
            SearchTopBarData(value = ""),
            SearchTopBarData(value = "some search")
        )
}

@JvmInline
value class SearchTopBarData(
    val value: String
)
