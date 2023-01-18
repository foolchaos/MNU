package mnu.unit

import com.ninjasquad.springmockk.MockkBean
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import mnu.config.SecurityConfig
import mnu.controller.ClientController
import mnu.controller.ScientistController
import mnu.model.entity.*
import mnu.model.entity.employee.Employee
import mnu.model.entity.employee.PersonStatus
import mnu.model.entity.employee.ScientistEmployee
import mnu.model.entity.request.NewWeaponRequest
import mnu.model.entity.request.PurchaseRequest
import mnu.model.entity.request.Request
import mnu.model.entity.request.RequestStatus
import mnu.model.entity.shop.ShoppingCart
import mnu.model.entity.shop.ShoppingCartItem
import mnu.model.entity.shop.ShoppingCartStatus
import mnu.model.form.NewExperimentForm
import mnu.model.form.NewReportForm
import mnu.repository.*
import mnu.repository.employee.EmployeeRepository
import mnu.repository.employee.ScientistEmployeeRepository
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
import java.time.LocalDateTime
import java.util.*
import kotlin.math.exp

@WebMvcTest(controllers = [ClientController::class])
@AutoConfigureMockMvc(addFilters = false)
@Import(ClientController::class)
class ClientControllerTest(@Autowired var mockMvc: MockMvc) {
    @MockkBean
    lateinit var weaponRepository: WeaponRepository

    @MockkBean
    lateinit var transportRepository: TransportRepository

    @MockkBean
    lateinit var shoppingCartItemRepository: ShoppingCartItemRepository

    @MockkBean
    lateinit var shoppingCartRepository: ShoppingCartRepository

    @MockkBean
    lateinit var purchaseRequestRepository: PurchaseRequestRepository

    @MockkBean
    lateinit var clientRepository: ClientRepository

    @MockkBean
    lateinit var userRepository: UserRepository

    private val mockPrincipal: Principal = mockk()

    private val testClientUser: User = User(login = "rogoclient", role = Role.SCIENTIST).apply { id = 313 }
    private val testClientClient: Client = Client(
        name = "rogoclient", email = "rogoclient@rogoclient.com", type = ClientType.CUSTOMER
    ).apply {
        id = 313
        user = testClientUser
    }

    @BeforeEach
    fun setUp() {
        every { mockPrincipal.name } returns testClientUser.login
        every { userRepository.findByLogin(testClientUser.login) } returns testClientUser
        every { clientRepository.findByUserId(testClientClient.id!!) } returns testClientClient
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @WithMockUser(value = "rogoclient", roles = ["CUSTOMER"])
    @Test
    fun `Check that market filter returns 200 OK and a list of filtered wares`() {
        val testMarketWeapon = Weapon(
            name = "knife",
            description = "kool knife",
            price = 2.0,
            type = WeaponType.MELEE,
            requiredAccessLvl = 1
        ).apply { quantity = 10 }
        every {
            weaponRepository.findAllByNameIgnoreCaseContainingAndTypeAndQuantityGreaterThan(
                testMarketWeapon.name,
                testMarketWeapon.type,
                0,
                Sort.by(Sort.Direction.ASC, "price")
            )
        } returns listOf(testMarketWeapon)

        every {
            shoppingCartItemRepository.findAllByCartUserIdAndCartStatus(
                testClientUser.id!!,
                ShoppingCartStatus.CREATING
            )
        } returns mutableListOf()

        mockMvc.perform(
            MockMvcRequestBuilders.get("/client/shop/weapon")
                .principal(mockPrincipal)
                .sessionAttr("user", testClientClient)
                .param("type", "melee")
                .param("sort", "price_asc")
                .param("name", "knife")
        ).andExpect(status().isOk)
            .andExpect(model().attribute("items", listOf(testMarketWeapon)))
    }

    @WithMockUser(value = "rogoclient", roles = ["CUSTOMER"])
    @Test
    fun `Check that cart purchase request returns 302 and a status`() {
        val testMarketWeapon = Weapon(
            name = "knife",
            description = "kool knife",
            price = 2.0,
            type = WeaponType.MELEE,
            requiredAccessLvl = 1
        ).apply {
            id = 313
            quantity = 10
        }

        every {
            weaponRepository.save(match {
                it.id == testMarketWeapon.id
            })
        } returns testMarketWeapon

        val testShoppingCart = ShoppingCart(
            user = testClientUser
        ).apply {
            id = 313
            status = ShoppingCartStatus.CREATING
        }
        val testShoppingCartItem = ShoppingCartItem(
            weapon = testMarketWeapon,
            quantity = 2
        ).apply {
            id = 313
            cart = testShoppingCart
        }
        testShoppingCart.apply {
            items = mutableListOf(testShoppingCartItem)
        }

        every {
            shoppingCartRepository.findAllByUserAndStatus(
                testClientUser,
                ShoppingCartStatus.CREATING
            )
        } returns mutableListOf(testShoppingCart)
        every {
            shoppingCartRepository.save(match {
                it.id == testShoppingCart.id
            })
        } returns testShoppingCart.apply { status = ShoppingCartStatus.REQUESTED }

        val testPurchaseRequest = PurchaseRequest(
            user = testClientUser,
            cart = testShoppingCart
        )
        every {
            purchaseRequestRepository.save(match {
                it.user == testPurchaseRequest.user
                        && it.cart == testPurchaseRequest.cart
            })
        } returns testPurchaseRequest

        mockMvc.perform(
            MockMvcRequestBuilders.post("/client/cart/sendRequest")
                .principal(mockPrincipal)
                .sessionAttr("user", testClientClient)
        ).andExpect(status().isFound)
            .andExpect(flash().attribute("status", "Request sent. Await for your managing employee's decision."))
    }
}