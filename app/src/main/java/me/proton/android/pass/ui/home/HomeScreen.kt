package me.proton.android.pass.ui.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE
import android.view.autofill.AutofillManager
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.AlertDialog
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.insets.systemBarsPadding
import me.proton.android.pass.R
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.default
import me.proton.core.compose.theme.headline
import me.proton.core.compose.theme.headlineSmall
import me.proton.core.domain.entity.UserId
import me.proton.core.pass.common_secret.SecretType
import me.proton.core.pass.common_secret.SecretValue
import me.proton.core.pass.common_secret.Secret
import me.proton.core.pass.presentation.components.common.rememberFlowWithLifecycle
import me.proton.core.pass.presentation.components.navigation.drawer.NavigationDrawer
import java.util.UUID

@ExperimentalMaterialApi
object HomeScreen {

    @Composable
    fun view(
        modifier: Modifier = Modifier,
        onDrawerStateChanged: (Boolean) -> Unit = {},
        onSignIn: (UserId?) -> Unit = {},
        onSignOut: (UserId) -> Unit = {},
        onRemove: (UserId?) -> Unit = {},
        onSwitch: (UserId) -> Unit = {},
        homeViewModel: HomeViewModel = hiltViewModel(),
    ) {
        val context = LocalContext.current
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requestAutofillAccessIfNeeded(context = context)
        }

        val homeScaffoldState = rememberHomeScaffoldState()
        val isDrawerOpen = with(homeScaffoldState.scaffoldState.drawerState) {
            isOpen && !isAnimationRunning || isClosed && isAnimationRunning
        }
        LaunchedEffect(isDrawerOpen) {
            onDrawerStateChanged(isDrawerOpen)
        }
        val drawerGesturesEnabled by homeScaffoldState.drawerGesturesEnabled

        val viewState by rememberFlowWithLifecycle(flow = homeViewModel.viewState)
            .collectAsState(initial = homeViewModel.initialViewState)

        val viewEvent = homeViewModel.viewEvent(
            navigateToSigningOut = { onRemove(null) },
        )

        Scaffold(
            modifier = modifier.systemBarsPadding(),
            scaffoldState = homeScaffoldState.scaffoldState,
            drawerContent = {
                NavigationDrawer(
                    drawerState = homeScaffoldState.scaffoldState.drawerState,
                    viewState = viewState.navigationDrawerViewState,
                    viewEvent = viewEvent.navigationDrawerViewEvent,
                    modifier = Modifier
                        .statusBarsPadding()
                        .navigationBarsPadding(),
                    onRemove = onRemove,
                    onSignIn = onSignIn,
                    onSignOut = onSignOut,
                    onSwitch = onSwitch,
                )
            },
            drawerGesturesEnabled = drawerGesturesEnabled,
            topBar = {
                TopAppBar(title = {
                    Text(stringResource(id = R.string.app_name), style = ProtonTheme.typography.headline)
                })
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { /*TODO*/ }) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_proton_plus),
                        contentDescription = stringResource(id = R.string.action_add_secret)
                    )
                }
            }
        ) { contentPadding ->
            Box {
                val secretToDelete = remember { mutableStateOf<Secret?>(null) }
                Home(
                    viewState.secrets,
                    modifier = Modifier.padding(contentPadding),
                    onDeleteSecretClicked = { secret ->
                        secretToDelete.value = secret
                    }
                )
                ConfirmSecretDeletionDialog(
                    secretState = secretToDelete,
                    onConfirm = { homeViewModel.deleteSecret(it) }
                )
            }
        }
    }
}

