package proton.android.pass.autofill.ui.autosave

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.AndroidEntryPoint
import proton.android.pass.autofill.entities.SaveInformation
import proton.android.pass.autofill.extensions.deserializeParcelable
import proton.android.pass.autofill.extensions.marshalParcelable

@AndroidEntryPoint
class AutoSaveActivity : FragmentActivity() {

    private val viewModel: AutosaveActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.register(this)

        val arguments = getArguments() ?: run {
            finishApp()
            return
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            AutoSaveApp(
                arguments = arguments,
                onNavigate = {
                    when (it) {
                        AutosaveNavigation.Success -> finishApp()
                        AutosaveNavigation.Cancel -> finishApp()
                        AutosaveNavigation.Upgrade -> { viewModel.upgrade() }
                    }
                }
            )
        }
    }

    private fun getArguments(): AutoSaveArguments? =
        intent?.extras?.let {
            AutoSaveArguments(
                saveInformation = it.getByteArray(ARG_SAVE_INFORMATION)?.deserializeParcelable() ?: return null,
                linkedAppInfo = it.getByteArray(ARG_LINKED_APP)?.deserializeParcelable(),
                title = it.getString(ARG_TITLE) ?: return null,
                website = it.getString(ARG_WEBSITE)
            )
        }

    private fun finishApp() {
        finish()
    }

    companion object {
        private const val ARG_SAVE_INFORMATION = "arg_save_information"
        private const val ARG_LINKED_APP = "arg_linked_app"
        private const val ARG_TITLE = "arg_title"
        private const val ARG_WEBSITE = "arg_website"

        fun newIntent(
            context: Context,
            saveInformation: SaveInformation,
            title: String,
            website: String?,
            linkedAppInfo: LinkedAppInfo?
        ): Intent {
            val extras = Bundle().apply {
                putByteArray(ARG_SAVE_INFORMATION, marshalParcelable(saveInformation))
                putString(ARG_TITLE, title)
                putString(ARG_WEBSITE, website)
                linkedAppInfo?.let {
                    putByteArray(ARG_LINKED_APP, marshalParcelable(it))
                }
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
