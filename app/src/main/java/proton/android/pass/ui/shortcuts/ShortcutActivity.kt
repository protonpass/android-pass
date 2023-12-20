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

package proton.android.pass.ui.shortcuts

import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import proton.android.pass.log.api.PassLogger

@AndroidEntryPoint
class ShortcutActivity : FragmentActivity() {

    private val shortcutViewModel: ShortcutViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                shortcutViewModel.closeState.collectLatest(::onCloseReceived)
            }
        }

        val shortcutAction = intent.extras?.getString("shortcutaction")
        PassLogger.i(TAG, "Started from shortcut $shortcutAction")
        when (shortcutAction) {
            "sharelogs" -> {
                shortcutViewModel.onShareLogs(this)
            }

            else -> finish()
        }
    }

    private fun onCloseReceived(close: Boolean) {
        if (close) {
            finish()
        }
    }

    companion object {
        private const val TAG = "ShortcutActivity"
    }
}
