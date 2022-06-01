package mnu.repository.request

import mnu.model.entity.User
import mnu.model.entity.request.PurchaseRequest
import mnu.repository.BaseRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface PurchaseRequestRepository : BaseRepository<PurchaseRequest, Long> {
    fun findAllByUser(user: User): List<PurchaseRequest>?

    @Query(
        "select r.id, pr.requester_id, sc.id from purchase_requests pr" +
                " inner join requests r on (pr.request_id = r.id)" +
                " inner join shopping_carts sc on (pr.cart_id = sc.id)" +
                " inner join clients cl on (pr.requester_id = cl.id)" +
                " where ((cl.manager_id = ?1) and (r.status = 'PENDING'));", nativeQuery = true
    )
    fun getAllPendingRequestsForManagerByClient(managerId: Long): List<Array<Any>>
}