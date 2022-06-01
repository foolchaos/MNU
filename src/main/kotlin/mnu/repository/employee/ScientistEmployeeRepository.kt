package mnu.repository.employee

import mnu.model.entity.employee.ScientistEmployee
import mnu.repository.BaseRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ScientistEmployeeRepository : BaseRepository<ScientistEmployee, Long> {


    @Query(
        "select s.employee_user_id as id, e.name as name, e.position as position, e.level as level from scientists s" +
                " inner join employees e on (s.employee_user_id = e.user_id) where (e.level < ?1);", nativeQuery = true
    )
    fun getAssistants(examinatorLvl: Int): List<Assistant>?

    interface Assistant {
        val id: Long;
        val name: String;
        val position: String;
        val level: Int
    }

    @Query(
        "select count(*) from scientists s inner join experiments ex on (s.id = ex.examinator_id);",
        nativeQuery = true
    )
    fun allConductedExperiments(): Long

    @Query(
        "select count(*) from scientists s inner join assistants_in_experiments aie on (s.id = aie.assistant_id);",
        nativeQuery = true
    )
    fun allAssistedExperiments(): Long

}