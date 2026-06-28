package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import android.widget.Toast
import android.content.Context
import android.net.Uri
import androidx.compose.ui.text.style.TextAlign

// Extension property to map non-existent WhatsApp icon to a beautiful standard Send icon safely
val Icons.Filled.WhatsApp: ImageVector
    get() = Icons.Filled.Send

sealed class AppScreen {
    object Dashboard : AppScreen()
    object ContactsList : AppScreen()
    data class ContactDetails(val contactId: Int) : AppScreen()
    object TemplatesCampaigns : AppScreen()
    object Catalogue : AppScreen()
    object RemindersReports : AppScreen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsCloseApp(viewModel: WhatsCloseViewModel) {
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Dashboard) }

    // Database states
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    val templates by viewModel.templates.collectAsStateWithLifecycle()
    val campaigns by viewModel.campaigns.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val followUps by viewModel.followUps.collectAsStateWithLifecycle()
    val sales by viewModel.sales.collectAsStateWithLifecycle()

    val context = LocalContext.current

    // Navigation rail/bar depending on screen width (simulated adaptive via basic box)
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .border(width = 1.dp, color = BorderColor)
                    .windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = CardBg,
                tonalElevation = 0.dp
            ) {
                val items = listOf(
                    Triple(AppScreen.Dashboard, Icons.Default.Dashboard, "Home"),
                    Triple(AppScreen.ContactsList, Icons.Default.People, "Leads"),
                    Triple(AppScreen.TemplatesCampaigns, Icons.Default.MarkEmailRead, "Campaigns"),
                    Triple(AppScreen.Catalogue, Icons.Default.Storefront, "Catalogue"),
                    Triple(AppScreen.RemindersReports, Icons.Default.Assessment, "Settings")
                )

                items.forEach { (screen, icon, label) ->
                    NavigationBarItem(
                        selected = when (currentScreen) {
                            AppScreen.Dashboard -> screen is AppScreen.Dashboard
                            AppScreen.ContactsList -> screen is AppScreen.ContactsList
                            is AppScreen.ContactDetails -> screen is AppScreen.ContactsList
                            AppScreen.TemplatesCampaigns -> screen is AppScreen.TemplatesCampaigns
                            AppScreen.Catalogue -> screen is AppScreen.Catalogue
                            AppScreen.RemindersReports -> screen is AppScreen.RemindersReports
                        },
                        onClick = { currentScreen = screen },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrandGreenMedium,
                            selectedTextColor = BrandGreenMedium,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            indicatorColor = BrandGreenLight
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
                .background(MaterialTheme.colorScheme.background)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "ScreenTransition"
            ) { target ->
                when (target) {
                    AppScreen.Dashboard -> DashboardScreen(
                        viewModel = viewModel,
                        settings = settings,
                        contacts = contacts,
                        followUps = followUps,
                        sales = sales,
                        campaigns = campaigns,
                        onNavigateToLeads = { currentScreen = AppScreen.ContactsList },
                        onNavigateToDetails = { id -> currentScreen = AppScreen.ContactDetails(id) },
                        onNavigateToCampaigns = { currentScreen = AppScreen.TemplatesCampaigns },
                        onNavigateToCatalogue = { currentScreen = AppScreen.Catalogue },
                        onNavigateToSettings = { currentScreen = AppScreen.RemindersReports }
                    )
                    AppScreen.ContactsList -> LeadsScreen(
                        viewModel = viewModel,
                        contacts = contacts,
                        products = products,
                        onContactClick = { id -> currentScreen = AppScreen.ContactDetails(id) }
                    )
                    is AppScreen.ContactDetails -> LeadDetailsScreen(
                        contactId = target.contactId,
                        viewModel = viewModel,
                        contacts = contacts,
                        products = products,
                        templates = templates,
                        onBack = { currentScreen = AppScreen.ContactsList }
                    )
                    AppScreen.TemplatesCampaigns -> TemplatesAndCampaignsScreen(
                        viewModel = viewModel,
                        templates = templates,
                        campaigns = campaigns,
                        contacts = contacts,
                        products = products,
                        settings = settings
                    )
                    AppScreen.Catalogue -> ProductCatalogueScreen(
                        viewModel = viewModel,
                        products = products,
                        settings = settings
                    )
                    AppScreen.RemindersReports -> ReportsSettingsScreen(
                        viewModel = viewModel,
                        settings = settings,
                        followUps = followUps,
                        sales = sales,
                        contacts = contacts,
                        campaigns = campaigns
                    )
                }
            }
        }
    }
}

