package me.proton.core.pass.autofill.ui.autofill

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.service.autofill.Dataset
import android.view.autofill.AutofillManager
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import me.proton.core.pass.autofill.entities.AutofillResponse
import me.proton.core.pass.autofill.entities.SearchCredentialsInfo
import me.proton.core.pass.autofill.entities.asAndroid
import me.proton.core.pass.autofill.extensions.deserializeParcelable
import me.proton.core.pass.autofill.extensions.marshalParcelable

@AndroidEntryPoint
class AutofillActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val info = getInfoFromExtras() ?: run {
            finish()
            return
        }
        setContent {
            AutofillApp(
                info = info,
                onAutofillResponse = { onAutofillResponse(it) }
            )
        }
    }

    private fun getInfoFromExtras(): SearchCredentialsInfo? =
        intent?.getByteArrayExtra(ARG_SEARCH_CREDENTIALS_INFO)?.deserializeParcelable()


    private fun onAutofillResponse(response: AutofillResponse?) {
        response?.let { prepareAutofillResult(it) }
        finish()
    }

    private fun prepareAutofillResult(response: AutofillResponse) {
        val dataset = generateDataset(response)
        val replyIntent = Intent().apply {
            putExtra(AutofillManager.EXTRA_AUTHENTICATION_RESULT, dataset)
        }
        setResult(RESULT_OK, replyIntent)
    }

    private fun generateDataset(response: AutofillResponse): Dataset {
        val datasetBuilder = Dataset.Builder()
        response.mappings.forEach { mapping ->
            val remoteView = RemoteViews(packageName, android.R.layout.simple_list_item_1)
            remoteView.setTextViewText(android.R.id.text1, mapping.displayValue)
            datasetBuilder.setValue(
                mapping.autofillFieldId.asAndroid().autofillId,
                AutofillValue.forText(mapping.contents),
                remoteView
            )
        }
        return datasetBuilder.build()
    }


    companion object {
        const val REQUEST_CODE = 1
        const val ARG_SEARCH_CREDENTIALS_INFO = "arg_search_credentials_info"

        fun newIntent(context: Context, credentials: SearchCredentialsInfo): Intent =
            Intent(context, AutofillActivity::class.java).apply {
                val extras = Bundle().apply {
                    putByteArray(ARG_SEARCH_CREDENTIALS_INFO, marshalParcelable(credentials))
                }
                putExtras(extras)
            }

    }
}
