package mnu.e2e.tests

import mnu.webdriverwrapper.driver.Driver
import mnu.e2e.pageobject.*
import java.time.Instant
import java.time.format.DateTimeFormatter
import kotlin.test.*

class SellProductFlowTest {
    private val DEFAULT_URL: String = "http://localhost:8080"
    private val ADMIN_CREDENTIAL: String = "rogoadmin"
    private val MANUFACTURER_CREDENTIAL: String = "rogomanuf"

    private val WARE_NAME: String = "testname_${DateTimeFormatter.ISO_INSTANT.format(Instant.now())}"
    private val WARE_TYPE: WareType = WareType.ASSAULT_RIFLE
    private val WARE_DESC: String = "testdesc_${DateTimeFormatter.ISO_INSTANT.format(Instant.now())}"
    private val WARE_PRICE: String = "10"
    private val WARE_ACCESS_LVL: String = "1"
    private val WARE_QUANTITY: String = "1"

    @BeforeTest
    fun setup() {
        val driver = Driver.getDriverInstance()!!
        driver.manage().window().maximize()
    }

    @AfterTest
    fun teardown() {
        Driver.clearDriverInstance()
    }

    @Test
    fun `Check that manufacturer can create a request for selling their wares and administrator can reject it`() {
        val driver = Driver.getDriverInstance()!!

        // 1. Goto login page, login, check that manufacturer's main page is loaded
        driver.get(DEFAULT_URL)
        loginAsUser(MANUFACTURER_CREDENTIAL, MANUFACTURER_CREDENTIAL)
        val shopPage = ShopPage()
        assertTrue(
            shopPage.isLoaded(),
            "Shop menu is not visible."
        )

        // 2. Goto add ware page
        shopPage.clickAddItemBtn()
        val addNewWarePage = AddNewWarePage()
        assertTrue(
            addNewWarePage.isLoaded(),
            "New ware suggestion form is not visible."
        )

        // 3. Submit a new ware request
        addNewWarePage.setNewWareName(WARE_NAME)
        addNewWarePage.setNewWareType(WARE_TYPE)
        addNewWarePage.setNewWareDescription(WARE_DESC)
        addNewWarePage.setNewWarePrice(WARE_PRICE)
        addNewWarePage.setNewWareAccessLevel(WARE_ACCESS_LVL)
        addNewWarePage.setNewWareQuantity(WARE_QUANTITY)
        addNewWarePage.clickSubmitBtn()

        assertTrue(
            shopPage.isLoaded(),
            "App did not redirect to the shop page."
        )
        assertEquals(
            "Request submitted. Await for administrator's decision.",
            shopPage.getAnotherPopupText(),
            "Successful request popup is not visible."
        )

        // 4. Logout, login as admin
        shopPage.clickProfileBtn()
        logoutFromProfilePage()
        loginAsUser(ADMIN_CREDENTIAL, ADMIN_CREDENTIAL)
        val adminMainPage = AdministratorMainPage()
        assertTrue(
            adminMainPage.isLoaded(),
            "Admin menu is not visible."
        )

        // 5. Go to new ware requests page
        adminMainPage.clickNewWeaponsBtn()
        val adminNewWeaponsPage = AdministratorNewWeaponsPage()
        assertTrue(
            adminNewWeaponsPage.isLoaded(),
            "New weapons request list is not visible."
        )

        // 6. Check that the request was created and reject it
        assertTrue(
            adminNewWeaponsPage.isWeaponRequestByNamePresent(WARE_NAME),
            "The request has not been created."
        )
        adminNewWeaponsPage.rejectNewWeaponRequestByName(WARE_NAME)
    }

    private fun loginAsUser(username: String, password: String) {
        val loginPage = LoginPage()
        assertTrue(
            loginPage.isLoaded(),
            "Login form is not visible."
        )

        loginPage.setLogin(username)
        loginPage.setPassword(password)
        loginPage.clickLoginBtn()
    }

    private fun logoutFromProfilePage() {
        val profilePage = ProfilePage()
        assertTrue(
            profilePage.isLoaded(),
            "Profile info is not visible."
        )
        profilePage.clickLogoutBtn()
    }
}