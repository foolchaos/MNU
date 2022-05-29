package mnu.repository

import mnu.model.Client
import mnu.model.ClientType
import mnu.model.employee.ManagerEmployee
import org.springframework.stereotype.Repository


@Repository
interface ClientRepository : BaseRepository<Client, Long> {

    fun findByEmail(email: String): Client

    fun findByUserId(id: Long): Client?

    fun findAllByManager(manager: ManagerEmployee): List<Client>

    fun findAllByManagerOrderByIdAsc(manager: ManagerEmployee): List<Client>?

    fun findAllByType(type: ClientType): List<Client>
}