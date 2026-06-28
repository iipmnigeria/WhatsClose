package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class WhatsCloseViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = WhatsCloseRepository(database.dao())

    // --- State Flows ---
    val settings = repository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val contacts = repository.allContacts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val templates = repository.allTemplates.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val campaigns = repository.allCampaigns.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val products = repository.allProducts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val followUps = repository.allFollowUps.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val sales = repository.allSales.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        // Initialize default business settings and sample templates/products if empty
        viewModelScope.launch {
            settings.collectLatest { currentSettings ->
                if (currentSettings == null) {
                    repository.saveSettings(BusinessSettings(
                        id = 1,
                        businessName = "Heritage Fabrics & Fashion",
                        whatsappNumber = "2348012345678",
                        email = "heritage@gmail.com",
                        location = "Lagos, Nigeria",
                        currency = "₦ (NGN)",
                        defaultPaymentLink = "https://flutterwave.com/pay/heritage",
                        bankDetails = "GTBank - 0123456789 - Heritage Fabrics"
                    ))
                }
            }
        }

        viewModelScope.launch {
            templates.collectLatest { list ->
                if (list.isEmpty()) {
                    // Populate default templates
                    val defaults = listOf(
                        MessageTemplate(
                            title = "Welcome Message",
                            category = "Welcome Message",
                            messageBody = "Hello [Customer Name], thank you for contacting [Business Name]! We are excited to assist you. What product or service can we help you with today?"
                        ),
                        MessageTemplate(
                            title = "Product Introduction",
                            category = "Product/Service Introduction",
                            messageBody = "Hi [Customer Name], here are the details for [Product/Service]. The price is [Price]. It is currently in stock! You can purchase directly using this secure payment link: [Payment Link]"
                        ),
                        MessageTemplate(
                            title = "Friendly Follow-Up",
                            category = "Follow-Up Message",
                            messageBody = "Hello [Customer Name], just checking in on your interest in [Product/Service]. Let me know if you have any questions or if you are ready to place your order!"
                        ),
                        MessageTemplate(
                            title = "Payment Prompt",
                            category = "Payment Reminder",
                            messageBody = "Hi [Customer Name], we have reserved your order for [Product/Service]. Kindly complete your payment of [Price] here: [Payment Link]. Let us know once paid so we can process your shipment immediately!"
                        ),
                        MessageTemplate(
                            title = "Closed Sale Thank You",
                            category = "Thank You Message",
                            messageBody = "Thank you [Customer Name]! Your payment for [Product/Service] has been confirmed by [Business Name]. We are excited to serve you. Kindly watch out for our dispatch/delivery update shortly!"
                        )
                    )
                    defaults.forEach { repository.saveTemplate(it) }
                }
            }
        }

        viewModelScope.launch {
            products.collectLatest { list ->
                if (list.isEmpty()) {
                    // Add default products
                    val defaults = listOf(
                        ProductService(
                            name = "Premium Ankara Fabric",
                            description = "High quality, 100% cotton premium African print Ankara, 6 yards per bundle.",
                            price = 15000.0,
                            category = "Product",
                            paymentLink = "https://flutterwave.com/pay/ankara6y"
                        ),
                        ProductService(
                            name = "Aso Oke Bridal Wear",
                            description = "Custom handcrafted Yoruba bridal traditional attire with beads and embroidery.",
                            price = 120000.0,
                            category = "Product",
                            paymentLink = "https://flutterwave.com/pay/asookebridal"
                        ),
                        ProductService(
                            name = "Fashion Design Consultation",
                            description = "1-hour virtual consultation and measurement guide for bespoke tailoring.",
                            price = 10000.0,
                            category = "Service",
                            paymentLink = "https://flutterwave.com/pay/fashionconsult"
                        )
                    )
                    defaults.forEach { repository.saveProduct(it) }
                }
            }
        }
    }

    // --- Operations ---

    fun saveSettings(businessName: String, whatsappNumber: String, email: String, location: String, currency: String, defaultPaymentLink: String, bankDetails: String, defaultWelcomeMessage: String, defaultThankYouMessage: String) {
        viewModelScope.launch {
            repository.saveSettings(BusinessSettings(
                id = 1,
                businessName = businessName,
                whatsappNumber = whatsappNumber,
                email = email,
                location = location,
                currency = currency,
                defaultPaymentLink = defaultPaymentLink,
                bankDetails = bankDetails,
                defaultWelcomeMessage = defaultWelcomeMessage,
                defaultThankYouMessage = defaultThankYouMessage
            ))
        }
    }

    fun saveContact(fullName: String, whatsappNumber: String, email: String, location: String, interest: String, leadSource: String, leadStatus: String, paymentStatus: String, notes: String, optIn: Boolean) {
        viewModelScope.launch {
            repository.saveContact(ContactLead(
                fullName = fullName,
                whatsappNumber = whatsappNumber,
                email = email,
                location = location,
                interest = interest,
                leadSource = leadSource,
                leadStatus = leadStatus,
                paymentStatus = paymentStatus,
                notes = notes,
                optInStatus = optIn
            ))
        }
    }

    fun updateContact(contact: ContactLead) {
        viewModelScope.launch {
            repository.saveContact(contact)
        }
    }

    fun deleteContact(contact: ContactLead) {
        viewModelScope.launch {
            repository.deleteContact(contact)
        }
    }

    fun saveTemplate(title: String, category: String, messageBody: String) {
        viewModelScope.launch {
            repository.saveTemplate(MessageTemplate(
                title = title,
                category = category,
                messageBody = messageBody
            ))
        }
    }

    fun deleteTemplate(template: MessageTemplate) {
        viewModelScope.launch {
            repository.deleteTemplate(template)
        }
    }

    fun saveCampaign(campaignTitle: String, productService: String, targetAudience: String, messageTemplate: String, offer: String, callToAction: String, status: String, selectedContacts: String = "") {
        viewModelScope.launch {
            repository.saveCampaign(Campaign(
                campaignTitle = campaignTitle,
                productService = productService,
                targetAudience = targetAudience,
                messageTemplate = messageTemplate,
                offer = offer,
                callToAction = callToAction,
                status = status,
                selectedContacts = selectedContacts
            ))
        }
    }

    fun deleteCampaign(campaign: Campaign) {
        viewModelScope.launch {
            repository.deleteCampaign(campaign)
        }
    }

    fun saveProduct(name: String, description: String, price: Double, category: String, availability: Boolean, paymentLink: String) {
        viewModelScope.launch {
            repository.saveProduct(ProductService(
                name = name,
                description = description,
                price = price,
                category = category,
                availability = availability,
                paymentLink = paymentLink
            ))
        }
    }

    fun deleteProduct(product: ProductService) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }

    fun saveFollowUp(contactId: Int, contactName: String, date: Long, time: String, reason: String, messageTemplateText: String) {
        viewModelScope.launch {
            repository.saveFollowUp(FollowUp(
                contactId = contactId,
                contactName = contactName,
                followUpDate = date,
                followUpTime = time,
                reason = reason,
                messageTemplate = messageTemplateText
            ))
        }
    }

    fun updateFollowUpStatus(id: Int, status: String) {
        viewModelScope.launch {
            repository.updateFollowUpStatus(id, status)
        }
    }

    fun deleteFollowUp(followUp: FollowUp) {
        viewModelScope.launch {
            repository.deleteFollowUp(followUp)
        }
    }

    // --- CLOSE SALE FUNCTION (CRITICAL EXPLICIT REQUIREMENT) ---
    fun closeSale(contactId: Int, contactName: String, productService: String, amountPaid: Double, paymentMethod: String, context: Context) {
        viewModelScope.launch {
            // 1. Save Sale record
            val sale = Sale(
                contactId = contactId,
                contactName = contactName,
                productService = productService,
                amountPaid = amountPaid,
                paymentMethod = paymentMethod,
                paymentStatus = "Paid",
                saleStatus = "Closed/Won",
                dateClosed = System.currentTimeMillis()
            )
            repository.saveSale(sale)

            // 2. Prepare thank-you message
            val currentSettings = settings.value
            val templateText = currentSettings?.defaultThankYouMessage ?: "Thank you [Customer Name]! Your payment for [Product/Service] has been confirmed. We are excited to serve you. Kindly watch out for the next update from [Business Name]."
            
            // Get contact for phone number
            val contact = contacts.value.find { it.id == contactId }
            if (contact != null) {
                // Generate WhatsApp Link and trigger intent
                val formattedLink = generateWhatsAppLink(
                    phoneNumber = contact.whatsappNumber,
                    templateText = templateText,
                    contact = contact,
                    settings = currentSettings,
                    product = null
                )
                
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(formattedLink)).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    Toast.makeText(context, "Sale Closed! Directing to WhatsApp thank-you message...", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Sale Closed! (Could not open WhatsApp)", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Sale Closed successfully!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- WHATSAPP UTILITY LINK GENERATOR ---
    fun generateWhatsAppLink(
        phoneNumber: String,
        templateText: String,
        contact: ContactLead?,
        settings: BusinessSettings?,
        product: ProductService? = null,
        customLink: String? = null
    ): String {
        var msg = templateText
        val customerName = contact?.fullName ?: "Customer"
        val bizName = settings?.businessName ?: "Our Business"
        val prodName = product?.name ?: contact?.interest ?: "Product/Service"
        val priceStr = product?.let { "${settings?.currency?.split(" ")?.get(0) ?: "₦"}${it.price}" } ?: ""
        val payLink = customLink ?: product?.paymentLink ?: settings?.defaultPaymentLink ?: ""
        val contactPerson = settings?.whatsappNumber ?: ""
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val offerStr = "10% Discount"

        msg = msg.replace("[Customer Name]", customerName, ignoreCase = true)
        msg = msg.replace("[Business Name]", bizName, ignoreCase = true)
        msg = msg.replace("[Product]", prodName, ignoreCase = true)
        msg = msg.replace("[Product/Service]", prodName, ignoreCase = true)
        msg = msg.replace("[Price]", priceStr, ignoreCase = true)
        msg = msg.replace("[Payment Link]", payLink, ignoreCase = true)
        msg = msg.replace("[Contact Person]", contactPerson, ignoreCase = true)
        msg = msg.replace("[Date]", dateStr, ignoreCase = true)
        msg = msg.replace("[Offer]", offerStr, ignoreCase = true)

        val formattedPhone = phoneNumber.replace("+", "").replace(" ", "").replace("-", "")
        val finalPhone = if (formattedPhone.startsWith("0") && formattedPhone.length == 11) {
            "234" + formattedPhone.substring(1)
        } else {
            formattedPhone
        }

        return "https://wa.me/$finalPhone?text=${Uri.encode(msg)}"
    }

    // Triggers direct WhatsApp intent with formatted text
    fun sendWhatsAppDirect(context: Context, phoneNumber: String, templateText: String, contact: ContactLead?, product: ProductService? = null) {
        val link = generateWhatsAppLink(phoneNumber, templateText, contact, settings.value, product)
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to launch WhatsApp. Link copied to clipboard instead.", Toast.LENGTH_LONG).show()
            // Copy to clipboard
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("WhatsApp Message", templateText)
            clipboard.setPrimaryClip(clip)
        }
    }
}
