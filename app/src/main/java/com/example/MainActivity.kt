package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.database.IntelBriefingEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.MonitorMetric
import com.example.ui.viewmodel.NewsEvent
import com.example.ui.viewmodel.WorldMonitorViewModel
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    private val viewModel: WorldMonitorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding(),
                    containerColor = DarkBackground
                ) { innerPadding ->
                    CommandCenterScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun CommandCenterScreen(
    viewModel: WorldMonitorViewModel,
    modifier: Modifier = Modifier
) {
    var currentTimeString by remember { mutableStateOf("00:00:00 UTC") }

    // Periodically update the high-tech tactical clock
    LaunchedEffect(Unit) {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        while (true) {
            currentTimeString = sdf.format(Date())
            kotlinx.coroutines.delay(1000)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(14.dp)
    ) {
        // --- 1. SYSTEM HEADER ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurface)
                .border(BorderStroke(1.dp, GridLine), RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(50))
                            .background(CyberGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "WORLD OPERATIONS MONITOR",
                        color = CyberCyan,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = 0.5.sp
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "AETHER TERMINAL // OPERATIONAL MONITOR ACTIVE",
                    color = TacticalGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 0.5.sp
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = currentTimeString,
                    color = CyberGreen,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "STATUS: CONTROL ACTIVE",
                    color = CyberCyan.copy(alpha = 0.8f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- 2. TACTICAL TAB SELECTOR ROW ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurface)
                .border(BorderStroke(1.dp, GridLine), RoundedCornerShape(16.dp))
                .padding(6.dp)
        ) {
            val tabs = listOf(
                "MONITOR" to "SYSTEM MONITOR",
                "BRIEFING" to "AI CRISIS BRIEF",
                "VAULT" to "INTEL VAULT"
            )
            tabs.forEach { (key, title) ->
                val isSelected = viewModel.activeTab == key
                val backgroundColor = if (isSelected) CyberCyan else Color.Transparent
                val contentColor = if (isSelected) Purple40 else TacticalGray

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(backgroundColor)
                        .clickable { viewModel.activeTab = key }
                        .padding(vertical = 12.dp)
                        .testTag("tab_$key"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = contentColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.SansSerif,
                        textAlign = TextAlign.Center,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- 3. MAIN WORKSPACE WITH VIEWPORT ROUTING ---
        Box(modifier = Modifier.weight(1f)) {
            when (viewModel.activeTab) {
                "MONITOR" -> SystemMonitorLayout(viewModel)
                "BRIEFING" -> AITacticalBriefingLayout(viewModel)
                "VAULT" -> IntelVaultLayout(viewModel)
            }
        }
    }
}

// ==================== [TAB 1: SYSTEM MONITOR LAYOUT] ====================
@Composable
fun SystemMonitorLayout(viewModel: WorldMonitorViewModel) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(modifier = Modifier.fillMaxSize()) {
        // Metric Sliders Board Title
        Text(
            text = "► GEO-INDEX SENSOR CONSOLE",
            color = CyberGreen,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        // Metrics Horizontal Scroll Deck
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(viewModel.monitorMetrics) { metric ->
                MetricCard(
                    metric = metric,
                    onFlare = { viewModel.simulateScenarioFlare(metric.id) }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // News Alert Feed header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "► WORLD INTELLIGENCE NEWS FEED",
                color = CyberCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Row(
                modifier = Modifier.clickable {
                    viewModel.resetNews()
                    viewModel.resetMetrics()
                },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset Feed",
                    tint = CyberCyan,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "RESET SENSORS",
                    color = CyberCyan,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Segmented Feeds Controller: Local Scan vs. Live Satellite
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (viewModel.newsFeedMode == "LOCAL") CyberCyan.copy(alpha = 0.15f) else DarkSurface)
                    .border(BorderStroke(1.dp, if (viewModel.newsFeedMode == "LOCAL") CyberCyan else GridLine), RoundedCornerShape(12.dp))
                    .clickable { viewModel.newsFeedMode = "LOCAL" }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Local scan",
                        tint = if (viewModel.newsFeedMode == "LOCAL") CyberCyan else TacticalGray,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LOCAL SCANNER",
                        color = if (viewModel.newsFeedMode == "LOCAL") CyberCyan else TacticalGray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (viewModel.newsFeedMode == "LIVE") CyberGreen.copy(alpha = 0.15f) else DarkSurface)
                    .border(BorderStroke(1.dp, if (viewModel.newsFeedMode == "LIVE") CyberGreen else GridLine), RoundedCornerShape(12.dp))
                    .clickable {
                        viewModel.newsFeedMode = "LIVE"
                        viewModel.fetchLiveNews()
                    }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Live feed",
                        tint = if (viewModel.newsFeedMode == "LIVE") CyberGreen else TacticalGray,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIVE SATELLITE",
                        color = if (viewModel.newsFeedMode == "LIVE") CyberGreen else TacticalGray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // If LIVE, show sources row
        if (viewModel.newsFeedMode == "LIVE") {
            val liveChannels = listOf("BBC Global", "CNN Intelligence", "Al Jazeera", "Sovereign General", "Cyber & Tech Matrix")
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(liveChannels) { channel ->
                    val isSel = viewModel.selectedLiveChannel == channel
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSel) CyberGreen else DarkSurfaceElevated)
                            .border(BorderStroke(1.dp, if (isSel) CyberGreen else GridLine), RoundedCornerShape(20.dp))
                            .clickable {
                                viewModel.selectedLiveChannel = channel
                                viewModel.fetchLiveNews()
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = channel.uppercase(),
                            color = if (isSel) Purple40 else Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Search Bar and Filters Case
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurface)
                .border(BorderStroke(1.dp, GridLine), RoundedCornerShape(12.dp))
                .padding(8.dp)
        ) {
            OutlinedTextField(
                value = viewModel.searchQuery,
                onValueChange = { viewModel.searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("search_news_input"),
                placeholder = {
                    Text(
                        if (viewModel.newsFeedMode == "LIVE") "Search live geopolitical headlines..." else "Search local news summaries...",
                        color = TacticalGray.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.SansSerif
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = if (viewModel.newsFeedMode == "LIVE") CyberGreen else CyberCyan,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (viewModel.searchQuery.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = TacticalGray,
                            modifier = Modifier
                                .clickable { viewModel.searchQuery = "" }
                                .size(18.dp)
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = if (viewModel.newsFeedMode == "LIVE") CyberGreen else CyberCyan,
                    unfocusedBorderColor = GridLine,
                    focusedContainerColor = DarkBackground,
                    unfocusedContainerColor = DarkBackground
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, fontFamily = FontFamily.SansSerif),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() })
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Categories horizontal filter chips
            val categories = listOf("All", "Cybersecurity", "Geopolitics", "Environment", "Economy", "Space")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(categories) { cat ->
                    val isCatSelected = viewModel.selectedCategoryFilter == cat
                    val accentCol = if (viewModel.newsFeedMode == "LIVE") CyberGreen else CyberCyan
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isCatSelected) accentCol.copy(alpha = 0.15f) else Color.Transparent)
                            .clickable { viewModel.selectedCategoryFilter = cat }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .drawBehind {
                                if (isCatSelected) {
                                    drawLine(
                                        color = accentCol,
                                        start = Offset(0f, size.height),
                                        end = Offset(size.width, size.height),
                                        strokeWidth = 2.dp.toPx()
                                    )
                                }
                            }
                    ) {
                        Text(
                            text = cat.uppercase(),
                            color = if (isCatSelected) accentCol else TacticalGray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // News List Display & Loading/Errors Handling
        if (viewModel.newsFeedMode == "LIVE" && viewModel.isFetchingLiveNews) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurface)
                    .border(BorderStroke(1.dp, GridLine), RoundedCornerShape(16.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = CyberGreen,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "ESTABLISHING LIVE FEED LINK...",
                        color = CyberGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Downloading telemetry for '${viewModel.selectedLiveChannel.uppercase()}'",
                        color = TacticalGray,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.SansSerif,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else if (viewModel.newsFeedMode == "LIVE" && viewModel.liveNewsError != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurface)
                    .border(BorderStroke(1.dp, CyberRed), RoundedCornerShape(16.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Error",
                        tint = CyberRed,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Satellite link failed to resolve",
                        color = CyberRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = viewModel.liveNewsError ?: "",
                        color = TacticalGray,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.SansSerif,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { viewModel.fetchLiveNews() },
                        modifier = Modifier.testTag("news_retry_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberRed.copy(alpha = 0.15f),
                            contentColor = CyberRed
                        ),
                        border = BorderStroke(1.dp, CyberRed.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Retry Icon",
                                tint = CyberRed,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "RETRY SATELLITE LINK",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.SansSerif,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        } else {
            val filteredNews = viewModel.getFilteredNews()
            if (filteredNews.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurface)
                        .border(BorderStroke(1.dp, GridLine), RoundedCornerShape(12.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Alerts Empty",
                            tint = CyberAmber,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "ZERO ANOMALIES DETECTED FOR SELECTION",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Adjust search query or filter switches to restore feed.",
                            color = TacticalGray,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.SansSerif,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredNews, key = { it.id }) { news ->
                        NewsItemCard(
                            event = news,
                            onAnalyze = { viewModel.runNewsAnalysis(news) }
                        )
                    }
                }
            }
        }
    }

    // Interactive Modal Dialog: Live AI Strategic News Decrypter
    val activeAnalysisEvent = viewModel.selectedNewsItemForAnalysis
    if (activeAnalysisEvent != null) {
        Dialog(onDismissRequest = { viewModel.closeNewsAnalysis() }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface),
                border = BorderStroke(1.5.dp, CyberCyan),
                color = DarkSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Intelligence",
                                tint = CyberCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "AI STRATEGIC DECRYPTION MODEL",
                                color = CyberCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        IconButton(
                            onClick = { viewModel.closeNewsAnalysis() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Decryption",
                                tint = TacticalGray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = activeAnalysisEvent.title.uppercase(),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Source: ${activeAnalysisEvent.location}  | Sensor: ${activeAnalysisEvent.activeSensors}",
                        color = TacticalGray,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Divider(color = GridLine, modifier = Modifier.padding(vertical = 8.dp))

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkBackground)
                            .padding(10.dp)
                    ) {
                        if (viewModel.isAnalyzingNews) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                LinearProgressIndicator(
                                    color = CyberCyan,
                                    trackColor = GridLine,
                                    modifier = Modifier.fillMaxWidth(0.7f)
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "SYNTAX ANALYSIS PROTOCOL INITIALIZING...",
                                    color = CyberCyan,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "Querying model 'gemini-3.5-flash' over telemetry channels",
                                    color = TacticalGray,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        } else {
                            val responseText = viewModel.newsAnalysisText
                            if (responseText != null) {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    item {
                                        Text(
                                            text = responseText,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            lineHeight = 18.sp
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Button(
                                            onClick = {
                                                viewModel.saveCustomBriefing(
                                                    title = "ANALYSIS: ${activeAnalysisEvent.title.take(30)}...",
                                                    region = activeAnalysisEvent.location,
                                                    content = responseText,
                                                    level = activeAnalysisEvent.severity.uppercase()
                                                )
                                                viewModel.closeNewsAnalysis()
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("pin_news_analysis_button"),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = CyberCyan,
                                                contentColor = Color(0xFF0F141C)
                                            ),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Pin To Vault",
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    "PIN STRATEGIC ANALYSIS TO INTEL VAULT",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                Text(
                                    text = "Tactical network breakdown. Failed to pull analyst stream.",
                                    color = CyberRed,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    metric: MonitorMetric,
    onFlare: () -> Unit
) {
    val statusColor = when (metric.status) {
        "Low" -> CyberGreen
        "Guard" -> CyberCyan
        "Elevated" -> CyberAmber
        else -> CyberRed
    }

    Box(
        modifier = Modifier
            .width(220.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurface)
            .border(BorderStroke(1.dp, GridLine), RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = metric.sensorId,
                    color = TacticalGray,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(statusColor.copy(alpha = 0.2f))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = metric.status.uppercase(),
                        color = statusColor,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = metric.title,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Progress slider meter
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(DarkBackground)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(metric.value / 100f)
                            .fillMaxHeight()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(statusColor.copy(alpha = 0.6f), statusColor)
                                )
                            )
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "${metric.value}%",
                    color = statusColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Flare scenario button
            Button(
                onClick = onFlare,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .testTag("flare_button_${metric.id}"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberRed.copy(alpha = 0.15f),
                    contentColor = CyberRed
                ),
                contentPadding = PaddingValues(0.dp),
                border = BorderStroke(1.dp, CyberRed.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Simulate",
                        tint = CyberRed,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "FLARE SCENARIO",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun NewsItemCard(
    event: NewsEvent,
    onAnalyze: () -> Unit
) {
    val levelColor = when (event.severity) {
        "Critical" -> CyberRed
        "Warning" -> CyberAmber
        else -> CyberCyan
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurface)
            .border(BorderStroke(1.dp, GridLine), RoundedCornerShape(8.dp))
    ) {
        Column {
            // Severity indicator bar on top
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(levelColor)
            )

            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${event.category.uppercase()} // ${event.date}",
                        color = TacticalGray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "LVL: ${event.severity.uppercase()}",
                        color = levelColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = event.title.uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = event.summary,
                    color = Color.White.copy(alpha = 0.82f),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "COORDS: ${event.location}",
                        color = TacticalGray,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Button(
                        onClick = onAnalyze,
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("analyze_news_${event.id}"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberCyan.copy(alpha = 0.15f),
                            contentColor = CyberCyan
                        ),
                        border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Run Decryption",
                                tint = CyberCyan,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "STRATEGIC AI BRIEF",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}


// ==================== [TAB 2: AI CRISIS BRIEFING LAYOUT] ====================
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AITacticalBriefingLayout(viewModel: WorldMonitorViewModel) {
    var customNotesText by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    val regionsList = listOf(
        "East Asia Grid Node",
        "Arctic Polar Cap Corridor",
        "Suez-Malacca Choke Point",
        "Sub-Saharan Mineral Belt",
        "Europe Core Financial Route",
        "North American Grid Matrix"
    )

    val scenariosList = listOf(
        "Rare Earth Geopolitics & Raw Scarcity",
        "Distributed Grid Infiltration & Quantum Decryption",
        "Sudden Biosphere Shifts & Coastal Retreat",
        "Active Kinetic Border Tension & Naval Blockade",
        "Lunar Extraction Contention & Space Dominance"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "► TACTICAL SITUATION SIMULATION PROTOCOL",
                color = CyberCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "Use the Gemini matrix to run predictive analyses of geopolitical and environmental crises.",
                color = TacticalGray,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Drop-down select boxes for Region & Scenario
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurface)
                    .border(BorderStroke(1.dp, GridLine), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "TARGET MONITOR ZONE //",
                    color = CyberGreen,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Region selector chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(regionsList) { item ->
                        val isSel = viewModel.selectedRegion == item
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSel) CyberCyan else DarkBackground)
                                .border(BorderStroke(1.dp, if (isSel) CyberCyan else GridLine), RoundedCornerShape(4.dp))
                                .clickable { viewModel.selectedRegion = item }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = item.uppercase(),
                                color = if (isSel) Color(0xFF0F141C) else Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "CRITICAL SITREP THREAT VECTORS //",
                    color = CyberAmber,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Scenario Selector scroll row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(scenariosList) { item ->
                        val isSel = viewModel.selectedScenario == item
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSel) CyberAmber else DarkBackground)
                                .border(BorderStroke(1.dp, if (isSel) CyberAmber else GridLine), RoundedCornerShape(4.dp))
                                .clickable { viewModel.selectedScenario = item }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = item.uppercase(),
                                color = if (isSel) Color(0xFF0F141C) else Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "OPTIONAL SPECIAL COMMAND OVERRIDES or PROMPTS //",
                    color = TacticalGray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = viewModel.customPromptPrompt,
                    onValueChange = { viewModel.customPromptPrompt = it },
                    placeholder = {
                        Text(
                            "Inject custom telemetry instructions, e.g., 'focus on deep sea fiber optics'...",
                            color = TacticalGray.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_dossier_prompt"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = GridLine,
                        focusedContainerColor = DarkBackground,
                        unfocusedContainerColor = DarkBackground
                    ),
                    textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, fontFamily = FontFamily.Monospace),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() })
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        keyboardController?.hide()
                        viewModel.runGenerateBriefing()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("generate_briefing_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberCyan,
                        contentColor = Color(0xFF0F141C)
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Briefing",
                            tint = Color(0xFF0F141C)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "GENERATE SECURE SITREP REPORT [SITREP]",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Active State of Generation Output
        item {
            AnimatedContent(targetState = viewModel.isGeneratingBriefing) { isGenerating ->
                if (isGenerating) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurface)
                            .border(BorderStroke(1.dp, CyberCyan), RoundedCornerShape(8.dp))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = CyberCyan)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "ESTABLISHING QUANTUM DECRYPTION HANDSHAKE...",
                                color = CyberCyan,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Running scenario model on regional sector grid telemetry...",
                                color = TacticalGray,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                } else {
                    val result = viewModel.generatedBriefingText
                    val error = viewModel.generatedBriefingError

                    if (error != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurface)
                                .border(BorderStroke(1.dp, CyberRed), RoundedCornerShape(8.dp))
                                .padding(14.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Alert",
                                        tint = CyberRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "TELEMETRY NETWORK REFUSAL",
                                        color = CyberRed,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = error,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    } else if (result != null) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurface)
                                    .border(BorderStroke(1.dp, CyberCyan), RoundedCornerShape(8.dp))
                                    .padding(14.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "DECRYPTED SITUATION REPORT [SITREP]",
                                            color = CyberGreen,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(CyberGreen.copy(alpha = 0.2f))
                                                .padding(horizontal = 5.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "VERIFIED INCOMING",
                                                color = CyberGreen,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }

                                    Divider(color = GridLine, modifier = Modifier.padding(vertical = 8.dp))

                                    Text(
                                        text = result,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        lineHeight = 18.sp
                                    )

                                    Divider(color = GridLine, modifier = Modifier.padding(vertical = 8.dp))

                                    Text(
                                        text = "COMMAND DEPUTY NOTES (SAVE COMPOSITION)",
                                        color = TacticalGray,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))

                                    OutlinedTextField(
                                        value = customNotesText,
                                        onValueChange = { customNotesText = it },
                                        placeholder = {
                                            Text(
                                                "Intermix individual notes or directives before pinning to logs...",
                                                color = TacticalGray.copy(alpha = 0.4f),
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("vault_notes_field"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = CyberCyan,
                                            unfocusedBorderColor = GridLine,
                                            focusedContainerColor = DarkBackground,
                                            unfocusedContainerColor = DarkBackground
                                        ),
                                        textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Button(
                                        onClick = {
                                            viewModel.saveLoadedDossierToVault(customNotesText)
                                            // Reset inputs
                                            customNotesText = ""
                                            viewModel.generatedBriefingText = null
                                            viewModel.activeTab = "VAULT"
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("pin_to_vault_button"),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = CyberGreen,
                                            contentColor = Color(0xFF0F141C)
                                        ),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Save Briefing",
                                                tint = Color(0xFF0F141C),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                "PIN DECRYPTED DOSSIER TO INTEL VAULT",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace
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
        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}


// ==================== [TAB 3: LOCAL INTEL VAULT LAYOUT] ====================
@Composable
fun IntelVaultLayout(viewModel: WorldMonitorViewModel) {
    val savedDossiers by viewModel.savedBriefings.collectAsStateWithLifecycle()
    var savedSearchQuery by remember { mutableStateOf("") }
    var expandedBriefingId by remember { mutableStateOf<Int?>(null) }
    var notesEditingText by remember { mutableStateOf("") }

    val filteredSaved = savedDossiers.filter { brief ->
        savedSearchQuery.isEmpty() ||
                brief.title.contains(savedSearchQuery, ignoreCase = true) ||
                brief.content.contains(savedSearchQuery, ignoreCase = true) ||
                brief.region.contains(savedSearchQuery, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "► SECURED COMMAND INTEL VAULT",
            color = CyberGreen,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = "Review and catalog pinned situation dashboards and AI reports stored offline inside Room database.",
            color = TacticalGray,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Search Bar inside saved logs
        OutlinedTextField(
            value = savedSearchQuery,
            onValueChange = { savedSearchQuery = it },
            placeholder = {
                Text(
                    "Search saved intelligence logs...",
                    color = TacticalGray.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Logs",
                    tint = CyberCyan,
                    modifier = Modifier.size(18.dp)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .testTag("vault_search_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = CyberCyan,
                unfocusedBorderColor = GridLine,
                focusedContainerColor = DarkSurface,
                unfocusedContainerColor = DarkSurface
            ),
            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, fontFamily = FontFamily.Monospace)
        )

        if (filteredSaved.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurface)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Empty Vault",
                        tint = TacticalGray.copy(alpha = 0.5f),
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "SECURED INTEL VAULT EMPTY",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Run SITREP models in the AI Brief tab or strategic news decryption analyses to pin critical briefings.",
                        color = TacticalGray,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredSaved, key = { it.id }) { dossier ->
                    val isExpanded = expandedBriefingId == dossier.id
                    val formattedDate = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(dossier.timestamp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurface)
                            .border(
                                BorderStroke(1.dp, if (isExpanded) CyberCyan else GridLine),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                if (isExpanded) {
                                    expandedBriefingId = null
                                } else {
                                    expandedBriefingId = dossier.id
                                    notesEditingText = dossier.userNotes
                                }
                            }
                            .padding(12.dp)
                            .testTag("saved_item_${dossier.id}")
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1.0f)) {
                                    Text(
                                        text = "${dossier.region.uppercase()} // CLASSIFIED INTEL",
                                        color = CyberGreen,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = dossier.title.uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace,
                                        maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (dossier.threatLevel == "CRITICAL") CyberRed.copy(alpha = 0.2f) else CyberCyan.copy(alpha = 0.2f))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = dossier.threatLevel,
                                        color = if (dossier.threatLevel == "CRITICAL") CyberRed else CyberCyan,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            Text(
                                text = "Logged Date: $formattedDate",
                                color = TacticalGray,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(top = 2.dp)
                            )

                            // Brief collapsed snapshot
                            if (!isExpanded) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = dossier.content.take(80) + "...",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                if (dossier.userNotes.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 6.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(DarkBackground)
                                            .padding(horizontal = 6.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "★ NOTES: ${dossier.userNotes.take(30)}...",
                                            color = CyberAmber,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            } else {
                                // Full expanded dossier layout
                                Divider(color = GridLine, modifier = Modifier.padding(vertical = 8.dp))

                                Text(
                                    text = dossier.content,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 18.sp
                                )

                                Divider(color = GridLine, modifier = Modifier.padding(vertical = 8.dp))

                                // Interactive Command Notes editing
                                Text(
                                    text = " ★ COMMAND DIRECTIVE NOTES (PERSISTENT LOGS) //",
                                    color = CyberAmber,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                OutlinedTextField(
                                    value = notesEditingText,
                                    onValueChange = { notesEditingText = it },
                                    placeholder = {
                                        Text(
                                            "Add local strategic command orders, telemetry updates...",
                                            color = TacticalGray.copy(alpha = 0.4f),
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("edit_notes_${dossier.id}"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = CyberCyan,
                                        unfocusedBorderColor = GridLine,
                                        focusedContainerColor = DarkBackground,
                                        unfocusedContainerColor = DarkBackground
                                    ),
                                    textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Button(
                                        onClick = {
                                            viewModel.updateDossierNotes(dossier.id, notesEditingText)
                                            expandedBriefingId = null
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = CyberCyan,
                                            contentColor = Color(0xFF0F141C)
                                        ),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.testTag("save_notes_btn_${dossier.id}")
                                    ) {
                                        Text(
                                            "SAVE DIRECTIVE NOTES",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.deleteDossierFromVault(dossier.id)
                                            expandedBriefingId = null
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = CyberRed.copy(alpha = 0.15f),
                                            contentColor = CyberRed
                                        ),
                                        border = BorderStroke(1.dp, CyberRed.copy(alpha = 0.5f)),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.testTag("delete_brief_btn_${dossier.id}")
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = CyberRed,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                "PURGELOG",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
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
    }
}
