package mnu.repository

import mnu.model.entity.DistrictHouse
import mnu.model.entity.DistrictIncident
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface DistrictIncidentRepository : BaseRepository<DistrictIncident, Long> {
    fun findAllByLevelToAndDangerLevelGreaterThan(level1: Int, dangerLevel: Short): List<DistrictIncident>?

    fun findAllByHouse(house: DistrictHouse): List<DistrictIncident>

    fun findAllByHouseAndDangerLevel(house: DistrictHouse, dangerLevel: Short): List<DistrictIncident>

    fun findAllByOrderByDangerLevelDesc(): List<DistrictIncident>?

    fun findAllByAvailablePlacesGreaterThanAndLevelFromLessThanEqualAndLevelToGreaterThanEqual(
        availablePlaces: Long,
        levelFrom: Int,
        levelTo: Int
    ): List<DistrictIncident>?

    fun findAllByAppearanceTimeAfterAndAppearanceTimeBefore(
        appearanceTime1: LocalDateTime,
        appearanceTime2: LocalDateTime
    ): List<DistrictIncident>?
}