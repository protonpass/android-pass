package proton.android.pass.data.api.usecases

import kotlinx.coroutines.flow.Flow

interface ObserveMFACount {
    operator fun invoke(): Flow<Int>
}
