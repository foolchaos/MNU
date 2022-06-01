package mnu.repository.employee

import mnu.model.entity.employee.Employee
import mnu.model.entity.employee.PersonStatus
import mnu.repository.BaseRepository
import org.springframework.stereotype.Repository

@Repository
interface EmployeeRepository : BaseRepository<Employee, Long> {

    fun findAllByOrderByIdAsc(): List<Employee>?

    fun findAllByNameIgnoreCaseContainingOrderByIdAsc(name: String): List<Employee>?

    fun findAllByNameIgnoreCaseContaining(name: String): List<Employee>?

    fun findByUserId(id: Long): Employee?

    fun findAllBySalaryGreaterThanEqual(salary: Long): List<Employee>?

    fun findAllByLevelLessThanEqualAndLevelGreaterThanEqual(levelLess: Long, levelGreater: Long): List<Employee>?

    fun findAllByStatus(status: PersonStatus): List<Employee>?

}