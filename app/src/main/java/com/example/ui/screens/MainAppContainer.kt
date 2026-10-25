package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.models.User
import com.example.ui.components.AgriTopBar
import com.example.ui.components.ServerConfigDialog
import com.example.ui.theme.AgriGreenPrimary
import com.example.viewmodels.AuthUiState
import com.example.viewmodels.CropHubUiState
import com.example.viewmodels.CropHubViewModel
import kotlinx.coroutines.launch

enum class Screen(val title: String) {
    HOME("Crop Hub Dashboard"),
    CROPS("Crop Management"),
    FARMERS("Farmers Directory"),
    FIELDS("Field Parcels"),
    PRODUCTION("Crop Production"),
    FERTILIZERS("Fertilizer Guide"),
    DISEASES("Crop Pathology Guide"),
    MARKETPLACE("Agri Marketplace"),
    CART_REVIEW("Review Shopping Cart"),
    WEATHER("Meteorological Center"),
    REPORTS("DBMS Analytics"),
    PROFILE("User Profile")
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer(
    authUiState: AuthUiState,
    cropHubUiState: CropHubUiState,
    cropHubViewModel: CropHubViewModel,
    weatherUiState: com.example.viewmodels.WeatherUiState,
    weatherViewModel: com.example.viewmodels.WeatherViewModel,
    preferencesManager: com.example.utils.PreferencesManager? = null,
    initialScreen: Screen = Screen.HOME,
    onTestServer: (String) -> Unit,
    onLogout: () -> Unit
) {
    var currentScreen by remember { mutableStateOf(initialScreen) }
    var screenStack by remember { mutableStateOf(listOf(initialScreen)) }
    var showCartDialog by remember { mutableStateOf(false) }
    var showServerDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun navigateTo(screen: Screen) {
        if (currentScreen != screen) {
            screenStack = screenStack + screen
            currentScreen = screen
        }
    }

    fun navigateBack() {
        if (screenStack.size > 1) {
            screenStack = screenStack.dropLast(1)
            currentScreen = screenStack.last()
        } else {
            currentScreen = Screen.HOME
        }
    }

    // Hardware/System back button handling
    BackHandler(enabled = currentScreen != Screen.HOME) {
        navigateBack()
    }

    // Observe notification messages from ViewModel
    LaunchedEffect(cropHubUiState.notificationMessage) {
        cropHubUiState.notificationMessage?.let { msg ->
            scope.launch {
                snackbarHostState.showSnackbar(msg)
                cropHubViewModel.clearNotification()
            }
        }
    }

    if (showServerDialog) {
        ServerConfigDialog(
            currentUrl = authUiState.currentBaseUrl,
            isConnected = authUiState.isServerConnected,
            onDismiss = { showServerDialog = false },
            onTestAndSave = { url ->
                onTestServer(url)
                showServerDialog = false
            }
        )
    }

    if (showCartDialog) {
        CartDialog(
            cartItems = cropHubUiState.cartItems,
            currentUser = authUiState.user,
            onDismiss = { showCartDialog = false },
            onQuantityChange = { prodId, delta ->
                cropHubViewModel.updateCartQuantity(prodId, delta)
            },
            onCheckout = { name, phone, address ->
                cropHubViewModel.checkout(authUiState.user, name, phone, address)
            }
        )
    }

    val bottomNavItems = listOf(
        BottomNavItem(Screen.HOME, "Home", Icons.Default.Home),
        BottomNavItem(Screen.CROPS, "Crops", Icons.Default.Grass),
        BottomNavItem(Screen.MARKETPLACE, "Market", Icons.Default.Storefront),
        BottomNavItem(Screen.REPORTS, "Reports", Icons.Default.BarChart),
        BottomNavItem(Screen.PROFILE, "Profile", Icons.Default.Person)
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AgriTopBar(
                title = currentScreen.title,
                onBackClick = if (currentScreen != Screen.HOME) { { navigateBack() } } else null,
                onServerClick = { showServerDialog = true },
                onCartClick = { showCartDialog = true },
                cartCount = cropHubUiState.cartItems.sumOf { it.quantity },
                isServerConnected = authUiState.isServerConnected
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("main_bottom_nav")
            ) {
                bottomNavItems.forEach { item ->
                    val selected = currentScreen == item.screen
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            currentScreen = item.screen
                            screenStack = listOf(Screen.HOME, item.screen).distinct()
                        },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AgriGreenPrimary,
                            selectedTextColor = AgriGreenPrimary,
                            indicatorColor = AgriGreenPrimary.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                Screen.HOME -> HomeScreen(
                    uiState = cropHubUiState,
                    currentUser = authUiState.user,
                    onNavigateCrops = { navigateTo(Screen.CROPS) },
                    onNavigateFarmers = { navigateTo(Screen.FARMERS) },
                    onNavigateFields = { navigateTo(Screen.FIELDS) },
                    onNavigateProduction = { navigateTo(Screen.PRODUCTION) },
                    onNavigateFertilizers = { navigateTo(Screen.FERTILIZERS) },
                    onNavigateDiseases = { navigateTo(Screen.DISEASES) },
                    onNavigateMarketplace = { navigateTo(Screen.MARKETPLACE) },
                    onNavigateWeather = { navigateTo(Screen.WEATHER) },
                    onNavigateReports = { navigateTo(Screen.REPORTS) }
                )

                Screen.CROPS -> CropsScreen(
                    uiState = cropHubUiState,
                    onSearchChange = { cropHubViewModel.setCropSearch(it) },
                    onCategoryChange = { cropHubViewModel.setCropCategory(it) },
                    onAddCrop = { cropHubViewModel.addCrop(it) },
                    onUpdateCrop = { cropHubViewModel.updateCrop(it) },
                    onDeleteCrop = { cropHubViewModel.deleteCrop(it) }
                )

                Screen.FARMERS -> FarmersScreen(
                    uiState = cropHubUiState,
                    onSearchChange = { cropHubViewModel.setFarmerSearch(it) },
                    onAddFarmer = { cropHubViewModel.addFarmer(it) },
                    onUpdateFarmer = { cropHubViewModel.updateFarmer(it) },
                    onDeleteFarmer = { cropHubViewModel.deleteFarmer(it) }
                )

                Screen.FIELDS -> FieldsScreen(
                    uiState = cropHubUiState,
                    onAddField = { cropHubViewModel.addField(it) },
                    onDeleteField = { cropHubViewModel.deleteField(it) }
                )

                Screen.PRODUCTION -> ProductionScreen(
                    uiState = cropHubUiState,
                    onAddProduction = { cropHubViewModel.addProduction(it) },
                    onDeleteProduction = { cropHubViewModel.deleteProduction(it) }
                )

                Screen.FERTILIZERS -> FertilizersScreen(
                    uiState = cropHubUiState,
                    onSearchChange = { cropHubViewModel.setFertilizerSearch(it) },
                    onTypeChange = { cropHubViewModel.setFertilizerType(it) },
                    onAddFertilizer = { cropHubViewModel.addFertilizer(it) }
                )

                Screen.DISEASES -> DiseasesScreen(
                    uiState = cropHubUiState,
                    onSearchChange = { cropHubViewModel.setDiseaseSearch(it) }
                )

                Screen.MARKETPLACE -> MarketplaceScreen(
                    uiState = cropHubUiState,
                    currentUser = authUiState.user,
                    onSearchChange = { cropHubViewModel.setMarketSearch(it) },
                    onCategoryChange = { cropHubViewModel.setMarketCategory(it) },
                    onAddToCart = { cropHubViewModel.addToCart(it) },
                    onOpenCart = { navigateTo(Screen.CART_REVIEW) },
                    onAddProduct = { cropHubViewModel.addProduct(it) },
                    onProductDetailViewed = { cropHubViewModel.recordProductInterest(it) }
                )

                Screen.CART_REVIEW -> CartReviewScreen(
                    cartItems = cropHubUiState.cartItems,
                    currentUser = authUiState.user,
                    onNavigateBack = { navigateBack() },
                    onQuantityChange = { id, delta -> cropHubViewModel.updateCartQuantity(id, delta) },
                    onRemoveItem = { id -> cropHubViewModel.removeFromCart(id) },
                    onClearCart = { cropHubViewModel.clearCart() },
                    onCheckout = { name, phone, addr ->
                        cropHubViewModel.checkout(authUiState.user, name, phone, addr)
                        navigateBack()
                    }
                )

                Screen.WEATHER -> WeatherScreen(
                    weatherUiState = weatherUiState,
                    onLocationSelected = { weatherViewModel.setLocation(it) },
                    onRefresh = { weatherViewModel.refresh() }
                )

                Screen.REPORTS -> ReportsScreen(
                    uiState = cropHubUiState
                )

                Screen.PROFILE -> ProfileScreen(
                    currentUser = authUiState.user,
                    currentBaseUrl = authUiState.currentBaseUrl,
                    isServerConnected = authUiState.isServerConnected,
                    preferencesManager = preferencesManager,
                    onTestNotification = { type -> cropHubViewModel.triggerTestNotification(type) },
                    onTestServer = onTestServer,
                    onLogout = onLogout
                )
            }
        }
    }
}
