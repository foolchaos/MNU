package mnu.repository

import mnu.model.DistrictHouse
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface DistrictHouseRepository : BaseRepository<DistrictHouse, Long> {
    @Query("select dt.id from district_houses dt;", nativeQuery = true)
    fun getAllIds(): List<Long>?

    fun existsByShelterColumnAndShelterRow(shelterColumn: Int, shelterRow: Int): Boolean

    fun findByShelterColumnAndShelterRow(shelterColumn: Int, shelterRow: Int): DistrictHouse?
}