package com.cherhy.stream

import com.cherhy.common.util.extension.toSeconds
import io.kotest.assertions.throwables.shouldThrow
import kotlinx.coroutines.time.withTimeout
import io.kotest.core.spec.style.StringSpec
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration

// https://huisam.tistory.com/entry/coroutine-channel [코루틴] Channel 사용법
class ChannelTest : StringSpec({

    "buffer가 없는 Channel에서 데이터를 소비하면 buffer가 없기 때문에 데이터 1개씩만 소비 되고 1개씩 소비된다" {
        val rendezvousChannel = Channel<Int>()

        val job = launch {
            rendezvousChannel.consumeAll()
        }

        repeat(10) {
            println("$rendezvousChannel send $it")
            rendezvousChannel.send(it)
        }

        rendezvousChannel.close()
        job.join()
    }

    "buffer가 없는 Channel에서 구독을 하지 않고 데이터를 송신하면 소비할 때 까지 무한 대기한다" {
        val rendezvousChannel = Channel<Int>()

        shouldThrow<TimeoutCancellationException> {
            withTimeout(3.toSeconds()) {
                repeat(10) {
                    println("$rendezvousChannel send $it")
                    rendezvousChannel.send(it)
                }

                rendezvousChannel.consumeAll()
                rendezvousChannel.close()
            }
        }
    }

    "buffer가 무한한 Channel에서 데이터 소비하면 buffer가 무한하기 때문에 데이터가 1번에 모두 소비된다" {
        val unlimitedChannel = Channel<Int>(UNLIMITED)

        repeat(10) {
            println("$unlimitedChannel send $it")
            unlimitedChannel.send(it)
            delay(10)
        }

        unlimitedChannel.close()

        unlimitedChannel.consumeAll()
    }

    """
        채널이 닫히지 않은 상태에서 소비를 하면 소비자는 계속해서 새로운 데이터가 온다고 생각해서 채널이 닫힐 때까지 대기한다
        대기 상태에선 close를 호출해도 consumeAll 함수는 신호를 받지 못하기 떄문에 무한 대기한다
    """ {
        val unlimitedChannel = Channel<Int>(UNLIMITED)

        shouldThrow<TimeoutCancellationException> {
            withTimeout(3.toSeconds()) {
                repeat(10) {
                    println("$unlimitedChannel send $it")
                    unlimitedChannel.send(it)
                    delay(10)
                }

                unlimitedChannel.consumeAll()

                unlimitedChannel.close()
            }
        }
    }
})


private suspend fun <T> Channel<T>.consumeAll() {
    for (i in this) {
        println("$this receive $i")
    }
}