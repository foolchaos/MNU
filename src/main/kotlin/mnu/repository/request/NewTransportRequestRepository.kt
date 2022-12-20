package mnu.repository.request

import mnu.model.entity.Client
import mnu.model.entity.request.NewTransportRequest
import mnu.repository.BaseRepository
import org.springframework.data.jpa.repository.Query

interface NewTransportRequestRepository : BaseRepository<NewTransportRequest, Long> {
    fun findAllByClient(client: Client) : List<NewTransportRequest>?

    @Query("select r.id, ntr.requester_id, ntr.name, ntr.type, ntr.description, ntr.quantity, ntr.required_access_lvl from new_transport_requests ntr" +
            " inner join requests r on (ntr.request_id = r.id) where (r.status = 'PENDING');", nativeQuery = true)
    fun getAllPendingRequests() : List<Array<Any>>
}