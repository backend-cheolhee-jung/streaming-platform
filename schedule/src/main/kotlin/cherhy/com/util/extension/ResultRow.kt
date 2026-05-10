package cherhy.com.util.extension

import extension.ktor.exposed.Shedlock
import org.jetbrains.exposed.v1.core.ResultRow

fun ResultRow.toShedlock() = Shedlock.of(this)