package me.proton.android.pass.ui.create.alias

import me.proton.pass.presentation.create.alias.InitialCreateAliasUiState
import me.proton.pass.presentation.create.alias.alias
import org.junit.Test
import kotlin.test.assertEquals

class InitialCreateAliasUiStateTest {

    @Test
    fun `alias with null title returns empty string`() {
        assertEquals(InitialCreateAliasUiState(null).alias(), "")
    }

    @Test
    fun `alias with empty title returns empty string`() {
        assertEquals(InitialCreateAliasUiState("").alias(), "")
    }

    @Test
    fun `alias removes special characters`() {
        assertEquals(InitialCreateAliasUiState("a_b@c#d=e%f-g").alias(), "a_bcdef-g")
    }

    @Test
    fun `alias removes spaces characters`() {
        assertEquals(InitialCreateAliasUiState("a b  c   d e").alias(), "a-b--c---d-e")
    }

    @Test
    fun `alias clears capitalization`() {
        assertEquals(InitialCreateAliasUiState("aBCdEFg").alias(), "abcdefg")
    }
}
