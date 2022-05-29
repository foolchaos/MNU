package mnu.model.employee

import mnu.model.BaseEntity
import mnu.model.Client
import mnu.model.Prawn
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.MapsId
import javax.persistence.OneToMany
import javax.persistence.OneToOne
import javax.persistence.Table

@Entity
@Table(name = "managers")
class ManagerEmployee : BaseEntity<Long>() {
    @OneToOne(fetch = FetchType.LAZY)
    var employee: Employee? = null

    @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], mappedBy = "manager")
    var clients: MutableList<Client>? = null

    @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], mappedBy = "manager")
    var prawns: MutableList<Prawn>? = null
}