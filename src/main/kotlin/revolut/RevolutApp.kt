    package revolut

import com.google.inject.Guice
import dev.misfitlabs.kotlinguice4.getInstance
import io.javalin.Javalin
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import io.javalin.apibuilder.ApiBuilder.get
import io.javalin.apibuilder.ApiBuilder.post

class RevolutApp {

    private lateinit var app: Javalin

    fun start(port: Int): Javalin? {
        app = Javalin.create().apply {
            exception(Exception::class.java) { e, _ -> e.printStackTrace() }
            error(404) { ctx -> ctx.json("not found") }
        }.start(port)
        val injector = Guice.createInjector(RevolutModule())
        //account CRUD
        crudRoutes<Account>(app, "api/account", injector.getInstance())
        val accountOperationManager = injector.getInstance<AccountOperationManager>()
        val executor = Executors.newScheduledThreadPool(6)
        app.routes {
            val operationsRepo = injector.getInstance<Repo<Operation>>()
            get("/api/operations/:id") { ctx ->
                ctx.json(CompletableFuture<Operation>().apply { executor.submit { this.complete(injector.getInstance<Repo<Operation>>().get(ctx.pathParam("id").toInt())) } })
            }

            post("/api/operations/") { ctx ->
                val op = ctx.body<Operation>()
                val sop = operationsRepo.save(op)
                ctx.json(CompletableFuture<Operation>().apply { executor.submit { this.complete(accountOperationManager.processOperation(sop.id)) } })
            }
        }

        return app
    }

    fun stop() {
        app.stop()
    }
}

inline fun <reified T : HasId> crudRoutes(app: Javalin, path: String, repo: Repo<T>) {
    app.routes {
        get("$path/") { ctx ->
            ctx.json(repo.getAll())
        }
        get("$path/:id") { ctx ->
            val id = ctx.pathParam("id")
            try {
                val entity = repo.get(id.toInt())
                ctx.json(entity)
            } catch (ex: IllegalArgumentException) {
                ctx.status(404)
            }

        }

        post("$path/") { ctx ->
            val entity = ctx.body<T>()
            val saved = repo.save(entity)
            ctx.json(saved)
            ctx.status(201)

        }
    }
}