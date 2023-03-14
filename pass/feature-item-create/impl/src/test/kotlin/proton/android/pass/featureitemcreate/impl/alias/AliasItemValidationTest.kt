package proton.android.pass.featureitemcreate.impl.alias

import com.google.common.truth.Truth.assertThat
import me.proton.core.util.kotlin.times
import org.junit.Test

class AliasItemValidationTest {

    @Test
    fun `empty title should return an error`() {
        val item = itemWithContents(title = "")

        val res = item.validate(allowEmptyTitle = false)
        assertThat(res.size).isEqualTo(1)
        assertThat(res.first()).isEqualTo(AliasItemValidationErrors.BlankTitle)
    }

    @Test
    fun `empty title allowing empty title should be ok`() {
        val item = itemWithContents(title = "")

        val res = item.validate(allowEmptyTitle = true)
        assertThat(res).isEmpty()
    }

    @Test
    fun `empty alias should return an error`() {
        val item = itemWithContents(prefix = "")

        val res = item.validate(allowEmptyTitle = false)
        assertThat(res.size).isEqualTo(1)
        assertThat(res.first()).isEqualTo(AliasItemValidationErrors.BlankPrefix)
    }

    @Test
    fun `alias with invalid characters return an error`() {
        val item = itemWithContents(prefix = "abc!=()")

        val res = item.validate(allowEmptyTitle = false)
        assertThat(res.size).isEqualTo(1)
        assertThat(res.first()).isEqualTo(AliasItemValidationErrors.InvalidAliasContent)
    }

    @Test
    fun `alias with valid special characters should not return error`() {
        val item = itemWithContents(prefix = "a.b_c-d")

        val res = item.validate(allowEmptyTitle = false)
        assertThat(res.isEmpty()).isTrue()
    }

    @Test
    fun `alias starting with dot should return error`() {
        val item = itemWithContents(prefix = ".somealias")

        val res = item.validate(allowEmptyTitle = false)
        assertThat(res.size).isEqualTo(1)
        assertThat(res.first()).isEqualTo(AliasItemValidationErrors.InvalidAliasContent)
    }

    @Test
    fun `alias ending with dot should return error`() {
        val item = itemWithContents(prefix = "somealias.")

        val res = item.validate(allowEmptyTitle = false)
        assertThat(res.size).isEqualTo(1)
        assertThat(res.first()).isEqualTo(AliasItemValidationErrors.InvalidAliasContent)
    }

    @Test
    fun `alias containing two dots should return error`() {
        val item = itemWithContents(prefix = "some..alias")

        val res = item.validate(allowEmptyTitle = false)
        assertThat(res.size).isEqualTo(1)
        assertThat(res.first()).isEqualTo(AliasItemValidationErrors.InvalidAliasContent)
    }

    @Test
    fun `alias containing two non-consecutive dots should not return error`() {
        val item = itemWithContents(prefix = "so.me.alias")

        val res = item.validate(allowEmptyTitle = false)
        assertThat(res.isEmpty()).isTrue()
    }

    @Test
    fun `alias containing uppercase should return error`() {
        val item = itemWithContents(prefix = "someAlias")

        val res = item.validate(allowEmptyTitle = false)
        assertThat(res.size).isEqualTo(1)
        assertThat(res.first()).isEqualTo(AliasItemValidationErrors.InvalidAliasContent)
    }

    @Test
    fun `empty mailboxes should return an error`() {
        val item = itemWithContents(mailboxes = emptyList())

        val res = item.validate(allowEmptyTitle = false)
        assertThat(res.size).isEqualTo(1)
        assertThat(res.first()).isEqualTo(AliasItemValidationErrors.NoMailboxes)
    }

    @Test
    fun `no mailbox selected should return an error`() {
        val item = itemWithContents(
            mailboxes = listOf(SelectedAliasMailboxUiModel(AliasMailboxUiModel(1, "email"), false))
        )

        val res = item.validate(allowEmptyTitle = false)
        assertThat(res.size).isEqualTo(1)
        assertThat(res.first()).isEqualTo(AliasItemValidationErrors.NoMailboxes)
    }

    @Test
    fun `prefix too long should return an error`() {
        val item = itemWithContents(prefix = "a".times(AliasItem.MAX_PREFIX_LENGTH + 1))

        val res = item.validate(allowEmptyTitle = false)
        assertThat(res.size).isEqualTo(1)
        assertThat(res.first()).isEqualTo(AliasItemValidationErrors.InvalidAliasContent)
    }

    @Test
    fun `prefix exactly MAX_PREFIX_LENGTH long should not return error`() {
        val item = itemWithContents(prefix = "a".times(AliasItem.MAX_PREFIX_LENGTH))

        val res = item.validate(allowEmptyTitle = false)
        assertThat(res).isEmpty()
    }

    private fun itemWithContents(
        title: String = "sometitle",
        prefix: String = "somealias",
        mailboxes: List<SelectedAliasMailboxUiModel>? = null
    ): AliasItem {
        return AliasItem(
            title = title,
            prefix = prefix,
            mailboxes = mailboxes ?: listOf(SelectedAliasMailboxUiModel(AliasMailboxUiModel(1, "email"), true))
        )
    }

}
