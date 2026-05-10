package cherhy.com.util

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.http.*
val httpClient
    get() = HttpClient(CIO)

suspend inline fun <reified RESPONSE_TYPE> HttpClient.call(
    url: String,
    httpMethod: HttpMethod,
    requestHeader: Map<String, String>? = null,
    requestBody: Any? = null,
) =
    this.use {
        this.request(url) {
            method = httpMethod
            requestHeader?.forEach { (key, value) ->
                this.headers {
                    append(key, value)
                }
            }
            if ((httpMethod == HttpMethod.Post || httpMethod == HttpMethod.Put) && requestBody != null) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }
        }
    }.body<RESPONSE_TYPE>()