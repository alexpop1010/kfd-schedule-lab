package schedule

class AuditedSessionRepository(
    private val delegate: SessionRepository,
) : SessionRepository by delegate {
    private val events = mutableListOf<AuditEvent>()

    var addedCount: Int = 0
        private set

    val auditEvents: List<AuditEvent>
        get() = java.util.List.copyOf(events)

    override fun add(session: Session) {
        delegate.add(session)
        addedCount++
        events.add(AuditEvent("add", 1))
    }

    override fun addAll(sessions: List<Session>) {
        delegate.addAll(sessions)
        addedCount += sessions.size
        events.add(AuditEvent("addAll", sessions.size))
    }
}
