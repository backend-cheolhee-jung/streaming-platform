package com.cherhy.api

import com.cherhy.common.util.model.Price
import com.cherhy.domain.VideoExtension
import com.cherhy.domain.VideoName
import com.cherhy.domain.VideoSize
import com.cherhy.domain.VideoUniqueName
import java.io.ByteArrayInputStream
import java.math.BigDecimal

data class VideoRequest private constructor(
    val name: VideoName,
    val uniqueName: VideoUniqueName,
    val data: ByteArrayInputStream,
    val size: VideoSize,
    val extension: VideoExtension,
    val price: Price,
) {
    companion object {
        fun of(
            name: VideoName,
            uniqueName: VideoUniqueName,
            data: ByteArrayInputStream,
            size: VideoSize,
            price: Price = Price.of(BigDecimal.ZERO),
        ) = VideoRequest(
            name = name,
            uniqueName = uniqueName,
            data = data,
            size = size,
            extension = VideoExtension.of("mp4"),
            price = price,
        )
    }
}