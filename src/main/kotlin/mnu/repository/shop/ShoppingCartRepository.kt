package mnu.repository.shop

import mnu.model.User
import mnu.model.shop.ShoppingCart
import mnu.model.shop.ShoppingCartStatus
import mnu.repository.BaseRepository
import org.springframework.stereotype.Repository

@Repository
interface ShoppingCartRepository : BaseRepository<ShoppingCart, Long> {
    fun findAllByUser(user: User): List<ShoppingCart>?

    fun findAllByUserAndStatus(user: User, status: ShoppingCartStatus): List<ShoppingCart>?
}