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

package proton.android.pass.features.itemcreate.creditcard

import androidx.compose.ui.text.AnnotatedString
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CardNumberTransformedTextTest {

    @Test
    fun `test transform card number`() {
        val cardNumber = "1234567890123456"
        val transformedText =
            proton.android.pass.features.itemcreate.creditcard.cardNumberTransformedText(
                AnnotatedString(cardNumber)
            )
        assertThat(transformedText.text.text).isEqualTo("1234 5678 9012 3456")
    }

    @Test
    fun `test offsetMapping originalToTransformed`() {
        val cardNumber = "1234567890123456"
        val transformedText =
            proton.android.pass.features.itemcreate.creditcard.cardNumberTransformedText(
                AnnotatedString(cardNumber)
            )
        val offsetMapping = transformedText.offsetMapping

        // Original:     1234567890123456
        // Transformed:  1234 5678 9012 3456

        assertThat(offsetMapping.originalToTransformed(0)).isEqualTo(0)
        assertThat(offsetMapping.originalToTransformed(1)).isEqualTo(1)
        assertThat(offsetMapping.originalToTransformed(2)).isEqualTo(2)
        assertThat(offsetMapping.originalToTransformed(3)).isEqualTo(3)

        assertThat(offsetMapping.originalToTransformed(4)).isEqualTo(5)
        assertThat(offsetMapping.originalToTransformed(5)).isEqualTo(6)
        assertThat(offsetMapping.originalToTransformed(6)).isEqualTo(7)
        assertThat(offsetMapping.originalToTransformed(7)).isEqualTo(8)

        assertThat(offsetMapping.originalToTransformed(8)).isEqualTo(10)
        assertThat(offsetMapping.originalToTransformed(9)).isEqualTo(11)
        assertThat(offsetMapping.originalToTransformed(10)).isEqualTo(12)
        assertThat(offsetMapping.originalToTransformed(11)).isEqualTo(13)

        assertThat(offsetMapping.originalToTransformed(12)).isEqualTo(15)
        assertThat(offsetMapping.originalToTransformed(13)).isEqualTo(16)
        assertThat(offsetMapping.originalToTransformed(14)).isEqualTo(17)
        assertThat(offsetMapping.originalToTransformed(15)).isEqualTo(18)

        assertThat(offsetMapping.originalToTransformed(16)).isEqualTo(19)
        assertThat(offsetMapping.originalToTransformed(17)).isEqualTo(19) // Should not happen
    }

    @Test
    fun `test offsetMapping transformedToOriginal`() {
        val cardNumber = "1234567890123456"
        val transformedText =
            proton.android.pass.features.itemcreate.creditcard.cardNumberTransformedText(
                AnnotatedString(cardNumber)
            )
        val offsetMapping = transformedText.offsetMapping

        // Original:     1234567890123456
        // Transformed:  1234 5678 9012 3456

        assertThat(offsetMapping.transformedToOriginal(0)).isEqualTo(0)
        assertThat(offsetMapping.transformedToOriginal(1)).isEqualTo(1)
        assertThat(offsetMapping.transformedToOriginal(2)).isEqualTo(2)
        assertThat(offsetMapping.transformedToOriginal(3)).isEqualTo(3)

        assertThat(offsetMapping.transformedToOriginal(4)).isEqualTo(4)

        assertThat(offsetMapping.transformedToOriginal(5)).isEqualTo(5)
        assertThat(offsetMapping.transformedToOriginal(6)).isEqualTo(6)
        assertThat(offsetMapping.transformedToOriginal(7)).isEqualTo(7)
        assertThat(offsetMapping.transformedToOriginal(8)).isEqualTo(7)

        assertThat(offsetMapping.transformedToOriginal(9)).isEqualTo(8)

        assertThat(offsetMapping.transformedToOriginal(10)).isEqualTo(9)
        assertThat(offsetMapping.transformedToOriginal(11)).isEqualTo(10)
        assertThat(offsetMapping.transformedToOriginal(12)).isEqualTo(10)
        assertThat(offsetMapping.transformedToOriginal(13)).isEqualTo(11)

        assertThat(offsetMapping.transformedToOriginal(14)).isEqualTo(12)

        assertThat(offsetMapping.transformedToOriginal(15)).isEqualTo(13)
        assertThat(offsetMapping.transformedToOriginal(16)).isEqualTo(13)
        assertThat(offsetMapping.transformedToOriginal(17)).isEqualTo(14)
        assertThat(offsetMapping.transformedToOriginal(18)).isEqualTo(15)

        assertThat(offsetMapping.transformedToOriginal(19)).isEqualTo(16)
        assertThat(offsetMapping.transformedToOriginal(20)).isEqualTo(17) // Should not happen
    }

}
