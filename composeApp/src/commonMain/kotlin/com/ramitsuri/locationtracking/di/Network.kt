package com.ramitsuri.locationtracking.di

import com.ramitsuri.locationtracking.log.logI
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal fun provideHttpClient(
    clientEngine: HttpClientEngine,
    json: Json,
    enableAllLogging: Boolean = false,
) = HttpClient(clientEngine) {
    install(ContentNegotiation) {
        json(json)
    }
    install(Logging) {
        logger =
            object : Logger {
                override fun log(message: String) {
                    logI("HTTP") { message }
                }
            }
        level = if (enableAllLogging) LogLevel.ALL else LogLevel.HEADERS
    }

    install(DefaultRequest) {
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        header("User-Agent", "LocationTrackingAndroid")
    }
}
