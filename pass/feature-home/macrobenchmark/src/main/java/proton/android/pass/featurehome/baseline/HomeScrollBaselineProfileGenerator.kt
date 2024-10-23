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

package proton.android.pass.featurehome.baseline

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import proton.android.pass.featurehome.ITEMS_LIST_ID
import proton.android.pass.featurehome.PACKAGE_NAME
import proton.android.pass.featurehome.TIMEOUT
import proton.android.pass.featurehome.VISIBLE_ITEM
import proton.android.pass.featurehome.setSafeGestureMargin

@RequiresApi(Build.VERSION_CODES.P)
class HomeScrollBaselineProfileGenerator {

    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun homeScrollBaseline() {
        baselineProfileRule.collect(PACKAGE_NAME) {
            startActivityAndWait()

            val contentList = device.findObject(By.res(ITEMS_LIST_ID))
            val searchCondition = Until.hasObject(By.text(VISIBLE_ITEM))

            contentList.wait(searchCondition, TIMEOUT)
            contentList.setSafeGestureMargin(device)
            contentList.fling(Direction.DOWN)
            device.waitForIdle()
        }
    }
}

