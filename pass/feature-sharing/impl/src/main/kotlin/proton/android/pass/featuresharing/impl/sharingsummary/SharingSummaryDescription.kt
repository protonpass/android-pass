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

package proton.android.pass.featuresharing.impl.sharingsummary

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.body3Norm
import proton.android.pass.commonui.api.body3Weak
import proton.android.pass.featuresharing.impl.R
import proton.android.pass.featuresharing.impl.sharingpermissions.SharingType

@Composable
fun SharingSummaryDescription(modifier: Modifier = Modifier, state: SharingSummaryUIState) {
    val emailString = state.email
    val nameString = stringResource(
        id = R.string.share_summary_vault,
        state.vaultWithItemCount?.vault?.name ?: ""
    )
    val count = (
        state.vaultWithItemCount?.activeItemCount
            ?: 0
        ) + (state.vaultWithItemCount?.trashedItemCount ?: 0)
    val countString =
        pluralStringResource(id = R.plurals.sharing_item_count, count.toInt(), count.toInt())
    val sharingTypeString = when (state.sharingType) {
        SharingType.Read -> stringResource(R.string.share_summary_view_items)
        SharingType.Write -> stringResource(R.string.share_summary_create_items)
        SharingType.Admin -> stringResource(R.string.share_summary_manage_vault)
    }
    val text = stringResource(
        R.string.share_summary_subtitle,
        emailString,
        nameString,
        countString,
        sharingTypeString
    )
    val emailIndex = text.indexOf(emailString)
    val nameIndex = text.indexOf(nameString)
    val countIndex = text.indexOf(countString)
    val typeIndex = text.indexOf(sharingTypeString)
    val spanStyles = listOf(
        AnnotatedString.Range(
            item = SpanStyle(
                fontWeight = FontWeight.Bold,
                color = PassTheme.typography.body3Norm().color
            ),
            start = emailIndex,
            end = emailIndex + emailString.length
        ),
        AnnotatedString.Range(
            item = SpanStyle(
                fontWeight = FontWeight.Bold,
                color = PassTheme.typography.body3Norm().color
            ),
            start = nameIndex,
            end = nameIndex + nameString.length
        ),
        AnnotatedString.Range(
            item = SpanStyle(
                fontWeight = FontWeight.Bold,
                color = PassTheme.typography.body3Norm().color
            ),
            start = countIndex,
            end = countIndex + countString.length
        ),
        AnnotatedString.Range(
            item = SpanStyle(
                fontWeight = FontWeight.Bold,
                color = PassTheme.typography.body3Norm().color
            ),
            start = typeIndex,
            end = typeIndex + sharingTypeString.length
        ),
    )
    Text(
        modifier = modifier,
        text = AnnotatedString(text = text, spanStyles = spanStyles),
        style = PassTheme.typography.body3Weak()
    )
}
