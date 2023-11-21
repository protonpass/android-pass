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

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import proton.android.pass.autofill.entities.AssistField
import proton.android.pass.autofill.entities.AutofillFieldId
import proton.android.pass.autofill.entities.FieldType

@Parcelize
sealed interface NodeCluster : Parcelable {

    fun isFocused(): Boolean
    fun fields(): List<AssistField>
    fun type(): String

    @Parcelize
    object Empty : NodeCluster {
        override fun isFocused() = true
        override fun fields(): List<AssistField> = emptyList()
        override fun type() = "NodeCluster.Empty"
    }

    @Parcelize
    sealed interface Login : NodeCluster {

        override fun isFocused() = fields().any { it.isFocused }

        @Parcelize
        @JvmInline
        value class OnlyUsername(val username: AssistField) : Login {
            override fun fields(): List<AssistField> = listOf(username)
            override fun type() = "OnlyUsername"
        }

        @Parcelize
        @JvmInline
        value class OnlyPassword(val password: AssistField) : Login {
            override fun fields(): List<AssistField> = listOf(password)
            override fun type() = "OnlyPassword"
        }

        @Parcelize
        data class UsernameAndPassword(
            val username: AssistField,
            val password: AssistField
        ) : Login {
            override fun fields(): List<AssistField> = listOf(username, password)
            override fun type() = "UsernameAndPassword"
        }
    }

    @Parcelize
    data class SignUp(
        val username: AssistField,
        val password: AssistField,
        val repeatPassword: AssistField
    ) : NodeCluster {
        override fun isFocused() = fields().any { it.isFocused }
        override fun fields(): List<AssistField> = listOf(username, password, repeatPassword)
        override fun type() = "SignUp"
    }

    @Parcelize
    data class CreditCard(
        val cardNumber: AssistField,
        val cardHolder: AssistField?,
        val cvv: AssistField?,
        val expiration: Expiration?
    ) : NodeCluster {

        @Parcelize
        sealed interface Expiration : Parcelable {

            fun fields(): List<AssistField>

            @JvmInline
            @Parcelize
            value class MmYySameField(val field: AssistField) : Expiration {
                override fun fields() = listOf(field)
            }

            @Parcelize
            data class MmYyDifferentfields(
                val month: AssistField,
                val year: AssistField
            ) : Expiration {
                override fun fields() = listOf(month, year)
            }

            @Parcelize
            data class MmYyyyDifferentfields(
                val month: AssistField,
                val year: AssistField
            ) : Expiration {
                override fun fields() = listOf(month, year)
            }
        }

        override fun isFocused() = fields().any { it.isFocused }
        override fun fields(): List<AssistField> {
            val fields = mutableListOf(cardNumber)
            cardHolder?.let { fields.add(it) }
            cvv?.let { fields.add(it) }
            expiration?.let { fields.addAll(it.fields()) }
            return fields
        }

        override fun type() = "CreditCard"
    }
}

fun List<NodeCluster>.focused(): NodeCluster {
    if (isEmpty()) return NodeCluster.Empty
    return firstOrNull { it.isFocused() } ?: first()
}

interface IdentifiableNode {
    val nodeId: AutofillFieldId?
    val parentPath: List<AutofillFieldId?>
}

object NodeClusterer {

    fun cluster(nodes: List<AssistField>): List<NodeCluster> {
        val clusters = mutableListOf<NodeCluster>()
        val addedNodes = mutableSetOf<AssistField>()

        clusterLogins(nodes, clusters, addedNodes)
        clusterCreditCards(nodes, clusters, addedNodes)

        return clusters
    }

