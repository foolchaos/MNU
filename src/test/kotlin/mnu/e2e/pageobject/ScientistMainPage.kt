package mnu.e2e.pageobject

import mnu.webdriverwrapper.element.OmniElement
import mnu.webdriverwrapper.element.TextContainer

class ScientistMainPage {
    private val experimentList: OmniElement = OmniElement("//*[@class='content']")
    private val addExperimentBtn: OmniElement = OmniElement("//*[@class='fixed-header']//*[contains(@href, 'experiment')]")
    private val profileBtn: OmniElement = OmniElement("//*[@class='fixed-header']//*[contains(@href, 'profile')]")
    private val textPopupContainer: TextContainer = TextContainer("//*[contains(@class, 'flash_success') and not(@id='message')]")


    fun isLoaded(): Boolean {
        return experimentList.isVisible()
    }

    fun clickAddExperimentBtn() {
        addExperimentBtn.click()
    }

    fun clickProfileBtn() {
        profileBtn.click()
    }

    fun getPopupText(): String? {
        return textPopupContainer.getText()
    }
}