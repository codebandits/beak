package io.github.codebandits.beak

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.LongIdTable

object BirdTable : LongIdTable("birds") {
    val name = varchar("type", 255)
}

class BirdEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<BirdEntity>(BirdTable)

    var name by BirdTable.name
    val feathers by FeatherEntity optionalReferrersOn FeatherTable.bird
}
