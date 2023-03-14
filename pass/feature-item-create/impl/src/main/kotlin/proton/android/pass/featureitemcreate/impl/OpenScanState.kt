package proton.android.pass.featureitemcreate.impl

import androidx.compose.runtime.Stable

@Stable
sealed interface OpenScanState {
    object Unknown : OpenScanState
    object Success : OpenScanState
}
