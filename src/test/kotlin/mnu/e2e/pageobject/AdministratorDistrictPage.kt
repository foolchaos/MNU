package mnu.e2e.pageobject

import mnu.webdriverwrapper.element.InputField
import mnu.webdriverwrapper.element.OmniElement
import mnu.webdriverwrapper.element.TextContainer

class AdministratorDistrictPage {
    private val menuContainer: OmniElement = OmniElement("//*[@class='grid']")
    private val incidentDistrictIncidentIdInput: InputField = InputField("//*[@class='grid']//input[@id='incidentId']")
    private val incidentSecurityAmountInput: InputField = InputField("//*[@class='grid']//input[@id='securityNeeded']")
    private val incidentSecurityLvlFromInput: InputField = InputField("//*[@class='grid']//input[@id='levelFrom']")
    private val incidentSecurityLvlToInput: InputField = InputField("//*[@class='grid']//input[@id='levelTo']")
    private val submitBtn: OmniElement = OmniElement("//*[@class='grid']//input[contains(@class, 'button')]")
    private val backBtn: OmniElement = OmniElement("//*[@class='grid']//*[contains(@href, 'main')]")
    private val textPopupContainer: TextContainer = TextContainer("//*[contains(@class, 'flash_success') and not(@id='message')]")

    fun isLoaded(): Boolean = menuContainer.isVisible()

    fun setIncidentDistrictIncidentId(id: String) {
        incidentDistrictIncidentIdInput.setValue(id)
    }

    fun setIncidentSecurityAmount(amount: String) {
        incidentSecurityAmountInput.setValue(amount)
    }

    fun setIncidentSecurityLvlFrom(lvlFrom: String) {
        incidentSecurityLvlFromInput.setValue(lvlFrom)
    }

    fun setIncidentSecurityLvlTo(lvlTo: String) {
        incidentSecurityLvlToInput.setValue(lvlTo)
    }

    fun clickSubmitBtn() {
        submitBtn.click()
    }

    fun getPopupText(): String? {
        return textPopupContainer.getText()
    }

    fun clickBackBtn() {
        backBtn.click()
    }
}