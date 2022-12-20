package mnu.controller

import mnu.model.entity.DistrictIncident
import mnu.model.entity.Transport
import mnu.model.entity.Weapon
import mnu.model.entity.WeaponType
import mnu.model.entity.request.ChangeEquipmentRequest
import mnu.model.entity.request.NewWeaponRequest
import mnu.model.entity.request.Request
import mnu.model.entity.request.RequestStatus
import mnu.model.form.NewEquipmentForm
import mnu.model.form.NewSearchForm
import mnu.repository.DistrictIncidentRepository
import mnu.repository.TransportRepository
import mnu.repository.WeaponRepository
import mnu.repository.employee.SecurityEmployeeRepository
import mnu.repository.request.ChangeEquipmentRequestRepository
import mnu.repository.request.NewWeaponRequestRepository
import mnu.repository.request.RequestRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.security.Principal

@Controller
@RequestMapping("/sec")
class SecurityController(
    val securityEmployeeRepository: SecurityEmployeeRepository,
    val weaponRepository: WeaponRepository,
    val newWeaponRequestRepository: NewWeaponRequestRepository,
    val transportRepository: TransportRepository,
    val requestRepository: RequestRepository,
    val changeEquipmentRequestRepository: ChangeEquipmentRequestRepository,
    val districtIncidentRepository: DistrictIncidentRepository
) : ApplicationController() {

    @GetMapping("/main")
    fun securityMenu(model: Model, principal: Principal): String {
        val curUser = userRepository?.findByLogin(principal.name)!!
        val curSecurity = securityEmployeeRepository.findById(curUser.id!!).get()

        val allSuitableIncidents = districtIncidentRepository
            .findAllByAvailablePlacesGreaterThanAndLevelFromLessThanEqualAndLevelToGreaterThanEqual(
                0, curSecurity.employee!!.level!!, curSecurity.employee!!.level!!
            ) as MutableList<DistrictIncident>?
        var currentInc = DistrictIncident()
        allSuitableIncidents?.forEach {
            if (it.assistants!!.contains(curSecurity) && it.dangerLevel > 0) {
                currentInc = it
            }
        }

        allSuitableIncidents?.remove(currentInc)

        val allIncidents = districtIncidentRepository.findAllByOrderByDangerLevelDesc()
        val incidentsWithEmployee = ArrayList<DistrictIncident>()
        allIncidents?.forEach {
            if (it.assistants!!.contains(curSecurity))
                incidentsWithEmployee.add(it)
        }

//        model.addAttribute("curr_incident", currentIncId)
        model.addAttribute(
            "ongoing_incidents",
            allSuitableIncidents
        )
        model.addAttribute("incidents_with_employee", incidentsWithEmployee)

        return "security/sec__main.html"
    }

    @GetMapping("/equipment")
    fun securityEquipment(model: Model, principal: Principal): String {
        val curUser = userRepository?.findByLogin(principal.name)!!
        val curSecurity = securityEmployeeRepository.findById(curUser.id!!).get()

        val allChangeRequests = changeEquipmentRequestRepository.findAllByEmployee(curSecurity)
        var currentChangeRequest = ChangeEquipmentRequest()
        allChangeRequests?.forEach {
            if (it.request!!.status == RequestStatus.PENDING) {
                currentChangeRequest = it
            }
        }

        val allAvailableWeapons =
            weaponRepository.findAllByRequiredAccessLvlLessThanEqualAndQuantityGreaterThanOrderByIdAsc(
                curSecurity.employee!!.level!!,
                0
            )
                    as MutableList<Weapon>

        val allAvailableTransport =
            transportRepository.findAllByRequiredAccessLvlLessThanEqualAndQuantityGreaterThanOrderByIdAsc(
                curSecurity.employee!!.level!!,
                0
            )
                    as MutableList<Transport>

        model.addAttribute("current_security", curSecurity)
        model.addAttribute("current_request", currentChangeRequest)
        model.addAttribute("available_weapons", allAvailableWeapons)
        model.addAttribute("available_transport", allAvailableTransport)
        model.addAttribute("form", NewEquipmentForm())
        return "security/sec__equipment-change"
    }

    @PostMapping("/equipment")
    fun requestNewEquipment(
        @ModelAttribute form: NewEquipmentForm, principal: Principal,
        redirect: RedirectAttributes
    ): String {
        val user = userRepository?.findByLogin(principal.name)
        val newRequest = Request().apply { this.status = RequestStatus.PENDING }
        val currentSecurity = securityEmployeeRepository.findById(user?.id!!).get()
        val allChangeRequests = changeEquipmentRequestRepository.findAllByEmployee(currentSecurity)

        allChangeRequests?.forEach {
            if (it.request!!.status == RequestStatus.PENDING) {
                redirect.addFlashAttribute("form", form)
                redirect.addFlashAttribute("error", "You cannot have more than 1 pending equipment change request.")
                return "redirect:/sec/equipment"
            }
        }

        val requestedWeapon = when (form.weaponId) {
            null, 0L -> null
            else -> weaponRepository.findById(form.weaponId)
        }
        val requestedTransport = when (form.transportId) {
            null, 0L -> null
            else -> transportRepository.findById(form.transportId)
        }

        when {
            requestedWeapon != null && !requestedWeapon.isPresent -> {
                redirect.addFlashAttribute("form", form)
                redirect.addFlashAttribute("error", "Such weapon does not exist.")
                return "redirect:/sec/equipment"
            }

            requestedTransport != null && !requestedTransport.isPresent -> {
                redirect.addFlashAttribute("form", form)
                redirect.addFlashAttribute("error", "Such transport does not exist.")
                return "redirect:/sec/equipment"
            }

            requestedTransport != null && requestedWeapon != null && !requestedTransport.isPresent && !requestedWeapon.isPresent -> {
                redirect.addFlashAttribute("form", form)
                redirect.addFlashAttribute("error", "Such weapon and transport do not exist.")
                return "redirect:/sec/equipment"
            }
        }


        var existingWeapon = Weapon()
        if (requestedWeapon != null)
            existingWeapon = requestedWeapon.get()
        var existingTransport = Transport()
        if (requestedTransport != null)
            existingTransport = requestedTransport.get()

        when {
            existingWeapon == currentSecurity.weapon && existingTransport == currentSecurity.transport -> {
                redirect.addFlashAttribute("form", form)
                redirect.addFlashAttribute("error", "Requested equipment is the same as yours.")
                return "redirect:/sec/equipment"
            }

            existingWeapon.quantity == 0L && existingWeapon.id != null -> {
                redirect.addFlashAttribute("form", form)
                redirect.addFlashAttribute("error", "Out of stock. Check back later or choose another weapon.")
                return "redirect:/sec/equipment"
            }

            existingTransport.quantity == 0L && existingTransport.id != null -> {
                redirect.addFlashAttribute("form", form)
                redirect.addFlashAttribute("error", "Out of stock. Check back later or choose another transport.")
                return "redirect:/sec/equipment"
            }

            existingWeapon.requiredAccessLvl > currentSecurity.employee!!.level!! && existingWeapon.id != null -> {
                redirect.addFlashAttribute("form", form)
                redirect.addFlashAttribute("error", "Requested weapon's access level is higher than yours.")
                return "redirect:/sec/equipment"
            }

            existingTransport.requiredAccessLvl > currentSecurity.employee!!.level!! && existingTransport.id != null -> {
                redirect.addFlashAttribute("form", form)
                redirect.addFlashAttribute("error", "Requested transport's access level is higher than yours.")
                return "redirect:/sec/equipment"
            }
        }

        requestRepository.save(newRequest)
        when {
            requestedWeapon == null && requestedTransport == null -> {
                changeEquipmentRequestRepository.save(
                    ChangeEquipmentRequest(currentSecurity, requestedWeapon, requestedTransport)
                        .apply { this.request = newRequest }
                )
            }

            requestedWeapon == null && requestedTransport != null -> {
                changeEquipmentRequestRepository.save(
                    ChangeEquipmentRequest(currentSecurity, requestedWeapon, existingTransport)
                        .apply { this.request = newRequest }
                )
            }

            requestedWeapon != null && requestedTransport == null -> {
                changeEquipmentRequestRepository.save(
                    ChangeEquipmentRequest(currentSecurity, existingWeapon, requestedTransport)
                        .apply { this.request = newRequest }
                )
            }

            requestedWeapon != null && requestedTransport != null -> {
                changeEquipmentRequestRepository.save(
                    ChangeEquipmentRequest(currentSecurity, existingWeapon, existingTransport)
                        .apply { this.request = newRequest }
                )
            }
        }

        redirect.addFlashAttribute("status", "Request sent. Wait for supervisor's decision.")
        return "redirect:/sec/main"
    }

    @PostMapping("/incident/{id}")
    fun acceptIncidentParticipation(
        @PathVariable id: Long,
        redirect: RedirectAttributes,
        principal: Principal
    ): String {
        val curUser = userRepository?.findByLogin(principal.name)!!
        val curSecurity = securityEmployeeRepository.findById(curUser.id!!).get()
        val curEmployeeLevel = employeeRepository?.findByUserId(curUser.id!!)!!.level!!
        val possibleIncident = districtIncidentRepository.findById(id)
        if (!possibleIncident.isPresent) {
            return "Incident with such id does not exist."
        }
        val incident = possibleIncident.get()

        var incidentAssistants = incident.assistants
        if (incidentAssistants == null)
            incidentAssistants = ArrayList()

        val allSuitableIncidents =
            districtIncidentRepository.findAllByAvailablePlacesGreaterThanAndLevelFromLessThanEqualAndLevelToGreaterThanEqual(
                0, curEmployeeLevel, curEmployeeLevel
            )
        when {
            incidentAssistants.contains(curSecurity) -> {
                redirect.addFlashAttribute("error", "You are already appointed to this incident.")
                return "redirect:/sec/main"
            }

            incident.availablePlaces == 0L -> {
                redirect.addFlashAttribute("error", "The amount of security is already sufficient for this incident.")
                return "redirect:/sec/main"
            }

            !allSuitableIncidents?.contains(incident)!! -> {
                redirect.addFlashAttribute("error", "You are not suitable for this incident.")
                return "redirect:/sec/main"
            }
        }
        allSuitableIncidents?.forEach {
            if (it.assistants!!.contains(curSecurity)) {
                redirect.addFlashAttribute("error", "You are already appointed for an incident #${it.id}.")
                return "redirect:/sec/main"
            }
        }
        incidentAssistants.add(securityEmployeeRepository.findById(curUser.id!!).get())

        incident.apply {
            this.assistants = incidentAssistants
            this.availablePlaces = this.availablePlaces - 1
        }

        districtIncidentRepository.save(incident)
        if (incident.availablePlaces == 0L) {
            redirect.addFlashAttribute(
                "status",
                "You were appointed to the incident and have successfully resolved it."
            )
            return "redirect:/sec/main"
        }

        redirect.addFlashAttribute("status", "You were appointed to the incident.")
        return "redirect:/sec/main"
    }

    fun searchReportAccessError(incidentId: Long, principal: Principal): String? {
        val user = userRepository?.findByLogin(principal.name)
        val possibleSecurity = securityEmployeeRepository.findById(user?.id!!)
        if (!possibleSecurity.isPresent)
            return "You are not a security employee."
        val currentSecurity = possibleSecurity.get()
        val incident = districtIncidentRepository.findById(incidentId)
        if (!incident.isPresent)
            return "Incident with such id does not exist."
        if (incident.get().availablePlaces > 0)
            return "You can not write a report on an incident which has not been resolved yet."
        if (!incident.get().assistants!!.contains(currentSecurity))
            return "You are not allowed to write a report on incident you did not participate in."

        return null
    }

    @GetMapping("/report")
    fun searchReport(@RequestParam id: String, model: Model, principal: Principal): String {
        val error = searchReportAccessError(id.toLong(), principal)
        if (error == null) {
            model.addAttribute("form", NewSearchForm())
            model.addAttribute("weapons", weaponRepository.findAll())
        } else
            model.addAttribute("error", error)

        return "security/sec__report.html"
    }

    @PostMapping("/report")
    fun addSearchReport(
        @ModelAttribute form: NewSearchForm,
        principal: Principal,
        redirect: RedirectAttributes
    ): String {
        val error = searchReportAccessError(form.incidentId.toLong(), principal)
        if (error == null) {
            val incident = districtIncidentRepository.findById(form.incidentId.toLong())
            when (form.isNew.toInt()) {
                0 -> {
                    districtIncidentRepository.save(incident.get().apply {
                        this.description += "\n\n${form.result}"
                        this.dangerLevel = 0
                        this.levelFrom = 0
                        this.levelTo = 0
                    })
                    redirect.addFlashAttribute("status", "Report submitted.")
                    return "redirect:/sec/main"
                }

                1 -> {
                    val possibleWeapon = weaponRepository.findById(form.weaponId.toLong())
                    return if (!possibleWeapon.isPresent) {
                        redirect.addFlashAttribute("form", form)
                        redirect.addFlashAttribute("error", "Such weapon does not exist.")
                        "redirect:/sec/report"
                    } else {
                        val weapon = possibleWeapon.get()
                        weapon.quantity += form.weaponQuantity1.toLong()
                        weaponRepository.save(weapon)
                        districtIncidentRepository.save(incident.get().apply {
                            this.description += "\n\n${form.result}"
                            this.dangerLevel = 0
                            this.levelFrom = 0
                            this.levelTo = 0
                        })
                        redirect.addFlashAttribute("status", "Report submitted and weapon added to the arsenal.")
                        "redirect:/sec/main"

                    }
                }

                2 -> {
                    val user = userRepository?.findByLogin(principal.name)
                    val newRequest = Request().apply { this.status = RequestStatus.PENDING }
                    val weaponType = when (form.weaponType) {
                        "melee" -> WeaponType.MELEE
                        "pistol" -> WeaponType.PISTOL
                        "submachine_gun" -> WeaponType.SUBMACHINE_GUN
                        "assault_rifle" -> WeaponType.ASSAULT_RIFLE
                        "light_machine_gun" -> WeaponType.LIGHT_MACHINE_GUN
                        "sniper_rifle" -> WeaponType.SNIPER_RIFLE
                        "alien" -> WeaponType.ALIEN
                        else -> {
                            redirect.addFlashAttribute("form", form)
                            redirect.addFlashAttribute("error", "Such weapon type does not exist.")
                            return "redirect:/sec/report"
                        }
                    }

                    when {
                        form.weaponLevel.toInt() < 1 || form.weaponLevel.toInt() > 10 -> {
                            redirect.addFlashAttribute("form", form)
                            redirect.addFlashAttribute("error", "Please enter weapon access level between 1-10.")
                            return "redirect:/sec/report"
                        }

                        form.weaponQuantity2.toLong() < 1 -> {
                            redirect.addFlashAttribute("form", form)
                            redirect.addFlashAttribute("error", "Please enter a valid quantity of this weapon.")
                            return "redirect:/sec/report"
                        }

                        form.weaponPrice.toDouble() < 1 -> {
                            redirect.addFlashAttribute("form", form)
                            redirect.addFlashAttribute("error", "Please enter a valid price for this weapon.")
                            return "redirect:/sec/report"
                        }
                    }

                    val newWeaponRequest = NewWeaponRequest(
                        form.weaponName, weaponType, form.weaponDescription,
                        form.weaponQuantity2.toLong(), form.weaponLevel.toInt(), form.weaponPrice.toDouble(), user
                    )

                    newWeaponRequestRepository.save(newWeaponRequest.apply { this.request = newRequest })
                    districtIncidentRepository.save(incident.get().apply {
                        this.description += "\n\n${form.result}"
                        this.dangerLevel = 0
                        this.levelFrom = 0
                        this.levelTo = 0
                    })
                    redirect.addFlashAttribute("status", "Report submitted. Await for supervisor's decision.")
                    return "redirect:/sec/main"

                }
            }

        } else {
            redirect.addFlashAttribute("form", form)
            redirect.addFlashAttribute("error", error)
            return "redirect:/sec/report"
        }
        return "redirect:/sec/report"
    }

    @PostMapping("/withdrawChange")
    fun withdrawApplication(principal: Principal, redirect: RedirectAttributes): String {
        val user = userRepository?.findByLogin(principal.name)
        val currentSecurity = securityEmployeeRepository.findById(user?.id!!).get()
        val allChangeRequests = changeEquipmentRequestRepository.findAllByEmployee(currentSecurity)

        allChangeRequests?.forEach {
            if (it.request!!.status == RequestStatus.PENDING) {
                it.request!!.status = RequestStatus.REJECTED
                changeEquipmentRequestRepository.save(it)
                redirect.addFlashAttribute("status", "Request withdrawn.")
                return "redirect:/sec/equipment"
            }
        }

        redirect.addFlashAttribute("error", "You have no active equipment change requests.")
        return "redirect:/sec/equipment"
    }

    @PostMapping("/withdrawParticipation")
    fun withdrawParticipation(principal: Principal, redirect: RedirectAttributes): String {
        val user = userRepository?.findByLogin(principal.name)
        val currentSecurity = securityEmployeeRepository.findById(user?.id!!).get()

        val allSuitableIncidents = districtIncidentRepository
            .findAllByAvailablePlacesGreaterThanAndLevelFromLessThanEqualAndLevelToGreaterThanEqual(
                0, currentSecurity.employee!!.level!!, currentSecurity.employee!!.level!!
            ) as MutableList<DistrictIncident>?
        allSuitableIncidents?.forEach {
            if (it.assistants!!.contains(currentSecurity) && it.dangerLevel > 0) {
                it.assistants!!.remove(currentSecurity)
                it.availablePlaces++
                districtIncidentRepository.save(it)
                redirect.addFlashAttribute("status", "Participation withdrawn.")
                return "redirect:/sec/main"
            }
        }

        redirect.addFlashAttribute(
            "error",
            "You do not participate in any district incident resolving operations right now."
        )
        return "redirect:/sec/main"
    }

}