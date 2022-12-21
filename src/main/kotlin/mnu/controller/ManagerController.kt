package mnu.controller

import mnu.EmailSender
import mnu.model.entity.Prawn
import mnu.model.entity.request.RequestStatus
import mnu.model.entity.Role
import mnu.model.entity.User
import mnu.model.entity.Weapon
import mnu.model.entity.request.NewWeaponRequest
import mnu.model.entity.shop.ShoppingCartStatus
import mnu.model.entity.request.PurchaseRequest
import mnu.model.entity.request.VacancyApplicationRequest
import mnu.model.form.PrawnRegistrationForm
import mnu.repository.DistrictHouseRepository
import mnu.repository.TransportRepository
import mnu.repository.VacancyRepository
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
    val newWeaponRequestRepository: NewWeaponRequestRepository,
    val vacancyApplicationRequestRepository: VacancyApplicationRequestRepository,
    val vacancyRepository: VacancyRepository,
    val emailSender: EmailSender
): ApplicationController() {

    @GetMapping("/main")
    fun manMenu(model: Model, principal: Principal): String {
        val pendingRequests = requestRepository.findAllByStatus(RequestStatus.PENDING)

        val user = userRepository?.findByLogin(principal.name)!!
        val curManager = managerEmployeeRepository.findById(user.id!!).get()
        /*
        val man = employeeRepository?.findByUserId(user.id!!)
        val curManager = managerEmployeeRepository.findByEmployeeId(man?.id!!)*/
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

        val vacApplicationRequests = vacancyApplicationRequestRepository.findAll()
        val vaPendingRequests = ArrayList<VacancyApplicationRequest>()
        if (pendingRequests != null) {
            for (i in pendingRequests.indices) {
                for (j in 0 until vacApplicationRequests.size) {
                    if (vacApplicationRequests[j].request == pendingRequests[i])
                        vaPendingRequests.add(vacApplicationRequests[j])
                }
            }
        }



        model.addAttribute("purch_count", pPendingRequests.size)
        model.addAttribute("vac_appl_count", vaPendingRequests.size)
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

    @GetMapping("/newWeapons")
    fun manNewWeapons(principal: Principal, model: Model): String {
        val weaponRequests = newWeaponRequestRepository.findAll()
        val requestsForManager = ArrayList<NewWeaponRequest>()
        weaponRequests.forEach {
            if ((it.user!!.role == Role.SECURITY || it.user!!.role == Role.SCIENTIST) && it.request!!.status == RequestStatus.PENDING)
                requestsForManager.add(it)
        }
        model.addAttribute("requests", requestsForManager)
        return "managers/manager__new-weapons.html"
    }

    fun newWeaponChoiceError(newWeaponRequestId: Long, principal: Principal): String? {
        val request = newWeaponRequestRepository.findById(newWeaponRequestId)
        if (!request.isPresent)
            return "Request with such id does not exist."
        return null
    }

    @PostMapping("/acceptNewWeapon/{id}")
    fun acceptNewWeapon(@PathVariable id: Long, principal: Principal, redirect: RedirectAttributes): String {
        val user = userRepository?.findByLogin(principal.name)
        val currentManager = employeeRepository?.findById(user?.id!!)?.get()

        val error = newWeaponChoiceError(id, principal)
        return if (error == null) {
            val checkedRequest = newWeaponRequestRepository.findById(id).get()

            if (checkedRequest.request!!.status != RequestStatus.PENDING) {
                redirect.addFlashAttribute("error", "Request has already been handled.")
                "redirect:/man/newWeapons"
            } else {
                checkedRequest.request!!.apply {
                    this.statusDate = LocalDateTime.now()
                    this.status = RequestStatus.ACCEPTED
                    this.resolver = currentManager
                }
                val newWeapon = Weapon(
                    checkedRequest.name, checkedRequest.type,
                    checkedRequest.description, checkedRequest.price, checkedRequest.requiredAccessLvl
                )
                    .apply { this.quantity = checkedRequest.quantity }
                weaponRepository.save(newWeapon)

                newWeaponRequestRepository.save(checkedRequest)
                redirect.addFlashAttribute("status", "Request accepted.")
                "redirect:/man/newWeapons"
            }

        } else {
            redirect.addFlashAttribute("error", error)
            "redirect:/man/newWeapons"
        }
    }


    @PostMapping("/rejectNewWeapon/{id}")
    fun rejectNewWeapon(@PathVariable id: Long, principal: Principal, redirect: RedirectAttributes): String {
        val user = userRepository?.findByLogin(principal.name)
        val currentManager = employeeRepository?.findById(user?.id!!)?.get()

        val error = newWeaponChoiceError(id, principal)
        return if (error == null) {
            val checkedRequest = newWeaponRequestRepository.findById(id).get()

            if (checkedRequest.request!!.status != RequestStatus.PENDING) {
                redirect.addFlashAttribute("error", "Request has already been handled.")
                "redirect:/man/newWeapons"
            } else {
                checkedRequest.request!!.apply {
                    this.statusDate = LocalDateTime.now()
                    this.status = RequestStatus.REJECTED
                    this.resolver = currentManager
                }
                newWeaponRequestRepository.save(checkedRequest)

                redirect.addFlashAttribute("status", "Request rejected.")
                "redirect:/man/newWeapons"
            }

        } else {
            redirect.addFlashAttribute("error", error)
            "redirect:/man/newWeapons"
        }
    }

    @PostMapping("/undoWeaponChoice/{id}")
    fun undoWeapChoice(@PathVariable id: Long, principal: Principal, redirect: RedirectAttributes): String {
        val user = userRepository?.findByLogin(principal.name)
        val currentManager = employeeRepository?.findById(user?.id!!)?.get()

        val error = newWeaponChoiceError(id, principal)
        return if (error == null) {
            val checkedRequest = newWeaponRequestRepository.findById(id).get()

            checkedRequest.request!!.apply {
                this.statusDate = LocalDateTime.now()
                this.status = RequestStatus.PENDING
                this.resolver = currentManager
            }
            val weapons = weaponRepository.findAll().reversed()
            for (i in weapons.indices) {
                if (weapons[i].name == checkedRequest.name)
                    weaponRepository.delete(weapons[i])
                break
            }

            newWeaponRequestRepository.save(checkedRequest)

            redirect.addFlashAttribute("status", "Undone.")
            "redirect:/man/newWeapons"

        } else {
            redirect.addFlashAttribute("error", error)
            "redirect:/man/newWeapons"
        }
    }


    @GetMapping("/jobApplications")
    fun manJobApplications(principal: Principal, model: Model): String {
        val user = userRepository?.findByLogin(principal.name)!!
        val curManager = managerEmployeeRepository.findById(user.id!!).get()

        val vacancyApplicationRequests = vacancyApplicationRequestRepository.findAll()
        val validVacAppRequests = ArrayList<VacancyApplicationRequest>()
        vacancyApplicationRequests.forEach {
            if (it.request!!.status == RequestStatus.PENDING && it.prawn!!.manager == curManager)
                validVacAppRequests.add(it)
        }
        model.addAttribute("requests", validVacAppRequests)
        return "managers/manager__job-applications.html"
    }
    fun jobApplicationChoiceError(jobAppRequestId: Long, principal: Principal): String? {
        val request = vacancyApplicationRequestRepository.findById(jobAppRequestId)
        if (!request.isPresent)
            return "Request with such id does not exist."
        return null
    }

    @PostMapping("/acceptJobApplication/{id}")
    fun acceptJobApplication(@PathVariable id: Long, principal: Principal, redirect: RedirectAttributes): String {
        val user = userRepository?.findByLogin(principal.name)
        val currentManager = employeeRepository?.findById(user?.id!!)?.get()

        val error = jobApplicationChoiceError(id, principal)
        return if (error == null) {
            val checkedRequest = vacancyApplicationRequestRepository.findById(id).get()

            if (checkedRequest.request!!.status != RequestStatus.PENDING) {
                redirect.addFlashAttribute("error", "Request has already been handled.")
                "redirect:/man/jobApplications"
            } else {
                if (checkedRequest.prawn!!.manager != managerEmployeeRepository.findById(currentManager!!.id!!).get()) {
                    redirect.addFlashAttribute("error", "You are not this prawn's supervising manager.")
                    return "redirect:/man/jobApplications"
                }

                if (checkedRequest.vacancy?.vacantPlaces!! == 0L) {
                    checkedRequest.request!!.apply {
                        this.statusDate = LocalDateTime.now()
                        this.status = RequestStatus.REJECTED
                        this.resolver = currentManager
                    }
                    vacancyApplicationRequestRepository.save(checkedRequest)
                    redirect.addFlashAttribute("error", "No vacant places left, request cannot be satisfied.")
                    return "redirect:/man/jobApplications"
                }

                val prawn = checkedRequest.prawn!!

                val prawnLastJob = prawn.job
                if (prawnLastJob != null) {
                    prawnLastJob.vacantPlaces++
                    vacancyRepository.save(prawnLastJob)
                }

                val prawnNewJob = checkedRequest.vacancy
                if (prawnNewJob != null) {
                    prawnNewJob.vacantPlaces--
                    vacancyRepository.save(prawnNewJob)
                }

                prawn.job = checkedRequest.vacancy

                checkedRequest.request!!.apply {
                    this.statusDate = LocalDateTime.now()
                    this.status = RequestStatus.ACCEPTED
                    this.resolver = currentManager
                }
                prawnRepository?.save(prawn)
                vacancyApplicationRequestRepository.save(checkedRequest)
                redirect.addFlashAttribute("status", "Request accepted.")
                "redirect:/man/jobApplications"
            }

        } else {
            redirect.addFlashAttribute("error", error)
            "redirect:/man/jobApplications"
        }
    }

    @PostMapping("/rejectJobApplication/{id}")
    fun rejectJobApplication(@PathVariable id: Long, principal: Principal, redirect: RedirectAttributes): String {
        val user = userRepository?.findByLogin(principal.name)
        val currentManager = employeeRepository?.findById(user?.id!!)?.get()

        val error = jobApplicationChoiceError(id, principal)
        return if (error == null) {
            val checkedRequest = vacancyApplicationRequestRepository.findById(id).get()

            if (checkedRequest.request!!.status != RequestStatus.PENDING) {
                redirect.addFlashAttribute("error", "Request has already been handled.")
                "redirect:/man/jobApplications"
            } else {
                if (checkedRequest.prawn!!.manager != managerEmployeeRepository.findById(currentManager!!.id!!).get()) {
                    redirect.addFlashAttribute("error", "You are not this prawn's supervising manager.")
                    return "redirect:/man/jobApplications"
                }

                checkedRequest.request!!.apply {
                    this.statusDate = LocalDateTime.now()
                    this.status = RequestStatus.REJECTED
                    this.resolver = currentManager
                }
                vacancyApplicationRequestRepository.save(checkedRequest)

                redirect.addFlashAttribute("status", "Request rejected.")
                "redirect:/man/jobApplications"
            }

        } else {
            redirect.addFlashAttribute("error", error)
            "redirect:/man/jobApplications"
        }
    }

}