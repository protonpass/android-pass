package proton.pass.domain

data class Plan(
    val planType: PlanType,
    val vaultLimit: Int,
    val aliasLimit: Int,
    val totpLimit: Int,
    val updatedAt: Long
)

sealed interface PlanType {

    fun humanReadableName(): String
    fun internalName(): String

    object Free : PlanType {
        override fun humanReadableName(): String = "Proton Free"
        override fun internalName(): String = "free"
    }

    data class Paid(val internal: String, val humanReadable: String) : PlanType {
        override fun humanReadableName(): String = humanReadable
        override fun internalName(): String = internal
    }
}
