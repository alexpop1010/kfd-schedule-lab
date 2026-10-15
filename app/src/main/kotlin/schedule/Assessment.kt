package schedule

import java.time.LocalDate

class Assessment(
    override val date: LocalDate,
    override val topic: String,
    val location: String,
    teachers: List<String>,
) : Session {
    val teachers: List<String> = java.util.List.copyOf(teachers)

    init {
        validateSession(date, topic)
        validateActiveSession(location, this.teachers)
        require(this.teachers.distinct().size >= 2) {
            "Assessment must have at least two different teachers"
        }
    }
}
