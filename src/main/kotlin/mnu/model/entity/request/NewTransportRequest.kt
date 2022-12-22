package mnu.model.entity.request

import mnu.model.entity.BaseEntity
import mnu.model.entity.Client
import mnu.model.entity.TransportType
import javax.persistence.*
import javax.validation.constraints.Min

@Entity
@Table(name = "new_transport_requests")
class NewTransportRequest(
    @Column(nullable = false) var name: String = "",
    @Enumerated(EnumType.STRING) var type: TransportType = TransportType.LAND,
    var description: String = "",
    @Min(1) var quantity: Long = 1,
    var requiredAccessLvl: Int = 0,
    @Min(0) var price: Double = 0.0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", referencedColumnName = "id")
    var client: Client? = null
) : BaseEntity<Long>() {
    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    var request: Request? = null
}