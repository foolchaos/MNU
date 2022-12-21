package mnu.model.entity

import mnu.model.entity.employee.ManagerEmployee
import java.time.LocalDateTime
import javax.persistence.*

enum class DeathReason {
    EXPERIMENT,
    NATURAL,
    KILLED
}

@Entity
@Table(name = "prawns")
class Prawn(
    @Column(nullable = false)
    var name: String
) : BaseEntity<Long>() {
    @OneToOne(fetch = FetchType.LAZY)
    var user: User? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "house_id", referencedColumnName = "id")
    var districtHouse: DistrictHouse? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", referencedColumnName = "id")
    var manager: ManagerEmployee? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", referencedColumnName = "id")
    var job: Vacancy? = null

    var balance: Double = 0.0

    var karma: Long = 0

    var dateOfDeath: LocalDateTime? = null

    @Enumerated
    var deathReason: DeathReason? = null
}