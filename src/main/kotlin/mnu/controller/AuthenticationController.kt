package mnu.controller

import mnu.model.form.ClientRegistrationForm
import mnu.model.form.LoginForm
import mnu.model.entity.Client
import mnu.model.entity.ClientType
import mnu.model.entity.Role
import mnu.model.entity.User
import mnu.repository.ClientRepository
import mnu.repository.UserRepository
import mnu.repository.employee.ManagerEmployeeRepository
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import javax.servlet.http.HttpSession

@Controller
@RequestMapping("/auth")
class AuthenticationController(
    private val userRepository: UserRepository,
    private val clientRepository: ClientRepository,
    private val managerEmployeeRepository: ManagerEmployeeRepository
) {

    @GetMapping("/login")
    fun login(model: Model, session: HttpSession, redirect: RedirectAttributes): String {
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

    @GetMapping("/register")
    fun register(model: Model): String {
        model.addAttribute("form", ClientRegistrationForm())
        return "/register.html"
    }

    @PostMapping("/register")
    fun addUser(@ModelAttribute form: ClientRegistrationForm, redirect: RedirectAttributes): String {
        val existingUser = userRepository.findByLogin(form.username)
        val clients = clientRepository.findAll()
        val regex = """[a-zA-Z0-9_.]+""".toRegex()

        return if (!regex.matches(form.username) || !regex.matches(form.password)) {
            redirect.addFlashAttribute("form", form)
            redirect.addFlashAttribute("error", "Only latin letters, numbers, \"_\" and \".\" are supported.")
            "redirect:/auth/register"
        } else {
            if (form.username.length < 4) {
                redirect.addFlashAttribute("form", form)
                redirect.addFlashAttribute("error", "Username length should be at least 4 symbols.")
                return "redirect:/auth/register"
            }
            if (form.password.length < 6) {
                redirect.addFlashAttribute("form", form)
                redirect.addFlashAttribute("error", "Password length should be at least 6 symbols.")
                return "redirect:/auth/register"
            }
            val passwordEncoder = BCryptPasswordEncoder()
            val encodedPassword = passwordEncoder.encode(form.password)
            form.password = encodedPassword

            clients.forEach {
                if (form.email == it.email) {
                    redirect.addFlashAttribute("form", form)
                    redirect.addFlashAttribute("error", "Email '${form.email}' is already taken.")
                    return "redirect:/auth/register"
                }
            }
            return if (existingUser != null) {
                redirect.addFlashAttribute("form", form)
                redirect.addFlashAttribute("error", "Username '${form.username}' is already taken. Please try again.")
                "redirect:/auth/register"
            } else {
                val role = when (form.type) {
                    "customer" -> Role.CUSTOMER
                    "manufacturer" -> Role.MANUFACTURER
                    else -> return "Error"
                }
                val newUser = User(form.username, form.password, role)

                val clientType = when (form.type) {
                    "customer" -> ClientType.CUSTOMER
                    "manufacturer" -> ClientType.MANUFACTURER
                    else -> return "Error"
                }
                val newClientUser = Client(form.name, form.email, clientType).apply { this.user = newUser }

                val managerIdList = managerEmployeeRepository.getAllIds()

                if (clientType == ClientType.CUSTOMER)
                    newClientUser.apply {
                        this.manager = managerEmployeeRepository.findById(managerIdList.random()).get()
                    }

                userRepository.save(newUser)
                clientRepository.save(newClientUser)

                redirect.addFlashAttribute("form", form)
                redirect.addFlashAttribute("status", "Successfully registered. You may now login.")
                "redirect:/auth/login"
            }
        }
    }

}