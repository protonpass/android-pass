package proton.android.pass.featureitemcreate.impl.alias

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun AliasAdvancedOptions(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    prefix: String,
    suffix: AliasSuffixUiModel?,
    isError: Boolean,
    canSelectSuffix: Boolean,
    showAdvancedOptionsInitially: Boolean = false,
    onPrefixChanged: (String) -> Unit,
    onSuffixClicked: () -> Unit
) {
    var showAdvancedOptions by rememberSaveable { mutableStateOf(showAdvancedOptionsInitially) }
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            ShowAdvancedOptionsButton(
                currentValue = showAdvancedOptions,
                onClick = { showAdvancedOptions = !showAdvancedOptions }
            )
        }

        AnimatedVisibility(visible = showAdvancedOptions) {
            AliasAdvancedOptionsSection(
                enabled = enabled,
                prefix = prefix,
                suffix = suffix,
                isError = isError,
                canSelectSuffix = canSelectSuffix,
                onPrefixChanged = onPrefixChanged,
                onSuffixClicked = onSuffixClicked
            )
        }
    }
}
