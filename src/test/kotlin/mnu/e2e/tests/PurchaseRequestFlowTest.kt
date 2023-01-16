package mnu.e2e.tests

import mnu.e2e.pageobject.*
import mnu.webdriverwrapper.driver.Driver
import kotlin.test.*

class PurchaseRequestFlowTest {
    private val DEFAULT_URL: String = "http://localhost:8080"
    private val CLIENT_CREDENTIAL: String = "rogoclient"
    private val MANAGER_CREDENTIAL: String = "rogomanager"

    private val WARE_NAME: String = "knife"
    private val WARE_DESCRIPTION: String = "kool knife"

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
    fun `Check that a client can add a ware to their cart and purchase it with manager's help`() {
        val driver = Driver.getDriverInstance()!!

        // 1. Goto login page, login, check that shop page is loaded
        driver.get(DEFAULT_URL)
        loginAsUser(CLIENT_CREDENTIAL, CLIENT_CREDENTIAL)
        val shopPage = ShopPage()
        assertTrue(
            shopPage.isLoaded(),
            "Shop menu is not visible."
        )

        // 2. Add test ware
        shopPage.addWareToCartByDescription(WARE_DESCRIPTION)
        assertEquals(
            "Added to cart!",
            shopPage.getPopupText(),
            "Cart addition success popup is not visible."
        )
        assertContains(
            shopPage.getAddWareButtonClassByDescription(WARE_DESCRIPTION)!!,
            "rejected",
            true,
            "Button did not change its' state to removing from cart."
        )

        // 3. Goto cart page
        shopPage.clickCartBtn()
        val cartPage = CartPage()
        assertTrue(
            cartPage.isLoaded(),
            "Cart items have not loaded in time."
        )

        // 4. Check that the selected item is present
        assertEquals(
            WARE_NAME,
            cartPage.getFirstCartItemName(),
            "Previously selected ware is not in cart."
        )

        // 5. Submit the request, check that the success message has appeared on shop page
        cartPage.clickSubmitRequestBtn()
        assertTrue(
            shopPage.isLoaded(),
            "App did not redirect to the shop page."
        )
        assertEquals(
            "Request sent. Await for your managing employee's decision.",
            shopPage.getAnotherPopupText(),
            "Successful request popup is not visible."
        )

        // 6. Logout from client profile
        shopPage.clickProfileBtn()
        val profilePage = ProfilePage()
        assertTrue(
            profilePage.isLoaded(),
            "Profile info is not visible."
        )
        profilePage.clickLogoutBtn()

        // 7. Login as manager, check that manager's main page is loaded
        loginAsUser(MANAGER_CREDENTIAL, MANAGER_CREDENTIAL)
        val managerMainPage = ManagerMainPage()
        assertTrue(
            managerMainPage.isLoaded(),
            "Manager main menu is not visible."
        )

        // 8. Goto purchase requests page, check that the page is loaded
        managerMainPage.clickPurchaseRequestsBtn()
        val managerPurchaseRequestsPage = ManagerPurchaseRequestsPage()
        assertTrue(
            managerPurchaseRequestsPage.isLoaded(),
            "Manager's purchase requests are not visible."
        )

        // 9. Check that the purchase request was created and reject it
        assertTrue(
            managerPurchaseRequestsPage.isClientRequestByWeaponPresent(CLIENT_CREDENTIAL, WARE_NAME),
            "The request has not been created."
        )
        managerPurchaseRequestsPage.rejectRequestByClientAndWeapon(CLIENT_CREDENTIAL, WARE_NAME)
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
}