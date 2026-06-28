package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "business_settings")
data class BusinessSettings(
    @PrimaryKey val id: Int = 1,
    val businessName: String = "My Business",
    val logo: String = "",
    val whatsappNumber: String = "",
    val email: String = "",
    val location: String = "Nigeria",
    val currency: String = "₦ (NGN)",
    val defaultPaymentLink: String = "",
    val bankDetails: String = "",
    val defaultWelcomeMessage: String = "Hello [Customer Name], thank you for contacting [Business Name]! We offer high quality [Product/Service]. Let us know how we can help you today.",
    val defaultThankYouMessage: String = "Thank you [Customer Name]! Your payment for [Product/Service] has been confirmed. We are excited to serve you. Kindly watch out for the next update from [Business Name]."
)

@Entity(tableName = "contacts_leads")
data class ContactLead(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fullName: String,
    val whatsappNumber: String,
    val email: String = "",
    val location: String = "",
    val interest: String = "",
    val leadSource: String = "WhatsApp Direct",
    val leadStatus: String = "New Lead", // New Lead, Interested, Hot Lead, Warm Lead, Cold Lead, Payment Ready, Follow-Up Needed, Paid, Closed/Won, Lost, Inactive
    val paymentStatus: String = "Not Sent", // Not Sent, Payment Link Sent, Pending Payment, Part-Paid, Paid, Failed, Refunded
    val lastContactDate: Long = System.currentTimeMillis(),
    val nextFollowUpDate: Long = 0L,
    val notes: String = "",
    val optInStatus: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "message_templates")
data class MessageTemplate(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String, // Welcome Message, Product/Service Introduction, Promo Message, Follow-Up Message, Payment Reminder, Thank You Message, Customer Reactivation Message, Referral Request, Testimonial Request, Event/Training Invitation, Apology/Service Recovery Message, Urgency/Limited Offer Message
    val messageBody: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "campaigns")
data class Campaign(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val campaignTitle: String,
    val productService: String,
    val targetAudience: String = "All Contacts",
    val messageTemplate: String,
    val selectedContacts: String = "", // Comma-separated lead IDs
    val startDate: Long = System.currentTimeMillis(),
    val followUpDate: Long = 0L,
    val offer: String = "",
    val callToAction: String = "Order Now",
    val status: String = "Draft", // Draft, Active, Completed, Paused
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "products_services")
data class ProductService(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val price: Double,
    val image: String = "",
    val category: String = "Product", // Product, Service
    val availability: Boolean = true,
    val paymentLink: String = "",
    val orderButton: Boolean = true,
    val whatsappInquiryButton: Boolean = true
)

@Entity(tableName = "follow_ups")
data class FollowUp(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val contactId: Int,
    val contactName: String,
    val followUpDate: Long,
    val followUpTime: String = "12:00",
    val reason: String,
    val messageTemplate: String = "",
    val status: String = "Pending" // Pending, Completed, Missed, Rescheduled
)

@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val contactId: Int,
    val contactName: String,
    val productService: String,
    val amountPaid: Double,
    val paymentMethod: String, // Bank Transfer, Payment Link, Cash, POS
    val paymentStatus: String = "Paid",
    val saleStatus: String = "Closed/Won",
    val dateClosed: Long = System.currentTimeMillis()
)
