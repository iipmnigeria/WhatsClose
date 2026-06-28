package com.example.data

import kotlinx.coroutines.flow.Flow

class WhatsCloseRepository(private val dao: WhatsCloseDao) {

    // --- Business Settings ---
    val settings: Flow<BusinessSettings?> = dao.getSettings()

    suspend fun saveSettings(settings: BusinessSettings) {
        dao.insertSettings(settings)
    }

    // --- Contacts / Leads ---
    val allContacts: Flow<List<ContactLead>> = dao.getAllContacts()

    fun getContactById(id: Int): Flow<ContactLead?> {
        return dao.getContactById(id)
    }

    suspend fun saveContact(contact: ContactLead): Long {
        return dao.insertContact(contact)
    }

    suspend fun deleteContact(contact: ContactLead) {
        dao.deleteContact(contact)
    }

    suspend fun updateContactStatus(id: Int, status: String, paymentStatus: String) {
        dao.updateContactStatus(id, status, paymentStatus, System.currentTimeMillis())
    }

    suspend fun updateContactFollowUpDate(id: Int, nextDate: Long) {
        dao.updateContactFollowUpDate(id, nextDate)
    }

    // --- Message Templates ---
    val allTemplates: Flow<List<MessageTemplate>> = dao.getAllTemplates()

    suspend fun saveTemplate(template: MessageTemplate) {
        dao.insertTemplate(template)
    }

    suspend fun deleteTemplate(template: MessageTemplate) {
        dao.deleteTemplate(template)
    }

    // --- Campaigns ---
    val allCampaigns: Flow<List<Campaign>> = dao.getAllCampaigns()

    suspend fun saveCampaign(campaign: Campaign) {
        dao.insertCampaign(campaign)
    }

    suspend fun deleteCampaign(campaign: Campaign) {
        dao.deleteCampaign(campaign)
    }

    // --- Products / Services ---
    val allProducts: Flow<List<ProductService>> = dao.getAllProducts()

    suspend fun saveProduct(product: ProductService) {
        dao.insertProduct(product)
    }

    suspend fun deleteProduct(product: ProductService) {
        dao.deleteProduct(product)
    }

    // --- Follow-Up Reminders ---
    val allFollowUps: Flow<List<FollowUp>> = dao.getAllFollowUps()

    suspend fun saveFollowUp(followUp: FollowUp) {
        dao.insertFollowUp(followUp)
        // Also update contact's next follow up date
        dao.updateContactFollowUpDate(followUp.contactId, followUp.followUpDate)
    }

    suspend fun updateFollowUpStatus(id: Int, status: String) {
        dao.updateFollowUpStatus(id, status)
    }

    suspend fun deleteFollowUp(followUp: FollowUp) {
        dao.deleteFollowUp(followUp)
    }

    // --- Sales ---
    val allSales: Flow<List<Sale>> = dao.getAllSales()

    suspend fun saveSale(sale: Sale) {
        dao.insertSale(sale)
        // Also update contact's status and payment status to Paid & Closed/Won
        dao.updateContactStatus(sale.contactId, "Closed/Won", "Paid", System.currentTimeMillis())
    }

    suspend fun deleteSale(sale: Sale) {
        dao.deleteSale(sale)
    }
}
