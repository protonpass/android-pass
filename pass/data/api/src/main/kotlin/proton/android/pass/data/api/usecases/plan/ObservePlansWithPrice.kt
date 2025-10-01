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

package proton.android.pass.data.api.usecases.plan

import kotlinx.coroutines.flow.Flow
import proton.android.pass.domain.plan.PlanWithPriceState

const val ANNUAL_PLAN_CYCLE = 12
const val MONTHLY_PLAN_CYCLE = 1
const val PASS_PLUS_NAME = "pass2023"
const val PASS_UNLIMITED_NAME = "bundle2022"


interface ObservePlansWithPrice {
    operator fun invoke(): Flow<PlanWithPriceState>
}
