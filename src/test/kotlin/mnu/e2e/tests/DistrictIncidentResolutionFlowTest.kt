package mnu.e2e.tests

import mnu.webdriverwrapper.driver.Driver
import mnu.e2e.pageobject.*
import kotlin.test.*

class DistrictIncidentResolutionFlowTest {
    private val DEFAULT_URL: String = "http://localhost:8080"
    private val ADMIN_CREDENTIAL: String = "rogoadmin"
    private val SECURITY_CREDENTIAL: String = "rogosec"

    private val DISTRICT_INCIDENT_ID: String = "3"
    private val INCIDENT_SEC_AMOUNT: String = "1"
    private val INCIDENT_SEC_LVL_FROM: String = "1"
    private val INCIDENT_SEC_LVL_TO: String = "10"

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
    fun `Check that an administrator can create a resolution task for security personnel`() {
        val driver = Driver.getDriverInstance()!!

        // 1. Goto login page, login, check that scientist's main page is loaded
        driver.get(DEFAULT_URL)
        loginAsUser(ADMIN_CREDENTIAL, ADMIN_CREDENTIAL)
        val adminMainPage = AdministratorMainPage()
        assertTrue(
            adminMainPage.isLoaded(),
            "Admin menu is not visible."
        )

        // 2. Goto district map
        adminMainPage.clickDistrictIncidentBtn()
        val adminDistrictPage = AdministratorDistrictPage()
        assertTrue(
            adminDistrictPage.isLoaded(),
            "District map is not visible."
        )

        // 3. Select an incident and submit it for resolution
        adminDistrictPage.setIncidentDistrictIncidentId(DISTRICT_INCIDENT_ID)

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