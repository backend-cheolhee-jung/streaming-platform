package com.cherhy.repository

import com.cherhy.common.util.model.Price
import com.cherhy.common.util.model.UserId
import com.cherhy.domain.*
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.nulls.shouldNotBeNull
import org.ktorm.database.Database
import java.math.BigDecimal

class VideoRepositoryTest : StringSpec({
    val db: Database by lazy { TestDatabase.start() }
    lateinit var postRepo: PostRepositoryImpl
    lateinit var videoRepo: VideoRepositoryImpl

    beforeEach {
        db.useConnection { conn ->
            conn.createStatement().execute("TRUNCATE TABLE video, post RESTART IDENTITY CASCADE")
        }
        postRepo = PostRepositoryImpl(db)
        videoRepo = VideoRepositoryImpl(db)
    }

    "save returns a valid VideoId (not ClassCastException)" {
        val postId = postRepo.save(
            UserId.of(1L),
            com.cherhy.domain.PostTitle.of("post"),
            com.cherhy.domain.PostContent.of("content"),
            PostCategory.MUSIC,
        )

        val videoId = videoRepo.save(
            userId = UserId.of(1L),
            postId = postId,
            name = VideoName.of("my-video"),
            uniqueName = VideoUniqueName.of("unique-abc"),
            size = VideoSize.of(1024L),
            extension = VideoExtension.of("mp4"),
            price = Price.of(BigDecimal.ZERO),
        )

        videoId.value shouldNotBe 0L
    }

    "findOne returns saved video by videoId" {
        val postId = postRepo.save(
            UserId.of(1L),
            com.cherhy.domain.PostTitle.of("post"),
            com.cherhy.domain.PostContent.of("content"),
            PostCategory.MUSIC,
        )
        val videoId = videoRepo.save(
            userId = UserId.of(1L),
            postId = postId,
            name = VideoName.of("my-video"),
            uniqueName = VideoUniqueName.of("unique-xyz"),
            size = VideoSize.of(2048L),
            extension = VideoExtension.of("mp4"),
            price = Price.of(BigDecimal.ZERO),
        )

        val found = videoRepo.findOne(videoId).shouldNotBeNull()
        found.name.value shouldBe "my-video"
    }

    "isExists returns true for saved video" {
        val postId = postRepo.save(
            UserId.of(1L),
            com.cherhy.domain.PostTitle.of("post"),
            com.cherhy.domain.PostContent.of("content"),
            PostCategory.MUSIC,
        )
        val videoId = videoRepo.save(
            userId = UserId.of(1L),
            postId = postId,
            name = VideoName.of("exists-video"),
            uniqueName = VideoUniqueName.of("unique-exists"),
            size = VideoSize.of(512L),
            extension = VideoExtension.of("mp4"),
            price = Price.of(BigDecimal.ZERO),
        )

        videoRepo.isExists(videoId) shouldBe true
    }

    "isExists returns false for nonexistent video" {
        videoRepo.isExists(VideoId.of(99999L)) shouldBe false
    }
})
