package com.cherhy.payment

import com.cherhy.payment.adapter.out.CacheReader
import com.cherhy.payment.util.WithTestContainers
import com.cherhy.payment.util.lib.onFailAction
import com.cherhy.payment.util.lib.set
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.StringRedisTemplate

@SpringBootTest
class CacheComponent(
    private val cacheReader: CacheReader,
    private val stringRedisTemplate: StringRedisTemplate,
) : WithTestContainers, BehaviorSpec({
    Given("Cache에서 조회 할 때 ") {
        When("값이 없는 경우 Result 객체를 반환 받고 ") {
            val result = cacheReader.read("key")

            Then("Result.failure()를 반환 받는다") {
                result.isFailure shouldBe true
            }

            And("Result.failure()를 반환 받고 ") {
                Then("onFailAction 를 통해 기본 값을 반환 받는다") {
                    val value = result.onFailAction { "fail value" }
                    value shouldBe "fail value"
                }

                Then("onFailAction 를 통해 Cache 에 값이 없을 때는 DB에서 조회하고 캐시에 저장하는 로직을 구현할 수 있다") {
                    val value = result.onFailAction {
                        val dbValue = "dbValue"
                        stringRedisTemplate.set("key", dbValue)
                        dbValue
                    }

                    value shouldBe "dbValue"
                }
            }
        }

        When("값이 있는 경우 Result 객체를 반환 받고 ") {
            stringRedisTemplate.set("key", "value")
            val result = cacheReader.read("key")

            Then("Result.success()를 반환 받는다") {
                result.isSuccess shouldBe true
            }

            And("Result.success()를 반환 받고 ") {
                Then("onFailAction 가 실행되지 않고 값을 반환 받는다") {
                    val value = result.onFailAction { "fail value" }
                    value shouldBe "value"
                }
            }
        }
    }
})