package schedule

data class SessionParseFailure(
    val lineNumber: Int,
    val reason: ParseErrorReason,
) : SessionParseResult
