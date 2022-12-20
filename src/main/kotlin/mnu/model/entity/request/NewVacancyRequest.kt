package mnu.model.entity.request

import mnu.model.entity.BaseEntity
import mnu.model.entity.Client
import javax.persistence.*
import javax.validation.constraints.Min

@Entity
@Table(name = "new_vacancy_requests")
class NewVacancyRequest(
    @Column(nullable = false) var title: String = "",
    @Min(1) var salary: Double = 0.0,
    @Min(0) var requiredKarma: Long = 0,
    @Min(0) var workHoursPerWeek: Int = 0,
    @Min(1) var vacantPlaces: Long = 1,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", referencedColumnName = "id")
    var client: Client? = null
): BaseEntity<Long>() {
    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    var request: Request? = null
}