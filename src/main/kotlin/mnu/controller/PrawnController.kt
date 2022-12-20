package mnu.controller

import mnu.model.entity.TransportType
import mnu.model.entity.Vacancy
import mnu.model.entity.WeaponType
import mnu.model.entity.request.PurchaseRequest
import mnu.model.entity.request.Request
import mnu.model.entity.request.RequestStatus
import mnu.model.entity.request.VacancyApplicationRequest
import mnu.model.entity.shop.ShoppingCart
import mnu.model.entity.shop.ShoppingCartItem
import mnu.model.entity.shop.ShoppingCartStatus
import mnu.model.form.NewPasswordForm
import mnu.repository.*
import mnu.repository.request.PurchaseRequestRepository
import mnu.repository.request.RequestRepository
import mnu.repository.request.VacancyApplicationRequestRepository
import mnu.repository.shop.ShoppingCartItemRepository
import mnu.repository.shop.ShoppingCartRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import org.springframework.data.domain.Sort
import org.springframework.web.bind.annotation.*
import java.security.Principal

@Controller
@RequestMapping("/prawn")
class PrawnController (
    val districtIncidentRepository: DistrictIncidentRepository,
    val districtHouseRepository: DistrictHouseRepository,
    val requestRepository: RequestRepository,
    val vacancyRepository: VacancyRepository,
    val vacancyApplicationRequestRepository: VacancyApplicationRequestRepository,
    val weaponRepository: WeaponRepository,
    val transportRepository: TransportRepository,
    val purchaseRequestRepository: PurchaseRequestRepository,
    val shoppingCartRepository: ShoppingCartRepository,
    val shoppingCartItemRepository: ShoppingCartItemRepository
) : ApplicationController() {

    @GetMapping("/main")
    fun prawnMain(model: Model, principal: Principal): String {
        val currentPrawn = prawnRepository?.findByUserId(userRepository?.findByLogin(principal.name)!!.id!!)
        model.addAttribute("user", currentPrawn)
        return "prawns/prawn__main.html"
    }

    @GetMapping("/shop/{category}")
    fun prawnShop(
        @PathVariable("category") category: String,
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) type: String?,
        @RequestParam(required = false) sort: String?,
        model: Model, principal: Principal, redirect: RedirectAttributes
    ): String {
        val userId = userRepository?.findByLogin(principal.name)!!.id!!
        val currentPrawn = prawnRepository?.findByUserId(userId)
        if (currentPrawn!!.karma < 5000) {
            redirect.addFlashAttribute("error", "You have no permission to buy our products.")
            return "redirect:/prawn/main"
        }

        model.addAttribute("user", currentPrawn)

        val productType = if (category == "weapon") WeaponType.fromClient(type) else TransportType.fromClient(type)
        val productSort = when (sort) {
            "price_asc" -> Sort.by(Sort.Direction.ASC, "price")
            "price_desc" -> Sort.by(Sort.Direction.DESC, "price")
            else -> Sort.by(Sort.Direction.ASC, "id")
        }

        val items: List<Any> = when {
            category == "weapon" && name != null && productType != null ->
                weaponRepository.findAllByNameIgnoreCaseContainingAndTypeAndQuantityGreaterThan(
                    name,
                    productType as WeaponType,
                    0,
                    productSort
                )
            category == "transport" && name != null && productType != null ->
                transportRepository.findAllByNameIgnoreCaseContainingAndTypeAndQuantityGreaterThan(
                    name,
                    productType as TransportType,
                    0,
                    productSort
                )
            category == "weapon" && name == null && productType != null ->
                weaponRepository.findAllByTypeAndQuantityGreaterThan(productType as WeaponType, 0, productSort)
            category == "transport" && name == null && productType != null ->
                transportRepository.findAllByTypeAndQuantityGreaterThan(productType as TransportType, 0, productSort)

            category == "weapon" && name != null && productType == null ->
                weaponRepository.findAllByNameIgnoreCaseContainingAndQuantityGreaterThan(name, 0, productSort)
            category == "transport" && name != null && productType == null ->
                transportRepository.findAllByNameIgnoreCaseContainingAndQuantityGreaterThan(name, 0, productSort)
            category == "weapon" && name == null && productType == null ->
                weaponRepository.findAllByQuantityGreaterThanEqual(1, productSort)
            category == "transport" && name == null && productType == null ->
                transportRepository.findAllByQuantityGreaterThanEqual(1, productSort)
            else -> {
                redirect.addFlashAttribute("error", "Such category filter does not exist.")
                return "redirect:/prawn/shop/weapon"
            }
        }
        val cartItems = shoppingCartItemRepository.findAllByCartUserIdAndCartStatus(userId, ShoppingCartStatus.CREATING)
        val cartItemIds = if (category == "weapon")
            cartItems.filter { it.weapon != null }.map { it.weapon!!.id }
        else
            cartItems.filter { it.transport != null }.map { it.transport!!.id }

        model.addAttribute("items", items)
        model.addAttribute("cartIds", cartItemIds)
        return "prawns/prawn__shop.html"
    }

    @GetMapping("/cart")
    fun saleCart(model: Model, principal: Principal, redirect: RedirectAttributes): String {
        val currentUser = userRepository?.findByLogin(principal.name)!!
        val currentPrawn = prawnRepository?.findByUserId(userRepository?.findByLogin(principal.name)!!.id!!)!!
        if (currentPrawn.karma < 5000) {
            redirect.addFlashAttribute("error", "You have no permission to buy our products.")
            return "redirect:/prawn/main"
        }
        val possibleCart = shoppingCartRepository.findAllByUserAndStatus(currentUser, ShoppingCartStatus.CREATING)
        val usersCart = when {
            possibleCart != null && possibleCart.isNotEmpty() ->
                possibleCart[0]
            else -> ShoppingCart(currentUser).apply {
                this.status = ShoppingCartStatus.CREATING
                this.items = mutableListOf()
            }
        }
        model.addAttribute("items", usersCart.items)
        model.addAttribute("user", currentPrawn)
        return "prawns/prawn__cart.html"
    }

    @GetMapping("/profile")
    fun prawnProfile(model: Model, principal: Principal): String {
        val currentPrawn = prawnRepository?.findByUserId(userRepository?.findByLogin(principal.name)!!.id!!)
        model.addAttribute("user", currentPrawn)
        model.addAttribute("form", NewPasswordForm())
        return "prawns/prawn__profile.html"
    }

    enum class CartItemType { WEAPON, TRANSPORT }
    data class CartItem(val type: CartItemType, var id: Long, var quantity: Long)

    data class CartModifyResponse(var isError: Boolean = false, var message: String = "")

    @PostMapping("/cart/updateQuantity")
    fun modifyQuantity(@RequestParam("itemId") itemId: Long,
                       @RequestParam("newQuantity") newQuantity: Long,
                       principal: Principal, redirect: RedirectAttributes): String {
        val currentUser = userRepository?.findByLogin(principal.name)!!
        val shoppingCartItem = shoppingCartItemRepository.findByIdAndCartUserIdAndCartStatus(itemId, currentUser.id!!, ShoppingCartStatus.CREATING)
        if (shoppingCartItem != null && newQuantity <= 0L) {
            shoppingCartItem.cart = null
            shoppingCartItemRepository.delete(shoppingCartItem)

            redirect.addFlashAttribute("status", "Item deleted.")
            return "redirect:/prawn/cart"
        }

        if (shoppingCartItem != null && newQuantity > 0) {
            shoppingCartItemRepository.save(shoppingCartItem.apply {
                if (this.weapon != null)
                    this.quantity = newQuantity
                else if (this.transport != null)
                    this.quantity = newQuantity
            })

            redirect.addFlashAttribute("status", "Quantity updated.")
            return "redirect:/prawn/cart"
        }

        redirect.addFlashAttribute("error", "Invalid request.")
        return "redirect:/prawn/cart"
    }

    @PostMapping("/cart/modifyAjax")
    @ResponseBody
    fun modifyCartAjax(@RequestBody cartItem: CartItem, principal: Principal, redirect: RedirectAttributes): CartModifyResponse {
        val currentUser = userRepository?.findByLogin(principal.name)!!
        val possibleCart = shoppingCartRepository.findAllByUserAndStatus(currentUser, ShoppingCartStatus.CREATING)
        val currentCreatingCart = when {
            possibleCart != null && possibleCart.isNotEmpty() ->
                possibleCart[0]
            else -> ShoppingCart(currentUser).apply {
                this.status = ShoppingCartStatus.CREATING
                this.items = mutableListOf()
            }
        }

        shoppingCartRepository.save(currentCreatingCart)

        var newShoppingCartItem = ShoppingCartItem()
        when (cartItem.type) {
            CartItemType.WEAPON -> {
                val possibleWeapon = weaponRepository.findById(cartItem.id)
                if (!possibleWeapon.isPresent) {
                    return CartModifyResponse(isError = true, message = "Such weapon does not exist.")
                }
                val existingWeapon = possibleWeapon.get()
                if (existingWeapon.quantity == 0L) {
                    return CartModifyResponse(isError = true, message = "Out of stock. Check back later or choose another weapon.")
                }
                val existingShoppingCartItemCheck = shoppingCartItemRepository.findByWeaponAndCart(existingWeapon, currentCreatingCart)
                if (existingShoppingCartItemCheck == null) {
                    if (cartItem.quantity <= 0L) {
                        return CartModifyResponse(isError = true, message = "Please enter a valid quantity for this item.")
                    }
                    newShoppingCartItem.apply {
                        this.weapon = existingWeapon
                        this.quantity = cartItem.quantity
                    }
                } else {
                    if (cartItem.quantity <= 0L) {
                        existingShoppingCartItemCheck.cart = null
                        shoppingCartItemRepository.delete(existingShoppingCartItemCheck)
                        return CartModifyResponse(isError = false, message = "Item removed from cart.")
                    }
                    existingShoppingCartItemCheck.quantity = cartItem.quantity
                    newShoppingCartItem = existingShoppingCartItemCheck
                }
            }

            CartItemType.TRANSPORT -> {
                val possibleTransport = transportRepository.findById(cartItem.id)
                if (!possibleTransport.isPresent) {
                    return CartModifyResponse(isError = true, message = "Such transport does not exist.")
                }
                val existingTransport = possibleTransport.get()
                if (existingTransport.quantity == 0L) {
                    return CartModifyResponse(isError = true, message = "Out of stock. Check back later or choose another transport.")
                }
                val existingShoppingCartItemCheck = shoppingCartItemRepository.findByTransportAndCart(existingTransport, currentCreatingCart)
                if (existingShoppingCartItemCheck == null) {
                    if (cartItem.quantity <= 0L) {
                        return CartModifyResponse(isError = true, message = "Please enter a valid quantity for this item.")
                    }
                    newShoppingCartItem.apply {
                        this.transport = existingTransport
                        this.quantity = cartItem.quantity
                    }
                } else {
                    if (cartItem.quantity <= 0L) {
                        existingShoppingCartItemCheck.cart = null
                        shoppingCartItemRepository.delete(existingShoppingCartItemCheck)
                        return CartModifyResponse(isError = false, message = "Item removed from cart.")
                    }
                    existingShoppingCartItemCheck.quantity = cartItem.quantity
                    newShoppingCartItem = existingShoppingCartItemCheck
                }
            }
        }

        shoppingCartItemRepository.save(newShoppingCartItem.apply { this.cart = currentCreatingCart })

        return CartModifyResponse(isError = false, message = "Added to cart!")
    }

    @PostMapping("/cart/sendRequest")
    fun sendPurchaseRequest(principal: Principal, redirect: RedirectAttributes) : String {
        val newRequest = Request().apply { this.status = RequestStatus.PENDING }
        val currentUser = userRepository?.findByLogin(principal.name)!!
        val currentPrawn = prawnRepository?.findById(currentUser.id!!)!!.get()
        val possibleCart = shoppingCartRepository.findAllByUserAndStatus(currentUser, ShoppingCartStatus.CREATING)
        val currentCreatingCart = when {
            possibleCart != null && possibleCart.isNotEmpty() ->
                possibleCart[0]
            else -> ShoppingCart(currentUser).apply {
                this.status = ShoppingCartStatus.CREATING
                this.items = mutableListOf()
            }
        }

        if (currentCreatingCart.items == null || currentCreatingCart.items!!.size == 0) {
            redirect.addFlashAttribute("error", "You have no items in your cart.")
            return "redirect:/prawn/shop/weapon"
        }

        val cartItems = currentCreatingCart.items
        var cartSum = 0.0

        cartItems!!.forEach {
            if (it.weapon != null) {
                if (it.weapon!!.quantity < it.quantity!!) {
                    redirect.addFlashAttribute("error",
                        "No sufficient \"${it.weapon!!.name}\" weapons, request cannot be satisfied.")
                    return "redirect:/prawn/cart"
                }
                it.weapon!!.quantity -= it.quantity!!
                weaponRepository.save(it.weapon!!)
            }
            if(it.transport != null) {
                if(it.transport!!.quantity < it.quantity!!) {
                    redirect.addFlashAttribute("error",
                        "No sufficient \"${it.transport!!.name}\" transport, request cannot be satisfied.")
                    return "redirect:/prawn/cart"
                }
                it.transport!!.quantity -= it.quantity!!
                transportRepository.save(it.transport!!)
            }
        }

        cartItems.forEach {
            cartSum += it.price() * it.quantity!!
        }

        if (currentPrawn.balance < cartSum) {
            redirect.addFlashAttribute("error", "Insufficient funds, please edit your cart.")
            return "redirect:/prawn/cart"
        }

        currentCreatingCart.status = ShoppingCartStatus.REQUESTED
        shoppingCartRepository.save(currentCreatingCart)
        purchaseRequestRepository.save(PurchaseRequest(currentUser, currentCreatingCart).apply { this.request = newRequest })

        redirect.addFlashAttribute("status", "Request sent. Await for your managing employee's decision.")
        return "redirect:/prawn/shop/weapon"
    }

    @GetMapping("/vacancies")
    fun prawnVacancies(@RequestParam (required = false) sort: String?, model: Model, principal: Principal) : String {
        val currentPrawn = prawnRepository?.findByUserId(userRepository?.findByLogin(principal.name)!!.id!!)!!
        val allVacancyRequests = vacancyApplicationRequestRepository.findAllByPrawn(currentPrawn)
        var currentVacancyRequest = VacancyApplicationRequest()

        allVacancyRequests?.forEach {
            if (it.request!!.status == RequestStatus.PENDING) {
                currentVacancyRequest = it
            }
        }

        var allVacancies = vacancyRepository.findAllByOrderBySalaryAsc() as MutableList<Vacancy>
        if (sort != null) {
            when (sort) {
                "salaryAsc" -> allVacancies = vacancyRepository.findAllByOrderBySalaryAsc() as MutableList<Vacancy>
                "salaryDesc" -> allVacancies = vacancyRepository.findAllByOrderBySalaryDesc() as MutableList<Vacancy>
                "karmaAsc" -> allVacancies = vacancyRepository.findAllByOrderByRequiredKarmaAsc() as MutableList<Vacancy>
                "karmaDesc" -> allVacancies = vacancyRepository.findAllByOrderByRequiredKarmaDesc() as MutableList<Vacancy>
                "workHoursAsc" -> allVacancies = vacancyRepository.findAllByOrderByWorkHoursPerWeekAsc() as MutableList<Vacancy>
                "workHoursDesc" -> allVacancies = vacancyRepository.findAllByOrderByWorkHoursPerWeekDesc() as MutableList<Vacancy>
            }
        }

        allVacancies.remove(currentPrawn.job)
        if (currentVacancyRequest.vacancy != null)
            allVacancies.remove(currentVacancyRequest.vacancy!!)

        model.addAttribute("user", currentPrawn)
        model.addAttribute("current_job", currentPrawn.job)
        model.addAttribute("current_application", currentVacancyRequest)
        model.addAttribute("all_vacancies", allVacancies)
        return "prawns/prawn__vacancies.html"
    }

    @PostMapping("/vacancyApplication/{id}")
    fun applyToVacancy(@PathVariable id: Long, principal: Principal, redirect: RedirectAttributes) : String {
        val currentPrawn = prawnRepository?.findByUserId(userRepository?.findByLogin(principal.name)!!.id!!)!!
        val newRequest = Request().apply { this.status = RequestStatus.PENDING }
        val allVacancyRequests = vacancyApplicationRequestRepository.findAllByPrawn(currentPrawn)

        allVacancyRequests?.forEach {
            if (it.request!!.status == RequestStatus.PENDING) {
                redirect.addFlashAttribute("error", "You cannot have more than 1 pending vacancy application request.")
                return "redirect:/prawn/vacancies"
            }
        }

        val applicationVacancy = vacancyRepository.findById(id)
        if (!vacancyRepository.findById(id).isPresent) {
            redirect.addFlashAttribute("error", "Such vacancy does not exist.")
            return "redirect:/prawn/vacancies"
        }

        val existingVacancy = applicationVacancy.get()

        when {
            existingVacancy.vacantPlaces == 0L -> {
                redirect.addFlashAttribute("error", "No vacant places left. Check back later or choose another vacancy.")
                return "redirect:/prawn/vacancies"
            }
            existingVacancy.requiredKarma > currentPrawn.karma -> {
                redirect.addFlashAttribute("error", "Requested vacancy's required karma is higher than yours.")
                return "redirect:/prawn/vacancies"
            }
        }

        requestRepository.save(newRequest)
        vacancyApplicationRequestRepository.save(
            VacancyApplicationRequest(currentPrawn, existingVacancy)
                .apply { this.request = newRequest }
        )

        redirect.addFlashAttribute("status", "Request sent. Wait for supervisor's decision.")
        return "redirect:/prawn/vacancies"
    }

    @PostMapping("/quitJob")
    fun withdrawMembership(principal: Principal, redirect: RedirectAttributes) : String {
        val currentPrawn = prawnRepository?.findByUserId(userRepository?.findByLogin(principal.name)!!.id!!)!!
        currentPrawn.job!!.vacantPlaces++
        currentPrawn.job = null
        prawnRepository?.save(currentPrawn)
        redirect.addFlashAttribute("status", "You have left your current job. You may now apply for other vacancies.")
        return "redirect:/prawn/vacancies"
    }

    @PostMapping("/withdrawApplication")
    fun withdrawApplication (principal: Principal, redirect: RedirectAttributes) : String {
        val currentPrawn = prawnRepository?.findByUserId(userRepository?.findByLogin(principal.name)!!.id!!)!!
        val allVacancyRequests = vacancyApplicationRequestRepository.findAllByPrawn(currentPrawn)

        allVacancyRequests?.forEach {
            if (it.request!!.status == RequestStatus.PENDING) {
                it.request!!.status = RequestStatus.REJECTED
                vacancyApplicationRequestRepository.save(it)
                redirect.addFlashAttribute("status", "Application withdrawn.")
                return "redirect:/prawn/vacancies"
            }
        }

        redirect.addFlashAttribute("error", "You have no active vacancy applications.")
        return "redirect:/prawn/vacancies"
    }

}