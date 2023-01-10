package proton.android.pass.test.domain

import proton.pass.domain.AliasMailbox

object TestAliasMailbox {

    fun create(): AliasMailbox = AliasMailbox(id = 0, email = "test-email")
}
