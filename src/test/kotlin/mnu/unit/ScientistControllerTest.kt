package mnu.unit

import com.ninjasquad.springmockk.MockkBean
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import mnu.config.SecurityConfig
import mnu.controller.ScientistController
import mnu.model.entity.*
import mnu.model.entity.employee.Employee
import mnu.model.entity.employee.PersonStatus
import mnu.model.entity.employee.ScientistEmployee
import mnu.model.entity.request.NewWeaponRequest
import mnu.model.entity.request.Request
import mnu.model.entity.request.RequestStatus
import mnu.model.form.NewExperimentForm
import mnu.model.form.NewReportForm
import mnu.repository.ExperimentRepository
import mnu.repository.UserRepository
import mnu.repository.WeaponRepository
import mnu.repository.employee.EmployeeRepository
import mnu.repository.employee.ScientistEmployeeRepository
import mnu.repository.request.NewWeaponRequestRepository
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
import java.time.LocalDateTime
import java.util.*
import kotlin.math.exp

@WebMvcTest(controllers = [ScientistController::class])
@AutoConfigureMockMvc(addFilters = false)
@Import(ScientistController::class)
class ScientistControllerTest(@Autowired var mockMvc: MockMvc) {
    @MockkBean
    lateinit var scientistEmployeeRepository: ScientistEmployeeRepository

    @MockkBean
    lateinit var experimentRepository: ExperimentRepository

    @MockkBean
    lateinit var weaponRepository: WeaponRepository

    @MockkBean
    lateinit var newWeaponRequestRepository: NewWeaponRequestRepository

    @MockkBean
    lateinit var employeeRepository: EmployeeRepository

    @MockkBean
    lateinit var userRepository: UserRepository

    private val mockPrincipal: Principal = mockk()

    private val testScientistUser: User = User(login = "rogosci", role = Role.SCIENTIST).apply { id = 313 }
    private val testScientistEmployee: Employee =
        Employee(name = "rogosci", level = 5, salary = 313, position = "rogosci").apply {
            id = 313
            user = testScientistUser
            status = PersonStatus.WORKING
        }
    private val testScientistScientistEmployee: ScientistEmployee =
        ScientistEmployee().apply {
            id = 313
            employee = testScientistEmployee
        }

