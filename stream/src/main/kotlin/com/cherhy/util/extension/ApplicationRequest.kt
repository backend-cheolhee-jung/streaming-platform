package com.cherhy.util.extension

import com.cherhy.common.util.USER_ID
import com.cherhy.common.util.model.toUserId
import io.ktor.http.*
import io.ktor.server.request.*

val ApplicationRequest.userId
    get() = this.headers[USER_ID]?.toLongOrNull()?.toUserId()
        ?: throw IllegalArgumentException("$USER_ID header is required")

val ApplicationRequest.lastWatchedCheckpoint
    get() = this.headers[HttpHeaders.Range]
        ?.substringAfter("bytes=")
        ?.substringBefore("-")
        ?.toLongOrNull()
        ?.takeIf { it >= 0 }
        ?.toByte()