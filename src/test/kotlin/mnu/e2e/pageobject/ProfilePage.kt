package mnu.e2e.pageobject

import mnu.webdriverwrapper.element.OmniElement

class ProfilePage {
    private val profileInfoContainer: OmniElement = OmniElement("//*[contains(@class, 'profile')]")
    private val logoutBtn: OmniElement = OmniElement("//*[contains(@class, 'button_logout')]")

    fun isLoaded(): Boolean {
        return profileInfoContainer.isVisible()
    }

    fun clickLogoutBtn() {
        logoutBtn.click()
    }
}