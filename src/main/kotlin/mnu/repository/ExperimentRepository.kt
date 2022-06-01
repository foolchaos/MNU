package mnu.repository

//import mnu.model.ExperimentType
import mnu.model.entity.Experiment
import mnu.model.entity.employee.ScientistEmployee
import org.springframework.stereotype.Repository

@Repository
interface ExperimentRepository : BaseRepository<Experiment, Long> {
//    fun findAllByType(type: ExperimentType): List<Experiment>

//    fun findAllByStatusAndType(status: ExperimentStatus, type: ExperimentType): List<Experiment>?

    fun findAllByExaminatorId(examinatorId: Long): List<Experiment>

    fun findAllByExaminatorIdOrderByStatusAsc(examinatorId: Long): List<Experiment>?

//    fun findAllByExaminatorAndType(examinator: ScientistEmployee, type: ExperimentType): List<Experiment>

    fun findAllByAssistant(assistant: ScientistEmployee): List<Experiment>

//    fun countAllByStatusAndType(status: ExperimentStatus, type: ExperimentType): Long?
}