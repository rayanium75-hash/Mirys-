package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JournalEntry
import com.example.ui.viewmodel.AuraViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun JournalScreen(
    viewModel: AuraViewModel,
    modifier: Modifier = Modifier
) {
    val journalEntries by viewModel.journalEntries.collectAsState()
    
    var isFormVisible by remember { mutableStateOf(false) }
    var entryTitle by remember { mutableStateOf("") }
    var entryContent by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("😊") }
    var moodScore by remember { mutableFloatStateOf(4f) }

    val emojis = listOf(
        "💖" to "Heureux",
        "😊" to "Serein",
        "😐" to "Neutre",
        "😔" to "Anxieux",
        "😢" to "Triste"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Toggle Form Expand Header Button
        item {
            Button(
                onClick = { isFormVisible = !isFormVisible },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("toggle_journal_form_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFormVisible) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                    contentColor = if (isFormVisible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = if (isFormVisible) Icons.Outlined.ExpandLess else Icons.Outlined.Add,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isFormVisible) "Fermer le formulaire" else "Nouvelle note de journal",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }

        // Form Item
        item {
            AnimatedVisibility(visible = isFormVisible) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Racontez votre journée",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )

                        OutlinedTextField(
                            value = entryTitle,
                            onValueChange = { entryTitle = it },
                            label = { Text("Titre de la note") },
                            placeholder = { Text("Ex: Une belle promenade") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("journal_title_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        OutlinedTextField(
                            value = entryContent,
                            onValueChange = { entryContent = it },
                            label = { Text("Que s'est-il passé ? Vos ressentis...") },
                            placeholder = { Text("Écrivez librement ici...") },
                            minLines = 3,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("journal_content_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        // Mood selector
                        val currentLabel = emojis.find { it.first == selectedEmoji }?.second ?: "Serein"
                        Text(
                            text = "Comment vous sentez-vous ? : $currentLabel",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            emojis.forEach { (emoji, label) ->
                                val isSelected = selectedEmoji == emoji
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primaryContainer 
                                            else Color.Transparent
                                        )
                                        .clickable { 
                                            selectedEmoji = emoji 
                                            // Assign scores depending on emoji
                                            moodScore = when(emoji) {
                                                "💖" -> 5f
                                                "😊" -> 4f
                                                "😐" -> 3f
                                                "😔" -> 2f
                                                "😢" -> 1f
                                                else -> 3f
                                            }
                                        }
                                        .testTag("emoji_btn_$emoji")
                                        .padding(4.dp)
                                ) {
                                    Icon(
                                        imageVector = when (emoji) {
                                            "💖" -> Icons.Outlined.SentimentVerySatisfied
                                            "😊" -> Icons.Outlined.SentimentSatisfied
                                            "😐" -> Icons.Outlined.SentimentNeutral
                                            "😔" -> Icons.Outlined.SentimentDissatisfied
                                            else -> Icons.Outlined.SentimentVeryDissatisfied
                                        },
                                        contentDescription = label,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }

                        // Score Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Intensité Émotionnelle : ${moodScore.toInt()}/5",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Slider(
                            value = moodScore,
                            onValueChange = { moodScore = it },
                            valueRange = 1f..5f,
                            steps = 3,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("mood_score_slider")
                        )

                        Button(
                            onClick = {
                                if (entryTitle.isNotBlank() && entryContent.isNotBlank()) {
                                    viewModel.addJournalEntry(
                                        title = entryTitle,
                                        content = entryContent,
                                        moodEmoji = selectedEmoji,
                                        moodScore = moodScore.toInt()
                                    )
                                    // Reset fields
                                    entryTitle = ""
                                    entryContent = ""
                                    selectedEmoji = "😊"
                                    moodScore = 4f
                                    isFormVisible = false
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("save_journal_entry_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Sauvegarder", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Section header
        item {
            Text(
                text = "Historique de vos pensées",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // List elements
        if (journalEntries.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Book,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                        modifier = Modifier.size(60.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Votre journal est encore vierge.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Écrivez une note pour l'analyse IA !",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        } else {
            items(journalEntries, key = { it.id }) { entry ->
                JournalCard(
                    entry = entry,
                    onDelete = { viewModel.deleteJournalEntry(entry.id) }
                )
            }
        }
    }
}

@Composable
fun JournalCard(
    entry: JournalEntry,
    onDelete: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.FRENCH) }
    val formattedDate = remember(entry.timestamp) { dateFormat.format(Date(entry.timestamp)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .testTag("journal_card_${entry.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .clickable { isExpanded = !isExpanded }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circle with Mood Emoji
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            MaterialTheme.colorScheme.surface,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = when (entry.moodEmoji) {
                            "💖" -> Icons.Outlined.SentimentVerySatisfied
                            "😊" -> Icons.Outlined.SentimentSatisfied
                            "😐" -> Icons.Outlined.SentimentNeutral
                            "😔" -> Icons.Outlined.SentimentDissatisfied
                            else -> Icons.Outlined.SentimentVeryDissatisfied
                        },
                        contentDescription = "Humeur",
                        tint = when (entry.moodEmoji) {
                            "💖" -> Color(0xFFFF5722)
                            "😊" -> Color(0xFF4CAF50)
                            "😐" -> Color(0xFFFFC107)
                            "😔" -> Color(0xFF9C27B0)
                            else -> Color(0xFF2196F3)
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formattedDate,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_journal_${entry.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Effacer l'entrée",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = entry.content,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis
            )

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Score : ${entry.moodScore}/5",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Text(
                        text = when (entry.moodScore) {
                            5 -> "Exceptionnel"
                            4 -> "Calme et Serein"
                            3 -> "Équilibré"
                            2 -> "Anxiété Légère"
                            else -> "Tristesse et Fatigue"
                        },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
