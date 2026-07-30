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

import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionListener
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeSessionListener @Inject constructor() : SessionListener {
    override suspend fun <T> withLock(sessionId: SessionId?, action: suspend () -> T): T = action()

    override suspend fun requestSession(): Boolean = false

    override suspend fun refreshSession(session: Session): Boolean = false

    override suspend fun onSessionTokenCreated(userId: UserId?, session: Session) = Unit

    override suspend fun onSessionTokenRefreshed(session: Session) = Unit

    override suspend fun onSessionScopesRefreshed(sessionId: SessionId, scopes: List<String>) = Unit

    override suspend fun onSessionForceLogout(session: Session, httpCode: Int) = Unit
}
