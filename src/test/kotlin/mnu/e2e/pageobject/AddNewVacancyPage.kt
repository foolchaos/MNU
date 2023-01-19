package mnu.e2e.pageobject

import mnu.webdriverwrapper.element.InputField
import mnu.webdriverwrapper.element.OmniElement

class AddNewVacancyPage {
    private val addNewWareFormContainer: OmniElement = OmniElement("//*[contains(@class,'content_form')]")
    private val addNewWarePageBtn: OmniElement = OmniElement("//*[contains(normalize-space(text()), 'Suggest product') and not(contains(@class, 'mobile'))]")
    private val addNewVacancyPageBtn: OmniElement = OmniElement("//*[contains(normalize-space(text()), 'Suggest vacancy') and not(contains(@class, 'mobile'))]")

    private val newVacancyTitleInput: InputField = InputField("//*[contains(@class,'content_form')]//input[@id='title']")
    private val newVacancySalaryInput: InputField = InputField("//*[contains(@class,'content_form')]//input[@id='salary']")
    private val newVacancyKarmaInput: InputField = InputField("//*[contains(@class,'content_form')]//input[@id='requiredKarma']")
    private val newVacancyWorkHoursInput: InputField = InputField("//*[contains(@class,'content_form')]//input[@id='workHoursPerWeek']")
    private val newVacancyVacantPlacesInput: InputField = InputField("//*[contains(@class,'content_form')]//input[@id='vacantPlaces']")
    private val newVacancySubmitBtn: OmniElement = OmniElement("//*[contains(@class,'content_form')]//input[contains(@class, 'button')]")


    fun isLoaded(): Boolean = addNewWareFormContainer.isVisible()
            && addNewVacancyPageBtn.getAttribute("class")!!.contains("pressed")

    fun clickAddNewWarePageBtn() {
        addNewWarePageBtn.click()
    }

    fun clickAddNewVacancyPageBtn() {
        addNewVacancyPageBtn.click()
    }

    fun setNewVacancyTitle(title: String) {
        newVacancyTitleInput.setValue(title)
    }

    fun setNewVacancySalary(salary: String) {
        newVacancySalaryInput.setValue(salary)
    }

    fun setNewVacancyKarma(karma: String) {
        newVacancyKarmaInput.setValue(karma)
    }

    fun setNewVacancyWorkHours(workHours: String) {
        newVacancyWorkHoursInput.setValue(workHours)
    }

    fun setNewVacancyVacantPlaces(vacantPlaces: String) {
        newVacancyVacantPlacesInput.setValue(vacantPlaces)
    }

    fun clickSubmitBtn() {
        newVacancySubmitBtn.click()
    }

}