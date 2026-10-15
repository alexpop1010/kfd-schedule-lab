package schedule

import java.time.LocalDate

data class Cancellation(
    override val date: LocalDate,
    override val topic: String,
) : Session {
    init {
        validateSession(date, topic)
    }
}
