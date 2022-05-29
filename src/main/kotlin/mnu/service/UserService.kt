package mnu.service

import mnu.model.User

interface UserService {
    fun findAll(): List<User>

    fun save(entity: User): User

    fun delete(entity: User)

    fun findById(id: Int): User

    fun findByLogin(login: String): User
}