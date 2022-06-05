package mnu.controller

import mnu.model.form.*
import mnu.model.entity.Experiment
import mnu.model.entity.ExperimentStatus
import mnu.repository.*
import mnu.repository.employee.ScientistEmployeeRepository
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
    val experimentRepository: ExperimentRepository
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
}