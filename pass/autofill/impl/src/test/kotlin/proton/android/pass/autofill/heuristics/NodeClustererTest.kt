/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.autofill.heuristics

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import proton.android.pass.autofill.TestAutofillId
import proton.android.pass.autofill.entities.AssistField
import proton.android.pass.autofill.entities.DetectionType
import proton.android.pass.autofill.entities.FieldType

class NodeClustererTest {

    @Test
    fun `can handle empty list`() {
        val clusters = NodeClusterer.cluster(emptyList())
        assert(clusters.isEmpty())
    }

    @Test
    fun `can handle single username`() {
        val field = loginField(1, emptyList())
        val clusters = NodeClusterer.cluster(listOf(field))
        assertThat(clusters).isEqualTo(listOf(NodeCluster.Login.OnlyUsername(field)))
    }

    @Test
    fun `can handle single password`() {
        val field = passwordField(1, emptyList())
        val clusters = NodeClusterer.cluster(listOf(field))
        assertThat(clusters).isEqualTo(listOf(NodeCluster.Login.OnlyPassword(field)))
    }

    @Test
    fun `can handle username and password`() {
        val username = loginField(1, emptyList())
        val password = passwordField(2, emptyList())
        val clusters = NodeClusterer.cluster(listOf(username, password))
        assertThat(clusters).isEqualTo(listOf(NodeCluster.Login.UsernameAndPassword(username, password)))
    }

    /**
     * SECTION 10 (UsernameAndPassword)
     * username1 (node 11)
     * password1 (node 12)
     *
     * SECTION 20 (UsernameAndPassword)
     * username2 (node 21)
     * password2 (node 22)
     */
    @Test
    fun `can handle 2 usernames and 2 passwords`() {
        val username1 = loginField(11, listOf(1, 10, 11))
        val password1 = passwordField(12, listOf(1, 10, 12))

        val username2 = loginField(21, listOf(1, 20, 21))
        val password2 = passwordField(22, listOf(1, 20, 22))
        val clusters = NodeClusterer.cluster(listOf(username1, username2, password1, password2))

        val cluster1 = NodeCluster.Login.UsernameAndPassword(username1, password1)
        val cluster2 = NodeCluster.Login.UsernameAndPassword(username2, password2)
        assertThat(clusters).isEqualTo(listOf(cluster1, cluster2))
    }

    /**
     * SECTION 10 (SignUp)
     * username (node 11)
     * password (node 12)
     * password2 (node 13)
     */
    @Test
    fun `can handle 1 username and 2 passwords`() {
        val username = loginField(11, listOf(1, 10, 11))
        val password1 = passwordField(12, listOf(1, 10, 12))
        val password2 = passwordField(13, listOf(1, 10, 13))
        val clusters = NodeClusterer.cluster(listOf(username, password1, password2))

        assertThat(clusters).isEqualTo(listOf(NodeCluster.SignUp(username, password1, password2)))
    }

    /**
     * SECTION 10 (SignUp)
     * username (node 11)
     * email (node 12)
     * password (node 13)
     * password2 (node 14)
     */
    @Test
    fun `can handle 1 username 1 email and 2 passwords`() {
        val username = loginField(11, listOf(1, 10, 11))
        val email = emailField(12, listOf(1, 10, 12))
        val password1 = passwordField(13, listOf(1, 10, 13))
        val password2 = passwordField(14, listOf(1, 10, 14))
        val clusters = NodeClusterer.cluster(listOf(username, email, password1, password2))

        assertThat(clusters).isEqualTo(
            listOf(NodeCluster.SignUp(username, password1, password2, email))
        )
    }

    /**
     * SECTION 10 (UsernameAndPassword)
     * username1 (node 11)
     * password (node 12)
     *
     * SECTION 20 (OnlyUsername)
     * username2 (node 21)
     */
    @Test
    fun `can handle 2 usernames and 1 password`() {
        val username1 = loginField(11, listOf(1, 10, 11))
        val password = passwordField(12, listOf(1, 10, 12))
        val username2 = loginField(21, listOf(1, 20, 21))
        val clusters = NodeClusterer.cluster(listOf(username1, username2, password))

        val cluster1 = NodeCluster.Login.UsernameAndPassword(username1, password)
        val cluster2 = NodeCluster.Login.OnlyUsername(username2)
        assertThat(clusters).isEqualTo(listOf(cluster1, cluster2))
    }

    /**
     * SECTION 10 (OnlyUsername)
     * username1 (node 11)
     *
     * SECTION 20 (UsernameAndPassword)
     * username2 (node 21)
     * password (node 22)
     */
    @Test
    fun `can handle single username and username+password`() {
        val username1 = loginField(11, listOf(1, 10, 11))
        val username2 = loginField(21, listOf(1, 20, 21))
        val password = passwordField(22, listOf(1, 20, 22))
        val clusters = NodeClusterer.cluster(listOf(username1, username2, password))

        val cluster1 = NodeCluster.Login.OnlyUsername(username1)
        val cluster2 = NodeCluster.Login.UsernameAndPassword(username2, password)
        assertThat(clusters).isEqualTo(listOf(cluster1, cluster2))
    }

    @Test
    fun `returns the right url for the cluster`() {
        val url = "some.url"
        val cluster = NodeCluster.Login.UsernameAndPassword(
            username = loginField(11, emptyList(), url),
            password = loginField(12, emptyList())
        )

        assertThat(cluster.url()).isEqualTo(url)
    }

    private fun loginField(
        id: Int,
        nodePath: List<Int>,
        url: String? = null
    ): AssistField = AssistField(
        id = TestAutofillId(id),
        type = FieldType.Username,
        detectionType = null,
        value = null,
        text = null,
        isFocused = false,
        nodePath = nodePath.map(::TestAutofillId),
        url = url
    )

    private fun emailField(
        id: Int,
        nodePath: List<Int>,
        url: String? = null
    ): AssistField = AssistField(
        id = TestAutofillId(id),
        type = FieldType.Email,
        detectionType = null,
        value = null,
        text = null,
        isFocused = false,
        nodePath = nodePath.map(::TestAutofillId),
        url = url
    )

    private fun passwordField(
        id: Int,
        nodePath: List<Int>,
        detectionType: DetectionType? = null
    ): AssistField = AssistField(
        id = TestAutofillId(id),
        type = FieldType.Password,
        detectionType = detectionType,
        value = null,
        text = null,
        isFocused = false,
        nodePath = nodePath.map(::TestAutofillId),
        url = null
    )
}
