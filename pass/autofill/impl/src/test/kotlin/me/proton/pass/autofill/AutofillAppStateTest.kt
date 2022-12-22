package me.proton.pass.autofill

import com.google.common.truth.Truth.assertThat
import me.proton.pass.autofill.entities.AutofillAppState
import me.proton.pass.autofill.entities.FieldType
import me.proton.pass.autofill.entities.isEmpty
import me.proton.pass.common.api.None
import me.proton.pass.domain.entity.PackageName
import org.junit.Test

class AutofillAppStateTest {

    @Test
    fun `empty androidAutofillIds returns isEmpty true`() {
        val state = AutofillAppState(
            packageName = PackageName(""),
            androidAutofillIds = listOf(),
            fieldTypes = listOf(FieldType.Email),
            webDomain = None,
            title = "123"
        )
        assertThat(state.isEmpty()).isTrue()
    }

}
