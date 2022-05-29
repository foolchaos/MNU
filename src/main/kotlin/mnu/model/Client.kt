package mnu.model

import mnu.model.employee.ManagerEmployee
import javax.persistence.*

enum class ClientType {
    CUSTOMER,
    MANUFACTURER
}

@Entity
@Table(name = "clients")
class Client(
    @Column(nullable = false)
    var name: String,
    @Column(nullable = false, unique = true)
    var email: String,
    @Enumerated(EnumType.STRING)
    var type: ClientType = ClientType.CUSTOMER
) : BaseEntity<Long>() {
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    var user: User? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", referencedColumnName = "employee_user_id")
    var manager: ManagerEmployee? = null
}