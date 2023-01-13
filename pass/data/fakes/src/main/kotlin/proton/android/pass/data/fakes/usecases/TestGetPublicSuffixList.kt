package proton.android.pass.data.fakes.usecases

import proton.android.pass.data.api.usecases.GetPublicSuffixList

class TestGetPublicSuffixList(private val tlds: Set<String>) : GetPublicSuffixList {
    override fun invoke(): Set<String> = tlds
}