@Composable
private fun ConfirmSecretDeletionDialog(
    secretState: MutableState<Secret?>,
    onConfirm: (Secret) -> Unit,
) {
    val secret = secretState.value
    if (secret != null) {
        AlertDialog(
            onDismissRequest = { secretState.value = null },
            title = { Text(stringResource(R.string.alert_confirm_secret_deletion_title)) },
            text = {
                Text(stringResource(R.string.alert_confirm_secret_deletion_message, secret.name))
            },
            confirmButton = {
                TextButton(onClick = {
                    onConfirm(secret)
                    secretState.value = null
                }) {
                    Text(text = stringResource(id = R.string.presentation_alert_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { secretState.value = null }) {
                    Text(text = stringResource(id = R.string.presentation_alert_cancel))
                }
            }
        )
    }
}

@Composable
internal fun Home(
    secrets: List<Secret>,
    modifier: Modifier = Modifier,
    onDeleteSecretClicked: (Secret) -> Unit,
) {
    if (secrets.isNotEmpty()) {
        LazyColumn(modifier = modifier, contentPadding = PaddingValues(bottom = 32.dp)) {
            items(secrets) { secret ->
                SecretRow(secret = secret, onDeleteClicked = onDeleteSecretClicked)
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                "You don't have any saved credentials.",
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
internal fun SecretRow(secret: Secret, onDeleteClicked: (Secret) -> Unit) {
    val typeText = stringResource(
        when (secret.type) {
            SecretType.Username -> R.string.secret_type_username
            SecretType.Email -> R.string.secret_type_email
            SecretType.Password -> R.string.secret_type_password
            SecretType.FullName -> R.string.secret_type_full_name
            SecretType.Phone -> R.string.secret_type_phone
            SecretType.Login -> R.string.secret_type_login
            SecretType.Other -> R.string.secret_type_other
        }
    )
    Row(modifier = Modifier
        .padding(20.dp)
        .fillMaxWidth()
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = secret.name, style = ProtonTheme.typography.headlineSmall)
            when (val contents = secret.contents) {
                is SecretValue.Login -> {
                    SecretRowContents(
                        secretValue = stringResource(
                            id = R.string.item_secret_login_identity, contents.identity
                        ),
                        showContents = true)
                    SecretRowContents(
                        secretValue = stringResource(
                            id = R.string.item_secret_login_password, contents.password
                        ),
                        showContents = false)
                }
                else -> {
                    val singleValue = contents as SecretValue.Single
                    SecretRowContents(secretValue = singleValue.contents, showContents = true)
                }
            }
            Text(
                text = stringResource(id = R.string.item_secret_type_message, typeText),
                style = ProtonTheme.typography.default
            )
        }
        IconButton(
            onClick = { onDeleteClicked(secret) },
        ) {
            Icon(
                ImageVector.vectorResource(R.drawable.ic_baseline_delete_24),
                contentDescription = stringResource(id = R.string.action_delete)
            )
        }
    }
}

@Composable
internal fun SecretRowContents(secretValue: String, showContents: Boolean) {
    val contents = if (showContents)
        secretValue else
        "*******"
    Text(text = contents, style = ProtonTheme.typography.default)
}

@Stable
@ExperimentalMaterialApi
data class HomeScaffoldState(
    val scaffoldState: ScaffoldState,
    val drawerGesturesEnabled: MutableState<Boolean>,
)

@Composable
@ExperimentalMaterialApi
fun rememberHomeScaffoldState(
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    drawerGesturesEnabled: MutableState<Boolean> = mutableStateOf(true),
): HomeScaffoldState = remember {
    HomeScaffoldState(
        scaffoldState,
        drawerGesturesEnabled,
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun requestAutofillAccessIfNeeded(context: Context) {
    val autofillManager = context.getSystemService(AutofillManager::class.java)
    if (!autofillManager.hasEnabledAutofillServices()) {
        LaunchedEffect(true) {
            val intent = Intent(ACTION_REQUEST_SET_AUTOFILL_SERVICE)
            intent.data = Uri.parse("package:${context.packageName}")
            context.startActivity(intent)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview_Home() {
    val secrets = listOf(
        Secret(
            UUID.randomUUID().toString(),
            "user_1",
            "address_1",
            "Login secret",
            SecretType.Login,
            false,
            SecretValue.Login("Username", "Password"),
            listOf("me.proton.android.pass")
        ),
        Secret(
            UUID.randomUUID().toString(),
            "user_1",
            "address_1",
            "Full name secret",
            SecretType.FullName,
            false,
            SecretValue.Single("Jorge Martín Espinosa"),
            listOf("me.proton.android.pass")
        )
    )
    Home(secrets = secrets, onDeleteSecretClicked = {})
}
