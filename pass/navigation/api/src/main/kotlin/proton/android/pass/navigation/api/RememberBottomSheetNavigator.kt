package proton.android.pass.navigation.api

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.google.accompanist.navigation.material.BottomSheetNavigator
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi

@OptIn(
    ExperimentalMaterialApi::class,
    ExperimentalMaterialNavigationApi::class,
)
@Composable
fun rememberBottomSheetNavigator(
    sheetState: ModalBottomSheetState,
): BottomSheetNavigator = remember(sheetState) {
    BottomSheetNavigator(sheetState = sheetState)
}
