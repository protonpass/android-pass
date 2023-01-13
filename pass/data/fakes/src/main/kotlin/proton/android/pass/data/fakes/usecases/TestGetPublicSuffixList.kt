package proton.android.pass.data.fakes.usecases

import proton.android.pass.data.api.usecases.GetPublicSuffixList

class TestGetPublicSuffixList : GetPublicSuffixList {

    private var tlds: Set<String> = emptySet()

    fun setTlds(value: Set<String>) {
        tlds = value
    }

    override fun invoke(): Set<String> = tlds
}
