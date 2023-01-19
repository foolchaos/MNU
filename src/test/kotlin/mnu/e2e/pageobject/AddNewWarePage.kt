package mnu.e2e.pageobject

import mnu.webdriverwrapper.element.InputField
import mnu.webdriverwrapper.element.OmniElement

enum class WareType(val strVal: String = "") {
    MELEE("melee"),
    PISTOL("pistol"),
    SUBMACHINE_GUN("submachine_gun"),
    ASSAULT_RIFLE("assault_rifle"),
    LIGHT_MACHINE_GUN("light_machine_gun"),
    SNIPER_RIFLE("sniper_rifle"),
    ALIEN("alien"),
    LAND("land"),
    AIR("air")
}

class AddNewWarePage {
    private val addNewWareFormContainer: OmniElement = OmniElement("//*[contains(@class,'content_form')]")
    private val addNewWarePageBtn: OmniElement = OmniElement("//*[contains(normalize-space(text()), 'Suggest product') and not(contains(@class, 'mobile'))]")
    private val addNewVacancyPageBtn: OmniElement = OmniElement("//*[contains(normalize-space(text()), 'Suggest vacancy') and not(contains(@class, 'mobile'))]")
    private val newWareNameInput: InputField = InputField("//*[contains(@class,'content_form')]//input[@id='name']")

    private val newWareTypeInput: InputField = InputField("//*[contains(@class,'content_form')]//select[@id='type']")
    private val newWareTypeOptionTemplate: String = "//*[contains(@class,'content_form')]//select[@id='type']//option[@value='{}']"

    private val newWareDescriptionInput: InputField = InputField("//*[contains(@class,'content_form')]//textarea[@id='description']")
    private val newWarePriceInput: InputField = InputField("//*[contains(@class,'content_form')]//input[@id='price']")
    private val newWareAccessLevelInput: InputField = InputField("//*[contains(@class,'content_form')]//input[@id='accessLvl']")
    private val newWareQuantityInput: InputField = InputField("//*[contains(@class,'content_form')]//input[@id='quantity']")
    private val newWareSubmitBtn: OmniElement = OmniElement("//*[contains(@class,'content_form')]//input[contains(@class, 'button')]")


    fun isLoaded(): Boolean {
        return addNewWareFormContainer.isVisible()
                && addNewWarePageBtn.getAttribute("class")!!.contains("pressed")
    }

    fun clickAddNewWarePageBtn() {
        addNewWarePageBtn.click()
    }

    fun clickAddNewVacancyPageBtn() {
        addNewVacancyPageBtn.click()
    }

    fun setNewWareName(name: String) {
        newWareNameInput.setValue(name)
    }

    fun setNewWareDescription(description: String) {
        newWareDescriptionInput.setValue(description)
    }

    fun setNewWarePrice(price: String) {
        newWarePriceInput.setValue(price)
    }

    fun setNewWareAccessLevel(accessLvl: String) {
        newWareAccessLevelInput.setValue(accessLvl)
    }

    fun setNewWareQuantity(quantity: String) {
        newWareQuantityInput.setValue(quantity)
    }

    fun setNewWareType(type: WareType) {
        newWareTypeInput.click()
        val element = OmniElement(newWareTypeOptionTemplate.format(type.strVal))
        element.clickJs()
    }

    fun clickSubmitBtn() {
        newWareSubmitBtn.click()
    }

}