package proton.android.pass.data.api.usecases

import kotlinx.coroutines.flow.Flow

data class ImageResponse(
    val content: ByteArray,
    val mimeType: String?
)

sealed interface ImageResponseResult {
    data class Data(val content: ByteArray, val mimeType: String?) : ImageResponseResult
    object Empty : ImageResponseResult
    data class Error(val throwable: Throwable) : ImageResponseResult
}

interface RequestImage {
    operator fun invoke(domain: String): Flow<ImageResponseResult>
}
