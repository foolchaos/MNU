package mnu.model.entity

import javax.persistence.*
import javax.validation.constraints.Min

enum class WeaponType {
    MELEE,
    PISTOL,
    SUBMACHINE_GUN,
    ASSAULT_RIFLE,
    LIGHT_MACHINE_GUN,
    SNIPER_RIFLE,
    ALIEN;

    companion object {
        fun fromClient(type: String?): WeaponType? = when (type) {
            "melee" -> MELEE
            "pistol" -> PISTOL
            "submachine_gun" -> SUBMACHINE_GUN
            "assault_rifle" -> ASSAULT_RIFLE
            "light_machine_gun" -> LIGHT_MACHINE_GUN
            "sniper_rifle" -> SNIPER_RIFLE
            "alien" -> ALIEN
            else -> null
        }
    }
}

@Entity
@Table(name = "weapons")
class Weapon(
    @Column(nullable = false)
    var name: String = "",
    @Enumerated(EnumType.STRING)
    var type: WeaponType = WeaponType.PISTOL,
    var description: String = "",
    @Min(0)
    var price: Double = 0.0,
    var requiredAccessLvl: Int = 0
) : BaseEntity<Long>() {
    @Min(0)
    var quantity: Long = 0
}