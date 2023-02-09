package proton.android.pass.autofill

import com.google.common.truth.Truth.assertThat
import proton.android.pass.autofill.entities.AutofillAppState
import proton.android.pass.autofill.entities.FieldType
import proton.android.pass.autofill.entities.isValid
import proton.android.pass.common.api.None
import proton.pass.domain.entity.PackageName
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
        assertThat(state.isValid()).isTrue()
    }

}
