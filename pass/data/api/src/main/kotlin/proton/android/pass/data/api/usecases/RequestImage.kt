package proton.android.pass.data.api.usecases

import kotlinx.coroutines.flow.Flow

interface RequestImage {
    operator fun invoke(domain: String): Flow<ByteArray>
}
