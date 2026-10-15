package schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SessionParserTest {
    private val parser = SessionParser()

    @Test
    fun invalidDateHasPriorityOverUnknownType() {
        val result = parser.parseLine(
            "2026-13-45|SEMINAR|Topic|Room 1|Ivanov",
            1,
        )

        val failure = assertIs<SessionParseFailure>(result)
        assertEquals(ParseErrorKind.INVALID_DATE, failure.reason.kind)
        assertEquals(
            "line 1: invalid date \"2026-13-45\"",
            formatParseFailure(failure),
        )
    }

    @Test
    fun duplicateTeacherIsReportedAfterTeacherCountCheck() {
        val result = parser.parseLine(
            "2026-10-05|EXAM|Topic|Room 1|Ivanov,Ivanov",
            1,
        )

        val failure = assertIs<SessionParseFailure>(result)
        assertEquals(ParseErrorKind.DUPLICATE_TEACHER, failure.reason.kind)
        assertEquals(
            "line 1: duplicate teacher \"Ivanov\"",
            formatParseFailure(failure),
        )
    }
}
