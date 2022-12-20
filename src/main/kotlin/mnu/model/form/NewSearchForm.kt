package mnu.model.form

data class NewSearchForm(
    val incidentId: String = "",
    val result: String = "",
    val isNew: String = "", // 0 - No; 1 - Yes, by Id; 2 - Brand-new weapon
    val weaponId: String = "", //if isSynthesized == 1
    val weaponQuantity1: String = "",
    val weaponName: String = "", //this and all below if isSynthesized == 2
    val weaponType: String = "",
    val weaponDescription: String = "",
    val weaponPrice: String = "",
    val weaponLevel: String = "",
    val weaponQuantity2: String = ""
)