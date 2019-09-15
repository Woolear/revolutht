package revolut

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

open class InMemoryRepo<T : HasId>() : Repo<T> {

    private val backingHM = ConcurrentHashMap<Int, T>()
    private val nextId = AtomicInteger(0)
    override fun get(id: Int): T {
        require(backingHM.containsKey(id)) { "Entity with id $id not exists" }
        return backingHM[id]!!
    }

    override fun getAll(): MutableCollection<T> {
        return backingHM.values
    }

    override fun update(entity: T): T {
        val id = entity.id
        require(backingHM.containsKey(id)) { "Entity with id $id not exists" }
        backingHM[entity.id] = entity
        return entity
    }

    override fun save(entity: T): T {
        require(entity.id == 0) { "Id is not empty! Use Update instead" }
        entity.id = nextId.incrementAndGet()
        backingHM[entity.id] = entity
        return entity
    }
    override fun delete(id: Int) {
        require(backingHM.containsKey(id)) { "Entity with id $id not exists" }
        backingHM.remove(id)
    }
}