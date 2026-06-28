package com.example.ui

import android.net.Uri

object WhatsAppUrlGenerator {
    /**
     * Generates a reusable WhatsApp 'wa.me' URL by replacing placeholders in a given template string.
     * 
     * @param phoneNumber The target WhatsApp phone number (with or without country code).
     * @param template The template string containing placeholders like [Customer Name] or [Product].
     * @param customerName The replacement for [Customer Name].
     * @param productName The replacement for [Product] (also replaces [Product/Service]).
     * @param businessName Optional business name replacement for [Business Name].
     * @return A fully formatted and URL-encoded wa.me link.
     */
    fun generateWaMeUrl(
        phoneNumber: String,
        template: String,
        customerName: String,
        productName: String,
        businessName: String = ""
    ): String {
        var formattedMessage = template
            .replace("[Customer Name]", customerName, ignoreCase = true)
            .replace("[Product]", productName, ignoreCase = true)
            .replace("[Product/Service]", productName, ignoreCase = true)
        
        if (businessName.isNotEmpty()) {
            formattedMessage = formattedMessage.replace("[Business Name]", businessName, ignoreCase = true)
        }

        // Clean up phone number to standard format
        val cleanPhone = phoneNumber.replace("+", "").replace(" ", "").replace("-", "")
        val finalPhone = if (cleanPhone.startsWith("0") && cleanPhone.length == 11) {
            "234" + cleanPhone.substring(1)
        } else {
            cleanPhone
        }

        val encodedMessage = Uri.encode(formattedMessage)
        return "https://wa.me/$finalPhone?text=$encodedMessage"
    }
}
