package mnu.controller

import mnu.model.form.LoginForm
import mnu.model.form.NewEmailForm
import mnu.model.form.NewPasswordForm
import mnu.model.entity.Role
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.security.Principal
import javax.servlet.http.HttpSession

@Controller
class PageController : ApplicationController() {

    @GetMapping("/username")
    @ResponseBody
    fun currentUsername(principal: Principal): String {
        return principal.name
    }

    @GetMapping("/")
    fun home(model: Model, session: HttpSession, redirect: RedirectAttributes): String {
        val authentication = SecurityContextHolder.getContext().authentication
        val roles = AuthorityUtils.authorityListToSet(authentication.authorities)
        if (authentication != null && authentication.isAuthenticated) {
            if (roles.contains("ADMIN"))
                return "redirect:/admin/main"
            if (roles.contains("MANAGER"))
                return "redirect:/man/main"
            if (roles.contains("SCIENTIST"))
                return "redirect:/sci/main"
            if (roles.contains("SECURITY"))
                return "redirect:/sec/main"
            if (roles.contains("CUSTOMER"))
                return "redirect:/client/shop/weapon"
            if (roles.contains("MANUFACTURER"))
                return "redirect:/manufacturer/market/weapon"
            if (roles.contains("PRAWN"))
                return "redirect:/prawn/main"
        }
        model.addAttribute("form", LoginForm())
        if (session.getAttribute("loginFailed") == true) {
            session.removeAttribute("loginFailed")
            model.addAttribute("loginFailed", true)
        }
        return "/login.html"
    }


    @GetMapping("/profile")
    fun profile(model: Model, principal: Principal): String {
        val currentEmployee = employeeRepository?.findByUserId(userRepository?.findByLogin(principal.name)!!.id!!)
        model.addAttribute("user", currentEmployee)
        model.addAttribute("form", NewPasswordForm())
        return "profile.html"
    }

    @GetMapping("/profileClient")
    fun profileClient(model: Model, principal: Principal): String {
        val currentClient = clientRepository?.findByUserId(userRepository?.findByLogin(principal.name)!!.id!!)
        model.addAttribute("user", currentClient)
        model.addAttribute("form_password", NewPasswordForm())
        model.addAttribute("form_email", NewEmailForm())
        return "profile_client.html"
    }

    @PostMapping("/changePass")
    fun changePass(@ModelAttribute form: NewPasswordForm, principal: Principal, redirect: RedirectAttributes): String {
        val curUser = userRepository?.findByLogin(principal.name)!!
        val regex = """[a-zA-Z0-9_.]+""".toRegex()
        val passwordEncoder = BCryptPasswordEncoder()
        when {
            !regex.matches(form.newPass) -> {
                redirect.addFlashAttribute("form", form)
                redirect.addFlashAttribute("error", "Only latin letters, numbers, \"_\" and \".\" are supported.")
                return when (curUser.role) {
                    Role.PRAWN -> "redirect:/prawn/profile"
                    Role.CUSTOMER, Role.MANUFACTURER -> "redirect:/profileClient"
                    Role.ADMIN, Role.MANAGER, Role.SECURITY, Role.SCIENTIST -> "redirect:/profile"
                    else -> "redirect:/auth/login"
                }
            }
            form.prevPass == "" || form.newPass == "" -> {
                redirect.addFlashAttribute("form", form)
                redirect.addFlashAttribute("error", "One of the fields is empty. Please fill all fields.")
                return when (curUser.role) {
                    Role.PRAWN -> "redirect:/prawn/profile"
                    Role.CUSTOMER, Role.MANUFACTURER -> "redirect:/profileClient"
                    Role.ADMIN, Role.MANAGER, Role.SECURITY, Role.SCIENTIST -> "redirect:/profile"
                    else -> "redirect:/auth/login"
                }
            }
            !passwordEncoder.matches(form.prevPass, curUser.password) -> {
                redirect.addFlashAttribute("form", form)
                redirect.addFlashAttribute("error", "Previous password is incorrect. Please try again.")
                return when (curUser.role) {
                    Role.PRAWN -> "redirect:/prawn/profile"
                    Role.CUSTOMER, Role.MANUFACTURER -> "redirect:/profileClient"
                    Role.ADMIN, Role.MANAGER, Role.SECURITY, Role.SCIENTIST -> "redirect:/profile"
                    else -> "redirect:/auth/login"
                }
            }
        }
        curUser.password = passwordEncoder.encode(form.newPass)
        userRepository?.save(curUser)

        redirect.addFlashAttribute("form", form)
        redirect.addFlashAttribute("status", "Password changed successfully.")
        return when (curUser.role) {
            Role.PRAWN -> "redirect:/prawn/profile"
            Role.CUSTOMER, Role.MANUFACTURER -> "redirect:/profileClient"
            Role.ADMIN, Role.MANAGER, Role.SECURITY, Role.SCIENTIST -> "redirect:/profile"
            else -> "redirect:/auth/login"
        }
    }

    @PostMapping("/changeEmail")
    fun changeEmail(@ModelAttribute form: NewEmailForm, principal: Principal, redirect: RedirectAttributes): String {
        val curUser = userRepository?.findByLogin(principal.name)!!
        val currentClient = clientRepository?.findByUserId(curUser.id!!)!!
        if (form.newEmail == "") {
            redirect.addFlashAttribute("form", form)
            redirect.addFlashAttribute("error", "Field is empty.")
            return "redirect:/profileClient"
        }
        currentClient.email = form.newEmail
        clientRepository?.save(currentClient)
        redirect.addFlashAttribute("form", form)
        redirect.addFlashAttribute("status", "Email changed successfully.")
        return "redirect:/profileClient"
    }
}