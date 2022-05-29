package mnu.repository

import mnu.model.Transport
import mnu.model.TransportType
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository

@Repository
interface TransportRepository : BaseRepository<Transport, Long> {
    fun findAllByQuantityGreaterThanEqual(quantity: Long, sort: Sort): List<Transport>

    fun findAllByType(type: TransportType, sort: Sort): List<Transport>

    fun findAllByTypeAndQuantityGreaterThan(type: TransportType, quantity: Long, sort: Sort): List<Transport>

    fun findAllByRequiredAccessLvlLessThanEqualAndQuantityGreaterThan(accessLevel: Int, quantity: Long): List<Transport>

    fun findAllByRequiredAccessLvlLessThanEqualAndQuantityGreaterThanOrderByIdAsc(
        accessLevel: Int,
        quantity: Long
    ): List<Transport>

    fun findAllByNameIgnoreCaseContaining(name: String, sort: Sort): List<Transport>
    fun findAllByNameIgnoreCaseContainingAndType(name: String, type: TransportType, sort: Sort): List<Transport>

    fun findAllByNameIgnoreCaseContainingAndQuantityGreaterThan(
        name: String,
        quantity: Long,
        sort: Sort
    ): List<Transport>

    fun findAllByNameIgnoreCaseContainingAndTypeAndQuantityGreaterThan(
        name: String,
        type: TransportType,
        quantity: Long,
        sort: Sort
    ): List<Transport>
}