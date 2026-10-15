package schedule

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle

class SessionParser {
    private val datePattern = Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}")
    private val dateFormatter = DateTimeFormatter
        .ofPattern("uuuu-MM-dd")
        .withResolverStyle(ResolverStyle.STRICT)
    private val fieldNames = listOf("date", "type", "topic", "location", "teachers")

    fun parse(lines: List<String>): List<SessionParseResult> {
        return lines.mapIndexed { index, line -> parseLine(line, index + 1) }
    }

    fun parseLine(line: String, lineNumber: Int): SessionParseResult {
        if (!line.contains('|')) {
            return failure(lineNumber, ParseErrorKind.INVALID_SEPARATOR)
        }

        val fields = line.split('|')
        if (fields.size != 5) {
            return failure(
                lineNumber,
                ParseErrorKind.WRONG_FIELD_COUNT,
                actualCount = fields.size,
            )
        }

        for (index in fields.indices) {
            if (fields[index].isBlank()) {
                return failure(
                    lineNumber,
                    ParseErrorKind.EMPTY_REQUIRED_FIELD,
                    fieldName = fieldNames[index],
                )
            }
        }

        val date = parseDate(fields[0])
            ?: return failure(
                lineNumber,
                ParseErrorKind.INVALID_DATE,
                value = fields[0],
            )

        val type = fields[1]
        if (type !in setOf("LECTURE", "PRACTICE", "EXAM", "CANCELLED")) {
            return failure(
                lineNumber,
                ParseErrorKind.UNKNOWN_SESSION_TYPE,
                value = type,
            )
        }

        val teachers = fields[4].split(',')
        if (type == "EXAM") {
            val validTeacherCount = teachers.count { it.isNotBlank() && it != "-" }
            if (validTeacherCount < 2) {
                return failure(
                    lineNumber,
                    ParseErrorKind.NOT_ENOUGH_TEACHERS,
                    actualCount = validTeacherCount,
                )
            }
        }

        findControlCharacter(fields)?.let { fieldIndex ->
            return failure(
                lineNumber,
                ParseErrorKind.CONTROL_CHARACTER,
                fieldName = fieldNames[fieldIndex],
                value = fields[fieldIndex],
            )
        }

        for (index in fields.indices) {
            if (fields[index] != fields[index].trim()) {
                return failure(
                    lineNumber,
                    ParseErrorKind.OUTER_WHITESPACE,
                    fieldName = fieldNames[index],
                    value = fields[index],
                )
            }
        }

        for (teacher in teachers) {
            if (teacher != teacher.trim()) {
                return failure(
                    lineNumber,
                    ParseErrorKind.OUTER_WHITESPACE,
                    fieldName = "teachers",
                    value = teacher,
                )
            }
        }

        if (type == "CANCELLED") {
            if (fields[3] != "-") {
                return failure(
                    lineNumber,
                    ParseErrorKind.INVALID_CANCELLATION_FIELD,
                    fieldName = "location",
                    value = fields[3],
                )
            }
            if (fields[4] != "-") {
                return failure(
                    lineNumber,
                    ParseErrorKind.INVALID_CANCELLATION_FIELD,
                    fieldName = "teachers",
                    value = fields[4],
                )
            }
            return SessionParseSuccess(Cancellation(date, fields[2]))
        }

        if (fields[3] == "-") {
            return failure(
                lineNumber,
                ParseErrorKind.INVALID_LOCATION,
                fieldName = "location",
                value = fields[3],
            )
        }

        for (teacher in teachers) {
            if (teacher.isBlank() || teacher == "-") {
                return failure(
                    lineNumber,
                    ParseErrorKind.INVALID_TEACHER,
                    fieldName = "teachers",
                    value = teacher,
                )
            }
        }

        val seenTeachers = mutableSetOf<String>()
        for (teacher in teachers) {
            if (!seenTeachers.add(teacher)) {
                return failure(
                    lineNumber,
                    ParseErrorKind.DUPLICATE_TEACHER,
                    fieldName = "teachers",
                    value = teacher,
                )
            }
        }

        val session = when (type) {
            "LECTURE" -> Lecture(date, fields[2], fields[3], teachers)
            "PRACTICE" -> Practice(date, fields[2], fields[3], teachers)
            else -> Assessment(date, fields[2], fields[3], teachers)
        }
        return SessionParseSuccess(session)
    }

    private fun parseDate(value: String): LocalDate? {
        if (!datePattern.matches(value)) {
            return null
        }

        return try {
            val date = LocalDate.parse(value, dateFormatter)
            if (date.year in 1..9999) date else null
        } catch (_: DateTimeParseException) {
            null
        }
    }

    private fun findControlCharacter(fields: List<String>): Int? {
        for (index in fields.indices) {
            if (fields[index].any { it.isISOControl() }) {
                return index
            }
        }
        return null
    }

    private fun failure(
        lineNumber: Int,
        kind: ParseErrorKind,
        fieldName: String? = null,
        value: String? = null,
        actualCount: Int? = null,
    ): SessionParseFailure {
        return SessionParseFailure(
            lineNumber,
            ParseErrorReason(kind, fieldName, value, actualCount),
        )
    }
}
