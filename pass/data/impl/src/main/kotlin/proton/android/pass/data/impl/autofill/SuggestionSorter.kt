package proton.android.pass.data.impl.autofill

import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.pass.domain.Item

object SuggestionSorter {

    fun sort(items: List<Item>, url: Option<String>): List<Item> =
        if (url is Some) {
            sort(items)
        } else {
            items
        }

    private fun sort(items: List<Item>): List<Item> = items

}
