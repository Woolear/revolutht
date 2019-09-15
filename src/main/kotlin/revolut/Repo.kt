package revolut

interface Repo<T : HasId> {
    fun get(id: Int): T
    fun getAll(): MutableCollection<T>
    fun update(entity: T): T
    fun save(entity: T): T
    fun delete(id: Int)
}