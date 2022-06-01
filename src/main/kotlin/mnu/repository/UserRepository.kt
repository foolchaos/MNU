package mnu.repository

import mnu.model.entity.User
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : BaseRepository<User, Long> {
    fun findByLogin(login: String): User?
}