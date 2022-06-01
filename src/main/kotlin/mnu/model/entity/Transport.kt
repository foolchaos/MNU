package mnu.model.entity

import javax.persistence.*
import javax.validation.constraints.Min

enum class TransportType {
    LAND,
    AIR;

    companion object {
        fun fromClient(type: String?): TransportType? = when (type) {
            "land" -> LAND
            "air" -> AIR
            else -> null
        }
    }
}

@Entity
@Table(name = "transport")
class Transport(
    @Column(nullable = false)
    var name: String = "",
    @Enumerated(EnumType.STRING)
    var type: TransportType = TransportType.LAND,
    var description: String = "",
    @Min(0)
    var price: Double = 0.0,
    var requiredAccessLvl: Int = 0
) : BaseEntity<Long>() {
    @Min(0)
    var quantity: Long = 0
}