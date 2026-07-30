/*
 * Copyright (c) 2026 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package proton.android.pass.account.fakes.payment

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import me.proton.core.humanverification.domain.HumanVerificationManager
import me.proton.core.network.domain.client.ClientId
import me.proton.core.network.domain.humanverification.HumanVerificationAvailableMethods
import me.proton.core.network.domain.humanverification.HumanVerificationDetails
import me.proton.core.network.domain.humanverification.HumanVerificationListener
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeHumanVerificationManager @Inject constructor() : HumanVerificationManager {
    override fun onHumanVerificationStateChanged(initialState: Boolean): Flow<HumanVerificationDetails> = flowOf()

    override suspend fun addDetails(details: HumanVerificationDetails) = Unit

    override suspend fun clearDetails(clientId: ClientId) = Unit

    override suspend fun getHumanVerificationDetails(clientId: ClientId): HumanVerificationDetails? = null

    override suspend fun onHumanVerificationNeeded(
        clientId: ClientId,
        methods: HumanVerificationAvailableMethods
    ): HumanVerificationListener.HumanVerificationResult = HumanVerificationListener.HumanVerificationResult.Success

    override suspend fun onHumanVerificationInvalid(clientId: ClientId) = Unit
}