    private fun clusterLogins(
        nodes: List<AssistField>,
        clusters: MutableList<NodeCluster>,
        addedNodes: MutableSet<AssistField>
    ) {
        val usernameFields = nodes.filter {
            it.type == FieldType.Username || it.type == FieldType.Email
        }
        val passwordFields = nodes.filter { it.type == FieldType.Password }

        if (usernameFields.size == 1 && passwordFields.size == 2) {
            val username = usernameFields.first()
            val password = passwordFields.first()
            val repeatPassword = passwordFields.last()
            clusters.add(NodeCluster.SignUp(username, password, repeatPassword))
            addedNodes.add(username)
            addedNodes.add(password)
            addedNodes.add(repeatPassword)
            return
        }

        for (usernameField in usernameFields) {
            val candidatePasswordFields = passwordFields.filter { !addedNodes.contains(it) }
            val nearestPasswordField = HeuristicsUtils.findNearestNodeByParentId(
                currentField = usernameField,
                fields = candidatePasswordFields
            )

            if (nearestPasswordField != null) {
                val remainingUsernames = usernameFields.filter { !addedNodes.contains(it) }
                val nearestUsernameToPassword = HeuristicsUtils.findNearestNodeByParentId(
                    currentField = nearestPasswordField,
                    fields = remainingUsernames
                )

                if (nearestUsernameToPassword == usernameField) {
                    clusters.add(
                        NodeCluster.Login.UsernameAndPassword(
                            username = usernameField,
                            password = nearestPasswordField
                        )
                    )
                    addedNodes.add(nearestPasswordField)
                } else {
                    clusters.add(NodeCluster.Login.OnlyUsername(usernameField))
                }
            } else {
                clusters.add(NodeCluster.Login.OnlyUsername(usernameField))
            }
            addedNodes.add(usernameField)
        }

        val remainingUsernameFields = usernameFields.filter { !addedNodes.contains(it) }
        val remainingPasswordFields = passwordFields.filter { !addedNodes.contains(it) }

        remainingUsernameFields.forEach { usernameField ->
            if (usernameField !in addedNodes) {
                clusters.add(NodeCluster.Login.OnlyUsername(usernameField))
                addedNodes.add(usernameField)
            }
        }

        remainingPasswordFields.forEach { passwordField ->
            if (passwordField !in addedNodes) {
                clusters.add(NodeCluster.Login.OnlyPassword(passwordField))
                addedNodes.add(passwordField)
            }
        }
    }

    private fun clusterCreditCards(
        nodes: List<AssistField>,
        clusters: MutableList<NodeCluster>,
        addedNodes: MutableSet<AssistField>
    ) {
        val cardNumberNode = nodes.firstOrNull { it.type == FieldType.CardNumber } ?: return
        val cardHolderNode = nodes.firstOrNull { it.type == FieldType.CardholderName }
        val cardCvvNode = nodes.firstOrNull { it.type == FieldType.CardCvv }

        val expirationMMNode = nodes.firstOrNull { it.type == FieldType.CardExpirationMM }
        val expirationMMYYNode = nodes
            .firstOrNull { it.type == FieldType.CardExpirationMMYY }

        val expirationYYNode = nodes.firstOrNull { it.type == FieldType.CardExpirationYY }
        val expirationYYYYNode = nodes
            .firstOrNull { it.type == FieldType.CardExpirationYYYY }

        val expiration = when {
            expirationMMYYNode != null -> {
                addedNodes.add(expirationMMYYNode)
                NodeCluster.CreditCard.Expiration.MmYySameField(expirationMMYYNode)
            }

            expirationMMNode != null && expirationYYNode != null -> {
                addedNodes.add(expirationMMNode)
                addedNodes.add(expirationYYNode)
                NodeCluster.CreditCard.Expiration.MmYyDifferentfields(
                    month = expirationMMNode,
                    year = expirationYYNode
                )
            }

            expirationMMNode != null && expirationYYYYNode != null -> {
                addedNodes.add(expirationMMNode)
                addedNodes.add(expirationYYYYNode)
                NodeCluster.CreditCard.Expiration.MmYyyyDifferentfields(
                    month = expirationMMNode,
                    year = expirationYYYYNode
                )
            }

            else -> null
        }

        val cluster = NodeCluster.CreditCard(
            cardNumber = cardNumberNode.also { addedNodes.add(it) },
            cardHolder = cardHolderNode?.also { addedNodes.add(it) },
            cvv = cardCvvNode?.also { addedNodes.add(it) },
            expiration = expiration
        )

        clusters.add(cluster)
    }
}
