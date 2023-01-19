package mnu.e2e.pageobject

import mnu.webdriverwrapper.element.OmniElement

class AdministratorMainPage {
    private val menuContainer: OmniElement = OmniElement("//*[@class='grid']")
    private val experimentRequestsBtn: OmniElement =
        OmniElement("//*[@class='grid']//*[contains(@class, 'experiments-articles')]")
    private val districtIncidentsBtn: OmniElement =
        OmniElement("//*[@class='grid']//*[contains(@class, 'district')]")
    private val newWaresBtn: OmniElement =
        OmniElement("//*[@class='grid']//*[contains(@class, 'new-weapons')]")
    private val prawnVacanciesBtn: OmniElement =
        OmniElement("//*[@class='grid']//*[contains(@class, 'prawn-vacancies')]")
    private val prawnJobApplicationsBtn: OmniElement =
        OmniElement("//*[@class='grid']//*[contains(@class, 'prawn-job-applications')]")
    private val profileBtn: OmniElement = OmniElement("//*[@class='header']//*[contains(@href, 'profile')]")


    fun isLoaded(): Boolean {
        return menuContainer.isVisible()
    }

    fun clickExperimentRequestsBtn() {
        experimentRequestsBtn.click()
    }

    fun clickDistrictIncidentBtn() {
        districtIncidentsBtn.click()
    }

    fun clickNewWeaponsBtn() {
        newWaresBtn.click()
    }

    fun clickPrawnVacanciesBtn() {
        prawnVacanciesBtn.click()
    }

    fun clickPrawnJobApplicationsBtn() {
        prawnJobApplicationsBtn.click()
    }

    fun clickProfileBtn() {
        profileBtn.click()
    }
}