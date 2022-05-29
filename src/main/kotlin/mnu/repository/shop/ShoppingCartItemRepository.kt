package mnu.repository.shop

import mnu.model.Transport
import mnu.model.Weapon
import mnu.model.shop.ShoppingCart
import mnu.model.shop.ShoppingCartItem
import mnu.model.shop.ShoppingCartStatus
import mnu.repository.BaseRepository
import org.springframework.stereotype.Repository

@Repository
interface ShoppingCartItemRepository : BaseRepository<ShoppingCartItem, Long> {
    fun findAllByCart(cart: ShoppingCart): List<ShoppingCartItem>?

    fun findByWeaponAndCart(weapon: Weapon, cart: ShoppingCart): ShoppingCartItem?

    fun findByTransportAndCart(transport: Transport, cart: ShoppingCart): ShoppingCartItem?

    fun findAllByCartUserIdAndCartStatus(cartUserId: Long, status: ShoppingCartStatus): List<ShoppingCartItem>

    fun findByIdAndCartUserIdAndCartStatus(id: Long, cartUserId: Long, status: ShoppingCartStatus): ShoppingCartItem?
}