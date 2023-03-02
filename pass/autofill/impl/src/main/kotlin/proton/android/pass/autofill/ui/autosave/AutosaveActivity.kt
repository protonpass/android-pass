package proton.android.pass.autofill.ui.autosave

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.AndroidEntryPoint
import proton.android.pass.autofill.entities.SaveInformation
import proton.android.pass.autofill.extensions.deserializeParcelable
import proton.android.pass.autofill.extensions.marshalParcelable

@AndroidEntryPoint
class AutoSaveActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val saveInformation = getSaveInformation() ?: run {
            finishApp()
            return
        }

        setContent {
            AutoSaveApp(
                info = saveInformation,
                onAutoSaveSuccess = { finishApp() },
                onAutoSaveCancel = { finishApp() }
            )
        }
    }

    private fun getSaveInformation(): SaveInformation? =
        intent?.extras?.getByteArray(ARG_SAVE_INFORMATION)?.deserializeParcelable()

    private fun finishApp() {
        finish()
    }

    companion object {
        private const val ARG_SAVE_INFORMATION = "arg_save_information"

        fun newIntent(context: Context, saveInformation: SaveInformation): Intent {
            val extras = Bundle().apply {
                putByteArray(ARG_SAVE_INFORMATION, marshalParcelable(saveInformation))
            }

            val intent = Intent(context, AutoSaveActivity::class.java).apply {
                putExtras(extras)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
            }
            return intent
        }
    }
}
