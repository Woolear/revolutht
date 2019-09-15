package revolut

import dev.misfitlabs.kotlinguice4.KotlinModule

class RevolutModule : KotlinModule() {
    override fun configure() {
        bind<Repo<Account>>().to<AccountRepo>().asEagerSingleton()
        bind<Repo<Operation>>().to<OperationRepo>().asEagerSingleton()
    }

}