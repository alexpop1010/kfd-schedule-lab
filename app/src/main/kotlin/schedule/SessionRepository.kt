package schedule

interface SessionRepository {
    fun add(session: Session)

    fun addAll(sessions: List<Session>)

    fun all(): List<Session>
}
