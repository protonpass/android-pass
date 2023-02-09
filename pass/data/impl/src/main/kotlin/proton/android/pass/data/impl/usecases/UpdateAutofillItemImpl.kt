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

