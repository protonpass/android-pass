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

package proton.android.pass.autofill.ui.autofill.upgrade

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AutofillUpgradeActivity : FragmentActivity() {

    private val viewModel: UpgradeActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.register(this)

        setContent {
            UpgradeDialog(
                onUpgrade = {
                    viewModel.upgrade()
                    cancelAutofill()
                },
                onClose = { cancelAutofill() }
            )
        }
    }

    private fun cancelAutofill() {
        setResult(RESULT_CANCELED)
        finish()
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, AutofillUpgradeActivity::class.java)
            .apply { setPackage(context.packageName) }
    }
}
