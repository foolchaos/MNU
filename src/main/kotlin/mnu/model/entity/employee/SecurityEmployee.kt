package mnu.model.entity.employee

import mnu.model.entity.BaseEntity
import mnu.model.entity.DistrictIncident
import mnu.model.entity.Transport
import mnu.model.entity.Weapon
import javax.persistence.*

@Entity
@Table(name = "security_employees")
class SecurityEmployee : BaseEntity<Long>() {
    @OneToOne(fetch = FetchType.LAZY)
    var employee: Employee? = null

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "weapon_id", referencedColumnName = "id")
    var weapon: Weapon? = null

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "transport_id", referencedColumnName = "id")
    var transport: Transport? = null

    @ManyToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], mappedBy = "assistants")
    var incidents: List<DistrictIncident>? = null
}