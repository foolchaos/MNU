package mnu.repository.request

import mnu.model.entity.Vacancy
import mnu.model.entity.Prawn
import mnu.model.entity.request.VacancyApplicationRequest
import org.springframework.data.jpa.repository.*

interface VacancyApplicationRequestRepository : JpaRepository<VacancyApplicationRequest, Long> {
    fun findAllByPrawn(prawn: Prawn) : List<VacancyApplicationRequest>?

    fun findAllByVacancy(vacancy: Vacancy) : List<VacancyApplicationRequest>?

    @Query("select r.id, var.prawn_id, var.vacancy_id from vacancy_application_requests var" +
            " inner join requests r on (var.request_id = r.id)" +
            " inner join prawns pr on (var.prawn_id = pr.id)" +
            " where ((pr.manager_id = ?1) and (r.status = 'PENDING'));", nativeQuery = true)
    fun getAllPendingRequestsForManagerByPrawn(managerId: Long) : List<Array<Any>>
}