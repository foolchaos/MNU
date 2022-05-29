package mnu.model.employee

import mnu.model.BaseEntity
import mnu.model.User
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Enumerated
import javax.persistence.MapsId
import javax.persistence.OneToOne
import javax.persistence.Table
import javax.validation.constraints.Max
import javax.validation.constraints.Min

enum class PersonStatus {
    WORKING,
    FIRED,
    DEAD
}

@Entity
@Table(name = "employees")
open class Employee(
    @Column(nullable = false)
    var name: String,
    var dateOfEmployment: LocalDateTime? = null,
    @Min(0) @Max(10)
    var level: Int? = null,
    @Min(0)
    var salary: Long? = null,
    var position: String? = null
) : BaseEntity<Long>() {
    @OneToOne
    var user: User? = null

    @Enumerated
    var status: PersonStatus? = null
}