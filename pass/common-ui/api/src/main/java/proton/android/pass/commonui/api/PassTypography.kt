package proton.android.pass.commonui.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.textNorm
import me.proton.core.compose.theme.textWeak

object PassTypography {
    private val heroRegular: TextStyle
        @Composable get() = TextStyle(
            fontSize = 28.sp,
            fontWeight = FontWeight.W700,
            letterSpacing = 0.01.em,
            lineHeight = 34.sp
        )

    val hero: TextStyle
        @Composable get() = hero()

    val heroWeak: TextStyle
        @Composable get() = heroWeak()

    @Composable
    fun hero(enabled: Boolean = true): TextStyle =
        heroRegular.copy(color = ProtonTheme.colors.textNorm(enabled))

    @Composable
    fun heroWeak(enabled: Boolean = true): TextStyle =
        heroRegular.copy(color = ProtonTheme.colors.textWeak(enabled))

}
