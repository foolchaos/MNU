package mnu.unit

import com.ninjasquad.springmockk.MockkBean
import io.mockk.clearAllMocks
import io.mockk.mockk
import io.mockk.every
import mnu.controller.ManufacturerController
import mnu.model.entity.Role
import mnu.model.entity.User
import mnu.model.entity.shop.ShoppingCartItem
import mnu.model.entity.shop.ShoppingCartStatus
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
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.model
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.security.Principal


@WebMvcTest(controllers = [ManufacturerController::class])
@Import(ManufacturerController::class)
class ManufacturerControllerTest(@Autowired val mockMvc: MockMvc) {
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
    lateinit var userRepository: UserRepository

    private val mockPrincipal: Principal = mockk()

    private val testManufacturer: User = User(login = "rogomanuf", role = Role.MANUFACTURER)

    @BeforeEach
    fun setUp() {
        every { mockPrincipal.name } returns testManufacturer.login
        every { userRepository.findByLogin(testManufacturer.login) } returns testManufacturer
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @WithMockUser(value = "rogomanuf")
    @Test
    fun `Check that cart page GET returns 200 OK and returns a new empty cart`() {
        val emptyCartArr = mutableListOf<ShoppingCartItem>()
        every { shoppingCartRepository.findAllByUserAndStatus(testManufacturer, ShoppingCartStatus.CREATING) } returns null

        mockMvc.perform(
            MockMvcRequestBuilders
                .get("/manufacturer/cart")
                .principal(mockPrincipal)
        )
            .andExpect(status().isOk)
            .andExpect(model().attribute("items", emptyCartArr))
    }
}