package mnu.repository.request

import mnu.model.entity.Client
import mnu.model.entity.request.NewVacancyRequest
import mnu.repository.BaseRepository
import org.springframework.data.jpa.repository.*

interface NewVacancyRequestRepository : BaseRepository<NewVacancyRequest, Long> {
    fun findAllByClient(client: Client) : List<NewVacancyRequest>?

    @Query("select r.id, nvr.requester_id, nvr.title, nvr.salary, nvr.required_karma, nvr.vacant_places, nvr.required_karma from new_vacancy_requests nvr" +
            " inner join requests r on (nvr.request_id = r.id)  where (r.status = 'PENDING');", nativeQuery = true)
    fun getAllPendingRequests() : List<Array<Any>>
}