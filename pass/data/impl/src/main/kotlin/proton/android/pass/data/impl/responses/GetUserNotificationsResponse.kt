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

package proton.android.pass.data.impl.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetUserNotificationsResponse(
    @SerialName("Code")
    val code: Int,
    @SerialName("Notifications")
    val response: NotificationsResponse
)

@Serializable
data class NotificationsResponse(
    @SerialName("Notifications")
    val list: List<NotificationResponse>,
    @SerialName("Total")
    val total: Int,
    @SerialName("LastID")
    val lastID: String
)

@Serializable
data class NotificationResponse(
    @SerialName("ID")
    val id: String,
    @SerialName("NotificationKey")
    val notificationKey: String,
    @SerialName("StartTime")
    val startTime: Long,
    @SerialName("EndTime")
    val endTime: Long,
    @SerialName("State")
    val state: Int,
    @SerialName("Content")
    val content: ContentResponse
)

@Serializable
data class ContentResponse(
    @SerialName("ImageUrl")
    val imageUrl: String,
    @SerialName("DisplayType")
    val displayType: Int,
    @SerialName("Title")
    val title: String,
    @SerialName("Message")
    val message: String,
    @SerialName("Theme")
    val theme: String,
    @SerialName("Cta")
    val cta: CtaResponse
)

@Serializable
data class CtaResponse(
    @SerialName("Text")
    val text: String,
    @SerialName("Type")
    val type: String,
    @SerialName("Ref")
    val ref: String
)
