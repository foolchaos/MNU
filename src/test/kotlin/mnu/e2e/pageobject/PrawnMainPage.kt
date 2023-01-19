package mnu.e2e.pageobject

import mnu.webdriverwrapper.element.OmniElement

class PrawnMainPage {
    private val prawnMenuContainer: OmniElement = OmniElement("//*[contains(@class, 'menu-content')]")
    private val vacanciesBtn: OmniElement = OmniElement("//*[contains(@class, 'menu-content')]//*[contains(@href, 'vacancies')]")
    private val profileBtn: OmniElement = OmniElement("//*[contains(@href, 'profile')]")

    fun isLoaded(): Boolean = prawnMenuContainer.isVisible()

    fun clickVacanciesBtn() {
        vacanciesBtn.click()
    }

    fun clickProfileBtn() {
        profileBtn.click()
    }
}