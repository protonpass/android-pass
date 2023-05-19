package proton.android.pass.data.impl.migration

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import proton.android.pass.log.api.PassLogger

@HiltWorker
open class DataMigrationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val dataMigrator: DataMigrator
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result =
        dataMigrator.run()
            .fold(
                onSuccess = {
                    PassLogger.i(TAG, "Data migration worker completed")
                    Result.success()
                },
                onFailure = {
                    PassLogger.w(TAG, it, "Data migration worker failed")
                    Result.failure()
                }
            )

    companion object {
        private const val TAG = "DataMigrationWorker"
    }
}
