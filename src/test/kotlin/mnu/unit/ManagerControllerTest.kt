package mnu.unit

import com.ninjasquad.springmockk.MockkBean
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import mnu.EmailSender
import mnu.config.SecurityConfig
import mnu.controller.ManagerController
import mnu.model.entity.*
import mnu.model.entity.employee.Employee
import mnu.model.entity.employee.ManagerEmployee
import mnu.model.entity.employee.PersonStatus
import mnu.model.entity.employee.SecurityEmployee
import mnu.model.entity.request.*
import mnu.model.entity.shop.ShoppingCart
import mnu.model.entity.shop.ShoppingCartItem
import mnu.model.entity.shop.ShoppingCartStatus
import mnu.model.form.PrawnRegistrationForm
import mnu.repository.*
import mnu.repository.employee.EmployeeRepository
import mnu.repository.employee.ManagerEmployeeRepository
import mnu.repository.employee.SecurityEmployeeRepository
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.security.Principal
import java.time.LocalDateTime
import java.util.*
import javax.sql.DataSource

@WebMvcTest(controllers = [ManagerController::class])
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = [SecurityConfig::class])
@Import(ManagerController::class)
class ManagerControllerTest(@Autowired var mockMvc: MockMvc) {

    @MockkBean
    lateinit var dataSource: DataSource

    @MockkBean
    lateinit var employeeRepository: EmployeeRepository

    @MockkBean
    lateinit var userRepository: UserRepository

    @MockkBean
    lateinit var prawnRepository: PrawnRepository

    @MockkBean
    lateinit var managerEmployeeRepository: ManagerEmployeeRepository

    @MockkBean
    lateinit var districtHouseRepository: DistrictHouseRepository

    @MockkBean
    lateinit var requestRepository: RequestRepository

    @MockkBean
    lateinit var weaponRepository: WeaponRepository

    @MockkBean
    lateinit var transportRepository: TransportRepository

    @MockkBean
    lateinit var purchaseRequestRepository: PurchaseRequestRepository

    @MockkBean
    lateinit var newWeaponRequestRepository: NewWeaponRequestRepository

    @MockkBean
    lateinit var vacancyApplicationRequestRepository: VacancyApplicationRequestRepository

    @MockkBean
    lateinit var vacancyRepository: VacancyRepository

    @MockkBean
    lateinit var changeEquipmentRequestRepository: ChangeEquipmentRequestRepository

    @MockkBean
    lateinit var securityEmployeeRepository: SecurityEmployeeRepository

    @MockkBean
    lateinit var emailSender: EmailSender

    private val mockPrincipal: Principal = mockk()

    private val testManagerUser: User = User(login = "rogomanager", role = Role.MANAGER).apply { id = 313 }
    private val testSecurityUser: User = User(login = "rogomanager", role = Role.MANAGER).apply { id = 313 }
    private val testManagerEmployee: Employee =
        Employee(name = "rogomanager", level = 5, salary = 313, position = "rogomanager").apply {
            id = 313
            user = testManagerUser
            status = PersonStatus.WORKING
        }
    private val testSecurityEmployee: Employee =
        Employee(name = "rogosec", level = 5, salary = 313, position = "rogosec").apply {
            id = 313
            user = testSecurityUser
            status = PersonStatus.WORKING
        }
    private val testSecuritySecurityEmployee: SecurityEmployee =
        SecurityEmployee().apply { id = 313
            employee = testSecurityEmployee
            weapon = testWeapon}
    private val testPrawnUser: User =
        User(login = "test_prawn", password = "qwerty", role = Role.PRAWN).apply {
            id = 313
        }
    private val testPrawnPrawn: Prawn =
        Prawn(name = "test prawn").apply {
            id = 313
        }
    private val testDistrictHouse: DistrictHouse = DistrictHouse(1,1)
    private val testManagerManagerEmployee: ManagerEmployee =
        ManagerEmployee().apply { id=313
            employee = testManagerEmployee }
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
    private val testRequest: Request = Request().apply { status=RequestStatus.PENDING }
    private val testShoppingCartItem: ShoppingCartItem =
        ShoppingCartItem(weapon = testWeapon, quantity = 5).apply { id = 3 }
    private val testShoppingCart: ShoppingCart =
        ShoppingCart(user = testPrawnUser, dateOfCreation = LocalDateTime.now()).apply { id = 3 }
    private val testPurchaseRequest: PurchaseRequest =
        PurchaseRequest(user = testPrawnUser, cart = testShoppingCart).apply {
            id = 313
            request = testRequest
        }
    private val testChangeEquipmentRequest: ChangeEquipmentRequest =
        ChangeEquipmentRequest(employee =  testSecuritySecurityEmployee, weapon = testWeapon).apply {
            id = 313
            request = testRequest
        }
    private val testVacancy: Vacancy =
        Vacancy().apply {
            id = 313
            vacantPlaces = 2
        }
    private val testNewWeaponRequest: NewWeaponRequest =
        NewWeaponRequest(user = testPrawnUser).apply {
            id = 313
            quantity = testWeapon.quantity
            requiredAccessLvl = testWeapon.requiredAccessLvl
            description = testWeapon.description
            name = testWeapon.name
            type = testWeapon.type
            request = testRequest
        }
    private val testVacancyApplicationRequest: VacancyApplicationRequest =
        VacancyApplicationRequest(prawn = testPrawnPrawn, vacancy = testVacancy).apply {
            id = 313
            request = testRequest
        }


