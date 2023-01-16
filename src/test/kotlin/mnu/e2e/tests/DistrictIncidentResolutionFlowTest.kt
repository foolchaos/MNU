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

    private val INCIDENT_DESCRIPTION: String = "everything is bad"

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
        adminDistrictPage.setIncidentSecurityAmount(INCIDENT_SEC_AMOUNT)
        adminDistrictPage.setIncidentSecurityLvlFrom(INCIDENT_SEC_LVL_FROM)
        adminDistrictPage.setIncidentSecurityLvlTo(INCIDENT_SEC_LVL_TO)
        adminDistrictPage.clickSubmitBtn()
        assertEquals(
            "Success. All security employees will be notified of the occurred incident.",
            adminDistrictPage.getPopupText(),
            "Successful request popup is not visible."
        )

        // 4. Log out of admin account
        adminDistrictPage.clickBackBtn()
        assertTrue(
            adminMainPage.isLoaded(),
            "Admin menu is not visible."
        )
        adminMainPage.clickProfileBtn()
        val profilePage = ProfilePage()
        assertTrue(
            profilePage.isLoaded(),
            "Profile info is not visible."
        )
        profilePage.clickLogoutBtn()

        // 5. Log in as security, check that security main page is loaded
        loginAsUser(SECURITY_CREDENTIAL, SECURITY_CREDENTIAL)
        val securityMainPage = SecurityMainPage()
        assertTrue(
            securityMainPage.isLoaded(),
            "Security main page is not visible."
        )

        // 6. Check that incident has been created and sign up for resolution
        assertTrue(
            securityMainPage.isIncidentSignupByDescriptionPresent(INCIDENT_DESCRIPTION),
            "The resolution task has not been created."
        )
        securityMainPage.clickApplyToIncidentResolutionBtnByDescription(INCIDENT_DESCRIPTION)
        assertEquals(
            "You were appointed to the incident and have successfully resolved it.",
            securityMainPage.getPopupText(),
            "Successful request popup is not visible."
        )

        // teardown - logout from sec, re-create the task, and sign off from resolution
        // КОСТЫЫЫЫЛЬ
        securityMainPage.clickProfileBtn()
        profilePage.clickLogoutBtn()
        loginAsUser(ADMIN_CREDENTIAL, ADMIN_CREDENTIAL)
        adminMainPage.clickDistrictIncidentBtn()
        adminDistrictPage.setIncidentDistrictIncidentId(DISTRICT_INCIDENT_ID)
        adminDistrictPage.setIncidentSecurityAmount(INCIDENT_SEC_AMOUNT)
        adminDistrictPage.setIncidentSecurityLvlFrom(INCIDENT_SEC_LVL_FROM)
        adminDistrictPage.setIncidentSecurityLvlTo(INCIDENT_SEC_LVL_TO)
        adminDistrictPage.clickSubmitBtn()
        adminDistrictPage.clickBackBtn()
        adminMainPage.clickProfileBtn()
        profilePage.clickLogoutBtn()
        loginAsUser(SECURITY_CREDENTIAL, SECURITY_CREDENTIAL)
        securityMainPage.clickWithdrawFromIncidentResolutionBtnByDescription(INCIDENT_DESCRIPTION)
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