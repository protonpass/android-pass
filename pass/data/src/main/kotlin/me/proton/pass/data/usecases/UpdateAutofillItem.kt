package me.proton.pass.data.usecases

import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import me.proton.android.pass.log.PassLogger
import me.proton.pass.common.api.Option
import me.proton.pass.data.work.AddPackageNameToItemWorker
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ShareId
import me.proton.pass.domain.entity.PackageName
import javax.inject.Inject

data class UpdateAutofillItemData(
    val packageName: Option<PackageName>,
    val url: Option<String>
)

interface UpdateAutofillItem {
    operator fun invoke(shareId: ShareId, itemId: ItemId, data: UpdateAutofillItemData)
}

class UpdateAutofillItemImpl @Inject constructor(
    private val workManager: WorkManager
) : UpdateAutofillItem {

    override fun invoke(shareId: ShareId, itemId: ItemId, data: UpdateAutofillItemData) {
        workManager.enqueue(
            OneTimeWorkRequestBuilder<AddPackageNameToItemWorker>()
                .setInputData(
                    AddPackageNameToItemWorker.create(shareId, itemId, data)
                )
                .build()
        )
        PassLogger.i(TAG, "Scheduled UpdateAutofillItem")
    }

    companion object {
        private const val TAG = "UpdateAutofillItem"
    }
}

