package mnu.model.request

import mnu.model.BaseEntity
import mnu.model.User
import mnu.model.shop.ShoppingCart
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.MapsId
import javax.persistence.OneToOne
import javax.persistence.Table

@Entity
@Table (name = "purchase_requests")
class PurchaseRequest (
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", referencedColumnName = "id")
    var user: User? = null,

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cart_id", referencedColumnName = "id")
    var cart: ShoppingCart? = null
): BaseEntity<Long>() {

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    var request: Request? = null

}