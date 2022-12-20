package mnu.repository.request

import mnu.model.entity.User
import mnu.model.entity.request.NewWeaponRequest
import mnu.repository.BaseRepository


interface NewWeaponRequestRepository : BaseRepository<NewWeaponRequest, Long> {
    fun findAllByUser(user: User) : List<NewWeaponRequest>?
}