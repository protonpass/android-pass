package proton.android.pass.featurecreateitem.impl.login.bottomsheet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier

@Stable
sealed interface LoginBottomSheetContent {
    object GeneratePassword : LoginBottomSheetContent
    object AliasOptions : LoginBottomSheetContent
}

@Composable
fun LoginBottomSheet(
    modifier: Modifier = Modifier,
    content: LoginBottomSheetContent,
    regeneratePassword: Boolean,
    setRegeneratePassword: (Boolean) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRemoveAliasClick: () -> Unit,
    hideBottomSheet: () -> Unit
) {
    when (content) {
        LoginBottomSheetContent.GeneratePassword -> {
            GeneratePasswordBottomSheet(
                modifier = modifier,
                regeneratePassword = regeneratePassword,
                onPasswordRegenerated = {
                    setRegeneratePassword(false)
                },
                onConfirm = { password ->
                    onPasswordChange(password)
                    hideBottomSheet()
                }
            )
        }
        LoginBottomSheetContent.AliasOptions -> {
            AliasOptionsBottomSheet(
                modifier = modifier,
                onRemoveAliasClick = onRemoveAliasClick
            )
        }
    }
}
