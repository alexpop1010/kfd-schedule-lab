package schedule

import java.time.LocalDate

sealed interface Session {
    val date: LocalDate
    val topic: String
}

fun validateSession(date: LocalDate, topic: String) {
    require(date.year in 1..9999) {
        "Session year must be between 1 and 9999"
    }
    validateText(topic, "Topic")
}

fun validateActiveSession(location: String, teachers: List<String>) {
    validateText(location, "Location")
    require(location != "-") {
        "Location must not be -"
    }
    require(teachers.distinct().size == teachers.size) {
        "Teachers must not contain duplicates"
    }

    for (teacher in teachers) {
        validateText(teacher, "Teacher name")
        require(teacher != "-") {
            "Teacher name must not be -"
        }
    }
}

private fun validateText(value: String, fieldName: String) {
    require(value.isNotBlank()) {
        "$fieldName must not be blank"
    }
    require(value == value.trim()) {
        "$fieldName must not have leading or trailing whitespace"
    }
    require(value.none { it.isISOControl() }) {
        "$fieldName must not contain control characters"
    }
}
