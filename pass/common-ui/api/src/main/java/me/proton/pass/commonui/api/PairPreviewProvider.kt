package me.proton.pass.commonui.api

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

open class PairPreviewProvider<T, U>(
    private val provider: Pair<PreviewParameterProvider<T>, PreviewParameterProvider<U>>
) : PreviewParameterProvider<Pair<T, U>> {
    override val values: Sequence<Pair<T, U>>
        get() = provider.first.values.flatMap { first ->
            provider.second.values.map { second ->
                first to second
            }
        }
}
