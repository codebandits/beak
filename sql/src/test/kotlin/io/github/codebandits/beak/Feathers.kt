package io.github.codebandits.beak

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.LongIdTable

object FeatherTable : LongIdTable("feathers") {
    val type = varchar("type", 255).nullable()
}

class FeatherEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<FeatherEntity>(FeatherTable)

    var type by FeatherTable.type
}
