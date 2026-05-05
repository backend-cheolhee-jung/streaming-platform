package com.cherhy.util.property

object StreamingProperty {
    const val CHUNK_SIZE = 10 * 1024 * 1024L  // 10 MB per streaming chunk
    const val OBJECT_PART_SIZE = -1L             // auto-calculate for multipart upload
}