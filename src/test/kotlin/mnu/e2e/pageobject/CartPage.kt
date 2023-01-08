package mnu.e2e.pageobject

import mnu.webdriverwrapper.element.OmniElement
import mnu.webdriverwrapper.element.TextContainer

class CartPage {
    private val cartItemsList: OmniElement = OmniElement("//*[@class='content']")
    private val firstCartItemContainerLocator: String = "(//*[contains(@class, 'content__item')][1])"
    private val firstCartItemNameContainer: TextContainer = TextContainer("$firstCartItemContainerLocator//*[@class='form-item_small'][1]")
    private val submitRequestBtn: OmniElement = OmniElement("//*[contains(@class, 'last-item')]//input[@class='button']")

    fun isLoaded(): Boolean {
        return cartItemsList.isVisible()
    }

    fun getFirstCartItemName(): String? {
        return firstCartItemNameContainer.getText()
    }

    fun clickSubmitRequestBtn() {
        submitRequestBtn.click()
    }
}