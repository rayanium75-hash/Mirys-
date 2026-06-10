package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.AuraViewModel

@Composable
fun AgendaScreen(
    viewModel: AuraViewModel,
    modifier: Modifier = Modifier
) {
    var subTab by remember { mutableStateOf("journal") } // "journal", "tasks"

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("agenda_screen")
    ) {
        // Upper Subtab row selector
        TabRow(
            selectedTabIndex = if (subTab == "journal") 0 else 1,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = subTab == "journal",
                onClick = { subTab = "journal" },
                modifier = Modifier
                    .height(48.dp)
                    .testTag("agenda_tab_journal")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Book,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Journal Intime",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Tab(
                selected = subTab == "tasks",
                onClick = { subTab = "tasks" },
                modifier = Modifier
                    .height(48.dp)
                    .testTag("agenda_tab_tasks")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ListAlt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Tâches & Objectifs",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            AnimatedContent(
                targetState = subTab,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "agenda_transitions"
            ) { targetTab ->
                when (targetTab) {
                    "journal" -> JournalScreen(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                    "tasks" -> TaskScreen(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
