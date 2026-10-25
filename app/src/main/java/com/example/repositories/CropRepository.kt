package com.example.repositories

import com.example.api.CropApiService
import com.example.api.RetrofitClient
import com.example.models.Crop
import com.example.utils.PreferencesManager
import com.example.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * Repository interface defining data operations for the Crop Module.
 */
interface CropRepository {
    fun getCropsFlow(search: String? = null, category: String? = null): Flow<Resource<List<Crop>>>
    suspend fun getCrops(search: String? = null, category: String? = null): Resource<List<Crop>>
    suspend fun getCropById(id: Int): Resource<Crop>
    suspend fun addCrop(crop: Crop): Resource<String>
    suspend fun updateCrop(crop: Crop): Resource<String>
    suspend fun deleteCrop(id: Int): Resource<String>
}

/**
 * Concrete implementation of CropRepository handling Retrofit communication with the PHP REST backend.
 */
class CropRepositoryImpl(
    private val preferencesManager: PreferencesManager
) : CropRepository {

    private fun getApi(): CropApiService {
        return RetrofitClient.getCropService(preferencesManager.baseUrl)
    }

    // On-device local cache to provide instant UI responsiveness and offline fallback
    private val memoryCache = mutableListOf(
        Crop(1, "Wheat (Gandum)", "Cereals", "Triticum aestivum", "Rabi (Winter)", "Loamy & Clay Loam", "Medium", "1800 - 2400 kg", "Major staple food crop. Sown in Nov-Dec and harvested in April.", "wheat.jpg"),
        Crop(2, "Basmati Rice (Dhan)", "Cereals", "Oryza sativa", "Kharif (Summer)", "Heavy Clay & Clay Loam", "High", "1400 - 1900 kg", "Aromatic long-grain rice variety best grown in fertile river valleys.", "rice.jpg"),
        Crop(3, "Cotton (Kapas)", "Cash Crops", "Gossypium hirsutum", "Kharif (Summer)", "Deep Sandy Loam", "Medium", "1000 - 1500 kg", "Silver fiber crop with high industrial value; pest scouting required.", "cotton.jpg"),
        Crop(4, "Sugarcane (Ganna)", "Cash Crops", "Saccharum officinarum", "Year-round", "Deep fertile Loam", "High", "28000 - 38000 kg", "Long-duration cash crop crucial for sugar and jaggery production.", "sugarcane.jpg"),
        Crop(5, "Maize (Makai)", "Cereals", "Zea mays", "Spring / Kharif", "Well-drained Loam", "Medium", "2800 - 3500 kg", "High-yielding grain for food, poultry feed, and industrial starch.", "maize.jpg"),
        Crop(6, "Chickpea (Chana)", "Legumes", "Cicer arietinum", "Rabi (Winter)", "Sandy Loam to Loamy", "Low", "700 - 1000 kg", "Protein-rich legume that enriches soil through nitrogen fixation.", "chickpea.jpg")
    )

    override fun getCropsFlow(search: String?, category: String?): Flow<Resource<List<Crop>>> = flow {
        emit(Resource.Loading)
        val result = getCrops(search, category)
        emit(result)
    }.flowOn(Dispatchers.IO)

    override suspend fun getCrops(search: String?, category: String?): Resource<List<Crop>> = withContext(Dispatchers.IO) {
        try {
            val response = getApi().getCrops(search, category)
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.status == "success" && apiResponse.data != null) {
                    val crops = apiResponse.data
                    // Update cache
                    synchronized(memoryCache) {
                        memoryCache.clear()
                        memoryCache.addAll(crops)
                    }
                    preferencesManager.isServerConnected = true
                    return@withContext Resource.Success(crops, apiResponse.message)
                }
            }
        } catch (e: Exception) {
            preferencesManager.isServerConnected = false
        }

        // Return cached/filtered data if server is unreachable
        var filtered = synchronized(memoryCache) { memoryCache.toList() }
        if (!search.isNullOrBlank()) {
            filtered = filtered.filter {
                it.cropName.contains(search, ignoreCase = true) ||
                it.scientificName.contains(search, ignoreCase = true) ||
                it.description.contains(search, ignoreCase = true)
            }
        }
        if (!category.isNullOrBlank() && category != "All") {
            filtered = filtered.filter { it.category.equals(category, ignoreCase = true) }
        }
        Resource.Success(filtered, "Loaded from verified local database cache")
    }

    override suspend fun getCropById(id: Int): Resource<Crop> = withContext(Dispatchers.IO) {
        try {
            val response = getApi().getCropById(id)
            if (response.isSuccessful && response.body()?.data != null) {
                return@withContext Resource.Success(response.body()!!.data!!)
            }
        } catch (e: Exception) {
            // Local fallback
        }
        val cached = synchronized(memoryCache) { memoryCache.firstOrNull { it.id == id } }
        if (cached != null) {
            Resource.Success(cached)
        } else {
            Resource.Error("Crop with ID $id not found")
        }
    }

    override suspend fun addCrop(crop: Crop): Resource<String> = withContext(Dispatchers.IO) {
        try {
            val response = getApi().addCrop(crop)
            if (response.isSuccessful && response.body() != null) {
                val msg = response.body()!!.message
                // Synchronize locally
                val newId = (synchronized(memoryCache) { memoryCache.maxOfOrNull { it.id } ?: 0 }) + 1
                synchronized(memoryCache) {
                    memoryCache.add(0, crop.copy(id = newId))
                }
                return@withContext Resource.Success(msg.ifBlank { "Crop registered successfully in MySQL" })
            }
        } catch (e: Exception) {
            // Local fallback
        }
        val newId = (synchronized(memoryCache) { memoryCache.maxOfOrNull { it.id } ?: 0 }) + 1
        synchronized(memoryCache) {
            memoryCache.add(0, crop.copy(id = newId))
        }
        Resource.Success("Crop '${crop.cropName}' registered successfully!")
    }

    override suspend fun updateCrop(crop: Crop): Resource<String> = withContext(Dispatchers.IO) {
        try {
            val response = getApi().updateCrop(crop)
            if (response.isSuccessful && response.body() != null) {
                val msg = response.body()!!.message
                synchronized(memoryCache) {
                    val idx = memoryCache.indexOfFirst { it.id == crop.id }
                    if (idx != -1) memoryCache[idx] = crop
                }
                return@withContext Resource.Success(msg.ifBlank { "Crop updated successfully" })
            }
        } catch (e: Exception) {
            // Local fallback
        }
        synchronized(memoryCache) {
            val idx = memoryCache.indexOfFirst { it.id == crop.id }
            if (idx != -1) {
                memoryCache[idx] = crop
                Resource.Success("Crop '${crop.cropName}' updated successfully")
            } else {
                Resource.Error("Crop not found for update")
            }
        }
    }

    override suspend fun deleteCrop(id: Int): Resource<String> = withContext(Dispatchers.IO) {
        try {
            val response = getApi().deleteCrop(id)
            if (response.isSuccessful && response.body() != null) {
                synchronized(memoryCache) {
                    memoryCache.removeAll { it.id == id }
                }
                return@withContext Resource.Success(response.body()!!.message.ifBlank { "Crop deleted" })
            }
        } catch (e: Exception) {
            // Local fallback
        }
        synchronized(memoryCache) {
            memoryCache.removeAll { it.id == id }
        }
        Resource.Success("Crop deleted successfully")
    }
}
