package schedule

import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SessionModelTest {
    @Test
    fun rejectsEmptyTopic() {
        assertFailsWith<IllegalArgumentException> {
            Cancellation(LocalDate.of(2026, 10, 15), "")
        }
    }

    @Test
    fun rejectsYearOutsideAllowedRange() {
        assertFailsWith<IllegalArgumentException> {
            Cancellation(LocalDate.of(0, 10, 15), "Kotlin")
        }
        assertFailsWith<IllegalArgumentException> {
            Cancellation(LocalDate.of(10000, 10, 15), "Kotlin")
        }
    }

    @Test
    fun rejectsActiveSessionWithoutTeachers() {
        assertFailsWith<IllegalArgumentException> {
            Lecture(
                LocalDate.of(2026, 10, 15),
                "Kotlin",
                "Room 1",
                emptyList(),
            )
        }
    }

    @Test
    fun rejectsAssessmentWithOneTeacher() {
        assertFailsWith<IllegalArgumentException> {
            Assessment(
                LocalDate.of(2026, 10, 15),
                "Kotlin",
                "Room 1",
                listOf("Ivanov"),
            )
        }
    }

    @Test
    fun rejectsAssessmentWithDuplicateTeachers() {
        assertFailsWith<IllegalArgumentException> {
            Assessment(
                LocalDate.of(2026, 10, 15),
                "Kotlin",
                "Room 1",
                listOf("Ivanov", "Ivanov"),
            )
        }
    }

    @Test
    fun changingSourceListDoesNotChangeSession() {
        val names = mutableListOf("Ivanov", "Petrov")
        val assessment = Assessment(
            LocalDate.of(2026, 10, 15),
            "Kotlin",
            "Room 1",
            names,
        )

        names.clear()

        assertEquals(listOf("Ivanov", "Petrov"), assessment.teachers)
    }

    @Test
    fun teachersListDoesNotAllowChangingSession() {
        val assessment = Assessment(
            LocalDate.of(2026, 10, 15),
            "Kotlin",
            "Room 1",
            listOf("Ivanov", "Petrov"),
        )

        assertFailsWith<UnsupportedOperationException> {
            (assessment.teachers as MutableList<String>).clear()
        }
        assertEquals(listOf("Ivanov", "Petrov"), assessment.teachers)
    }

    @Test
    fun copyCannotBypassValidation() {
        val cancellation = Cancellation(
            LocalDate.of(2026, 10, 15),
            "Kotlin",
        )

        assertFailsWith<IllegalArgumentException> {
            cancellation.copy(topic = " ")
        }
    }

    @Test
    fun createsAllSessionTypes() {
        val date = LocalDate.of(2026, 10, 15)

        val lecture = Lecture(date, "Lecture", "Room 1", listOf("Ivanov"))
        val practice = Practice(date, "Practice", "Room 2", listOf("Petrov"))
        val assessment = Assessment(
            date,
            "Assessment",
            "Room 3",
            listOf("Ivanov", "Petrov"),
        )
        val cancellation = Cancellation(date, "Cancellation")

        assertEquals("Lecture", lecture.topic)
        assertEquals("Practice", practice.topic)
        assertEquals("Assessment", assessment.topic)
        assertEquals("Cancellation", cancellation.topic)
    }

    @Test
    fun repositoryReturnsProtectedSnapshot() {
        val repository = InMemorySessionRepository()
        val first = Cancellation(LocalDate.of(2026, 10, 15), "First")
        val second = Cancellation(LocalDate.of(2026, 10, 16), "Second")
        repository.add(first)

        val snapshot = repository.all()
        repository.add(second)

        assertEquals(listOf(first), snapshot)
        assertEquals(listOf(first, second), repository.all())
        assertFailsWith<UnsupportedOperationException> {
            (snapshot as MutableList<Session>).clear()
        }
    }

    @Test
    fun auditsSingleAndBatchAddition() {
        val repository = AuditedSessionRepository(InMemorySessionRepository())
        val first = Cancellation(LocalDate.of(2026, 10, 15), "First")
        val second = Cancellation(LocalDate.of(2026, 10, 16), "Second")
        val third = Cancellation(LocalDate.of(2026, 10, 17), "Third")
        val fourth = Cancellation(LocalDate.of(2026, 10, 18), "Fourth")

        repository.add(first)
        repository.addAll(listOf(second, third, fourth))

        assertEquals(4, repository.addedCount)
        assertEquals(
            listOf(AuditEvent("add", 1), AuditEvent("addAll", 3)),
            repository.auditEvents,
        )
        assertEquals(listOf(first, second, third, fourth), repository.all())
    }

    @Test
    fun emptyBatchCreatesAuditEvent() {
        val repository = AuditedSessionRepository(InMemorySessionRepository())

        repository.addAll(emptyList())

        assertEquals(0, repository.addedCount)
        assertEquals(listOf(AuditEvent("addAll", 0)), repository.auditEvents)
    }
}
