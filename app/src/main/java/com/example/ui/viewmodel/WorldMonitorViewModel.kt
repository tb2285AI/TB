package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.IntelBriefingEntity
import com.example.data.repository.IntelRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

data class MonitorMetric(
    val id: String,
    val title: String,
    val value: Int, // 0 - 100
    val category: String,
    val description: String,
    val trends: List<String>,
    val status: String, // Low, Guard, Elevated, Critical
    val sensorId: String
)

data class NewsEvent(
    val id: String,
    val title: String,
    val category: String,
    val date: String,
    val severity: String, // Info, Warning, Critical
    val summary: String,
    val location: String,
    val activeSensors: String
)

class WorldMonitorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = IntelRepository(application)

    // Room Persistent Breefings List
    val savedBriefings: StateFlow<List<IntelBriefingEntity>> = repository.allSavedBriefings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Current Monitor Metrics State
    var monitorMetrics by mutableStateOf(emptyList<MonitorMetric>())
        private set

    // Current News Events State
    var newsEvents by mutableStateOf(emptyList<NewsEvent>())
        private set

    // Search & Filter state
    var selectedCategoryFilter by mutableStateOf("All")
    var selectedSeverityFilter by mutableStateOf("All")
    var searchQuery by mutableStateOf("")

    // Active Tab: Dashboard ("MONITOR"), Custom Generator ("INTELLIGENCE BRIEFING"), Local Saved ("INTEL VAULT")
    var activeTab by mutableStateOf("MONITOR")

    // AI Dossier Generation State
    var selectedRegion by mutableStateOf("East Asia Sector")
    var selectedScenario by mutableStateOf("Resource Geopolitics & Mineral Scarcity")
    var customPromptPrompt by mutableStateOf("")
    var isGeneratingBriefing by mutableStateOf(false)
    var generatedBriefingText by mutableStateOf<String?>(null)
    var generatedBriefingError by mutableStateOf<String?>(null)

    // Dedicated state for AI analytical deep-dives of existing news items
    var selectedNewsItemForAnalysis by mutableStateOf<NewsEvent?>(null)
    var isAnalyzingNews by mutableStateOf(false)
    var newsAnalysisText by mutableStateOf<String?>(null)

    init {
        resetMetrics()
        resetNews()
    }

    fun resetMetrics() {
        monitorMetrics = listOf(
            MonitorMetric(
                id = "env",
                title = "Environmental Biosphere Stress",
                value = 54,
                category = "Environment",
                description = "Monitors planetary biosphere degradation, atmospheric heat dispersion, glacier shear stress, and freshwater depletion indicators.",
                trends = listOf("Glacial suture shear strain: +14%", "Oceanic thermocline shift: Elevating", "Arable land index: Stabilized"),
                status = "Guard",
                sensorId = "BIO-SEN-08"
            ),
            MonitorMetric(
                id = "cyber",
                title = "Cyber Threat Matrix Level",
                value = 78,
                category = "Cybersecurity",
                description = "Monitors aggregate global network vulnerability, quantum-decryption key attempts, cloud node attacks, and core optic highway intrusions.",
                trends = listOf("Brute decryption probes: Critical spike", "Hostile botnet activations: +45%", "DNS root defense systems: Constant friction"),
                status = "Elevated",
                sensorId = "CYB-TRX-99"
            ),
            MonitorMetric(
                id = "geo",
                title = "Geopolitical Tension Index",
                value = 82,
                category = "Geopolitics",
                description = "Monitors systemic friction, military posturing, maritime choke point density, embargo protocols, and active boundary friction vectors.",
                trends = listOf("Strait of Malacca sensor: Yellow alert", "Trade corridor restrictions: Escalated", "Boundary kinetic warnings: Dormant"),
                status = "Critical",
                sensorId = "GEO-POL-12"
            ),
            MonitorMetric(
                id = "econ",
                title = "Global Trade & Resource Friction",
                value = 46,
                category = "Economy",
                description = "Monitors rare earth metal supply stability, microchip semiconductor supply chain blockages, sovereign default indicators, and cargo routes.",
                trends = listOf("Lithium/Cobalt supply flow: Stable", "Cargo transit latency: +12%", "Sovereign currency delta: High variance"),
                status = "Low",
                sensorId = "ECO-MKT-04"
            ),
            MonitorMetric(
                id = "space",
                title = "Space Domain & Orbital Friction",
                value = 35,
                category = "Space",
                description = "Tracks orbit overcrowding density, anti-satellite kinetic test records, geomagnetic field disruptions, and solar flares.",
                trends = listOf("Debris density orbit LEO: Elevated", "Ionization sensor fluctuations: Normal", "Lunar mining posturing: Stable"),
                status = "Low",
                sensorId = "ORB-DEF-02"
            )
        )
    }

    fun resetNews() {
        newsEvents = listOf(
            NewsEvent(
                id = "news_1",
                title = "Distributed Sovereign Root DNS Probe Attack Recorded",
                category = "Cybersecurity",
                date = "2026-06-15 22:04",
                severity = "Critical",
                summary = "An unprecedented, highly coordinated quantum-assisted decryption probe targeted core internet DNS root nodes in the North Sea sector today. Intrusion defenses successfully absorbed 3.4 Terabits per second of malicious protocol packets before active system routing bypassed infected channels.",
                location = "North Sea Telemetry Deck",
                activeSensors = "CYB-TRX-99, SENSOR-GRID-C"
            ),
            NewsEvent(
                id = "news_2",
                title = "Major Shear Detected in Antarctic Ice-Shelf Boundary",
                category = "Environment",
                date = "2026-06-15 19:15",
                severity = "Warning",
                summary = "Environmental satellite constellations have recorded a three-kilometer physical fissure opening inside the Amundsen Sea Embayment ice sheet. Glacier sliding speed has spiked by 18%, predicting micro-coastal high tide anomalies in parts of the southern hemisphere within seventy-two hours.",
                location = "Antarctica Sector D",
                activeSensors = "BIO-SEN-08, COPERNICUS-VIII"
            ),
            NewsEvent(
                id = "news_3",
                title = "Secured Rare Earth Accord Dissolved unexpectedly",
                category = "Economy",
                date = "2026-06-14 11:30",
                severity = "Warning",
                summary = "Sovereign states in the Asia-Pacific trade consortium have terminated standard rare-metal sharing protocols. Global industrial lithium processors are warning of immediate secondary-market price surges of up to 45% for next-generation electric cells.",
                location = "Global Supply Network",
                activeSensors = "ECO-MKT-04, PACIFIC-M"
            ),
            NewsEvent(
                id = "news_4",
                title = "Low-Orbit Lunar Mining Core Operations Complete First Extract",
                category = "Space",
                date = "2026-06-13 08:31",
                severity = "Info",
                summary = "Autonomous mining shuttle networks successfully harvested and processed five metric tons of Helium-3 directly from primary lunar impact craters. The payload is scheduled to perform deep-vacuum orbit atmospheric entry for sustainable energy reactor injections in mid-August.",
                location = "Crater Copernicus (Lunar Space)",
                activeSensors = "ORB-DEF-02, APOLLO-XX"
            ),
            NewsEvent(
                id = "news_5",
                title = "Systemic Deep-Fusing Fusion Grid Reaches Sustainable Output",
                category = "Space",
                date = "2026-06-12 16:50",
                severity = "Info",
                summary = "Fusion scientists have verified continuous commercial power output totaling 820 Megawatts sustained for precisely 600 seconds. Grid operators have successfully integrated the discharge into local energy sectors, indicating real progress towards post-carbon grids.",
                location = "Cadarache Advanced Plasma Core",
                activeSensors = "NET-GRID-99"
            ),
            NewsEvent(
                id = "news_6",
                title = "Maritime Choke Point Blockade Imposed near Strait of Malacca",
                category = "Geopolitics",
                date = "2026-06-11 23:10",
                severity = "Critical",
                summary = "Unidentified naval skirmishes and severe electronic GPS jamming have effectively closed the primary Strait of Malacca transit corridor. Over 185 commercial petrochemical supertankers are redirected south to Sunda Strait, escalating international maritime fuel costs overnight.",
                location = "Strait of Malacca East Sector",
                activeSensors = "GEO-POL-12, SAT-SHIELD-V"
            )
        )
    }

    // Trigger local simulations
    fun simulateScenarioFlare(id: String) {
        monitorMetrics = monitorMetrics.map { metric ->
            if (metric.id == id) {
                // Raise index and recompute status
                val newValue = (metric.value + Random.nextInt(12, 24)).coerceAtMost(100)
                val newStatus = when {
                    newValue < 35 -> "Low"
                    newValue < 65 -> "Guard"
                    newValue < 85 -> "Elevated"
                    else -> "Critical"
                }

                // Add a dynamic alert event
                val newAlert = NewsEvent(
                    id = "news_sim_${System.currentTimeMillis()}",
                    title = "SITUATION ALERT: ${metric.title} Instability Flared",
                    category = metric.category,
                    date = "Live Sensor Feed",
                    severity = if (newValue > 80) "Critical" else "Warning",
                    summary = "Active telemetry sensors (ID: ${metric.sensorId}) have noted an irregular, rapid spike in the global ${metric.title} dashboard. Analytical subroutines report unstable parameters. Urgent local command assessment is advised.",
                    location = "Global Dashboard Sensor Grid",
                    activeSensors = metric.sensorId
                )
                newsEvents = listOf(newAlert) + newsEvents

                metric.copy(
                    value = newValue,
                    status = newStatus,
                    trends = listOf("Telemetry shift detected: +${newValue - metric.value}%", "Continuous sensor telemetry tracking: Active") + metric.trends.take(2)
                )
            } else {
                metric
            }
        }
    }

    // Generate custom intelligence briefing via Gemini API
    fun runGenerateBriefing() {
        if (isGeneratingBriefing) return
        isGeneratingBriefing = true
        generatedBriefingText = null
        generatedBriefingError = null

        viewModelScope.launch {
            try {
                val regionText = selectedRegion
                val scenarioText = selectedScenario
                val customPromptText = customPromptPrompt

                val result = repository.generateGeminiIntel(
                    region = regionText,
                    scenario = scenarioText,
                    customPrompt = customPromptText
                )

                if (result.startsWith("ERROR") || result.startsWith("ERROR_KEY")) {
                    generatedBriefingError = result
                } else {
                    generatedBriefingText = result
                }
            } catch (e: Exception) {
                generatedBriefingError = "Telemetry connection break: ${e.localizedMessage}"
            } finally {
                isGeneratingBriefing = false
            }
        }
    }

    // Direct AI news analysis
    fun runNewsAnalysis(event: NewsEvent) {
        if (isAnalyzingNews) return
        selectedNewsItemForAnalysis = event
        isAnalyzingNews = true
        newsAnalysisText = null

        viewModelScope.launch {
            try {
                val result = repository.analyzeNewsItem(
                    title = event.title,
                    summary = event.summary,
                    category = event.category
                )
                newsAnalysisText = result
            } catch (e: Exception) {
                newsAnalysisText = "Tactical decryption protocol failed: ${e.localizedMessage}"
            } finally {
                isAnalyzingNews = false
            }
        }
    }

    fun closeNewsAnalysis() {
        selectedNewsItemForAnalysis = null
        newsAnalysisText = null
        isAnalyzingNews = false
    }

    // Save active briefing to the Room database
    fun saveLoadedDossierToVault(notes: String = "") {
        val text = generatedBriefingText ?: return
        viewModelScope.launch {
            val title = "$selectedRegion SITREP: ${selectedScenario.take(30)}..."
            val entry = IntelBriefingEntity(
                title = title,
                scenario = selectedScenario,
                region = selectedRegion,
                threatLevel = if (text.contains("CRITICAL", ignoreCase = true)) "CRITICAL" else "ELEVATED",
                content = text,
                userNotes = notes
            )
            repository.saveBriefing(entry)
        }
    }

    // Save arbitrary custom text as briefing
    fun saveCustomBriefing(title: String, region: String, content: String, level: String) {
        viewModelScope.launch {
            val entry = IntelBriefingEntity(
                title = title,
                scenario = "AI Regional Analysis",
                region = region,
                threatLevel = level,
                content = content,
                userNotes = ""
            )
            repository.saveBriefing(entry)
        }
    }

    fun updateDossierNotes(id: Int, notes: String) {
        viewModelScope.launch {
            val existing = repository.getBriefingById(id) ?: return@launch
            repository.saveBriefing(existing.copy(userNotes = notes))
        }
    }

    fun deleteDossierFromVault(id: Int) {
        viewModelScope.launch {
            repository.deleteBriefing(id)
        }
    }

    // Filtered helper lists
    fun getFilteredNews(): List<NewsEvent> {
        return newsEvents.filter { event ->
            val matchesCategory = selectedCategoryFilter == "All" || event.category.equals(selectedCategoryFilter, ignoreCase = true)
            val matchesSeverity = selectedSeverityFilter == "All" || event.severity.equals(selectedSeverityFilter, ignoreCase = true)
            val matchesQuery = searchQuery.isEmpty() ||
                    event.title.contains(searchQuery, ignoreCase = true) ||
                    event.summary.contains(searchQuery, ignoreCase = true) ||
                    event.location.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSeverity && matchesQuery
        }
    }
}
