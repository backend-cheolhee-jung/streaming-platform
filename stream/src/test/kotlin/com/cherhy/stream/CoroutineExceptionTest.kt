package com.cherhy.stream

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.*

class CoroutineExceptionTest : FunSpec({
//    https://huisam.tistory.com/entry/kotlin-coroutine-exception?category=705896

    test("자식 코루틴에서 예외가 발생하면 부모 코루틴도 취소되고 다른 자식 코루틴도 취소된다") {
        var count = 0

        runBlocking {
            coroutineScope {
                launch {
                    shouldThrow<Exception> {
                        coroutineScope {
                            val failed = launch {
                                coroutineScope { launch { throw Exception("Exception") } }
                            }
                            val success = launch {
                                delay(100)
                                coroutineScope { launch { count++ } }
                            }
                            joinAll(success, failed)
                        }
                    }
                }
            }
        }

        count shouldBe 0
    }

    test("자식 코루틴에서 예외가 발생해도 최상위 루트 코루틴 스코프에 Exception Handler를 등록해도 부모 코루틴과 다른 자식 코루틴은 취소된다") {
        var count = 0

        shouldThrow<Exception> {
            runBlocking {
                launch(CoroutineExceptionHandler { coroutineContext, throwable ->
                    println("CoroutineContext: $coroutineContext")
                    println("Exception: $throwable")
                }) {
                    coroutineScope { launch { throw Exception("Exception") } }
                }
                launch {
                    delay(100)
                    coroutineScope { launch { count++ } }
                }
            }
        }

        count shouldBe 0
    }

    test("supervisorScope로 선언하면 자식 코루틴에서 예외가 발생해도 부모 코루틴과 다른 자식 코루틴은 취소되지 않는다") {
        var count = 0

        supervisorScope {
            launch {
                coroutineScope { launch { throw Exception("Exception") } }
            }
            launch {
                delay(100)
                coroutineScope { launch { count++ } }
            }
        }

        count shouldBe 1
    }
})
