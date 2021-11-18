package me.alekseinovikov.blockchain

import me.alekseinovikov.blockchain.plugins.configureSerializationWithRouting
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureSerializationWithRouting()
    }.start(wait = true)
}
