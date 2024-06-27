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

package proton.android.pass.features.security.center.home.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.Instant
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Radius
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.asAnnotatedString
import proton.android.pass.commonui.api.heroNorm
import proton.android.pass.composecomponents.impl.buttons.PassCircleButton
import proton.android.pass.composecomponents.impl.container.roundedContainer
import proton.android.pass.composecomponents.impl.icon.PassPlusIcon
import proton.android.pass.composecomponents.impl.item.SectionSubtitle
import proton.android.pass.composecomponents.impl.item.SectionTitle
import proton.android.pass.composecomponents.impl.utils.PassBlurEffect
import proton.android.pass.composecomponents.impl.utils.passFormattedDateText
import proton.android.pass.features.security.center.R
import proton.android.pass.features.security.center.shared.ui.DateUtils
import proton.android.pass.composecomponents.impl.R as CompR

@[Composable Suppress("FunctionMaxLength")]
internal fun SecurityCenterHomeDataBreachesWidget(
    modifier: Modifier = Modifier,
    onActionClick: () -> Unit,
    dataBreachedSite: String,
    dataBreachedTime: Long,
    dataBreachedEmail: String,
    dataBreachedPassword: String
) {
    Column(
        modifier = modifier.roundedContainer(
            backgroundColor = PassTheme.colors.passwordInteractionNormMinor2,
            borderColor = PassTheme.colors.passwordInteractionNormMinor1
        ),
        verticalArrangement = Arrangement.spacedBy(space = Spacing.small)
    ) {
        PassPlusIcon(
            modifier = Modifier
                .align(Alignment.End)
                .padding(
                    top = Spacing.medium,
                    end = Spacing.medium
                )
        )

        Column(
            modifier = Modifier.padding(
                start = 24.dp,
                end = 24.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(space = Spacing.medium)
        ) {
            Text(
                text = stringResource(id = R.string.security_center_home_widget_breaches_title),
                color = PassTheme.colors.passwordInteractionNormMajor2,
                fontSize = 24.sp,
                style = PassTheme.typography.heroNorm()
            )

            Text(
                text = stringResource(id = R.string.security_center_home_widget_breaches_subtitle),
                color = PassTheme.colors.passwordInteractionNormMajor2,
                style = ProtonTheme.typography.body1Regular
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                Icon(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(shape = RoundedCornerShape(Radius.small))
                        .background(color = PassTheme.colors.passwordInteractionNormMinor1)
                        .padding(all = Spacing.small),
                    painter = painterResource(id = CompR.drawable.ic_union_filled),
                    contentDescription = null,
                    tint = PassTheme.colors.passwordInteractionNormMajor2
                )

                Column(
                    modifier = Modifier.padding(start = Spacing.extraSmall)
                ) {
                    SectionSubtitle(text = dataBreachedSite.asAnnotatedString())

                    SectionTitle(
                        text = DateUtils.formatDate(dataBreachedTime.toInt()).getOrElse {
                            passFormattedDateText(
                                endInstant = Instant.fromEpochSeconds(dataBreachedTime)
                            )
                        }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .clip(PassTheme.shapes.containerInputShape)
                    .background(color = PassTheme.colors.passwordInteractionNormMinor1)
                    .fillMaxWidth()
                    .padding(all = Spacing.medium),
                verticalArrangement = Arrangement.spacedBy(space = Spacing.small)
            ) {
                BreachRow(
                    labelResId = CompR.string.email_address,
                    value = dataBreachedEmail
                )

                BreachRow(
                    labelResId = CompR.string.password,
                    value = dataBreachedPassword
                )
            }

            PassCircleButton(
                text = stringResource(id = CompR.string.action_view_details),
                backgroundColor = PassTheme.colors.passwordInteractionNormMajor2,
                onClick = onActionClick
            )
        }
    }
}

@Composable
private fun BreachRow(
    modifier: Modifier = Modifier,
    @StringRes labelResId: Int,
    value: String
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = stringResource(id = labelResId),
            color = PassTheme.colors.passwordInteractionNormMajor2,
            style = ProtonTheme.typography.defaultNorm
        )

        PassBlurEffect(
            blurRadius = 4.dp,
            shape = CircleShape,
            noiseFactor = 0.2f
        ) { blurModifier ->
            Text(
                modifier = blurModifier.padding(1.dp),
                text = value,
                color = PassTheme.colors.passwordInteractionNormMajor2,
                style = ProtonTheme.typography.defaultNorm
            )
        }
    }
}

@[Preview Composable Suppress("FunctionMaxLength")]
fun SecurityCenterHomeDataBreachesWidgetPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            SecurityCenterHomeDataBreachesWidget(
                dataBreachedSite = "breached.site.com",
                dataBreachedTime = 1_664_195_804,
                dataBreachedEmail = "email@proton.me",
                dataBreachedPassword = "********",
                onActionClick = {}
            )
        }
    }
}
