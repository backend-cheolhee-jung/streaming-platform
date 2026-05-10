package com.cherhy.schedule

import cherhy.com.util.ApplicationConfigUtils
import cherhy.com.util.DataSourceProperty
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith

class ApplicationConfigUtilsTest : StringSpec({

    afterEach {
        // no per-test state to clean up
    }

    "DataSourceProperty URL key is 'url'" {
        DataSourceProperty.URL shouldBe "url"
    }

    "DataSourceProperty USERNAME key is 'username'" {
        DataSourceProperty.USERNAME shouldBe "username"
    }

    "DataSourceProperty PASSWORD key is 'password'" {
        DataSourceProperty.PASSWORD shouldBe "password"
    }

    "DataSourceProperty DRIVER_CLASS_NAME key is 'driver-class-name'" {
        DataSourceProperty.DRIVER_CLASS_NAME shouldBe "driver-class-name"
    }

    "DataSourceProperty MAX_POOL_SIZE key is 'max-pool-size'" {
        DataSourceProperty.MAX_POOL_SIZE shouldBe "max-pool-size"
    }

    "DataSourceProperty ISOLATION_LEVEL key is 'isolation-level'" {
        DataSourceProperty.ISOLATION_LEVEL shouldBe "isolation-level"
    }

    "ApplicationConfigUtils returns JDBC URL starting with jdbc:postgresql protocol" {
        val jdbcUrl = ApplicationConfigUtils.getDataSource(DataSourceProperty.URL)
        jdbcUrl shouldStartWith "jdbc:postgresql://"
    }
})
