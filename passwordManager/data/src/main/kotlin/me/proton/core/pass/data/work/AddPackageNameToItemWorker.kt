package me.proton.core.pass.data.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import me.proton.android.pass.log.PassLogger
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.domain.entity.PackageName
import me.proton.core.pass.domain.repositories.ItemRepository

@HiltWorker
class AddPackageNameToItemWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted private val workerParameters: WorkerParameters,
    private val itemRepository: ItemRepository
) : CoroutineWorker(appContext, workerParameters) {
    override suspend fun doWork(): Result {
        PassLogger.i("AddPackageNameToItemWorker", "Starting work")
        val data = getData()
        if (data.isFailure) {
            data.exceptionOrNull()?.let {
                PassLogger.e("AddPackageNameToItemWorker", it, "Error retrieving data")
            }
            return Result.failure()
        }

        val inputData = data.getOrThrow()
        return run(inputData)
    }

    private suspend fun run(inputData: InputData): Result {
        PassLogger.d("AddPackageNameToItemWorker", "Data received: $inputData")

        try {
            PassLogger.d(
                "AddPackageNameToItemWorker",
                "Adding package to item [itemId=${inputData.itemId}] [packageName=${inputData.packageName}]"
            )
            itemRepository.addPackageToItem(
                inputData.shareId,
                inputData.itemId,
                inputData.packageName
            )
            PassLogger.i(
                "AddPackageNameToItemWorker",
                "Added package to item [itemId=${inputData.itemId}] [packageName=${inputData.packageName}]"
            )
            return Result.success()
        } catch (e: Throwable) {
            PassLogger.e(
                "AddPackageNameToItemWorker",
                e,
                "Error adding package to item [packageName=$inputData.packageName]"
            )
            return Result.failure()
        }
    }

    private fun getData(): kotlin.Result<InputData> {
        val shareId =
            workerParameters.inputData.getString(ARG_SHARE_ID) ?: return kotlin.Result.failure(
                IllegalStateException("Missing $ARG_SHARE_ID")
            )
        val itemId =
            workerParameters.inputData.getString(ARG_ITEM_ID) ?: return kotlin.Result.failure(
                IllegalStateException("Missing $ARG_ITEM_ID")
            )
        val packageName =
            workerParameters.inputData.getString(ARG_PACKAGE_NAME) ?: return kotlin.Result.failure(
                IllegalStateException("Missing $ARG_PACKAGE_NAME")
            )

        return kotlin.Result.success(
            InputData(
                shareId = ShareId(shareId),
                itemId = ItemId(itemId),
                packageName = PackageName(packageName)
            )
        )
    }

    internal data class InputData(
        val shareId: ShareId,
        val itemId: ItemId,
        val packageName: PackageName
    )

    companion object {

        private const val ARG_SHARE_ID = "arg_share_id"
        private const val ARG_ITEM_ID = "arg_item_id"
        private const val ARG_PACKAGE_NAME = "arg_package_name"

        fun create(shareId: ShareId, itemId: ItemId, packageName: PackageName): Data =
            Data.Builder()
                .putAll(
                    mapOf(
                        ARG_SHARE_ID to shareId.id,
                        ARG_ITEM_ID to itemId.id,
                        ARG_PACKAGE_NAME to packageName.packageName
                    )
                )
                .build()
    }
}
