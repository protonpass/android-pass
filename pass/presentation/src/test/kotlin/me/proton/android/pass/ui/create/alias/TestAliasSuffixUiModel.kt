package me.proton.android.pass.ui.create.alias

import me.proton.pass.presentation.create.alias.AliasSuffixUiModel

object TestAliasSuffixUiModel {

    fun create(): AliasSuffixUiModel =
        AliasSuffixUiModel(
            suffix = "@test-suffix",
            signedSuffix = "test-signedSuffix",
            isCustom = false,
            domain = "test-domain"
        )
}
