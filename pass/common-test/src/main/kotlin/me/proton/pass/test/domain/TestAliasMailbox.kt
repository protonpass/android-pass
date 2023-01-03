package me.proton.pass.test.domain

import me.proton.pass.domain.AliasMailbox

object TestAliasMailbox {

    fun create(): AliasMailbox = AliasMailbox(id = 0, email = "test-email")
}
