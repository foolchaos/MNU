package mnu.model.form

data class ClientRegistrationForm(
    val username: String = "",
    var password: String = "",
    val email: String = "",
    val name: String = "",
    val type: String = ""
)