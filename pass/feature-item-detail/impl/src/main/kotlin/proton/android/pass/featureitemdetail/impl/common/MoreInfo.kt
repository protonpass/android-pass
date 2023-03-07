package proton.android.pass.featureitemdetail.impl.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.commonui.api.DateFormatUtils
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.featureitemdetail.impl.R
import java.time.format.DateTimeFormatter
import java.util.Locale

@Suppress("MagicNumber")
@Composable
fun MoreInfo(
    modifier: Modifier = Modifier,
    moreInfoUiState: MoreInfoUiState,
    shouldShowMoreInfoInitially: Boolean = false
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        var showMoreInfo by remember { mutableStateOf(shouldShowMoreInfoInitially) }
        var rotation by remember { mutableStateOf(0f) }
        val displayRotation by animateFloatAsState(targetValue = rotation)

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                    showMoreInfo = !showMoreInfo
                    rotation = if (rotation == 0f) {
                        180f
                    } else {
                        0f
                    }
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(me.proton.core.presentation.R.drawable.ic_info_circle),
                contentDescription = stringResource(R.string.more_info_icon),
                tint = ProtonTheme.colors.iconWeak
            )
            MoreInfoText(
                modifier = Modifier.padding(8.dp),
                text = stringResource(R.string.more_info_title)
            )

            Icon(
                modifier = Modifier
                    .size(16.dp)
                    .rotate(displayRotation),
                painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_chevron_down),
                contentDescription = stringResource(R.string.more_info_icon),
                tint = ProtonTheme.colors.iconWeak,
            )
        }
        AnimatedVisibility(visible = showMoreInfo) {
            Row(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Column(
                    modifier = Modifier.weight(0.3f),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MoreInfoLastAutofilledTitle(lastAutofilled = moreInfoUiState.lastAutofilled)
                    MoreInfoModifiedTitle(numRevisions = moreInfoUiState.numRevisions)
                    // Created
                    MoreInfoText(text = stringResource(R.string.more_info_created))
                }
                Column(
                    modifier = Modifier.weight(0.7f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MoreInfoLastAutofilledContent(moreInfoUiState = moreInfoUiState)
                    MoreInfoModifiedContent(moreInfoUiState = moreInfoUiState)
                    // Created
                    MoreInfoText(
                        text = formatMoreInfoInstantText(
                            now = moreInfoUiState.now,
                            toFormat = moreInfoUiState.createdTime
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun MoreInfoLastAutofilledTitle(
    modifier: Modifier = Modifier,
    lastAutofilled: Option<Instant>
) {
    if (lastAutofilled.isNotEmpty()) {
        MoreInfoText(modifier = modifier, text = stringResource(R.string.more_info_autofilled))
    }
}

@Composable
private fun MoreInfoModifiedTitle(
    modifier: Modifier = Modifier,
    numRevisions: Long
) {
    if (numRevisions > 1) {
        MoreInfoText(modifier = modifier, text = stringResource(R.string.more_info_modified))
        Spacer(modifier = Modifier.height(14.dp))
    }
}

@Composable
private fun MoreInfoLastAutofilledContent(
    modifier: Modifier = Modifier,
    moreInfoUiState: MoreInfoUiState
) {
    if (moreInfoUiState.lastAutofilled is Some) {
        MoreInfoText(
            modifier = modifier,
            text = formatMoreInfoInstantText(
                now = moreInfoUiState.now,
                toFormat = moreInfoUiState.lastAutofilled.value
            )
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ColumnScope.MoreInfoModifiedContent(
    modifier: Modifier = Modifier,
    moreInfoUiState: MoreInfoUiState
) {
    val modifiedTimes = moreInfoUiState.numRevisions - 1
    if (modifiedTimes > 0) {
        val lastUpdateString = formatMoreInfoInstantText(
            now = moreInfoUiState.now,
            toFormat = moreInfoUiState.lastModified
        )

        MoreInfoText(
            modifier = modifier,
            text = pluralStringResource(
                id = R.plurals.more_info_modified_times,
                count = modifiedTimes.toInt(),
                modifiedTimes.toInt()
            )
        )
        MoreInfoText(
            modifier = modifier,
            text = stringResource(R.string.more_info_last_time_modified, lastUpdateString)
        )
    }
}

@Composable
fun formatMoreInfoInstantText(
    now: Instant,
    toFormat: Instant,
    locale: Locale = Locale.getDefault()
): String =
    when (
        DateFormatUtils.getFormat(
            now = now,
            toFormat = toFormat,
            timeZone = TimeZone.currentSystemDefault(),
            acceptedFormats = listOf(
                DateFormatUtils.Format.Date,
                DateFormatUtils.Format.Yesterday,
                DateFormatUtils.Format.DateOfSameYear,
                DateFormatUtils.Format.Date
            )
        )
    ) {
        DateFormatUtils.Format.Date -> {
            val pattern =
                stringResource(R.string.date_full_date_format_with_year)
            DateTimeFormatter.ofPattern(pattern)
                .withLocale(locale)
                .format(
                    toFormat.toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime()
                )
        }
        DateFormatUtils.Format.DateOfSameYear -> {
            val pattern =
                stringResource(R.string.date_full_date_format)
            DateTimeFormatter.ofPattern(pattern)
                .withLocale(locale)
                .format(
                    toFormat.toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime()
                )
        }
        DateFormatUtils.Format.Today -> stringResource(
            R.string.date_today,
            extractHour(toFormat.toLocalDateTime(TimeZone.currentSystemDefault()))
        )
        DateFormatUtils.Format.Yesterday -> stringResource(
            R.string.date_yesterday,
            extractHour(toFormat.toLocalDateTime(TimeZone.currentSystemDefault()))
        )
        else -> throw IllegalStateException("Unexpected date format")
    }

private fun extractHour(instant: LocalDateTime): String {
    val hour = instant.hour.toString().padStart(2, '0')
    val minute = instant.minute.toString().padStart(2, '0')
    return "$hour:$minute"
}

class ThemedMoreInfoPreviewProvider :
    ThemePairPreviewProvider<MoreInfoPreview>(MoreInfoPreviewProvider())

@Preview
@Composable
fun MoreInfoPreview(
    @PreviewParameter(ThemedMoreInfoPreviewProvider::class) input: Pair<Boolean, MoreInfoPreview>
) {
    PassTheme(isDark = input.first) {
        Surface {
            MoreInfo(
                shouldShowMoreInfoInitially = input.second.showMoreInfo,
                moreInfoUiState = input.second.uiState
            )
        }
    }
}
