package proton.android.pass.featuresettings.impl

import androidx.compose.runtime.Stable

@Stable
sealed interface IsClearClipboardOptionSaved {
    object Unknown : IsClearClipboardOptionSaved
    object Success : IsClearClipboardOptionSaved
}
