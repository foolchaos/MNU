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
import mnu.model.entity.shop.ShoppingCartItem
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.security.Principal
import java.time.LocalDateTime
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
    lateinit var cashRewardRepository: CashRewardRepository

    @MockkBean
    lateinit var prawnRepository: PrawnRepository

    @MockkBean
    lateinit var clientRepository: ClientRepository

    @MockkBean
    lateinit var userRepository: UserRepository

    private val mockPrincipal: Principal = mockk()

    private val testAdministratorUser: User = User(login = "rogoadmin", role = Role.ADMIN).apply { id = 313 }
    private val testAdministratorEmployee: Employee =
        Employee(name = "rogoadmin", level = 5, salary = 313, position = "rogoadmin").apply {
            id = 313
            user = testAdministratorUser
            status = PersonStatus.WORKING
        }
    private val testAdministratorAdminEmployee: AdminEmployee =
        AdminEmployee().apply {
            id = 313
            employee = testAdministratorEmployee
        }

    private val testPrawnUser: User =
        User(login = "test_prawn", password = "qwerty", role = Role.PRAWN).apply {
            id = 323
        }
    private val testPrawnPrawn: Prawn =
        Prawn(
            name = "test prawn"
        ).apply {
            id = 323
            karma = 10000
        }

    private val testScientistUser: User = User(login = "rogosci", role = Role.SCIENTIST).apply { id = 333 }
    private val testScientistEmployee: Employee =
        Employee(name = "rogosci", level = 5, salary = 313, position = "rogosci").apply {
            id = 333
            user = testScientistUser
            status = PersonStatus.WORKING
        }
    private val testScientistScientistEmployee: ScientistEmployee =
        ScientistEmployee().apply {
            id = 333
            employee = testScientistEmployee
        }
    val testExperiment = Experiment(
        title = "testExperiment",
        description = "testDescription",
        examinator = testScientistScientistEmployee
    ).apply {
        id = 333
        status = ExperimentStatus.PENDING
    }

    private val testClientUser: User = User(login = "rogoclient", role = Role.CUSTOMER).apply { id = 343 }
    private val testClientClient: Client = Client(
        name = "rogoclient", email = "rogoclient@rogoclient.com", type = ClientType.CUSTOMER
    ).apply {
        id = 343
        user = testClientUser
    }

    private val testManufacturerUser: User = User(login = "rogomanuf", role = Role.MANUFACTURER).apply {
        id = 353
    }
    private val testManufacturerClient: Client =
        Client(name = "rogomanuf", email = "rogomanuf@rogomanuf.com", type = ClientType.MANUFACTURER).apply {
            id = 353
            user = testManufacturerUser
        }

    private val testDistrictHouse: DistrictHouse = DistrictHouse(1,1)
    private val testDistrictIncident: DistrictIncident = DistrictIncident(
        dangerLevel = 1,
        house = testDistrictHouse,
        description = "everything is bad"
    )

    private val testWeapon: Weapon = Weapon(
        name = "knife",
        type = WeaponType.MELEE,
        description = "kool knife",
        price = 2.0,
        requiredAccessLvl = 4
    ).apply {
        id = 313
        quantity = 5
    }
    private val testTransport: Transport = Transport(
        name = "testTransport",
        type = TransportType.AIR,
        description = "tool transport",
        price = 1.0,
        requiredAccessLvl = 1
    ).apply {
        id = 313
        quantity = 5
    }
    private val testRequest: Request = Request().apply { status = RequestStatus.PENDING }
    private val testShoppingCartItemPrawn: ShoppingCartItem =
        ShoppingCartItem(weapon = testWeapon, quantity = 5).apply { id = 313 }
    private val testShoppingCartPrawn: ShoppingCart =
        ShoppingCart(user = testPrawnUser, dateOfCreation = LocalDateTime.now()).apply {
            id = 313
            items = mutableListOf(testShoppingCartItemPrawn)
        }
    private val testPurchaseRequestPrawn: PurchaseRequest =
        PurchaseRequest(user = testPrawnUser, cart = testShoppingCartPrawn).apply {
            id = 313
            request = testRequest
        }

    private val testShoppingCartItemClient: ShoppingCartItem =
        ShoppingCartItem(weapon = testWeapon, quantity = 5).apply { id = 323 }
    private val testShoppingCartClient: ShoppingCart =
        ShoppingCart(user = testClientUser, dateOfCreation = LocalDateTime.now()).apply {
            id = 323
            items = mutableListOf(testShoppingCartItemClient)
        }
    private val testPurchaseRequestClient: PurchaseRequest =
        PurchaseRequest(user = testClientUser, cart = testShoppingCartClient).apply {
            id = 323
            request = testRequest
        }

    private val testVacancyExisting: Vacancy = Vacancy(title = "rogovac", salary = 100.0, requiredKarma = 1, workHoursPerWeek = 1).apply {
        id = 313
        vacantPlaces = 10
    }
    private val testVacancyNew: Vacancy = Vacancy(title = "rogonew", salary = 50.0, requiredKarma = 5, workHoursPerWeek = 100).apply {
        vacantPlaces = 10
    }
    private val testNewVacancyRequest = NewVacancyRequest(
        title = testVacancyNew.title,
        salary = testVacancyNew.salary,
        requiredKarma = testVacancyNew.requiredKarma,
        workHoursPerWeek = testVacancyNew.workHoursPerWeek,
        vacantPlaces = testVacancyNew.vacantPlaces,
        client = testManufacturerClient
    ).apply { request = testRequest }
    private val testVacancyApplicationRequest: VacancyApplicationRequest = VacancyApplicationRequest(
        prawn = testPrawnPrawn,
        vacancy = testVacancyExisting
    ).apply { request = testRequest }

    private val testNewWeaponRequest: NewWeaponRequest = NewWeaponRequest(
        name = testWeapon.name,
        type = testWeapon.type,
        description = testWeapon.description,
        quantity = testWeapon.quantity,
        requiredAccessLvl = testWeapon.requiredAccessLvl,
        price = testWeapon.price,
        user = testManufacturerUser
    ).apply { request = testRequest }
    private val testNewTransportRequest: NewTransportRequest = NewTransportRequest(
        name = testTransport.name,
        type = testTransport.type,
        description = testTransport.description,
        quantity = testTransport.quantity,
        requiredAccessLvl = testTransport.requiredAccessLvl,
        price = testTransport.price,
        client = testManufacturerClient
    ).apply { request = testRequest }

    @BeforeEach
    fun setUp() {
        every { mockPrincipal.name } returns testAdministratorUser.login
        every { userRepository.findByLogin(testAdministratorUser.login) } returns testAdministratorUser
        every { employeeRepository.findByUserId(testAdministratorUser.id!!) } returns testAdministratorEmployee
        every { administratorEmployeeRepository.findByEmployeeId(testAdministratorEmployee.id!!) } returns testAdministratorAdminEmployee
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @WithMockUser(value = "rogoadmin", roles = ["ADMIN"])
    @Test
    fun `Check that getting every available request for resolution returns 200 and a total count of each such requests`() {
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
            MockMvcRequestBuilders.get("/admin/main")
                .principal(mockPrincipal)
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
    fun `Check that getting a list of experiment requests returns 200 and the list itself`() {
        val experiments = arrayListOf(testExperiment)

        every {
            experimentRepository.findAllByStatus(ExperimentStatus.PENDING)
        } returns experiments

        mockMvc.perform(
            MockMvcRequestBuilders.get("/admin/experiments")
                .principal(mockPrincipal)
        ).andExpect(status().isOk)
            .andExpect(model().attribute("experiments", experiments))
    }

    @WithMockUser(value = "rogoadmin", roles = ["ADMIN"])
    @Test
    fun `Check that getting a list of available vacancies returns 200 and the list itself`() {
        val experiments = arrayListOf(testExperiment)

        every {
            experimentRepository.findAllByStatus(ExperimentStatus.PENDING)
        } returns experiments

        mockMvc.perform(
            MockMvcRequestBuilders.get("/admin/experiments")
                .principal(mockPrincipal)
        ).andExpect(status().isOk)
            .andExpect(model().attribute("experiments", experiments))
    }

    @WithMockUser(value = "rogoadmin", roles = ["ADMIN"])
    @Test
    fun `Check that getting a list of district incidents returns 200 and the list itself`() {
        val incidents = arrayListOf(testDistrictIncident)

        every {
            districtIncidentRepository.findAllByLevelToAndDangerLevelGreaterThan(0, 0)
        } returns incidents

        mockMvc.perform(
            MockMvcRequestBuilders.get("/admin/district")
                .principal(mockPrincipal)
        ).andExpect(status().isOk)
            .andExpect(model().attribute("current_incidents", incidents))
            .andExpect(model().attribute("form", AppointResolversForm()))
    }

    @WithMockUser(value = "rogoadmin", roles = ["ADMIN"])
    @Test
    fun `Check that getting a list of purchase requests returns 200 and the list itself`() {
        val purchaseRequests = arrayListOf(testPurchaseRequestPrawn)

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
    fun `Check that getting a list of employees returns 200 and the list itself`() {
        val q = "admin"

        every {
            employeeRepository.findAllByNameIgnoreCaseContainingOrderByIdAsc(q)
        } returns listOf(testAdministratorEmployee)

        every {
            employeeRepository.findAllByOrderByIdAsc()
        } returns listOf(testAdministratorEmployee, testScientistEmployee)

        mockMvc.perform(
            MockMvcRequestBuilders.get("/admin/employee")
                .param("q", q)
                .principal(mockPrincipal)
        ).andExpect(status().isOk)
            .andExpect(model().attribute("employees", listOf(testAdministratorEmployee)))
            .andExpect(model().attribute("form_add", EmployeeRegistrationForm()))
            .andExpect(model().attribute("form_edit", EmployeeEditForm()))
            .andExpect(model().attribute("form_reward", CashRewardForm()))
    }

    @WithMockUser(value = "rogoadmin", roles = ["ADMIN"])
    @Test
    fun `Check that accepting experiment request from a scientist returns 302 and a status`() {
        prepareExperiment()

        mockMvc.perform(
            MockMvcRequestBuilders.post("/admin/acceptExperiment/{id}", testExperiment.id)
                .principal(mockPrincipal)
        ).andExpect(status().isFound)
            .andExpect(flash().attribute("status", "Request accepted."))
    }

    @WithMockUser(value = "rogoadmin", roles = ["ADMIN"])
    @Test
    fun `Check that rejecting experiment request from a scientist returns 302 and a status`() {
        prepareExperiment()

        mockMvc.perform(
            MockMvcRequestBuilders.post("/admin/rejectExperiment/{id}", testExperiment.id)
                .principal(mockPrincipal)
        ).andExpect(status().isFound)
            .andExpect(flash().attribute("status", "Request rejected."))
    }

    @WithMockUser(value = "rogoadmin", roles = ["ADMIN"])
    @Test
    fun `Check that accepting purchase request from a client returns 302 and a status`() {
        setUpPurchaseRequest()

        mockMvc.perform(
            MockMvcRequestBuilders.post("/admin/acceptPurchaseRequest/{id}", testPurchaseRequestClient.id)
                .principal(mockPrincipal)
        ).andExpect(status().isFound)
            .andExpect(flash().attribute("status", "Request accepted."))
    }

    @WithMockUser(value = "rogoadmin", roles = ["ADMIN"])
    @Test
    fun `Check that rejecting purchase request from a client returns 302 and a status`() {
        setUpPurchaseRequest()

        mockMvc.perform(
            MockMvcRequestBuilders.post("/admin/rejectPurchaseRequest/{id}", testPurchaseRequestClient.id)
                .principal(mockPrincipal)
        ).andExpect(status().isFound)
            .andExpect(flash().attribute("status", "Request rejected."))
    }

    @WithMockUser(value = "rogoadmin", roles = ["ADMIN"])
    @Test
    fun `Check that registering an employee returns 302 and a status`() {
        val employeeRegistrationForm = EmployeeRegistrationForm(
            username = "registerEmployee.test.form.username",
            password = "registerEmployee.test.from.password",
            type = "administrator",
            name = "rogotestovich",
            position = "rogotesting",
            salary = "100500",
            level = "10"
        )

        val testUser = User(login = employeeRegistrationForm.username, role = Role.ADMIN)
        val testEmployee = Employee(
            name = employeeRegistrationForm.name,
            position = employeeRegistrationForm.position,
            salary = employeeRegistrationForm.salary.toLong(),
            level = employeeRegistrationForm.level.toInt()
        ).apply {
            user = testUser
            status = PersonStatus.WORKING
        }

        every {
            userRepository.findByLogin(employeeRegistrationForm.username)
        } returns null

        every {
            userRepository.save(match {
                it.login == testUser.login
                        && it.role == testUser.role
            })
        } returns testUser

        every {
            employeeRepository.save(match {
                it.name == testEmployee.name
                        && it.position == testEmployee.position
                        && it.salary == testEmployee.salary
                        && it.level == testEmployee.level
            })
        } returns testEmployee

        val adminEmployee = AdminEmployee().apply { employee = testEmployee }
        every {
            administratorEmployeeRepository.save(any())
        } returns adminEmployee

        mockMvc.perform(
            MockMvcRequestBuilders.post("/admin/registerEmployee")
                .param("username", employeeRegistrationForm.username)
                .param("password", employeeRegistrationForm.password)
                .param("name", employeeRegistrationForm.name)
                .param("type", employeeRegistrationForm.type)
                .param("position", employeeRegistrationForm.position)
                .param("salary", employeeRegistrationForm.salary)
                .param("level", employeeRegistrationForm.level)
                .principal(mockPrincipal)
        ).andExpect(status().isFound)
            .andExpect(flash().attribute("status", "Successfully registered a new employee."))
    }

    @WithMockUser(value = "rogoadmin", roles = ["ADMIN"])
    @Test
    fun `Check that editing an employee returns 302 and a status`() {
        val testUser = User(login = "registerEmployee.test.form.username", role = Role.ADMIN).apply { id = 414 }
        val testEmployee = Employee(
            name = "rogotestovich",
            position = "rogotesting",
            salary = 100500,
            level = 10
        ).apply {
            id = 414
            user = testUser
            status = PersonStatus.WORKING
        }
        val adminEmployee = AdminEmployee().apply {
            id = 414
            employee = testEmployee
        }

        val testEmployeeEditForm = EmployeeEditForm(
            id_edit = adminEmployee.id.toString(),
            name_edit = "rogoadmin_brother",
            position_edit = "new_position",
            salary_edit = "100",
            status_edit = "fired",
            level_edit = "1",
        )

        every {
            employeeRepository.findById(testEmployee.id!!)
        } returns Optional.of(testEmployee)

        every {
            employeeRepository.save(match {
                it.name == testEmployee.name
                        && it.position == testEmployee.position
                        && it.salary == testEmployee.salary
                        && it.level == testEmployee.level
            })
        } returns testEmployee

        mockMvc.perform(
            MockMvcRequestBuilders.post("/admin/editEmployee")
                .param("id_edit", testEmployeeEditForm.id_edit)
                .param("name_edit", testEmployeeEditForm.name_edit)
                .param("position_edit", testEmployeeEditForm.position_edit)
                .param("salary_edit", testEmployeeEditForm.salary_edit)
                .param("status_edit", testEmployeeEditForm.status_edit)
                .param("level_edit", testEmployeeEditForm.level_edit)
                .principal(mockPrincipal)
        ).andExpect(status().isFound)
            .andExpect(flash().attribute("status", "Successfully edited."))
    }

    @WithMockUser(value = "rogoadmin", roles = ["ADMIN"])
    @Test
    fun `Check that giving cash reward to an employee returns 302 and a status`() {
        val testUser = User(login = "registerEmployee.test.form.username", role = Role.ADMIN).apply { id = 414 }
        val testEmployee = Employee(
            name = "rogotestovich",
            position = "rogotesting",
            salary = 100500,
            level = 10
        ).apply {
            id = 414
            user = testUser
            status = PersonStatus.WORKING
        }

        every {
            employeeRepository.findById(testEmployee.id!!)
        } returns Optional.of(testEmployee)

        val testCashReward = CashReward(employee = testEmployee, reward = 1000)
        val testCashRewardForm = CashRewardForm(
            id_cash = testCashReward.employee!!.id.toString(),
            reward = testCashReward.reward.toString()
        )
        every {
            cashRewardRepository.save(match{
                it.employee!!.id == testCashReward.employee!!.id
                        && it.reward == testCashReward.reward
            })
        } returns testCashReward

        mockMvc.perform(
            MockMvcRequestBuilders.post("/admin/giveReward")
                .param("id_cash", testCashRewardForm.id_cash)
                .param("reward", testCashRewardForm.reward)
                .principal(mockPrincipal)
        ).andExpect(status().isFound)
            .andExpect(flash().attribute("status", "Reward given."))
    }

    @WithMockUser(value = "rogoadmin", roles = ["ADMIN"])
    @Test
    fun `Check that getting a list of vacancies returns 200 and the list itself`() {
        val vacancies = arrayListOf(testVacancyExisting)

        every {
            vacancyRepository.findAll()
        } returns vacancies

        every {
            requestRepository.findAllByStatus(RequestStatus.PENDING)
        } returns mutableListOf()

        every {
            newVacancyRequestRepository.findAll()
        } returns mutableListOf()

        mockMvc.perform(
            MockMvcRequestBuilders.get("/admin/vacancies").principal(mockPrincipal)
        ).andExpect(status().isOk)
            .andExpect(model().attribute("vacancies", vacancies))
    }

    @WithMockUser(value = "rogoadmin", roles = ["ADMIN"])
    @Test
    fun `Check that getting a list of vacancy requests returns 200 and the list itself`() {
        val vacancyRequests = arrayListOf(testNewVacancyRequest.apply{ id = 313 })

        every {
            newVacancyRequestRepository.findAll()
        } returns vacancyRequests

        mockMvc.perform(
            MockMvcRequestBuilders.get("/admin/vacancies/requests").principal(mockPrincipal)
        ).andExpect(status().isOk)
            .andExpect(model().attribute("requests", vacancyRequests))
    }

    @WithMockUser(value = "rogoadmin", roles = ["ADMIN"])
    @Test
    fun `Check that adding a new vacancy returns 302 and a status`() {
        every {
            vacancyRepository.save(match {
                it.title == testVacancyNew.title
                        && it.salary == testVacancyNew.salary
                        && it.requiredKarma == testVacancyNew.requiredKarma
                        && it.workHoursPerWeek == testVacancyNew.workHoursPerWeek
            })
        } returns testVacancyNew

        val testNewVacancyForm = NewVacancyForm(
            title = testVacancyNew.title,
            salary = testVacancyNew.salary.toString(),
            requiredKarma = testVacancyNew.requiredKarma.toString(),
            workHoursPerWeek = testVacancyNew.workHoursPerWeek.toString(),
            vacantPlaces = testVacancyNew.vacantPlaces.toString()
        )

        mockMvc.perform(
            MockMvcRequestBuilders.post("/admin/addVacancy")
                .param("title", testNewVacancyForm.title)
                .param("salary", testNewVacancyForm.salary)
                .param("requiredKarma", testNewVacancyForm.requiredKarma)
                .param("workHoursPerWeek", testNewVacancyForm.workHoursPerWeek)
                .param("vacantPlaces", testNewVacancyForm.vacantPlaces)
                .principal(mockPrincipal)
        ).andExpect(status().isFound)
            .andExpect(flash().attribute("form", testNewVacancyForm))
            .andExpect(flash().attribute("status", "Vacancy added."))
    }

    @WithMockUser(value = "rogoadmin", roles = ["ADMIN"])
    @Test
    fun `Check that accepting new vacancy request from a scientist returns 302 and a status`() {
        prepareNewVacancy()

        mockMvc.perform(
            MockMvcRequestBuilders.post("/admin/acceptNewVacancy/{id}", testNewVacancyRequest.apply { id = 313 }.id)
                .principal(mockPrincipal)
        ).andExpect(status().isFound)
            .andExpect(flash().attribute("status", "Request accepted."))
    }

    @WithMockUser(value = "rogoadmin", roles = ["ADMIN"])
    @Test
    fun `Check that rejecting new vacancy request from a scientist returns 302 and a status`() {
        prepareNewVacancy()

        mockMvc.perform(
            MockMvcRequestBuilders.post("/admin/rejectNewVacancy/{id}", testNewVacancyRequest.apply { id = 313 }.id)
                .principal(mockPrincipal)
        ).andExpect(status().isFound)
            .andExpect(flash().attribute("status", "Request rejected."))
    }

    @WithMockUser(value = "rogoadmin", roles = ["ADMIN"])
    @Test
    fun `Check that getting a list of job applications returns 200 and the list itself`() {
        val vacancyAppRequests = arrayListOf(testVacancyApplicationRequest.apply{ id = 313 })

        every {
            vacancyApplicationRequestRepository.findAll()
        } returns vacancyAppRequests

        mockMvc.perform(
            MockMvcRequestBuilders.get("/admin/jobApplications").principal(mockPrincipal)
        ).andExpect(status().isOk)
            .andExpect(model().attribute("requests", vacancyAppRequests))
    }

    @WithMockUser(value = "rogoadmin", roles = ["ADMIN"])
    @Test
    fun `Check that accepting new weapon request from a manufacturer returns 302 and a status`() {
        prepareNewWeapon()

        mockMvc.perform(
            MockMvcRequestBuilders.post("/admin/acceptNewWeapon/{id}", testNewWeaponRequest.apply{ id = 313 }.id)
                .principal(mockPrincipal)
        ).andExpect(status().isFound)
            .andExpect(flash().attribute("status", "Request accepted."))
    }

    @WithMockUser(value = "rogoadmin", roles = ["ADMIN"])
    @Test
    fun `Check that rejecting new weapon request from a manufacturer returns 302 and a status`() {
        prepareNewWeapon()

        mockMvc.perform(
            MockMvcRequestBuilders.post("/admin/rejectNewWeapon/{id}", testNewWeaponRequest.apply{ id = 313 }.id)
                .principal(mockPrincipal)
        ).andExpect(status().isFound)
            .andExpect(flash().attribute("status", "Request rejected."))
    }

    @WithMockUser(value = "rogoadmin", roles = ["ADMIN"])
    @Test
    fun `Check that accepting new transport request from a manufacturer returns 302 and a status`() {
        prepareNewTransport()

        mockMvc.perform(
            MockMvcRequestBuilders.post("/admin/acceptNewTransport/{id}", testNewTransportRequest.apply{ id = 313 }.id)
                .principal(mockPrincipal)
        ).andExpect(status().isFound)
            .andExpect(flash().attribute("status", "Request accepted."))
    }

    @WithMockUser(value = "rogoadmin", roles = ["ADMIN"])
    @Test
    fun `Check that rejecting new transport request from a manufacturer returns 302 and a status`() {
        prepareNewTransport()

        mockMvc.perform(
            MockMvcRequestBuilders.post("/admin/rejectNewTransport/{id}", testNewTransportRequest.apply{ id = 313 }.id)
                .principal(mockPrincipal)
        ).andExpect(status().isFound)
            .andExpect(flash().attribute("status", "Request rejected."))
    }

    @WithMockUser(value = "rogoadmin", roles = ["ADMIN"])
    @Test
    fun `Check that creating a task for incident resolution returns 302 and a status`() {
        testDistrictIncident.apply {
            id = 313
            availablePlaces = 2
            levelFrom = 1
            levelTo = 10
        }
        val testAppointResolversForm = AppointResolversForm(
            incidentId = testDistrictIncident.id.toString(),
            securityNeeded = testDistrictIncident.availablePlaces.toString(),
            levelFrom = testDistrictIncident.levelFrom.toString(),
            levelTo = testDistrictIncident.levelTo.toString()
        )

        every {
            districtIncidentRepository.findById(testDistrictIncident.id!!)
        } returns Optional.of(testDistrictIncident)

        every {
            districtIncidentRepository.save(match {
                it.id == testDistrictIncident.id
            })
        } returns testDistrictIncident

        mockMvc.perform(
            MockMvcRequestBuilders.post("/admin/appointResolvers")
                .principal(mockPrincipal)
                .param("incidentId", testAppointResolversForm.incidentId)
                .param("securityNeeded", testAppointResolversForm.incidentId)
                .param("levelFrom", testAppointResolversForm.incidentId)
                .param("levelTo", testAppointResolversForm.incidentId)
        ).andExpect(status().isFound)
            .andExpect(flash().attribute("status", "Success. All security employees will be notified of the occurred incident."))
    }

    private fun prepareExperiment() {
        every {
            experimentRepository.findById(testExperiment.id!!)
        } returns Optional.of(testExperiment)

        every {
            experimentRepository.save(match {
                it.status == testExperiment.status
            })
        } returns testExperiment
    }

    private fun prepareNewVacancy() {
        testNewVacancyRequest.apply { id = 313 }
        every {
            newVacancyRequestRepository.findById(testNewVacancyRequest.id!!)
        } returns Optional.of(testNewVacancyRequest)

        every {
            vacancyRepository.save(match {
                it.title == testNewVacancyRequest.title
                        && it.salary == testNewVacancyRequest.salary
                        && it.requiredKarma == testNewVacancyRequest.requiredKarma
                        && it.workHoursPerWeek == testNewVacancyRequest.workHoursPerWeek
                        && it.vacantPlaces == testNewVacancyRequest.vacantPlaces
            })
        } returns testVacancyNew

        every {
            newVacancyRequestRepository.save(match {
                it.request!!.status == testNewVacancyRequest.request!!.status
            })
        } returns testNewVacancyRequest
    }

    private fun prepareNewWeapon() {
        testNewWeaponRequest.apply { id = 313 }
        every {
            newWeaponRequestRepository.findById(testNewWeaponRequest.id!!)
        } returns Optional.of(testNewWeaponRequest)

        every {
            weaponRepository.save(match {
                it.name == testNewWeaponRequest.name
                        && it.type == testNewWeaponRequest.type
                        && it.description == testNewWeaponRequest.description
                        && it.price == testNewWeaponRequest.price
                        && it.requiredAccessLvl == testNewWeaponRequest.requiredAccessLvl
            })
        } returns testWeapon

        every {
            newWeaponRequestRepository.save(match {
                it.request!!.status == testNewWeaponRequest.request!!.status
            })
        } returns testNewWeaponRequest
    }

    private fun prepareNewTransport() {
        testNewTransportRequest.apply { id = 313 }
        every {
            newTransportRequestRepository.findById(testNewTransportRequest.id!!)
        } returns Optional.of(testNewTransportRequest)

        every {
            transportRepository.save(match {
                it.name == testNewTransportRequest.name
                        && it.type == testNewTransportRequest.type
                        && it.description == testNewTransportRequest.description
                        && it.price == testNewTransportRequest.price
                        && it.requiredAccessLvl == testNewTransportRequest.requiredAccessLvl
            })
        } returns testTransport

        every {
            newTransportRequestRepository.save(match {
                it.request!!.status == testNewTransportRequest.request!!.status
            })
        } returns testNewTransportRequest
    }

    private fun setUpPurchaseRequest(isPrawn: Boolean = false) {
        val purchaseRequest = if (isPrawn) testPurchaseRequestPrawn else testPurchaseRequestClient
        every {
            purchaseRequestRepository.findById(purchaseRequest.id!!)
        } returns Optional.of(purchaseRequest)

        every {
            purchaseRequestRepository.save(match {
                it.user?.role == purchaseRequest.user?.role &&
                        it.request?.status == purchaseRequest.request?.status &&
                        it.cart == purchaseRequest.cart
            })
        } returns purchaseRequest

        every {
            clientRepository.findByUserId(purchaseRequest.user!!.id!!)
        } returns testClientClient

        every {
            prawnRepository.findByUserId(purchaseRequest.user!!.id!!)
        } returns testPrawnPrawn

        every {
            weaponRepository.save(match{
                it.id == testWeapon.id
            })
        } returns testWeapon

        var cartContents = ""
        purchaseRequest.cart!!.items!!.forEach {
            cartContents += "\n${it.name()} - ${it.quantity} pieces"
        }

        every {
            emailSender.sendMessage(testClientClient.email, "Request id#${purchaseRequest.id} accepted", any())
        } returns Unit

        every {
            emailSender.sendMessage(testClientClient.email, "Request id#${purchaseRequest.id} rejected", any())
        } returns Unit
    }
}