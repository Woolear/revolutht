package revolut

import com.google.inject.Inject
import com.google.inject.Singleton

@Singleton
class AccountOperationManager @Inject constructor(private val operationsRepository: Repo<Operation>, private val accountRepository: Repo<Account>) {
    private val _accountLock: Any = Any()

    fun processOperation(id: Int): Operation {
        //assume saved operation passed initial checks
        val op = operationsRepository.get(id)
        return try {
            //if we put lock only at write, other threads can acquire not actual data, so read locked too
            //to test it remove sync, and watch test "should process all concurrent operations correctly"
            synchronized(_accountLock) {
                val debitAcc = accountRepository.get(op.debit)

                /*begin transaction*/
                /***
                 * This must be transactional in real world with persisted database
                but if we write to in-memory repo and something fail, nothing can save us)
                we can later just replay accepted operations to recover balance
                 ***/
                //if not opening - charge credit account
                when (op.operationType) {
                    OperationType.Transfer -> {
                        val creditAcc = accountRepository.get(op.credit)
                        require(creditAcc.balance >= op.amount) { "Insufficient funds" }
                        accountRepository.update(creditAcc.copy(balance = creditAcc.balance - op.amount))
                    }
                    OperationType.Opening -> {
                        require(operationsRepository.getAll().none { (it.debit == debitAcc.id || it.debit == debitAcc.id) && it.state == OperationState.Accepted }) { "There are some operations accepted on this account, balance opened already" }
                        require(debitAcc.balance == 0.0) { "Balance of opening account must be 0" }
                    }
                }
                accountRepository.update(debitAcc.copy(balance = debitAcc.balance + op.amount))
                operationsRepository.update(op.copy(state = OperationState.Accepted))
                /*commit transaction*/
            }
        } catch (ex: Exception) {
            synchronized(_accountLock) {
                operationsRepository.update(op.copy(state = OperationState.Rejected, comment = ex.message ?: ""))
                //recover
            }
            /*rollback transaction*/
        }
    }
}