    @BeforeEach
    fun setUp() {
        every { mockPrincipal.name } returns testManagerUser.login
        every { userRepository.findByLogin(testManagerUser.login) } returns testManagerUser
        every { employeeRepository.findByUserId(testManagerUser.id!!) } returns testManagerEmployee
        every { managerEmployeeRepository.findByEmployeeId(testManagerEmployee.id!!) } returns testManagerManagerEmployee
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @WithMockUser(value = "rogomanager", roles = ["MANAGER"])
    @Test
    fun `Check that register prawn POST returns 302`() {
        every { userRepository.findByLogin(testPrawnUser.login) } returns testManagerUser
        every {
            districtHouseRepository.getAllIds()!!
        } returns listOf(1)
        every {
            districtHouseRepository.findById(1)
        } returns Optional.of(testDistrictHouse)
        every {
            userRepository.save(match {
                it.role == Role.PRAWN
            })
        } returns testPrawnUser
        every {
            prawnRepository.save(match {
                it.name == "test prawn"
            })
        } returns testPrawnPrawn
        val newPrawnForm = PrawnRegistrationForm(username = "test_prawn", password = "qwerty", name = "test prawn")
        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/man/registerPrawn")
                .principal(mockPrincipal)
                .param("username", newPrawnForm.username)
                .param("password", newPrawnForm.password)
                .param("name", newPrawnForm.name)
        ).andExpect(MockMvcResultMatchers.status().isFound)
    }

