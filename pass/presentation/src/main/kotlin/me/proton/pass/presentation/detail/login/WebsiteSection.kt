package me.proton.pass.presentation.detail.login

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.commonui.api.ThemePairPreviewProvider
import me.proton.pass.presentation.R
import me.proton.pass.presentation.components.previewproviders.WebsiteProvider
import me.proton.pass.presentation.detail.DetailSectionTitle

@Composable
fun WebsiteSection(
    modifier: Modifier = Modifier,
    websites: List<String>
) {
    Column(modifier = modifier) {
        DetailSectionTitle(text = stringResource(R.string.field_websites))
        Spacer(modifier = Modifier.height(8.dp))

        val context = LocalContext.current
        websites.forEach { website ->
            Text(
                modifier = Modifier
                    .clickable { openWebsite(context, website) }
                    .padding(vertical = 8.dp),
                text = website,
                color = ProtonTheme.colors.interactionNorm,
                fontSize = 16.sp,
                fontWeight = FontWeight.W400
            )
        }
    }
}

fun openWebsite(context: Context, website: String) {
    runCatching {
        Uri.parse(website)
    }.onSuccess { uri ->
        val i = Intent(Intent.ACTION_VIEW).apply {
            setData(uri)
        }
        context.startActivity(i)
    }
}

class ThemedWebsiteSectionPreviewProvider :
    ThemePairPreviewProvider<List<String>>(WebsiteProvider())

@Preview
@Composable
fun WebsitesSectionPreview(
    @PreviewParameter(ThemedWebsiteSectionPreviewProvider::class) input: Pair<Boolean, List<String>>
) {
    ProtonTheme(isDark = input.first) {
        Surface {
            WebsiteSection(websites = input.second)
        }
    }
}
