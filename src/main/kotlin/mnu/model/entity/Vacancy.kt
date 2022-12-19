package mnu.model

import mnu.model.entity.BaseEntity
import mnu.model.entity.Prawn
import javax.persistence.*
import javax.validation.constraints.Min


@Entity
@Table(name = "vacancies")
class Vacancy (@Column(nullable = false) var title: String = "",
                    @Min(1) var salary: Double = 1.0,
                    @Min(0) var requiredKarma: Long = 0,
                    @Min(0) var workHoursPerWeek: Int = 0): BaseEntity<Long>() {

    @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], mappedBy = "job")
    var workers: MutableList<Prawn>? = null

    @Min(0)
    var vacantPlaces: Long = 0

}