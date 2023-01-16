package mnu.e2e.pageobject

import mnu.webdriverwrapper.element.OmniElement

class AdministratorExperimentsPage {
    private val experimentRequestList: OmniElement = OmniElement("//*[@class='content']")

    fun isLoaded(): Boolean = experimentRequestList.isVisible()

    fun isExperimentRequestByScientistAndNamePresent(scientistName: String, experimentName: String): Boolean {
        val element =
            OmniElement(
                "//*[@class='content']//*[contains(normalize-space(text()), '$experimentName')]" +
                        "//ancestor::*[contains(@class, 'content__item')]" +
                        "//*[contains(normalize-space(text()), '$scientistName')]"
            )
        return element.isVisible()
    }

    fun approveExperimentRequestByScientistAndName(scientistName: String, experimentName: String) {
        val element =
            OmniElement(
                "//*[@class='content']//*[contains(normalize-space(text()), '$experimentName')]" +
                        "//ancestor::*[contains(@class, 'content__item')]" +
                        "//*[contains(normalize-space(text()), '$scientistName')]" +
                        "//ancestor::*[contains(@class, 'content__item')]//*[contains(@class, 'approved')]"
            )
        element.clickJs()
        element.clickJs()
    }

    fun rejectExperimentRequestByScientistAndName(scientistName: String, experimentName: String) {
        val element =
            OmniElement(
                "//*[@class='content']//*[contains(normalize-space(text()), '$experimentName')]" +
                        "//ancestor::*[contains(@class, 'content__item')]" +
                        "//*[contains(normalize-space(text()), '$scientistName')]" +
                        "//ancestor::*[contains(@class, 'content__item')]//*[contains(@class, 'rejected')]"
            )
        element.clickJs()
        element.clickJs()
    }
}