/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.enterextrapassword

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.AndroidEntryPoint
import me.proton.core.domain.entity.UserId
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.ui.AppNavigation
import javax.inject.Inject

@AndroidEntryPoint
class EnterExtraPasswordActivity : FragmentActivity() {

    @Inject
    lateinit var snackbarDispatcher: SnackbarDispatcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val userId = intent.getStringExtra(EXTRA_USER_ID)?.let { UserId(it) } ?: run {
            PassLogger.w(TAG, "Missing user id")
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        setContent {
            EnterExtraPasswordApp(
                userId = userId,
                onNavigate = {
                    when (it) {
                        is AppNavigation.Finish -> setResultAndFinish(success = true)
                        is AppNavigation.ForceSignOut -> setResultAndFinish(success = false)
                        else -> {}
                    }
                }
            )
        }
    }

    private fun setResultAndFinish(success: Boolean) {
        snackbarDispatcher.reset()
        setResult(if (success) RESULT_OK else RESULT_CANCELED)
        finish()
    }

    companion object {
        private const val TAG = "EnterExtraPasswordActivity"
        const val EXTRA_USER_ID = "extra_user_id"
    }
}
