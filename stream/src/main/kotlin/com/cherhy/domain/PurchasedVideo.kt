package com.cherhy.domain

import com.cherhy.common.util.model.Price
import com.cherhy.common.util.model.UserId
import com.cherhy.util.model.BaseEntity
import com.cherhy.util.model.BaseTable
import org.ktorm.database.Database
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.decimal
import org.ktorm.schema.long

object PurchasedVideos : BaseTable<PurchasedVideo>("purchased_video") {
    val id = long("id").primaryKey()
        .transform({ PurchasedVideoId.of(it) }, { it.value })
        .bindTo { it.id }
    val userId = long("user_id")
        .transform({ UserId.of(it) }, { it.value })
        .bindTo { it.userId }
    val videoId = long("video_id")
        .transform({ VideoId.of(it) }, { it.value })
        .bindTo { it.videoId }
    val price = decimal("price")
        .transform({ Price.of(it) }, { it.value })
        .bindTo { it.purchasePrice }
}

interface PurchasedVideo : BaseEntity<PurchasedVideo> {
    val id: PurchasedVideoId
    val videoId: VideoId
    val userId: UserId
    val purchasePrice: Price
}

@JvmInline
value class PurchasedVideoId(
    val value: Long,
) {
    companion object {
        @JvmStatic
        fun of(
            value: Long,
        ) = PurchasedVideoId(value)
    }
}

fun Any.toPurchasedVideoId() = PurchasedVideoId.of(this as Long)
val Database.purchasedVideos get() = this.sequenceOf(PurchasedVideos)