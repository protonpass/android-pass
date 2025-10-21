/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.data.impl.local.inappmessages

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import me.proton.core.domain.entity.UserId
import me.proton.core.util.kotlin.takeIfNotBlank
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.getOrElse
import proton.android.pass.common.api.toOption
import proton.android.pass.data.impl.db.PassDatabase
import proton.android.pass.data.impl.db.entities.InAppMessageEntity
import proton.android.pass.domain.inappmessages.InAppMessage
import proton.android.pass.domain.inappmessages.InAppMessageCTA
import proton.android.pass.domain.inappmessages.InAppMessageCTAType
import proton.android.pass.domain.inappmessages.InAppMessageId
import proton.android.pass.domain.inappmessages.InAppMessageKey
import proton.android.pass.domain.inappmessages.InAppMessagePromoContents
import proton.android.pass.domain.inappmessages.InAppMessagePromoThemedContents
import proton.android.pass.domain.inappmessages.InAppMessageRange
import proton.android.pass.domain.inappmessages.InAppMessageStatus
import proton.android.pass.domain.inappmessages.MODE_BANNER
import proton.android.pass.domain.inappmessages.MODE_MODAL
import proton.android.pass.domain.inappmessages.MODE_PROMO
import proton.android.pass.domain.inappmessages.STATUS_DISMISSED
import javax.inject.Inject

class LocalInAppMessagesDataSourceImpl @Inject constructor(
    private val database: PassDatabase
) : LocalInAppMessagesDataSource {

    override fun observePromoMinimizedUserMessages(userId: UserId, currentTimestamp: Long): Flow<InAppMessage.Promo?> =
        database.inAppMessagesDao()
            .observeDeliverableMessagesWithNotStatus(
                userId = userId.id,
                mode = MODE_PROMO,
                status = STATUS_DISMISSED,
                currentTimestamp = currentTimestamp
            )
            .map { entities ->
                entities.firstOrNull()?.toDomain() as? InAppMessage.Promo
            }

    override fun observeUserMessage(userId: UserId, id: InAppMessageId): Flow<InAppMessage> =
        database.inAppMessagesDao().observeUserMessage(userId.id, id.value)
            .map(InAppMessageEntity::toDomain)

    override fun observeTopDeliverableUserMessage(userId: UserId, currentTimestamp: Long): Flow<InAppMessage?> =
        database.inAppMessagesDao()
            .observeDeliverableMessagesWithNotStatus(
                userId = userId.id,
                mode = null, // No mode filter for top message
                status = STATUS_DISMISSED,
                currentTimestamp = currentTimestamp
            )
            .map { entities ->
                entities
                    .firstOrNull { entity ->
                        when (entity.mode) {
                            MODE_PROMO -> entity.promoStartMinimized?.not() ?: false
                            else -> true
                        }
                    }
                    ?.toDomain()
            }

    override suspend fun storeMessages(userId: UserId, messages: List<InAppMessage>) {
        database.inTransaction(name = "storeMessages") {
            database.inAppMessagesDao().deleteAll(userId.id)
            database.inAppMessagesDao().insertOrUpdate(*messages.map(InAppMessage::toEntity).toTypedArray())
        }
    }

    override suspend fun updateMessage(userId: UserId, message: InAppMessage) {
        database.inAppMessagesDao().insertOrUpdate(message.toEntity())
    }
}

private fun InAppMessageEntity.toDomain(): InAppMessage {
    val baseMessage = BaseMessageData(
        id = InAppMessageId(id),
        key = InAppMessageKey(key),
        priority = priority,
        title = title,
        message = message.toOption(),
        imageUrl = imageUrl?.takeIfNotBlank().toOption(),
        cta = if (ctaText != null && ctaRoute != null && ctaType != null) {
            Some(InAppMessageCTA(ctaText, ctaRoute, InAppMessageCTAType.fromValue(ctaType)))
        } else {
            None
        },
        state = InAppMessageStatus.fromValue(state),
        range = InAppMessageRange(
            start = Instant.fromEpochSeconds(rangeStart),
            end = rangeEnd?.let(Instant.Companion::fromEpochSeconds).toOption()
        ),
        userId = UserId(userId)
    )

    return when (mode) {
        MODE_BANNER -> InAppMessage.Banner(
            baseMessage.id, baseMessage.key, baseMessage.priority, baseMessage.title,
            baseMessage.message, baseMessage.imageUrl, baseMessage.cta, baseMessage.state,
            baseMessage.range, baseMessage.userId
        )

        MODE_MODAL -> InAppMessage.Modal(
            baseMessage.id, baseMessage.key, baseMessage.priority, baseMessage.title,
            baseMessage.message, baseMessage.imageUrl, baseMessage.cta, baseMessage.state,
            baseMessage.range, baseMessage.userId
        )

        MODE_PROMO -> {
            val promoContents = toPromoContents().getOrElse {
                throw IllegalArgumentException("PromoContents is required for Promo mode")
            }
            InAppMessage.Promo(
                baseMessage.id, baseMessage.key, baseMessage.priority, baseMessage.title,
                baseMessage.message, baseMessage.imageUrl, baseMessage.cta, baseMessage.state,
                baseMessage.range, baseMessage.userId, promoContents
            )
        }

        else -> throw IllegalArgumentException("Unknown mode: $mode")
    }
}

