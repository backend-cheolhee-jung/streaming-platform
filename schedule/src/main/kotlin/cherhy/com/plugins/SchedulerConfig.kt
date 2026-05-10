package cherhy.com.plugins

import cherhy.com.util.extension.toShedlock
import extension.ktor.exposed.Shedlocks
import extension.ktor.exposed.reactiveTransaction
import extension.ktor.exposed.shedlock
import extension.ktor.schedule
import io.ktor.server.application.*
import mu.KotlinLogging
import org.jetbrains.exposed.v1.core.SortOrder.DESC
import org.jetbrains.exposed.v1.jdbc.selectAll
import kotlin.time.Duration.Companion.minutes

fun Application.configureScheduler() {
    val logger = KotlinLogging.logger { }

    schedule(EVERY_DAY_AT_12_AM) {
        shedlock(TEST_SCHEDULED, 10.minutes) {
            reactiveTransaction {
                val lastShedlock = Shedlocks.selectAll()
                    .where { Shedlocks.name eq TEST_SCHEDULED }
                    .orderBy(Shedlocks.lockedAt, DESC)
                    .last()
                    .toShedlock()

                logger.info { "Last shedlock=$lastShedlock" }
            }
        }
    }
}

private const val TEST_SCHEDULED = "test"
private const val EVERY_DAY_AT_12_AM = "0 0 12 * * ?"