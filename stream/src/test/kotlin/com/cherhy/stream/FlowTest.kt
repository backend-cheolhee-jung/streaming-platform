package com.cherhy.stream

import io.kotest.core.spec.style.StringSpec
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withTimeout

// https://huisam.tistory.com/entry/coroutine3?category=705896 [코틀린] 코루틴 Flow 사용법
class FlowTest : StringSpec({
    "Flow 생성 예시" {
        // 고정된 값 집합에서 flow를 만든다.
        val one = flowOf(1, 2, 3)

        // 동적으로 값을 생성하는 flow를 만든다.
        val two = flow {
            repeat(3) { data ->
                emit(data)
            }
        }

        // 다양한 타입의 컬렉션을 확장 함수를 사용해서 flow로 변환한다.
        val three = listOf(1, 2, 3).asFlow()

        // 비동기적으로 send 함수를 동시에 호출해 데이터를 채널에 보내고 flow를 만든다. (결국 비동기적으로 데이터를 생성하는 flow)
        val four = channelFlow {
            repeat(3) {
                send(it)
            }
        }

        // MutableStateFlow 및 MutableSharedFlow는 값을 직접 변경할 수 있는 hot flow를 생성한다. hot flow는 활성 상태이며 값을 직접 변경 할 수 있다.
        val five = MutableStateFlow(1)
        five.emit(2)
        five.emit(3)

        val six = MutableSharedFlow<Int>()
        six.emit(1)
        six.emit(2)
        six.emit(3)
    }

    "Flow 는 비동기로 데이터를 생산하는 Producer이고 Collect는 Consumer이다" {
        flow {
            repeat(3) { data ->
                delay(300L)
                emit(data)
                println("produced $data")
            }
        }.collect { data ->
            delay(700L)
            println("consumed $data")
        }
    }

    "Producer만 존재하고 Consumer가 없으면 Flow는 실행되지 않는다" {
        flow {
            repeat(3) { data ->
                delay(300L)
                emit(data)
                println("produced $data")
            }
        }
    }

//    "flow는 cold stream 형태여서 발행 0.3초 + 소비 0.7초 = 1초 * 3번 = 3초가 걸린다" {
//        withTimeout(2000L) {
//            flow {
//                repeat(3) { data ->
//                    delay(300L)
//                    emit(data)
//                }
//            }.collect {
//                delay(700L)
//            }
//        }
//    }
//
//    "cold stream의 단점을 보완하기 위해 channel 기반의 buffer를 사용해서 속도를 개선할 수 있다" {
//        withTimeout(2000L) {
//            flow {
//                repeat(3) { data ->
//                    delay(300L)
//                    emit(data)
//                }
//            }.buffer(10, BufferOverflow.SUSPEND)
//                .collect {
//                    delay(700L)
//                }
//        }
//    }
})