package mnu.model.entity

import mnu.model.entity.employee.Employee
import java.time.LocalDateTime
import javax.persistence.*
import javax.validation.constraints.Min

@Entity
@Table(name = "cash_rewards")
class CashReward (@ManyToOne(fetch = FetchType.EAGER)
                       @JoinColumn(name = "employee_id", referencedColumnName = "user_id")
                       var employee: Employee? = null,

                       @Min(1) var reward: Long = 1,
                       var issueDate: LocalDateTime = LocalDateTime.now())
    :BaseEntity<Long>() {
}