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

package proton.android.pass.data.impl.fakes

import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId
import proton.android.pass.data.impl.remote.RemoteBreachDataSource
import proton.android.pass.data.impl.responses.BreachCustomEmailResponse
import proton.android.pass.data.impl.responses.BreachCustomEmailsResponse
import proton.android.pass.data.impl.responses.BreachEmailsResponse
import proton.android.pass.data.impl.responses.BreachesResponse
import proton.android.pass.data.impl.responses.UpdateGlobalMonitorStateResponse
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.breach.CustomEmailId

class FakeRemoteBreachDataSource : RemoteBreachDataSource {

    private var getAllBreachesResult: Result<BreachesResponse>? = null
    private var getCustomEmailsResult: Result<BreachCustomEmailsResponse>? = null
    private var addCustomEmailResult: Result<BreachCustomEmailResponse>? = null
    private var verifyCustomEmailResult: Result<Unit>? = null
    private var getBreachesForProtonEmailResult: Result<BreachEmailsResponse>? = null
    private var getBreachesForCustomEmailResult: Result<BreachEmailsResponse>? = null
    private var getBreachesForAliasEmailResult: Result<BreachEmailsResponse>? = null
    private var markProtonEmailAsResolvedResult: Result<Unit>? = null
    private var markAliasEmailAsResolvedResult: Result<Unit>? = null
    private var markCustomEmailAsResolvedResult: Result<BreachCustomEmailResponse>? = null
    private var resendVerificationCodeResult: Result<Unit>? = null
    private var removeCustomEmailResult: Result<Unit>? = null
    private var updateGlobalProtonAddressResult: Result<UpdateGlobalMonitorStateResponse>? = null
    private var updateGlobalAliasAddressResult: Result<UpdateGlobalMonitorStateResponse>? = null
    private var updateProtonAddressMonitorStateResult: Result<Unit>? = null

    fun setGetAllBreachesResult(result: Result<BreachesResponse>) {
        getAllBreachesResult = result
    }

    fun setGetCustomEmailsResult(result: Result<BreachCustomEmailsResponse>) {
        getCustomEmailsResult = result
    }

    fun setAddCustomEmailResult(result: Result<BreachCustomEmailResponse>) {
        addCustomEmailResult = result
    }

    fun setVerifyCustomEmailResult(result: Result<Unit>) {
        verifyCustomEmailResult = result
    }

    fun setGetBreachesForProtonEmailResult(result: Result<BreachEmailsResponse>) {
        getBreachesForProtonEmailResult = result
    }

    fun setGetBreachesForCustomEmailResult(result: Result<BreachEmailsResponse>) {
        getBreachesForCustomEmailResult = result
    }

    fun setGetBreachesForAliasEmailResult(result: Result<BreachEmailsResponse>) {
        getBreachesForAliasEmailResult = result
    }

    fun setMarkProtonEmailAsResolvedResult(result: Result<Unit>) {
        markProtonEmailAsResolvedResult = result
    }

    fun setMarkAliasEmailAsResolvedResult(result: Result<Unit>) {
        markAliasEmailAsResolvedResult = result
    }

    fun setMarkCustomEmailAsResolvedResult(result: Result<BreachCustomEmailResponse>) {
        markCustomEmailAsResolvedResult = result
    }

    fun setResendVerificationCodeResult(result: Result<Unit>) {
        resendVerificationCodeResult = result
    }

    fun setRemoveCustomEmailResult(result: Result<Unit>) {
        removeCustomEmailResult = result
    }

    fun setUpdateGlobalProtonAddressMonitorStateResult(result: Result<UpdateGlobalMonitorStateResponse>) {
        updateGlobalProtonAddressResult = result
    }

    fun setUpdateGlobalAliasAddressMonitorStateResult(result: Result<UpdateGlobalMonitorStateResponse>) {
        updateGlobalAliasAddressResult = result
    }

    fun setUpdateProtonAddressMonitorStateResult(result: Result<Unit>) {
        updateProtonAddressMonitorStateResult = result
    }

    override suspend fun getAllBreaches(userId: UserId): BreachesResponse {
        return getAllBreachesResult?.getOrThrow()
            ?: throw IllegalStateException("getAllBreaches result not set")
    }

