/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.data.impl.work

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Some
import proton.android.pass.data.api.usecases.UpdateAutofillItemData
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.entity.AppName
import proton.android.pass.domain.entity.PackageInfo
import proton.android.pass.domain.entity.PackageName

class UpdateAutofillItemWorkerTest {

    @Test
    fun `create preserves signing certificate hashes in the WorkManager Data payload`() {
        val hashes = setOf("AA:BB:CC", "DD:EE:FF")
        val data = UpdateAutofillItemData(
            shareId = ShareId("share-id"),
            itemId = ItemId("item-id"),
            packageInfo = Some(PackageInfo(PackageName("some.package"), AppName("Some App"), hashes)),
            url = None,
            shouldAssociate = true
        )

        val workData = UpdateAutofillItemWorker.create(data)

        assertThat(workData.getStringArray("arg_hashes")?.toSet()).isEqualTo(hashes)
    }

    @Test
    fun `create does not crash when there are no hashes`() {
        val data = UpdateAutofillItemData(
            shareId = ShareId("share-id"),
            itemId = ItemId("item-id"),
            packageInfo = Some(PackageInfo(PackageName("some.package"), AppName("Some App"))),
            url = None,
            shouldAssociate = true
        )

        val workData = UpdateAutofillItemWorker.create(data)

        assertThat(workData.getStringArray("arg_hashes")?.toSet()).isEqualTo(emptySet<String>())
    }
}
