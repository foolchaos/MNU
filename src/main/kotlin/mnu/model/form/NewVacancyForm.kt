package mnu.model.form

data class NewVacancyForm (
    val title: String = "",
    val salary: String = "",
    val requiredKarma: String = "",
    val workHoursPerWeek: String = "",
    val vacantPlaces: String = ""
)