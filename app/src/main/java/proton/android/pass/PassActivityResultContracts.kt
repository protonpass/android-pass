/*
 * Copyright (c) 2024 Proton AG
 * This file is part of Proton AG and ProtonCore.
 *
 * ProtonCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import me.proton.core.domain.entity.UserId
import proton.android.pass.enterextrapassword.EnterExtraPasswordActivity

object StartExtraPassword : ActivityResultContract<UserId, Unit?>() {

    override fun createIntent(context: Context, input: UserId) =
        Intent(context, EnterExtraPasswordActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(EnterExtraPasswordActivity.EXTRA_USER_ID, input.id)
        }

    override fun parseResult(resultCode: Int, intent: Intent?): Unit? {
        if (resultCode != Activity.RESULT_OK) return null
        return Unit
    }
}
