package mnu.controller

import mnu.EmailSender
import mnu.model.entity.shop.ShoppingCartStatus
import mnu.model.form.*
import mnu.model.entity.*
import mnu.model.entity.request.*
import mnu.repository.*
import mnu.repository.employee.*
import mnu.repository.request.*
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.security.Principal
import java.time.LocalDateTime

@Controller
@RequestMapping("/admin")
class AdministratorController (
    val districtIncidentRepository: DistrictIncidentRepository,
    val purchaseRequestRepository: PurchaseRequestRepository,
    val weaponRepository: WeaponRepository,
    val transportRepository: TransportRepository,
    val experimentRepository: ExperimentRepository,
    val managerEmployeeRepository: ManagerEmployeeRepository,
    val emailSender: EmailSender
) : ApplicationController() {

    @GetMapping("/main")
    fun adminMenu(model: Model) : String {

        val purchaseRequests = purchaseRequestRepository.findAll()
        val pPendingRequests = ArrayList<PurchaseRequest>()
        purchaseRequests.forEach {
            val requestUser = it.user!!
            if (it.request!!.status == RequestStatus.PENDING
                && (requestUser.role == Role.CUSTOMER || requestUser.role == Role.PRAWN || requestUser.role == Role.MANUFACTURER)) {
                pPendingRequests.add(it)
            }
        }

        model.addAttribute("purch_count", pPendingRequests.size)
        model.addAttribute("experiment_count",
            experimentRepository.countAllByStatus(ExperimentStatus.PENDING))
        model.addAttribute("ongoing_incidents",
            districtIncidentRepository.findAllByLevelToAndDangerLevelGreaterThan(0, 0)?.size)
        return "administrators/admin__menu.html"
    }

    @GetMapping("/experiments")
    fun adminExperiments(model: Model): String {
        model.addAttribute(
            "experiments",
            experimentRepository.findAllByStatus(ExperimentStatus.PENDING)
        )
        return "administrators/admin__experiments.html"
    }

    @GetMapping("/district")
    fun adminDistrict(model: Model): String {
        model.addAttribute("current_incidents", districtIncidentRepository.findAllByLevelToAndDangerLevelGreaterThan(0, 0))
        model.addAttribute("form", AppointResolversForm())
        return "administrators/admin__district.html"
    }

    @GetMapping("/purchaseRequests")
    fun adminPurchaseRequests(principal: Principal, model: Model) : String {
        val user = userRepository?.findByLogin(principal.name)!!

        val purchaseRequests = purchaseRequestRepository.findAll()
        val validPurchRequests = ArrayList<PurchaseRequest>()
        purchaseRequests.forEach {
            val requestUser = it.user!!
            if (it.request!!.status == RequestStatus.PENDING
                && (requestUser.role == Role.CUSTOMER || requestUser.role == Role.PRAWN || requestUser.role == Role.MANUFACTURER)) {
                validPurchRequests.add(it)
            }
        }
        model.addAttribute("requests", validPurchRequests)
        return "/administrators/admin__purchase-sale.html"
    }

    fun experimentChoiceError(experimentId: Long): String? {
        val experiment = experimentRepository.findById(experimentId)
        if (!experiment.isPresent)
            return "Experiment with such id does not exist."
        return null
    }

    @PostMapping("/acceptExperiment/{id}")
    fun acceptExperiment(@PathVariable id: Long, redirect: RedirectAttributes): String {
        val error = experimentChoiceError(id)
        return if (error == null) {
            val checkedExperiment = experimentRepository.findById(id).get()

            if (checkedExperiment.status != ExperimentStatus.PENDING) {
                redirect.addFlashAttribute("error", "Request has already been handled.")
                "redirect:/admin/experiments"
            } else {
                checkedExperiment.statusDate = LocalDateTime.now()
                checkedExperiment.status = ExperimentStatus.APPROVED
                experimentRepository.save(checkedExperiment)

                redirect.addFlashAttribute("status", "Request accepted.")
                "redirect:/admin/experiments"
            }

        } else {
            redirect.addFlashAttribute("error", error)
            "redirect:/admin/experiments"
        }
    }


    @PostMapping("/rejectExperiment/{id}")
    fun rejectExperiment(@PathVariable id: Long, redirect: RedirectAttributes): String {
        val error = experimentChoiceError(id)
        return if (error == null) {
            val checkedExperiment = experimentRepository.findById(id).get()

            if (checkedExperiment.status != ExperimentStatus.PENDING) {
                redirect.addFlashAttribute("error", "Request has already been handled.")
                "redirect:/admin/experiments"
            } else {
                checkedExperiment.statusDate = LocalDateTime.now()
                checkedExperiment.status = ExperimentStatus.REJECTED
                experimentRepository.save(checkedExperiment)

                redirect.addFlashAttribute("status", "Request rejected.")
                "redirect:/admin/experiments"
            }

        } else {
            redirect.addFlashAttribute("error", error)
            "redirect:/admin/experiments"
        }
    }

    @PostMapping("/undoExperimentChoice/{id}")
    @ResponseBody
    fun undoExpChoice(@PathVariable id: Long, redirect: RedirectAttributes): String {
        val error = experimentChoiceError(id)
        return if (error == null) {
            val checkedExperiment = experimentRepository.findById(id).get()

            checkedExperiment.statusDate = LocalDateTime.now()
            checkedExperiment.status = ExperimentStatus.PENDING
            experimentRepository.save(checkedExperiment)

            redirect.addFlashAttribute("status", "Undone.")
            "redirect:/admin/experiments"

        } else {
            redirect.addFlashAttribute("error", error)
            "redirect:/admin/experiments"
        }
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
        val currentAdmin = employeeRepository?.findById(user?.id!!)?.get()

        val error = purchaseReqChoiceError(id, principal)
        return if (error == null) {
            val checkedRequest = purchaseRequestRepository.findById(id).get()
            val userRequestRole = checkedRequest.user!!.role

            if (checkedRequest.request!!.status != RequestStatus.PENDING) {
                redirect.addFlashAttribute("error", "Request has already been handled.")
                return "redirect:/admin/purchaseRequests"
            }
            when (userRequestRole) {
                Role.CUSTOMER -> {
                    val curCustomer = clientRepository?.findByUserId(checkedRequest.user!!.id!!)
                    checkedRequest.request!!.apply {
                        this.statusDate = LocalDateTime.now()
                        this.status = RequestStatus.ACCEPTED
                        this.resolver = currentAdmin
                    }
                    checkedRequest.cart!!.status = ShoppingCartStatus.RETRIEVED
                    purchaseRequestRepository.save(checkedRequest)

                    var cartContents = ""
                    checkedRequest.cart!!.items!!.forEach {
                        cartContents += "\n${it.name()} - ${it.quantity} pieces"
                    }

                    emailSender.sendMessage(
                        curCustomer!!.email,
                        "Request id#${checkedRequest.id} accepted",
                        "Your purchase request (id #${checkedRequest.id}) has been accepted.\n" +
                                "Cart items are as follows:\n$cartContents\n\n" +
                                "Please contact us at +1-800-FUCK-OFF for payment and delivery discussions."
                    )

                    // todo idk if its working still have to test yet
                    redirect.addFlashAttribute("status", "Request accepted.")
                    return "redirect:/admin/purchaseRequests"
                }

                Role.MANUFACTURER -> {
                    val curCustomer = clientRepository?.findByUserId(checkedRequest.user!!.id!!)
                    checkedRequest.request!!.apply {
                        this.statusDate = LocalDateTime.now()
                        this.status = RequestStatus.ACCEPTED
                        this.resolver = currentAdmin
                    }
                    checkedRequest.cart!!.status = ShoppingCartStatus.RETRIEVED
                    purchaseRequestRepository.save(checkedRequest)

                    var cartContents = ""
                    checkedRequest.cart!!.items!!.forEach {
                        cartContents += "\n${it.name()} - ${it.quantity} pieces"
                    }

                    emailSender.sendMessage(
                        curCustomer!!.email,
                        "Request id#${checkedRequest.id} accepted",
                        "Your sale request (id #${checkedRequest.id}) has been accepted.\n" +
                                "Your \"for sale\" items are as follows:\n$cartContents\n\n" +
                                "Please contact us at +1-800-FUCK-OFF for payment and delivery discussions."
                    )

                    // todo same as customers
                    redirect.addFlashAttribute("status", "Request accepted.")
                    return "redirect:/admin/purchaseRequests"
                }

                Role.PRAWN -> {
                    val reqPrawn = prawnRepository?.findById(checkedRequest.user!!.id!!)!!.get()

                    checkedRequest.request!!.apply {
                        this.statusDate = LocalDateTime.now()
                        this.status = RequestStatus.ACCEPTED
                        this.resolver = currentAdmin
                    }
                    checkedRequest.cart!!.status = ShoppingCartStatus.RETRIEVED
                    checkedRequest.cart!!.items!!.forEach {
                        reqPrawn.balance -= it.price() * it.quantity!!
                    }
                    prawnRepository?.save(reqPrawn)
                    purchaseRequestRepository.save(checkedRequest)

                    redirect.addFlashAttribute("status", "Request accepted.")
                    return "redirect:/admin/purchaseRequests"
                }

                else -> {
                    redirect.addFlashAttribute("error", "Error. Wrong request credentials.")
                    return "redirect:/admin/purchaseRequests"
                }
            }


        } else {
            redirect.addFlashAttribute("error", error)
            "redirect:/admin/purchaseRequests"
        }
    }

    @PostMapping("/rejectPurchaseRequest/{id}")
    fun rejectPurchaseRequest(@PathVariable id: Long, principal: Principal, redirect: RedirectAttributes): String {
        val user = userRepository?.findByLogin(principal.name)
        val currentAdmin = employeeRepository?.findById(user?.id!!)?.get()

        val error = purchaseReqChoiceError(id, principal)
        return if (error == null) {
            val checkedRequest = purchaseRequestRepository.findById(id).get()
            val userRequestRole = checkedRequest.user!!.role

            if (checkedRequest.request!!.status != RequestStatus.PENDING) {
                redirect.addFlashAttribute("error", "Request has already been handled.")
                return "redirect:/admin/purchaseRequests"
            }
            when (userRequestRole) {
                Role.CUSTOMER -> {
                    val curCustomer = clientRepository?.findByUserId(checkedRequest.user!!.id!!)

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
                        this.resolver = currentAdmin
                    }
                    checkedRequest.cart!!.status = ShoppingCartStatus.REJECTED
                    purchaseRequestRepository.save(checkedRequest)

                    var cartContents = ""
                    checkedRequest.cart!!.items!!.forEach {
                        cartContents += "\n${it.name()} - ${it.quantity} pieces"
                    }

                    emailSender.sendMessage(
                        curCustomer!!.email,
                        "Request id#${checkedRequest.id} rejected",
                        "Your purchase request (id #${checkedRequest.id}) has been rejected.\n" +
                                "Unretrieved cart:\n$cartContents\n\n" +
                                "If you are unsatisfied with this decision, please make a new request or contact us at +1-800-FUCK-OFF."
                    )

                    // todo same
                    redirect.addFlashAttribute("status", "Request rejected.")
                    "redirect:/admin/purchaseRequests"
                }

                Role.MANUFACTURER -> {
                    val curCustomer = clientRepository?.findByUserId(checkedRequest.user!!.id!!)

                    val cartItems = checkedRequest.cart!!.items
                    cartItems!!.forEach {
                        if (it.weapon != null) {
                            it.weapon!!.quantity -= it.quantity!!
                            weaponRepository.save(it.weapon!!)
                        }
                        if(it.transport != null) {
                            it.transport!!.quantity -= it.quantity!!
                            transportRepository.save(it.transport!!)
                        }
                    }
                    checkedRequest.request!!.apply {
                        this.statusDate = LocalDateTime.now()
                        this.status = RequestStatus.REJECTED
                        this.resolver = currentAdmin
                    }
                    checkedRequest.cart!!.status = ShoppingCartStatus.REJECTED
                    purchaseRequestRepository.save(checkedRequest)

                    var cartContents = ""
                    checkedRequest.cart!!.items!!.forEach {
                        cartContents += "\n${it.name()} - ${it.quantity} pieces"
                    }

                    emailSender.sendMessage(
                        curCustomer!!.email,
                        "Request id#${checkedRequest.id} rejected",
                        "Your sale request (id #${checkedRequest.id}) has been rejected.\n" +
                                "Unsold item list:\n$cartContents\n\n" +
                                "If you are unsatisfied with this decision, please make a new request or contact us at +1-800-FUCK-OFF."
                    )

                    // todo same...
                    redirect.addFlashAttribute("status", "Request rejected.")
                    "redirect:/admin/purchaseRequests"
                }

                Role.PRAWN -> {
                    val curPrawn = prawnRepository?.findByUserId(checkedRequest.user!!.id!!)
                    if (curPrawn!!.manager != managerEmployeeRepository.findById(currentAdmin!!.id!!).get()) {
                        redirect.addFlashAttribute("error", "You are not this prawn's supervising manager.")
                        return "redirect:/admin/purchaseRequests"
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
                        this.resolver = currentAdmin
                    }
                    checkedRequest.cart!!.status = ShoppingCartStatus.REJECTED
                    purchaseRequestRepository.save(checkedRequest)

                    redirect.addFlashAttribute("status", "Request rejected.")
                    return "redirect:/admin/purchaseRequests"
                }

                else -> {
                    redirect.addFlashAttribute("error", "Error. Wrong request credentials.")
                    return "redirect:/admin/purchaseRequests"
                }
            }


        } else {
            redirect.addFlashAttribute("error", error)
            "redirect:/admin/purchaseRequests"
        }
    }

}