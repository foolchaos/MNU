package mnu.unit

import com.ninjasquad.springmockk.MockkBean
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import mnu.config.SecurityConfig
import mnu.controller.ManufacturerController
import mnu.model.entity.*
import mnu.model.entity.request.NewVacancyRequest
import mnu.model.entity.request.NewWeaponRequest
import mnu.model.entity.shop.ShoppingCartItem
import mnu.model.entity.shop.ShoppingCartStatus
import mnu.model.form.NewProductForm
import mnu.model.form.NewVacancyForm
import mnu.repository.ClientRepository
import mnu.repository.TransportRepository
import mnu.repository.UserRepository
import mnu.repository.WeaponRepository
import mnu.repository.request.NewTransportRequestRepository
import mnu.repository.request.NewVacancyRequestRepository
import mnu.repository.request.NewWeaponRequestRepository
import mnu.repository.request.PurchaseRequestRepository
import mnu.repository.shop.ShoppingCartItemRepository
import mnu.repository.shop.ShoppingCartRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.Sort
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.security.Principal
import javax.sql.DataSource


@WebMvcTest(controllers = [ManufacturerController::class])
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = [SecurityConfig::class])
@Import(ManufacturerController::class)
class ManufacturerControllerTest(@Autowired var mockMvc: MockMvc) {
    @MockkBean
    lateinit var dataSource: DataSource

    @MockkBean
    lateinit var shoppingCartItemRepository: ShoppingCartItemRepository

    @MockkBean
    lateinit var shoppingCartRepository: ShoppingCartRepository

    @MockkBean
    lateinit var purchaseRequestRepository: PurchaseRequestRepository

    @MockkBean
    lateinit var newWeaponRequestRepository: NewWeaponRequestRepository

    @MockkBean
    lateinit var newTransportRequestRepository: NewTransportRequestRepository

    @MockkBean
    lateinit var newVacancyRequestRepository: NewVacancyRequestRepository

    @MockkBean
    lateinit var transportRepository: TransportRepository

    @MockkBean
    lateinit var weaponRepository: WeaponRepository

    @MockkBean
    lateinit var clientRepository: ClientRepository

    @MockkBean
    lateinit var userRepository: UserRepository

    private val mockPrincipal: Principal = mockk()

    private val testManufacturerUser: User = User(login = "rogomanuf", role = Role.MANUFACTURER).apply { id = 313 }
    private val testManufacturerClient: Client =
        Client(name = "rogomanuf", email = "rogomanuf@rogomanuf.com", type = ClientType.MANUFACTURER).apply { id = 313 }

    @BeforeEach
    fun setUp() {
        every { mockPrincipal.name } returns testManufacturerUser.login
        every { userRepository.findByLogin(testManufacturerUser.login) } returns testManufacturerUser
        every { clientRepository.findByUserId(testManufacturerUser.id!!) } returns testManufacturerClient
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @WithMockUser(value = "rogomanuf", roles = ["MANUFACTURER"])
    @Test
    fun `Check that cart page GET returns 200 OK and returns a new empty cart`() {
        val emptyCartArr = mutableListOf<ShoppingCartItem>()
        every {
            shoppingCartRepository.findAllByUserAndStatus(
                testManufacturerUser,
                ShoppingCartStatus.CREATING
            )
        } returns null

        mockMvc.perform(
            MockMvcRequestBuilders
                .get("/manufacturer/cart")
                .principal(mockPrincipal)
        )
            .andExpect(status().isOk)
            .andExpect(model().attribute("items", emptyCartArr))
    }

    @WithMockUser(value = "rogomanuf", roles = ["MANUFACTURER"])
    @Test
    fun `Check that new product page GET returns 200 OK and returns a new product form`() {
        val newProductForm = NewProductForm()
        mockMvc.perform(
            MockMvcRequestBuilders
                .get("/manufacturer/newProduct")
                .principal(mockPrincipal)
        )
            .andExpect(status().isOk)
            .andExpect(model().attribute("form", newProductForm))
    }

    @WithMockUser(value = "rogomanuf", roles = ["MANUFACTURER"])
    @Test
    fun `Check that new vacancy page GET returns 200 OK and returns a new vacancy form`() {
        val newVacancyForm = NewVacancyForm()
        mockMvc.perform(
            MockMvcRequestBuilders
                .get("/manufacturer/newVacancy")
                .principal(mockPrincipal)
        )
            .andExpect(status().isOk)
            .andExpect(model().attribute("form", newVacancyForm))
    }

    @WithMockUser(value = "rogomanuf")
    @Test
    fun `Check that add new valid vacancy POST returns 302 and returns status`() {
        val newValidVacancyRequest = NewVacancyRequest(
            title = "test vac", requiredKarma = 5L,
            salary = 1.0, vacantPlaces = 5L, workHoursPerWeek = 10,
            client = testManufacturerClient
        )
        val newVacancyForm = NewVacancyForm(
            title = "test vac", requiredKarma = "5",
            salary = "1.0", vacantPlaces = "5", workHoursPerWeek = "10"
        )
        every {
            newVacancyRequestRepository.save(match {
                it.title == newValidVacancyRequest.title
                        && it.requiredKarma == newValidVacancyRequest.requiredKarma
                        && it.salary == newValidVacancyRequest.salary
                        && it.vacantPlaces == newValidVacancyRequest.vacantPlaces
                        && it.workHoursPerWeek == newValidVacancyRequest.workHoursPerWeek
                        && it.client == newValidVacancyRequest.client
            })
        } returns newValidVacancyRequest

        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/manufacturer/newVacancy")
                .param("title", newVacancyForm.title)
                .param("requiredKarma", newVacancyForm.requiredKarma)
                .param("salary", newVacancyForm.salary)
                .param("vacantPlaces", newVacancyForm.vacantPlaces)
                .param("workHoursPerWeek", newVacancyForm.workHoursPerWeek)
                .principal(mockPrincipal)
        )
            .andExpect(status().isFound)
            .andExpect(flash().attribute("status", "Request submitted. Await for administrator's decision."))
    }

    @WithMockUser(value = "rogomanuf")
    @Test
    fun `Check that add new valid product POST returns 302 and returns status`() {
        val newValidProductRequest = NewWeaponRequest(
            name = "knife",
            description = "kool knife",
            type = WeaponType.MELEE,
            price = 2.0,
            requiredAccessLvl = 1,
            quantity = 10,
            user = testManufacturerUser
        )
        val newProductForm = NewProductForm(
            name = "knife",
            description = "kool knife",
            type = "melee",
            price = "2.0",
            accessLvl = "1",
            quantity = "10"
        )
        every {
            newWeaponRequestRepository.save(match {
                it.name == newValidProductRequest.name
                        && it.description == newValidProductRequest.description
                        && it.type == newValidProductRequest.type
                        && it.price == newValidProductRequest.price
                        && it.requiredAccessLvl == newValidProductRequest.requiredAccessLvl
                        && it.quantity == newValidProductRequest.quantity
                        && it.user == newValidProductRequest.user
            })
        } returns newValidProductRequest

        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/manufacturer/newProduct")
                .param("name", newProductForm.name)
                .param("description", newProductForm.description)
                .param("type", newProductForm.type)
                .param("price", newProductForm.price)
                .param("accessLvl", newProductForm.accessLvl)
                .param("quantity", newProductForm.quantity)
                .principal(mockPrincipal)
        )
            .andExpect(status().isFound)
            .andExpect(flash().attribute("status", "Request submitted. Await for administrator's decision."))
    }

}