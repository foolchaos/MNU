package mnu.repository

import mnu.model.entity.Client
import mnu.model.entity.ClientType
import mnu.model.entity.employee.ManagerEmployee
import org.springframework.stereotype.Repository


@Repository
interface ClientRepository : BaseRepository<Client, Long> {

    fun findByEmail(email: String): Client

    fun findByUserId(id: Long): Client?

    fun findAllByManager(manager: ManagerEmployee): List<Client>

    fun findAllByManagerOrderByIdAsc(manager: ManagerEmployee): List<Client>?

    fun findAllByType(type: ClientType): List<Client>
}