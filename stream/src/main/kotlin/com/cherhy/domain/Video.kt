package com.cherhy.domain

import com.cherhy.common.util.model.Price
import com.cherhy.common.util.model.UserId
import com.cherhy.util.model.BaseEntity
import com.cherhy.util.model.BaseEntityFactory
import com.cherhy.util.model.BaseTable
import org.ktorm.database.Database
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.decimal
import org.ktorm.schema.long
import org.ktorm.schema.varchar

object Videos : BaseTable<Video>("video") {
    val id = long("id").primaryKey()
        .transform({ VideoId.of(it) }, { it.value })
        .bindTo { it.id }
    val name = varchar("name")
        .transform({ VideoName.of(it) }, { it.value })
        .bindTo { it.name }
    val uniqueName = varchar("unique_name")
        .transform({ VideoUniqueName.of(it) }, { it.value })
        .bindTo { it.uniqueName }
    val size = long("size")
        .transform({ VideoSize.of(it) }, { it.value })
        .bindTo { it.size }
    val extension = varchar("extension")
        .transform({ VideoExtension.of(it) }, { it.value })
        .bindTo { it.extension }
    val price = decimal("price")
        .transform({ Price.of(it) }, { it.value })
        .bindTo { it.price }
    val owner = long("owner")
        .transform({ UserId.of(it) }, { it.value })
        .bindTo { it.owner }
    val post = long("post")
        .transform({ PostId.of(it) }, { it.value })
        .bindTo { it.post }
}

interface Video : BaseEntity<Video> {
    companion object : BaseEntityFactory<Video>()
    val id: VideoId
    var name: VideoName
    var uniqueName: VideoUniqueName
    var size: VideoSize
    var extension: VideoExtension
    var price: Price
    val owner: UserId
    val post: PostId
}

@JvmInline
value class VideoId(
    val value: Long,
) {
    companion object {
        @JvmStatic
        fun of(
            value: Long,
        ) = VideoId(value)
    }
}

fun Any.toVideoId() = VideoId.of(this as Long)

@JvmInline
value class VideoSize(
    val value: Long,
) {
    companion object {
        @JvmStatic
        fun of(
            value: Long,
        ) = VideoSize(value)
    }
}

@JvmInline
value class VideoName(
    val value: String,
) {
    companion object {
        @JvmStatic
        fun of(
            value: String,
        ) = VideoName(value)
    }
}

@JvmInline
value class VideoUniqueName(
    val value: String,
) {
    companion object {
        @JvmStatic
        fun of(
            value: String,
        ) = VideoUniqueName(value)
    }
}

@JvmInline
value class VideoExtension(
    val value: String,
) {
    companion object {
        @JvmStatic
        fun of(
            value: String,
        ) = VideoExtension(value)
    }
}

val Database.videos get() = this.sequenceOf(Videos)