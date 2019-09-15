package revolut

import dev.misfitlabs.kotlinguice4.KotlinModule

class RevolutModule : KotlinModule() {
    override fun configure() {
        bind<Repo<Account>>().to<InMemoryRepo<Account>>().asEagerSingleton()
        bind<Repo<Operation>>().to<InMemoryRepo<Operation>>().asEagerSingleton()
        // bind<AccountOperationManager>().to<AccountOperationManager>().asEagerSingleton()
    }

}