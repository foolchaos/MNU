package mnu.model.entity.employee

import mnu.model.entity.BaseEntity
import mnu.model.entity.Experiment
import javax.persistence.*

@Entity
@Table(name = "scientists")
class ScientistEmployee : BaseEntity<Long>() {
    @OneToOne(fetch = FetchType.LAZY)
    var employee: Employee? = null

    @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], mappedBy = "examinator")
    var conductedExperiments: MutableList<Experiment>? = null

    @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], mappedBy = "assistant")
    var assistedExperiments: MutableList<Experiment>? = null
}