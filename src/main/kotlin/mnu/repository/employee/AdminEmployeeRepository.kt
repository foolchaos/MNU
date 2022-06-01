package mnu.repository.employee

import mnu.model.entity.employee.AdminEmployee
import mnu.repository.BaseRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface AdministratorEmployeeRepository : BaseRepository<AdminEmployee, Long> {

    @Query(
        value = "select count(*) from administrators a inner join requests r on (a.employee_id = r.resolver_id)" +
                " inner join new_weapon_requests nwr on (r.id = nwr.request_id)" +
                " where (r.status = 'RESOLVED');", nativeQuery = true
    )
    fun allResolvedNewWeaponRequests(): Long


    @Query(
        value = "select count(*) from administrators a inner join requests r on (a.employee_id = r.resolver_id)" +
                " inner join purchase_requests pr on (r.id = pr.request_id)" +
                " where (r.status = 'RESOLVED');", nativeQuery = true
    )
    fun allResolvedPurchaseRequests(): Long

    @Query(
        value = "select count(*) from administrators a inner join requests r on (a.employee_id = r.resolver_id)" +
                " inner join change_equipment_requests cer on (r.id = cer.request_id)" +
                " where (r.status = 'RESOLVED');", nativeQuery = true
    )
    fun allResolvedChangeEquipmentRequests(): Long

    @Query(
        value = "select count(*) from administrators a inner join requests r on (a.employee_id = r.resolver_id)" +
                " inner join vacancy_application_requests var on (r.id = var.request_id)" +
                " where (r.status = 'RESOLVED');", nativeQuery = true
    )
    fun allResolvedVacancyApplicationRequests(): Long

    @Query(
        value = "select count(*) from administrators a inner join requests r on (a.employee_id = r.resolver_id)" +
                " inner join new_transport_requests ntr on (r.id = ntr.request_id)" +
                " where (r.status = 'RESOLVED');", nativeQuery = true
    )
    fun allResolvedNewTransportRequests(): Long

    @Query(
        value = "select count(*) from administrators a inner join requests r on (a.employee_id = r.resolver_id)" +
                " inner join new_vacancy_requests nvr on (r.id = nvr.request_id)" +
                " where (r.status = 'RESOLVED');", nativeQuery = true
    )
    fun allResolvedNewVacancyRequests(): Long

}