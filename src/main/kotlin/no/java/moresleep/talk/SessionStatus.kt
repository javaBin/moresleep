package no.java.moresleep.talk



enum class SessionStatus {
    DRAFT,
    SUBMITTED,
    APPROVED,
    REJECTED,
    HISTORIC;

    companion object {
        fun saveValue(value:String) = values().firstOrNull { it.name == value }

        val publcStatuses = setOf(APPROVED,HISTORIC)
    }

    val isPublicStatus:Boolean by lazy { publcStatuses.contains(this) }
}