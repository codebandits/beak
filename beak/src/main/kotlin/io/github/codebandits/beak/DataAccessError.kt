package io.github.codebandits.beak

sealed class DataAccessError {
    abstract val cause: Throwable

    sealed class SystemError : DataAccessError() {
        data class ConnectionError(override val cause: Throwable) : SystemError()
        data class TransactionError(override val cause: Throwable) : SystemError()
    }

    sealed class QueryError : DataAccessError() {
        data class NotFoundError(override val cause: Throwable) : QueryError()
        data class BadRequestError(override val cause: Throwable) : QueryError()
    }
}
