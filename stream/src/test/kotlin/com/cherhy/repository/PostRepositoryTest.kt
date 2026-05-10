package com.cherhy.repository

import com.cherhy.common.util.model.*
import com.cherhy.domain.PostCategory
import com.cherhy.domain.PostContent
import com.cherhy.domain.PostTitle
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.ktorm.database.Database

class PostRepositoryTest : StringSpec({
    val db: Database by lazy { TestDatabase.start() }
    lateinit var repo: PostRepositoryImpl

    beforeEach {
        db.useConnection { conn ->
            conn.createStatement().execute("TRUNCATE TABLE post RESTART IDENTITY CASCADE")
        }
        repo = PostRepositoryImpl(db)
    }

    "save returns a valid PostId" {
        val postId = repo.save(
            UserId.of(1L),
            PostTitle.of("title"),
            PostContent.of("content"),
            PostCategory.MUSIC,
        )
        postId.value shouldNotBe 0L
    }

    "findAll filters posts by author — other users' posts not returned" {
        repo.save(UserId.of(1L), PostTitle.of("user1-post"), PostContent.of("c"), PostCategory.MUSIC)
        repo.save(UserId.of(2L), PostTitle.of("user2-post"), PostContent.of("c"), PostCategory.MUSIC)

        val result = repo.findAll(
            userId = UserId.of(1L),
            keyword = null,
            category = null,
            page = Page(1),
            size = Size(10),
        )

        result.data.size shouldBe 1
        result.data[0].title.value shouldBe "user1-post"
    }

    "findAll filters posts by keyword" {
        repo.save(UserId.of(1L), PostTitle.of("kotlin tutorial"), PostContent.of("c"), PostCategory.EDUCATION)
        repo.save(UserId.of(1L), PostTitle.of("java guide"), PostContent.of("c"), PostCategory.EDUCATION)

        val result = repo.findAll(
            userId = UserId.of(1L),
            keyword = Keyword.of("kotlin"),
            category = null,
            page = Page(1),
            size = Size(10),
        )

        result.data.size shouldBe 1
        result.data[0].title.value shouldBe "kotlin tutorial"
    }

    "findAll filters posts by category" {
        repo.save(UserId.of(1L), PostTitle.of("music post"), PostContent.of("c"), PostCategory.MUSIC)
        repo.save(UserId.of(1L), PostTitle.of("edu post"), PostContent.of("c"), PostCategory.EDUCATION)

        val result = repo.findAll(
            userId = UserId.of(1L),
            keyword = null,
            category = PostCategory.MUSIC,
            page = Page(1),
            size = Size(10),
        )

        result.data.size shouldBe 1
        result.data[0].title.value shouldBe "music post"
    }

    "findOne returns null when post belongs to different user" {
        val postId = repo.save(
            UserId.of(1L),
            PostTitle.of("owner's post"),
            PostContent.of("c"),
            PostCategory.MUSIC,
        )

        val result = repo.findOne(UserId.of(2L), postId)
        result shouldBe null
    }
})
