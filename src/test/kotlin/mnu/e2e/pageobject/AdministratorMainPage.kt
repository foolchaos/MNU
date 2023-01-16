package mnu.e2e.pageobject

import mnu.webdriverwrapper.element.OmniElement

class AdministratorMainPage {
    private val menuContainer: OmniElement = OmniElement("//*[@class='grid']")
    private val experimentRequestsBtn: OmniElement =
        OmniElement("//*[@class='grid']//*[contains(@class, 'experiments-articles')]")
    private val districtIncidentsBtn: OmniElement =
        OmniElement("//*[@class='grid']//*[contains(@class, 'district')]")
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

    fun clickProfileBtn() {
        profileBtn.click()
    }
}