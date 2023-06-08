package proton.android.pass.preferences

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

object InternalSettingsSerializer : Serializer<InternalSettings> {
    override val defaultValue: InternalSettings = InternalSettings.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): InternalSettings {
        try {
            return InternalSettings.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(
        t: InternalSettings,
        output: OutputStream
    ) = t.writeTo(output)
}

