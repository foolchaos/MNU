package mnu.unit

import com.ninjasquad.springmockk.MockkBean
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import mnu.config.SecurityConfig
import mnu.controller.SecurityController
import mnu.model.entity.*
import mnu.model.entity.employee.Employee
import mnu.model.entity.employee.PersonStatus
import mnu.model.entity.employee.SecurityEmployee
import mnu.model.entity.request.ChangeEquipmentRequest
import mnu.model.entity.request.NewWeaponRequest
import mnu.model.entity.request.Request
import mnu.model.entity.request.RequestStatus
import mnu.model.form.NewEquipmentForm
import mnu.model.form.NewSearchForm
import mnu.repository.DistrictIncidentRepository
import mnu.repository.TransportRepository
import mnu.repository.UserRepository
import mnu.repository.WeaponRepository
import mnu.repository.employee.EmployeeRepository
import mnu.repository.employee.SecurityEmployeeRepository
import mnu.repository.request.ChangeEquipmentRequestRepository
import mnu.repository.request.NewWeaponRequestRepository
import mnu.repository.request.RequestRepository
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

@WebMvcTest(controllers = [SecurityController::class])
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = [SecurityConfig::class])
@Import(SecurityController::class)
class SecurityControllerTest(@Autowired var mockMvc: MockMvc) {
    @MockkBean
    lateinit var dataSource: DataSource

    @MockkBean
    lateinit var securityEmployeeRepository: SecurityEmployeeRepository

    @MockkBean
    lateinit var weaponRepository: WeaponRepository

    @MockkBean
    lateinit var newWeaponRequestRepository: NewWeaponRequestRepository

    @MockkBean
    lateinit var transportRepository: TransportRepository

    @MockkBean
    lateinit var requestRepository: RequestRepository

    @MockkBean
    lateinit var changeEquipmentRequestRepository: ChangeEquipmentRequestRepository

    @MockkBean
    lateinit var districtIncidentRepository: DistrictIncidentRepository

    @MockkBean
    lateinit var employeeRepository: EmployeeRepository

    @MockkBean
    lateinit var userRepository: UserRepository

    private val mockPrincipal: Principal = mockk()

