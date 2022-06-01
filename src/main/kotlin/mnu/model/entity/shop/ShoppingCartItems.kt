package mnu.model.entity.shop

import mnu.model.entity.BaseEntity
import mnu.model.entity.Transport
import mnu.model.entity.Weapon
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "shopping_cart_items")
class ShoppingCartItem (
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "weapon_id", referencedColumnName = "id")
    var weapon: Weapon? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "transport_id", referencedColumnName = "id")
    var transport: Transport? = null,

    var quantity: Long? = null
): BaseEntity<Long>() {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cart_id", referencedColumnName = "id")
    var cart: ShoppingCart? = null

    fun name() = weapon?.name ?: transport!!.name
    fun price() = weapon?.price ?: transport!!.price
}
