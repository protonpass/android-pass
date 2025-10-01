/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.commonpresentation.api.plan

import androidx.compose.runtime.Stable
import me.proton.core.domain.entity.UserId
import me.proton.core.plan.domain.entity.DynamicPlan
import proton.android.pass.domain.plan.PaymentButton

fun PaymentButton.toUiModel() = PaymentButtonUiState(
    currency = this.currency,
    cycle = this.cycle,
    plan = this.plan,
    userId = this.userId
)

@Stable
data class PaymentButtonUiState(
    val currency: String = "",
    val cycle: Int = 1,
    val plan: DynamicPlan? = null,
    val userId: UserId? = null,
    val defaultButtonText: String? = null
)

