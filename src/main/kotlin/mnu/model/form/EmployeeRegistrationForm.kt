package mnu.model.form

data class EmployeeRegistrationForm(
    val username: String = "",
    var password: String = "",
    val name: String = "",
    val type: String = "",
    val position: String = "",
    val salary: String = "",
    val level: String = ""
)