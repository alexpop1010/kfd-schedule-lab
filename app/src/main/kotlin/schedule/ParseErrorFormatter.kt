package schedule

fun formatParseFailure(failure: SessionParseFailure): String {
    val line = "line ${failure.lineNumber}: "
    val reason = failure.reason

    return line + when (reason.kind) {
        ParseErrorKind.INVALID_SEPARATOR -> "invalid field separator"
        ParseErrorKind.WRONG_FIELD_COUNT ->
            "got ${reason.actualCount} fields, expected 5"
        ParseErrorKind.EMPTY_REQUIRED_FIELD ->
            "empty required field \"${reason.fieldName}\""
        ParseErrorKind.INVALID_DATE ->
            "invalid date \"${escapeErrorValue(reason.value.orEmpty())}\""
        ParseErrorKind.UNKNOWN_SESSION_TYPE ->
            "unknown session type \"${escapeErrorValue(reason.value.orEmpty())}\""
        ParseErrorKind.NOT_ENOUGH_TEACHERS ->
            "type \"EXAM\" requires at least 2 teachers, got ${reason.actualCount}"
        ParseErrorKind.CONTROL_CHARACTER ->
            "field \"${reason.fieldName}\" contains control characters " +
                "\"${escapeErrorValue(reason.value.orEmpty())}\""
        ParseErrorKind.OUTER_WHITESPACE ->
            "field \"${reason.fieldName}\" has leading or trailing whitespace " +
                "\"${escapeErrorValue(reason.value.orEmpty())}\""
        ParseErrorKind.INVALID_CANCELLATION_FIELD ->
            "type \"CANCELLED\" requires field \"${reason.fieldName}\" to be \"-\""
        ParseErrorKind.INVALID_LOCATION ->
            "invalid location \"${escapeErrorValue(reason.value.orEmpty())}\""
        ParseErrorKind.INVALID_TEACHER ->
            "invalid teacher \"${escapeErrorValue(reason.value.orEmpty())}\""
        ParseErrorKind.DUPLICATE_TEACHER ->
            "duplicate teacher \"${escapeErrorValue(reason.value.orEmpty())}\""
    }
}

fun escapeErrorValue(value: String): String {
    val result = StringBuilder()

    for (character in value) {
        when {
            character == '\\' -> result.append("\\\\")
            character == '"' -> result.append("\\\"")
            character.isISOControl() || character.code > 127 -> {
                result.append("\\u")
                result.append(character.code.toString(16).uppercase().padStart(4, '0'))
            }
            else -> result.append(character)
        }
    }

    return result.toString()
}
