package me.proton.pass.autofill.ui.autofill

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.autofill.AutofillId
import android.view.autofill.AutofillManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.os.bundleOf
import dagger.hilt.android.AndroidEntryPoint
import me.proton.pass.autofill.DatasetUtils
import me.proton.pass.autofill.entities.AndroidAutofillFieldId
import me.proton.pass.autofill.entities.AutofillAppState
import me.proton.pass.autofill.entities.AutofillData
import me.proton.pass.autofill.entities.AutofillResponse
import me.proton.pass.autofill.entities.FieldType
import me.proton.pass.autofill.entities.asAndroid
import me.proton.pass.common.api.Some
import me.proton.pass.common.api.toOption
import me.proton.pass.domain.entity.PackageName

@AndroidEntryPoint
class AutofillActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val ids: List<AndroidAutofillFieldId> =
            intent.extras?.getParcelableArrayList<AutofillId>(ARG_AUTOFILL_IDS)
                ?.map { AndroidAutofillFieldId(it) }
                ?: emptyList()
        val types: List<FieldType> = intent.extras?.getStringArrayList(ARG_AUTOFILL_TYPES)
            ?.map {
                try {
                    FieldType.valueOf(it)
                } catch (_: Exception) {
                    FieldType.Unknown
                }
            }
            ?: emptyList()
        val packageName = intent.extras?.getString(ARG_PACKAGE_NAME) ?: ""
        val webDomain = intent.extras?.getString(ARG_WEB_DOMAIN).toOption()

        if (ids.isEmpty() || types.isEmpty() || packageName.isEmpty()) {
            finish()
            return
        }

        setContent {
            AutofillApp(
                state = AutofillAppState(PackageName(packageName), ids, types, webDomain),
                onAutofillResponse = { onAutofillResponse(it) }
            )
        }
    }

    private fun onAutofillResponse(response: AutofillResponse?) {
        response?.let { prepareAutofillResult(it) }
        finish()
    }

    private fun prepareAutofillResult(response: AutofillResponse) {
        val dataset = DatasetUtils.generateDataset(packageName, response)
        val replyIntent = Intent().apply {
            putExtra(AutofillManager.EXTRA_AUTHENTICATION_RESULT, dataset)
        }
        setResult(RESULT_OK, replyIntent)
    }

    companion object {
        const val REQUEST_CODE = 1
        const val ARG_AUTOFILL_IDS = "arg_autofill_ids"
        const val ARG_AUTOFILL_TYPES = "arg_autofill_types"
        const val ARG_PACKAGE_NAME = "arg_package_name"
        const val ARG_WEB_DOMAIN = "arg_web_domain"

        fun newIntent(context: Context, data: AutofillData): Intent =
            Intent(context, AutofillActivity::class.java).apply {
                if (data.assistInfo.url is Some) {
                    putExtra(ARG_WEB_DOMAIN, data.assistInfo.url.value)
                }

                val fields = data.assistInfo.fields
                putExtras(
                    bundleOf(
                        ARG_AUTOFILL_IDS to fields.map { it.id.asAndroid().autofillId },
                        ARG_AUTOFILL_TYPES to fields.map { it.type?.toString() },
                        ARG_PACKAGE_NAME to data.packageName
                    )
                )
            }
    }
}
