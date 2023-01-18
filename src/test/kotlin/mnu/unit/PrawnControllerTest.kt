package mnu.unit;

import com.ninjasquad.springmockk.MockkBean
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import mnu.config.SecurityConfig
import mnu.controller.PrawnController;
import mnu.model.entity.*
import mnu.model.entity.employee.Employee
import mnu.model.entity.employee.ManagerEmployee
import mnu.model.entity.employee.PersonStatus
import mnu.model.entity.request.PurchaseRequest
import mnu.model.entity.shop.ShoppingCart
import mnu.model.entity.shop.ShoppingCartItem
import mnu.model.entity.shop.ShoppingCartStatus
import mnu.repository.*
import mnu.repository.request.PurchaseRequestRepository
import mnu.repository.request.RequestRepository
import mnu.repository.request.VacancyApplicationRequestRepository
import mnu.repository.shop.ShoppingCartItemRepository
import mnu.repository.shop.ShoppingCartRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.security.Principal
import java.util.*
import javax.sql.DataSource

@WebMvcTest(controllers = [PrawnController::class])
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = [SecurityConfig::class])
@Import(PrawnController::class)
public class PrawnControllerTest(@Autowired var mockMvc: MockMvc) {

    @MockkBean
    lateinit var dataSource: DataSource

    @MockkBean
    lateinit var districtIncidentRepository: DistrictIncidentRepository

    @MockkBean
    lateinit var districtHouseRepository: DistrictHouseRepository

    @MockkBean
    lateinit var requestRepository: RequestRepository

    @MockkBean
    lateinit var vacancyRepository: VacancyRepository

    @MockkBean
    lateinit var vacancyApplicationRequestRepository: VacancyApplicationRequestRepository

    @MockkBean
    lateinit var weaponRepository: WeaponRepository

    @MockkBean
    lateinit var transportRepository: TransportRepository

    @MockkBean
    lateinit var purchaseRequestRepository: PurchaseRequestRepository

    @MockkBean
    lateinit var shoppingCartRepository: ShoppingCartRepository

    @MockkBean
    lateinit var shoppingCartItemRepository: ShoppingCartItemRepository

    @MockkBean
    lateinit var userRepository: UserRepository

    @MockkBean
    lateinit var prawnRepository: PrawnRepository

    private val mockPrincipal: Principal = mockk()

    private val testPrawnUser: User = User(login = "rogoprawn", role = Role.PRAWN).apply { id = 313 }
    private val testManagerUser: User = User(login = "rogomanager", role = Role.MANAGER).apply { id = 313 }
    private val testPrawnPrawn: Prawn = Prawn(
        name = "prawn"
    ).apply {
        id = 313
        user = testPrawnUser
        karma = 6000
        balance = 400.0
    }
    private val testManagerEmployee: Employee =
        Employee(name = "rogomanager", level = 5, salary = 313, position = "rogomanager").apply {
            id = 313
            user = testManagerUser
            status = PersonStatus.WORKING
        }
    private val testManagerManagerEmployee: ManagerEmployee =
        ManagerEmployee().apply { id=313
            employee = testManagerEmployee }

    @BeforeEach
    fun setUp() {
        every { mockPrincipal.name } returns testPrawnUser.login
        every { userRepository.findByLogin(testPrawnUser.login) } returns testPrawnUser
        every { prawnRepository.findByUserId(testPrawnPrawn.user!!.id!!) } returns testPrawnPrawn
        every { prawnRepository.findById(testPrawnPrawn.id!!) } returns Optional.of(testPrawnPrawn)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @WithMockUser(value = "rogoprawn", roles = ["PRAWN"])
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
            user = testPrawnUser
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
                testPrawnUser,
                ShoppingCartStatus.CREATING
            )
        } returns mutableListOf(testShoppingCart)
        every {
            shoppingCartRepository.save(match {
                it.id == testShoppingCart.id
            })
        } returns testShoppingCart.apply { status = ShoppingCartStatus.REQUESTED }

        val testPurchaseRequest = PurchaseRequest(
            user = testPrawnUser,
            cart = testShoppingCart
        )
        every {
            purchaseRequestRepository.save(match {
                it.user == testPurchaseRequest.user
                        && it.cart == testPurchaseRequest.cart
            })
        } returns testPurchaseRequest

        mockMvc.perform(
            MockMvcRequestBuilders.post("/prawn/cart/sendRequest")
                .principal(mockPrincipal)
                .sessionAttr("user", testPrawnPrawn)
        ).andExpect(MockMvcResultMatchers.status().isFound)
            .andExpect(MockMvcResultMatchers.flash().attribute("status", "Request sent. Await for your managing employee's decision."))
    }

    @WithMockUser(value = "rogoprawn", roles = ["PRAWN"])
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
                testPrawnUser.id!!,
                ShoppingCartStatus.CREATING
            )
        } returns mutableListOf()

        mockMvc.perform(
            MockMvcRequestBuilders.get("/prawn/shop/weapon")
                .principal(mockPrincipal)
                .sessionAttr("user", testPrawnPrawn)
                .param("type", "melee")
                .param("sort", "price_asc")
                .param("name", "knife")
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.model().attribute("items", listOf(testMarketWeapon)))
    }

}