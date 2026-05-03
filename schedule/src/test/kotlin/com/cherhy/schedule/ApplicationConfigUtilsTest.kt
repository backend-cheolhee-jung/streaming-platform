package com.cherhy.schedule

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith

class ApplicationConfigUtilsTest : FunSpec({

    test("DataSourceProperty URL key is 'url'") {
        cherhy.com.util.DataSourceProperty.URL shouldBe "url"
    }

    test("DataSourceProperty USERNAME key is 'username'") {
        cherhy.com.util.DataSourceProperty.USERNAME shouldBe "username"
    }

    test("DataSourceProperty PASSWORD key is 'password'") {
        cherhy.com.util.DataSourceProperty.PASSWORD shouldBe "password"
    }

    test("DataSourceProperty DRIVER_CLASS_NAME key is 'driver-class-name'") {
        cherhy.com.util.DataSourceProperty.DRIVER_CLASS_NAME shouldBe "driver-class-name"
    }

    test("DataSourceProperty MAX_POOL_SIZE key is 'max-pool-size'") {
        cherhy.com.util.DataSourceProperty.MAX_POOL_SIZE shouldBe "max-pool-size"
    }

    test("DataSourceProperty ISOLATION_LEVEL key is 'isolation-level'") {
        cherhy.com.util.DataSourceProperty.ISOLATION_LEVEL shouldBe "isolation-level"
    }

    test("application.conf JDBC URL starts with jdbc:postgresql protocol") {
        val jdbcUrl = "jdbc:postgresql://host.docker.internal:5432/schedule"
        jdbcUrl shouldStartWith "jdbc:postgresql://"
    }
})
