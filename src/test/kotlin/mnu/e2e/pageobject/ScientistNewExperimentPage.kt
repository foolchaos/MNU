package mnu.e2e.pageobject

import mnu.webdriverwrapper.element.InputField
import mnu.webdriverwrapper.element.OmniElement

class ScientistNewExperimentPage {
    private val newExperimentForm: OmniElement = OmniElement("//*[@class='grid']")
    private val experimentTitleInputField: InputField = InputField("//*[@class='grid']//input[@id='title']")
    private val experimentAssistantInputField: InputField = InputField("//*[@class='grid']//input[@id='assistantId']")
    private val experimentDescriptionInputField: InputField =
        InputField("//*[@class='grid']//textarea[@id='description']")
    private val submitRequestBtn: OmniElement = OmniElement("//*[@class='grid']//input[contains(@class, 'button')]")

    fun isLoaded(): Boolean {
        return newExperimentForm.isVisible()
    }

    fun setExperimentTitle(title: String) {
        experimentTitleInputField.setValue(title)
    }

    fun setExperimentAssistantId(assistantId: String) {
        experimentAssistantInputField.setValue(assistantId)
    }

    fun setExperimentDescription(description: String) {
        experimentDescriptionInputField.setValue(description)
    }

    fun clickSubmitRequestBtn() {
        submitRequestBtn.click()
    }
}