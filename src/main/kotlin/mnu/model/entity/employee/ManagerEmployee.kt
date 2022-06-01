package mnu.model.entity.employee

import mnu.model.entity.BaseEntity
import mnu.model.entity.Client
import mnu.model.entity.Prawn
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.OneToMany
import javax.persistence.OneToOne
import javax.persistence.Table

@Entity
@Table(name = "managers")
class ManagerEmployee : BaseEntity<Long>(), java.io.Serializable {
    @OneToOne(fetch = FetchType.LAZY)
    var employee: Employee? = null

    @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], mappedBy = "manager")
    var clients: MutableList<Client>? = null

    @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], mappedBy = "manager")
    var prawns: MutableList<Prawn>? = null
}