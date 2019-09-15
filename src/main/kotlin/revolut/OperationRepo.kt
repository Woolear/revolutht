package revolut

import java.time.Instant

class OperationRepo : InMemoryRepo<Operation>() {
    override val entityName: String
        get() = Operation::class.simpleName!!

    override fun save(op: Operation): Operation {
        val isOpening = op.operationType == OperationType.Opening
        if (isOpening) {
            require(op.debit == op.credit) { "debit must be eq to credit for opening ops" }
            require(op.amount >= 0.0) { "amount must be >= 0" }
        } else {
            require(op.debit != op.credit) { "debit must not be eq to credit" }
            require(op.amount > 0.0) { "amount must be > 0" }
        }
        //guard state and update ts
        return super.save(op.copy(state = OperationState.New, timestamp = Instant.now().toEpochMilli()))
    }
}