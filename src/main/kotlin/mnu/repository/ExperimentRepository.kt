package mnu.repository

//import mnu.model.ExperimentType
import mnu.model.entity.Experiment
import mnu.model.entity.ExperimentStatus
import mnu.model.entity.employee.ScientistEmployee
import org.springframework.stereotype.Repository

@Repository
interface ExperimentRepository : BaseRepository<Experiment, Long> {

    fun findAllByStatus(status: ExperimentStatus): List<Experiment>?

    fun findAllByExaminatorId(examinatorId: Long): List<Experiment>

    fun findAllByExaminatorIdOrderByStatusAsc(examinatorId: Long): List<Experiment>?

    fun findAllByExaminator(examinator: ScientistEmployee): List<Experiment>

    fun findAllByAssistant(assistant: ScientistEmployee): List<Experiment>

    fun countAllByStatus(status: ExperimentStatus): Long?
}