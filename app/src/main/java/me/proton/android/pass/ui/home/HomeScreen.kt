package me.proton.android.pass.ui.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE
import android.view.autofill.AutofillManager
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.appcompat.view.ContextThemeWrapper
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
import androidx.compose.material.MaterialTheme
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
import androidx.navigation.NavHostController
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.insets.systemBarsPadding
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.proton.android.pass.R
import me.proton.android.pass.ui.launcher.AccountViewModel
import me.proton.core.accountmanager.presentation.view.AccountPrimaryView
import me.proton.core.accountmanager.presentation.viewmodel.AccountSwitcherViewModel
import me.proton.core.domain.entity.UserId
import me.proton.core.pass.common_secret.SecretType
import me.proton.core.pass.common_secret.SecretValue
import me.proton.core.pass.common_secret.Secret
import me.proton.core.pass.presentation.components.common.rememberFlowWithLifecycle
import me.proton.core.pass.presentation.components.navigation.drawer.NavigationDrawer
import java.util.UUID

@ExperimentalMaterialApi
object HomeScreen {
    operator fun invoke(userId: UserId) = "home/${userId.id}"
    const val route = "home/{userId}"
    const val userId = "userId"

    @Composable
    fun view(
        userId: UserId,
        navController: NavHostController,
        modifier: Modifier = Modifier,
        onDrawerStateChanged: (Boolean) -> Unit,
        accountViewModel: AccountViewModel,
        homeViewModel: HomeViewModel = hiltViewModel(),
    ) {
        val context = LocalContext.current
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requestAutofillAccessIfNeeded(context = context)
        }

        val accountPrimaryScope = rememberCoroutineScope()

        val homeScaffoldState = rememberHomeScaffoldState()
        val isDrawerOpen = with(homeScaffoldState.scaffoldState.drawerState) {
            isOpen && !isAnimationRunning || isClosed && isAnimationRunning
        }
        LaunchedEffect(isDrawerOpen) {
            onDrawerStateChanged(isDrawerOpen)
        }
        val drawerGesturesEnabled by homeScaffoldState.drawerGesturesEnabled

        val homeViewModel = remember { homeViewModel }
        val viewState by rememberFlowWithLifecycle(flow = homeViewModel.viewState)
            .collectAsState(initial = homeViewModel.initialViewState)

        val viewEvent = homeViewModel.viewEvent(
            navigateToSigningOut = {
               accountPrimaryScope.launch {
                   val currentUserId = accountViewModel.getPrimaryUserId().firstOrNull()
                   currentUserId?.let { accountViewModel.signOut(it) }
               }
            },
        )

        val accountSwitcherViewModel: AccountSwitcherViewModel = hiltViewModel()
        val accountPrimaryView = remember {
            accountSwitcherViewModel.onAction().onEach {
                when (it) {
                    is AccountSwitcherViewModel.Action.Add -> accountViewModel.signIn()
                    is AccountSwitcherViewModel.Action.SignIn -> accountViewModel.signIn(it.account.username)
                    is AccountSwitcherViewModel.Action.SignOut -> accountViewModel.signOut(it.account.userId)
                    is AccountSwitcherViewModel.Action.Remove -> accountViewModel.remove(it.account.userId)
                    is AccountSwitcherViewModel.Action.SetPrimary -> accountViewModel.setAsPrimary(it.account.userId)
                }
            }.launchIn(accountPrimaryScope)
            val themedContext = ContextThemeWrapper(context, R.style.ProtonTheme)
            AccountPrimaryView(themedContext).also {
                it.setViewModel(accountSwitcherViewModel)
            }
        }

        Scaffold(
            modifier = modifier.systemBarsPadding(),
            scaffoldState = homeScaffoldState.scaffoldState,
            drawerContent = {
                NavigationDrawer(
                    accountPrimaryView,
                    drawerState = homeScaffoldState.scaffoldState.drawerState,
                    viewState = viewState.navigationDrawerViewState,
                    viewEvent = viewEvent.navigationDrawerViewEvent,
                    modifier = Modifier
                        .statusBarsPadding()
                        .navigationBarsPadding(),
                )
            },
            drawerGesturesEnabled = drawerGesturesEnabled,
            topBar = {
                TopAppBar(title = {
                    Text(stringResource(id = R.string.app_name), style = MaterialTheme.typography.h6)
                })
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { /*TODO*/ }) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_plus),
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
            Text(text = secret.name, style = MaterialTheme.typography.h5)
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
                style = MaterialTheme.typography.body2
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
    Text(text = contents, style = MaterialTheme.typography.body1)
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
