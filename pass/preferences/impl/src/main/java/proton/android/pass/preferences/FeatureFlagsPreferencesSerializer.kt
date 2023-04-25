package proton.android.pass.preferences

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

object FeatureFlagsPreferencesSerializer : Serializer<FeatureFlagsPreferences> {
    override val defaultValue: FeatureFlagsPreferences = FeatureFlagsPreferences.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): FeatureFlagsPreferences {
        try {
            return FeatureFlagsPreferences.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(
        t: FeatureFlagsPreferences,
        output: OutputStream
    ) = t.writeTo(output)
}
