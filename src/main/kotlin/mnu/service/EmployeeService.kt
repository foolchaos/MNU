package mnu.service

import mnu.model.entity.employee.Employee

interface EmployeeService {
    fun findAll(): List<Employee>

    fun save(entity: Employee): Employee

    fun delete(entity: Employee)

    fun findByUserId(id: Long): Employee

    fun findAllBySalaryGreaterThanEqual (salary: Long) : List<Employee>

    fun findAllByLevelLessThanEqualAndLevelGreaterThanEqual (levelLess: Long, levelGreater: Long) : List<Employee>
}