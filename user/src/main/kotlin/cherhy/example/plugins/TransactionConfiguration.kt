package cherhy.example.plugins

import cherhy.example.util.DatabaseFactory
import cherhy.example.util.TransactionType
import cherhy.example.util.TransactionType.READ_ONLY
import org.jetbrains.exposed.v1.r2dbc.R2dbcTransaction
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction

suspend fun <T> reactiveTransaction(
    transactionType: TransactionType = TransactionType.WRITE,
    block: suspend R2dbcTransaction.() -> T,
): T {
    val db = if (transactionType == READ_ONLY) DatabaseFactory.slaveDatabase else DatabaseFactory.masterDatabase
    return suspendTransaction(db = db) { block() }
}
