package com.cherhy.payment.adapter.out.persistence

import com.cherhy.payment.annotation.PersistenceAdapter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.domain.Pageable
import org.springframework.data.mapping.toDotPath
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.ReactiveSelectOperation
import org.springframework.data.r2dbc.core.select
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface TestCoroutineRepository : CoroutineCrudRepository<TestR2dbcEntity, Long>, TestRepositoryCustom

interface TestRepositoryCustom {
    suspend fun findAll(
        name: String?,
        status: String?,
        pageable: Pageable,
    ): Flow<TestR2dbcEntity>

    suspend fun countAll(
        name: String?,
        status: String?,
    ): Long
}

@PersistenceAdapter
class TestRepositoryCustomImpl(
    private val template: R2dbcEntityTemplate,
) : TestRepositoryCustom {
    override suspend fun findAll(
        name: String?,
        status: String?,
        pageable: Pageable,
    ) =
        template.select<TestR2dbcEntity>()
            .findAll(buildQuery(name, status), pageable)

    override suspend fun countAll(
        name: String?,
        status: String?,
    ) =
        template.select<TestR2dbcEntity>()
            .count(buildQuery(name, status))
}

private fun buildQuery(
    name: String?,
    status: String?,
): Query {
    val criteria = buildList {
        if (name != null) add(Criteria.where(TestR2dbcEntity::name.toDotPath()).`is`(name))
        if (status != null) add(Criteria.where(TestR2dbcEntity::status.toDotPath()).`is`(status))
    }
    return if (criteria.isEmpty()) Query.empty()
    else Query.query(criteria.reduce(Criteria::and))
}

private suspend fun <T> ReactiveSelectOperation.ReactiveSelect<T>.count(
    toQuery: Query,
): Long =
    this.matching(toQuery).count().awaitSingleOrNull() ?: 0L

fun <T : Any> ReactiveSelectOperation.ReactiveSelect<T>.findAll(
    predicate: Query,
    pageable: Pageable,
): Flow<T> = this.matching(predicate.with(pageable)).all().asFlow()