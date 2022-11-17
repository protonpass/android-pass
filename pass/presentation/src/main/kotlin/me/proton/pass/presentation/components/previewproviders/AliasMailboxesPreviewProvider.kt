package me.proton.pass.presentation.components.previewproviders

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import me.proton.pass.domain.AliasMailbox

class AliasMailboxesPreviewProvider : PreviewParameterProvider<List<AliasMailbox>> {
    override val values: Sequence<List<AliasMailbox>>
        get() = sequenceOf(
            listOf(AliasMailbox(1, "some@mail.box")),
            listOf(AliasMailbox(1, "some@mail.box"), AliasMailbox(2, "other@mail.box")),
            listOf(AliasMailbox(1, "some.very.long.mailbox.that.can.get.even.longer.than.you.expect@mail.box"))
        )
}
