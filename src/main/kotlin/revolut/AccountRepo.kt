package revolut

class AccountRepo : InMemoryRepo<Account>() {
    override val entityName: String
        get() = Account::class.simpleName!!

    override fun save(entity: Account): Account {
        require(entity.balance == 0.0) { "Cannot create account with not 0 balance. Use opening operation instead" }
        return super.save(entity)
    }
}