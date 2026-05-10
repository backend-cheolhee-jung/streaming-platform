package com.cherhy.stream

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.*

class CoroutineExceptionTest : StringSpec({
//    https://huisam.tistory.com/entry/kotlin-coroutine-exception?category=705896

    "자식 코루틴에서 예외가 발생하면 부모 코루틴도 취소되고 다른 자식 코루틴도 취소된다" {
        var count = 0

        runBlocking {
            coroutineScope {
                launch {
                    shouldThrow<Exception> { childScope { count++ } }
                }
            }
        }

        count shouldBe 0
    }

    "자식 코루틴에서 예외가 발생해도 최상위 루트 코루틴 스코프에 Exception Handler를 등록해도 부모 코루틴과 다른 자식 코루틴은 취소된다" {
        var count = 0

        shouldThrow<Exception> {
            runBlocking {
                launch(exceptionHandler()) {
                    throwException()
                }
                launch {
                    delay(100)
                    success { count++ }
                }
            }
        }

        count shouldBe 0
    }

    "supervisorScope로 선언하면 자식 코루틴에서 예외가 발생해도 부모 코루틴과 다른 자식 코루틴은 취소되지 않는다" {
        var count = 0

        supervisorScope {
            launch {
                throwException()
            }
            launch {
                delay(100)
                success { count++ }
            }
        }

        count shouldBe 1
    }
})

private suspend fun throwException() {
    coroutineScope {
        launch { throw Exception("Exception") }
    }
}

private suspend fun success(
    block: suspend () -> Unit,
) {
    coroutineScope {
        launch { block.invoke() }
    }
}

private suspend fun childScope(
    block: suspend () -> Unit,
) {
    coroutineScope {
        val failed = launch { throwException() }
        val success = launch {
            delay(100)
            block.invoke()
        }

        joinAll(success, failed)
    }
}

private fun exceptionHandler() =
    CoroutineExceptionHandler { coroutineContext, throwable ->
        println("CoroutineContext: $coroutineContext")
        println("Exception: $throwable")
    }