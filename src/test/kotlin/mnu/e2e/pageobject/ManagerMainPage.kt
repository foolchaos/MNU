package mnu.e2e.pageobject

import mnu.webdriverwrapper.element.OmniElement

class ManagerMainPage {
    private val managerMenuContainer: OmniElement = OmniElement("//*[@class='managers-grid__buttons']")
    private val purchaseRequestsBtn: OmniElement = OmniElement("//*[@class='managers-grid__buttons']//*[contains(@href, 'purchaseRequests')]")

    fun isLoaded(): Boolean {
        return managerMenuContainer.isVisible()
    }

    fun clickPurchaseRequestsBtn() {
        purchaseRequestsBtn.click()
    }
}