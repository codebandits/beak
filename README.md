# Beak

[![Concourse](https://wings.pivotal.io/api/v1/teams/codebandits/pipelines/beak/badge)](https://wings.pivotal.io/teams/codebandits/pipelines/beak)
[![Download](https://api.bintray.com/packages/codebandits/beak/beak/images/download.svg)](https://bintray.com/codebandits/beak/beak/_latestVersion)

Beak is a functional Kotlin SQL DSL. It provides a type-safe, exception-free data access layer.
Beak builds upon JetBrains' [Exposed](https://github.com/JetBrains/Exposed) Kotlin SQL DSL library and the
[Arrow](http://arrow-kt.io/) Kotlin functional programming library.

## Alpha Release Considerations

Beak is in early stages of development and is released as alpha software.
All aspects of the API are subject to change until our first release.

Currently, Beak is built as extension functions on top of the Exposed library's DAO layer.
Beak may remove Exposed as a dependency which would significantly change the API. 

Currently, Beak supports H2 and MySQL. We plan to support more databases in the future.

## Installation

Add Beak's Maven repository to your build configuration:

https://dl.bintray.com/codebandits/beak

Find example dependency configurations for the latest version here:

[![Download](https://api.bintray.com/packages/codebandits/beak/beak/images/download.svg)](https://bintray.com/codebandits/beak/beak/_latestVersion)

## Usage

Here is a simple example comparing Exposed's `.new {}` with Beak's `.newOrError {}`:

```kotlin
import arrow.core.Either
import io.github.codebandits.beak.DataAccessError
import io.github.codebandits.beak.newOrError
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.LongIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.transactions.transaction

// Setup table objects and entity classes using Exposed's API:
object FeatherTable : LongIdTable("feathers") {
    val type = varchar("type", 255)
}

class FeatherEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<FeatherEntity>(FeatherTable)

    var type by FeatherTable.type
}

fun main(args: Array<String>) {
    Database.connect("jdbc:h2:mem:test", driver = "org.h2.Driver")

    transaction {
        create(FeatherTable)

        // Create a new Feather using Exposed's API.
        // This will throw exceptions when errors are encountered.
        val feather: FeatherEntity = FeatherEntity.new {
            type = "contour"
        }

        // Create a new Feather using Beak's API.
        // This will return a DataAccessError when errors are encountered.
        val featherResult: Either<DataAccessError, FeatherEntity> = FeatherEntity.newOrError {
            type = "down"
        }
    }
}
```

### Error Handling

Beak's data access functions endeavor to never throw exceptions. Instead, they return an
[Either](http://arrow-kt.io/docs/datatypes/either/) data type with a
[DataAccessError](beak/src/main/kotlin/io/github/codebandits/beak/DataAccessError.kt) as the left hand side.
DataAccessError is a sealed class hierarchy of all possible data access errors so you can use functional patterns to
efficiently handle all data access failures in your application. 

See [Railway Oriented Programming](https://fsharpforfunandprofit.com/rop/)
for more insight into this error handling approach.