// ==================== DASHBOARD SCREEN ====================
@Composable
fun DashboardScreen(
    viewModel: WhatsCloseViewModel,
    settings: BusinessSettings?,
    contacts: List<ContactLead>,
    followUps: List<FollowUp>,
    sales: List<Sale>,
    campaigns: List<Campaign>,
    onNavigateToLeads: () -> Unit,
    onNavigateToDetails: (Int) -> Unit,
    onNavigateToCampaigns: () -> Unit,
    onNavigateToCatalogue: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    var showQuickAddLead by remember { mutableStateOf(false) }
    var showQuickCloseSale by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header (Clean Utility / Minimal theme styling)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = settings?.businessName ?: "WhatsClose",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = BrandGreenMedium
                    )
                    Text(
                        text = "BUSINESS DASHBOARD",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Header avatar bubble MB
                val initials = (settings?.businessName ?: "WhatsClose")
                    .split(" ")
                    .take(2)
                    .mapNotNull { it.firstOrNull()?.toString()?.uppercase() }
                    .joinToString("")
                    .ifEmpty { "WC" }

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(BrandGreenLight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = BrandGreenDark
                    )
                }
            }
        }

        // Key Business Metrics Grid
        item {
            Text(
                text = "Business Pulse",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            val pendingPayments = contacts.filter { it.paymentStatus == "Pending Payment" || it.paymentStatus == "Payment Link Sent" }.size
            val hotLeadsCount = contacts.filter { it.leadStatus == "Hot Lead" }.size
            val pendingFollowUpsCount = followUps.filter { it.status == "Pending" }.size

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    MetricCard(
                        title = "Total Leads",
                        value = "${contacts.size}",
                        icon = Icons.Default.Group,
                        color = BrandGreenMedium,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToLeads
                    )
                    MetricCard(
                        title = "Hot Leads",
                        value = "$hotLeadsCount",
                        icon = Icons.Default.Whatshot,
                        color = AccentYellow,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToLeads
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    MetricCard(
                        title = "Follow-Ups",
                        value = "$pendingFollowUpsCount",
                        icon = Icons.Default.Campaign,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToSettings
                    )
                    MetricCard(
                        title = "Pending Payments",
                        value = "$pendingPayments",
                        icon = Icons.Default.HourglassEmpty,
                        color = BrandGreenMedium,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToLeads
                    )
                }
            }
        }

        // Quick Actions Row
        item {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionChip(
                    label = "Add Lead",
                    icon = Icons.Default.PersonAdd,
                    onClick = { showQuickAddLead = true }
                )
                QuickActionChip(
                    label = "Close Sale",
                    icon = Icons.Default.LockOpen,
                    onClick = { showQuickCloseSale = true }
                )
                QuickActionChip(
                    label = "Campaign",
                    icon = Icons.Default.Send,
                    onClick = onNavigateToCampaigns
                )
                QuickActionChip(
                    label = "Products",
                    icon = Icons.Default.AddBusiness,
                    onClick = onNavigateToCatalogue
                )
            }
        }

        // Follow-Ups Due Today
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Follow-Ups Due Today",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                TextButton(onClick = onNavigateToSettings) {
                    Text("View All")
                }
            }
        }

        val todayDateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val todayFollowups = followUps.filter {
            val fDateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date(it.followUpDate))
            fDateStr == todayDateStr && it.status == "Pending"
        }

        if (todayFollowups.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.DoneAll,
                            contentDescription = "No followups",
                            tint = BrandGreenLight,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "You are all caught up!",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "No follow-up reminders pending for today.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(todayFollowups) { fUp ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(fUp.contactName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                            Text("Reason: ${fUp.reason}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Time: ${fUp.followUpTime}", style = MaterialTheme.typography.labelMedium, color = BrandGreenMedium)
                        }
                        IconButton(
                            onClick = {
                                val c = contacts.find { it.id == fUp.contactId }
                                if (c != null) {
                                    val templateText = fUp.messageTemplate.ifEmpty { "Hello [Customer Name], just checking back with you! Let me know if you are ready to proceed. [Business Name]" }
                                    viewModel.sendWhatsAppDirect(context, c.whatsappNumber, templateText, c)
                                    viewModel.updateFollowUpStatus(fUp.id, "Completed")
                                }
                            },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = LightGreenBg)
                        ) {
                            Icon(Icons.Default.WhatsApp, contentDescription = "Send WhatsApp", tint = BrandGreenDark)
                        }
                    }
                }
            }
        }
    }

    // Modal Forms
    if (showQuickAddLead) {
        AddLeadDialog(
            viewModel = viewModel,
            onDismiss = { showQuickAddLead = false }
        )
    }

    if (showQuickCloseSale) {
        QuickCloseSaleDialog(
            viewModel = viewModel,
            contacts = contacts,
            onDismiss = { showQuickCloseSale = false }
        )
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isTotalLeads = title.contains("Total Contacts", ignoreCase = true) || title.contains("Total Leads", ignoreCase = true)
    val isHotLeads = title.contains("Hot Leads", ignoreCase = true)
    val isPendingPayments = title.contains("Pending Payments", ignoreCase = true)

    val containerColor = when {
        isTotalLeads -> LightGreenBg
        isHotLeads -> LightYellowBg
        else -> CardBg
    }

    val strokeColor = when {
        isTotalLeads -> BrandGreenMedium.copy(alpha = 0.15f)
        isHotLeads -> Color(0x33FFB300)
        else -> BorderColor
    }

    val titleTextColor = when {
        isTotalLeads -> BrandGreenMedium
        isHotLeads -> AccentYellow
        isPendingPayments -> BrandGreenMedium
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val valueTextColor = when {
        isTotalLeads -> BrandGreenDark
        isHotLeads -> AccentYellow
        isPendingPayments -> BrandGreenMedium
        else -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = modifier
            .height(112.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, strokeColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = titleTextColor
                )
                if (isHotLeads) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color.Red)
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = titleTextColor.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp
                ),
                color = valueTextColor
            )
        }
    }
}

@Composable
fun QuickActionChip(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val isPrimary = label.contains("Add", ignoreCase = true)
    val isAccent = label.contains("Close", ignoreCase = true) || label.contains("Sale", ignoreCase = true)

    val containerColor = when {
        isPrimary -> BrandGreenMedium
        isAccent -> BrandGreenLight
        else -> CardBg
    }

    val contentColor = when {
        isPrimary -> Color.White
        isAccent -> BrandGreenDark
        else -> BrandGreenMedium
    }

    val borderStroke = if (!isPrimary && !isAccent) {
        BorderStroke(1.dp, BrandGreenMedium)
    } else {
        null
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor)
            .clickable(onClick = onClick)
            .run {
                if (borderStroke != null) {
                    this.border(borderStroke, RoundedCornerShape(16.dp))
                } else this
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(16.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                ),
                color = contentColor
            )
        }
    }
}

