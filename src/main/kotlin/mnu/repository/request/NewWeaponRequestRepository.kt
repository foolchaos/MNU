package mnu.repository.request

import mnu.model.entity.User
import mnu.model.entity.request.NewWeaponRequest
import org.springframework.data.jpa.repository.JpaRepository


interface NewWeaponRequestRepository : JpaRepository<NewWeaponRequest, Long> {
    fun findAllByUser(user: User) : List<NewWeaponRequest>?
}