    private val testSecurityUser: User = User(login = "rogosec", role = Role.SECURITY).apply { id = 313 }
    private val testSecurityEmployee: Employee =
        Employee(name = "rogosec", level = 5, salary = 313, position = "rogosec").apply {
            id = 313
            user = testSecurityUser
            status = PersonStatus.WORKING
        }
    private val testSecuritySecurityEmployee: SecurityEmployee =
        SecurityEmployee().apply { employee = testSecurityEmployee }
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
        name = "car",
        type = TransportType.LAND,
        description = "cool car",
        price = 2.0,
        requiredAccessLvl = 4
    ).apply {
        id = 313
        quantity = 5
    }

    @BeforeEach
    fun setUp() {
        every { mockPrincipal.name } returns testSecurityUser.login
        every { userRepository.findByLogin(testSecurityUser.login) } returns testSecurityUser
        every { employeeRepository.findByUserId(testSecurityUser.id!!) } returns testSecurityEmployee
        every { securityEmployeeRepository.findByEmployeeId(testSecurityEmployee.id!!) } returns testSecuritySecurityEmployee
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @WithMockUser(value = "rogosec", roles = ["SECURITY"])
    @Test
    fun `Check that security equipment GET returns 200 OK and lists of empty data`() {
        val testRequest = Request().apply { status = RequestStatus.PENDING }
        val testEquipmentRequest = ChangeEquipmentRequest().apply { request = testRequest }

        val equipmentRequests = listOf(testEquipmentRequest)
        every {
            changeEquipmentRequestRepository.findAllByEmployee(testSecuritySecurityEmployee)
        } returns equipmentRequests

        val weapons = ArrayList<Weapon>()
        val transports = ArrayList<Transport>()

        every {
            weaponRepository.findAllByRequiredAccessLvlLessThanEqualAndQuantityGreaterThanOrderByIdAsc(
                testSecuritySecurityEmployee.employee!!.level!!, 0
            )
        } returns weapons
        every {
            transportRepository.findAllByRequiredAccessLvlLessThanEqualAndQuantityGreaterThanOrderByIdAsc(
                testSecuritySecurityEmployee.employee!!.level!!, 0
            )
        } returns transports

        mockMvc.perform(
            MockMvcRequestBuilders.get("/sec/equipment").principal(mockPrincipal)
        ).andExpect(status().isOk)
            .andExpect(model().attribute("current_security", testSecuritySecurityEmployee))
            .andExpect(model().attribute("current_request", testEquipmentRequest))
            .andExpect(model().attribute("available_weapons", weapons))
            .andExpect(model().attribute("available_transport", transports))
            .andExpect(model().attribute("form", NewEquipmentForm()))
    }

    @WithMockUser(value = "rogosec", roles = ["SECURITY"])
    @Test
    fun `Check that valid change equipment request POST returns 302 and returns status`() {
        val equipmentRequests = mutableListOf<ChangeEquipmentRequest>()
        every {
            changeEquipmentRequestRepository.findAllByEmployee(testSecuritySecurityEmployee)
        } returns equipmentRequests

        every {
            weaponRepository.findById(testWeapon.id!!)
        } returns Optional.of(testWeapon)
        every {
            transportRepository.findById(testTransport.id!!)
        } returns Optional.of(testTransport)

        val testRequest = Request().apply {
            id = 313
            status = RequestStatus.PENDING
        }
        val testEquipmentRequest = ChangeEquipmentRequest().apply {
            id = 313
            request = testRequest
            weapon = testWeapon
            transport = testTransport
            employee = testSecuritySecurityEmployee
        }

        every {
            requestRepository.save(match {
                it.status == testRequest.status
            })
        } returns testRequest
        every {
            changeEquipmentRequestRepository.save(match {
                it.weapon == testEquipmentRequest.weapon
                        && it.transport == testEquipmentRequest.transport
                        && it.employee == testEquipmentRequest.employee
            })
        } returns testEquipmentRequest

        val newEquipmentForm = NewEquipmentForm(weaponId = testWeapon.id, transportId = testTransport.id)
        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/sec/equipment")
                .principal(mockPrincipal)
                .param("weaponId", newEquipmentForm.weaponId.toString())
                .param("transportId", newEquipmentForm.transportId.toString())
        ).andExpect(status().isFound)
            .andExpect(flash().attribute("status", "Request sent. Wait for supervisor's decision."))
    }

    @WithMockUser(value = "rogosec", roles = ["SECURITY"])
    @Test
    fun `Check that applying for incident resolution POST returns 302 and status`() {
        val districtIncident = DistrictIncident().apply {
            id = 313
            assistants = mutableListOf(SecurityEmployee())
            levelFrom = 1
            levelTo = 10
        }
        every {
            districtIncidentRepository.findById(districtIncident.id!!)
        } returns Optional.of(districtIncident)

        val districtIncidents = listOf(districtIncident)
        every {
            districtIncidentRepository.findAllByAvailablePlacesGreaterThanAndLevelFromLessThanEqualAndLevelToGreaterThanEqual(
                0, testSecurityEmployee.level!!, testSecurityEmployee.level!!
            )
        } returns districtIncidents

        districtIncident.availablePlaces = 10L

        every {
            districtIncidentRepository.save(districtIncident)
        } returns districtIncident

        mockMvc.perform(
            MockMvcRequestBuilders.post("/sec/incident/{id}", districtIncident.id)
                .principal(mockPrincipal)
        ).andExpect(status().isFound)
            .andExpect(flash().attribute("status", "You were appointed to the incident."))
    }

    @WithMockUser(value = "rogosec", roles = ["SECURITY"])
    @Test
    fun `Check that adding report for an incident POST returns 302 and status`() {
        val newSearchForm = NewSearchForm(
            isNew = "2",
            incidentId = "313",
            result = "testResult",
            weaponType = "pistol",
            weaponLevel = "5",
            weaponQuantity2 = "100",
            weaponPrice = "100.0"
        )

        val districtIncident = DistrictIncident(description = "everything is bad").apply {
            id = 313
            assistants = mutableListOf(testSecuritySecurityEmployee)
        }

        every {
            districtIncidentRepository.findById(districtIncident.id!!)
        } returns Optional.of(districtIncident)

        districtIncident.apply {
            description += "\n\n${newSearchForm.result}"
            dangerLevel = 0
            levelFrom = 0
            levelTo = 0
        }

        every {
            districtIncidentRepository.save(match {
                it.assistants == districtIncident.assistants
                        && it.description == districtIncident.description
                        && it.dangerLevel == districtIncident.dangerLevel
                        && it.levelFrom == districtIncident.levelFrom
                        && it.levelTo == districtIncident.levelTo
            })
        } returns districtIncident

        val newRequest = Request().apply { this.status = RequestStatus.PENDING }
        val newWeaponRequest = NewWeaponRequest(
            name = newSearchForm.weaponName,
            type = WeaponType.PISTOL,
            description = newSearchForm.weaponDescription,
            quantity = newSearchForm.weaponQuantity2.toLong(),
            requiredAccessLvl =  newSearchForm.weaponLevel.toInt(),
            price = newSearchForm.weaponPrice.toDouble(),
            user = testSecurityUser
        ).apply { request = newRequest }

        every {
            newWeaponRequestRepository.save(match {
                it.name == newWeaponRequest.name
                        && it.type == newWeaponRequest.type
                        && it.description == newWeaponRequest.description
                        && it.quantity == newWeaponRequest.quantity
                        && it.requiredAccessLvl == newWeaponRequest.requiredAccessLvl
                        && it.price == newWeaponRequest.price
                        && it.user == newWeaponRequest.user
            })
        } returns newWeaponRequest

        mockMvc.perform(
            MockMvcRequestBuilders.post("/sec/report")
                .param("incidentId", newSearchForm.incidentId)
                .param("isNew", newSearchForm.isNew)
                .param("weaponType", newSearchForm.weaponType)
                .param("weaponLevel", newSearchForm.weaponLevel)
                .param("weaponQuantity2", newSearchForm.weaponQuantity2)
                .param("weaponPrice", newSearchForm.weaponPrice)
                .principal(mockPrincipal)
        ).andExpect(status().isFound)
            .andExpect(flash().attribute("status", "Report submitted. Await for supervisor's decision."))
    }
}