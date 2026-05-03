package com.cherhy.domain

import com.cherhy.common.util.model.UserId
import com.cherhy.util.model.BaseEntity
import com.cherhy.util.model.BaseEntityFactory
import com.cherhy.util.model.BaseTable
import kotlinx.serialization.Serializable
import org.ktorm.database.Database
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.long
import org.ktorm.schema.varchar

object Posts : BaseTable<Post>("post") {
    val id = long("id").primaryKey()
        .transform({ PostId.of(it) }, { it.value })
        .bindTo { it.id }
    val title = varchar("title")
        .transform({ PostTitle.of(it) }, { it.value })
        .bindTo { it.title }
    val content = varchar("content")
        .transform({ PostContent.of(it) }, { it.value })
        .bindTo { it.content }
    val author = long("author")
        .transform({ UserId.of(it) }, { it.value })
        .bindTo { it.author }
    val category = varchar("category")
        .transform({ PostCategory.valueOf(it) }, { it.name })
        .bindTo { it.category }
}

interface Post : BaseEntity<Post> {
    companion object : BaseEntityFactory<Post>()
    val id: PostId
    var title: PostTitle
    var content: PostContent
    val author: UserId
    val category: PostCategory
}

@JvmInline
@Serializable
value class PostId(
    val value: Long,
) {
    companion object {
        @JvmStatic
        fun of(
            value: Long,
        ) = PostId(value)
    }
}

fun Any.toPostId() = this as PostId

@JvmInline
@Serializable
value class PostTitle(
    val value: String,
) {
    companion object {
        @JvmStatic
        fun of(
            value: String,
        ) = PostTitle(value)
    }
}

@JvmInline
@Serializable
value class PostContent(
    val value: String,
) {
    companion object {
        @JvmStatic
        fun of(
            value: String,
        ) = PostContent(value)
    }
}

enum class PostCategory {
    COMEDY,
    VIDEO_GAME,
    MUSIC,
    AUTOS_VEHICLES,
    EDUCATION,
    ENTERTAINMENT,
    PETS_ANIMALS,
    SCIENCE_TECHNOLOGY_STUDIES,
    NEWS_POLITICS,
    PEOPLE_BLOGS,
    VLOGGING,
    TRAVEL,
    SPORTS,
    ;
}

val Database.posts get() = this.sequenceOf(Posts)