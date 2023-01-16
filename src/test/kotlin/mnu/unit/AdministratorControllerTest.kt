package mnu.unit

import com.ninjasquad.springmockk.MockkBean
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import mnu.EmailSender
import mnu.config.SecurityConfig
import mnu.controller.AdministratorController
import mnu.model.entity.*
import mnu.model.entity.employee.*
import mnu.model.entity.request.*
import mnu.model.entity.shop.ShoppingCart
import mnu.model.form.*
import mnu.repository.*
import mnu.repository.employee.*
import mnu.repository.request.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.security.Principal
import java.util.*
import javax.sql.DataSource


@WebMvcTest(controllers = [AdministratorController::class])
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = [SecurityConfig::class])
@Import(AdministratorController::class)
class AdministratorControllerTest(@Autowired var mockMvc: MockMvc) {
    @MockkBean
    lateinit var dataSource: DataSource

    @MockkBean
    lateinit var districtIncidentRepository: DistrictIncidentRepository

    @MockkBean
    lateinit var purchaseRequestRepository: PurchaseRequestRepository

    @MockkBean
    lateinit var weaponRepository: WeaponRepository

    @MockkBean
    lateinit var transportRepository: TransportRepository

    @MockkBean
    lateinit var experimentRepository: ExperimentRepository

    @MockkBean
    lateinit var managerEmployeeRepository: ManagerEmployeeRepository

    @MockkBean
    lateinit var securityEmployeeRepository: SecurityEmployeeRepository

    @MockkBean
    lateinit var scientistEmployeeRepository: ScientistEmployeeRepository

    @MockkBean
    lateinit var administratorEmployeeRepository: AdministratorEmployeeRepository

    @MockkBean
    lateinit var districtHouseRepository: DistrictHouseRepository

    @MockkBean
    lateinit var requestRepository: RequestRepository

    @MockkBean
    lateinit var vacancyRepository: VacancyRepository

    @MockkBean
    lateinit var newVacancyRequestRepository: NewVacancyRequestRepository

    @MockkBean
    lateinit var vacancyApplicationRequestRepository: VacancyApplicationRequestRepository

    @MockkBean
    lateinit var newWeaponRequestRepository: NewWeaponRequestRepository

    @MockkBean
    lateinit var newTransportRequestRepository: NewTransportRequestRepository

    @MockkBean
    lateinit var employeeRepository: EmployeeRepository

    @MockkBean
    lateinit var emailSender: EmailSender

    @MockkBean
    lateinit var clientRepository: ClientRepository

    @MockkBean
    lateinit var userRepository: UserRepository

    private val mockPrincipal: Principal = mockk()

    private val testAdministratorUser: User = User(login = "rogoadmin", role = Role.ADMIN).apply { id = 313 }

