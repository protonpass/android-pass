package proton.android.pass.composecomponents.impl.bottomsheet

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.composecomponents.impl.PassDimens

@ExperimentalMaterialApi
@Composable
fun PassModalBottomSheetLayout(
    modifier: Modifier = Modifier,
    sheetContent: @Composable ColumnScope.() -> Unit,
    sheetState: ModalBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden),
    content: @Composable () -> Unit,
) {
    ModalBottomSheetLayout(
        modifier = modifier,
        sheetState = sheetState,
        sheetContent = sheetContent,
        sheetShape = RoundedCornerShape(
            topStart = PassDimens.DefaultCornerRadius,
            topEnd = PassDimens.DefaultCornerRadius,
            bottomStart = 0.dp,
            bottomEnd = 0.dp
        ),
        sheetBackgroundColor = ProtonTheme.colors.backgroundNorm,
        sheetContentColor = ProtonTheme.colors.textNorm,
        scrimColor = ProtonTheme.colors.blenderNorm,
        content = content
    )
}
