package mnu.e2e.pageobject

import mnu.webdriverwrapper.driver.Driver
import mnu.webdriverwrapper.element.OmniElement
import mnu.webdriverwrapper.element.TextContainer

class ManagerPurchaseRequestsPage {
    private val purchaseRequestList: OmniElement = OmniElement("//*[@class='content']")

    fun isLoaded(): Boolean {
        return purchaseRequestList.isVisible()
    }

    fun isClientRequestByWeaponPresent(client: String, weapon: String): Boolean {
        val element =
            OmniElement(
                "//*[@class='content']//*[contains(normalize-space(text()), '$weapon')]" +
                        "//ancestor::*[contains(@id, 'request-content')]//*[contains(normalize-space(text()), '$client')]"
            )
        return element.isVisible()
    }

    fun approveRequestByClientAndWeapon(client: String, weapon: String) {
        val element =
            OmniElement(
                "//*[@class='content']//*[contains(normalize-space(text()), '$weapon')]" +
                        "//ancestor::*[contains(@id, 'request-content')]//*[contains(normalize-space(text()), '$client')]" +
                        "//ancestor::*[contains(@id, 'request-content')]//*[contains(@class, 'approved')]"
            )
        element.clickJs()
        element.clickJs()
    }

    fun rejectRequestByClientAndWeapon(client: String, weapon: String) {
        val element =
            OmniElement(
                "//*[@class='content']//*[contains(normalize-space(text()), '$weapon')]" +
                        "//ancestor::*[contains(@id, 'request-content')]//*[contains(normalize-space(text()), '$client')]" +
                        "//ancestor::*[contains(@id, 'request-content')]//*[contains(@class, 'rejected')]"
            )
        element.clickJs()
        element.clickJs()
    }

    fun getRequestPopupByClientAndWeapon(client: String, weapon: String): String? {
        val element =
            TextContainer(
                "//*[@class='content']//*[contains(normalize-space(text()), '$weapon')]" +
                        "//ancestor::*[contains(@id, 'request-content')]//*[contains(normalize-space(text()), '$client')]" +
                        "//ancestor::*[contains(@id, 'request-content')]//*[contains(@class, 'request-flash')]"
            )
        Driver.waitMs(500)
        return element.getAttribute("innerHTML")
    }
}