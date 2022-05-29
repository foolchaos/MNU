package mnu.model.shop

import mnu.model.BaseEntity
import mnu.model.User
import java.time.LocalDateTime
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.Table

enum class ShoppingCartStatus {
    CREATING,
    REQUESTED,
    REJECTED,
    RETRIEVED
}

@Entity
@Table(name = "shopping_carts")
class ShoppingCart(
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "client_id", referencedColumnName = "id")
    var user: User? = null,

    var dateOfCreation: LocalDateTime = LocalDateTime.now()
): BaseEntity<Long>() {

    @Enumerated(EnumType.STRING)
    var status: ShoppingCartStatus? = null

    @OneToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL], mappedBy = "cart")
    var items: MutableList<ShoppingCartItem>? = null
}