private data class BaseMessageData(
    val id: InAppMessageId,
    val key: InAppMessageKey,
    val priority: Int,
    val title: String,
    val message: Option<String>,
    val imageUrl: Option<String>,
    val cta: Option<InAppMessageCTA>,
    val state: InAppMessageStatus,
    val range: InAppMessageRange,
    val userId: UserId
)

@Suppress("ComplexCondition")
private fun InAppMessageEntity.toPromoContents(): Option<InAppMessagePromoContents> = if (
    promoStartMinimized != null &&
    promoCloseText != null &&
    promoLightBackgroundUrl != null &&
    promoLightContentUrl != null &&
    promoLightCloseTextColor != null &&
    promoDarkBackgroundUrl != null &&
    promoDarkContentUrl != null &&
    promoDarkCloseTextColor != null
) {
    Some(
        InAppMessagePromoContents(
            startMinimised = promoStartMinimized,
            closePromoText = promoCloseText,
            lightThemeContents = InAppMessagePromoThemedContents(
                backgroundImageUrl = promoLightBackgroundUrl,
                contentImageUrl = promoLightContentUrl,
                closePromoTextColor = promoLightCloseTextColor
            ),
            darkThemeContents = InAppMessagePromoThemedContents(
                backgroundImageUrl = promoDarkBackgroundUrl,
                contentImageUrl = promoDarkContentUrl,
                closePromoTextColor = promoDarkCloseTextColor
            )
        )
    )
} else {
    None
}

@Suppress("LongMethod")
private fun InAppMessage.toEntity(): InAppMessageEntity = when (this) {
    is InAppMessage.Banner -> InAppMessageEntity(
        id = id.value,
        key = key.value,
        mode = MODE_BANNER,
        priority = priority,
        title = title,
        message = message.value(),
        imageUrl = imageUrl.value(),
        ctaText = cta.map(InAppMessageCTA::text).value(),
        ctaRoute = cta.map(InAppMessageCTA::route).value(),
        ctaType = cta.map(InAppMessageCTA::type).map(InAppMessageCTAType::value).value(),
        state = state.value,
        rangeStart = range.start.epochSeconds,
        rangeEnd = range.end.map(Instant::epochSeconds).value(),
        userId = userId.id,
        promoStartMinimized = null,
        promoCloseText = null,
        promoLightBackgroundUrl = null,
        promoLightContentUrl = null,
        promoLightCloseTextColor = null,
        promoDarkBackgroundUrl = null,
        promoDarkContentUrl = null,
        promoDarkCloseTextColor = null
    )

    is InAppMessage.Modal -> InAppMessageEntity(
        id = id.value,
        key = key.value,
        mode = MODE_MODAL,
        priority = priority,
        title = title,
        message = message.value(),
        imageUrl = imageUrl.value(),
        ctaText = cta.map(InAppMessageCTA::text).value(),
        ctaRoute = cta.map(InAppMessageCTA::route).value(),
        ctaType = cta.map(InAppMessageCTA::type).map(InAppMessageCTAType::value).value(),
        state = state.value,
        rangeStart = range.start.epochSeconds,
        rangeEnd = range.end.map(Instant::epochSeconds).value(),
        userId = userId.id,
        promoStartMinimized = null,
        promoCloseText = null,
        promoLightBackgroundUrl = null,
        promoLightContentUrl = null,
        promoLightCloseTextColor = null,
        promoDarkBackgroundUrl = null,
        promoDarkContentUrl = null,
        promoDarkCloseTextColor = null
    )

    is InAppMessage.Promo -> InAppMessageEntity(
        id = id.value,
        key = key.value,
        mode = MODE_PROMO,
        priority = priority,
        title = title,
        message = message.value(),
        imageUrl = imageUrl.value(),
        ctaText = cta.map(InAppMessageCTA::text).value(),
        ctaRoute = cta.map(InAppMessageCTA::route).value(),
        ctaType = cta.map(InAppMessageCTA::type).map(InAppMessageCTAType::value).value(),
        state = state.value,
        rangeStart = range.start.epochSeconds,
        rangeEnd = range.end.map(Instant::epochSeconds).value(),
        userId = userId.id,
        promoStartMinimized = promoContents.startMinimised,
        promoCloseText = promoContents.closePromoText,
        promoLightBackgroundUrl = promoContents.lightThemeContents.backgroundImageUrl,
        promoLightContentUrl = promoContents.lightThemeContents.contentImageUrl,
        promoLightCloseTextColor = promoContents.lightThemeContents.closePromoTextColor,
        promoDarkBackgroundUrl = promoContents.darkThemeContents.backgroundImageUrl,
        promoDarkContentUrl = promoContents.darkThemeContents.contentImageUrl,
        promoDarkCloseTextColor = promoContents.darkThemeContents.closePromoTextColor
    )
}
