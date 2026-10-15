package schedule

data class ParseErrorReason(
    val kind: ParseErrorKind,
    val fieldName: String? = null,
    val value: String? = null,
    val actualCount: Int? = null,
)
