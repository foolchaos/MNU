package mnu.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Table

enum class Role {
    CUSTOMER,
    MANUFACTURER,
    ADMIN,
    MANAGER,
    SECURITY,
    SCIENTIST,
    PRAWN
}

@Entity
@Table(name = "users")
class User(
    @Column(nullable = false, unique = true)
    var login: String = "",
    @Column(nullable = false)
    var password: String = "",
    @Enumerated(EnumType.STRING)
    var role: Role? = null
) : BaseEntity<Long>()