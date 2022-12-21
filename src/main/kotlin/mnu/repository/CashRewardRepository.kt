package mnu.repository

import mnu.model.entity.CashReward
import mnu.model.entity.employee.Employee
import org.springframework.data.jpa.repository.JpaRepository

interface CashRewardRepository : JpaRepository<CashReward, Long> {
    fun findAllByEmployee (employee: Employee) : List<CashReward>

    fun findAllByEmployeeOrderByIssueDateDesc (employee: Employee) : List<CashReward>?
}