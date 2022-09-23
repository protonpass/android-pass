package me.proton.core.pass.test.domain

import me.proton.core.pass.domain.AliasMailbox

object TestAliasMailbox {

    fun create(): AliasMailbox = AliasMailbox(id = 0, email = "test-email")
}
