package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import com.example.ui.viewmodel.AuraViewModel
import com.example.ui.viewmodel.ChessPiece
import com.example.ui.viewmodel.QuizQuestion
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GamesScreen(
    viewModel: AuraViewModel,
    modifier: Modifier = Modifier
) {
    var subTab by remember { mutableStateOf("hub") } // "hub", "quiz", "chess", "memory", "sudoku", "2048", "wordle", "lightsout", "simon"
    
    // Ambient soundscapes state
    var isAmbientPlaying by remember { mutableStateOf(false) }
    var selectedSoundscape by remember { mutableStateOf("rain") } // "rain", "waves", "tibetan"
    var ambientVolume by remember { mutableFloatStateOf(0.4f) }
    
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_bars")
    
    // Light-weight frequency visualizer for background sound play states
    val barHeight1 by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 24f,
        animationSpec = infiniteRepeatable(
            animation = tween(450),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar1"
    )
    val barHeight2 by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = tween(300),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar2"
    )
    val barHeight3 by infiniteTransition.animateFloat(
        initialValue = 3f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar3"
    )
    val barHeight4 by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(380),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar4"
    )
    val barHeight5 by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 25f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar5"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("games_screen")
    ) {
        // --- GAME HEADER ROW (Back bar when inside a game) ---
        if (subTab != "hub") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
                    .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clickable { subTab = "hub" }
                    .testTag("back_to_hub_tag"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Retour",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Hub de Jeux",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Box(
                    modifier = Modifier
                        .height(16.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                Text(
                    text = when (subTab) {
                        "quiz" -> "Quiz Trivia"
                        "chess" -> "Échecs IA"
                        "memory" -> "Jeu de Mémoire"
                        "sudoku" -> "Sudoku Zen"
                        "2048" -> "2048 Math"
                        "wordle" -> "Mots Doux"
                        "lightsout" -> "Lumières Out"
                        "simon" -> "Simon Sens"
                        else -> ""
                    },
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = "Monnaie",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${viewModel.coins}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
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
                label = "subtab_transitions"
            ) { targetTab ->
                when (targetTab) {
                    "hub" -> {
                        // --- HUB DE JEUX COGNITIFS ---
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "🧠 Hub Cognitif Mirys",
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Entraînez votre esprit dans le calme. Chaque victoire vous rapporte des jetons à dépenser dans la boutique de badges célestes.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // --- AMBIENT SOUNDSCAPE SECTION ---
                            Card(
                                shape = RoundedCornerShape(24.dp),
                                border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outlineVariant),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.12f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "🎵 Ambiance Zen Relaxante",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            if (isAmbientPlaying) {
                                                Row(
                                                    verticalAlignment = Alignment.Bottom,
                                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                    modifier = Modifier.height(18.dp).padding(horizontal = 4.dp)
                                                ) {
                                                    val bars: List<Float> = listOf(barHeight1, barHeight2, barHeight3, barHeight4, barHeight5)
                                                    bars.forEach { valHeight ->
                                                        Box(
                                                            modifier = Modifier
                                                                .width(3.dp)
                                                                .height(Dp(valHeight))
                                                                .clip(RoundedCornerShape(1.dp))
                                                                .background(MaterialTheme.colorScheme.primary)
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        IconButton(
                                            onClick = { isAmbientPlaying = !isAmbientPlaying },
                                            colors = IconButtonDefaults.iconButtonColors(
                                                containerColor = if (isAmbientPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                                contentColor = if (isAmbientPlaying) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                            ),
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isAmbientPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                contentDescription = "Lecture Ambiance",
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val sounds = listOf(
                                            Triple("rain", "☔ Pluie", "Calme d'automne"),
                                            Triple("waves", "🌊 Vagues", "Océan relaxant"),
                                            Triple("tibetan", "🌲 Forêt", "Vent et oiseaux")
                                        )
                                        sounds.forEach { (id, nameStr, descStr) ->
                                            val isChosen = selectedSoundscape == id
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(
                                                        if (isChosen) MaterialTheme.colorScheme.primaryContainer
                                                        else MaterialTheme.colorScheme.surface
                                                    )
                                                    .border(
                                                        width = 1.dp,
                                                        color = if (isChosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                                        shape = RoundedCornerShape(12.dp)
                                                    )
                                                    .clickable { 
                                                        selectedSoundscape = id
                                                        isAmbientPlaying = true
                                                    }
                                                    .padding(vertical = 10.dp, horizontal = 6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text(text = nameStr, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (isChosen) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
                                                    Text(text = descStr, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Text(
                                text = "🏆 Sélection des Défis",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            val games = listOf(
                                GameConfig("quiz", "Quiz Trivia", "QCM & Culture", "Philosophie zen, organisation logique et bien-être. Établissez votre score de sagesse !", "🧠 Sagesse", "Tous Niveaux", Color(0xFF673AB7)),
                                GameConfig("chess", "Échecs IA", "Tactique pure", "Défiez notre robot programmateur d'échecs sur un échiquier élégant.", "♟️ Tactique", "Avancé", Color(0xFFFF5722)),
                                GameConfig("memory", "Mémoire Visuelle", "Paires de Cartes", "Faites correspondre les cartes identiques avec un chronomètre fluide.", "👁️ Attention", "Facile", Color(0xFF4CAF50)),
                                GameConfig("sudoku", "Sudoku Zen", "Grille de Chiffres", "Le puzzle logique par excellence. Comprend un système d'aide et de détection d'erreurs.", "🔢 Logique", "Moyen", Color(0xFF00BCD4)),
                                GameConfig("2048", "2048 Math", "Calcul & Fusion", "Glissez les carrés et combinez les multiples pour récolter l'ultime tuile dorée 2048.", "➕ Réflexe", "Moyen", Color(0xFF9C27B0)),
                                GameConfig("wordle", "Mots Doux", "Orthographe", "Devinez le mot bienveillant secret de 5 lettres en un maximum de 6 tentatives.", "🔤 Vocabulaire", "Amusant", Color(0xFFE91E63)),
                                GameConfig("lightsout", "Lumières Out", "Cassecou", "Éteignez toutes les cases. Chaque clic commute un motif cruciforme sur l'échiquier !", "💡 Parité", "Excellent", Color(0xFF3F51B5)),
                                GameConfig("simon", "Simon Says", "Concentration", "Mémorisez et répétez la mélodie visuelle et rythmique de Simon. Attention accrue !", "🔔 Écoute", "Moyen", Color(0xFFFF9800))
                            )

                            games.forEach { game ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { subTab = game.id }
                                        .testTag("game_card_${game.id}"),
                                    shape = RoundedCornerShape(20.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(52.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(game.color.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = when (game.id) {
                                                    "quiz" -> "❓"
                                                    "chess" -> "♟️"
                                                    "memory" -> "🃏"
                                                    "sudoku" -> "🧩"
                                                    "2048" -> "🔢"
                                                    "wordle" -> "✍️"
                                                    "lightsout" -> "💡"
                                                    else -> "🔔"
                                                },
                                                fontSize = 24.sp
                                            )
                                        }

                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = game.title,
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 15.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                SuggestionChip(
                                                    onClick = {},
                                                    label = { Text(game.category, fontSize = 9.sp, fontWeight = FontWeight.Bold) },
                                                    modifier = Modifier.height(18.dp)
                                                )
                                            }
                                            Text(
                                                text = game.description,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                lineHeight = 15.sp
                                            )
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Difficulté: ${game.difficulty}",
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(text = "•", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                                Text(
                                                    text = game.tag,
                                                    fontSize = 10.sp,
                                                    color = game.color,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Icon(
                                            imageVector = Icons.Default.ArrowForwardIos,
                                            contentDescription = "Lancer",
                                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    "quiz" -> QuizSection(viewModel = viewModel)
                    "chess" -> ChessSection(viewModel = viewModel)
                    "memory" -> MemorySection(viewModel = viewModel)
                    "sudoku" -> SudokuSection(viewModel = viewModel)
                    "2048" -> TwoZeroFourEightSection(viewModel = viewModel)
                    "wordle" -> WordleSection(viewModel = viewModel)
                    "lightsout" -> LightsOutSection(viewModel = viewModel)
                    "simon" -> SimonSaysSection(viewModel = viewModel)
                }
            }
        }
    }
}

// Data grid helper holding games configuration parameters
data class GameConfig(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val tag: String,
    val difficulty: String,
    val color: Color
)

// ================== QUIZ TRIVIA DESIGN ==================
@Composable
fun QuizSection(viewModel: AuraViewModel) {
    val context = LocalContext.current
    val activeCat = viewModel.quizActiveCat
    val completed = viewModel.quizCompleted

    if (activeCat != null) {
        // Active Quiz Loop Screen !
        ActiveQuizScreen(viewModel = viewModel, category = activeCat)
    } else if (completed) {
        // Completed Score Card Screen !
        QuizCompletionScreen(viewModel = viewModel)
    } else {
        // Dashboard selecting category screen
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Entraînez votre esprit !",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "Participez à nos quiz thématiques quotidiens pour remporter des pièces et doper vos XP. Resserrez votre concentration !",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            // Category Selection Cards
            val categories = listOf(
                Pair("Sciences", "🔬"),
                Pair("Histoire", "📚"),
                Pair("Echecs", "♟️"),
                Pair("Culture", "🎬")
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(categories.size) { idx ->
                    val (catId, emoji) = categories[idx]
                    Card(
                        onClick = { viewModel.startQuizSession(catId) },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                            .testTag("quiz_cat_$catId")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (catId) {
                                        "Sciences" -> Icons.Default.Science
                                        "Histoire" -> Icons.Default.MenuBook
                                        "Echecs" -> Icons.Default.Extension
                                        else -> Icons.Default.Movie
                                    },
                                    contentDescription = catId,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (catId == "Echecs") "Échecs & Tactique" else catId,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "5 Questions • Multiplicateur x${idx+2}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Quick Streak info banner
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Bonus : Une série de réponses correctes consécutives multiplie vos pièces récoltées à la fin du quiz !",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveQuizScreen(viewModel: AuraViewModel, category: String) {
    val questions = viewModel.quizQuestions[category] ?: return
    val currentIndex = viewModel.quizCurrentIndex
    val currentQuestion = questions[currentIndex]
    val selectedIdx = viewModel.selectedOptionIdx
    val score = viewModel.quizCurrentScore

    // Active Timer countdown representation (15 seconds per question)
    var secondsLeft by remember { mutableStateOf(15) }
    var keyToResetTimer by remember { mutableStateOf(0) }

    LaunchedEffect(currentIndex, keyToResetTimer) {
        secondsLeft = 15
        while (secondsLeft > 0 && viewModel.selectedOptionIdx == null) {
            delay(1000)
            secondsLeft -= 1
        }
        if (secondsLeft == 0 && viewModel.selectedOptionIdx == null) {
            // Auto submit incorrect to move forward
            viewModel.submitQuizAnswer(-1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Question Header Metadata
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Quiz : ${if (category == "Echecs") "Échecs" else category}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Question ${currentIndex + 1} sur ${questions.size}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Beautiful timer circle countdown
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        if (secondsLeft <= 5) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$secondsLeft",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (secondsLeft <= 5) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        // Inline Linear Progress Indicator
        LinearProgressIndicator(
            progress = (currentIndex + 1).toFloat() / questions.size.toFloat(),
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Question Statement Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currentQuestion.qText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Answer Option Buttons list
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            currentQuestion.options.forEachIndexed { idx, optionText ->
                val isSelected = selectedIdx == idx
                val isCorrectAnswer = idx == currentQuestion.correctIdx
                val isWrongSelection = isSelected && !isCorrectAnswer

                // Determine styling based on verification states
                val (buttonColor, textColor, borderColor) = when {
                    selectedIdx != null && isCorrectAnswer -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), Color(0xFF4CAF50))
                    selectedIdx != null && isWrongSelection -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), Color(0xFFE57373))
                    isSelected -> Triple(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer, MaterialTheme.colorScheme.primary)
                    else -> Triple(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onSurface, MaterialTheme.colorScheme.outlineVariant)
                }

                Card(
                    onClick = { viewModel.submitQuizAnswer(idx) },
                    colors = CardDefaults.cardColors(containerColor = buttonColor),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                        .testTag("quiz_opt_$idx")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = optionText,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = textColor
                        )

                        // Action feedback icons
                        if (selectedIdx != null) {
                            if (isCorrectAnswer) {
                                Icon(Icons.Default.CheckCircle, "Correct", tint = Color(0xFF2E7D32))
                            } else if (isWrongSelection) {
                                Icon(Icons.Default.Cancel, "Incorrect", tint = Color(0xFFC62828))
                            }
                        }
                    }
                }
            }
        }

        // Explanation text description overlay
        if (selectedIdx != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Explication :",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = currentQuestion.explanation,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.proceedToNextQuizOrFinish() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_quiz_next")
            ) {
                Text(
                    text = if (currentIndex == questions.size - 1) "Terminer le Quiz" else "Question Suivante",
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {},
                enabled = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Choisissez une réponse pour continuer")
            }
        }
    }
}

@Composable
fun QuizCompletionScreen(viewModel: AuraViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .testTag("quiz_completion_screen"),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(42.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Quiz Terminé !",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 22.sp,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Votre performance : ${viewModel.quizCurrentScore} réponses correctes sur 5",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Rewards Earned Summary Card !
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Récompenses remportées :",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Coins Awarded
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Paid,
                            contentDescription = "Pièces",
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "+ ${viewModel.quizEarnedCoins}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // XP Awarded
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "XP",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "+ ${viewModel.quizEarnedXp} XP",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                if (viewModel.quizStreakMultiplier > 1) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text("Multiplicateur Série : x${viewModel.quizStreakMultiplier} activé", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = { viewModel.resetQuizToHome() },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_close_quiz")
        ) {
            Text("Retour aux Quiz", fontWeight = FontWeight.Bold)
        }
    }
}

// ================== CHESS VS IA DESIGN ==================
@Composable
fun ChessSection(viewModel: AuraViewModel) {
    val isChessActive = viewModel.isChessActive
    val resultMsg = viewModel.chessResultMessage

    if (isChessActive) {
        ActiveChessGameScreen(viewModel = viewModel)
    } else {
        // Landing Screen choose difficulty
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Échecs Tactiques vs IA",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Votre Classement Chess : ${viewModel.eloChess} ELO",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Text(
                text = "Entraînez-vous contre l'intelligence artificielle de Mirys. Réfléchissez avec perspicacité tactique et capturez le Roi adverse pour gagner ELO et Pièces !",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            if (resultMsg != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = resultMsg,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Difficulty selections buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Choisissez un niveau d'intelligence artificielle :",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val difficulties = listOf(
                    Triple("Débutant", "Niveau 1 de l'IA (Trames simples)", Color(0xFF4CAF50)),
                    Triple("Intermédiaire", "Niveau 2 (Recherche gloutonne)", Color(0xFFFF9800)),
                    Triple("Aiguisé Expert", "Niveau Supérieur (Évaluations multiples)", Color(0xFFF44336))
                )

                difficulties.forEach { (name, desc, color) ->
                    Card(
                        onClick = { viewModel.startChessGame(name) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                            .testTag("btn_chess_start_$name")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(color, CircleShape)
                                    )
                                    Text(name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                Text(desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveChessGameScreen(viewModel: AuraViewModel) {
    val board = viewModel.chessBoardState
    val captureByAi = viewModel.chessCapturedByAi
    val captureByPlayer = viewModel.chessCapturedByPlayer
    val activeTurn = viewModel.chessPlayerTurn
    val moveHistory = viewModel.moveHistoryList
    val whiteTimeLeft = viewModel.whiteChessTime
    val blackTimeLeft = viewModel.blackChessTime
    val selectedCell = viewModel.selectedChessCell

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Clocks and turn metadata row !
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // White Clock
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (activeTurn) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.width(110.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Joueur (Blanc)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = formatTimeClock(whiteTimeLeft),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = if (activeTurn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Turn notification tag
            SuggestionChip(
                onClick = {},
                label = {
                    Text(
                        text = if (activeTurn) "Votre Tour (Blancs)" else "IA Réfléchit (Noirs)...",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = if (activeTurn) Color(0xFFE8F5E9) else Color(0xFFECEFF1),
                    labelColor = if (activeTurn) Color(0xFF2E7D32) else Color(0xFF455A64)
                )
            )

            // Black Clock
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (!activeTurn) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.width(110.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("IA (Noirs)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = formatTimeClock(blackTimeLeft),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = if (!activeTurn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Row of captured White pieces (Captured by AI)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("Pièces perdues : ", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (captureByAi.isEmpty()) {
                Text("Aucune", fontSize = 10.sp, color = Color.Gray)
            } else {
                captureByAi.forEach { piece ->
                    Text(getPieceGlyphUnicode(piece), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 8x8 Chessboard Render Canvas !
        Column(
            modifier = Modifier
                .aspectRatio(1f)
                .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                .clip(RoundedCornerShape(4.dp))
        ) {
            for (r in 0..7) {
                Row(modifier = Modifier.weight(1f)) {
                    for (c in 0..7) {
                        val isDarkSquare = (r + c) % 2 == 1
                        val squareColor = if (isDarkSquare) Color(0xFFB88B4A) else Color(0xFFE3D6B5)
                        val isSelected = selectedCell == Pair(r, c)
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(squareColor)
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                                )
                                .clickable(enabled = activeTurn) {
                                    viewModel.selectOrCreateChessCell(r, c)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            val piece = board[r][c]
                            if (piece != null) {
                                Text(
                                    text = getPieceGlyphUnicode(piece),
                                    fontSize = 28.sp,
                                    // Highlight Whites as primary, Blacks with darker outlines
                                    color = if (piece.isWhite) Color.White else Color(0xFF212121),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Row of captured Black pieces (Captured by Player)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("Butin de chasse : ", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
            if (captureByPlayer.isEmpty()) {
                Text("Aucun", fontSize = 10.sp, color = Color.Gray)
            } else {
                captureByPlayer.forEach { piece ->
                    Text(getPieceGlyphUnicode(piece), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Moves history list layout
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = "Historique des mouvements :",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                if (moveHistory.isEmpty()) {
                    Text(
                        text = "Jouez un coup pour démarrer la partie.",
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        moveHistory.takeLast(4).forEach { move ->
                            SuggestionChip(
                                onClick = {},
                                label = { Text(move, fontSize = 10.sp) },
                                modifier = Modifier.height(24.dp)
                            )
                        }
                    }
                }
            }
        }

        // Actions Resign or Quit bar !
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { viewModel.quitChessGame() },
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_chess_quit")
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Quitter le plateau")
            }

            Button(
                onClick = { viewModel.endChessGame(false, "Vous avez abandonné la partie.") },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_chess_resign")
            ) {
                Icon(Icons.Default.Flag, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Abandonner")
            }
        }
    }
}

// Chess graphic glyph converters
fun getPieceGlyphUnicode(piece: ChessPiece): String {
    return when (piece.type) {
        "P" -> if (piece.isWhite) "♙" else "♟"
        "T" -> if (piece.isWhite) "♖" else "♜"
        "C" -> if (piece.isWhite) "♘" else "♞"
        "F" -> if (piece.isWhite) "♗" else "♝"
        "D" -> if (piece.isWhite) "♕" else "♛"
        "R" -> if (piece.isWhite) "♔" else "♚"
        else -> ""
    }
}

fun formatTimeClock(sec: Int): String {
    val m = sec / 60
    val s = sec % 60
    return String.format("%02d:%02d", m, s)
}

// ================== MEMORY GAME DESIGN SOLUTIONS ==================
@Composable
fun MemorySection(viewModel: AuraViewModel) {
    val isMemoryActive = viewModel.isMemoryActive
    val resultMsg = viewModel.memoryResultMessage

    if (isMemoryActive) {
        ActiveMemoryGameScreen(viewModel = viewModel)
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Jeu de Mémoire Tactile",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Entraînez votre concentration de travail",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Extension,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = "Trouvez toutes les paires d'icônes identiques le plus rapidement possible. Moins vous faites de mouvements erronés, plus vous gagnez de Pièces !",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            if (resultMsg != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = resultMsg,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.startMemoryGame() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_memory_start")
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Lancer une partie", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ActiveMemoryGameScreen(viewModel: AuraViewModel) {
    val cards = viewModel.memoryCards
    val moves = viewModel.memoryMovesCount

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    text = "Mouvements : $moves",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            TextButton(onClick = { viewModel.startMemoryGame() }) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Recommencer", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("memory_grid")
        ) {
            items(cards.size) { index ->
                val card = cards[index]
                val showContent = card.isFlipped || card.isMatched

                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (card.isMatched) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            } else if (card.isFlipped) {
                                MaterialTheme.colorScheme.secondaryContainer
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable(enabled = !showContent) {
                            viewModel.selectMemoryCard(index)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (showContent) {
                        Icon(
                            imageVector = when (card.iconName) {
                                "Whatshot" -> Icons.Default.Whatshot
                                "AutoAwesome" -> Icons.Default.AutoAwesome
                                "Diamond" -> Icons.Default.Diamond
                                "Star" -> Icons.Default.Star
                                "SmartToy" -> Icons.Default.SmartToy
                                "Lightbulb" -> Icons.Default.Lightbulb
                                "Favorite" -> Icons.Default.Favorite
                                "MusicNote" -> Icons.Default.MusicNote
                                else -> Icons.Default.Help
                            },
                            contentDescription = "Carte",
                            tint = if (card.isMatched) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(32.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.QuestionMark,
                            contentDescription = "Dos de carte",
                            tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        OutlinedButton(
            onClick = { viewModel.quitMemoryGame() },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_memory_quit")
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Quitter la partie")
        }
    }
}

// ================== GAME 1: SUDOKU ZEN ==================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SudokuSection(viewModel: AuraViewModel) {
    val easyInitial = listOf(
        listOf(5, 3, 0,  0, 7, 0,  0, 0, 0),
        listOf(6, 0, 0,  1, 9, 5,  0, 0, 0),
        listOf(0, 9, 8,  0, 0, 0,  0, 6, 0),
        listOf(8, 0, 0,  0, 6, 0,  0, 0, 3),
        listOf(4, 0, 0,  8, 0, 3,  0, 0, 1),
        listOf(7, 0, 0,  0, 2, 0,  0, 0, 6),
        listOf(0, 6, 0,  0, 0, 0,  2, 8, 0),
        listOf(0, 0, 0,  4, 1, 9,  0, 0, 5),
        listOf(0, 0, 0,  0, 8, 0,  0, 7, 9)
    )
    val easySolution = listOf(
        listOf(5, 3, 4,  6, 7, 8,  9, 1, 2),
        listOf(6, 7, 2,  1, 9, 5,  3, 4, 8),
        listOf(1, 9, 8,  3, 4, 2,  5, 6, 7),
        listOf(8, 5, 9,  7, 6, 1,  4, 2, 3),
        listOf(4, 2, 6,  8, 5, 3,  7, 9, 1),
        listOf(7, 1, 3,  9, 2, 4,  8, 5, 6),
        listOf(9, 6, 1,  5, 3, 7,  2, 8, 4),
        listOf(2, 8, 7,  4, 1, 9,  6, 3, 5),
        listOf(3, 4, 5,  2, 8, 6,  1, 7, 9)
    )

    val mediumInitial = listOf(
        listOf(0, 0, 0,  2, 6, 0,  7, 0, 1),
        listOf(6, 8, 0,  0, 7, 0,  0, 9, 0),
        listOf(1, 9, 0,  0, 0, 4,  5, 0, 0),
        listOf(8, 2, 0,  1, 0, 0,  0, 4, 0),
        listOf(0, 0, 4,  6, 0, 2,  9, 0, 0),
        listOf(0, 5, 0,  0, 0, 3,  0, 2, 8),
        listOf(0, 0, 9,  3, 0, 0,  0, 7, 4),
        listOf(0, 4, 0,  0, 5, 0,  0, 3, 6),
        listOf(7, 0, 3,  0, 1, 8,  0, 0, 0)
    )
    val mediumSolution = listOf(
        listOf(4, 3, 5,  2, 6, 9,  7, 8, 1),
        listOf(6, 8, 2,  5, 7, 1,  4, 9, 3),
        listOf(1, 9, 7,  8, 3, 4,  5, 6, 2),
        listOf(8, 2, 6,  1, 9, 5,  3, 4, 7),
        listOf(3, 7, 4,  6, 8, 2,  9, 1, 5),
        listOf(9, 5, 1,  7, 4, 3,  6, 2, 8),
        listOf(5, 1, 9,  3, 2, 6,  8, 7, 4),
        listOf(2, 4, 8,  9, 5, 7,  1, 3, 6),
        listOf(7, 6, 3,  4, 1, 8,  2, 5, 9)
    )

    var selectedDiff by remember { mutableStateOf("Easy") }
    val initialBoard = if (selectedDiff == "Easy") easyInitial else mediumInitial
    val solutionBoard = if (selectedDiff == "Easy") easySolution else mediumSolution

    var currentGrid by remember(selectedDiff) { 
        mutableStateOf(initialBoard.map { it.toMutableList() }) 
    }
    var selectedCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var mistakes by remember { mutableStateOf(0) }
    var userHasWon by remember { mutableStateOf(false) }
    var hintsRemaining by remember { mutableStateOf(3) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Upper stats banner
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Niveau de Difficulté", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Facile",
                            fontWeight = if (selectedDiff == "Easy") FontWeight.Black else FontWeight.Normal,
                            color = if (selectedDiff == "Easy") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            fontSize = 14.sp,
                            modifier = Modifier.clickable { selectedDiff = "Easy" }
                        )
                        Text(
                            text = "Moyen",
                            fontWeight = if (selectedDiff == "Medium") FontWeight.Black else FontWeight.Normal,
                            color = if (selectedDiff == "Medium") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            fontSize = 14.sp,
                            modifier = Modifier.clickable { selectedDiff = "Medium" }
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Erreurs", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$mistakes / 3", fontWeight = FontWeight.Bold, color = if (mistakes >= 3) Color.Red else MaterialTheme.colorScheme.primary)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Indices", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$hintsRemaining restants", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }

        if (userHasWon) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("🎉 Sudoku Complété !", fontWeight = FontWeight.Black, fontSize = 20.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("Vous avez parfaitement résolu la grille Sudoku avec brio.", fontSize = 13.sp, textAlign = TextAlign.Center)
                    Text("+120 🪙 et +100 XP accordés !", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Button(
                        onClick = {
                            mistakes = 0
                            hintsRemaining = 3
                            userHasWon = false
                            currentGrid = initialBoard.map { it.toMutableList() }
                            selectedCell = null
                        }
                    ) {
                        Text("Recommencer la grille")
                    }
                }
            }
        } else {
            // Sudoku Grid Drawing
            Card(
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    for (r in 0..8) {
                        Row {
                            for (c in 0..8) {
                                val value = currentGrid[r][c]
                                val isInitial = initialBoard[r][c] != 0
                                val isSelected = selectedCell == Pair(r, c)
                                
                                val isSubGridBorderRight = (c == 2 || c == 5)
                                val isSubGridBorderBottom = (r == 2 || r == 5)

                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            when {
                                                isSelected -> MaterialTheme.colorScheme.primaryContainer
                                                isInitial -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                                else -> Color.Transparent
                                            }
                                        )
                                        .border(
                                            width = 0.3.dp,
                                            color = MaterialTheme.colorScheme.outlineVariant
                                        )
                                        .clickable {
                                            selectedCell = Pair(r, c)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (value != 0) "$value" else "",
                                        fontWeight = if (isInitial) FontWeight.Black else FontWeight.Medium,
                                        fontSize = 15.sp,
                                        color = when {
                                            isInitial -> MaterialTheme.colorScheme.onSurface
                                            else -> MaterialTheme.colorScheme.primary
                                        }
                                    )

                                    // Inter-grid boundary line drawing shadows
                                    if (isSubGridBorderRight) {
                                        Box(modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight().width(1.5.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)))
                                    }
                                    if (isSubGridBorderBottom) {
                                        Box(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(1.5.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Virtual Numpad
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (num in 1..5) {
                        Button(
                            onClick = {
                                val cell = selectedCell
                                if (cell != null) {
                                    val (r, c) = cell
                                    if (initialBoard[r][c] == 0) {
                                        val sol = solutionBoard[r][c]
                                        if (num == sol) {
                                            val copy = currentGrid.map { it.toMutableList() }
                                            copy[r][c] = num
                                            currentGrid = copy
                                            
                                            // Check win State
                                            var solved = true
                                            for (i in 0..8) {
                                                for (j in 0..8) {
                                                    if (copy[i][j] != solutionBoard[i][j]) solved = false
                                                }
                                            }
                                            if (solved) {
                                                userHasWon = true
                                                viewModel.coins += 120
                                                viewModel.xp += 100
                                            }
                                        } else {
                                            mistakes++
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Text("$num", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (num in 6..9) {
                        Button(
                            onClick = {
                                val cell = selectedCell
                                if (cell != null) {
                                    val (r, c) = cell
                                    if (initialBoard[r][c] == 0) {
                                        val sol = solutionBoard[r][c]
                                        if (num == sol) {
                                            val copy = currentGrid.map { it.toMutableList() }
                                            copy[r][c] = num
                                            currentGrid = copy
                                            
                                            var solved = true
                                            for (i in 0..8) {
                                                for (j in 0..8) {
                                                    if (copy[i][j] != solutionBoard[i][j]) solved = false
                                                }
                                            }
                                            if (solved) {
                                                userHasWon = true
                                                viewModel.coins += 120
                                                viewModel.xp += 100
                                            }
                                        } else {
                                            mistakes++
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Text("$num", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Erase cell Button
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                        onClick = {
                            val cell = selectedCell
                            if (cell != null) {
                                val (r, c) = cell
                                if (initialBoard[r][c] == 0) {
                                    val copy = currentGrid.map { it.toMutableList() }
                                    copy[r][c] = 0
                                    currentGrid = copy
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1.2f)
                            .height(44.dp)
                    ) {
                        Text("Vider", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Suggest / Hint Actions Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        enabled = hintsRemaining > 0 && selectedCell != null,
                        onClick = {
                            val cell = selectedCell
                            if (cell != null) {
                                val (r, c) = cell
                                if (currentGrid[r][c] != solutionBoard[r][c]) {
                                    val copy = currentGrid.map { it.toMutableList() }
                                    copy[r][c] = solutionBoard[r][c]
                                    currentGrid = copy
                                    hintsRemaining--
                                    
                                    var solved = true
                                    for (i in 0..8) {
                                        for (j in 0..8) {
                                            if (copy[i][j] != solutionBoard[i][j]) solved = false
                                        }
                                    }
                                    if (solved) {
                                        userHasWon = true
                                        viewModel.coins += 120
                                        viewModel.xp += 100
                                    }
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Help, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Révéler la Case")
                    }

                    OutlinedButton(
                        onClick = {
                            mistakes = 0
                            hintsRemaining = 3
                            currentGrid = initialBoard.map { it.toMutableList() }
                            selectedCell = null
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Réinitialiser")
                    }
                }
            }
        }
    }
}

// ================== GAME 2: MATH 2048 ==================
@Composable
fun TwoZeroFourEightSection(viewModel: AuraViewModel) {
    var board by remember { mutableStateOf(Array(4) { IntArray(4) { 0 } }) }
    var score by remember { mutableIntStateOf(0) }
    var highTile by remember { mutableIntStateOf(0) }
    var isWin by remember { mutableStateOf(false) }
    var isLoss by remember { mutableStateOf(false) }

    // Sliding helper logic
    fun slideAndMerge(line: IntArray): Pair<IntArray, Int> {
        val filtered = line.filter { it != 0 }
        val mergedList = ArrayList<Int>()
        var addedScore = 0
        var pointer = 0
        while (pointer < filtered.size) {
            if (pointer + 1 < filtered.size && filtered[pointer] == filtered[pointer + 1]) {
                val mergedVal = filtered[pointer] * 2
                mergedList.add(mergedVal)
                addedScore += mergedVal
                pointer += 2
            } else {
                mergedList.add(filtered[pointer])
                pointer++
            }
        }
        while (mergedList.size < 4) {
            mergedList.add(0)
        }
        return Pair(mergedList.toIntArray(), addedScore)
    }

    // Add a new tile '2' (90% chance) or '4' (10% chance) on empty spot
    fun addRandomTile(currentGrid: Array<IntArray>): Array<IntArray> {
        val copy = Array(4) { r -> currentGrid[r].clone() }
        val emptyCoordinates = ArrayList<Pair<Int, Int>>()
        for (r in 0..3) {
            for (c in 0..3) {
                if (copy[r][c] == 0) emptyCoordinates.add(Pair(r, c))
            }
        }
        if (emptyCoordinates.isNotEmpty()) {
            val (chosenRow, chosenCol) = emptyCoordinates.random()
            copy[chosenRow][chosenCol] = if (java.util.Random().nextFloat() < 0.9f) 2 else 4
        }
        return copy
    }

    // Initialize board
    LaunchedEffect(Unit) {
        var startBoard = Array(4) { IntArray(4) { 0 } }
        startBoard = addRandomTile(startBoard)
        startBoard = addRandomTile(startBoard)
        board = startBoard
        score = 0
        isWin = false
        isLoss = false
    }

    // Check game over state
    fun evaluateGameOver(current: Array<IntArray>) {
        var canMove = false
        for (r in 0..3) {
            for (c in 0..3) {
                if (current[r][c] == 0) canMove = true
                if (r + 1 < 4 && current[r][c] == current[r+1][c]) canMove = true
                if (c + 1 < 4 && current[r][c] == current[r][c+1]) canMove = true
            }
        }
        if (!canMove) {
            isLoss = true
        }
    }

    // Trigger sliding animations & actions
    fun handleMove(direction: String) {
        var gridCopy = Array(4) { r -> board[r].clone() }
        var scoreAcc = 0
        var boardAffected = false

        when (direction) {
            "LEFT" -> {
                for (r in 0..3) {
                    val original = gridCopy[r].clone()
                    val (slidLine, pointsEarned) = slideAndMerge(original)
                    gridCopy[r] = slidLine
                    scoreAcc += pointsEarned
                    if (!original.contentEquals(slidLine)) boardAffected = true
                }
            }
            "RIGHT" -> {
                for (r in 0..3) {
                    val original = gridCopy[r].clone()
                    val reversed = original.reversedArray()
                    val (slidLine, pointsEarned) = slideAndMerge(reversed)
                    val resultFinal = slidLine.reversedArray()
                    gridCopy[r] = resultFinal
                    scoreAcc += pointsEarned
                    if (!original.contentEquals(resultFinal)) boardAffected = true
                }
            }
            "UP" -> {
                for (c in 0..3) {
                    val originalLine = intArrayOf(gridCopy[0][c], gridCopy[1][c], gridCopy[2][c], gridCopy[3][c])
                    val (slidLine, pointsEarned) = slideAndMerge(originalLine)
                    scoreAcc += pointsEarned
                    for (r in 0..3) {
                        if (gridCopy[r][c] != slidLine[r]) boardAffected = true
                        gridCopy[r][c] = slidLine[r]
                    }
                }
            }
            "DOWN" -> {
                for (c in 0..3) {
                    val originalLine = intArrayOf(gridCopy[0][c], gridCopy[1][c], gridCopy[2][c], gridCopy[3][c])
                    val reversedLine = originalLine.reversedArray()
                    val (slidLine, pointsEarned) = slideAndMerge(reversedLine)
                    val resultFinal = slidLine.reversedArray()
                    scoreAcc += pointsEarned
                    for (r in 0..3) {
                        if (gridCopy[r][c] != resultFinal[r]) boardAffected = true
                        gridCopy[r][c] = resultFinal[r]
                    }
                }
            }
        }

        if (boardAffected) {
            gridCopy = addRandomTile(gridCopy)
            board = gridCopy
            score += scoreAcc
            
            // Check max tiles
            var maxVal = 0
            for (r in 0..3) {
                for (c in 0..3) {
                    if (gridCopy[r][c] > maxVal) maxVal = gridCopy[r][c]
                }
            }
            highTile = maxVal
            if (maxVal >= 2048 && !isWin) {
                isWin = true
                viewModel.coins += 150
                viewModel.xp += 120
            }
            evaluateGameOver(gridCopy)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // High fidelity heading scoring
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Score Actuel", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("$score", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Tuile Max", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("$highTile", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.secondary)
            }
        }

        if (isWin) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("🌟 Splendide Victoire !", fontWeight = FontWeight.Black, fontSize = 18.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("Félicitations, vous avez atteint la précieuse tuile 2048 !", fontSize = 12.sp, textAlign = TextAlign.Center)
                    Text("+150 jetons et +120 XP récoltés !", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Button(
                        onClick = {
                            var startBoard = Array(4) { IntArray(4) { 0 } }
                            startBoard = addRandomTile(startBoard)
                            startBoard = addRandomTile(startBoard)
                            board = startBoard
                            score = 0
                            highTile = 0
                            isWin = false
                            isLoss = false
                        }
                    ) {
                        Text("Continuer de Jouer")
                    }
                }
            }
        }

        if (isLoss) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("💀 Plus de Mouvements Possibles", fontWeight = FontWeight.Black, fontSize = 15.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                    Text("Tentez à nouveau de repousser les limites logiques.", fontSize = 12.sp)
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        onClick = {
                            var startBoard = Array(4) { IntArray(4) { 0 } }
                            startBoard = addRandomTile(startBoard)
                            startBoard = addRandomTile(startBoard)
                            board = startBoard
                            score = 0
                            highTile = 0
                            isWin = false
                            isLoss = false
                        }
                    ) {
                        Text("Recommencer la partie", color = Color.White)
                    }
                }
            }
        }

        // Draw the 4x4 board grid
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
            ),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (r in 0..3) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (c in 0..3) {
                            val tileValue = board[r][c]
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        when (tileValue) {
                                            0 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                            2 -> Color(0xFFEEE4DA)
                                            4 -> Color(0xFFEDE0C8)
                                            8 -> Color(0xFFF2B179)
                                            16 -> Color(0xFFF59563)
                                            32 -> Color(0xFFF67C5F)
                                            64 -> Color(0xFFF65E3B)
                                            128 -> Color(0xFFEDCF72)
                                            256 -> Color(0xFFEDCC61)
                                            512 -> Color(0xFFEDC850)
                                            1024 -> Color(0xFFEDC53F)
                                            2048 -> Color(0xFFEDC22E)
                                            else -> MaterialTheme.colorScheme.primary
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (tileValue != 0) "$tileValue" else "",
                                    fontWeight = FontWeight.Black,
                                    fontSize = if (tileValue > 512) 16.sp else 20.sp,
                                    color = if (tileValue <= 4) Color(0xFF776E65) else Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // Action Direction Controllers
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            IconButton(
                onClick = { handleMove("UP") },
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
            ) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Haut", tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                IconButton(
                    onClick = { handleMove("LEFT") },
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                ) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Gauche", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }

                Spacer(modifier = Modifier.width(36.dp))

                IconButton(
                    onClick = { handleMove("RIGHT") },
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                ) {
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Droite", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }

            IconButton(
                onClick = { handleMove("DOWN") },
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
            ) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Bas", tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    }
}

// ================== GAME 3: MOTS DOUX (WORDLE) ==================
@Composable
fun WordleSection(viewModel: AuraViewModel) {
    val wordList = listOf("AMOUR", "CALME", "CORPS", "SANTE", "PAUSE", "VIVRE", "FORCE", "TEMPS", "COEUR", "DOUCE", "REVER", "VITAL")
    var secretWord by remember { mutableStateOf(wordList.random()) }
    var guesses by remember { mutableStateOf(List(6) { "" }) }
    var attemptIndex by remember { mutableIntStateOf(0) }
    var currentInput by remember { mutableStateOf("") }
    var isDone by remember { mutableStateOf(false) }
    var hasWon by remember { mutableStateOf(false) }
    var statusMsg by remember { mutableStateOf("Devinez le mot bienveillant secret de 5 lettres !") }

    fun processGuess() {
        if (currentInput.length != 5) {
            statusMsg = "⚠️ Le mot doit contenir exactement 5 lettres !"
            return
        }

        val formattedGuess = currentInput.uppercase()
        val copy = guesses.toMutableList()
        copy[attemptIndex] = formattedGuess
        guesses = copy

        if (formattedGuess == secretWord) {
            hasWon = true
            isDone = true
            statusMsg = "🎉 Merveilleux ! Vous avez deviné le mot : $secretWord"
            viewModel.coins += 100
            viewModel.xp += 80
        } else {
            if (attemptIndex >= 5) {
                isDone = true
                statusMsg = "💀 Échec ! Le mot secret était : $secretWord"
            } else {
                attemptIndex++
                currentInput = ""
                statusMsg = "Lettres validées. Continuez de chercher !"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = statusMsg,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        // Draw 6 attempts lines
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            for (row in 0..5) {
                val guess = guesses[row]
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (col in 0..4) {
                        val isSubmittedRow = row < attemptIndex
                        val isCurrentEditingRow = row == attemptIndex

                        val char = if (isCurrentEditingRow) {
                            if (col < currentInput.length) currentInput[col].toString() else ""
                        } else {
                            if (col < guess.length) guess[col].toString() else ""
                        }

                        // Determine box color
                        val boxColor = when {
                            isSubmittedRow -> {
                                val literal = guess[col]
                                when {
                                    literal == secretWord[col] -> Color(0xFF4CAF50) // Green
                                    secretWord.contains(literal) -> Color(0xFFFF9800) // Yellow
                                    else -> Color(0xFF757575) // Grey
                                }
                            }
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }

                        val textColor = if (isSubmittedRow) Color.White else MaterialTheme.colorScheme.onSurface

                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(boxColor)
                                .border(
                                    width = 1.dp,
                                    color = if (isCurrentEditingRow && col == currentInput.length) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = char,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = textColor
                            )
                        }
                    }
                }
            }
        }

        if (isDone) {
            Button(
                onClick = {
                    secretWord = wordList.random()
                    guesses = List(6) { "" }
                    attemptIndex = 0
                    currentInput = ""
                    isDone = false
                    hasWon = false
                    statusMsg = "Devinez le mot bienveillant secret de 5 lettres !"
                }
            ) {
                Text("Recommencer une partie")
            }
        } else {
            // Virtual Keyboard
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val rows = listOf(
                    listOf("A", "Z", "E", "R", "T", "Y", "U", "I", "O", "P"),
                    listOf("Q", "S", "D", "F", "G", "H", "J", "K", "L", "M"),
                    listOf("W", "X", "C", "V", "B", "N")
                )

                rows.forEachIndexed { idx, rowList ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        rowList.forEach { key ->
                            Box(
                                modifier = Modifier
                                    .size(width = 28.dp, height = 38.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                    .clickable {
                                        if (currentInput.length < 5) {
                                            currentInput += key
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(key, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }

                        // Appends functional keys to first row bottom rows
                        if (idx == 2) {
                            // Erase / Return key
                            Box(
                                modifier = Modifier
                                    .size(width = 46.dp, height = 38.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.errorContainer)
                                    .clickable {
                                        if (currentInput.isNotEmpty()) {
                                            currentInput = currentInput.dropLast(1)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Backspace, contentDescription = "Effacer", tint = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.size(16.dp))
                            }

                            // Enter Key
                            Box(
                                modifier = Modifier
                                    .size(width = 64.dp, height = 38.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                                    .clickable { processGuess() },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Entrée", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ================== GAME 4: LIGHTS OUT (LUMIERES OUT) ==================
@Composable
fun LightsOutSection(viewModel: AuraViewModel) {
    var grid by remember { mutableStateOf(Array(5) { BooleanArray(5) { false } }) } // true is On, false is Off
    var moveCount by remember { mutableIntStateOf(0) }
    var solvedState by remember { mutableStateOf(false) }

    fun generateLevel(levelId: Int) {
        val nextGrid = Array(5) { BooleanArray(5) { false } }
        when (levelId) {
            1 -> {
                // "X" Shape patterns
                nextGrid[0][0] = true; nextGrid[0][4] = true
                nextGrid[1][1] = true; nextGrid[1][3] = true
                nextGrid[2][2] = true
                nextGrid[3][1] = true; nextGrid[3][3] = true
                nextGrid[4][0] = true; nextGrid[4][4] = true
            }
            2 -> {
                // Cruciform cross
                for (i in 0..4) {
                    nextGrid[2][i] = true
                    nextGrid[i][2] = true
                }
            }
            else -> {
                // Random reverse toggled 12 times to guarantee a valid solution
                val rand = java.util.Random()
                for (step in 1..12) {
                    val r = rand.nextInt(5)
                    val c = rand.nextInt(5)
                    // Toggle crosses
                    nextGrid[r][c] = !nextGrid[r][c]
                    if (r - 1 >= 0) nextGrid[r-1][c] = !nextGrid[r-1][c]
                    if (r + 1 < 5) nextGrid[r+1][c] = !nextGrid[r+1][c]
                    if (c - 1 >= 0) nextGrid[r][c-1] = !nextGrid[r][c-1]
                    if (c + 1 < 5) nextGrid[r][c+1] = !nextGrid[r][c+1]
                }
            }
        }
        grid = nextGrid
        moveCount = 0
        solvedState = false
    }

    LaunchedEffect(Unit) {
        generateLevel(1)
    }

    fun handlePress(r: Int, c: Int) {
        if (solvedState) return
        val copy = Array(5) { row -> grid[row].clone() }
        
        // Commute surrounding crosses
        copy[r][c] = !copy[r][c]
        if (r - 1 >= 0) copy[r-1][c] = !copy[r-1][c]
        if (r + 1 < 5) copy[r+1][c] = !copy[r+1][c]
        if (c - 1 >= 0) copy[r][c-1] = !copy[r][c-1]
        if (c + 1 < 5) copy[r][c+1] = !copy[r][c+1]
        
        grid = copy
        moveCount++

        // Check if all Lights are OFF
        var win = true
        for (i in 0..4) {
            for (j in 0..4) {
                if (copy[i][j]) win = false
            }
        }
        if (win) {
            solvedState = true
            viewModel.coins += 80
            viewModel.xp += 80
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Upper choices Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Total Coups", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("$moveCount", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { generateLevel(1) }, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp), modifier = Modifier.height(32.dp)) {
                    Text("Niv.1", fontSize = 10.sp)
                }
                Button(onClick = { generateLevel(2) }, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp), modifier = Modifier.height(32.dp)) {
                    Text("Niv.2", fontSize = 10.sp)
                }
                Button(onClick = { generateLevel(3) }, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp), modifier = Modifier.height(32.dp)) {
                    Text("Aléat", fontSize = 10.sp)
                }
            }
        }

        if (solvedState) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("💡 Écran Totalement Éteint !", fontWeight = FontWeight.Black, fontSize = 16.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("Superbe finesse algébrique. Vous avez déchiffré l'énigme !", fontSize = 12.sp)
                    Text("+80 🪙 et +80 XP accordés !", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Button(onClick = { generateLevel(3) }) {
                        Text("Suivant")
                    }
                }
            }
        }

        // Draw Board 5x5
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.aspectRatio(1f).fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (r in 0..4) {
                    Row(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (c in 0..4) {
                            val isLit = grid[r][c]
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isLit) Color(0xFFFFEB3B).copy(alpha = 0.85f)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isLit) Color(0xFFFBC02D) else MaterialTheme.colorScheme.outlineVariant,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { handlePress(r, c) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = if (isLit) Color(0xFFF57F17) else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ================== GAME 5: SIMON SENSE (SIMON SAYS) ==================
@Composable
fun SimonSaysSection(viewModel: AuraViewModel) {
    var aiSequence by remember { mutableStateOf(listOf<Int>()) }
    var playerSequence by remember { mutableStateOf(listOf<Int>()) }
    var activeTileIndex by remember { mutableIntStateOf(-1) }
    var score by remember { mutableIntStateOf(0) }
    var bestScore by remember { mutableIntStateOf(0) }
    var isShowingSeq by remember { mutableStateOf(false) }
    var stateMessage by remember { mutableStateOf("Prenez une grande respiration, puis appuyez sur Démarrer !") }

    val coroutineScope = rememberCoroutineScope()

    fun triggerAiSequence() {
        coroutineScope.launch {
            isShowingSeq = true
            playerSequence = listOf()
            stateMessage = "Simon génère la mélodie..."
            
            // Add a new random index
            val updated = aiSequence + (0..3).random()
            aiSequence = updated
            
            delay(800)
            for (step in updated) {
                activeTileIndex = step
                delay(550)
                activeTileIndex = -1
                delay(180)
            }
            
            isShowingSeq = false
            stateMessage = "À vous de jouer ! Répétez la même suite..."
        }
    }

    fun handleTileClick(index: Int) {
        if (isShowingSeq || aiSequence.isEmpty()) return
        
        coroutineScope.launch {
            // Momentarily highlight the tile clicked
            activeTileIndex = index
            delay(150)
            activeTileIndex = -1
            
            val nextPlayerSeq = playerSequence + index
            playerSequence = nextPlayerSeq
            
            // Verify step matching
            val stepIndexToCheck = nextPlayerSeq.size - 1
            if (nextPlayerSeq[stepIndexToCheck] == aiSequence[stepIndexToCheck]) {
                // Correct tile !
                if (nextPlayerSeq.size == aiSequence.size) {
                    score++
                    if (score > bestScore) bestScore = score
                    stateMessage = "Correct ! Préparation du tour suivant..."
                    viewModel.coins += 10
                    viewModel.xp += 10
                    delay(1200)
                    triggerAiSequence()
                }
            } else {
                // WRONG! Game Over
                stateMessage = "💀 Erreur ! Fin de partie. Score final : $score"
                aiSequence = listOf()
                playerSequence = listOf()
                score = 0
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Statistics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Score", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("$score", fontWeight = FontWeight.Black, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Meilleur", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("$bestScore", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.secondary)
            }
        }

        Text(
            text = stateMessage,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        // 2x2 Simon Button Panel
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            modifier = Modifier.aspectRatio(1f).fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Quadrant 0: Green (Top Left)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 8.dp, bottomStart = 8.dp, bottomEnd = 8.dp))
                            .background(
                                if (activeTileIndex == 0) Color(0xFF00FF00) else Color(0xFF005A00)
                            )
                            .clickable { handleTileClick(0) }
                    )
                    
                    // Quadrant 1: Red (Top Right)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 40.dp, bottomStart = 8.dp, bottomEnd = 8.dp))
                            .background(
                                if (activeTileIndex == 1) Color(0xFFFF0000) else Color(0xFF6E0000)
                            )
                            .clickable { handleTileClick(1) }
                    )
                }

                Row(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Quadrant 2: Yellow (Bottom Left)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 40.dp, bottomEnd = 8.dp))
                            .background(
                                if (activeTileIndex == 2) Color(0xFFFFEB3B) else Color(0xFF635A00)
                            )
                            .clickable { handleTileClick(2) }
                    )

                    // Quadrant 3: Blue (Bottom Right)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 8.dp, bottomEnd = 40.dp))
                            .background(
                                if (activeTileIndex == 3) Color(0xFF2196F3) else Color(0xFF0D3F69)
                            )
                            .clickable { handleTileClick(3) }
                    )
                }
            }
        }

        if (aiSequence.isEmpty()) {
            Button(
                onClick = { triggerAiSequence() },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Démarrer le Simon", fontWeight = FontWeight.Bold)
            }
        }
    }
}

