package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Task
import com.example.ui.viewmodel.AuraViewModel

@Composable
fun TaskScreen(
    viewModel: AuraViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.tasks.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    var aiGoalInput by remember { mutableStateOf("") }
    var selectedDurationIndex by remember { mutableStateOf(0) }
    val durationOptions = listOf(
        "Auto (10 à 30 jours)" to null,
        "7 jours" to 7,
        "10 jours" to 10,
        "15 jours" to 15,
        "30 jours" to 30
    )
    
    var manualTaskTitle by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf("Moyenne") }
    var selectedCategory by remember { mutableStateOf("Général") }

    val priorities = listOf("Basse", "Moyenne", "Haute")
    val categories = listOf("Général", "Bien-être", "Études", "Personnel")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. IA Planner Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                ),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Générateur d'Objectifs Mirys IA",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        text = "Écrivez votre objectif (ex: 'Perdre du poids' ou 'Apprendre le piano') et Mirys planifiera un calendrier d'étapes concret adapté à la durée choisie ci-dessous !",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )

                    // BEAUTIFUL DURATION SELECTOR
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(imageVector = Icons.Outlined.HourglassEmpty, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(13.dp))
                            Text(
                                text = "Durée de Planification :",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        // Row of horizontal chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            durationOptions.forEachIndexed { idx, (label, daysVal) ->
                                val isSelected = selectedDurationIndex == idx
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        )
                                        .clickable { selectedDurationIndex = idx }
                                        .border(
                                            1.dp,
                                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                            RoundedCornerShape(10.dp)
                                        )
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label.replace(" jours", "j").replace(" jours", "j"),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = aiGoalInput,
                            onValueChange = { aiGoalInput = it },
                            placeholder = { Text("Votre objectif...", fontSize = 13.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                if (aiGoalInput.isNotBlank() && !viewModel.isGeneratingTasks) {
                                    val durationDays = durationOptions[selectedDurationIndex].second
                                    viewModel.generateTasksWithAi(aiGoalInput, durationDays)
                                    aiGoalInput = ""
                                    keyboardController?.hide()
                                }
                            }),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("ai_goal_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            ),
                            enabled = !viewModel.isGeneratingTasks
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        FloatingActionButton(
                            onClick = {
                                if (aiGoalInput.isNotBlank() && !viewModel.isGeneratingTasks) {
                                    val durationDays = durationOptions[selectedDurationIndex].second
                                    viewModel.generateTasksWithAi(aiGoalInput, durationDays)
                                    aiGoalInput = ""
                                    keyboardController?.hide()
                                }
                            },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("ai_generate_tasks_button"),
                            elevation = FloatingActionButtonDefaults.elevation(0.dp)
                        ) {
                            if (viewModel.isGeneratingTasks) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.Bolt,
                                    contentDescription = "Générer les tâches"
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Manual Task Creator Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Ajouter manuellement une tâche",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = manualTaskTitle,
                        onValueChange = { manualTaskTitle = it },
                        label = { Text("Nom de la tâche") },
                        placeholder = { Text("Ex: Boire 1.5L d'eau") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("manual_task_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    // Priority Chips
                    Text(
                        text = "Priorité",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        priorities.forEach { priority ->
                            val isSelected = selectedPriority == priority
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedPriority = priority },
                                label = { Text(priority, fontSize = 12.sp) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("priority_chip_$priority"),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = when(priority) {
                                        "Haute" -> MaterialTheme.colorScheme.errorContainer
                                        "Basse" -> MaterialTheme.colorScheme.tertiaryContainer
                                        else -> MaterialTheme.colorScheme.secondaryContainer
                                    }
                                )
                            )
                        }
                    }

                    // Category Chips
                    Text(
                        text = "Catégorie",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { category ->
                            val isSelected = selectedCategory == category
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = category },
                                label = { Text(category, fontSize = 12.sp) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("category_chip_$category")
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (manualTaskTitle.isNotBlank()) {
                                viewModel.addTask(
                                    title = manualTaskTitle,
                                    priority = selectedPriority,
                                    category = selectedCategory
                                )
                                manualTaskTitle = ""
                                keyboardController?.hide()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("add_manual_task_button")
                    ) {
                        Text("Ajouter la tâche", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 3. Section Header Tasks with clean pruning action
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mes Tâches",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (tasks.any { it.isCompleted }) {
                    TextButton(
                        onClick = { viewModel.deleteCompletedTasks() },
                        modifier = Modifier.testTag("prune_completed_tasks")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CleaningServices,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Nettoyer", fontSize = 13.sp)
                    }
                }
            }
        }

        // List tasks
        if (tasks.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                        modifier = Modifier.size(60.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Aucune tâche de planifiée.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Générez un plan d'action avec l'IA !",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        } else {
            items(tasks, key = { it.id }) { task ->
                TaskCard(
                    task = task,
                    onToggleComplete = { viewModel.toggleTask(task.id, !task.isCompleted) },
                    onDelete = { viewModel.deleteTask(task.id) },
                    onUpdateColor = { hexVal -> viewModel.updateTaskCustomColor(task.id, hexVal) }
                )
            }
        }
    }
}

@Composable
fun TaskCard(
    task: Task,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit,
    onUpdateColor: (String?) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    val parsedColor = remember(task.customBgColorHex) {
        if (task.customBgColorHex != null) {
            try {
                Color(android.graphics.Color.parseColor(task.customBgColorHex))
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    val targetBgColor = when {
        parsedColor != null -> {
            if (task.isCompleted) parsedColor.copy(alpha = 0.5f) else parsedColor
        }
        task.isCompleted -> {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
        }
        else -> {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        }
    }

    val backgroundColor by animateColorAsState(
        targetValue = targetBgColor,
        label = "task_bg_anim"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("task_card_${task.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = { onToggleComplete() },
                    modifier = Modifier.testTag("task_check_${task.id}")
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (task.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Priority Indicator Badge
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = when(task.priority) {
                                        "Haute" -> MaterialTheme.colorScheme.error
                                        "Basse" -> MaterialTheme.colorScheme.tertiary
                                        else -> MaterialTheme.colorScheme.secondary
                                    },
                                    shape = CircleShape
                                )
                        )

                        Text(
                            text = task.priority,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )

                        Text(
                            text = "•",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )

                        // Category text
                        Text(
                            text = task.category,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Icon(
                            imageVector = if (isExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                            contentDescription = if (isExpanded) "Masquer les détails" else "Voir les détails",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_task_${task.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Effacer la tâche",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, end = 14.dp, bottom = 14.dp)
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f),
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    Text(
                        text = "Aperçu des détails :",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    val detailsText = task.description.ifBlank {
                        "Cette étape planifiée par Mirys IA consiste à progresser sereinement vers votre objectif. Suivez les consignes régulières, adaptez votre environnement et préservez votre bien-être au quotidien."
                    }

                    Text(
                        text = detailsText,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                        lineHeight = 17.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f),
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    // Style Picker
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Personnaliser le fond (Gratuit) :",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val colorOptions = listOf(
                        "Défaut" to null,
                        "Menthe" to "#1A3324",
                        "Océan" to "#0F2027",
                        "Cosmique" to "#2C1B4D",
                        "Volcan" to "#381515",
                        "Violet" to "#3F2B96",
                        "Ardoise" to "#1E293B",
                        "Rose" to "#471D27"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        colorOptions.forEach { (colorName, hexVal) ->
                            val isColorSelected = task.customBgColorHex == hexVal
                            val circleColor = if (hexVal == null) {
                                MaterialTheme.colorScheme.surfaceVariant
                            } else {
                                Color(android.graphics.Color.parseColor(hexVal))
                            }

                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(circleColor)
                                    .border(
                                        width = if (isColorSelected) 3.dp else 1.dp,
                                        color = if (isColorSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.4f),
                                        shape = CircleShape
                                    )
                                    .clickable { onUpdateColor(hexVal) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isColorSelected) {
                                    Icon(
                                        imageVector = Icons.Outlined.Check,
                                        contentDescription = "Sélectionné",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(14.dp)
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
