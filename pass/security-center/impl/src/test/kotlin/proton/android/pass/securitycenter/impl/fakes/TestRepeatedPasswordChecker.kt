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

package proton.android.pass.securitycenter.impl.fakes

import proton.android.pass.domain.Item
import proton.android.pass.securitycenter.fakes.mother.ReusedPasswordsResultMother
import proton.android.pass.securitycenter.impl.checkers.RepeatedPasswordChecker
import proton.android.pass.securitycenter.impl.checkers.RepeatedPasswordsData

class TestRepeatedPasswordChecker : RepeatedPasswordChecker {

    private var result: Result<RepeatedPasswordsData> = Result.success(
        run {
            val instance = ReusedPasswordsResultMother.random()
            RepeatedPasswordsData(
                repeatedPasswordsCount = instance.reusedPasswordsCount
            )
        }
    )

    private val memory: MutableList<List<Item>> = mutableListOf()
    fun memory(): List<List<Item>> = memory

    fun setResult(value: Result<RepeatedPasswordsData>) {
        result = value
    }

    override fun invoke(items: List<Item>): RepeatedPasswordsData {
        memory.add(items)
        return result.getOrThrow()
    }

}