    @WithMockUser(value = "rogomanager", roles = ["MANAGER"])
    @Test
    fun `Check that process purchase request POST returns 302`() {
        every { purchaseRequestRepository.findById(313) } returns Optional.of(testPurchaseRequest)
        every { prawnRepository.findByUserId(testPurchaseRequest.user!!.id!!) } returns testPrawnPrawn
        every { managerEmployeeRepository.findById(testManagerManagerEmployee.id!!) } returns Optional.of(testManagerManagerEmployee)
        every {
            purchaseRequestRepository.save(match {
                it.id == testPurchaseRequest.id
            })
        } returns testPurchaseRequest
        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/man/acceptPurchaseRequest/313")
                .principal(mockPrincipal)
        ).andExpect(MockMvcResultMatchers.status().isFound)
    }

    @WithMockUser(value = "rogomanager", roles = ["MANAGER"])
    @Test
    fun `Check that process new weapon request POST returns 302`() {
        every { newWeaponRequestRepository.findById(313) } returns Optional.of(testNewWeaponRequest)
        every {
            newWeaponRequestRepository.save(match {
                it.id == testNewWeaponRequest.id
            })
        } returns testNewWeaponRequest
        every {
            weaponRepository.save(match {
                it.name == testNewWeaponRequest.name
            })
        } returns testWeapon
        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/man/acceptNewWeapon/313")
                .principal(mockPrincipal)
        ).andExpect(MockMvcResultMatchers.status().isFound)
    }

    @WithMockUser(value = "rogomanager", roles = ["MANAGER"])
    @Test
    fun `Check that process equipment change request POST returns 302`() {
        every { changeEquipmentRequestRepository.findById(testChangeEquipmentRequest.id!!) } returns Optional.of(testChangeEquipmentRequest)
        every { weaponRepository.save(match{
            it.name == testWeapon.name
        }) } returns testWeapon
        every { securityEmployeeRepository.save(match{
            it.id == testSecuritySecurityEmployee.id
        }) } returns testSecuritySecurityEmployee
        every { changeEquipmentRequestRepository.save(match{
            it.id == testChangeEquipmentRequest.id
        }) } returns testChangeEquipmentRequest
        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/man/acceptNewEquipment/313")
                .principal(mockPrincipal)
        ).andExpect(MockMvcResultMatchers.status().isFound)
    }

    @WithMockUser(value = "rogomanager", roles = ["MANAGER"])
    @Test
    fun `Check that process prawn job application request POST returns 302`() {
        every { vacancyApplicationRequestRepository.findById(testVacancyApplicationRequest.id!!) } returns Optional.of(testVacancyApplicationRequest)
        every { vacancyApplicationRequestRepository.save(match{
            it.id == testVacancyApplicationRequest.id
        }) } returns testVacancyApplicationRequest
        every { vacancyRepository.save(match{
            it.id == testVacancy.id
        }) } returns testVacancy
        every { prawnRepository.save(match{
            it.id == testPrawnPrawn.id
        }) } returns testPrawnPrawn
        every { managerEmployeeRepository.findById(testManagerManagerEmployee.id!!) } returns Optional.of(testManagerManagerEmployee)
        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/man/acceptJobApplication/313")
                .principal(mockPrincipal)
        ).andExpect(MockMvcResultMatchers.status().isFound)
    }

    @WithMockUser(value = "rogomanager", roles = ["MANAGER"])
    @Test
    fun `Check that purchase requests list GET returns 200 OK`() {
        val validPurchRequests = ArrayList<PurchaseRequest>()
        every {  purchaseRequestRepository.findAll() } returns validPurchRequests
        mockMvc.perform(
            MockMvcRequestBuilders
                .get("/man/purchaseRequests")
                .principal(mockPrincipal)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.model().attribute("requests", validPurchRequests))
    }

    @WithMockUser(value = "rogomanager", roles = ["MANAGER"])
    @Test
    fun `Check that weapon requests list GET returns 200 OK`() {
        val weaponRequests = ArrayList<NewWeaponRequest>()
        every {  newWeaponRequestRepository.findAll() } returns weaponRequests
        mockMvc.perform(
            MockMvcRequestBuilders
                .get("/man/newWeapons")
                .principal(mockPrincipal)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.model().attribute("requests", weaponRequests))
    }

    @WithMockUser(value = "rogomanager", roles = ["MANAGER"])
    @Test
    fun `Check that prawn job application list GET returns 200 OK`() {
        val vacancyRequests = ArrayList<VacancyApplicationRequest>()
        every {  vacancyApplicationRequestRepository.findAll() } returns vacancyRequests
        mockMvc.perform(
            MockMvcRequestBuilders
                .get("/man/jobApplications")
                .principal(mockPrincipal)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.model().attribute("requests", vacancyRequests))
    }

    @WithMockUser(value = "rogomanager", roles = ["MANAGER"])
    @Test
    fun `Check that equipment change requests list GET returns 200 OK`() {
        val changeRequests = ArrayList<ChangeEquipmentRequest>()
        every { changeEquipmentRequestRepository.findAll() } returns changeRequests
        mockMvc.perform(
            MockMvcRequestBuilders
                .get("/man/newEquipment")
                .principal(mockPrincipal)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.model().attribute("requests", changeRequests))
    }

}