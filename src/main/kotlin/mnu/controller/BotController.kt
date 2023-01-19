package mnu.controller

import mnu.model.entity.DistrictIncident
import mnu.repository.DistrictHouseRepository
import mnu.repository.DistrictIncidentRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/bot")
class BotController(
    val districtIncidentRepository: DistrictIncidentRepository,
    val districtHouseRepository: DistrictHouseRepository
) {
    data class NewIncident(
        var dangerLevel: Short = 0, var row: Int = 0, var column: Int = 0,
        var description: String = "", var appearanceTime: LocalDateTime = LocalDateTime.now()
    )

    @PostMapping("/reportIncident")
    @ResponseBody
    fun reportOnANewIncident(@RequestBody incident: NewIncident): ResponseEntity<String> {
        val house = districtHouseRepository.findByShelterColumnAndShelterRow(incident.column, incident.row)
            ?: return ResponseEntity.ok().body("Such house does not exist.")
        val newIncident = DistrictIncident(incident.dangerLevel, house, incident.description, incident.appearanceTime)
        districtIncidentRepository.save(newIncident)
        return ResponseEntity.ok().body("Report sent. Thank you for your goodwill!")
    }
}