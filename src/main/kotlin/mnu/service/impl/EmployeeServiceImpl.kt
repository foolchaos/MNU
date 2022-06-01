package mnu.service.impl

import mnu.model.entity.employee.Employee
import mnu.repository.employee.EmployeeRepository
import mnu.service.EmployeeService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service("employeeService")
class EmployeeServiceImpl : EmployeeService {
    @Autowired
    val employeeRepository: EmployeeRepository? = null

    override fun findAll(): List<Employee> = (employeeRepository?.findAll() as List<Employee>?)!!

    override fun save(entity: Employee) = employeeRepository?.save(entity)!!

    override fun delete(entity: Employee) {
        employeeRepository?.delete(entity)
    }

    override fun findByUserId(id: Long) = employeeRepository?.findByUserId(id)!!

    override fun findAllBySalaryGreaterThanEqual (salary: Long) = employeeRepository?.findAllBySalaryGreaterThanEqual(salary)!!

    override fun findAllByLevelLessThanEqualAndLevelGreaterThanEqual (levelLess: Long, levelGreater: Long) =
        employeeRepository?.findAllByLevelLessThanEqualAndLevelGreaterThanEqual(levelLess,levelGreater)!!
}