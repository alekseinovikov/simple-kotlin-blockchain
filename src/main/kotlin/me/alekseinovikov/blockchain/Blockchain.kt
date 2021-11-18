package me.alekseinovikov.blockchain

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.google.common.hash.Hashing
import java.math.BigDecimal
import java.time.ZonedDateTime

data class Transaction(
    val sender: String,
    val recipient: String,
    val amount: BigDecimal
)

data class Block(
    val index: Int,
    @field:JsonIgnore val timeStamp: ZonedDateTime = ZonedDateTime.now(),
    val previousHash: String,
    val transactions: List<Transaction>,
    var proof: Long = 0
)

interface Blockchain {
    fun addTransactionAndGetBlockIndex(transaction: Transaction): Int
    fun getBlock(index: Int): Block
    fun checkAndRegisterBlock(proof: Long): Boolean
    fun hash(block: Block): String
    fun buildBlockAndMining(): Boolean
    fun getChain(): List<Block>

    val lastIndex: Int
}

class LohcChain : Blockchain {
    private val chain: MutableList<Block> = mutableListOf()
    private val currentTransactions: MutableList<Transaction> = mutableListOf()

    private val miningLastZeroCount = 4
    private val objectMapper = ObjectMapper().registerKotlinModule()

    init {
        val initBlock = Block(0, ZonedDateTime.now(), "0", listOf(), 0)
        chain.add(initBlock)
    }

    override fun addTransactionAndGetBlockIndex(transaction: Transaction): Int {
        currentTransactions.add(transaction)
        return lastIndex + 1
    }

    override fun getBlock(index: Int): Block = chain[index]

    override fun checkAndRegisterBlock(proof: Long): Boolean {
        val block = Block(
            index = lastIndex + 1,
            timeStamp = ZonedDateTime.now(),
            previousHash = hash(chain[lastIndex]),
            transactions = mutableListOf<Transaction>().also { it.addAll(currentTransactions) },
            proof = proof
        )

        if (!proofInBlockValid(block)) {
            return false
        }

        if (hash(block) !in chain.map { hash(it) }) {
            chain.add(block)
            currentTransactions.clear()
            return true
        }

        return false
    }

    override fun buildBlockAndMining(): Boolean {
        val block = Block(
            index = lastIndex + 1,
            timeStamp = ZonedDateTime.now(),
            previousHash = getLastBlockHash(),
            currentTransactions
        )

        val proof = mining(block)
        return checkAndRegisterBlock(proof)
    }

    override fun getChain(): List<Block> = chain

    private fun mining(block: Block): Long {
        while (hash(block).takeLast(miningLastZeroCount) != "0".repeat(miningLastZeroCount)) {
            block.proof++
        }

        return block.proof
    }

    private fun proofInBlockValid(block: Block): Boolean =
        hash(block).takeLast(miningLastZeroCount) == "0".repeat(miningLastZeroCount)

    private fun getLastBlockHash() = hash(chain[lastIndex])
    override fun hash(block: Block): String = objectMapper.writeValueAsString(block).sha256()
    private fun String.sha256() = Hashing.sha256().hashString(this, Charsets.UTF_8).toString()

    override val lastIndex: Int
        get() = chain.lastIndex
}