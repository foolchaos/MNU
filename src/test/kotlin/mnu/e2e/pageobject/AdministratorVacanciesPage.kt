package mnu.e2e.pageobject

import mnu.webdriverwrapper.element.OmniElement

class AdministratorVacanciesPage {
    private val mainContentList: OmniElement = OmniElement("//*[@class='content']")
    private val vacancyRequestsBtn: OmniElement = OmniElement("//*[contains(@href, 'requests')]")

    fun isLoaded(): Boolean = mainContentList.isVisible()

    fun isVacancyRequestByNamePresent(name: String): Boolean {
        val element =
            OmniElement(
                "//*[@class='content']//*[contains(normalize-space(text()), '$name')]"
            )
        return element.isVisible()
    }

    fun approveNewVacancyRequestByName(name: String) {
        val element =
            OmniElement(
                "//*[@class='content']//*[contains(normalize-space(text()), '$name')]" +
                        "//ancestor::*[contains(@id, 'request-content')]//*[contains(@class, 'approved')]"
            )
        element.clickJs()
    }

    fun rejectNewVacancyRequestByName(name: String) {
        val element =
            OmniElement(
                "//*[@class='content']//*[contains(normalize-space(text()), '$name')]" +
                        "//ancestor::*[contains(@id, 'request-content')]//*[contains(@class, 'rejected')]"
            )
        element.clickJs()
    }

    fun clickVacancyRequestsBtn() {
        vacancyRequestsBtn.click()
    }
}