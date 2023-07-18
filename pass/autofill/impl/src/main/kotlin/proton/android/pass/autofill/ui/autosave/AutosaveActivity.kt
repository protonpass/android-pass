/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.autofill.ui.autosave

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import proton.android.pass.autofill.di.UserPreferenceEntryPoint
import proton.android.pass.autofill.entities.SaveInformation
import proton.android.pass.autofill.extensions.deserializeParcelable
import proton.android.pass.autofill.extensions.marshalParcelable
import proton.android.pass.common.api.Option
import proton.android.pass.commonui.api.setSecureMode
import proton.android.pass.preferences.AllowScreenshotsPreference

@AndroidEntryPoint
class AutoSaveActivity : FragmentActivity() {

    private val viewModel: AutosaveActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        setSecureMode()
        super.onCreate(savedInstanceState)
        viewModel.register(this)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collectLatest(::onStateReceived)
            }
        }

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
                        AutosaveNavigation.ForceSignOut -> {
                            viewModel.signOut()
                        }
                    }
                }
            )
        }
    }

    override fun onStop() {
        viewModel.onStop()
        super.onStop()
    }

    private fun onStateReceived(state: Option<AutosaveEvent>) {
        val event = state.value() ?: return
        if (event == AutosaveEvent.Close) {
            finishApp()
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

    private fun setSecureMode() {
        val factory = EntryPointAccessors.fromApplication(this, UserPreferenceEntryPoint::class.java)
        val repository = factory.getRepository()
        val setting = runBlocking {
            repository.getAllowScreenshotsPreference()
                .firstOrNull()
                ?: AllowScreenshotsPreference.Disabled
        }
        setSecureMode(setting)
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
