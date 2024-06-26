/*
 * Copyright (c) 2024 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.preferences.tooltips

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import proton.android.pass.preferences.TooltipsPreferences
import java.io.InputStream
import java.io.OutputStream

internal object TooltipPreferencesSerializer : Serializer<TooltipsPreferences> {

    override val defaultValue: TooltipsPreferences = TooltipsPreferences.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): TooltipsPreferences = try {
        TooltipsPreferences.parseFrom(input)
    } catch (exception: InvalidProtocolBufferException) {
        throw CorruptionException("Cannot read Tooltips Preferences proto.", exception)
    }

    override suspend fun writeTo(t: TooltipsPreferences, output: OutputStream) = t.writeTo(output)

}
