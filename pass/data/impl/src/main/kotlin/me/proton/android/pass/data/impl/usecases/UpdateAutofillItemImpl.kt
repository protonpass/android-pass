package me.proton.android.pass.data.impl.usecases

import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import me.proton.android.pass.data.api.usecases.UpdateAutofillItem
import me.proton.android.pass.data.api.usecases.UpdateAutofillItemData
import me.proton.android.pass.data.impl.work.UpdateAutofillItemWorker
import me.proton.android.pass.log.api.PassLogger
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ShareId
import javax.inject.Inject

class UpdateAutofillItemImpl @Inject constructor(
    private val workManager: WorkManager
) : UpdateAutofillItem {

    override fun invoke(shareId: ShareId, itemId: ItemId, data: UpdateAutofillItemData) {
        workManager.enqueue(
            OneTimeWorkRequestBuilder<UpdateAutofillItemWorker>()
                .setInputData(
                    UpdateAutofillItemWorker.create(shareId, itemId, data)
                )
                .build()
        )
        PassLogger.i(TAG, "Scheduled UpdateAutofillItem")
    }

    companion object {
        private const val TAG = "UpdateAutofillItem"
    }
}

