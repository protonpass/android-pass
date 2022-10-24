package me.proton.pass.data.usecases

import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import me.proton.android.pass.log.PassLogger
import me.proton.pass.data.work.AddPackageNameToItemWorker
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ShareId
import me.proton.pass.domain.entity.PackageName
import javax.inject.Inject

interface AddPackageNameToItem {
    operator fun invoke(shareId: ShareId, itemId: ItemId, packageName: PackageName)
}

class AddPackageNameToItemImpl @Inject constructor(
    private val workManager: WorkManager
) : AddPackageNameToItem {
    override fun invoke(shareId: ShareId, itemId: ItemId, packageName: PackageName) {
        workManager.enqueue(
            OneTimeWorkRequestBuilder<AddPackageNameToItemWorker>()
                .setInputData(
                    AddPackageNameToItemWorker.create(shareId, itemId, packageName)
                )
                .build()
        )
        PassLogger.i(TAG, "Scheduled AddPackageNameToItem")
    }

    companion object {
        private const val TAG = "AddPackageNameToItem"
    }
}

