package mnu.e2e.pageobject

import mnu.webdriverwrapper.element.OmniElement

class AdministratorNewWeaponsPage {
    private val weaponRequestList: OmniElement = OmniElement("//*[@class='content']")

    fun isLoaded(): Boolean = weaponRequestList.isVisible()

    fun isWeaponRequestByNamePresent(name: String): Boolean {
        val element =
            OmniElement(
                "//*[@class='content']//*[contains(normalize-space(text()), '$name')]"
            )
        return element.isVisible()
    }

    fun approveNewWeaponRequestByName(name: String) {
        val element =
            OmniElement(
                "//*[@class='content']//*[contains(normalize-space(text()), '$name')]" +
                        "//ancestor::*[contains(@class, 'content__item')]//*[contains(@class, 'approved')]"
            )
        element.clickJs()
    }

    fun rejectNewWeaponRequestByName(name: String) {
        val element =
            OmniElement(
                "//*[@class='content']//*[contains(normalize-space(text()), '$name')]" +
                        "//ancestor::*[contains(@class, 'content__item')]//*[contains(@class, 'rejected')]"
            )
        element.clickJs()
    }
}