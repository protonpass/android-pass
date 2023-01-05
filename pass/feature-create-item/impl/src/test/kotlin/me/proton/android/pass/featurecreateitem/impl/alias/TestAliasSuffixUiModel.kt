package me.proton.android.pass.featurecreateitem.impl.alias

object TestAliasSuffixUiModel {

    fun create(): AliasSuffixUiModel =
        AliasSuffixUiModel(
            suffix = "@test-suffix",
            signedSuffix = "test-signedSuffix",
            isCustom = false,
            domain = "test-domain"
        )
}
