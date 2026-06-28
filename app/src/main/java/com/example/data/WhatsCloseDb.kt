package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WhatsCloseDao {

    // --- Business Settings ---
    @Query("SELECT * FROM business_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<BusinessSettings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: BusinessSettings)

    // --- Contacts / Leads ---
    @Query("SELECT * FROM contacts_leads ORDER BY createdAt DESC")
    fun getAllContacts(): Flow<List<ContactLead>>

    @Query("SELECT * FROM contacts_leads WHERE id = :id LIMIT 1")
    fun getContactById(id: Int): Flow<ContactLead?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactLead): Long

    @Delete
    suspend fun deleteContact(contact: ContactLead)

    @Query("UPDATE contacts_leads SET leadStatus = :status, paymentStatus = :paymentStatus, lastContactDate = :lastContactDate WHERE id = :id")
    suspend fun updateContactStatus(id: Int, status: String, paymentStatus: String, lastContactDate: Long)

    @Query("UPDATE contacts_leads SET nextFollowUpDate = :nextDate WHERE id = :id")
    suspend fun updateContactFollowUpDate(id: Int, nextDate: Long)

    // --- Message Templates ---
    @Query("SELECT * FROM message_templates ORDER BY createdAt DESC")
    fun getAllTemplates(): Flow<List<MessageTemplate>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: MessageTemplate)

    @Delete
    suspend fun deleteTemplate(template: MessageTemplate)

    // --- Campaigns ---
    @Query("SELECT * FROM campaigns ORDER BY createdAt DESC")
    fun getAllCampaigns(): Flow<List<Campaign>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCampaign(campaign: Campaign)

    @Delete
    suspend fun deleteCampaign(campaign: Campaign)

    // --- Products / Services ---
    @Query("SELECT * FROM products_services ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductService>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductService)

    @Delete
    suspend fun deleteProduct(product: ProductService)

    // --- Follow-Up Reminders ---
    @Query("SELECT * FROM follow_ups ORDER BY followUpDate ASC")
    fun getAllFollowUps(): Flow<List<FollowUp>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollowUp(followUp: FollowUp)

    @Query("UPDATE follow_ups SET status = :status WHERE id = :id")
    suspend fun updateFollowUpStatus(id: Int, status: String)

    @Delete
    suspend fun deleteFollowUp(followUp: FollowUp)

    // --- Sales ---
    @Query("SELECT * FROM sales ORDER BY dateClosed DESC")
    fun getAllSales(): Flow<List<Sale>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: Sale)

    @Delete
    suspend fun deleteSale(sale: Sale)
}

@Database(
    entities = [
        BusinessSettings::class,
        ContactLead::class,
        MessageTemplate::class,
        Campaign::class,
        ProductService::class,
        FollowUp::class,
        Sale::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): WhatsCloseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "whats_close_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
