package mnu.model.entity.request

import mnu.model.entity.BaseEntity
import mnu.model.entity.employee.Employee
import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

enum class RequestStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}

@Entity
@Table(name = "requests")
class Request: BaseEntity<Long>() {

    @Enumerated(EnumType.STRING)
    var status: RequestStatus? = null

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "resolver_id", referencedColumnName = "user_id")
    var resolver: Employee? = null

    var statusDate: LocalDateTime? = null
}