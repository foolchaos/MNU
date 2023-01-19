package mnu.e2e.tests

import mnu.webdriverwrapper.driver.Driver
import mnu.e2e.pageobject.*
import java.time.Instant
import java.time.format.DateTimeFormatter
import kotlin.test.*

class AddVacancyFlowTest {
    private val DEFAULT_URL: String = "http://localhost:8080"
    private val ADMIN_CREDENTIAL: String = "rogoadmin"
    private val MANUFACTURER_CREDENTIAL: String = "rogomanuf"

    private val VACANCY_TITLE: String = "testname_${DateTimeFormatter.ISO_INSTANT.format(Instant.now())}"
    private val VACANCY_SALARY: String = "5"
    private val VACANCY_KARMA: String = "1"
    private val VACANCY_WORK_HOURS: String = "1"
    private val VACANCY_VACANT_PLACES: String = "1"

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
    fun `Check that manufacturer can create a request for adding their vacancy and administrator can reject it`() {
        val driver = Driver.getDriverInstance()!!

        // 1. Goto login page, login, check that manufacturer's main page is loaded
        driver.get(DEFAULT_URL)
        loginAsUser(MANUFACTURER_CREDENTIAL, MANUFACTURER_CREDENTIAL)
        val shopPage = ShopPage()
        assertTrue(
            shopPage.isLoaded(),
            "Shop menu is not visible."
        )

        // 2. Goto add vacancy page
        shopPage.clickAddItemBtn()
        val addNewWarePage = AddNewWarePage()
        assertTrue(
            addNewWarePage.isLoaded(),
            "New ware suggestion form is not visible."
        )
        addNewWarePage.clickAddNewVacancyPageBtn()
        val addNewVacancyPage = AddNewVacancyPage()
        assertTrue(
            addNewVacancyPage.isLoaded(),
            "New vacancy suggestion form is not visible."
        )

        // 3. Submit a new ware request
        addNewVacancyPage.setNewVacancyTitle(VACANCY_TITLE)
        addNewVacancyPage.setNewVacancySalary(VACANCY_SALARY)
        addNewVacancyPage.setNewVacancyKarma(VACANCY_KARMA)
        addNewVacancyPage.setNewVacancyWorkHours(VACANCY_WORK_HOURS)
        addNewVacancyPage.setNewVacancyVacantPlaces(VACANCY_VACANT_PLACES)
        addNewVacancyPage.clickSubmitBtn()

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

        // 5. Go to new vacancy requests page
        adminMainPage.clickPrawnVacanciesBtn()
        val adminVacanciesPage = AdministratorVacanciesPage()
        assertTrue(
            adminVacanciesPage.isLoaded(),
            "Vacancies list is not visible."
        )
        adminVacanciesPage.clickVacancyRequestsBtn()
        assertTrue(
            adminVacanciesPage.isLoaded(),
            "New vacancy requests list is not visible."
        )

        // 6. Check that the request was created and reject it
        assertTrue(
            adminVacanciesPage.isVacancyRequestByNamePresent(VACANCY_TITLE),
            "The request has not been created."
        )
        adminVacanciesPage.rejectNewVacancyRequestByName(VACANCY_TITLE)
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