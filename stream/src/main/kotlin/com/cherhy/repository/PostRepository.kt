package com.cherhy.repository

import com.cherhy.api.PostDetailResponse
import com.cherhy.api.PostItemResponse
import com.cherhy.common.util.PageOffsetCalculator
import com.cherhy.common.util.extension.noReturn
import com.cherhy.common.util.model.*
import com.cherhy.domain.*
import com.cherhy.plugins.database
import com.cherhy.util.extension.contains
import org.ktorm.dsl.*
import org.ktorm.entity.count
import org.ktorm.entity.filter
import org.ktorm.entity.find

interface PostRepository {
    suspend fun save(
        userId: UserId,
        title: PostTitle,
        content: PostContent,
        category: PostCategory,
    ): PostId

    suspend fun update(
        userId: UserId,
        postId: PostId,
        title: PostTitle,
        content: PostContent,
        category: PostCategory,
    )

    suspend fun delete(
        userId: UserId,
        postId: PostId,
    )

    suspend fun isExists(
        userId: UserId,
        postId: PostId,
    ): Boolean

    suspend fun findOne(
        userId: UserId,
        postId: PostId,
    ): PostDetailResponse?

    suspend fun findAll(
        userId: UserId,
        keyword: Keyword?,
        category: PostCategory?,
        page: Page,
        size: Size,
    ): PageResponse<PostItemResponse>
}

class PostRepositoryImpl(
    private val db: org.ktorm.database.Database = database,
) : PostRepository {
    override suspend fun save(
        userId: UserId,
        title: PostTitle,
        content: PostContent,
        category: PostCategory,
    ) =
        db.insertAndGenerateKey(Posts) {
            set(it.title, title)
            set(it.content, content)
            set(it.author, userId)
            set(it.category, category)
        }.toPostId()

    override suspend fun update(
        userId: UserId,
        postId: PostId,
        title: PostTitle,
        content: PostContent,
        category: PostCategory
    ) =
        db.update(Posts) {
            set(it.title, title)
            set(it.content, content)
            set(it.category, category)
            where {
                it.id eq postId
                it.author eq userId
            }
        }.noReturn

    override suspend fun delete(
        userId: UserId,
        postId: PostId,
    ) =
        db.delete(Posts) {
            it.id eq postId
            it.author eq userId
        }.noReturn

    override suspend fun isExists(
        userId: UserId,
        postId: PostId,
    ) =
        db.posts.filter {
            it.id eq postId
            it.author eq userId
        }.count() > 0

    override suspend fun findOne(
        userId: UserId,
        postId: PostId,
    ) =
        db.posts.find {
            it.id eq postId
            it.author eq userId
        }?.let(PostDetailResponse::of)

    override suspend fun findAll(
        userId: UserId,
        keyword: Keyword?,
        category: PostCategory?,
        page: Page,
        size: Size,
    ): PageResponse<PostItemResponse> {
        val generator = PageOffsetCalculator.of(page, size)
        var expression = db.posts.filter { it.author eq userId }
        keyword?.let { kw -> expression = expression.filter { it.title contains kw.value } }
        category?.let { cat -> expression = expression.filter { it.category eq cat } }

        val count = expression.count().toLong()
        val data = expression.query
            .limit(generator.offset, generator.limit)
            .map(Posts::createEntity)
            .map(PostItemResponse::of)

        return PageResponse.of(
            data = data,
            total = count,
            page = page,
            size = size,
        )
    }
}
