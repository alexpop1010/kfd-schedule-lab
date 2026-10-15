package schedule

interface CommandHandlers {
    fun period(command: Command.Period): Int
    fun byType(command: Command.ByType): Int
    fun errors(command: Command.Errors): Int
}

object StudentCommandHandlers : CommandHandlers {
    override fun period(command: Command.Period): Int = runPeriod(command)
    override fun byType(command: Command.ByType): Int = runType(command)
    override fun errors(command: Command.Errors): Int = runErrors(command)
}

fun runPeriod(command: Command.Period): Int {
    val parser = SessionParser()
    var total = 0
    var hasErrors = false

    forEachScheduleLine(command.file) { number, line ->
        when (val result = parser.parseLine(line, number.toInt())) {
            is SessionParseSuccess -> {
                if (result.session.date in command.from..command.to) {
                    println(formatSession(result.session, includeTeachers = false))
                    total++
                }
            }
            is SessionParseFailure -> hasErrors = true
        }
    }

    println("Total: $total")
    return if (hasErrors) 1 else 0
}

fun runType(command: Command.ByType): Int {
    val parser = SessionParser()
    var total = 0
    var hasErrors = false

    forEachScheduleLine(command.file) { number, line ->
        when (val result = parser.parseLine(line, number.toInt())) {
            is SessionParseSuccess -> {
                if (sessionType(result.session) == command.type) {
                    println(formatSession(result.session, includeTeachers = true))
                    total++
                }
            }
            is SessionParseFailure -> hasErrors = true
        }
    }

    println("Total: $total")
    return if (hasErrors) 1 else 0
}

fun runErrors(command: Command.Errors): Int {
    val parser = SessionParser()
    var lineCount = 0
    var rejectedCount = 0

    forEachScheduleLine(command.file) { number, line ->
        lineCount++
        val result = parser.parseLine(line, number.toInt())
        if (result is SessionParseFailure) {
            println(formatParseFailure(result))
            rejectedCount++
        }
    }

    println("Rejected: $rejectedCount of $lineCount")
    return if (rejectedCount > 0) 1 else 0
}

private fun sessionType(session: Session): SessionType {
    return when (session) {
        is Lecture -> SessionType.LECTURE
        is Practice -> SessionType.PRACTICE
        is Assessment -> SessionType.EXAM
        is Cancellation -> SessionType.CANCELLED
    }
}

private fun formatSession(session: Session, includeTeachers: Boolean): String {
    val location = when (session) {
        is Lecture -> session.location
        is Practice -> session.location
        is Assessment -> session.location
        is Cancellation -> "-"
    }
    val result = "${session.date} | ${sessionType(session)} | ${session.topic} | $location"

    if (!includeTeachers) {
        return result
    }

    val teachers = when (session) {
        is Lecture -> session.teachers.joinToString(", ")
        is Practice -> session.teachers.joinToString(", ")
        is Assessment -> session.teachers.joinToString(", ")
        is Cancellation -> "-"
    }
    return "$result | $teachers"
}