    @BeforeEach
    fun setUp() {
        every { mockPrincipal.name } returns testAdministratorUser.login
        every { userRepository.findByLogin(testAdministratorUser.login) } returns testAdministratorUser
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @WithMockUser(value = "rogoadmin", roles = ["ADMIN"])
    @Test
    fun `main OK`() {
        every {
            requestRepository.findAllByStatus(RequestStatus.PENDING)
        } returns ArrayList()

        every {
            purchaseRequestRepository.findAll()
        } returns ArrayList()

        every {
            newWeaponRequestRepository.findAll()
        } returns ArrayList()

        every {
            newTransportRequestRepository.findAll()
        } returns ArrayList()

        every {
            newVacancyRequestRepository.findAll()
        } returns ArrayList()

        every {
            vacancyApplicationRequestRepository.findAll()
        } returns ArrayList()

        every {
            experimentRepository.countAllByStatus(ExperimentStatus.PENDING)
        } returns 0L

        every {
            districtIncidentRepository.findAllByLevelToAndDangerLevelGreaterThan(0, 0)?.size
        } returns 0

        mockMvc.perform(
            MockMvcRequestBuilders.get("/admin/main").principal(mockPrincipal)
        ).andExpect(status().isOk)
            .andExpect(model().attribute("new_prod_count", 0))
            .andExpect(model().attribute("new_vac_count", 0))
            .andExpect(model().attribute("vac_appl_count", 0))
            .andExpect(model().attribute("purch_count", 0))
            .andExpect(model().attribute("experiment_count", 0L))
            .andExpect(model().attribute("ongoing_incidents", 0))
    }

    @WithMockUser(value = "rogoadmin", roles = ["ADMIN"])
    @Test
    fun `experiments OK`() {
        val experiments = ArrayList<Experiment>()

        every {
            experimentRepository.findAllByStatus(ExperimentStatus.PENDING)
        } returns experiments

        mockMvc.perform(
            MockMvcRequestBuilders.get("/admin/experiments").principal(mockPrincipal)
        ).andExpect(status().isOk)
            .andExpect(model().attribute("experiments", experiments))
    }

    @WithMockUser(value = "rogoadmin", roles = ["ADMIN"])
    @Test
    fun `district OK`() {
        val incidents = ArrayList<DistrictIncident>()

        every {
            districtIncidentRepository.findAllByLevelToAndDangerLevelGreaterThan(0, 0)
        } returns incidents

        mockMvc.perform(
            MockMvcRequestBuilders.get("/admin/district").principal(mockPrincipal)
        ).andExpect(status().isOk)
            .andExpect(model().attribute("current_incidents", incidents))
            .andExpect(model().attribute("form", AppointResolversForm()))
    }

    @WithMockUser(value = "rogoadmin", roles = ["ADMIN"])
    @Test
    fun `purchaseRequest OK`() {
        val purchaseRequests = arrayListOf(preparePurchaseRequest())

        every {
            purchaseRequestRepository.findAll()
        } returns purchaseRequests

        mockMvc.perform(
            MockMvcRequestBuilders.get("/admin/purchaseRequests").principal(mockPrincipal)
        ).andExpect(status().isOk)
            .andExpect(model().attribute("requests", purchaseRequests))
    }

    @WithMockUser(value = "rogoadmin", roles = ["ADMIN"])
    @Test
    fun `employee OK`() {
        val q = "q"
        val employee = Employee(name = "employee.test.admin")
        employee.user = testAdministratorUser

        every {
            employeeRepository.findAllByNameIgnoreCaseContainingOrderByIdAsc(q)
        } returns listOf(employee)

        every {
            employeeRepository.findAllByOrderByIdAsc()
        } returns listOf(employee)

        mockMvc.perform(
            MockMvcRequestBuilders.get("/admin/employee").param("q", q).principal(mockPrincipal)
        ).andExpect(status().isOk)
            .andExpect(model().attribute("form_add", EmployeeRegistrationForm()))
            .andExpect(model().attribute("form_edit", EmployeeEditForm()))
            .andExpect(model().attribute("form_reward", CashRewardForm()))
    }

    @WithMockUser(value = "rogoadmin", roles = ["ADMIN"])
    @Test
    fun `acceptExperiment OK`() {
        val id = 0L

        prepareExperiment(id)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/admin/acceptExperiment/{id}", id).principal(mockPrincipal)
        ).andExpect(status().isFound).andExpect(flash().attribute("status", "Request accepted."))
    }

