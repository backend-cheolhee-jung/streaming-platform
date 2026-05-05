package com.cherhy.e2e

import com.cherhy.repository.TestDatabase
import com.typesafe.config.ConfigFactory
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.testing.*
import io.minio.BucketExistsArgs
import io.minio.MakeBucketArgs
import io.minio.MinioClient
import org.ktorm.database.Database
import org.ktorm.support.postgresql.PostgreSqlDialect
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.PostgreSQLContainer

class VideoStreamingE2ETest : FunSpec({

    test("upload video post and stream it back successfully") {
        testApplication {
            application(Application::streamE2eModule)

            val userId = 1L
            val videoContent = ByteArray(1024) { (it % 256).toByte() }

            // Step 1: Upload video with post metadata as multipart form fields
            val uploadResponse = client.post("/streams/posts") {
                header("user-id", userId.toString())
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("title", "E2E Test Video")
                            append("content", "E2E test content")
                            append("category", "MUSIC")
                            append(
                                key = "video",
                                value = videoContent,
                                headers = Headers.build {
                                    append(
                                        HttpHeaders.ContentDisposition,
                                        "form-data; name=\"video\"; filename=\"test_video.mp4\""
                                    )
                                    append(HttpHeaders.ContentType, ContentType.Video.MP4.toString())
                                }
                            )
                        }
                    )
                )
            }

            uploadResponse.status shouldBe HttpStatusCode.Created

            // Step 2: Verify metadata persisted in PostgreSQL and retrieve IDs
            val (postId, videoId) = VideoStreamingE2ETest.queryIds(userId)
            postId shouldNotBe 0L
            videoId shouldNotBe 0L

            // Step 3: Stream the video
            val streamResponse = client.get("/streams/posts/$postId/videos/$videoId") {
                header("user-id", userId.toString())
            }

            streamResponse.status shouldBe HttpStatusCode.OK
            val streamedBytes = streamResponse.readRawBytes()
            streamedBytes shouldBe videoContent
        }
    }

    test("stream video with range returns 206 Partial Content") {
        testApplication {
            application(Application::streamE2eModule)

            val userId = 2L
            val videoContent = ByteArray(4096) { (it % 256).toByte() }

            val uploadResp = client.post("/streams/posts") {
                header("user-id", userId.toString())
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("title", "Range Test Video")
                            append("content", "range test")
                            append("category", "EDUCATION")
                            append(
                                key = "video",
                                value = videoContent,
                                headers = Headers.build {
                                    append(
                                        HttpHeaders.ContentDisposition,
                                        "form-data; name=\"video\"; filename=\"range_video.mp4\""
                                    )
                                    append(HttpHeaders.ContentType, ContentType.Video.MP4.toString())
                                }
                            )
                        }
                    )
                )
            }
            uploadResp.status shouldBe HttpStatusCode.Created

            val (postId, videoId) = VideoStreamingE2ETest.queryIds(userId)

            // Request from byte offset 10
            val rangeResponse = client.get("/streams/posts/$postId/videos/$videoId") {
                header("user-id", userId.toString())
                header(HttpHeaders.Range, "bytes=10-")
            }

            rangeResponse.status shouldBe HttpStatusCode.PartialContent
            val rangeBody = rangeResponse.readRawBytes()
            rangeBody.size shouldNotBe 0
            rangeResponse.headers[HttpHeaders.ContentRange] shouldNotBe null
        }
    }

}) {
    companion object {
        private val postgres = PostgreSQLContainer<Nothing>("postgres:16-alpine").apply {
            withDatabaseName("stream_e2e")
            withUsername("postgres")
            withPassword("postgres")
            start()
        }

        private val mongodb = MongoDBContainer("mongo:7.0").apply { start() }

        private val minio: GenericContainer<*> = GenericContainer<Nothing>("minio/minio:latest").apply {
            withCommand("server /data --console-address :9001")
            withExposedPorts(9000)
            withEnv("MINIO_ROOT_USER", "test-access-key")
            withEnv("MINIO_ROOT_PASSWORD", "test-secret-key")
            start()
        }

        private val minioHost get() = minio.host
        private val minioPort get() = minio.getMappedPort(9000)

        // Direct JDBC database for setup and post-test verification (autoCommit=true)
        val verifyDb: Database

        init {
            System.setProperty("database.datasource.url", postgres.jdbcUrl)
            System.setProperty("database.datasource.username", postgres.username)
            System.setProperty("database.datasource.password", postgres.password)
            System.setProperty("database.datasource.driver-class-name", "org.postgresql.Driver")
            System.setProperty("database.datasource.max-pool-size", "5")

            System.setProperty("minio.url", "http://$minioHost:$minioPort")
            System.setProperty("minio.access-key", "test-access-key")
            System.setProperty("minio.secret-key", "test-secret-key")
            System.setProperty("minio.bucket", "e2e-test-bucket")

            val mongoHost = mongodb.host
            val mongoPort = mongodb.getMappedPort(27017)
            System.setProperty("mongo.url", "mongodb://$mongoHost:$mongoPort/stream_e2e")
            System.setProperty("mongo.database", "stream_e2e")

            // Force HOCON to re-read with the new system properties
            ConfigFactory.invalidateCaches()

            verifyDb = Database.connect(
                dataSource = HikariDataSource(
                    HikariConfig().apply {
                        driverClassName = "org.postgresql.Driver"
                        jdbcUrl = postgres.jdbcUrl
                        username = postgres.username
                        password = postgres.password
                        maximumPoolSize = 3
                        isAutoCommit = true
                    }
                ),
                dialect = PostgreSqlDialect(),
            )

            verifyDb.useConnection { conn ->
                conn.createStatement().execute(TestDatabase.ddl)
            }

            val minioClient = MinioClient.builder()
                .endpoint("http://$minioHost:$minioPort")
                .credentials("test-access-key", "test-secret-key")
                .build()
            val bucketName = "e2e-test-bucket"
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build())
            }
        }

        fun queryIds(userId: Long): Pair<Long, Long> =
            verifyDb.useConnection { conn ->
                val rs = conn.createStatement().executeQuery(
                    """
                    SELECT p.id AS post_id, v.id AS video_id
                    FROM post p
                    JOIN video v ON v.post = p.id
                    WHERE p.author = $userId
                    ORDER BY p.id DESC
                    LIMIT 1
                    """.trimIndent()
                )
                check(rs.next()) { "No post/video found for userId=$userId after upload" }
                Pair(rs.getLong("post_id"), rs.getLong("video_id"))
            }
    }
}