    @BeforeEach
    fun setUp() {
        every { mockPrincipal.name } returns testScientistUser.login
        every { userRepository.findByLogin(testScientistUser.login) } returns testScientistUser
        every { employeeRepository.findByUserId(testScientistUser.id!!) } returns testScientistEmployee
        every { scientistEmployeeRepository.findByEmployeeId(testScientistEmployee.id!!) } returns testScientistScientistEmployee
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @WithMockUser(value = "rogosci", roles = ["SCIENTIST"])
    @Test
    fun `Check that fetching experiment list returns 200 OK and the list itself`() {
        val testExperiment = Experiment(
            title = "testExperiment",
            description = "testDescription",
            examinator = testScientistScientistEmployee
        ).apply { id = 313 }
        every {
            experimentRepository.findAllByExaminatorIdOrderByStatusAsc(testScientistScientistEmployee.id!!)
        } returns mutableListOf(testExperiment)

        mockMvc.perform(
            MockMvcRequestBuilders.get("/sci/main")
                .principal(mockPrincipal)
        ).andExpect(status().isOk)
            .andExpect(model().attribute("experiments", mutableListOf(testExperiment)))
    }

    @WithMockUser(value = "rogosci", roles = ["SCIENTIST"])
    @Test
    fun `Check that going to experiment form page returns 200 OK and the list of assistants`() {
        val testAssistantUser: User = User(login = "rogoass", role = Role.SCIENTIST).apply { id = 314 }
        val testAssistantEmployee: Employee =
            Employee(name = "rogoass", level = 2, salary = 1, position = "rogoass").apply {
                id = 314
                user = testAssistantUser
                status = PersonStatus.WORKING
            }
        val testAssistantScientistEmployeeInterfaceInstance = object : ScientistEmployeeRepository.Assistant {
            override val id: Long
                get() = testAssistantEmployee.id!!
            override val level: Int
                get() = testAssistantEmployee.level!!
            override val name: String
                get() = testAssistantEmployee.name
            override val position: String
                get() = testAssistantEmployee.position!!
        }

        every {
            scientistEmployeeRepository.getAssistants(testScientistEmployee.level!!)
        } returns mutableListOf(testAssistantScientistEmployeeInterfaceInstance)

        mockMvc.perform(
            MockMvcRequestBuilders.get("/sci/experiment")
                .principal(mockPrincipal)
        ).andExpect(status().isOk)
            .andExpect(model().attribute("assistants", mutableListOf(testAssistantScientistEmployeeInterfaceInstance)))
            .andExpect(model().attribute("form", NewExperimentForm()))
    }

    @WithMockUser(value = "rogosci", roles = ["SCIENTIST"])
    @Test
    fun `Check that requesting for an experiment returns 302 and status`() {
        val testAssistantUser: User = User(login = "rogoass", role = Role.SCIENTIST).apply { id = 314 }
        val testAssistantEmployee: Employee =
            Employee(name = "rogoass", level = 2, salary = 1, position = "rogoass").apply {
                id = 314
                user = testAssistantUser
                status = PersonStatus.WORKING
            }
        val testAssistantScientistEmployee = ScientistEmployee().apply {
            id = 314
            employee = testAssistantEmployee
        }

        every {
            scientistEmployeeRepository.findByEmployeeId(testAssistantScientistEmployee.id!!)
        } returns testAssistantScientistEmployee

        val testExperiment = Experiment(
            title = "testExperiment",
            description = "testDescription",
            examinator = testScientistScientistEmployee,
            assistant = testAssistantScientistEmployee
        ).apply { id = 313 }
        every {
            experimentRepository.save(match{
                it.title == testExperiment.title
                        && it.description == testExperiment.description
                        && it.examinator == testExperiment.examinator
                        && it.assistant == testExperiment.assistant
            })
        } returns testExperiment

        val testExperimentForm = NewExperimentForm(
            title = testExperiment.title,
            assistantId = testExperiment.assistant!!.id,
            description = testExperiment.description
        )

        mockMvc.perform(
            MockMvcRequestBuilders.post("/sci/experiment")
                .principal(mockPrincipal)
                .param("title", testExperimentForm.title)
                .param("assistantId", testExperimentForm.assistantId.toString())
                .param("description", testExperimentForm.description)
        ).andExpect(status().isFound)
            .andExpect(flash().attribute("status", "Request sent. Wait for supervisor's decision."))
    }

    @WithMockUser(value = "rogosci", roles = ["SCIENTIST"])
    @Test
    fun `Check that submitting a report with a new synthesized weapon for an experiment returns 302 and status`() {
        val testExperiment = Experiment(
            title = "testExperiment",
            description = "testDescription",
            examinator = testScientistScientistEmployee
        ).apply { id = 313 }
        every {
            experimentRepository.findById(testExperiment.id!!)
        } returns Optional.of(testExperiment)

        val testRequest = Request().apply {
            id = 313
            status = RequestStatus.PENDING
        }
        val testNewWeaponRequest = NewWeaponRequest(
            name = "testWeapon",
            type = WeaponType.ALIEN,
            description = "testDescription",
            price = 1000.0,
            quantity = 1,
            requiredAccessLvl = 10,
            user = testScientistUser
        ).apply { request = testRequest }
        every {
            newWeaponRequestRepository.save(match {
                it.name == testNewWeaponRequest.name
                        && it.type == testNewWeaponRequest.type
                        && it.description == testNewWeaponRequest.description
                        && it.price == testNewWeaponRequest.price
                        && it.requiredAccessLvl == testNewWeaponRequest.requiredAccessLvl
                        && it.quantity == testNewWeaponRequest.quantity
            })
        } returns testNewWeaponRequest

        val testReportForm = NewReportForm(
            experimentId = testExperiment.id.toString(),
            result = "testResult",
            isSynthesized = "2",
            weaponName = testNewWeaponRequest.name,
            weaponType = "alien",
            weaponDescription = testNewWeaponRequest.description,
            weaponLevel = testNewWeaponRequest.requiredAccessLvl.toString(),
            weaponQuantity2 = testNewWeaponRequest.quantity.toString(),
            weaponPrice = testNewWeaponRequest.price.toString()
        )

        testExperiment.apply {
            statusDate = LocalDateTime.now()
            status = ExperimentStatus.FINISHED
            result = testReportForm.result
        }
        every {
            experimentRepository.save(match {
                it.title == testExperiment.title
                        && it.description == testExperiment.description
                        && it.examinator == testExperiment.examinator
                        && it.status == testExperiment.status
                        && it.statusDate == testExperiment.statusDate
                        && it.result == testExperiment.result
            })
        } returns testExperiment

        mockMvc.perform(
            MockMvcRequestBuilders.post("/sci/report")
                .principal(mockPrincipal)
                .param("experimentId", testReportForm.experimentId)
                .param("result", testReportForm.result)
                .param("isSynthesized", testReportForm.isSynthesized)
                .param("weaponName", testReportForm.weaponName)
                .param("weaponType", testReportForm.weaponType)
                .param("weaponDescription", testReportForm.weaponDescription)
                .param("weaponLevel", testReportForm.weaponLevel)
                .param("weaponQuantity2", testReportForm.weaponQuantity2)
                .param("weaponPrice", testReportForm.weaponPrice)
        ).andExpect(status().isFound)
            .andExpect(flash().attribute("status", "Report submitted. Await for supervisor's decision."))
    }

    @WithMockUser(value = "rogosci", roles = ["SCIENTIST"])
    @Test
    fun `Check that submitting a report with an old weapon ID for an experiment returns 302 and status`() {
        val testExperiment = Experiment(
            title = "testExperiment",
            description = "testDescription",
            examinator = testScientistScientistEmployee
        ).apply { id = 313 }
        every {
            experimentRepository.findById(testExperiment.id!!)
        } returns Optional.of(testExperiment)

        val testNewWeapon = Weapon(
            name = "testWeapon",
            type = WeaponType.ALIEN,
            description = "testDescription",
            price = 1000.0,
            requiredAccessLvl = 10,
        ).apply {
            id = 313
            quantity = 1
        }
        every {
            weaponRepository.findById(testNewWeapon.id!!)
        } returns Optional.of(testNewWeapon)

        testNewWeapon.apply { quantity = 2 }
        every {
            weaponRepository.save(match {
                it.id == testNewWeapon.id
            })
        } returns testNewWeapon

        val testReportForm = NewReportForm(
            experimentId = testExperiment.id.toString(),
            result = "testResult",
            isSynthesized = "1",
            weaponId = testNewWeapon.id.toString(),
            weaponQuantity1 = "1"
        )

        testExperiment.apply {
            statusDate = LocalDateTime.now()
            status = ExperimentStatus.FINISHED
            result = testReportForm.result
        }
        every {
            experimentRepository.save(match {
                it.title == testExperiment.title
                        && it.description == testExperiment.description
                        && it.examinator == testExperiment.examinator
                        && it.status == testExperiment.status
                        && it.statusDate == testExperiment.statusDate
                        && it.result == testExperiment.result
            })
        } returns testExperiment

        mockMvc.perform(
            MockMvcRequestBuilders.post("/sci/report")
                .principal(mockPrincipal)
                .param("experimentId", testReportForm.experimentId)
                .param("result", testReportForm.result)
                .param("isSynthesized", testReportForm.isSynthesized)
                .param("weaponId", testReportForm.weaponId)
                .param("weaponQuantity1", testReportForm.weaponQuantity1)
        ).andExpect(status().isFound)
            .andExpect(flash().attribute("status", "Report submitted and weapon added to the arsenal."))
    }

    @WithMockUser(value = "rogosci", roles = ["SCIENTIST"])
    @Test
    fun `Check that submitting a report without a synthesized weapon for an experiment returns 302 and status`() {
        val testExperiment = Experiment(
            title = "testExperiment",
            description = "testDescription",
            examinator = testScientistScientistEmployee
        ).apply { id = 313 }
        every {
            experimentRepository.findById(testExperiment.id!!)
        } returns Optional.of(testExperiment)

        val testReportForm = NewReportForm(
            experimentId = testExperiment.id.toString(),
            result = "testResult",
            isSynthesized = "0"
        )

        testExperiment.apply {
            statusDate = LocalDateTime.now()
            status = ExperimentStatus.FINISHED
            result = testReportForm.result
        }
        every {
            experimentRepository.save(match {
                it.title == testExperiment.title
                        && it.description == testExperiment.description
                        && it.examinator == testExperiment.examinator
                        && it.status == testExperiment.status
                        && it.statusDate == testExperiment.statusDate
                        && it.result == testExperiment.result
            })
        } returns testExperiment

        mockMvc.perform(
            MockMvcRequestBuilders.post("/sci/report")
                .principal(mockPrincipal)
                .param("experimentId", testReportForm.experimentId)
                .param("result", testReportForm.result)
                .param("isSynthesized", testReportForm.isSynthesized)
        ).andExpect(status().isFound)
            .andExpect(flash().attribute("status", "Report submitted."))
    }
}