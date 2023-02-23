package proton.android.pass.data.api.usecases

import kotlinx.coroutines.flow.Flow

data class ImageResponse(
    val content: ByteArray,
    val mimeType: String?
)

interface RequestImage {
    operator fun invoke(domain: String): Flow<ImageResponse?>
}
