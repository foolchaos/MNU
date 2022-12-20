package mnu.controller

import mnu.model.form.*
import mnu.model.entity.request.Request
import mnu.model.entity.Experiment
import mnu.model.entity.ExperimentStatus
import mnu.model.entity.WeaponType
import mnu.model.entity.request.NewWeaponRequest
import mnu.model.entity.request.RequestStatus
import mnu.repository.*
import mnu.repository.employee.ScientistEmployeeRepository
import mnu.repository.request.NewWeaponRequestRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.security.Principal
import java.time.LocalDateTime

@Controller
@RequestMapping("/sci")
class ScientistController (
    val scientistEmployeeRepository: ScientistEmployeeRepository,
    val experimentRepository: ExperimentRepository,
    val weaponRepository: WeaponRepository,
    val newWeaponRequestRepository: NewWeaponRequestRepository
) : ApplicationController() {

    @GetMapping("/main")
    fun main(model: Model, principal: Principal): String {
        val user = userRepository?.findByLogin(principal.name)!!
        val currentScientistEmployee = employeeRepository?.findByUserId(user.id!!)
        val currentScientist = scientistEmployeeRepository.findByEmployeeId(currentScientistEmployee?.id!!)
        val experimentList = experimentRepository.findAllByExaminatorIdOrderByStatusAsc(currentScientist?.id!!)
        model.addAttribute("experiments", experimentList)
        return "scientists/sci__main.html"
    }

    @GetMapping("/experiment")
    fun experiment(model: Model, principal: Principal): String {
        val user = userRepository?.findByLogin(principal.name)
        val currentScientistLvl = employeeRepository?.findByUserId(user?.id!!)?.level
        val assistants = scientistEmployeeRepository.getAssistants(currentScientistLvl!!)
        model.addAttribute("assistants", assistants)
        if (!model.containsAttribute("form"))
            model.addAttribute("form", NewExperimentForm())
        return "/scientists/sci__new-experiment.html"
    }

    @PostMapping("/experiment")
    fun requestExperiment(
        @ModelAttribute form: NewExperimentForm, principal: Principal,
        redirect: RedirectAttributes
    ): String {
        val user = userRepository?.findByLogin(principal.name)
        val currentScientistEmployee = employeeRepository?.findByUserId(user?.id!!)
        val currentScientist = scientistEmployeeRepository.findByEmployeeId(currentScientistEmployee?.id!!)
        val assistant = form.assistantId?.let { assistantId ->
            val requestedAssistant = scientistEmployeeRepository.findByEmployeeId(assistantId)
            when {
                requestedAssistant == null -> {
                    redirect.addFlashAttribute("form", form)
                    redirect.addFlashAttribute("error", "Scientist does not exist.")
                    return "redirect:experiment"
                }
                requestedAssistant.employee!!.level!! >= currentScientist!!.employee!!.level!! -> {
                    redirect.addFlashAttribute("form", form)
                    redirect.addFlashAttribute("error", "Requested assistant's level is higher than yours.")
                    return "redirect:experiment"
                }
                else -> requestedAssistant
            }
        }

        experimentRepository.save(
            Experiment(
                form.title,
                form.description,
                currentScientist,
                assistant
            ).apply {
                this.status = ExperimentStatus.PENDING
                this.statusDate = LocalDateTime.now()
            })

        redirect.addFlashAttribute("status", "Request sent. Wait for supervisor's decision.")
        return "redirect:main"
    }

    fun reportAccessError(experimentId: Long, principal: Principal): String? {
        val user = userRepository?.findByLogin(principal.name)!!
        val currentScientistEmployee = employeeRepository?.findByUserId(user.id!!)
        val currentScientist = scientistEmployeeRepository.findByEmployeeId(currentScientistEmployee?.id!!)
        val experiment = experimentRepository.findById(experimentId)
        if (!experiment.isPresent)
            return "Experiment with such id does not exist."
        if (experiment.get().examinator != currentScientist)
            return "You are not allowed to write a report on experiment you did not conduct."
        if (experiment.get().assistant == currentScientist)
            return "Assistants are not allowed to write a report on experiments."

        return null
    }

    @GetMapping("/report")
    fun report(@RequestParam id: String, model: Model, principal: Principal): String {
        val error = reportAccessError(id.toLong(), principal)
        if (error == null) {
            model.addAttribute("form", NewReportForm())
            model.addAttribute("weapons", weaponRepository.findAll())
        } else
            model.addAttribute("error", error)

        return "scientists/sci__report.html"
    }

    @PostMapping("/report")
    fun addReport(@ModelAttribute form: NewReportForm, principal: Principal, redirect: RedirectAttributes): String {
        val error = reportAccessError(form.experimentId.toLong(), principal)
        if (error == null) {
            val experiment = experimentRepository.findById(form.experimentId.toLong())
            when (form.isSynthesized.toInt()) {
                0 -> {
                    experimentRepository.save(experiment.get().apply {
                        this.statusDate = LocalDateTime.now()
                        this.result = form.result
                        this.status = ExperimentStatus.FINISHED
                    })
                    redirect.addFlashAttribute("status", "Report submitted.")
                    return "redirect:main"
                }

                1 -> {
                    val possibleWeapon = weaponRepository.findById(form.weaponId.toLong())
                    return if (!possibleWeapon.isPresent) {
                        redirect.addFlashAttribute("form", form)
                        redirect.addFlashAttribute("error", "Such weapon does not exist.")
                        "redirect:report"
                    }
                    else {
                        val weapon = possibleWeapon.get()
                        weapon.quantity += form.weaponQuantity1.toLong()
                        weaponRepository.save(weapon)
                        experimentRepository.save(experiment.get().apply {
                            this.statusDate = LocalDateTime.now()
                            this.result = form.result
                            this.status = ExperimentStatus.FINISHED
                        })
                        redirect.addFlashAttribute("status", "Report submitted and weapon added to the arsenal.")
                        "redirect:main"

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
                            return "redirect:report"
                        }
                    }

                    when {
                        form.weaponLevel.toInt() < 1 || form.weaponLevel.toInt() > 10 -> {
                            redirect.addFlashAttribute("form", form)
                            redirect.addFlashAttribute("error", "Please enter weapon access level between 1-10.")
                            return "redirect:report"
                        }
                        form.weaponQuantity2.toLong() < 1 -> {
                            redirect.addFlashAttribute("form", form)
                            redirect.addFlashAttribute("error", "Please enter a valid quantity of this weapon.")
                            return "redirect:report"
                        }
                        form.weaponPrice.toDouble() < 1 -> {
                            redirect.addFlashAttribute("form", form)
                            redirect.addFlashAttribute("error", "Please enter a valid price for this weapon.")
                            return "redirect:report"
                        }
                    }

                    val newWeaponRequest = NewWeaponRequest(
                        form.weaponName, weaponType, form.weaponDescription,
                        form.weaponQuantity2.toLong(), form.weaponLevel.toInt(), form.weaponPrice.toDouble(), user
                    )

                    newWeaponRequestRepository.save(newWeaponRequest.apply { this.request = newRequest })
                    experimentRepository.save(experiment.get().apply {
                        this.statusDate = LocalDateTime.now()
                        this.result = form.result
                        this.status = ExperimentStatus.FINISHED
                    })
                    redirect.addFlashAttribute("status", "Report submitted. Await for supervisor's decision.")
                    return "redirect:main"

                }
            }

        } else {
            redirect.addFlashAttribute("form", form)
            redirect.addFlashAttribute("error", error)
            return "redirect:report"
        }
        return "redirect:report"
    }
}