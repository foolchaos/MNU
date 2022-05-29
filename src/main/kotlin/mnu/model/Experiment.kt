package mnu.model

import mnu.model.employee.ScientistEmployee
import java.time.LocalDateTime
import javax.persistence.*

enum class ExperimentStatus {
    APPROVED,
    PENDING,
    FINISHED,
    REJECTED
}

@Entity
@Table(name = "experiments")
class Experiment(
    @Column(nullable = false)
    var title: String = "",

    var description: String = "",

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "examinator_id", referencedColumnName = "employee_id")
    var examinator: ScientistEmployee? = null,

    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "assistant_id", referencedColumnName = "employee_id")
    var assistant: ScientistEmployee? = null
) : BaseEntity<Long>() {
    var status: ExperimentStatus? = null
    var result: String? = null
    var statusDate: LocalDateTime? = null
}