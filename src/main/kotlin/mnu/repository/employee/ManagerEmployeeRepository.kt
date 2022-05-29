package mnu.repository.employee

import mnu.model.employee.ManagerEmployee
import mnu.repository.BaseRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ManagerEmployeeRepository : BaseRepository<ManagerEmployee, Long> {
    @Query("select m.employee_user_id from managers m;", nativeQuery = true)
    fun getAllIds(): List<Long>

    @Query(
        value = "select e.user_id, e.name, e.level, m.position, e.salary from employees e " +
                "inner join managers m on (e.user_id = m.employee_user_id) where (e.status = 'WORKING');",
        nativeQuery = true
    )
    fun getAllWorkingManagers(): List<Array<Any>>

    @Query(
        value = "select count(*) from managers m inner join requests r on (m.employee_user_id = r.resolver_id)" +
                " inner join new_weapon_requests nwr on (r.id = nwr.request_id)" +
                " where (r.status = 'RESOLVED');", nativeQuery = true
    )
    fun allResolvedNewWeaponRequests(): Long

    @Query(
        value = "select count(*) from managers m inner join requests r on (m.employee_user_id = r.resolver_id)" +
                " inner join purchase_requests pr on (r.id = pr.request_id)" +
                " where (r.status = 'RESOLVED');", nativeQuery = true
    )
    fun allResolvedPurchaseRequests(): Long

    @Query(
        value = "select count(*) from managers m inner join requests r on (m.employee_user_id = r.resolver_id)" +
                " inner join change_equipment_requests cer on (r.id = cer.request_id)" +
                " where (r.status = 'RESOLVED');", nativeQuery = true
    )
    fun allResolvedChangeEquipmentRequests(): Long

    @Query(
        value = "select count(*) from managers m inner join requests r on (m.employee_user_id = r.resolver_id)" +
                " inner join vacancy_application_requests var on (r.id = var.request_id)" +
                " where (r.status = 'RESOLVED');", nativeQuery = true
    )
    fun allResolvedVacancyApplicationRequests(): Long

}