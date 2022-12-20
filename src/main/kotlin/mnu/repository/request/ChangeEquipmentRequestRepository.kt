package mnu.repository.request

import mnu.model.entity.employee.SecurityEmployee
import mnu.model.entity.request.ChangeEquipmentRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ChangeEquipmentRequestRepository : JpaRepository<ChangeEquipmentRequest, Long> {
    fun findAllByEmployee(employee: SecurityEmployee): List<ChangeEquipmentRequest>?

    @Query(
        "select r.id, cer.requester_id, cer.new_weapon_id, cer.new_transport_id from change_equipment_requests cer" +
                " inner join requests r on (cer.request_id = r.id)" +
                " where (r.status = 'PENDING');", nativeQuery = true
    )
    fun getAllPendingRequests(): List<Array<Any>>

}