    @WithMockUser(value = "rogoadmin", roles = ["ADMIN"])
    @Test
    fun `rejectExperiment OK`() {
        val id = 0L

        prepareExperiment(id)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/admin/rejectExperiment/{id}", id).principal(mockPrincipal)
        ).andExpect(status().isFound).andExpect(flash().attribute("status", "Request rejected."))
    }

    @WithMockUser(value = "rogoadmin", roles = ["ADMIN"])
    @Test
    fun `undoExperimentChoice OK`() {
        val id = 0L

        prepareExperiment(id)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/admin/undoExperimentChoice/{id}", id).principal(mockPrincipal)
        ).andExpect(status().isOk)
    }

    @WithMockUser(value = "rogoadmin", roles = ["ADMIN"])
    @Test
    fun `acceptPurchaseRequest OK`() {
        val id = 0L

        setUpPurchaseRequest(id)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/admin/acceptPurchaseRequest/{id}", id).principal(mockPrincipal)
        ).andExpect(status().isFound).andExpect(flash().attribute("status", "Request accepted."))
    }

    @WithMockUser(value = "rogoadmin", roles = ["ADMIN"])
    @Test
    fun `rejectPurchaseRequest OK`() {
        val id = 0L

        setUpPurchaseRequest(id)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/admin/rejectPurchaseRequest/{id}", id).principal(mockPrincipal)
        ).andExpect(status().isFound).andExpect(flash().attribute("status", "Request rejected."))
    }

    @WithMockUser(value = "rogoadmin", roles = ["ADMIN"])
    @Test
    fun `registerEmployee OK`() {
        val employeeRegistrationForm = EmployeeRegistrationForm(
            username = "registerEmployee.test.form.username",
            password = "registerEmployee.test.from.password",
            type = "administrator"
        )

        val user = User()

        val employee = Employee(name = "registerEmployee.test.employee")

        every {
            userRepository.findByLogin(employeeRegistrationForm.username)
        } returns user

        every {
            userRepository.save(user)
        } returns user

        every {
            employeeRepository.save(employee)
        } returns employee

        val managerEmployee = ManagerEmployee().apply { this.employee = employee }

        every {
            managerEmployeeRepository.save(managerEmployee)
        } returns managerEmployee

        val scientistEmployee = ScientistEmployee().apply { this.employee = employee }

        every {
            scientistEmployeeRepository.save(scientistEmployee)
        } returns scientistEmployee

        val securityEmployee = SecurityEmployee().apply { this.employee = employee }

        every {
            securityEmployeeRepository.save(securityEmployee)
        } returns securityEmployee

        val adminEmployee = AdminEmployee().apply { this.employee = employee }

        every {
            administratorEmployeeRepository.save(adminEmployee)
        } returns adminEmployee

        mockMvc.perform(
            MockMvcRequestBuilders.post("/admin/registerEmployee")
                .sessionAttr("form", employeeRegistrationForm)
                .principal(mockPrincipal)
        ).andExpect(status().isFound)
            .andExpect(flash().attribute("form", employeeRegistrationForm))
            .andExpect(flash().attribute("status", "Successfully registered a new employee."))
    }

    @WithMockUser(value = "rogoadmin", roles = ["ADMIN"])
    @Test
    fun `addVacancy OK`() {
        val vacancy = Vacancy()

        every {
            vacancyRepository.save(vacancy)
        } returns vacancy

        mockMvc.perform(
            MockMvcRequestBuilders.post("/admin/addVacancy")
                .sessionAttr("form", NewVacancyForm())
                .principal(mockPrincipal)
        ).andExpect(status().isFound)
            .andExpect(flash().attribute("form", NewVacancyForm()))
    }

    private fun prepareExperiment(id: Long) {
        val experiment = Experiment()
        experiment.status = ExperimentStatus.PENDING

        every {
            experimentRepository.findById(id)
        } returns Optional.of(experiment)

        every {
            experimentRepository.save(match {
                it.status == experiment.status
            })
        } returns experiment
    }

    private fun preparePurchaseRequest(): PurchaseRequest {
        val user = User()
        user.id = 0L
        user.role = Role.CUSTOMER

        val request = Request()
        request.status = RequestStatus.PENDING

        val cart = ShoppingCart()
        cart.items = mutableListOf()

        val purchaseRequest = PurchaseRequest()
        purchaseRequest.user = user
        purchaseRequest.request = request
        purchaseRequest.cart = cart

        return purchaseRequest
    }

    private fun setUpPurchaseRequest(id: Long) {
        val employee = Employee(name = "acceptPurchaseRequest.test.admin")
        employee.id = 0L
        employee.user = testAdministratorUser

        every {
            employeeRepository.findByUserId(testAdministratorUser.id!!)
        } returns employee

        val adminEmployee = AdminEmployee()
        adminEmployee.employee = employee

        every {
            administratorEmployeeRepository.findByEmployeeId(employee.id!!)
        } returns adminEmployee

        val purchaseRequest = preparePurchaseRequest()

        every {
            purchaseRequestRepository.findById(id)
        } returns Optional.of(purchaseRequest)

        every {
            purchaseRequestRepository.save(match {
                it.user?.role == purchaseRequest.user?.role &&
                        it.request?.status == purchaseRequest.request?.status &&
                        it.cart == purchaseRequest.cart
            })
        } returns purchaseRequest

        val client = Client(
            name = "acceptPurchaseRequest.test.client",
            email = "acceptPurchaseRequest.test.admin@test.test"
        )

        every {
            clientRepository.findByUserId(purchaseRequest.user!!.id!!)
        } returns client

        var cartContents = ""
        purchaseRequest.cart!!.items!!.forEach {
            cartContents += "\n${it.name()} - ${it.quantity} pieces"
        }

        every {
            emailSender.sendMessage(
                client.email,
                "Request id#${purchaseRequest.id} accepted",
                "Your purchase request (id #${purchaseRequest.id}) has been accepted.\n" +
                        "Cart items are as follows:\n$cartContents\n\n" +
                        "Please contact us at +1-800-FUCK-OFF for payment and delivery discussions."
            )
        } returns Unit

        every {
            emailSender.sendMessage(
                client.email,
                "Request id#${purchaseRequest.id} rejected",
                "Your purchase request (id #${purchaseRequest.id}) has been rejected.\n" +
                        "Unretrieved cart:\n$cartContents\n\n" +
                        "If you are unsatisfied with this decision, please make a new request or contact us at +1-800-FUCK-OFF."
            )
        } returns Unit
    }
}