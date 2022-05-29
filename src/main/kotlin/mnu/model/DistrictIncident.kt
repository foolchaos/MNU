package mnu.model

import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table
import javax.validation.constraints.*

@Entity
@Table(name = "district_incidents")
class DistrictIncident(
    @Column(nullable = false)
    @Min(0) @Max(3)
    var dangerLevel: Short = 0,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "house_id", referencedColumnName = "id")
    var house: DistrictHouse? = null,

    var description: String = "",
    var appearanceTime: LocalDateTime = LocalDateTime.now()

) : BaseEntity<Long>()