package mnu.model.form

data class NewProductForm(
    val name: String = "",
    val type: String = "",
    val description: String = "",
    val price: String = "",
    val accessLvl: String = "",
    val quantity: String = ""
)