    override suspend fun getCustomEmails(userId: UserId): BreachCustomEmailsResponse {
        return getCustomEmailsResult?.getOrThrow()
            ?: throw IllegalStateException("getCustomEmails result not set")
    }

    override suspend fun addCustomEmail(userId: UserId, email: String): BreachCustomEmailResponse {
        return addCustomEmailResult?.getOrThrow()
            ?: throw IllegalStateException("addCustomEmail result not set")
    }

    override suspend fun verifyCustomEmail(
        userId: UserId,
        id: CustomEmailId,
        code: String
    ) {
        verifyCustomEmailResult?.getOrThrow()
            ?: throw IllegalStateException("verifyCustomEmail result not set")
    }

    override suspend fun getBreachesForProtonEmail(userId: UserId, id: AddressId): BreachEmailsResponse {
        return getBreachesForProtonEmailResult?.getOrThrow()
            ?: throw IllegalStateException("getBreachesForProtonEmail result not set")
    }

    override suspend fun getBreachesForCustomEmail(userId: UserId, id: CustomEmailId): BreachEmailsResponse {
        return getBreachesForCustomEmailResult?.getOrThrow()
            ?: throw IllegalStateException("getBreachesForCustomEmail result not set")
    }

    override suspend fun getBreachesForAliasEmail(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): BreachEmailsResponse {
        return getBreachesForAliasEmailResult?.getOrThrow()
            ?: throw IllegalStateException("getBreachesForAliasEmail result not set")
    }

    override suspend fun markProtonEmailAsResolved(userId: UserId, id: AddressId) {
        markProtonEmailAsResolvedResult?.getOrThrow()
            ?: throw IllegalStateException("markProtonEmailAsResolved result not set")
    }

    override suspend fun markAliasEmailAsResolved(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ) {
        markAliasEmailAsResolvedResult?.getOrThrow()
            ?: throw IllegalStateException("markAliasEmailAsResolved result not set")
    }

    override suspend fun markCustomEmailAsResolved(userId: UserId, id: CustomEmailId): BreachCustomEmailResponse {
        return markCustomEmailAsResolvedResult?.getOrThrow()
            ?: throw IllegalStateException("markCustomEmailAsResolved result not set")
    }

    override suspend fun resendVerificationCode(userId: UserId, id: CustomEmailId) {
        resendVerificationCodeResult?.getOrThrow()
            ?: throw IllegalStateException("resendVerificationCode result not set")
    }

    override suspend fun removeCustomEmail(userId: UserId, id: CustomEmailId) {
        removeCustomEmailResult?.getOrThrow()
            ?: throw IllegalStateException("removeCustomEmail result not set")
    }

    override suspend fun updateGlobalProtonAddressMonitorState(
        userId: UserId,
        enabled: Boolean
    ): UpdateGlobalMonitorStateResponse {
        return updateGlobalProtonAddressResult?.getOrThrow()
            ?: throw IllegalStateException("updateGlobalProtonAddressMonitorState result not set")
    }

    override suspend fun updateGlobalAliasAddressMonitorState(
        userId: UserId,
        enabled: Boolean
    ): UpdateGlobalMonitorStateResponse {
        return updateGlobalAliasAddressResult?.getOrThrow()
            ?: throw IllegalStateException("updateGlobalAliasAddressMonitorState result not set")
    }

    override suspend fun updateProtonAddressMonitorState(
        userId: UserId,
        id: AddressId,
        enabled: Boolean
    ) {
        updateProtonAddressMonitorStateResult?.getOrThrow()
            ?: throw IllegalStateException("updateProtonAddressMonitorState result not set")
    }

    fun clear() {
        getAllBreachesResult = null
        getCustomEmailsResult = null
        addCustomEmailResult = null
        verifyCustomEmailResult = null
        getBreachesForProtonEmailResult = null
        getBreachesForCustomEmailResult = null
        getBreachesForAliasEmailResult = null
        markProtonEmailAsResolvedResult = null
        markAliasEmailAsResolvedResult = null
        markCustomEmailAsResolvedResult = null
        resendVerificationCodeResult = null
        removeCustomEmailResult = null
        updateGlobalProtonAddressResult = null
        updateGlobalAliasAddressResult = null
        updateProtonAddressMonitorStateResult = null
    }
}

