package mnu.repository

//import mnu.model.Vacancy
import mnu.model.entity.Prawn
import mnu.model.entity.employee.ManagerEmployee
import org.springframework.stereotype.Repository

@Repository
interface PrawnRepository : BaseRepository<Prawn, Long> {
    fun findAllByManager(manager: ManagerEmployee): List<Prawn>

    fun findAllByManagerOrderByIdAsc(manager: ManagerEmployee): List<Prawn>?

    fun findByUserId(id: Long): Prawn?

    fun findAllByKarmaGreaterThanEqual(karma: Long): List<Prawn>

    fun findAllByKarmaLessThanEqual(karma: Long): List<Prawn>

//    fun findAllByJob(job: Vacancy): List<Prawn>

}