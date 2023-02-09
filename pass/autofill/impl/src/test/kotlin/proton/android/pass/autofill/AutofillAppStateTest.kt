package proton.android.pass.autofill

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import proton.android.pass.autofill.entities.AutofillAppState
import proton.android.pass.autofill.entities.FieldType
import proton.android.pass.autofill.entities.isValid
import proton.android.pass.common.api.None
import proton.android.pass.common.api.toOption
import proton.pass.domain.entity.PackageName

class AutofillAppStateTest {

    @Test
    fun `empty androidAutofillIds returns isEmpty true`() {
        val state = AutofillAppState(
            packageName = PackageName("").toOption(),
            androidAutofillIds = listOf(),
            fieldTypes = listOf(FieldType.Email),
            webDomain = None,
            title = "123"
        )
        assertThat(state.isValid()).isTrue()
    }

}
