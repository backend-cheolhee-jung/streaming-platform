package com.cherhy.util.extension

import com.cherhy.api.VideoRequest
import com.cherhy.domain.VideoName
import com.cherhy.domain.VideoSize
import com.cherhy.domain.VideoUniqueName
import com.cherhy.util.PathParameter
import com.cherhy.util.VideoValidator
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*

val mapper = jacksonObjectMapper()

val ApplicationCall.pathParameter
    get() = PathParameter(this)

inline fun <reified T : Any> ApplicationCall.getQueryParams(): T {
    return this.request.queryParameters.toClass()
}

data class MultipartVideoData(
    val video: VideoRequest,
    val fields: Map<String, String>,
)

suspend fun ApplicationCall.getVideoWithFields(): MultipartVideoData {
    val multipart = receiveMultipart()
    val fields = mutableMapOf<String, String>()
    var fileName: String? = null
    val buffer = ByteArrayOutputStream()

    multipart.forEachPart { part ->
        when (part) {
            is PartData.FormItem -> part.name?.let { fields[it] = part.value }
            is PartData.FileItem -> {
                fileName = part.originalFileName ?: part.name
                part.streamProvider().use { it.copyTo(buffer) }
            }
            else -> {}
        }
        part.dispose()
    }

    val name = fileName ?: throw IllegalArgumentException("video file is required.")
    val data = ByteArrayInputStream(buffer.toByteArray())
    val size = data.available().toLong()
    VideoValidator.validate(name, size)

    return MultipartVideoData(
        video = VideoRequest.of(
            name = VideoName.of(name),
            uniqueName = VideoUniqueName.of(UUID.randomUUID().toString()),
            data = data,
            size = VideoSize.of(size),
        ),
        fields = fields,
    )
}

suspend fun ApplicationCall.getVideo(): VideoRequest = getVideoWithFields().video

inline fun <reified T : Any> Parameters.toClass(): T {
    val map = this.entries().associate {
        it.key to (it.value.getOrNull(0)
            ?: throw IllegalArgumentException("Missing value for key ${it.key}"))
    }
    return mapper.convertValue(map, T::class.java)
}