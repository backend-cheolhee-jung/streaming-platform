package com.cherhy.schedule

import cherhy.com.util.ApplicationConfigUtils
import cherhy.com.util.DataSourceProperty
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith

class ApplicationConfigUtilsTest : FunSpec({

    afterEach {
        // no per-test state to clean up
    }

    test("DataSourceProperty URL key is 'url'") {
        DataSourceProperty.URL shouldBe "url"
    }

    test("DataSourceProperty USERNAME key is 'username'") {
        DataSourceProperty.USERNAME shouldBe "username"
    }

    test("DataSourceProperty PASSWORD key is 'password'") {
        DataSourceProperty.PASSWORD shouldBe "password"
    }

    test("DataSourceProperty DRIVER_CLASS_NAME key is 'driver-class-name'") {
        DataSourceProperty.DRIVER_CLASS_NAME shouldBe "driver-class-name"
    }

    test("DataSourceProperty MAX_POOL_SIZE key is 'max-pool-size'") {
        DataSourceProperty.MAX_POOL_SIZE shouldBe "max-pool-size"
    }

    test("DataSourceProperty ISOLATION_LEVEL key is 'isolation-level'") {
        DataSourceProperty.ISOLATION_LEVEL shouldBe "isolation-level"
    }

    test("ApplicationConfigUtils returns JDBC URL starting with jdbc:postgresql protocol") {
        val jdbcUrl = ApplicationConfigUtils.getDataSource(DataSourceProperty.URL)
        jdbcUrl shouldStartWith "jdbc:postgresql://"
    }
})
