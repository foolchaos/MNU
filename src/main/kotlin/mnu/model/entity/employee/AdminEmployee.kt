package mnu.model.entity.employee

import mnu.model.entity.BaseEntity
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.OneToOne
import javax.persistence.Table

@Entity
@Table(name = "admins")
class AdminEmployee : BaseEntity<Long>() {
    @OneToOne(fetch = FetchType.LAZY)
    var employee: Employee? = null
}