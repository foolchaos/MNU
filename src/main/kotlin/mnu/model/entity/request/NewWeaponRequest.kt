package mnu.model.entity.request

import mnu.model.entity.BaseEntity
import mnu.model.entity.User
import mnu.model.entity.WeaponType
import javax.persistence.*
import javax.validation.constraints.Min

@Entity
@Table(name = "new_weapon_requests")
class NewWeaponRequest(
    @Column(nullable = false) var name: String = "",
    @Enumerated(EnumType.STRING) var type: WeaponType = WeaponType.PISTOL,
    var description: String = "",
    @Min(1) var quantity: Long = 0,
    var requiredAccessLvl: Int = 0,
    @Min(0) var price: Double = 0.0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", referencedColumnName = "id")
    var user: User? = null
) : BaseEntity<Long>() {
    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    var request: Request? = null
}