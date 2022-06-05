package mnu.model.form

data class NewExperimentForm(
    val title: String = "",
    val assistantId: Long? = null,
    val description: String = ""
)