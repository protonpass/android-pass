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

package proton.android.pass.data.impl.usecases

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import proton.android.pass.data.api.usecases.capabilities.DefaultVaultDecision
import proton.android.pass.data.fakes.usecases.FakeCanCreateDefaultVault
import proton.android.pass.data.impl.usecases.capabilities.DecideDefaultVaultCreationImpl
import proton.android.pass.domain.UserId
import proton.android.pass.preferences.FakeFeatureFlagsPreferenceRepository
import proton.android.pass.preferences.FakeInternalSettingsRepository
import proton.android.pass.preferences.FeatureFlag

internal class DecideDefaultVaultCreationImplTest {

    private lateinit var canCreateDefaultVault: FakeCanCreateDefaultVault
    private lateinit var internalSettings: FakeInternalSettingsRepository
    private lateinit var featureFlags: FakeFeatureFlagsPreferenceRepository
    private lateinit var instance: DecideDefaultVaultCreationImpl

    @Before
    fun setup() {
        canCreateDefaultVault = FakeCanCreateDefaultVault()
        internalSettings = FakeInternalSettingsRepository()
        featureFlags = FakeFeatureFlagsPreferenceRepository()
        instance = DecideDefaultVaultCreationImpl(
            canCreateDefaultVault = canCreateDefaultVault,
            internalSettingsRepository = internalSettings,
            preferencesRepository = featureFlags
        )
    }

    @Test
    fun `skips when the default vault cannot be created`() = runTest {
        canCreateDefaultVault.setResult(false)

        assertThat(instance(USER_ID)).isEqualTo(DefaultVaultDecision.SkipNotAllowed)
    }

    @Test
    fun `skips when no vault is allowed and the default one was already created`() = runTest {
        featureFlags.set(FeatureFlag.PASS_ALLOW_NO_VAULT, true)
        internalSettings.setDefaultVaultHasBeenCreated(USER_ID)

        assertThat(instance(USER_ID)).isEqualTo(DefaultVaultDecision.SkipAlreadyCreated)
    }

    @Test
    fun `creates when the user can create a vault and none was created before`() = runTest {
        featureFlags.set(FeatureFlag.PASS_ALLOW_NO_VAULT, true)

        assertThat(instance(USER_ID)).isEqualTo(DefaultVaultDecision.Create)
    }

    @Test
    fun `creates again when the allow-no-vault flag is off even if one was created before`() = runTest {
        internalSettings.setDefaultVaultHasBeenCreated(USER_ID)

        assertThat(instance(USER_ID)).isEqualTo(DefaultVaultDecision.Create)
    }

    @Test
    fun `does not ask whether a vault can be created when it was already created`() = runTest {
        canCreateDefaultVault.setResult(false)
        featureFlags.set(FeatureFlag.PASS_ALLOW_NO_VAULT, true)
        internalSettings.setDefaultVaultHasBeenCreated(USER_ID)

        assertThat(instance(USER_ID)).isEqualTo(DefaultVaultDecision.SkipAlreadyCreated)
    }

    private companion object {
        val USER_ID = UserId("user-id")
    }
}
