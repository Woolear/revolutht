package revolut

import com.google.inject.Inject
import com.google.inject.Singleton

@Singleton
class AccountOperationManager @Inject constructor(private val operationsRepository: Repo<Operation>, private val accountRepository: Repo<Account>) {
    private val _accountLock: Any = Any()

    fun processOperation(id: Int): Operation {
        val op = operationsRepository.get(id)
        return try {
            require(op.debit != op.credit) { "debit must not be eq to credit" }
            require(op.amount > 0.0) { "amount must be >0" }
            //if we put lock only at write, other threads can acquire not actual data, so read locked too
            //to test it remove sync, and watch test "should process all concurrent operations correctly"
           synchronized(_accountLock) {
                val debitAcc = accountRepository.get(op.debit)
                val creditAcc = accountRepository.get(op.credit)
                require(creditAcc.balance >= op.amount) { "Insufficient funds" }

                //this must be transactional in real world
                accountRepository.update(debitAcc.copy(balance = debitAcc.balance + op.amount))
                accountRepository.update(creditAcc.copy(balance = creditAcc.balance - op.amount))
                operationsRepository.update(op.copy(state = OperationState.Accepted))
                //end of tran
            }
        } catch (ex: Exception) {
            synchronized(_accountLock) {
                 operationsRepository.update(op.copy(state = OperationState.Rejected))
            }
        }
    }
}