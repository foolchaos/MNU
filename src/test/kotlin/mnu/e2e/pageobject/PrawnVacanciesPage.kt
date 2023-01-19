package mnu.e2e.pageobject

import mnu.webdriverwrapper.element.OmniElement
import mnu.webdriverwrapper.element.TextContainer

class PrawnVacanciesPage {
    private val vacancyListContainer: OmniElement = OmniElement("//*[@class='content']")
    private val messageContainer: TextContainer =
        TextContainer("//*[@class='content']//*[contains(@class, 'flash_success')]")
    private val backBtn: OmniElement = OmniElement("//*[@href='/']")

    fun isLoaded(): Boolean = vacancyListContainer.isVisible()

    fun isVacancyByNamePresent(name: String): Boolean {
        val element =
            OmniElement("//*[@class='content']//*[contains(normalize-space(text()), '$name')]//ancestor::*[contains(@class, 'vacancy-item')]")
        return element.isVisible()
    }

    fun applyToVacancyByName(name: String) {
        val element =
            OmniElement("//*[@class='content']//*[contains(normalize-space(text()), '$name')]//ancestor::*[contains(@class, 'vacancy-item')]//input[contains(@class, 'approved')]")
        element.clickJs()
    }

    fun getMessageText(): String = messageContainer.getText()!!

    fun clickBackBtn() {
        backBtn.click()
    }
}