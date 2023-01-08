package mnu.webdriverwrapper.element

class InputField(locator: String) : BaseElement(locator) {
    fun setValue(value: String?) {
        if (isClickable() && isVisible()) {
            this.getWebElement()?.clear()
            this.getWebElement()?.sendKeys(value)
        }
    }
}