package mnu.webdriverwrapper.element

class TextContainer(locator: String) : BaseElement(locator) {
    fun getText(): String? {
        return if (isVisible()) this.getWebElement()?.text else ""
    }
}