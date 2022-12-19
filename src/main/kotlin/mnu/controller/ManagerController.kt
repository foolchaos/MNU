package mnu.controller

import mnu.EmailSender
import mnu.model.entity.Prawn
import mnu.model.entity.request.RequestStatus
import mnu.model.entity.Role
import mnu.model.entity.User
import mnu.model.entity.shop.ShoppingCartStatus
import mnu.model.entity.request.PurchaseRequest
import mnu.model.form.PrawnRegistrationForm
import mnu.repository.DistrictHouseRepository
import mnu.repository.TransportRepository
import mnu.repository.WeaponRepository
import mnu.repository.employee.ManagerEmployeeRepository
import mnu.repository.request.*
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.security.Principal
import java.time.LocalDateTime

@Controller
@RequestMapping("/man")
class ManagerController (
    val managerEmployeeRepository: ManagerEmployeeRepository,
    val requestRepository: RequestRepository,
    val weaponRepository: WeaponRepository,
    val transportRepository: TransportRepository,
    val purchaseRequestRepository: PurchaseRequestRepository,
    val districtHouseRepository: DistrictHouseRepository,
    val emailSender: EmailSender
): ApplicationController() {

    @GetMapping("/main")
    fun manMenu(model: Model, principal: Principal): String {
        val pendingRequests = requestRepository.findAllByStatus(RequestStatus.PENDING)

        val user = userRepository?.findByLogin(principal.name)!!
        val curManager = managerEmployeeRepository.findById(user.id!!).get()
        val purchaseRequests = purchaseRequestRepository.findAll()
        val pPendingRequests = ArrayList<PurchaseRequest>()
        purchaseRequests.forEach {
            val requestUser = it.user!!
            if (it.request!!.status == RequestStatus.PENDING) {
                if (requestUser.role == Role.PRAWN) {
                    if (prawnRepository?.findByUserId(requestUser.id!!)!!.manager == curManager)
                        pPendingRequests.add(it)
                } else if (requestUser.role == Role.CUSTOMER) {
                    if (clientRepository?.findByUserId(requestUser.id!!)!!.manager == curManager)
                        pPendingRequests.add(it)
                }
            }
        }

        model.addAttribute("purch_count", pPendingRequests.size)
        return "managers/manager__main.html"
    }

    @GetMapping("/clients")
    fun manClients(principal: Principal, model: Model): String {
        val user = userRepository?.findByLogin(principal.name)!!
        val curManager = managerEmployeeRepository.findById(user.id!!).get()
        model.addAttribute("clients", clientRepository?.findAllByManagerOrderByIdAsc(curManager))
        return "managers/manager__client-list.html"
    }

    @GetMapping("/prawns")
    fun manPrawns(principal: Principal, model: Model): String {
        val user = userRepository?.findByLogin(principal.name)!!
        val curManager = managerEmployeeRepository.findById(user.id!!).get()
        model.addAttribute("clients", prawnRepository?.findAllByManagerOrderByIdAsc(curManager))
        return "managers/manager__prawn-list.html"
    }

    @GetMapping("/purchaseRequests")
    fun manPurchaseRequests(principal: Principal, model: Model) : String {
        val user = userRepository?.findByLogin(principal.name)!!
        val curManager = managerEmployeeRepository.findById(user.id!!).get()

        val purchaseRequests = purchaseRequestRepository.findAll()
        val validPurchRequests = ArrayList<PurchaseRequest>()
        purchaseRequests.forEach {
            val requestUser = it.user!!
            if (it.request!!.status == RequestStatus.PENDING) {
                if (requestUser.role == Role.PRAWN) {
                    if (prawnRepository?.findByUserId(requestUser.id!!)!!.manager == curManager)
                        validPurchRequests.add(it)
                } else if (requestUser.role == Role.CUSTOMER) {
                    if (clientRepository?.findByUserId(requestUser.id!!)!!.manager == curManager)
                        validPurchRequests.add(it)
                }
            }
        }
        model.addAttribute("requests", validPurchRequests)
        return "/managers/manager__purchase-requests.html"
    }

    fun purchaseReqChoiceError(purchaseRequestId: Long, principal: Principal): String? {
        val request = purchaseRequestRepository.findById(purchaseRequestId)
        if (!request.isPresent)
            return "Request with such id does not exist."
        return null
    }

    @PostMapping("/acceptPurchaseRequest/{id}")
    fun acceptPurchaseRequest(@PathVariable id: Long, principal: Principal, redirect: RedirectAttributes): String {
        val user = userRepository?.findByLogin(principal.name)
        val currentManager = employeeRepository?.findById(user?.id!!)?.get()

        val error = purchaseReqChoiceError(id, principal)
        return if (error == null) {
            val checkedRequest = purchaseRequestRepository.findById(id).get()
            val userRequestRole = checkedRequest.user!!.role

            if (checkedRequest.request!!.status != RequestStatus.PENDING) {
                redirect.addFlashAttribute("error", "Request has already been handled.")
                return "redirect:/man/purchaseRequests"
            }
            when (userRequestRole) {
                Role.CUSTOMER -> {
                    val curCustomer = clientRepository?.findByUserId(checkedRequest.user!!.id!!)
                    if (curCustomer!!.manager != managerEmployeeRepository.findById(currentManager!!.id!!).get()) {
                        redirect.addFlashAttribute("error", "You are not this client's supervising manager.")
                        return "redirect:/man/purchaseRequests"
                    }

                    checkedRequest.request!!.apply {
                        this.statusDate = LocalDateTime.now()
                        this.status = RequestStatus.ACCEPTED
                        this.resolver = currentManager
                    }
                    checkedRequest.cart!!.status = ShoppingCartStatus.RETRIEVED
                    purchaseRequestRepository.save(checkedRequest)

                    var cartContents = ""
                    checkedRequest.cart!!.items!!.forEach {
                        cartContents += "\n${it.name()} - ${it.quantity} pieces"
                    }

                    emailSender.sendMessage(
                        curCustomer.email,
                        "Request id#${checkedRequest.id} accepted",
                        "Your purchase request (id #${checkedRequest.id}) has been accepted.\n" +
                                "Cart items are as follows:\n$cartContents\n\n" +
                                "Please contact us at +1-800-MNU-COOL for payment and delivery discussions."
                    )

                    // todo idk if its working still have to test yet
                    redirect.addFlashAttribute("status", "Request accepted.")
                    return "redirect:/man/purchaseRequests"
                }

                Role.MANUFACTURER -> {
                    redirect.addFlashAttribute("error", "You cannot process manufacturers' requests.")
                    return "redirect:/man/purchaseRequests"
                }

                Role.PRAWN -> {
                    val reqPrawn = prawnRepository?.findByUserId(checkedRequest.user!!.id!!)
                    if (reqPrawn!!.manager != managerEmployeeRepository.findById(currentManager!!.id!!).get()) {
                        redirect.addFlashAttribute("error", "You are not this prawn's supervising manager.")
                        return "redirect:/man/purchaseRequests"
                    }

                    checkedRequest.request!!.apply {
                        this.statusDate = LocalDateTime.now()
                        this.status = RequestStatus.ACCEPTED
                        this.resolver = currentManager
                    }
                    checkedRequest.cart!!.items!!.forEach {
                        reqPrawn.balance -= it.price() * it.quantity!!
                    }
                    prawnRepository?.save(reqPrawn)
                    checkedRequest.cart!!.status = ShoppingCartStatus.RETRIEVED
                    purchaseRequestRepository.save(checkedRequest)

                    redirect.addFlashAttribute("status", "Request accepted.")
                    return "redirect:/man/purchaseRequests"
                }

                else -> {
                    redirect.addFlashAttribute("error", "Error. Wrong request credentials.")
                    return "redirect:/man/purchaseRequests"
                }
            }


        } else {
            redirect.addFlashAttribute("error", error)
            "redirect:/man/purchaseRequests"
        }
    }

    @PostMapping("/rejectPurchaseRequest/{id}")
    fun rejectPurchaseRequest(@PathVariable id: Long, principal: Principal, redirect: RedirectAttributes): String {
        val user = userRepository?.findByLogin(principal.name)
        val currentManager = employeeRepository?.findById(user?.id!!)?.get()

        val error = purchaseReqChoiceError(id, principal)
        return if (error == null) {
            val checkedRequest = purchaseRequestRepository.findById(id).get()
            val userRequestRole = checkedRequest.user!!.role

            if (checkedRequest.request!!.status != RequestStatus.PENDING) {
                redirect.addFlashAttribute("error", "Request has already been handled.")
                return "redirect:/man/purchaseRequests"
            }
            when (userRequestRole) {
                Role.CUSTOMER -> {
                    val curCustomer = clientRepository?.findByUserId(checkedRequest.user!!.id!!)
                    if (curCustomer!!.manager != managerEmployeeRepository.findById(currentManager!!.id!!).get()) {
                        redirect.addFlashAttribute("error", "You are not this client's supervising manager.")
                        return "redirect:/man/purchaseRequests"
                    }

                    val cartItems = checkedRequest.cart!!.items
                    cartItems!!.forEach {
                        if (it.weapon != null) {
                            it.weapon!!.quantity += it.quantity!!
                            weaponRepository.save(it.weapon!!)
                        }
                        if(it.transport != null) {
                            it.transport!!.quantity += it.quantity!!
                            transportRepository.save(it.transport!!)
                        }
                    }
                    checkedRequest.request!!.apply {
                        this.statusDate = LocalDateTime.now()
                        this.status = RequestStatus.REJECTED
                        this.resolver = currentManager
                    }
                    checkedRequest.cart!!.status = ShoppingCartStatus.REJECTED
                    purchaseRequestRepository.save(checkedRequest)

                    var cartContents = ""
                    checkedRequest.cart!!.items!!.forEach {
                        cartContents += "\n${it.name()} - ${it.quantity} pieces"
                    }

                    emailSender.sendMessage(
                        curCustomer.email,
                        "Request id#${checkedRequest.id} rejected",
                        "Your purchase request (id #${checkedRequest.id}) has been rejected.\n" +
                                "Unretrieved cart:\n$cartContents\n\n" +
                                "If you are unsatisfied with this decision, please make a new request or contact us at +1-800-MNU-COOL."
                    )

                    // todo same
                    redirect.addFlashAttribute("status", "Request rejected.")
                    return "redirect:/man/purchaseRequests"
                }

                Role.MANUFACTURER -> {
                    redirect.addFlashAttribute("error", "You cannot process manufacturers' requests.")
                    return "redirect:/man/purchaseRequests"
                }

                Role.PRAWN -> {
                    val curPrawn = prawnRepository?.findByUserId(checkedRequest.user!!.id!!)
                    if (curPrawn!!.manager != managerEmployeeRepository.findById(currentManager!!.id!!).get()) {
                        redirect.addFlashAttribute("error", "You are not this prawn's supervising manager.")
                        return "redirect:/man/purchaseRequests"
                    }

                    val cartItems = checkedRequest.cart!!.items
                    cartItems!!.forEach {
                        if (it.weapon != null) {
                            it.weapon!!.quantity += it.quantity!!
                            weaponRepository.save(it.weapon!!)
                        }
                        if(it.transport != null) {
                            it.transport!!.quantity += it.quantity!!
                            transportRepository.save(it.transport!!)
                        }
                    }
                    checkedRequest.request!!.apply {
                        this.statusDate = LocalDateTime.now()
                        this.status = RequestStatus.REJECTED
                        this.resolver = currentManager
                    }
                    checkedRequest.cart!!.status = ShoppingCartStatus.REJECTED
                    purchaseRequestRepository.save(checkedRequest)

                    redirect.addFlashAttribute("status", "Request rejected.")
                    return "redirect:/man/purchaseRequests"
                }

                else -> {
                    redirect.addFlashAttribute("error", "Error. Wrong request credentials.")
                    return "redirect:/man/purchaseRequests"
                }
            }


        } else {
            redirect.addFlashAttribute("error", error)
            "redirect:/man/purchaseRequests"
        }
    }

    @GetMapping("/registerPrawn")
    fun registerPrawn(model: Model): String {
        model.addAttribute("form", PrawnRegistrationForm())
        return "managers/manager__prawn-registration.html"
    }

    @PostMapping("/registerPrawn")
    fun addPrawn(
        @ModelAttribute form: PrawnRegistrationForm, principal: Principal,
        redirect: RedirectAttributes
    ): String {

        val curUser = userRepository?.findByLogin(principal.name)!!
        val possibleManager = managerEmployeeRepository.findById(curUser.id!!)

        /* sussy code
        if (!possibleManager.isPresent) {
            redirect.addFlashAttribute("form", form)
            redirect.addFlashAttribute("error", "You are not a manager.")
            return "redirect:/"
        }*/

        val existingUser = userRepository?.findByLogin(form.username)
        val regex = """[a-zA-Z0-9_.]+""".toRegex()

        return if (!regex.matches(form.username) || !regex.matches(form.password)) {
            redirect.addFlashAttribute("form", form)
            redirect.addFlashAttribute("error", "Only latin letters, numbers, \"_\" and \".\" are supported.")
            "redirect:/man/registerPrawn"
        } else {
            if (form.username.length < 4) {
                redirect.addFlashAttribute("form", form)
                redirect.addFlashAttribute("error", "Username length should be at least 4 symbols.")
                return "redirect:/man/registerPrawn"
            }
            if (form.password.length < 6) {
                redirect.addFlashAttribute("form", form)
                redirect.addFlashAttribute("error", "Password length should be at least 6 symbols.")
                return "redirect:/man/registerPrawn"
            }

            val passwordEncoder = BCryptPasswordEncoder()
            val encodedPassword = passwordEncoder.encode(form.password)
            form.password = encodedPassword

            return if (existingUser != null) {
                redirect.addFlashAttribute("form", form)
                redirect.addFlashAttribute("error", "Username '${form.username}' is already taken. Please try again.")
                "redirect:/man/registerPrawn"
            } else {
                val houseIdList = districtHouseRepository.getAllIds()!!

                val newUser = User(form.username, form.password, Role.PRAWN)
                val newPrawn = Prawn(form.name).apply {
                    this.user = newUser
                    this.districtHouse = districtHouseRepository.findById(houseIdList.random()).get()
                    this.manager = possibleManager.get()
                    this.karma = 50
                    this.balance = 350.0
                }

                userRepository?.save(newUser)
                prawnRepository?.save(newPrawn)

                redirect.addFlashAttribute("form", form)
                redirect.addFlashAttribute("status", "Successfully registered a new prawn.")
                "redirect:/man/main"
            }
        }
    }

}