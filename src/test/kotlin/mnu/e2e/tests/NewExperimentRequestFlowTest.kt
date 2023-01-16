package mnu.e2e.tests

import mnu.e2e.pageobject.*
import mnu.webdriverwrapper.driver.Driver
import java.time.Instant
import java.time.format.DateTimeFormatter
import kotlin.test.*

class NewExperimentRequestFlowTest {
    private val DEFAULT_URL: String = "http://localhost:8080"
    private val SCIENTIST_CREDENTIAL: String = "rogosci"
    private val ADMIN_CREDENTIAL: String = "rogoadmin"

    private val EXPERIMENT_TITLE: String = "test unique ${DateTimeFormatter.ISO_INSTANT.format(Instant.now())}"
    private val ADDITIONAL_SCIENTIST_ID: String = "9"
    private val EXPERIMENT_DESCRIPTION: String = "test unique desc ${DateTimeFormatter.ISO_INSTANT.format(Instant.now())}"
    private val MAIN_SCIENTIST_NAME: String = "Nikitako Rogalen"

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
    fun `Check that a scientist can conduct an experiment with administrators' approval`() {
        val driver = Driver.getDriverInstance()!!

        // 1. Goto login page, login, check that scientist's main page is loaded
        driver.get(DEFAULT_URL)
        loginAsUser(SCIENTIST_CREDENTIAL, SCIENTIST_CREDENTIAL)
        val scientistMainPage = ScientistMainPage()
        assertTrue(
            scientistMainPage.isLoaded(),
            "Scientist menu is not visible."
        )

        // 2. Goto new experiment requests page
        scientistMainPage.clickAddExperimentBtn()
        val scientistNewExperimentPage = ScientistNewExperimentPage()
        assertTrue(
            scientistNewExperimentPage.isLoaded(),
            "New experiment menu is not visible."
        )

        // 3. Set form values and submit, check that redirect popup has a correct message
        scientistNewExperimentPage.setExperimentTitle(EXPERIMENT_TITLE)
        scientistNewExperimentPage.setExperimentAssistantId(ADDITIONAL_SCIENTIST_ID)
        scientistNewExperimentPage.setExperimentDescription(EXPERIMENT_DESCRIPTION)
        scientistNewExperimentPage.clickSubmitRequestBtn()
        assertEquals(
            "Request sent. Wait for supervisor's decision.",
            scientistMainPage.getPopupText(),
            "Successful request popup is not visible."
        )

        // 4. Logout from scientist profile
        scientistMainPage.clickProfileBtn()
        val profilePage = ProfilePage()
        assertTrue(
            profilePage.isLoaded(),
            "Profile info is not visible."
        )
        profilePage.clickLogoutBtn()

        // 5. Login as administrator
        loginAsUser(ADMIN_CREDENTIAL, ADMIN_CREDENTIAL)
        val adminMainPage = AdministratorMainPage()
        assertTrue(
            adminMainPage.isLoaded(),
            "Administrator menu is not visible."
        )

        // 6. Goto experiment requests page
        adminMainPage.clickExperimentRequestsBtn()
        val adminExperimentPage = AdministratorExperimentsPage()
        assertTrue(
            adminExperimentPage.isLoaded(),
            "Experiment requests page is not visible."
        )

        // 7. Check that the request was created and reject it
        assertTrue(
            adminExperimentPage.isExperimentRequestByScientistAndNamePresent(MAIN_SCIENTIST_NAME, EXPERIMENT_TITLE),
            "The request has not been created."
        )
        adminExperimentPage.rejectExperimentRequestByScientistAndName(MAIN_SCIENTIST_NAME, EXPERIMENT_TITLE)

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