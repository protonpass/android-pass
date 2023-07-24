/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.useraccess.impl

import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import proton.android.pass.data.fakes.usecases.TestGetVaultById
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.TestFeatureFlagsPreferenceRepository
import proton.pass.domain.ShareId
import proton.pass.domain.Vault

class UserAccessImplTest {

    private lateinit var userAccess: UserAccessImpl
    private lateinit var featureFlagsPreferencesRepository: TestFeatureFlagsPreferenceRepository
    private lateinit var getVaultById: TestGetVaultById

    @Before
    fun setup() {
        featureFlagsPreferencesRepository = TestFeatureFlagsPreferenceRepository()
        getVaultById = TestGetVaultById()
        userAccess = UserAccessImpl(featureFlagsPreferencesRepository, getVaultById)
    }


    @Test
    fun `test canShare when sharing flag is disabled`() = runTest {
        // Given
        featureFlagsPreferencesRepository.set(FeatureFlag.SHARING_V1, false)

        // When
        val result = userAccess.canShare(shareId = ShareId("some_share_id"))

        // Then
        assertFalse(result)
    }

    @Test
    fun `test canShare when vault is not found`() = runTest {
        // Given
        featureFlagsPreferencesRepository.set(FeatureFlag.SHARING_V1, true)
        getVaultById.sendException(RuntimeException("some error"))

        // When
        val result = userAccess.canShare(shareId = ShareId("some_share_id"))

        // Then
        assertFalse(result)
    }

    @Test
    fun `test canShare when vault is primary`() = runTest {
        // Given
        featureFlagsPreferencesRepository.set(FeatureFlag.SHARING_V1, true)
        val vault = Vault(shareId = ShareId(id = ""), name = "", isPrimary = true)
        getVaultById.emitValue(vault)

        // When
        val result = userAccess.canShare(shareId = ShareId("some_share_id"))

        // Then
        assertFalse(result)
    }

    @Test
    fun `test canShare when all conditions are met`() = runTest {
        // Given
        featureFlagsPreferencesRepository.set(FeatureFlag.SHARING_V1, true)
        val vault = Vault(shareId = ShareId(id = ""), name = "", isPrimary = false)
        getVaultById.emitValue(vault)

        // When
        val result = userAccess.canShare(shareId = ShareId("some_share_id"))

        // Then
        assertTrue(result)
    }
}
