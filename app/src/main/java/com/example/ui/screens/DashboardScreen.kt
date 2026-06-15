package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.AuraViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    viewModel: AuraViewModel,
    modifier: Modifier = Modifier
) {
    val journalEntries by viewModel.journalEntries.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val aiReport = viewModel.aiReport
    val chatMessages by viewModel.chatMessages.collectAsState()
    
    val coroutineScope = rememberCoroutineScope()
    val chatListState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current

    var chatInput by remember { mutableStateOf("") }
    var quickNote by remember { mutableStateOf("") }
    var selectedQuickEmoji by remember { mutableStateOf("😊") }
    var quickMoodScore by remember { mutableIntStateOf(4) }

    // Auto scroll chat to bottom when message list changes
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            chatListState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    // Proactively launch mood review if needed
    LaunchedEffect(journalEntries) {
        if (journalEntries.isNotEmpty() && viewModel.aiReport == null) {
            viewModel.analyzeRecentMood()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header Card
        item {
            HeaderCard()
        }

        // Quick Daily Mood Logger Component
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                ),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quick_mood_logger_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Enregistrer votre humeur du jour 📝",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Text(
                        text = "Sélectionnez votre ressenti et ajoutez une courte note journalière.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Sentiment Icons Bar (Lucide styled)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val quickMoods = listOf(
                            Triple("💖", 5, "Génial"),
                            Triple("😊", 4, "Bien"),
                            Triple("😐", 3, "Neutre"),
                            Triple("😔", 2, "Anxieux"),
                            Triple("😢", 1, "Triste")
                        )
                        
                        quickMoods.forEach { (emoji, score, labelName) ->
                            val isSelected = selectedQuickEmoji == emoji
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                        else Color.Transparent
                                    )
                                    .clickable {
                                        selectedQuickEmoji = emoji
                                        quickMoodScore = score
                                    }
                                    .testTag("quick_emoji_$emoji")
                                    .padding(8.dp)
                            ) {
                                Icon(
                                    imageVector = when (score) {
                                        5 -> Icons.Outlined.SentimentVerySatisfied
                                        4 -> Icons.Outlined.SentimentSatisfied
                                        3 -> Icons.Outlined.SentimentNeutral
                                        2 -> Icons.Outlined.SentimentDissatisfied
                                        else -> Icons.Outlined.SentimentVeryDissatisfied
                                    },
                                    contentDescription = labelName,
                                    tint = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        when (score) {
                                            5 -> Color(0xFFFF5722)
                                            4 -> Color(0xFF4CAF50)
                                            3 -> Color(0xFFFFC107)
                                            2 -> Color(0xFF9C27B0)
                                            else -> Color(0xFF2196F3)
                                        }
                                    },
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = labelName,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary 
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Short Note TextField
                    OutlinedTextField(
                        value = quickNote,
                        onValueChange = { quickNote = it },
                        placeholder = { Text("Comment s'est passée votre journée ? (note rapide)", fontSize = 13.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("quick_note_input_field"),
                        textStyle = TextStyle(fontSize = 13.sp)
                    )

                    Button(
                        onClick = {
                            if (quickNote.isNotBlank()) {
                                viewModel.addJournalEntry(
                                    title = "Humeur du jour",
                                    content = quickNote,
                                    moodEmoji = selectedQuickEmoji,
                                    moodScore = quickMoodScore
                                )
                                quickNote = "" // reset
                            } else {
                                // If note is blank, log with generic state text
                                viewModel.addJournalEntry(
                                    title = "Humeur du jour",
                                    content = "Enregistrement rapide d'humeur : ressenti de la journée.",
                                    moodEmoji = selectedQuickEmoji,
                                    moodScore = quickMoodScore
                                )
                            }
                            // Proactively request AI analysis immediately!
                            viewModel.analyzeRecentMood()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("quick_save_mood_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Enregistrer l'humeur", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // --- SECTION : SUIVI JOURNALIER PREMIUM (MODE COMPLET AVEC ENREGISTREMENTS THEMATIQUES) ---
        item {
            DailyPremiumTracker(viewModel = viewModel)
        }

        // --- HOOK UNIQUE ET ADDICTIF : REACTEUR DE RESONANCE ALCHIMIQUE D'AURA ---
        item {
            SoulResonanceReactor(viewModel = viewModel)
        }

        // 2. Stats Row
        item {
            val moodText = if (journalEntries.isNotEmpty()) {
                journalEntries.first().moodEmoji
            } else "N/A"
            
            val totalCompleted = tasks.count { it.isCompleted }
            val taskStatText = if (tasks.isNotEmpty()) "$totalCompleted/${tasks.size}" else "0"

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatItem(
                    title = "Mon Humeur",
                    value = moodText,
                    icon = Icons.Outlined.Favorite,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    title = "Mes Tâches",
                    value = taskStatText,
                    icon = Icons.Outlined.CheckCircle,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    title = "Journal",
                    value = "${journalEntries.size} entrées",
                    icon = Icons.Outlined.Book,
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 3. AI Mood Report Section
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.AutoAwesome,
                                contentDescription = "Mood Assessment Icon",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Bilan Émotionnel IA",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        IconButton(
                            onClick = { viewModel.analyzeRecentMood() },
                            enabled = !viewModel.isAnalyzingMood && journalEntries.isNotEmpty(),
                            modifier = Modifier.testTag("refresh_ai_report")
                        ) {
                            if (viewModel.isAnalyzingMood) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.Refresh,
                                    contentDescription = "Refrechir l'analyse",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (journalEntries.isEmpty()) {
                        Text(
                            text = "Rédigez d'abord des entrées de journal pour que Mirys puisse analyser vos variations d'humeur.",
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else if (aiReport != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Circular Score Indicator
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                                Color.Transparent
                                            )
                                        ),
                                        shape = CircleShape
                                    )
                            ) {
                                CircularProgressIndicator(
                                    progress = { aiReport.score / 100f },
                                    modifier = Modifier.fillMaxSize(),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                    strokeWidth = 6.dp,
                                )
                                Text(
                                    text = "${aiReport.score}%",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = "Indice d'Énergie Vital",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = when {
                                        aiReport.score >= 80 -> "Énergie Excellente 🌟"
                                        aiReport.score >= 60 -> "Stabilité Sereine ✨"
                                        aiReport.score >= 40 -> "Besoin de Repos 🌙"
                                        else -> "Soutien Nécessaire Recharge 💕"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = aiReport.description,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                        Spacer(modifier = Modifier.height(12.dp))

                        // Advisory Title
                        Text(
                            text = "Suggestions Bien-être",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.align(Alignment.Start)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // 3 Tips Items
                        aiReport.tips.forEachIndexed { idx, tip ->
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                            CircleShape
                                        )
                                ) {
                                    Text(
                                        text = "${idx + 1}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = tip,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    } else {
                        // Report is compiling empty
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                        ) {
                            Text(
                                text = "Lancement de l'analyse...",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 4. Compact Interactive AI Companion Chat Card Widget
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Chat Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Chat,
                                contentDescription = "AI chat icon",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Discussion IA",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Compagnon Mirys",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.clearChat() },
                            modifier = Modifier.size(32.dp).testTag("clear_chat_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteSweep,
                                contentDescription = "Effacer l'historique",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    Spacer(modifier = Modifier.height(10.dp))

                    // Dialog history list
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
                                RoundedCornerShape(16.dp)
                            )
                            .padding(8.dp)
                    ) {
                        LazyColumn(
                            state = chatListState,
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 8.dp)
                        ) {
                            items(chatMessages) { (msg, isUser) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(
                                                RoundedCornerShape(
                                                    topStart = 16.dp,
                                                    topEnd = 16.dp,
                                                    bottomStart = if (isUser) 16.dp else 4.dp,
                                                    bottomEnd = if (isUser) 4.dp else 16.dp
                                                )
                                            )
                                            .background(
                                                if (isUser) MaterialTheme.colorScheme.primary 
                                                else MaterialTheme.colorScheme.surfaceVariant
                                            )
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                            .widthIn(max = 240.dp)
                                    ) {
                                        Text(
                                            text = msg,
                                            fontSize = 13.sp,
                                            lineHeight = 18.sp,
                                            color = if (isUser) MaterialTheme.colorScheme.onPrimary 
                                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            if (viewModel.isChatLoading) {
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Start
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                text = "Mirys réfléchit...",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Input Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = chatInput,
                            onValueChange = { chatInput = it },
                            placeholder = { Text("Posez une question...", fontSize = 13.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = {
                                if (chatInput.isNotBlank() && !viewModel.isChatLoading) {
                                    viewModel.sendMessageToAi(chatInput)
                                    chatInput = ""
                                    keyboardController?.hide()
                                }
                            }),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 40.dp)
                                .testTag("chat_input_field"),
                            textStyle = TextStyle(fontSize = 13.sp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        FloatingActionButton(
                            onClick = {
                                if (chatInput.isNotBlank() && !viewModel.isChatLoading) {
                                    viewModel.sendMessageToAi(chatInput)
                                    chatInput = ""
                                    keyboardController?.hide()
                                }
                            },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            shape = CircleShape,
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("send_chat_button"),
                            elevation = FloatingActionButtonDefaults.elevation(0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Send,
                                contentDescription = "Envoyer",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderCard() {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Bienvenue sur Mirys ✨",
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Veuillez configurer votre clé d'API dans AI Studio Secrets.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Votre compagnon pour un esprit éclairé et des journées organisées.",
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
fun StatItem(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun DailyPremiumTracker(viewModel: AuraViewModel) {
    val isPremium = viewModel.subscriptionTier != "Gratuit"
    val theme = viewModel.trackerSelectedTheme

    // Dynamically retrieve visual gradients and accents based on sub-themes
    val gradientColors = when (theme) {
        "Mirys Cosmique 🌌" -> listOf(Color(0xFF2C1B4D), Color(0xFF13082A))
        "Forêt Zen 🌿" -> listOf(Color(0xFF122C1C), Color(0xFF041008))
        "Océan Serein 🌊" -> listOf(Color(0xFF0E2540), Color(0xFF030D1A))
        else -> listOf(Color(0xFF381515), Color(0xFF100303)) // Volcan Créatif
    }

    val glowColor = when (theme) {
        "Mirys Cosmique 🌌" -> Color(0xFFB088FF)
        "Forêt Zen 🌿" -> Color(0xFF50D08A)
        "Océan Serein 🌊" -> Color(0xFF3DA9FC)
        else -> Color(0xFFFF6B6B)
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.2.dp, glowColor.copy(alpha = 0.3f)),
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(gradientColors),
                shape = RoundedCornerShape(24.dp)
            )
            .testTag("daily_premium_tracker_card")
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Main content
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .blur(if (isPremium) 0.dp else 10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header of tracker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(imageVector = Icons.Outlined.CheckCircle, contentDescription = null, tint = glowColor, modifier = Modifier.size(18.dp))
                        Text(
                            text = "Suivi Journalier Premium 🌟",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .background(glowColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .border(1.dp, glowColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = "PREMIUM", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = glowColor)
                    }
                }

                // Theme choice pills
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Thème de l'Habit Tracker :", fontSize = 11.sp, color = Color.LightGray)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val themes = listOf("Mirys Cosmique 🌌", "Forêt Zen 🌿", "Océan Serein 🌊", "Volcan 🌋")
                        themes.forEach { t ->
                            val currentThemeStr = if (t == "Volcan 🌋") "Volcan Créatif 🌋" else t
                            val isSelected = theme == currentThemeStr
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) glowColor else Color.White.copy(alpha = 0.08f))
                                    .clickable { if (isPremium) viewModel.trackerSelectedTheme = currentThemeStr }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = t.split(" ")[0],
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isSelected) Color.Black else Color.White
                                )
                            }
                        }
                    }
                }

                Divider(color = Color.White.copy(alpha = 0.08f))

                // Trackers lists
                // 1. Water Cup Intake
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Hydratation 💧", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("${viewModel.trackerWater}/8 verres", fontSize = 10.sp, color = Color.LightGray)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (isPremium && viewModel.trackerWater > 0) { viewModel.trackerWater--; viewModel.triggerBeep(3) } },
                            modifier = Modifier.size(28.dp).background(Color.White.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Text("-", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        IconButton(
                            onClick = { if (isPremium && viewModel.trackerWater < 8) { viewModel.trackerWater++; viewModel.triggerBeep(3) } },
                            modifier = Modifier.size(28.dp).background(glowColor.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Text("+", color = glowColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // 2. Meditate State
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Méditation Active 🧘", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(if (viewModel.trackerMeditate) "Complétée (+10 XP)" else "S'accorder 5 minutes", fontSize = 10.sp, color = Color.LightGray)
                    }
                    Checkbox(
                        checked = viewModel.trackerMeditate,
                        onCheckedChange = {
                            if (isPremium) {
                                viewModel.trackerMeditate = it
                                if (it) {
                                    viewModel.xp += 10
                                    viewModel.triggerBeep(1)
                                } else {
                                    viewModel.xp = kotlin.math.max(0, viewModel.xp - 10)
                                    viewModel.triggerBeep(3)
                                }
                            }
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = glowColor,
                            checkmarkColor = Color.Black
                        )
                    )
                }

                // 3. Steps Target
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Marche Active / Pas 🚶", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("${viewModel.trackerSteps} / 10 000 pas", fontSize = 10.sp, color = Color.LightGray)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (isPremium && viewModel.trackerSteps >= 1000) { viewModel.trackerSteps -= 1000; viewModel.triggerBeep(3) } },
                            modifier = Modifier.size(28.dp).background(Color.White.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Text("-1k", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        IconButton(
                            onClick = { if (isPremium && viewModel.trackerSteps < 20000) { viewModel.trackerSteps += 1000; viewModel.triggerBeep(3) } },
                            modifier = Modifier.size(28.dp).background(glowColor.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Text("+1k", color = glowColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // 4. Sleep
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Heures de Sommeil 😴", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("${viewModel.trackerSleepHours}h / 8h recommandées", fontSize = 10.sp, color = Color.LightGray)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (isPremium && viewModel.trackerSleepHours > 0) { viewModel.trackerSleepHours--; viewModel.triggerBeep(3) } },
                            modifier = Modifier.size(28.dp).background(Color.White.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Text("-", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        IconButton(
                            onClick = { if (isPremium && viewModel.trackerSleepHours < 24) { viewModel.trackerSleepHours++; viewModel.triggerBeep(3) } },
                            modifier = Modifier.size(28.dp).background(glowColor.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Text("+", color = glowColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Lock Overlay (shown if free user)
            if (!isPremium) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.72f), RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = "🔒",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "Suivi Journalier Premium 🔒",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Débloquez l'habit tracking dynamique avec thèmes exclusifs (Cosmique, Forêt, Océan) et rapports statistiques.",
                            fontSize = 10.5.sp,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center,
                            lineHeight = 15.sp
                        )
                        Button(
                            onClick = {
                                // Simulate switching to Premium instantly to test and enjoy the app!
                                viewModel.subscriptionTier = "Premium Ultimate 👑"
                                viewModel.coins += 200
                                viewModel.triggerBeep(1)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700), contentColor = Color.Black),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Débloquer Gratuitement (Démo) 🔑", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SoulResonanceReactor(viewModel: AuraViewModel) {
    val coroutineScope = rememberCoroutineScope()
    var isFusing by remember { mutableStateOf(false) }

    // Floating animation for deep mental immersion
    val infiniteTransition = rememberInfiniteTransition()
    val animatedPulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val animatedPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("soul_resonance_reactor_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.AllInclusive,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Synchroniseur Vibratoire Mirys 🌀",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Technologie de résonance cognitive et d'engagement quotidien.",
                        fontSize = 10.sp,
                        color = Color.LightGray
                    )
                }
                
                // Active indicators count
                Box(
                    modifier = Modifier
                        .background(Color(0xFFE040FB).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(text = "${viewModel.activeResonanceSparkles} ✨", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE040FB))
                }
            }

            Text(
                text = "Sélectionnez une fréquence de concentration organique et synchronisez votre énergie quotidienne. Compléter la résonance chaque jour vous apporte des graines, des reliques rares et renforce l'assiduité !",
                fontSize = 11.sp,
                color = Color.LightGray,
                lineHeight = 15.sp
            )

            // Frequency Pill Selector
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Fréquence Vibratoire active :", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val frequencies = listOf("432 Hz - Calme", "528 Hz - Succès", "963 Hz - Focus")
                    frequencies.forEach { freq ->
                        val isChosen = viewModel.resonanceFrequencyName.contains(freq.substring(0, 3))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isChosen) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f))
                                .border(1.dp, if (isChosen) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .clickable {
                                    if (!isFusing && !viewModel.resonanceDailyCompleted) {
                                        viewModel.resonanceFrequencyName = freq
                                        viewModel.triggerBeep(3)
                                    }
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(freq, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isChosen) Color.White else Color.Gray)
                        }
                    }
                }
            }

            // central reactor canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Background waves when charging
                Canvas(modifier = Modifier.size(110.dp)) {
                    val sizePx = size.width
                    // concentric expanding circles representing vibration energy waves
                    if (isFusing) {
                        drawCircle(
                            color = Color(0xFF00E5FF).copy(alpha = animatedPulseAlpha),
                            radius = (sizePx / 2f) * animatedPulseScale,
                            style = Stroke(width = 3f)
                        )
                        drawCircle(
                            color = Color(0xFFE040FB).copy(alpha = animatedPulseAlpha * 0.7f),
                            radius = (sizePx / 2f) * (animatedPulseScale * 0.7f),
                            style = Stroke(width = 2f)
                        )
                    } else if (viewModel.resonanceDailyCompleted) {
                        // complete state golden halo
                        drawCircle(
                            color = Color(0xFFFFD700).copy(alpha = 0.15f),
                            radius = sizePx / 2f
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (viewModel.resonanceDailyCompleted) {
                        Icon(imageVector = Icons.Outlined.BrightnessHigh, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(36.dp))
                        Text("Résonance Synchronisée ! 🎉", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFFFFD700))
                        Text(text = "Rendez-vous demain pour recharger", fontSize = 10.sp, color = Color.Gray)
                    } else if (isFusing) {
                        CircularProgressIndicator(
                            progress = viewModel.resonanceCoreCharge / 100f,
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 4.dp,
                            modifier = Modifier.size(36.dp)
                        )
                        Text("Chargement : ${viewModel.resonanceCoreCharge}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Gardez l'esprit centré...", fontSize = 9.sp, color = Color.LightGray)
                    } else {
                        IconButton(
                            onClick = {
                                if (!isFusing && !viewModel.resonanceDailyCompleted) {
                                    isFusing = true
                                    coroutineScope.launch {
                                        for (progress in 0..100 step 10) {
                                            delay(400)
                                            viewModel.chargeResonanceUnitStep(10)
                                        }
                                        isFusing = false
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        ) {
                            Icon(imageVector = Icons.Outlined.Fingerprint, contentDescription = null, tint = Color.Black, modifier = Modifier.size(24.dp))
                        }
                        Text("Toucher pour Synchroniser", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Nécessite 4 secondes de concentration", fontSize = 9.sp, color = Color.LightGray)
                    }
                }
            }

            // ALCHEMICAL RELICS FORGE - HOOK THAT MAKES USERS ADDICTED to collect stars and forge
            Divider(color = Color.White.copy(alpha = 0.05f))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(imageVector = Icons.Outlined.Eco, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(15.dp))
                        Text("Graines de Vie Alchimiques :", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Text("${viewModel.resonanceEnergySeeds} / 3 🍀", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Dépensez 3 graines pour forger une étoile légendaire dans votre ciel spirituel (+100 Or, +50 XP) !",
                        fontSize = 10.sp,
                        color = Color.LightGray,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = { viewModel.forgeAstralRelic() },
                        enabled = viewModel.resonanceEnergySeeds >= 3,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE040FB)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("Forger ⭐", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Star sky container representation
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(12.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Votre Constellation :", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        (1..6).forEach { starIdx ->
                            val isForged = viewModel.activeConstellationStars.contains(starIdx)
                            Icon(
                                imageVector = if (isForged) Icons.Outlined.Star else Icons.Outlined.StarBorder,
                                contentDescription = null,
                                tint = if (isForged) Color(0xFFFFD700) else Color.DarkGray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
