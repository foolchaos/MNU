package mnu.model.entity.request

import mnu.model.entity.BaseEntity
import mnu.model.entity.Transport
import mnu.model.entity.Weapon
import mnu.model.entity.employee.SecurityEmployee
import javax.persistence.*

@Entity
@Table(name = "change_equipment_requests")
class ChangeEquipmentRequest(
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "requester_id", referencedColumnName = "id")
    var employee: SecurityEmployee? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "new_weapon_id", referencedColumnName = "id")
    var weapon: Weapon? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "new_transport_id", referencedColumnName = "id")
    var transport: Transport? = null
) : BaseEntity<Long>() {

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    var request: Request? = null
}