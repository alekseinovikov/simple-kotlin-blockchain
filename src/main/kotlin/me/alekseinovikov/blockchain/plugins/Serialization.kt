package me.alekseinovikov.blockchain.plugins

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.jackson.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import me.alekseinovikov.blockchain.Blockchain
import me.alekseinovikov.blockchain.LohcChain
import me.alekseinovikov.blockchain.Transaction

fun Application.configureSerializationWithRouting() {
    val blockchain: Blockchain = LohcChain()

    install(ContentNegotiation) {
        jackson()
    }

    routing {
        post("/add") {
            val transaction = call.receive<Transaction>()
            val blockIndex = blockchain.addTransactionAndGetBlockIndex(transaction)
            call.respond(mapOf("blockId" to blockIndex))
        }

        post("/build") {
            val success = blockchain.buildBlockAndMining()
            call.respond(mapOf("success" to success))
        }

        get("/block/{index}") {
            val blockIndex = call.parameters["index"]!!.toInt()
            val block = blockchain.getBlock(blockIndex)
            call.respond(block)
        }

        get("/chain") {
            call.respond(blockchain.getChain())
        }
    }
}
