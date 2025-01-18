package com.ramitsuri.locationtracking.network

import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal suspend inline fun <reified T> apiRequest(
    ioDispatcher: CoroutineDispatcher,
    crossinline call: suspend () -> HttpResponse,
): Result<T> {
    return withContext(ioDispatcher) {
        try {
            val response: HttpResponse = call()
            when {
                response.status == HttpStatusCode.OK -> {
                    val data: T = response.body()
                    Result.success(data)
                }

                response.status == HttpStatusCode.Created && T::class == Unit::class -> {
                    Result.success(response.body())
                }

                response.status == HttpStatusCode.BadRequest -> {
                    Result.failure(BadRequestException)
                }

                else -> {
                    Result.failure(UnknownErrorException())
                }
            }
        } catch (e: Exception) {
            if (e is java.io.IOException) {
                Result.failure(NoInternetException)
            } else {
                Result.failure(UnknownErrorException(e))
            }
        }
    }
}

data object BadRequestException : RuntimeException("Bad request") {
    private fun readResolve(): Any = BadRequestException
}

data object NoInternetException : RuntimeException("No internet") {
    private fun readResolve(): Any = NoInternetException
}

data class UnknownErrorException(val exception: Exception? = null) :
    RuntimeException("Unknown error")
