package mnu.repository.request

import mnu.model.entity.employee.Employee
import mnu.model.entity.request.Request
import mnu.model.entity.request.RequestStatus
import mnu.repository.BaseRepository
import org.springframework.stereotype.Repository

@Repository
interface RequestRepository : BaseRepository<Request, Long> {
    fun findAllByStatus(status: RequestStatus): List<Request>?

    fun findAllByResolver(resolver: Employee): List<Request>?
}