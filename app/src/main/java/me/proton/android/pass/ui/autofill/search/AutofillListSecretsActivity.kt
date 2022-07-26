package me.proton.android.pass.ui.autofill.search

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.service.autofill.Dataset
import android.view.autofill.AutofillManager
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import dagger.hilt.android.AndroidEntryPoint
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.pass.autofill.service.AutofillSecretMapper
import me.proton.core.pass.autofill.service.Constants
import me.proton.core.pass.autofill.service.entities.SearchCredentialsInfo
import me.proton.core.pass.autofill.service.entities.asAndroid
import me.proton.core.pass.autofill.service.util.fromByteArray
import me.proton.core.pass.common_secret.Secret

@RequiresApi(Build.VERSION_CODES.O)
@AndroidEntryPoint
class AutofillListSecretsActivity : ComponentActivity() {

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val info: SearchCredentialsInfo = getSearchCredentialsInfo(intent) ?: run {
            finish()
            return
        }

        // TODO: Add actual authentication method (biometrics, password, pin code, pattern, etc.)
        setContent {
            val navController = rememberAnimatedNavController()
            ProtonTheme {
                AnimatedNavHost(
                    navController,
                    startDestination = AutofillAuthenticationScreen.route,
                    modifier = Modifier.defaultMinSize(minHeight = 200.dp)
                ) {
                    composable(AutofillAuthenticationScreen.route) {
                        AutofillAuthenticationScreen.view(
                            onAuthenticated = {
                                navController.navigate(AutofillListSecretsScreen.route) {
                                    popUpTo(0)
                                }
                            }
                        )
                    }
                    composable(AutofillListSecretsScreen.route) {
                        AutofillListSecretsScreen.view(
                            packageName = info.appPackageName,
                            onSelectedCredentials = {
                                replyToAutofillService(it, info)
                            }
                        )
                    }
                }
            }
        }
    }

    private fun getSearchCredentialsInfo(intent: Intent): SearchCredentialsInfo? =
        intent.getByteArrayExtra(Constants.ARG_SEARCH_CREDENTIALS_INFO)
            ?.let { SearchCredentialsInfo.CREATOR.fromByteArray(it) }

    private fun replyToAutofillService(
        secret: Secret,
        searchCredentialsInfo: SearchCredentialsInfo,
    ) {
        val listItemId = android.R.layout.simple_list_item_1

        val datasetBuilder = Dataset.Builder()
        val mappings = AutofillSecretMapper().mapSecretsToFields(
            secret,
            searchCredentialsInfo.assistFields
        )

        mappings.forEach { value ->
            val remoteView = RemoteViews(packageName, listItemId)
            remoteView.setTextViewText(android.R.id.text1, value.displayValue)
            datasetBuilder.setValue(
                value.autofillFieldId.asAndroid().autofillId,
                AutofillValue.forText(value.contents),
                remoteView
            )
        }

        val replyIntent = Intent().apply {
            putExtra(AutofillManager.EXTRA_AUTHENTICATION_RESULT, datasetBuilder.build())
        }
        setResult(RESULT_OK, replyIntent)
        finish()
    }
}
