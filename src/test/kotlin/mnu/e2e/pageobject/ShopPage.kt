package mnu.e2e.pageobject

import mnu.webdriverwrapper.driver.Driver
import mnu.webdriverwrapper.element.OmniElement
import mnu.webdriverwrapper.element.TextContainer

class ShopPage {
    private val shopSectionsList: OmniElement = OmniElement("//*[@id='mobile-menu']")
    private val textPopupContainer: TextContainer = TextContainer("//*[@id='message']")
    private val anotherTextPopupContainer: TextContainer = TextContainer("//*[contains(@class, 'flash_success') and not(@id='message')]")
    private val cartBtn: OmniElement = OmniElement("//*[contains(@class, 'desktop')]//*[@alt='Cart']")
    private val profileBtn: OmniElement = OmniElement("//*[contains(@class, 'desktop')]//*[@alt='Profile settings']")

    fun isLoaded(): Boolean {
        return shopSectionsList.isVisible()
    }

    fun addWareToCartByDescription(wareDesc: String) {
        val testWareAddBtn =
            OmniElement("//*[contains(text(), '$wareDesc')]//ancestor::*[contains(@class, 'shop-item')]//*[contains(@class, 'button')]")
        testWareAddBtn.click()
    }

    fun getAddWareButtonClassByDescription(wareDesc: String): String? {
        val testWareAddBtn =
            OmniElement("//*[contains(text(), '$wareDesc')]//ancestor::*[contains(@class, 'shop-item')]//*[contains(@class, 'button')]")
        return testWareAddBtn.getAttribute("class")
    }

    fun getPopupText(): String? {
        return textPopupContainer.getText()
    }

    fun getAnotherPopupText(): String? {
        return anotherTextPopupContainer.getText()
    }

    fun clickCartBtn() {
        cartBtn.click()
    }

    fun clickProfileBtn() {
        profileBtn.click()
    }
}