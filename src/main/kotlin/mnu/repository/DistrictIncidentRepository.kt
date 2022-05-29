package mnu.repository

import mnu.model.DistrictHouse
import mnu.model.DistrictIncident
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
        level1: Int,
        level2: Int
    ): List<DistrictIncident>?

    fun findAllByAppearanceTimeAfterAndAppearanceTimeBefore(
        appearanceTime1: LocalDateTime,
        appearanceTime2: LocalDateTime
    ): List<DistrictIncident>?
}