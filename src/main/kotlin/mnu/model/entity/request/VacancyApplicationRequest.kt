package mnu.model.entity.request

import mnu.model.entity.BaseEntity
import mnu.model.entity.Prawn
import mnu.model.entity.Vacancy
import javax.persistence.*

@Entity
@Table (name = "vacancy_application_requests")
class VacancyApplicationRequest (@ManyToOne(fetch = FetchType.EAGER)
                                      @JoinColumn(name = "prawn_id", referencedColumnName = "user_id")
                                      var prawn: Prawn? = null,

                                      @ManyToOne(fetch = FetchType.EAGER)
                                      @JoinColumn(name = "vacancy_id", referencedColumnName = "id")
                                      var vacancy: Vacancy? = null): BaseEntity<Long>() {

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    var request: Request? = null
}