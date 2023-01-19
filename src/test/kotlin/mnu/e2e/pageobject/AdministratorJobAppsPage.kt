package mnu.e2e.pageobject

import mnu.webdriverwrapper.element.OmniElement

class AdministratorJobAppsPage {
    private val mainContentList: OmniElement = OmniElement("//*[contains(@class,'content')]")

    fun isLoaded(): Boolean = mainContentList.isVisible()

    fun isApplicationRequestByPrawnAndTitlePresent(prawnName: String, vacancyTitle: String): Boolean {
        val element =
            OmniElement(
                "//*[contains(@class, 'content')]//*[contains(normalize-space(text()), '$prawnName')]" +
                        "//ancestor::*[contains(@id, 'request-content')]" +
                        "//*[contains(normalize-space(text()), '$vacancyTitle')]"
            )
        return element.isVisible()
    }

    fun approveApplicationRequestByPrawnAndTitle(prawnName: String, vacancyTitle: String) {
        val element =
            OmniElement(
                "//*[contains(@class, 'content')]//*[contains(normalize-space(text()), '$prawnName')]" +
                        "//ancestor::*[contains(@id, 'request-content')]" +
                        "//*[contains(normalize-space(text()), '$vacancyTitle')]" +
                        "//ancestor::*[contains(@id, 'request-content')]//*[contains(@class, 'approved')]"
            )
        element.clickJs()
    }

    fun rejectApplicationRequestByPrawnAndTitle(prawnName: String, vacancyTitle: String) {
        val element =
            OmniElement(
                "//*[contains(@class, 'content')]//*[contains(normalize-space(text()), '$prawnName')]" +
                        "//ancestor::*[contains(@id, 'request-content')]" +
                        "//*[contains(normalize-space(text()), '$vacancyTitle')]" +
                        "//ancestor::*[contains(@id, 'request-content')]//*[contains(@class, 'rejected')]"
            )
        element.clickJs()
    }
}