package me.proton.core.pass.autofill.ui.autosave

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import me.proton.core.pass.autofill.entities.SaveInformation
import me.proton.core.pass.autofill.extensions.deserializeParcelable
import me.proton.core.pass.autofill.extensions.marshalParcelable

@AndroidEntryPoint
class AutosaveActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val saveInformation = getSaveInformation() ?: run {
            finish()
            return
        }

        setContent {
            AutosaveApp(
                info = saveInformation,
                onFinished = { onFinished() }
            )
        }
    }

    private fun getSaveInformation(): SaveInformation? =
        intent?.extras?.getByteArray(ARG_SAVE_INFORMATION)?.deserializeParcelable()

    private fun onFinished() {
        finish()
    }

    companion object {
        private const val ARG_SAVE_INFORMATION = "arg_save_information"

        fun newIntent(context: Context, saveInformation: SaveInformation): Intent {
            val extras = Bundle().apply {
                putByteArray(ARG_SAVE_INFORMATION, marshalParcelable(saveInformation))
            }

            val intent = Intent(context, AutosaveActivity::class.java).apply {
                putExtras(extras)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
            }
            return intent
        }
    }
}
