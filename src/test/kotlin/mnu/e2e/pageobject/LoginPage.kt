package mnu.e2e.pageobject

import mnu.webdriverwrapper.element.InputField
import mnu.webdriverwrapper.element.OmniElement

class LoginPage {
    private val loginForm: OmniElement = OmniElement("//form")
    private val loginInput: InputField = InputField("//input[@id='username']")
    private val passwordInput: InputField = InputField("//input[@id='password']")
    private val loginButton: OmniElement = OmniElement("//input[contains(@class, 'button')]")

    fun isLoaded(): Boolean {
        return loginForm.isVisible()
    }

    fun setLogin(login: String) {
        loginInput.setValue(login)
    }

    fun setPassword(password: String) {
        passwordInput.setValue(password)
    }

    fun clickLoginBtn() {
        loginButton.click()
    }
}