package com.example.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.models.*
import com.example.repositories.CropHubRepository
import com.example.utils.NotificationHelper
import com.example.utils.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CropHubUiState(
    val isLoading: Boolean = false,
    val crops: List<Crop> = emptyList(),
    val selectedCropCategory: String = "All",
    val cropSearchQuery: String = "",
    val farmers: List<Farmer> = emptyList(),
    val farmerSearchQuery: String = "",
    val fields: List<FieldEntity> = emptyList(),
    val productions: List<CropProduction> = emptyList(),
    val fertilizers: List<Fertilizer> = emptyList(),
    val selectedFertilizerType: String = "All",
    val fertilizerSearchQuery: String = "",
    val diseases: List<CropDisease> = emptyList(),
    val diseaseSearchQuery: String = "",
    val products: List<MarketplaceProduct> = emptyList(),
    val selectedMarketCategory: String = "All",
    val productSearchQuery: String = "",
    val cartItems: List<CartItem> = emptyList(),
    val orders: List<Order> = emptyList(),
    val weather: WeatherResponseData? = null,
    val reports: ReportsResponseData? = null,
    val notificationMessage: String? = null,
    val isError: Boolean = false
)

class CropHubViewModel(
    private val repository: CropHubRepository,
    private val localCartRepository: com.example.repositories.LocalCartRepository? = null,
    private val context: Context? = null,
    private val preferencesManager: PreferencesManager? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(CropHubUiState())
    val uiState: StateFlow<CropHubUiState> = _uiState.asStateFlow()

    init {
        initializeFcmToken()
        loadAllData()
        observeLocalCart()
    }

    @Suppress("DEPRECATION")
    private fun initializeFcmToken() {
        context?.let { ctx ->
            NotificationHelper.createNotificationChannels(ctx)
            try {
                com.google.firebase.FirebaseApp.getApps(ctx).firstOrNull()?.let {
                    com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                        if (task.isSuccessful && task.result != null) {
                            preferencesManager?.fcmToken = task.result
                            Log.d("CropHubViewModel", "FCM token retrieved: ${task.result}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("CropHubViewModel", "Firebase token initialization: ${e.message}")
            }
            if (preferencesManager != null && preferencesManager.fcmToken.isNullOrBlank()) {
                preferencesManager.fcmToken = "fcm_token_crophub_${(System.currentTimeMillis() % 1000000)}"
            }
        }
    }

    private fun observeLocalCart() {
        localCartRepository?.let { localCart ->
            viewModelScope.launch {
                localCart.cartItemsFlow.collect { items ->
                    _uiState.value = _uiState.value.copy(cartItems = items)
                }
            }
        }
    }

    fun loadAllData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            loadCrops()
            loadFarmers()
            loadFields()
            loadProductions()
            loadFertilizers()
            loadDiseases()
            loadProducts()
            loadWeather()
            loadReports()
            loadOrders()
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    // CROPS
    fun loadCrops() {
        viewModelScope.launch {
            val list = repository.getCrops(
                search = _uiState.value.cropSearchQuery,
                category = _uiState.value.selectedCropCategory
            )
            _uiState.value = _uiState.value.copy(crops = list)
        }
    }

    fun setCropSearch(query: String) {
        _uiState.value = _uiState.value.copy(cropSearchQuery = query)
        loadCrops()
    }

    fun setCropCategory(category: String) {
        _uiState.value = _uiState.value.copy(selectedCropCategory = category)
        loadCrops()
    }

    fun addCrop(crop: Crop) {
        viewModelScope.launch {
            val success = repository.addCrop(crop)
            if (success) {
                notify("Crop '${crop.cropName}' registered successfully!")
                loadCrops()
                loadReports()
            } else {
                notify("Failed to add crop", isErr = true)
            }
        }
    }

    fun updateCrop(crop: Crop) {
        viewModelScope.launch {
            val success = repository.updateCrop(crop)
            if (success) {
                notify("Crop '${crop.cropName}' updated successfully!")
                loadCrops()
            } else {
                notify("Failed to update crop", isErr = true)
            }
        }
    }

    fun deleteCrop(id: Int) {
        viewModelScope.launch {
            val success = repository.deleteCrop(id)
            if (success) {
                notify("Crop deleted successfully")
                loadCrops()
                loadReports()
            } else {
                notify("Failed to delete crop", isErr = true)
            }
        }
    }

    // FARMERS
    fun loadFarmers() {
        viewModelScope.launch {
            val list = repository.getFarmers(_uiState.value.farmerSearchQuery)
            _uiState.value = _uiState.value.copy(farmers = list)
        }
    }

    fun setFarmerSearch(query: String) {
        _uiState.value = _uiState.value.copy(farmerSearchQuery = query)
        loadFarmers()
    }

    fun addFarmer(farmer: Farmer) {
        viewModelScope.launch {
            val success = repository.addFarmer(farmer)
            if (success) {
                notify("Farmer '${farmer.name}' registered!")
                loadFarmers()
                loadReports()
            } else {
                notify("Failed to register farmer", isErr = true)
            }
        }
    }

    fun updateFarmer(farmer: Farmer) {
        viewModelScope.launch {
            val success = repository.updateFarmer(farmer)
            if (success) {
                notify("Farmer '${farmer.name}' updated!")
                loadFarmers()
            }
        }
    }

    fun deleteFarmer(id: Int) {
        viewModelScope.launch {
            val success = repository.deleteFarmer(id)
            if (success) {
                notify("Farmer removed")
                loadFarmers()
                loadReports()
            }
        }
    }

    // FIELDS
    fun loadFields() {
        viewModelScope.launch {
            val list = repository.getFields()
            _uiState.value = _uiState.value.copy(fields = list)
            // Re-evaluate weather alerts for registered fields
            evaluateWeatherAlertsForFields()
        }
    }

    fun addField(field: FieldEntity) {
        viewModelScope.launch {
            val success = repository.addField(field)
            if (success) {
                notify("Field '${field.fieldName}' added!")
                loadFields()
                loadReports()
            }
        }
    }

    fun deleteField(id: Int) {
        viewModelScope.launch {
            val success = repository.deleteField(id)
            if (success) {
                notify("Field removed")
                loadFields()
                loadReports()
            }
        }
    }

    // PRODUCTION
    fun loadProductions() {
        viewModelScope.launch {
            val list = repository.getProductions()
            _uiState.value = _uiState.value.copy(productions = list)
        }
    }

    fun addProduction(rec: CropProduction) {
        viewModelScope.launch {
            val success = repository.addProduction(rec)
            if (success) {
                notify("Harvest yield logged: ${rec.quantityProducedKg} kg!")
                loadProductions()
                loadReports()
            }
        }
    }

    fun deleteProduction(id: Int) {
        viewModelScope.launch {
            val success = repository.deleteProduction(id)
            if (success) {
                notify("Production record deleted")
                loadProductions()
                loadReports()
            }
        }
    }

    // FERTILIZERS
    fun loadFertilizers() {
        viewModelScope.launch {
            val list = repository.getFertilizers(
                search = _uiState.value.fertilizerSearchQuery,
                type = _uiState.value.selectedFertilizerType
            )
            _uiState.value = _uiState.value.copy(fertilizers = list)
        }
    }

    fun setFertilizerSearch(query: String) {
        _uiState.value = _uiState.value.copy(fertilizerSearchQuery = query)
        loadFertilizers()
    }

    fun setFertilizerType(type: String) {
        _uiState.value = _uiState.value.copy(selectedFertilizerType = type)
        loadFertilizers()
    }

    fun addFertilizer(fert: Fertilizer) {
        viewModelScope.launch {
            val success = repository.addFertilizer(fert)
            if (success) {
                notify("Fertilizer added!")
                loadFertilizers()
            }
        }
    }

    // DISEASES
    fun loadDiseases() {
        viewModelScope.launch {
            val list = repository.getDiseases(_uiState.value.diseaseSearchQuery)
            _uiState.value = _uiState.value.copy(diseases = list)
        }
    }

    fun setDiseaseSearch(query: String) {
        _uiState.value = _uiState.value.copy(diseaseSearchQuery = query)
        loadDiseases()
    }

    // MARKETPLACE
    fun loadProducts() {
        viewModelScope.launch {
            val list = repository.getProducts(
                search = _uiState.value.productSearchQuery,
                category = _uiState.value.selectedMarketCategory
            )
            _uiState.value = _uiState.value.copy(products = list)

            // Check for new listings relevant to user interests (viewed or past purchases)
            checkAndNotifyMarketplaceRecommendations(list)
        }
    }

    private fun checkAndNotifyMarketplaceRecommendations(products: List<MarketplaceProduct>) {
        if (context == null || preferencesManager == null) return
        if (!preferencesManager.notificationsMarketplaceEnabled) return

        val unnotifiedRelevant = products.filter { prod ->
            preferencesManager.isCategoryOfInterest(prod.category) && !preferencesManager.isProductNotified(prod.id)
        }

        // Notify for the top relevant unnotified product
        unnotifiedRelevant.firstOrNull()?.let { prod ->
            NotificationHelper.sendMarketplaceNotification(
                context = context,
                title = "🌱 New Supply Match: ${prod.name}",
                body = "New arrival in '${prod.category}': PKR ${prod.price}. Matches your viewed supplies & past purchases.",
                productId = prod.id,
                category = prod.category
            )
            preferencesManager.markProductNotified(prod.id)
        }
    }

    fun setMarketSearch(query: String) {
        _uiState.value = _uiState.value.copy(productSearchQuery = query)
        loadProducts()
    }

    fun setMarketCategory(category: String) {
        _uiState.value = _uiState.value.copy(selectedMarketCategory = category)
        // Record user's viewed category interest
        preferencesManager?.recordCategoryViewed(category)
        loadProducts()
    }

    fun recordProductInterest(product: MarketplaceProduct) {
        preferencesManager?.recordCategoryViewed(product.category)
    }

    fun addProduct(product: MarketplaceProduct) {
        viewModelScope.launch {
            val success = repository.addProduct(product)
            if (success) {
                notify("Product '${product.name}' listed for sale!")
                loadProducts()
                loadReports()
            }
        }
    }

    fun addToCart(product: MarketplaceProduct) {
        viewModelScope.launch {
            preferencesManager?.recordCategoryViewed(product.category)
            if (localCartRepository != null) {
                localCartRepository.addToCart(product, 1)
            } else {
                val current = _uiState.value.cartItems.toMutableList()
                val existing = current.indexOfFirst { it.product.id == product.id }
                if (existing != -1) {
                    val item = current[existing]
                    current[existing] = item.copy(quantity = item.quantity + 1)
                } else {
                    current.add(CartItem(product = product, quantity = 1))
                }
                _uiState.value = _uiState.value.copy(cartItems = current)
            }
            notify("Added '${product.name}' to cart")
        }
    }

    fun updateCartQuantity(productId: Int, delta: Int) {
        viewModelScope.launch {
            if (localCartRepository != null) {
                localCartRepository.updateQuantity(productId, delta)
            } else {
                val current = _uiState.value.cartItems.toMutableList()
                val index = current.indexOfFirst { it.product.id == productId }
                if (index != -1) {
                    val newQty = current[index].quantity + delta
                    if (newQty <= 0) {
                        current.removeAt(index)
                    } else {
                        current[index] = current[index].copy(quantity = newQty)
                    }
                    _uiState.value = _uiState.value.copy(cartItems = current)
                }
            }
        }
    }

    fun removeFromCart(productId: Int) {
        viewModelScope.launch {
            if (localCartRepository != null) {
                localCartRepository.removeFromCart(productId)
            } else {
                val current = _uiState.value.cartItems.filter { it.product.id != productId }
                _uiState.value = _uiState.value.copy(cartItems = current)
            }
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            if (localCartRepository != null) {
                localCartRepository.clearCart()
            } else {
                _uiState.value = _uiState.value.copy(cartItems = emptyList())
            }
        }
    }

    fun checkout(user: User?, name: String, phone: String, address: String) {
        val cart = _uiState.value.cartItems
        if (cart.isEmpty()) {
            notify("Your cart is empty", isErr = true)
            return
        }
        val total = cart.sumOf { it.product.price * it.quantity }
        val req = CreateOrderRequest(
            userId = user?.id,
            customerName = name.ifBlank { user?.fullName ?: "Customer" },
            customerPhone = phone.ifBlank { user?.phone ?: "+92 300 0000000" },
            deliveryAddress = address.ifBlank { "Agrarian Supply Drop, Faisalabad" },
            totalAmount = total,
            items = cart.map {
                OrderLineItemRequest(
                    productId = it.product.id,
                    productName = it.product.name,
                    quantity = it.quantity,
                    unitPrice = it.product.price
                )
            }
        )

        viewModelScope.launch {
            val res = repository.placeOrder(req)
            res.fold(
                onSuccess = { msg ->
                    // Record purchased categories to refine user interests
                    cart.forEach { item ->
                        preferencesManager?.recordCategoryPurchased(item.product.category)
                    }

                    val generatedOrderId = (1000 + (System.currentTimeMillis() % 9000)).toInt()

                    // Send push notification for Order Status: Confirmed
                    context?.let { ctx ->
                        if (preferencesManager?.notificationsOrdersEnabled == true) {
                            NotificationHelper.sendOrderStatusNotification(
                                context = ctx,
                                title = "📦 Order Confirmed (#$generatedOrderId)",
                                body = "Your order of ${cart.size} farm items (PKR $total) has been confirmed and scheduled for dispatch.",
                                orderId = generatedOrderId,
                                status = "Confirmed"
                            )
                            preferencesManager.markOrderStatusNotified(generatedOrderId, "Confirmed")
                        }
                    }

                    clearCart()
                    notify(msg)
                    loadOrders()
                    loadReports()
                    loadProducts()
                },
                onFailure = { err ->
                    notify(err.message ?: "Failed to place order", isErr = true)
                }
            )
        }
    }

    fun loadOrders() {
        viewModelScope.launch {
            val list = repository.getOrders()
            _uiState.value = _uiState.value.copy(orders = list)

            // Check for any unnotified order status updates
            if (context != null && preferencesManager != null && preferencesManager.notificationsOrdersEnabled) {
                list.forEach { order ->
                    if (!preferencesManager.isOrderStatusNotified(order.id, order.status)) {
                        NotificationHelper.sendOrderStatusNotification(
                            context = context,
                            title = "📦 Order #${order.id} Status: ${order.status}",
                            body = "Your order of PKR ${order.totalAmount} is currently ${order.status}.",
                            orderId = order.id,
                            status = order.status
                        )
                        preferencesManager.markOrderStatusNotified(order.id, order.status)
                    }
                }
            }
        }
    }

    // WEATHER
    fun loadWeather(location: String? = null) {
        viewModelScope.launch {
            val data = repository.getWeather(location)
            _uiState.value = _uiState.value.copy(weather = data)
            evaluateWeatherAlertsForFields()
        }
    }

    private fun evaluateWeatherAlertsForFields() {
        if (context == null || preferencesManager == null) return
        if (!preferencesManager.notificationsWeatherEnabled) return

        val weatherData = _uiState.value.weather ?: return
        val current = weatherData.current ?: return

        val now = System.currentTimeMillis()
        // Throttle automatic push alerts to once every 20 minutes to prevent notification fatigue
        if (now - preferencesManager.lastWeatherAlertTime < 20 * 60 * 1000) return

        val temp = current.temperatureC
        val rainChance = current.rainProbabilityPct
        val windSpeed = current.windSpeedKph
        val fields = _uiState.value.fields
        val targetField = fields.firstOrNull()?.fieldName ?: "Registered Farm Field"

        when {
            temp > 38.0 -> {
                NotificationHelper.sendWeatherAlertNotification(
                    context = context,
                    title = "🔥 Heat Stress Warning: $targetField",
                    body = "Extreme temperature of ${temp}°C detected. Irrigate fields during evening or early morning to prevent crop flower abortion.",
                    fieldLocation = targetField,
                    isSevere = true
                )
                preferencesManager.lastWeatherAlertTime = now
            }
            temp < 10.0 -> {
                NotificationHelper.sendWeatherAlertNotification(
                    context = context,
                    title = "❄️ Frost Hazard Warning: $targetField",
                    body = "Low temperature of ${temp}°C detected. Light evening field watering recommended to insulate soil thermal mass.",
                    fieldLocation = targetField,
                    isSevere = true
                )
                preferencesManager.lastWeatherAlertTime = now
            }
            rainChance >= 60 -> {
                NotificationHelper.sendWeatherAlertNotification(
                    context = context,
                    title = "🌧️ Severe Rain & Flooding Alert: $targetField",
                    body = "High rain probability of $rainChance%. Suspend pesticide/fertilizer spraying; inspect parcel drainage ditches.",
                    fieldLocation = targetField,
                    isSevere = false
                )
                preferencesManager.lastWeatherAlertTime = now
            }
            windSpeed >= 25.0 -> {
                NotificationHelper.sendWeatherAlertNotification(
                    context = context,
                    title = "💨 High Wind Drift Alert: $targetField",
                    body = "Strong winds (${windSpeed.toInt()} km/h) detected. Halt knapsack/tractor foliar spraying; monitor tall crops for lodging.",
                    fieldLocation = targetField,
                    isSevere = false
                )
                preferencesManager.lastWeatherAlertTime = now
            }
        }
    }

    // Direct test trigger for user in Profile Settings
    fun triggerTestNotification(type: String) {
        if (context == null) return
        when (type.lowercase()) {
            "marketplace" -> {
                val interests = preferencesManager?.getUserInterestCategories()?.joinToString(", ") ?: "Seeds, Fertilizers"
                NotificationHelper.sendMarketplaceNotification(
                    context = context,
                    title = "🌱 New Supply: Pioneer Hybrid Corn Seed",
                    body = "Recommended for your farm based on interests in [$interests]. Certified 98% germination rate at PKR 4,200/bag.",
                    productId = 101,
                    category = "Seeds"
                )
            }
            "weather" -> {
                val fieldName = _uiState.value.fields.firstOrNull()?.fieldName ?: "North Parcel #1"
                NotificationHelper.sendWeatherAlertNotification(
                    context = context,
                    title = "⚠️ Critical Weather Warning: $fieldName",
                    body = "Frost Hazard (4°C) predicted for tonight. Apply light evening canal irrigation to protect young wheat/vegetable seedlings.",
                    fieldLocation = fieldName,
                    isSevere = true
                )
            }
            "order" -> {
                val orderId = 1042
                NotificationHelper.sendOrderStatusNotification(
                    context = context,
                    title = "📦 Order #$orderId: Out for Delivery",
                    body = "Your shipment of Urea 46% N (5 Bags) has been dispatched from Faisalabad Ag-Hub and is en route to your farm.",
                    orderId = orderId,
                    status = "Dispatched"
                )
            }
        }
    }

    // REPORTS
    fun loadReports() {
        viewModelScope.launch {
            val data = repository.getReports()
            _uiState.value = _uiState.value.copy(reports = data)
        }
    }

    private fun notify(msg: String, isErr: Boolean = false) {
        _uiState.value = _uiState.value.copy(notificationMessage = msg, isError = isErr)
    }

    fun clearNotification() {
        _uiState.value = _uiState.value.copy(notificationMessage = null)
    }
}
