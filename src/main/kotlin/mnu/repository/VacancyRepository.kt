package mnu.repository

import mnu.model.entity.Vacancy
import org.springframework.data.jpa.repository.*

interface VacancyRepository : JpaRepository<Vacancy, Long> {
    fun findAllByRequiredKarmaLessThanEqual(karma: Long) : List<Vacancy>

    fun findAllByVacantPlacesGreaterThanEqual(places: Long) : List<Vacancy>

    fun findAllByOrderBySalaryAsc() : List<Vacancy>?

    fun findAllByOrderBySalaryDesc() : List<Vacancy>?

    fun findAllByOrderByWorkHoursPerWeekAsc() : List<Vacancy>?

    fun findAllByOrderByWorkHoursPerWeekDesc() : List<Vacancy>?

    fun findAllByOrderByRequiredKarmaAsc() : List<Vacancy>?

    fun findAllByOrderByRequiredKarmaDesc() : List<Vacancy>?
}