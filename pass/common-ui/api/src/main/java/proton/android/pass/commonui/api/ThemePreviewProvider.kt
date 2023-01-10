package proton.android.pass.commonui.api

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class ThemePreviewProvider : PreviewParameterProvider<Boolean> {
    override val values: Sequence<Boolean> = sequenceOf(true, false)
}

open class ThemePairPreviewProvider<T>(
    private val provider: PreviewParameterProvider<T>
) : PreviewParameterProvider<Pair<Boolean, T>> {
    private val themePreviewProvider = ThemePreviewProvider()

    override val values: Sequence<Pair<Boolean, T>>
        get() = themePreviewProvider.values
            .flatMap { first ->
                provider.values
                    .map { second ->
                        first to second
                    }
            }
}
