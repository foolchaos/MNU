package mnu

import mnu.model.DistrictHouse
import mnu.repository.DistrictHouseRepository
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class DbSeeder(
    private val districtHouseRepository: DistrictHouseRepository
) {
    @PostConstruct
    fun init() {
        val housesExist = districtHouseRepository.existsByShelterColumnAndShelterRow(1, 1)
        if (housesExist)
            return

        for (i in 0..14)
            for (j in 0..14)
                districtHouseRepository.save(DistrictHouse(i, j))
    }
}