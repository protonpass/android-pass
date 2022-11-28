package me.proton.android.pass.ui.create.alias

import com.google.common.truth.Truth.assertThat
import me.proton.pass.domain.AliasMailbox
import me.proton.pass.presentation.create.alias.AliasItem
import me.proton.pass.presentation.create.alias.AliasItemValidationErrors
import me.proton.pass.presentation.create.alias.AliasMailboxUiModel
import org.junit.Test

class AliasItemValidationTest {

    @Test
    fun `empty title should return an error`() {
        val item = itemWithContents(title = "")

        val res = item.validate()
        assertThat(res.size).isEqualTo(1)
        assertThat(res.first()).isEqualTo(AliasItemValidationErrors.BlankTitle)
    }

    @Test
    fun `empty alias should return an error`() {
        val item = itemWithContents(alias = "")

        val res = item.validate()
        assertThat(res.size).isEqualTo(1)
        assertThat(res.first()).isEqualTo(AliasItemValidationErrors.BlankAlias)
    }

    @Test
    fun `alias with invalid characters return an error`() {
        val item = itemWithContents(alias = "abc!=()")

        val res = item.validate()
        assertThat(res.size).isEqualTo(1)
        assertThat(res.first()).isEqualTo(AliasItemValidationErrors.InvalidAliasContent)
    }

    @Test
    fun `alias with valid special characters should not return error`() {
        val item = itemWithContents(alias = "a.b_c-d")

        val res = item.validate()
        assertThat(res.isEmpty()).isTrue()
    }

    @Test
    fun `alias starting with dot should return error`() {
        val item = itemWithContents(alias = ".somealias")

        val res = item.validate()
        assertThat(res.size).isEqualTo(1)
        assertThat(res.first()).isEqualTo(AliasItemValidationErrors.InvalidAliasContent)
    }

    @Test
    fun `alias ending with dot should return error`() {
        val item = itemWithContents(alias = "somealias.")

        val res = item.validate()
        assertThat(res.size).isEqualTo(1)
        assertThat(res.first()).isEqualTo(AliasItemValidationErrors.InvalidAliasContent)
    }

    @Test
    fun `alias containing two dots should return error`() {
        val item = itemWithContents(alias = "some..alias")

        val res = item.validate()
        assertThat(res.size).isEqualTo(1)
        assertThat(res.first()).isEqualTo(AliasItemValidationErrors.InvalidAliasContent)
    }

    @Test
    fun `alias containing two non-consecutive dots should not return error`() {
        val item = itemWithContents(alias = "so.me.alias")

        val res = item.validate()
        assertThat(res.isEmpty()).isTrue()
    }

    @Test
    fun `alias containing uppercase should return error`() {
        val item = itemWithContents(alias = "someAlias")

        val res = item.validate()
        assertThat(res.size).isEqualTo(1)
        assertThat(res.first()).isEqualTo(AliasItemValidationErrors.InvalidAliasContent)
    }

    @Test
    fun `empty mailboxes should return an error`() {
        val item = itemWithContents(mailboxes = emptyList())

        val res = item.validate()
        assertThat(res.size).isEqualTo(1)
        assertThat(res.first()).isEqualTo(AliasItemValidationErrors.NoMailboxes)
    }

    @Test
    fun `no mailbox selected should return an error`() {
        val item = itemWithContents(
            mailboxes = listOf(AliasMailboxUiModel(AliasMailbox(1, "email"), false))
        )

        val res = item.validate()
        assertThat(res.size).isEqualTo(1)
        assertThat(res.first()).isEqualTo(AliasItemValidationErrors.NoMailboxes)
    }


    private fun itemWithContents(
        title: String = "sometitle",
        alias: String = "somealias",
        mailboxes: List<AliasMailboxUiModel>? = null
    ): AliasItem {
        return AliasItem(
            title = title,
            alias = alias,
            mailboxes = mailboxes ?: listOf(AliasMailboxUiModel(AliasMailbox(1, "email"), true))
        )
    }

}
