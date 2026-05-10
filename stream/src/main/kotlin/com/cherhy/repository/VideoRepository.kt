package com.cherhy.repository

import com.cherhy.api.VideoDetailResponse
import com.cherhy.common.util.extension.noReturn
import com.cherhy.common.util.model.Price
import com.cherhy.common.util.model.UserId
import com.cherhy.domain.*
import com.cherhy.plugins.database
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.find

interface VideoRepository {
    suspend fun save(
        userId: UserId,
        postId: PostId,
        name: VideoName,
        uniqueName: VideoUniqueName,
        size: VideoSize,
        extension: VideoExtension,
        price: Price,
    ): VideoId

    suspend fun update(
        videoId: VideoId,
        userId: UserId,
        name: VideoName,
        uniqueName: VideoUniqueName,
        size: VideoSize,
        extension: VideoExtension,
    )

    suspend fun delete(
        userId: UserId,
        videoId: VideoId,
    )

    suspend fun findOne(
        videoId: VideoId,
    ): VideoDetailResponse?

    suspend fun findOne(
        postId: PostId,
    ): VideoDetailResponse?

    suspend fun findOne(
        userId: UserId,
        postId: PostId,
        videoId: VideoId,
    ): VideoDetailResponse?

    suspend fun isExists(
        videoId: VideoId,
    ): Boolean
}

class VideoRepositoryImpl(
    private val db: Database = database,
) : VideoRepository {
    override suspend fun save(
        userId: UserId,
        postId: PostId,
        name: VideoName,
        uniqueName: VideoUniqueName,
        size: VideoSize,
        extension: VideoExtension,
        price: Price,
    ) =
        db.insertAndGenerateKey(Videos) {
            set(it.owner, userId)
            set(it.post, postId)
            set(it.name, name)
            set(it.uniqueName, uniqueName)
            set(it.size, size)
            set(it.extension, extension)
            set(it.price, price)
        }.toVideoId()

    override suspend fun update(
        videoId: VideoId,
        userId: UserId,
        name: VideoName,
        uniqueName: VideoUniqueName,
        size: VideoSize,
        extension: VideoExtension,
    ) =
        db.update(Videos) {
            set(it.name, name)
            set(it.uniqueName, uniqueName)
            set(it.size, size)
            set(it.extension, extension)
            where {
                it.id eq videoId
                it.owner eq userId
            }
        }.noReturn

    override suspend fun delete(
        userId: UserId,
        videoId: VideoId,
    ) =
        db.delete(Videos) {
            it.id eq videoId
            it.owner eq userId
        }.noReturn

    override suspend fun findOne(
        videoId: VideoId,
    ) =
        db.videos.find {
            it.id eq videoId
        }?.let(VideoDetailResponse::of)

    override suspend fun findOne(
        postId: PostId,
    ) =
        db.videos.find {
            it.post eq postId
        }?.let(VideoDetailResponse::of)

    override suspend fun findOne(
        userId: UserId,
        postId: PostId,
        videoId: VideoId,
    ) =
        db.videos.find {
            it.owner eq userId
            it.post eq postId
            it.id eq videoId
        }?.let(VideoDetailResponse::of)

    override suspend fun isExists(
        videoId: VideoId,
    ) =
        db.videos.find { it.id eq videoId } != null
}