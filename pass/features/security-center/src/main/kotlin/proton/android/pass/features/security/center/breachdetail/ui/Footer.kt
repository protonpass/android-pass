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

package proton.android.pass.features.security.center.breachdetail.ui

import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withAnnotation
import androidx.compose.ui.text.withStyle
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmallNorm
import me.proton.core.compose.theme.defaultSmallWeak
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.features.security.center.R

private const val LINK_ANNOTATION_TAG = "link"

@OptIn(ExperimentalTextApi::class)
@Composable
internal fun Footer(
    modifier: Modifier = Modifier,
    name: String,
    onOpenUrl: (String) -> Unit
) {
    val learnMore = stringResource(R.string.security_center_report_detail_learn_more)
    val footerText = buildAnnotatedString {
        withStyle(style = ProtonTheme.typography.defaultSmallWeak.toSpanStyle()) {
            append(
                stringResource(
                    id = R.string.security_center_report_detail_note,
                    name
                )
            )
        }
        append(" ")
        withStyle(
            style = ProtonTheme.typography.defaultSmallNorm
                .copy(color = PassTheme.colors.interactionNormMajor2)
                .toSpanStyle()
        ) {
            withAnnotation(
                tag = LINK_ANNOTATION_TAG,
                annotation = "https://proton.me/blog/breach-recommendations"
            ) {
                append(learnMore)
            }
        }
    }
    ClickableText(
        modifier = modifier,
        text = footerText,
        onClick = {
            footerText.getStringAnnotations(LINK_ANNOTATION_TAG, it, it).firstOrNull()
                ?.let { annotation ->
                    onOpenUrl(annotation.item)
                }
        }
    )
}
