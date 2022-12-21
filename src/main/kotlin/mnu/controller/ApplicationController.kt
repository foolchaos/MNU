package mnu.controller

//import mnu.repository.CashRewardRepository
import mnu.repository.CashRewardRepository
import mnu.repository.ClientRepository
import mnu.repository.PrawnRepository
import mnu.repository.UserRepository
import mnu.repository.employee.EmployeeRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller

@Controller
class ApplicationController {
    @Autowired
    val userRepository: UserRepository? = null

    @Autowired
    val clientRepository: ClientRepository? = null

    @Autowired
    val prawnRepository: PrawnRepository? = null

    @Autowired
    val employeeRepository: EmployeeRepository? = null

    @Autowired
    val cashRewardRepository: CashRewardRepository? = null

}