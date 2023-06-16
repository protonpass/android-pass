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

package proton.android.pass.data.impl.usecases

import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import proton.android.pass.data.api.usecases.UpdateAutofillItem
import proton.android.pass.data.api.usecases.UpdateAutofillItemData
import proton.android.pass.data.impl.work.UpdateAutofillItemWorker
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

class UpdateAutofillItemImpl @Inject constructor(
    private val workManager: WorkManager
) : UpdateAutofillItem {

    override fun invoke(data: UpdateAutofillItemData) {
        workManager.enqueue(
            OneTimeWorkRequestBuilder<UpdateAutofillItemWorker>()
                .setInputData(
                    UpdateAutofillItemWorker.create(data)
                )
                .build()
        )
        PassLogger.i(TAG, "Scheduled UpdateAutofillItem")
    }

    companion object {
        private const val TAG = "UpdateAutofillItem"
    }
}

