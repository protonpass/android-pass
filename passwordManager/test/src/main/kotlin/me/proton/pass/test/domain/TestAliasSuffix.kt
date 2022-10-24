package me.proton.pass.test.domain

import me.proton.pass.domain.AliasSuffix

object TestAliasSuffix {

    fun create(): AliasSuffix =
        AliasSuffix(
            suffix = "test-suffix",
            signedSuffix = "test-signedSuffix",
            isCustom = false,
            domain = "test-domain"
        )
}
