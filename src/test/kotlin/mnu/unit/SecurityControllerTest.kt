package mnu.unit

import com.ninjasquad.springmockk.MockkBean
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import mnu.config.SecurityConfig
import mnu.controller.SecurityController
import mnu.model.entity.*
import mnu.model.entity.employee.Employee
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

    @BeforeEach
    fun setUp() {
        every { mockPrincipal.name } returns testSecurityUser.login
        every { userRepository.findByLogin(testSecurityUser.login) } returns testSecurityUser
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @WithMockUser(value = "rogosec", roles = ["SECURITY"])
    @Test
    fun `securityEquipment OK`() {
        val employee = Employee(name = "equipment.test.security")
        employee.id = 1L
        employee.level = 5

        every {
            employeeRepository.findByUserId(testSecurityUser.id!!)
        } returns employee

        val security = SecurityEmployee()
        security.employee = employee

        every {
            securityEmployeeRepository.findByEmployeeId(employee.id!!)
        } returns security

        val request = Request()
        request.status = RequestStatus.PENDING

        val equipment = ChangeEquipmentRequest()
        equipment.request = request

        val equipments = listOf(equipment)

        every {
            changeEquipmentRequestRepository.findAllByEmployee(security)
        } returns equipments

        val weapons = ArrayList<Weapon>()

        every {
            weaponRepository.findAllByRequiredAccessLvlLessThanEqualAndQuantityGreaterThanOrderByIdAsc(
                security.employee!!.level!!, 0
            )
        } returns weapons

        val transports = ArrayList<Transport>()

        every {
            transportRepository.findAllByRequiredAccessLvlLessThanEqualAndQuantityGreaterThanOrderByIdAsc(
                security.employee!!.level!!, 0
            )
        } returns transports

        mockMvc.perform(
            MockMvcRequestBuilders.get("/sec/equipment").principal(mockPrincipal)
        ).andExpect(status().isOk)
            .andExpect(model().attribute("current_security", security))
            .andExpect(model().attribute("current_request", equipment))
            .andExpect(model().attribute("available_weapons", weapons))
            .andExpect(model().attribute("available_transport", transports))
            .andExpect(model().attribute("form", NewEquipmentForm()))
    }

    /*    @WithMockUser(value = "rogosec", roles = ["SECURITY"])
        @Test
        fun `requestNewEquipment OK`() {
            val employee = Employee(name = "equipment.test.security")
            employee.id = 1L
            employee.level = 5

            every {
                employeeRepository.findByUserId(testSecurityUser.id!!)
            } returns employee

            val security = SecurityEmployee()
            security.employee = employee

            every {
                securityEmployeeRepository.findByEmployeeId(employee.id!!)
            } returns security

            val equipment = ChangeEquipmentRequest()
            equipment.request = Request()

            val equipments = listOf(equipment)

            every {
                changeEquipmentRequestRepository.findAllByEmployee(security)
            } returns equipments

            val newEquipmentForm = NewEquipmentForm(weaponId = null, transportId = null)

            val weapon = Weapon()

            every {
                weaponRepository.findById(0L)
            } returns Optional.of(weapon)

            val transport = Transport()

            every {
                transportRepository.findById(0L)
            } returns Optional.of(transport)

            val request = Request().apply { this.status = RequestStatus.PENDING }
            request.id = null

            every {
                requestRepository.save(Request())
            } returns request

            val changeEquipmentRequest = ChangeEquipmentRequest()

            every {
                changeEquipmentRequestRepository.save(changeEquipmentRequest)
            } returns changeEquipmentRequest

            mockMvc.perform(
                MockMvcRequestBuilders.post("/sec/equipment")
                    .sessionAttr("form", newEquipmentForm)
                    .principal(mockPrincipal)
            ).andExpect(status().isFound)
                .andExpect(flash().attribute("status", "Request sent. Wait for supervisor's decision."))
        }*/

    @WithMockUser(value = "rogosec", roles = ["SECURITY"])
    @Test
    fun `acceptIncidentParticipation OK`() {
        val id = 1L

        val employee = Employee(name = "equipment.test.security")
        employee.id = 1L
        employee.level = 5

        every {
            employeeRepository.findByUserId(testSecurityUser.id!!)
        } returns employee

        val security = SecurityEmployee()
        security.employee = employee

        every {
            securityEmployeeRepository.findByEmployeeId(employee.id!!)
        } returns security

        val districtIncident = DistrictIncident()
        districtIncident.assistants = mutableListOf(SecurityEmployee())

        every {
            districtIncidentRepository.findById(id)
        } returns Optional.of(districtIncident)

        val districtIncidents = listOf(districtIncident)

        every {
            districtIncidentRepository.findAllByAvailablePlacesGreaterThanAndLevelFromLessThanEqualAndLevelToGreaterThanEqual(
                0, employee.level!!, employee.level!!
            )
        } returns districtIncidents

        districtIncident.availablePlaces = 10L

        every {
            districtIncidentRepository.save(districtIncident)
        } returns districtIncident

        mockMvc.perform(
            MockMvcRequestBuilders.post("/sec/incident/{id}", id).principal(mockPrincipal)
        ).andExpect(status().isFound)
            .andExpect(flash().attribute("status", "You were appointed to the incident."))
    }

    /*@WithMockUser(value = "rogosec", roles = ["SECURITY"])
    @Test
    fun `addSearchReport OK`() {
        val newSearchForm = NewSearchForm(
            isNew = "2",
            incidentId = "100",
            result = "addSearchReport.test.security.result",
            weaponType = "pistol",
            weaponLevel = "5",
            weaponQuantity2 = "100",
            weaponPrice = "100"
        )

        val employee = Employee(name = "equipment.test.security")
        employee.id = 1000L

        every {
            employeeRepository.findByUserId(testSecurityUser.id!!)
        } returns employee

        val security = SecurityEmployee()
        security.employee = employee

        every {
            securityEmployeeRepository.findByEmployeeId(employee.id!!)
        } returns security

        val districtIncident = DistrictIncident()
        districtIncident.assistants = mutableListOf(security)

        every {
            districtIncidentRepository.findById(100L)
        } returns Optional.of(districtIncident)

        every {
            districtIncidentRepository.save(districtIncident.apply {
                this.description += "\n\n${newSearchForm.result}"
                this.dangerLevel = 0
                this.levelFrom = 0
                this.levelTo = 0
            })
        } returns districtIncident

        val newRequest = Request().apply { this.status = RequestStatus.PENDING }

        val newWeaponRequest = NewWeaponRequest(
            newSearchForm.weaponName,
            WeaponType.PISTOL,
            newSearchForm.weaponDescription,
            newSearchForm.weaponQuantity2.toLong(),
            newSearchForm.weaponLevel.toInt(),
            newSearchForm.weaponPrice.toDouble(),
            testSecurityUser
        )
        newWeaponRequest.request = newRequest

        every {
            newWeaponRequestRepository.save(newWeaponRequest)
        } returns newWeaponRequest

        mockMvc.perform(
            MockMvcRequestBuilders.post("/sec/report")
                .sessionAttr("form", newSearchForm)
                .param("incidentId", "100")
                .param("isNew", "2")
                .param("weaponType", "pistol")
                .param("weaponLevel", "5")
                .param("weaponQuantity2", "100")
                .param("weaponPrice", "10000")
                .principal(mockPrincipal)
        ).andExpect(status().isFound)
            .andExpect(flash().attribute("error", "Report submitted. Await for supervisor's decision."))
    }*/
}