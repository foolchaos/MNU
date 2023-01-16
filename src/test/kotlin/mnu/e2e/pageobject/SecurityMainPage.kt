package mnu.e2e.pageobject

import mnu.webdriverwrapper.element.OmniElement
import mnu.webdriverwrapper.element.TextContainer

class SecurityMainPage {
    private val menuContainer: OmniElement = OmniElement("//*[@class='content']")
    private val profileBtn: OmniElement = OmniElement("//*[contains(@class,'header')]//*[contains(@href, 'profile')]")
    private val textPopupContainer: TextContainer = TextContainer("//*[contains(@class, 'flash_success') and not(@id='message')]")

    fun isLoaded(): Boolean {
        return menuContainer.isVisible()
    }

    fun isIncidentSignupByDescriptionPresent(description: String): Boolean {
        val element = OmniElement("//*[@class='content']//*[contains(normalize-space(text()), '$description')]")
        return element.isVisible()
    }

    fun clickApplyToIncidentResolutionBtnByDescription(description: String) {
        val element = OmniElement("//*[@class='content']//*[contains(normalize-space(text()), '$description')]" +
                "//ancestor::*[contains(@class, 'content__item')]//*[contains(@class, 'approved')]")
        element.clickJs()
    }

    fun clickWithdrawFromIncidentResolutionBtnByDescription(description: String) {
        val element = OmniElement("//*[@class='content']//*[contains(normalize-space(text()), '$description')]" +
                "//ancestor::*[contains(@class, 'content__item')]//*[contains(@class, 'rejected')]")
        element.clickJs()
    }

    fun clickProfileBtn() {
        profileBtn.click()
    }

    fun getPopupText(): String? {
        return textPopupContainer.getText()
    }
}