package proton.android.pass.featurecreateitem.impl

import androidx.compose.runtime.Stable

@Stable
sealed interface OpenScanState {
    object Unknown : OpenScanState
    object Success : OpenScanState
}
