package proton.android.pass.featureitemdetail.impl.alias

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassColors
import proton.android.pass.commonui.api.asAnnotatedString
import proton.android.pass.featureitemdetail.impl.R
import proton.android.pass.featureitemdetail.impl.common.SectionSubtitle
import proton.android.pass.featureitemdetail.impl.common.SectionTitle

@Composable
fun AliasAddressRow(
    modifier: Modifier = Modifier,
    alias: String,
    onCopyAlias: (String) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCopyAlias(alias) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_alias),
            contentDescription = stringResource(R.string.alias_address_icon_content_description),
            tint = PassColors.GreenAccent
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SectionTitle(text = stringResource(R.string.field_alias_title))
            SectionSubtitle(text = alias.asAnnotatedString())
        }
    }
}
