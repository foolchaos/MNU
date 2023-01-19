package mnu.e2e.tests

import mnu.webdriverwrapper.driver.Driver
import mnu.e2e.pageobject.*
import java.time.Instant
import java.time.format.DateTimeFormatter
import kotlin.test.*

class ApplyToVacancyFlowTest {
    private val DEFAULT_URL: String = "http://localhost:8080"
    private val ADMIN_CREDENTIAL: String = "rogoadmin"
    private val PRAWN_CREDENTIAL: String = "rogoprawn"

    private val PRAWN_NAME: String = "RUGU EWONKO"
    private val VACANCY_TITLE: String = "hard worker"

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
    fun `Check that a prawn can apply to a job and administrator can reject its request`() {
        val driver = Driver.getDriverInstance()!!

        // 1. Goto login page, login, check that prawn's main page is loaded
        driver.get(DEFAULT_URL)
        loginAsUser(PRAWN_CREDENTIAL, PRAWN_CREDENTIAL)
        val prawnMainPage = PrawnMainPage()
        assertTrue(
            prawnMainPage.isLoaded(),
            "Prawn main menu is not visible."
        )

        // 2. Goto vacancy list page and apply to a set vacancy
        prawnMainPage.clickVacanciesBtn()
        val prawnVacanciesPage = PrawnVacanciesPage()
        assertTrue(
            prawnVacanciesPage.isLoaded(),
            "Prawn vacancy selection menu is not visible."
        )

        // 3. Apply to a vacancy and check that the request was sent
        assertTrue(
            prawnVacanciesPage.isVacancyByNamePresent(VACANCY_TITLE),
            "The vacancy with this name is not present."
        )
        prawnVacanciesPage.applyToVacancyByName(VACANCY_TITLE)
        assertEquals(
            "Request sent. Wait for supervisor's decision.",
            prawnVacanciesPage.getMessageText(),
            "Successful request popup is not visible."
        )

        // 4. Logout, login as admin
        prawnVacanciesPage.clickBackBtn()
        prawnMainPage.clickProfileBtn()
        logoutFromProfilePage()
        loginAsUser(ADMIN_CREDENTIAL, ADMIN_CREDENTIAL)
        val adminMainPage = AdministratorMainPage()
        assertTrue(
            adminMainPage.isLoaded(),
            "Admin menu is not visible."
        )

        // 5. Goto vacancy application requests page
        adminMainPage.clickPrawnJobApplicationsBtn()
        val adminJobAppsPage = AdministratorJobAppsPage()
        assertTrue(
            adminJobAppsPage.isLoaded(),
            "Admin job app page is not visible."
        )

        // 6. Check that the request was created and reject it
        assertTrue(
            adminJobAppsPage.isApplicationRequestByPrawnAndTitlePresent(PRAWN_NAME, VACANCY_TITLE),
            "The vacancy with this name is not present."
        )
        adminJobAppsPage.rejectApplicationRequestByPrawnAndTitle(PRAWN_NAME, VACANCY_TITLE)
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