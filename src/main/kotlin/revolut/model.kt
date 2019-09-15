package revolut

import java.time.Instant

enum class OperationState {
    New,
    Rejected,
    Accepted
}

interface HasId {
    var id: Int
}

data class Account(val name: String, val balance: Double, val currency: String, override var id: Int = 0) : HasId
data class Operation(val debit: Int, val credit: Int, val amount: Double, val state: OperationState = OperationState.New, override var id: Int = 0, val timestamp: Long = Instant.now().toEpochMilli(), val comment: String ="") : HasId

