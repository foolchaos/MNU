package mnu.repository

import mnu.model.User
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : BaseRepository<User, Long> {
    fun findByLogin(login: String): User?
}