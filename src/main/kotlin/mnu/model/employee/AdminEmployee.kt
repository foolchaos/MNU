package mnu.model.employee

import mnu.model.BaseEntity
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.MapsId
import javax.persistence.OneToOne
import javax.persistence.Table

@Entity
@Table(name = "admins")
class AdminEmployee : BaseEntity<Long>() {
    @OneToOne(fetch = FetchType.LAZY)
    var employee: Employee? = null
}