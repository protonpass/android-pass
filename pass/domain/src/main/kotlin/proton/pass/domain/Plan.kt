package proton.pass.domain

data class Plan(
    val planType: PlanType,
    val hideUpgrade: Boolean,
    val vaultLimit: PlanLimit,
    val aliasLimit: PlanLimit,
    val totpLimit: PlanLimit,
    val updatedAt: Long
)

sealed interface PlanLimit {

    fun limitOrNull(): Int?

    object Unlimited : PlanLimit {
        override fun limitOrNull() = null
    }
    data class Limited(val limit: Int) : PlanLimit {
        override fun limitOrNull() = limit
    }
}

sealed interface PlanType {

    fun humanReadableName(): String
    fun internalName(): String

    object Free : PlanType {
        override fun humanReadableName(): String = "Proton Free"
        override fun internalName(): String = "free"
    }

    data class Unknown(val internal: String, val humanReadable: String) : PlanType {
        override fun humanReadableName(): String = humanReadable
        override fun internalName(): String = internal
    }

    data class Trial(
        val internal: String,
        val humanReadable: String,
        val remainingDays: Int
    ) : PlanType {
        override fun humanReadableName(): String = humanReadable
        override fun internalName(): String = internal
    }

    data class Paid(val internal: String, val humanReadable: String) : PlanType {
        override fun humanReadableName(): String = humanReadable
        override fun internalName(): String = internal
    }

    companion object {
        const val PLAN_NAME_FREE = "free"
        const val PLAN_NAME_PLUS = "plus"
    }
}
