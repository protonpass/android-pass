package proton.android.pass.featureitemcreate.impl.login

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import kotlinx.coroutines.flow.StateFlow
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.composable
import proton.android.pass.navigation.api.toPath
import proton.pass.domain.ShareId

object CreateLogin : NavItem(
    baseRoute = "login/create",
    optionalArgIds = listOf(CommonOptionalNavArgId.ShareId)
) {
    fun createNavRoute(shareId: Option<ShareId>) = buildString {
        append(baseRoute)
        val map = mutableMapOf<String, Any>()
        if (shareId is Some) {
            map[CommonOptionalNavArgId.ShareId.key] = shareId.value.id
        }
        val path = map.toPath()
        append(path)
    }
}

@OptIn(
    ExperimentalAnimationApi::class,
    ExperimentalLifecycleComposeApi::class
)
fun NavGraphBuilder.createLoginGraph(
    initialCreateLoginUiState: InitialCreateLoginUiState = InitialCreateLoginUiState(),
    getPrimaryTotp: () -> StateFlow<String?>,
    onSuccess: (ItemUiModel) -> Unit,
    onClose: () -> Unit,
    onScanTotp: () -> Unit
) {
    composable(CreateLogin) {
        val primaryTotp by getPrimaryTotp().collectAsStateWithLifecycle()
        val initialContents = initialCreateLoginUiState.copy(
            primaryTotp = primaryTotp
        )
        CreateLoginScreen(
            initialContents = initialContents,
            onClose = onClose,
            onSuccess = onSuccess,
            onScanTotp = onScanTotp
        )
    }
}
