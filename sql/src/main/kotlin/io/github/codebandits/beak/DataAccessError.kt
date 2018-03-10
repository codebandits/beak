package io.github.codebandits.beak

sealed class DataAccessError {
    abstract val cause: Throwable

    sealed class SystemError : DataAccessError() {
        data class ConnectionError(override val cause: Throwable) : SystemError()
    }
}
