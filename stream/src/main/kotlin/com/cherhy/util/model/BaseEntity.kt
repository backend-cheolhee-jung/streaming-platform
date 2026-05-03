package com.cherhy.util.model

import org.ktorm.entity.Entity
import org.ktorm.schema.Table

abstract class BaseTable<T : Entity<T>>(
    name: String,
) : Table<T>(name)

interface BaseEntity<T : Entity<T>> : Entity<T>

abstract class BaseEntityFactory<T : Entity<T>> : Entity.Factory<T>()
