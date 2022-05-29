package mnu.repository


import mnu.model.Weapon
import mnu.model.WeaponType
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository

@Repository
interface WeaponRepository : BaseRepository<Weapon, Long> {
    fun findAllByQuantityGreaterThanEqual(quantity: Long, sort: Sort): List<Weapon>

    fun findAllByType(type: WeaponType, sort: Sort): List<Weapon>

    fun findAllByTypeAndQuantityGreaterThan(type: WeaponType, quantity: Long, sort: Sort): List<Weapon>

    fun findAllByRequiredAccessLvlLessThanEqualAndQuantityGreaterThanOrderByIdAsc(
        accessLevel: Int,
        quantity: Long
    ): List<Weapon>

    fun findAllByNameIgnoreCaseContaining(name: String, sort: Sort): List<Weapon>
    fun findAllByNameIgnoreCaseContainingAndType(name: String, type: WeaponType, sort: Sort): List<Weapon>

    fun findAllByNameIgnoreCaseContainingAndQuantityGreaterThan(name: String, quantity: Long, sort: Sort): List<Weapon>
    fun findAllByNameIgnoreCaseContainingAndTypeAndQuantityGreaterThan(
        name: String,
        type: WeaponType,
        quantity: Long,
        sort: Sort
    ): List<Weapon>
}