// ==================== CONTACTS / LEADS SCREEN ====================
@Composable
fun LeadsScreen(
    viewModel: WhatsCloseViewModel,
    contacts: List<ContactLead>,
    products: List<ProductService>,
    onContactClick: (Int) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }
    var viewMode by remember { mutableStateOf("Table") } // Default to "Table" view

    val filteredContacts = contacts.filter {
        val matchesSearch = it.fullName.contains(searchQuery, ignoreCase = true) ||
                it.whatsappNumber.contains(searchQuery)
        val matchesStatus = statusFilter == "All" || it.leadStatus == statusFilter
        matchesSearch && matchesStatus
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = BrandGreenMedium,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Contact")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Header with layout toggle switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Contacts & Leads",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = BrandGreenMedium
                )
                
                // Switch between Table and List card views
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (viewMode == "Table") BrandGreenMedium else Color.Transparent)
                            .clickable { viewMode = "Table" }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Table",
                            color = if (viewMode == "Table") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (viewMode == "Card") BrandGreenMedium else Color.Transparent)
                            .clickable { viewMode = "Card" }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Cards",
                            color = if (viewMode == "Card") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Search & Filter
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by name or number...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_leads_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandGreenMedium,
                    unfocusedBorderColor = BorderColor
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Horizontal status selector filter
            val statuses = listOf("All", "New Lead", "Interested", "Hot Lead", "Payment Ready", "Paid", "Lost")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                statuses.forEach { status ->
                    val isSelected = statusFilter == status
                    FilterChip(
                        selected = isSelected,
                        onClick = { statusFilter = status },
                        label = { Text(status) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BrandGreenLight,
                            selectedLabelColor = BrandGreenDark,
                            containerColor = Color.Transparent,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredContacts.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Group,
                            contentDescription = null,
                            tint = BorderColor,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No contacts found", fontWeight = FontWeight.Bold)
                        Text(
                            "Add some leads using the green '+' button to start tracking sales.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                if (viewMode == "Table") {
                    LeadsTableView(
                        contacts = filteredContacts,
                        onContactClick = onContactClick,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredContacts) { contact ->
                            ContactItemCard(contact = contact, onClick = { onContactClick(contact.id) })
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddLeadDialog(viewModel = viewModel, onDismiss = { showAddDialog = false })
    }
}

@Composable
fun LeadsTableView(
    contacts: List<ContactLead>,
    onContactClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = BorderStroke(1.dp, BorderColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Horizontal scroll container wrapping header & row list to prevent layout squeezing
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(scrollState)
            ) {
                Column {
                    // Table Header Row
                    Row(
                        modifier = Modifier
                            .background(LightBg)
                            .border(width = 1.dp, color = BorderColor)
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "NAME",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(160.dp)
                        )
                        Text(
                            text = "WHATSAPP",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(140.dp)
                        )
                        Text(
                            text = "STATUS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(140.dp)
                        )
                        Text(
                            text = "ACTIONS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(80.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    // Table Rows
                    LazyColumn(
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        items(contacts) { contact ->
                            Row(
                                modifier = Modifier
                                    .border(width = 0.5.dp, color = BorderColor.copy(alpha = 0.3f))
                                    .clickable { onContactClick(contact.id) }
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = contact.fullName,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.width(160.dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = contact.whatsappNumber,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.width(140.dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Box(modifier = Modifier.width(140.dp)) {
                                    StatusTag(status = contact.leadStatus)
                                }
                                Box(
                                    modifier = Modifier.width(80.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = "Details",
                                        tint = BrandGreenMedium,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContactItemCard(contact: ContactLead, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("contact_item_${contact.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        contact.fullName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    StatusTag(status = contact.leadStatus)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    contact.whatsappNumber,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (contact.interest.isNotEmpty()) {
                    Text(
                        "Interest: ${contact.interest}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = "View Details", tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun StatusTag(status: String) {
    val (colorBg, colorText) = when (status) {
        "New Lead" -> Pair(LightBlueBg, BrandGreenMedium)
        "Hot Lead" -> Pair(LightYellowBg, AccentYellow)
        "Payment Ready" -> Pair(LightGreenBg, BrandGreenMedium)
        "Paid", "Closed/Won" -> Pair(LightGreenBg, BrandGreenDark)
        "Lost" -> Pair(Color(0xFFFFEBEE), SoftRed)
        else -> Pair(BorderColor, Color.Gray)
    }

    Box(
        modifier = Modifier
            .background(colorBg, shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(status, color = colorText, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
    }
}

// ==================== LEAD DETAILS SCREEN (WITH SALES AUTOMATION FLOW) ====================
@Composable
fun LeadDetailsScreen(
    contactId: Int,
    viewModel: WhatsCloseViewModel,
    contacts: List<ContactLead>,
    products: List<ProductService>,
    templates: List<MessageTemplate>,
    onBack: () -> Unit
) {
    val contact = contacts.find { it.id == contactId } ?: return
    val context = LocalContext.current

    var showCloseSaleDialog by remember { mutableStateOf(false) }
    var showAddFollowUpDialog by remember { mutableStateOf(false) }
    var notesText by remember { mutableStateOf(contact.notes) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Appbar Navigation Back
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                }
                Text("Lead Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }

        // Contact Profile Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(contact.fullName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text("WhatsApp: ${contact.whatsappNumber}", style = MaterialTheme.typography.bodyLarge)
                            if (contact.email.isNotEmpty()) {
                                Text("Email: ${contact.email}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (contact.location.isNotEmpty()) {
                                Text("Location: ${contact.location}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        IconButton(
                            onClick = {
                                viewModel.sendWhatsAppDirect(context, contact.whatsappNumber, "Hello ${contact.fullName}, just checking in!", contact)
                            },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = LightGreenBg)
                        ) {
                            Icon(Icons.Default.WhatsApp, contentDescription = "Open WhatsApp", tint = BrandGreenDark)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatusTag(status = contact.leadStatus)
                        Box(
                            modifier = Modifier
                                .background(LightBlueBg, shape = RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Pay: ${contact.paymentStatus}", color = BrandGreenMedium, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // SEMI-AUTOMATIC SALES AUTOMATION FLOW (CRITICAL EXPLICIT REQUIREMENT)
        item {
            Text(
                "Sales Automation Playbook",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Automate steps manually to guide lead from inquiry to closed payment.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    
                    // Stage 1: Welcome message
                    PlaybookStep(
                        title = "1. Send Welcome Message",
                        description = "Introduce your business and make inquiries friction-free.",
                        actionLabel = "Welcome Lead",
                        icon = Icons.Default.ChatBubble,
                        onClick = {
                            val welcomeTemplate = templates.find { it.category == "Welcome Message" }
                            val text = welcomeTemplate?.messageBody ?: "Hello [Customer Name], welcome to [Business Name]! How can we assist you today?"
                            viewModel.sendWhatsAppDirect(context, contact.whatsappNumber, text, contact)
                            viewModel.updateContact(contact.copy(leadStatus = "Interested"))
                        }
                    )

                    // Stage 2: Send Product Details
                    PlaybookStep(
                        title = "2. Send Product/Service Info",
                        description = "Share price, benefit, catalogue images, and detail summaries.",
                        actionLabel = "Share Catalog Info",
                        icon = Icons.Default.Storefront,
                        onClick = {
                            val infoTemplate = templates.find { it.category == "Product/Service Introduction" }
                            val text = infoTemplate?.messageBody ?: "Hello [Customer Name], check out details for [Product/Service] here. The price is [Price]. Link: [Payment Link]"
                            val defaultProd = products.firstOrNull()
                            viewModel.sendWhatsAppDirect(context, contact.whatsappNumber, text, contact, defaultProd)
                        }
                    )

                    // Stage 3: Send Payment Link
                    PlaybookStep(
                        title = "3. Send Payment Link / Prompt",
                        description = "Urgent call-to-action with secure link (Flutterwave, Paystack, bank).",
                        actionLabel = "Prompt Payment",
                        icon = Icons.Default.Send,
                        onClick = {
                            val promptTemplate = templates.find { it.category == "Payment Reminder" }
                            val text = promptTemplate?.messageBody ?: "Hi [Customer Name], secure your order for [Product/Service] at [Price] here: [Payment Link]"
                            val defaultProd = products.firstOrNull()
                            viewModel.sendWhatsAppDirect(context, contact.whatsappNumber, text, contact, defaultProd)
                            viewModel.updateContact(contact.copy(leadStatus = "Payment Ready", paymentStatus = "Payment Link Sent"))
                        }
                    )

                    // Stage 4: Set Follow-Up
                    PlaybookStep(
                        title = "4. Schedule Follow-Up Reminder",
                        description = "Ensure you never forget when a hot lead goes cold.",
                        actionLabel = "Set Reminder",
                        icon = Icons.Default.CalendarMonth,
                        onClick = { showAddFollowUpDialog = true }
                    )

                    // Stage 5: CLOSE SALE (CRITICAL EXTRA SPECIAL OPTION)
                    PlaybookStep(
                        title = "5. Confirm & Close Sale 🏆",
                        description = "Mark paid, record amount, update settings and send automated receipt.",
                        actionLabel = "Close Sale Now",
                        icon = Icons.Default.Stars,
                        onClick = { showCloseSaleDialog = true },
                        highlight = true
                    )
                }
            }
        }

        // Contact Notes Editor
        item {
            Text("Internal Business Notes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = notesText,
                        onValueChange = {
                            notesText = it
                            viewModel.updateContact(contact.copy(notes = it))
                        },
                        placeholder = { Text("Write delivery details, custom options, fabric preferences...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = BorderColor
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Auto-saves instantly", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
        }

        // Danger Action
        item {
            Button(
                onClick = {
                    viewModel.updateContact(contact.copy(leadStatus = "Lost"))
                    onBack()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFCDD2), contentColor = SoftRed),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Mark as Lost Lead ❌", fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showCloseSaleDialog) {
        CloseSaleConfirmDialog(
            viewModel = viewModel,
            contact = contact,
            products = products,
            onDismiss = { showCloseSaleDialog = false },
            onDone = {
                showCloseSaleDialog = false
                onBack()
            }
        )
    }

    if (showAddFollowUpDialog) {
        AddFollowUpDialog(
            viewModel = viewModel,
            contact = contact,
            onDismiss = { showAddFollowUpDialog = false }
        )
    }
}

@Composable
fun PlaybookStep(
    title: String,
    description: String,
    actionLabel: String,
    icon: ImageVector,
    onClick: () -> Unit,
    highlight: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (highlight) LightGreenBg else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, color = if (highlight) BrandGreenDark else MaterialTheme.colorScheme.onSurface)
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (highlight) BrandGreenDark else MaterialTheme.colorScheme.primary
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp))
                    Text(actionLabel, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==================== TEMPLATES & CAMPAIGNS SCREEN ====================
@Composable
fun TemplatesAndCampaignsScreen(
    viewModel: WhatsCloseViewModel,
    templates: List<MessageTemplate>,
    campaigns: List<Campaign>,
    contacts: List<ContactLead>,
    products: List<ProductService>,
    settings: BusinessSettings?
) {
    var selectedSubTab by remember { mutableStateOf(0) } // 0: Templates, 1: Campaigns
    var showAddTemplate by remember { mutableStateOf(false) }
    var showAddCampaign by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Messaging & Marketing",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(12.dp))

        TabRow(selectedTabIndex = selectedSubTab) {
            Tab(selected = selectedSubTab == 0, onClick = { selectedSubTab = 0 }) {
                Box(modifier = Modifier.padding(12.dp)) {
                    Text("WhatsApp Templates", fontWeight = FontWeight.SemiBold)
                }
            }
            Tab(selected = selectedSubTab == 1, onClick = { selectedSubTab = 1 }) {
                Box(modifier = Modifier.padding(12.dp)) {
                    Text("Broadcast Campaigns", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedSubTab == 0) {
            // Templates Sub-tab
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Saved Quick Replies", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Button(onClick = { showAddTemplate = true }, shape = RoundedCornerShape(8.dp)) {
                    Text("+ Save New")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (templates.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text("No custom templates saved yet.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(templates) { template ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, BorderColor)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(template.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                        Box(
                                            modifier = Modifier
                                                .background(LightBlueBg, shape = RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(template.category, color = BrandGreenMedium, style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                    IconButton(onClick = { viewModel.deleteTemplate(template) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SoftRed)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    template.messageBody,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Supports placeholders: [Customer Name], [Product/Service], [Price], [Payment Link], [Business Name]", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        } else {
            // Campaigns Sub-tab
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Broadcast Campaigns", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Button(onClick = { showAddCampaign = true }, shape = RoundedCornerShape(8.dp)) {
                    Text("+ Create Campaign")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (campaigns.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text("Create a broadcast campaign to prompt interested leads on WhatsApp.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(campaigns) { campaign ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, BorderColor)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(campaign.campaignTitle, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                        Text("Targeting: ${campaign.targetAudience}", style = MaterialTheme.typography.bodySmall, color = BrandGreenMedium)
                                    }
                                    Row {
                                        Box(
                                            modifier = Modifier
                                                .background(if (campaign.status == "Active") LightGreenBg else LightYellowBg, shape = RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(campaign.status, color = if (campaign.status == "Active") BrandGreenDark else AccentYellow, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                        IconButton(onClick = { viewModel.deleteCampaign(campaign) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SoftRed, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Promoting: ${campaign.productService}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                if (campaign.offer.isNotEmpty()) {
                                    Text("Offer: ${campaign.offer}", style = MaterialTheme.typography.bodySmall, color = AccentYellow)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(campaign.messageTemplate, modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.bodySmall)
                                }
                                Spacer(modifier = Modifier.height(12.dp))

                                // Send sequential broadcast link! (Extremely cool feature)
                                val campaignContacts = contacts.filter {
                                    campaign.targetAudience == "All Contacts" || it.leadStatus == campaign.targetAudience
                                }
                                Button(
                                    onClick = {
                                        if (campaignContacts.isEmpty()) {
                                            Toast.makeText(context, "No contacts match the campaign target audience!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            // Open WhatsApp for the first matched contact
                                            val first = campaignContacts.first()
                                            viewModel.sendWhatsAppDirect(context, first.whatsappNumber, campaign.messageTemplate, first)
                                            Toast.makeText(context, "Launching broadcast! Directed to WhatsApp for ${first.fullName}.", Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreenMedium)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Default.WhatsApp, contentDescription = null)
                                        Text("Launch Broadcast (${campaignContacts.size} leads)", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddTemplate) {
        AddTemplateDialog(viewModel = viewModel, onDismiss = { showAddTemplate = false })
    }

    if (showAddCampaign) {
        AddCampaignDialog(
            viewModel = viewModel,
            products = products,
            templates = templates,
            contacts = contacts,
            onDismiss = { showAddCampaign = false }
        )
    }
}

// ==================== PRODUCT CATALOGUE SCREEN ====================
@Composable
fun ProductCatalogueScreen(
    viewModel: WhatsCloseViewModel,
    products: List<ProductService>,
    settings: BusinessSettings?
) {
    var showAddProduct by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddProduct = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                "Product & Service Catalogue",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (products.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text("No products or services defined. Create one to share easily!", color = Color.Gray)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(products) { prod ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(210.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, BorderColor)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(75.dp)
                                        .background(LightBlueBg, shape = RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        if (prod.category == "Product") Icons.Default.ShoppingBag else Icons.Default.DesignServices,
                                        contentDescription = null,
                                        tint = BrandGreenMedium,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(prod.name, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyLarge)
                                Text(prod.description, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "${settings?.currency?.split(" ")?.get(0) ?: "₦"}${String.format("%,.2f", prod.price)}",
                                    fontWeight = FontWeight.Bold,
                                    color = BrandGreenDark,
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                Spacer(modifier = Modifier.weight(1f))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = {
                                            val link = prod.paymentLink.ifEmpty { settings?.defaultPaymentLink ?: "" }
                                            if (link.isNotEmpty()) {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                                val clip = android.content.ClipData.newPlainText("Payment Link", link)
                                                clipboard.setPrimaryClip(clip)
                                                Toast.makeText(context, "Payment link copied!", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "No link available", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Link", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    }

                                    IconButton(
                                        onClick = {
                                            val msg = "Hello! Check out our ${prod.name}. Fee is ${settings?.currency?.split(" ")?.get(0) ?: "₦"}${prod.price}. Secure payment link: ${prod.paymentLink}"
                                            viewModel.sendWhatsAppDirect(context, "", msg, null, prod)
                                        },
                                        modifier = Modifier.size(32.dp),
                                        colors = IconButtonDefaults.iconButtonColors(containerColor = LightGreenBg)
                                    ) {
                                        Icon(Icons.Default.WhatsApp, contentDescription = "Share", tint = BrandGreenDark, modifier = Modifier.size(16.dp))
                                    }

                                    IconButton(
                                        onClick = { viewModel.deleteProduct(prod) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SoftRed, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddProduct) {
        AddProductDialog(viewModel = viewModel, onDismiss = { showAddProduct = false })
    }
}

// ==================== REPORTS & SETTINGS SCREEN ====================
@Composable
fun ReportsSettingsScreen(
    viewModel: WhatsCloseViewModel,
    settings: BusinessSettings?,
    followUps: List<FollowUp>,
    sales: List<Sale>,
    contacts: List<ContactLead>,
    campaigns: List<Campaign>
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Reports, 1: Settings, 2: Reminders

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Intelligence & Settings",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(12.dp))

        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                Box(modifier = Modifier.padding(12.dp)) {
                    Text("Sales Reports", fontWeight = FontWeight.SemiBold)
                }
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                Box(modifier = Modifier.padding(12.dp)) {
                    Text("Business Settings", fontWeight = FontWeight.SemiBold)
                }
            }
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                Box(modifier = Modifier.padding(12.dp)) {
                    Text("Reminders List", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            0 -> ReportsView(sales = sales, contacts = contacts, campaigns = campaigns, settings = settings)
            1 -> BusinessSettingsView(viewModel = viewModel, settings = settings)
            2 -> RemindersListView(viewModel = viewModel, followUps = followUps)
        }
    }
}

@Composable
fun ReportsView(sales: List<Sale>, contacts: List<ContactLead>, campaigns: List<Campaign>, settings: BusinessSettings?) {
    val currencySymbol = settings?.currency?.split(" ")?.get(0) ?: "₦"
    val totalRevenue = sales.sumOf { it.amountPaid }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Automated Financial Metrics", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Revenue closed from WhatsApp CRM: $currencySymbol${String.format("%,.2f", totalRevenue)}",
                        fontWeight = FontWeight.ExtraBold,
                        color = BrandGreenDark,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text("Successful closed invoices: ${sales.size}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Conversion Metrics", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))

                    val totalContacts = contacts.size
                    val closedWon = contacts.filter { it.leadStatus == "Closed/Won" || it.leadStatus == "Paid" }.size
                    val conversionRate = if (totalContacts > 0) (closedWon.toDouble() / totalContacts * 100) else 0.0

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total CRM Leads: $totalContacts")
                            Text("Successful Closures: $closedWon")
                        }
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .background(LightGreenBg, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(String.format("%.0f%%", conversionRate), fontWeight = FontWeight.Bold, color = BrandGreenDark, fontSize = 18.sp)
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Best Selling Offerings", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (sales.isEmpty()) {
                        Text("No sale records recorded yet to compile product metrics.", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                    } else {
                        val productGrouping = sales.groupBy { it.productService }
                        productGrouping.forEach { (name, list) ->
                            val productRevenue = list.sumOf { it.amountPaid }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(name, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("${list.size} sold ($currencySymbol${String.format("%,.0f", productRevenue)})", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BusinessSettingsView(viewModel: WhatsCloseViewModel, settings: BusinessSettings?) {
    val context = LocalContext.current
    val initialSettings = settings ?: BusinessSettings()

    var businessName by remember { mutableStateOf(initialSettings.businessName) }
    var whatsappNumber by remember { mutableStateOf(initialSettings.whatsappNumber) }
    var email by remember { mutableStateOf(initialSettings.email) }
    var location by remember { mutableStateOf(initialSettings.location) }
    var currency by remember { mutableStateOf(initialSettings.currency) }
    var defaultPaymentLink by remember { mutableStateOf(initialSettings.defaultPaymentLink) }
    var bankDetails by remember { mutableStateOf(initialSettings.bankDetails) }
    var defaultWelcome by remember { mutableStateOf(initialSettings.defaultWelcomeMessage) }
    var defaultThankYou by remember { mutableStateOf(initialSettings.defaultThankYouMessage) }

    LaunchedEffect(settings) {
        settings?.let {
            businessName = it.businessName
            whatsappNumber = it.whatsappNumber
            email = it.email
            location = it.location
            currency = it.currency
            defaultPaymentLink = it.defaultPaymentLink
            bankDetails = it.bankDetails
            defaultWelcome = it.defaultWelcomeMessage
            defaultThankYou = it.defaultThankYouMessage
        }
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            OutlinedTextField(
                value = businessName,
                onValueChange = { businessName = it },
                label = { Text("Business Name") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = whatsappNumber,
                onValueChange = { whatsappNumber = it },
                label = { Text("WhatsApp Business Phone") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Business Email") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = currency,
                onValueChange = { currency = it },
                label = { Text("Currency Setting") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = defaultPaymentLink,
                onValueChange = { defaultPaymentLink = it },
                label = { Text("Default Payment Link") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = bankDetails,
                onValueChange = { bankDetails = it },
                label = { Text("Bank Transfer details") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = defaultWelcome,
                onValueChange = { defaultWelcome = it },
                label = { Text("Default Welcome WhatsApp message template") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
        }
        item {
            OutlinedTextField(
                value = defaultThankYou,
                onValueChange = { defaultThankYou = it },
                label = { Text("Default Closed-Sale thank WhatsApp template") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
        }

        item {
            Button(
                onClick = {
                    viewModel.saveSettings(
                        businessName, whatsappNumber, email, location, currency, defaultPaymentLink, bankDetails, defaultWelcome, defaultThankYou
                    )
                    Toast.makeText(context, "Settings saved successfully!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Profile Settings", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun RemindersListView(viewModel: WhatsCloseViewModel, followUps: List<FollowUp>) {
    if (followUps.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No scheduled follow-up reminders recorded.", color = Color.Gray)
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(followUps) { fUp ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(fUp.contactName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                            Text("Due: ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(fUp.followUpDate))} at ${fUp.followUpTime}", style = MaterialTheme.typography.bodySmall, color = BrandGreenMedium)
                            Text("Reason: ${fUp.reason}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row {
                            if (fUp.status == "Pending") {
                                IconButton(
                                    onClick = { viewModel.updateFollowUpStatus(fUp.id, "Completed") },
                                    colors = IconButtonDefaults.iconButtonColors(containerColor = LightGreenBg)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = "Complete", tint = BrandGreenDark)
                                }
                            }
                            IconButton(onClick = { viewModel.deleteFollowUp(fUp) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SoftRed)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================== DIALOGS & POPUPS ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLeadDialog(viewModel: WhatsCloseViewModel, onDismiss: () -> Unit) {
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var interest by remember { mutableStateOf("") }
    var source by remember { mutableStateOf("WhatsApp") }
    var notes by remember { mutableStateOf("") }
    var leadStatus by remember { mutableStateOf("New Lead") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Lead / Contact", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth().testTag("add_lead_name_input"))
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("WhatsApp Number (e.g., 234...)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth().testTag("add_lead_phone_input"))
                
                Text("Lead Status", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                val statusOptions = listOf("New Lead", "Interested", "Hot Lead", "Payment Ready", "Paid", "Lost")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    statusOptions.forEach { status ->
                        val isSelected = leadStatus == status
                        FilterChip(
                            selected = isSelected,
                            onClick = { leadStatus = status },
                            label = { Text(status) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BrandGreenLight,
                                selectedLabelColor = BrandGreenDark
                            )
                        )
                    }
                }

                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location / City") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = interest, onValueChange = { interest = it }, label = { Text("Product/Service Interest") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = source, onValueChange = { source = it }, label = { Text("Lead Source") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fullName.isNotEmpty() && phone.isNotEmpty()) {
                        viewModel.saveContact(fullName, phone, email, location, interest, source, leadStatus, "Not Sent", notes, true)
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("add_lead_confirm_button")
            ) {
                Text("Save Contact")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun QuickCloseSaleDialog(viewModel: WhatsCloseViewModel, contacts: List<ContactLead>, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var selectedContact by remember { mutableStateOf<ContactLead?>(null) }
    var productService by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("Bank Transfer") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register Closed Sale 🏆", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Select lead to close sale:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                
                // Simplified contact dropdown selector
                var expandedContacts by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(onClick = { expandedContacts = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(selectedContact?.fullName ?: "Choose Contact Lead")
                    }
                    DropdownMenu(expanded = expandedContacts, onDismissRequest = { expandedContacts = false }) {
                        contacts.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c.fullName) },
                                onClick = {
                                    selectedContact = c
                                    productService = c.interest
                                    expandedContacts = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(value = productService, onValueChange = { productService = it }, label = { Text("Product / Service Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount Paid") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                
                var expandedPayment by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(onClick = { expandedPayment = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Pay Method: $paymentMethod")
                    }
                    DropdownMenu(expanded = expandedPayment, onDismissRequest = { expandedPayment = false }) {
                        val methods = listOf("Bank Transfer", "Payment Link", "Cash", "POS")
                        methods.forEach { m ->
                            DropdownMenuItem(
                                text = { Text(m) },
                                onClick = {
                                    paymentMethod = m
                                    expandedPayment = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val sc = selectedContact
                    val amtDouble = amount.toDoubleOrNull() ?: 0.0
                    if (sc != null && productService.isNotEmpty()) {
                        viewModel.closeSale(sc.id, sc.fullName, productService, amtDouble, paymentMethod, context)
                        onDismiss()
                    }
                }
            ) {
                Text("Confirm Sale Close")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CloseSaleConfirmDialog(
    viewModel: WhatsCloseViewModel,
    contact: ContactLead,
    products: List<ProductService>,
    onDismiss: () -> Unit,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    var productService by remember { mutableStateOf(contact.interest) }
    var amount by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("Bank Transfer") }

    LaunchedEffect(products) {
        val matchingProduct = products.find { it.name.contains(contact.interest, ignoreCase = true) }
        matchingProduct?.let {
            amount = it.price.toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Close Sale: ${contact.fullName}", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(value = productService, onValueChange = { productService = it }, label = { Text("Product Purchased") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount Paid") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                
                var expandedPayment by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(onClick = { expandedPayment = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Payment Method: $paymentMethod")
                    }
                    DropdownMenu(expanded = expandedPayment, onDismissRequest = { expandedPayment = false }) {
                        val methods = listOf("Bank Transfer", "Payment Link", "Cash", "POS")
                        methods.forEach { m ->
                            DropdownMenuItem(
                                text = { Text(m) },
                                onClick = {
                                    paymentMethod = m
                                    expandedPayment = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amtDouble = amount.toDoubleOrNull() ?: 0.0
                    if (productService.isNotEmpty()) {
                        viewModel.closeSale(contact.id, contact.fullName, productService, amtDouble, paymentMethod, context)
                        onDone()
                    }
                }
            ) {
                Text("Confirm Closed & Send Receipt")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddFollowUpDialog(
    viewModel: WhatsCloseViewModel,
    contact: ContactLead,
    onDismiss: () -> Unit
) {
    var reason by remember { mutableStateOf("Payment reminder check-in") }
    var hours by remember { mutableStateOf("10") }
    var minutes by remember { mutableStateOf("00") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule Follow-Up Reminder", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Lead: ${contact.fullName}", fontWeight = FontWeight.SemiBold)
                OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Reason") }, modifier = Modifier.fillMaxWidth())
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = hours,
                        onValueChange = { hours = it },
                        label = { Text("Hour (0-23)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = minutes,
                        onValueChange = { minutes = it },
                        label = { Text("Minute (0-59)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val date = System.currentTimeMillis() + 24 * 60 * 60 * 1000L // Defaulting to tomorrow
                    viewModel.saveFollowUp(
                        contactId = contact.id,
                        contactName = contact.fullName,
                        date = date,
                        time = "$hours:$minutes",
                        reason = reason,
                        messageTemplateText = "Hi [Customer Name], just checking on you in regards to [Product/Service]! Do let us know. [Business Name]"
                    )
                    onDismiss()
                }
            ) {
                Text("Schedule Tomorrow")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddTemplateDialog(viewModel: WhatsCloseViewModel, onDismiss: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Welcome Message") }
    var body by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save WhatsApp Template", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Template Title") }, modifier = Modifier.fillMaxWidth())
                
                var expandedCat by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(onClick = { expandedCat = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Category: $category")
                    }
                    DropdownMenu(expanded = expandedCat, onDismissRequest = { expandedCat = false }) {
                        val categories = listOf("Welcome Message", "Product/Service Introduction", "Promo Message", "Follow-Up Message", "Payment Reminder", "Thank You Message")
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    expandedCat = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("Template Message Body") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotEmpty() && body.isNotEmpty()) {
                        viewModel.saveTemplate(title, category, body)
                        onDismiss()
                    }
                }
            ) {
                Text("Save Template")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddCampaignDialog(
    viewModel: WhatsCloseViewModel,
    products: List<ProductService>,
    templates: List<MessageTemplate>,
    contacts: List<ContactLead>,
    onDismiss: () -> Unit
) {
    var step by remember { mutableStateOf(1) } // Step 1: Configuration, Step 2: Interactive Builder & Actionable Links

    var title by remember { mutableStateOf("") }
    var selectedProductObj by remember { mutableStateOf<ProductService?>(null) }
    var targetAudience by remember { mutableStateOf("All Contacts") }
    var messageBody by remember { mutableStateOf("Hello [Customer Name], check out [Product/Service] from [Business Name]! Available now for [Price]. Secure link: [Payment Link]") }
    var offer by remember { mutableStateOf("") }

    // Dropdowns expanded states
    var expandedProduct by remember { mutableStateOf(false) }
    var expandedAudience by remember { mutableStateOf(false) }
    var expandedTemplate by remember { mutableStateOf(false) }

    // Dynamic contact matching for targetAudience
    val matchingContacts = remember(targetAudience, contacts) {
        contacts.filter {
            targetAudience == "All Contacts" || it.leadStatus == targetAudience
        }
    }

    // Interactive checklist selection (contactId -> isSelected)
    val selectedContactIds = remember(matchingContacts) {
        mutableStateMapOf<Int, Boolean>().apply {
            matchingContacts.forEach { put(it.id, true) }
        }
    }

    // Track clicked/sent status for the actionable links locally (contactId -> wasLaunched)
    val sentContactIds = remember { mutableStateMapOf<Int, Boolean>() }

    val businessSettings by viewModel.settings.collectAsStateWithLifecycle()

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .fillMaxHeight(0.85f),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Campaign Builder", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = BrandGreenMedium)
                    Text(
                        text = if (step == 1) "Step 1 of 2: Setup & Message" else "Step 2 of 2: Actionable WhatsApp Links",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close Dialog")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (step == 1) {
                    // STEP 1: CONFIGURATION WIZARD
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Campaign Name / Title") },
                            placeholder = { Text("e.g., Independence Day Sales") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandGreenMedium)
                        )

                        Text("What are you promoting?", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Box {
                            OutlinedButton(
                                onClick = { expandedProduct = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, if (selectedProductObj != null) BrandGreenMedium else BorderColor)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = selectedProductObj?.name ?: "Select a Product/Service to promote",
                                        color = if (selectedProductObj != null) BrandGreenDark else Color.Gray,
                                        fontWeight = if (selectedProductObj != null) FontWeight.Bold else FontWeight.Normal
                                    )
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = BrandGreenMedium)
                                }
                            }
                            DropdownMenu(
                                expanded = expandedProduct,
                                onDismissRequest = { expandedProduct = false },
                                modifier = Modifier.fillMaxWidth(0.85f)
                            ) {
                                products.forEach { p ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                                Text(p.name, fontWeight = FontWeight.SemiBold)
                                                Text(
                                                    text = "${businessSettings?.currency?.split(" ")?.get(0) ?: "₦"}${String.format("%,.2f", p.price)}",
                                                    color = BrandGreenMedium,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedProductObj = p
                                            expandedProduct = false
                                        }
                                    )
                                }
                            }
                        }

                        Text("Choose Base Message Template", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Box {
                            OutlinedButton(
                                onClick = { expandedTemplate = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Custom / Saved Templates",
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = BrandGreenMedium)
                                }
                            }
                            DropdownMenu(
                                expanded = expandedTemplate,
                                onDismissRequest = { expandedTemplate = false },
                                modifier = Modifier.fillMaxWidth(0.85f)
                            ) {
                                templates.forEach { t ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(t.title, fontWeight = FontWeight.Bold)
                                                Text(t.messageBody, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                            }
                                        },
                                        onClick = {
                                            messageBody = t.messageBody
                                            expandedTemplate = false
                                        }
                                    )
                                }
                            }
                        }

                        Text("Target Audience Group", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Box {
                            OutlinedButton(
                                onClick = { expandedAudience = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, BrandGreenMedium)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Audience: $targetAudience", color = BrandGreenDark, fontWeight = FontWeight.Bold)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = BrandGreenMedium)
                                }
                            }
                            DropdownMenu(
                                expanded = expandedAudience,
                                onDismissRequest = { expandedAudience = false },
                                modifier = Modifier.fillMaxWidth(0.85f)
                            ) {
                                val audiences = listOf("All Contacts", "New Lead", "Interested", "Hot Lead", "Payment Ready", "Paid", "Lost")
                                audiences.forEach { aud ->
                                    DropdownMenuItem(
                                        text = { Text(aud) },
                                        onClick = {
                                            targetAudience = aud
                                            expandedAudience = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = offer,
                            onValueChange = { offer = it },
                            label = { Text("Special Discount or Offer (Optional)") },
                            placeholder = { Text("e.g., Get 10% Off today only!") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandGreenMedium)
                        )

                        OutlinedTextField(
                            value = messageBody,
                            onValueChange = { messageBody = it },
                            label = { Text("Personalized Message Template Body") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 4,
                            maxLines = 8,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandGreenMedium)
                        )

                        Card(
                            colors = CardDefaults.cardColors(containerColor = LightBg),
                            border = BorderStroke(1.dp, BorderColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Tip: Click tag below to insert dynamic customer values automatically:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = BrandGreenMedium)
                                Spacer(modifier = Modifier.height(6.dp))
                                val tags = listOf("[Customer Name]", "[Product]", "[Price]", "[Payment Link]", "[Business Name]", "[Offer]")
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    tags.forEach { tag ->
                                        Box(
                                            modifier = Modifier
                                                .background(Color.White, shape = RoundedCornerShape(4.dp))
                                                .border(0.5.dp, BorderColor, shape = RoundedCornerShape(4.dp))
                                                .clickable {
                                                    messageBody += " $tag"
                                                }
                                                .padding(horizontal = 6.dp, vertical = 4.dp)
                                        ) {
                                            Text(tag, style = MaterialTheme.typography.labelSmall, color = BrandGreenDark, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // STEP 2: INTERACTIVE PREVIEW & ACTIONABLE WHATSAPP LINKS LIST
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = LightGreenBg),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Campaign, contentDescription = null, tint = BrandGreenMedium)
                                Text(
                                    text = "Launch links generated for ${matchingContacts.filter { selectedContactIds[it.id] == true }.size} selected leads.",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = BrandGreenDark
                                )
                            }
                        }

                        // Contacts checklist
                        Text("Verify Campaign Recipients:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(matchingContacts) { c ->
                                val isSelected = selectedContactIds[c.id] ?: false
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedContactIds[c.id] = !isSelected },
                                    label = { Text(c.fullName) },
                                    leadingIcon = {
                                        if (isSelected) {
                                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                        }
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BrandGreenLight,
                                        selectedLabelColor = BrandGreenDark
                                    )
                                )
                            }
                        }

                        HorizontalDivider(color = BorderColor)

                        Text("Actionable WhatsApp Links:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        
                        val selectedContactsList = matchingContacts.filter { selectedContactIds[it.id] == true }
                        if (selectedContactsList.isEmpty()) {
                            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text("No recipients selected. Check some contacts above.", color = Color.Gray)
                            }
                        } else {
                            val context = LocalContext.current
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(selectedContactsList) { contact ->
                                    val wasSent = sentContactIds[contact.id] ?: false
                                    
                                    // Generate the live customized template preview body
                                    val personalizedText = viewModel.generateWhatsAppLink(
                                        phoneNumber = contact.whatsappNumber,
                                        templateText = messageBody,
                                        contact = contact,
                                        settings = businessSettings,
                                        product = selectedProductObj,
                                        customLink = selectedProductObj?.paymentLink
                                    )
                                    // Extract message parameter query safely for preview
                                    val finalMessageText = remember(personalizedText) {
                                        try {
                                            val uri = Uri.parse(personalizedText)
                                            uri.getQueryParameter("text") ?: messageBody
                                        } catch (e: Exception) {
                                            messageBody
                                        }
                                    }

                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = if (wasSent) LightBg else Color.White),
                                        border = BorderStroke(1.dp, if (wasSent) BorderColor else BrandGreenLight),
                                        elevation = CardDefaults.cardElevation(if (wasSent) 0.dp else 1.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(contact.fullName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                                    Text(contact.whatsappNumber, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                                }
                                                
                                                Box(
                                                    modifier = Modifier
                                                        .background(if (wasSent) LightGreenBg else LightYellowBg, shape = RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = if (wasSent) "Launched ✅" else "Ready to Send 🚀",
                                                        color = if (wasSent) BrandGreenDark else AccentYellow,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                            
                                            Spacer(modifier = Modifier.height(8.dp))
                                            
                                            // The formatted message body preview
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(LightBg, shape = RoundedCornerShape(6.dp))
                                                    .padding(8.dp)
                                            ) {
                                                Text(
                                                    text = finalMessageText,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            
                                            Spacer(modifier = Modifier.height(8.dp))
                                            
                                            Button(
                                                onClick = {
                                                    viewModel.sendWhatsAppDirect(context, contact.whatsappNumber, finalMessageText, contact, selectedProductObj)
                                                    sentContactIds[contact.id] = true
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (wasSent) Color.Gray else BrandGreenMedium
                                                ),
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Icon(Icons.Filled.WhatsApp, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Text(if (wasSent) "Re-launch on WhatsApp" else "Launch on WhatsApp", fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (step == 1) {
                Button(
                    onClick = {
                        if (title.isNotEmpty() && selectedProductObj != null && messageBody.isNotEmpty()) {
                            step = 2
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreenMedium)
                ) {
                    Text("Build Launch List")
                }
            } else {
                Button(
                    onClick = {
                        if (title.isNotEmpty() && selectedProductObj != null && messageBody.isNotEmpty()) {
                            val targetIdsString = selectedContactIds.filter { it.value }.keys.joinToString(",")
                            viewModel.saveCampaign(
                                campaignTitle = title,
                                productService = selectedProductObj?.name ?: "",
                                targetAudience = targetAudience,
                                messageTemplate = messageBody,
                                offer = offer,
                                callToAction = "Order Now",
                                status = "Active",
                                selectedContacts = targetIdsString
                            )
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreenMedium)
                ) {
                    Text("Save Campaign History")
                }
            }
        },
        dismissButton = {
            if (step == 2) {
                TextButton(onClick = { step = 1 }) {
                    Text("Back to Edit")
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

@Composable
fun AddProductDialog(viewModel: WhatsCloseViewModel, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Product") }
    var paymentLink by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Product or Service", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                
                var expandedCat by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(onClick = { expandedCat = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Category: $category")
                    }
                    DropdownMenu(expanded = expandedCat, onDismissRequest = { expandedCat = false }) {
                        listOf("Product", "Service").forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    expandedCat = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(value = paymentLink, onValueChange = { paymentLink = it }, label = { Text("Payment Link (Flutterwave / Paystack...)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val priceD = price.toDoubleOrNull() ?: 0.0
                    if (name.isNotEmpty() && priceD > 0) {
                        viewModel.saveProduct(name, description, priceD, category, true, paymentLink)
                        onDismiss()
                    }
                }
            ) {
                Text("Add to Catalog")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
