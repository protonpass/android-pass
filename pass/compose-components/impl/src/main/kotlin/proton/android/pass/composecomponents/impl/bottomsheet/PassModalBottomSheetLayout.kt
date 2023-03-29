package proton.android.pass.composecomponents.impl.bottomsheet

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import com.google.accompanist.navigation.material.BottomSheetNavigator
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme

@ExperimentalMaterialApi
@Composable
fun PassModalBottomSheetLayout(
    modifier: Modifier = Modifier,
    sheetContent: @Composable ColumnScope.() -> Unit,
    sheetState: ModalBottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    ),
    content: @Composable () -> Unit,
) {
    ModalBottomSheetLayout(
        modifier = modifier,
        sheetState = sheetState,
        sheetContent = sheetContent,
        sheetShape = PassTheme.shapes.bottomsheetShape,
        sheetBackgroundColor = PassTheme.colors.backgroundWeak
            .compositeOver(PassTheme.colors.backgroundNorm),
        sheetContentColor = ProtonTheme.colors.textNorm,
        scrimColor = PassTheme.colors.backdrop,
        content = content
    )
}

@OptIn(ExperimentalMaterialNavigationApi::class)
@Composable
fun PassModalBottomSheetLayout(
    bottomSheetNavigator: BottomSheetNavigator,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    ModalBottomSheetLayout(
        modifier = modifier,
        bottomSheetNavigator = bottomSheetNavigator,
        sheetShape = PassTheme.shapes.bottomsheetShape,
        sheetBackgroundColor = PassTheme.colors.backgroundWeak
            .compositeOver(PassTheme.colors.backgroundNorm),
        scrimColor = PassTheme.colors.backdrop,
        content = content
    )
}
