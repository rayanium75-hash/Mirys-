package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.viewmodel.AuraViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumHubDialog(
    viewModel: AuraViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var activeSubSection by remember { mutableStateOf("home") } // "home", or specific feature page
    
    // Local state for dream oracle
    var dreamText by remember { mutableStateOf("") }
    
    // Local state for translator
    var translateInputText by remember { mutableStateOf("Bonjour mon ami. Que le calme cosmique guide tes pas de dev aujourd'hui.") }
    var translatedResult by remember { mutableStateOf("") }
    var isTranslating by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF030712))
                .testTag("premium_hub_dialog"),
            color = Color(0xFF030712)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Top Header of Premium Club
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = {
                                if (activeSubSection != "home") {
                                    activeSubSection = "home"
                                    viewModel.triggerBeep(3)
                                } else {
                                    onDismiss()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (activeSubSection == "home") Icons.Outlined.Close else Icons.Outlined.ArrowBack,
                                contentDescription = "Retour",
                                tint = Color(0xFFECEFF1)
                            )
                        }

                        Column {
                            Text(
                                text = "CLUB PREMIUM MULTIVERS 👑",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFFFFD700),
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = if (activeSubSection == "home") "Espace Club Mirys" else activeSubSection.uppercase(),
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }
                    }

                    // Gold balance showcase
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2937)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Paid,
                                contentDescription = "Pièces",
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${viewModel.coins} 🪙",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }
                    }
                }

                // Check Subscription Status Hook
                val isPremiumUser = viewModel.subscriptionTier != "Gratuit" || viewModel.isFreeTrialActive
                if (viewModel.isFreeTrialActive) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        border = BorderStroke(1.2.dp, Brush.horizontalGradient(listOf(Color(0xFF00FFCC), Color(0xFF8B5CF6)))),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Timer,
                                contentDescription = null,
                                tint = Color(0xFF00FFCC),
                                modifier = Modifier.size(24.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Essai Gratuit Actif (2 jours)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    "Temps restant : ${viewModel.trialTimeLabel}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF00FFCC)
                                )
                            }
                            Button(
                                onClick = {
                                    viewModel.endFreeTrialImmediately()
                                    Toast.makeText(context, "Essai expiré ! Retour au payant.", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("SIMULER FIN", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                } else if (!isPremiumUser) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        border = BorderStroke(1.dp, Color(0xFF475569)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(24.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Accès Club Privé Mirys",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    "Gratuit pendant 2 jours !",
                                    fontSize = 10.sp,
                                    color = Color(0xFFFFD700),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = {
                                        viewModel.start2DayFreeTrial()
                                        Toast.makeText(context, "Essai de 2 jours activé ! Profitez des outils de Mirys. 🎉", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFCC)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("ESSAI 2J", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                                }
                                Button(
                                    onClick = {
                                        viewModel.subscriptionTier = "Premium Ultimate 👑"
                                        viewModel.triggerBeep(1)
                                        Toast.makeText(context, "Abonnement Premium activé ! 🎉", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("ACHETER", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                            }
                        }
                    }
                }

                // Render Espace body
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    AnimatedContent(
                        targetState = activeSubSection,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                        },
                        label = "PremiumTabTransition"
                    ) { state ->
                        when (state) {
                            "home" -> PremiumHubMenu(
                                viewModel = viewModel,
                                isPremium = isPremiumUser,
                                onSelectSub = { activeSubSection = it }
                            )
                            "radar" -> PremiumRadarPage(viewModel = viewModel, isPremium = isPremiumUser)
                            "ambient_sound" -> PremiumAmbientSoundPage(viewModel = viewModel, isPremium = isPremiumUser)
                            "translator" -> PremiumTranslatorPage(
                                viewModel = viewModel,
                                isPremium = isPremiumUser,
                                translateInputText = translateInputText,
                                onInputTextChange = { translateInputText = it },
                                translatedResult = translatedResult,
                                onTranslatedResultChange = { translatedResult = it },
                                isTranslating = isTranslating,
                                onTranslatingChange = { isTranslating = it }
                            )
                            "dream" -> PremiumDreamOraclePage(
                                viewModel = viewModel,
                                isPremium = isPremiumUser,
                                dreamText = dreamText,
                                onDreamTextChange = { dreamText = it }
                            )
                            "skins" -> PremiumAvatarSkinsPage(viewModel = viewModel, isPremium = isPremiumUser)
                            "tournament" -> PremiumTournamentLeaguePage(viewModel = viewModel, isPremium = isPremiumUser)
                            "spider_chart" -> PremiumAuraSpiderGraphPage(viewModel = viewModel, isPremium = isPremiumUser)
                            "astral_salon" -> PremiumAstralSalonPage(viewModel = viewModel, isPremium = isPremiumUser)
                            "mood_halo" -> PremiumEmotionalHaloPage(viewModel = viewModel, isPremium = isPremiumUser)
                            "story_quest" -> PremiumStoryQuestPage(viewModel = viewModel, isPremium = isPremiumUser)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumHubMenu(
    viewModel: AuraViewModel,
    isPremium: Boolean,
    onSelectSub: (String) -> Unit
) {
    val items = listOf(
        Triple("radar", "Radar Spatial de Compatibilité", "Détectez des esprits de Mirys synchrones près de vous."),
        Triple("ambient_sound", "Générateur Acoustique Ambiant", "Procedural synthesiser d'ambiance cosmique d'ondes vibrantes."),
        Triple("translator", "Traducteur Universel Vocal", "Traduisez les messages DMs vocaux vers d'autres dialectes."),
        Triple("dream", "Oracle Décrypteur de Rêves", "Sondez votre subconscient pour tirer votre Tarot Cosmique quotidien."),
        Triple("skins", "Skins de Profil Holographiques", "Activez d'incroyables auras d'éclats fluorescents autour de vous."),
        Triple("tournament", "Arène de Tournois d'Élite", "Défiez d'autres maîtres d'Échecs ou Quiz pour gagner des trophées."),
        Triple("spider_chart", "Anatomie de votre Graphe de Mirys", "Une toile d'analyse de données 3D multi-axes de vos performances."),
        Triple("astral_salon", "Salon Vocal Privé Spatialisé", "Hébergez des conférences privées avec égaliseur réactif."),
        Triple("mood_halo", "Projecteur Émotionnel Halo", "Affichez un halo lumineux de votre humeur courante sur les en-têtes."),
        Triple("story_quest", "Aventure Interactive IA", "Participez à des aventures IA textuelles thérapeutiques à choix multiple.")
    )

    ScrollableMenuLayout {
        Text(
            text = "Bienvenue dans le multivers de Mirys. Voici dix modules forgés pour transcender votre expérience émotionnelle et tactique. Sélectionnez un module pour démarrer.",
            fontSize = 12.sp,
            color = Color.LightGray,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        items.forEachIndexed { idx, (id, title, desc) ->
            Card(
                onClick = {
                    viewModel.triggerBeep(1)
                    onSelectSub(id)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .testTag("premium_menu_item_$id"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                border = BorderStroke(
                    1.2.dp,
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFFFD700).copy(alpha = 0.5f),
                            Color(0xFF8B5CF6).copy(alpha = 0.5f)
                        )
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(Color(0xFF374151), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${idx + 1}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFFD700)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Text(
                            text = desc,
                            fontSize = 11.sp,
                            color = Color.Gray,
                            lineHeight = 15.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = Color(0xFFFFD700)
                    )
                }
            }
        }
    }
}

// Wrapper for scrolling menus
@Composable
fun ScrollableMenuLayout(content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030712))
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            content()
        }
    }
}

// --- FEATURE 1: COSMIC RADAR COMPATIBILITY ---
@Composable
fun PremiumRadarPage(viewModel: AuraViewModel, isPremium: Boolean) {
    val coroutineScope = rememberCoroutineScope()
    var scannedUsers by remember { mutableStateOf<List<Map<String, String>>>(emptyList()) }
    val context = LocalContext.current

    // Rotation animation
    val infiniteTransition = rememberInfiniteTransition(label = "RadarTransition")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RadarAngle"
    )

    ScrollableMenuLayout {
        Text(
            "Le radar utilise la télémétrie astrale de Mirys pour rechercher d'autres esprits connectés partageant des affinités tactiques proches de vous.",
            fontSize = 12.sp,
            color = Color.LightGray
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Large animated scanning interface (Canvas)
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(240.dp)
                .background(Color(0xFF0D1B2A), CircleShape)
                .border(2.dp, Color(0xFF00E676), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Rotating scanner brush line
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                
                // Static rings
                drawCircle(color = Color(0xFF00E676).copy(alpha = 0.15f), radius = size.width / 4, style = Stroke(1.5f))
                drawCircle(color = Color(0xFF00E676).copy(alpha = 0.1f), radius = size.width / 2.5f, style = Stroke(1.5f))
                drawCircle(color = Color(0xFF00E676).copy(alpha = 0.05f), radius = size.width / 1.7f, style = Stroke(1.5f))

                // Axis indicators
                drawLine(
                    color = Color(0xFF00E676).copy(alpha = 0.3f),
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width, size.height / 2),
                    strokeWidth = 1f
                )
                drawLine(
                    color = Color(0xFF00E676).copy(alpha = 0.3f),
                    start = Offset(size.width / 2, 0f),
                    end = Offset(size.width / 2, size.height),
                    strokeWidth = 1f
                )
            }

            // Radar needle pointer rotation overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(angle)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)
                    // Draw sweep line
                    drawLine(
                        color = Color(0xFF00E676),
                        start = center,
                        end = Offset(
                            size.width / 2 + (size.width / 2) * cos(0f),
                            size.height / 2 + (size.height / 2) * sin(0f)
                        ),
                        strokeWidth = 4f
                    )
                }
            }

            // Central beacon pulse
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(Color.White, CircleShape)
                    .border(3.dp, Color(0xFF00E676), CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (!isPremium && viewModel.coins < 40) {
                    Toast.makeText(context, "Requiert un abonnement ou 40 pièces d'or !", Toast.LENGTH_SHORT).show()
                } else {
                    if (!isPremium) viewModel.coins -= 40
                    viewModel.scanPremiumRadar {
                        scannedUsers = listOf(
                            mapOf("handle" to "sonia_coder", "name" to "Sonia 🇸🇳", "compat" to "96%", "elo" to "1320 ELO", "vibe" to "Énergie Sereine 🍃"),
                            mapOf("handle" to "alane_coder", "name" to "Alane 🇨🇲", "compat" to "88%", "elo" to "1450 ELO", "vibe" to "Créative Émeraude 🌿"),
                            mapOf("handle" to "zia_peace", "name" to "Zia 🇬🇦", "compat" to "74%", "elo" to "1250 ELO", "vibe" to "Feu Astral 🔥")
                        )
                    }
                }
            },
            enabled = !viewModel.premiumRadarScanning,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("btn_scan_premium_radar"),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
        ) {
            Text(
                text = if (viewModel.premiumRadarScanning) "Astronomie en cours... 🛰️" else "Démarrer le Scanner Tactique (40 🪙)",
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        if (scannedUsers.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Résultats trouvés à proximité :", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
            scannedUsers.forEach { user ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(user["name"] ?: "", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                            Text("@${user["handle"]} • ${user["elo"]}", fontSize = 11.sp, color = Color.LightGray)
                            Text("Vibe : ${user["vibe"]}", fontSize = 10.sp, color = Color(0xFF00E676))
                        }
                        Text(
                            "Affinité : ${user["compat"]}",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = Color(0xFFFFD700)
                        )
                    }
                }
            }
        }
    }
}

// --- FEATURE 2: PROCEDURAL AMBIENT SOUND GENERATOR ---
@Composable
fun PremiumAmbientSoundPage(viewModel: AuraViewModel, isPremium: Boolean) {
    val context = LocalContext.current
    val themes = listOf(
        Triple("serenity", "Sérénité d'Émeraude 🍃", "Ton apaisant 440 Hz alignant l'empathie naturelle."),
        Triple("nebula", "Nébuleuse Cosmique 🌌", "Écho de fréquence spatiale 660 Hz pour la relaxation profonde."),
        Triple("fire", "Tempête Martienne 🔥", "Glow de vagues thermiques tactiques de haute focalisation.")
    )

    ScrollableMenuLayout {
        Text(
            "Génère des harmonies d'ambiance et des ondes cérébrales par résonance acoustique directe pour optimiser vos phases de travail ou de méditation.",
            fontSize = 12.sp,
            color = Color.LightGray
        )

        Spacer(modifier = Modifier.height(16.dp))

        themes.forEach { (id, title, desc) ->
            val isActive = viewModel.premiumSoundscapeTheme == id
            Card(
                onClick = {
                    if (!isPremium && viewModel.coins < 50) {
                        Toast.makeText(context, "Requiert 50 pièces !", Toast.LENGTH_SHORT).show()
                    } else {
                        if (!isPremium && !isActive) viewModel.coins -= 50
                        viewModel.playPremiumAmbientSound(if (isActive) "none" else id)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isActive) Color(0xFF312E81) else Color(0xFF1F2937)
                ),
                border = BorderStroke(
                    1.5.dp,
                    if (isActive) Color(0xFF818CF8) else Color.Transparent
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                        Text(desc, fontSize = 11.sp, color = Color.LightGray)
                    }

                    Icon(
                        imageVector = if (isActive) Icons.Outlined.PauseCircle else Icons.Outlined.PlayCircle,
                        contentDescription = "Contrôler",
                        tint = if (isActive) Color(0xFFFFD700) else Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        if (viewModel.premiumSoundscapeTheme != "none") {
            Spacer(modifier = Modifier.height(24.dp))
            Text("Simulateur d'égaliseur graphique :", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
            Spacer(modifier = Modifier.height(10.dp))
            
            // Render bouncing wave diagram
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(Color(0xFF111827), RoundedCornerShape(12.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "EqualizerLoop")
                for (i in 0..11) {
                    val floatVal by infiniteTransition.animateFloat(
                        initialValue = 0.1f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(150 + i * 40, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "EqualizerBar_$i"
                    )
                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .fillMaxHeight(floatVal)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFFFFD700), Color(0xFF818CF8))
                                ),
                                RoundedCornerShape(4.dp)
                            )
                    )
                }
            }
        }
    }
}

// --- FEATURE 3: DM VOCAL COSMIC TRANSLATOR ---
@Composable
fun PremiumTranslatorPage(
    viewModel: AuraViewModel,
    isPremium: Boolean,
    translateInputText: String,
    onInputTextChange: (String) -> Unit,
    translatedResult: String,
    onTranslatedResultChange: (String) -> Unit,
    isTranslating: Boolean,
    onTranslatingChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val languages = listOf("Wolof 🇸🇳", "Lingala 🇨🇩", "Bambara 🇲🇱", "Spanish 🇪🇸", "Japanese 🇯🇵")

    ScrollableMenuLayout {
        Text(
            "Le convertisseur vocal astral traduit instantanément les communications directes dans d'autres langues et textures sonores avec synthèse de relecture TextToSpeech.",
            fontSize = 12.sp,
            color = Color.LightGray
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = translateInputText,
            onValueChange = onInputTextChange,
            label = { Text("Script vocal à traduire", color = Color.LightGray) },
            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFFD700),
                unfocusedBorderColor = Color.Gray
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("Langue de destination :", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            languages.forEach { lang ->
                val isSelected = viewModel.premiumTranslatorLanguage == lang
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) Color(0xFFFFD700) else Color(0xFF374151))
                        .clickable {
                            viewModel.premiumTranslatorLanguage = lang
                            viewModel.triggerBeep(1)
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(lang, fontWeight = FontWeight.Bold, color = if (isSelected) Color.Black else Color.White, fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (!isPremium && viewModel.coins < 15) {
                    Toast.makeText(context, "Requiert 15 pièces d'or !", Toast.LENGTH_SHORT).show()
                } else {
                    if (!isPremium) viewModel.coins -= 15
                    coroutineScope.launch {
                        onTranslatingChange(true)
                        val trans = viewModel.runPremiumTranslation(translateInputText, viewModel.premiumTranslatorLanguage)
                        onTranslatedResultChange(trans)
                        onTranslatingChange(false)
                        viewModel.triggerBeep(1)
                    }
                }
            },
            enabled = !isTranslating && translateInputText.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
        ) {
            Text(if (isTranslating) "Traduction en cours... 🪐" else "Lancer la Traduction Cosmique (-15 🪙)", color = Color.Black, fontWeight = FontWeight.Bold)
        }

        if (translatedResult.isNotBlank()) {
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = BorderStroke(1.dp, Color(0xFFFFD700))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Résultat Traduit • ${viewModel.premiumTranslatorLanguage}", fontWeight = FontWeight.Bold, color = Color(0xFFFFD700), fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(translatedResult, color = Color.White, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            viewModel.speakText(translatedResult)
                            Toast.makeText(context, "Lecture audio text-to-speech...", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Outlined.VolumeUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Relecture Audio", color = Color.White, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// --- FEATURE 4: DREAM ORACLE TAROT CARDS ---
@Composable
fun PremiumDreamOraclePage(
    viewModel: AuraViewModel,
    isPremium: Boolean,
    dreamText: String,
    onDreamTextChange: (String) -> Unit
) {
    val context = LocalContext.current
    val borderHexColor = remember(viewModel.premiumDreamMoodColorHex) {
        try {
            Color(android.graphics.Color.parseColor(viewModel.premiumDreamMoodColorHex))
        } catch (e: Exception) {
            Color(0xFFEC4899)
        }
    }

    ScrollableMenuLayout {
        Text(
            "Entrez la description de votre dernier rêve. L'algorithme Oracle IA calculera sa couleur d'aura sous-jacente et tirera votre carte de Tarot spirituel quotidienne.",
            fontSize = 12.sp,
            color = Color.LightGray
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = dreamText,
            onValueChange = onDreamTextChange,
            placeholder = { Text("J'ai rêvé que je volais au-dessus d'une grande forêt lumineuse...", color = Color.Gray, fontSize = 12.sp) },
            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFEC4899),
                unfocusedBorderColor = Color.Gray
            ),
            enabled = !viewModel.isDreamOracleAnalyzing
        )

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = {
                if (!isPremium && viewModel.coins < 30) {
                    Toast.makeText(context, "Requiert 30 pièces d'or !", Toast.LENGTH_SHORT).show()
                } else {
                    if (!isPremium) viewModel.coins -= 30
                    viewModel.triggerPremiumDreamOracle(dreamText)
                }
            },
            enabled = dreamText.isNotBlank() && !viewModel.isDreamOracleAnalyzing,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899))
        ) {
            if (viewModel.isDreamOracleAnalyzing) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("L'Oracle interroge le Cosmos...", color = Color.White, fontWeight = FontWeight.Bold)
            } else {
                Text("Sonder mes Rêves (-30 🪙)", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        if (viewModel.isDreamOracleAnalyzing) {
            Spacer(modifier = Modifier.height(30.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🔮 Alignement spirituel en cours...", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(color = Color(0xFFEC4899), modifier = Modifier.width(180.dp))
            }
        } else if (viewModel.premiumDreamAnalysisResult != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
                border = BorderStroke(1.5.dp, borderHexColor)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Tarot Card representation
                    Box(
                        modifier = Modifier
                            .size(width = 120.dp, height = 180.dp)
                            .background(Color(0xFF312E81), RoundedCornerShape(12.dp))
                            .border(2.dp, borderHexColor, RoundedCornerShape(12.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🔮 ORACLE", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = borderHexColor)
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = viewModel.premiumDreamOracleCard ?: "•",
                                fontSize = 28.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text("AURA ALIGN", fontSize = 9.sp, color = Color.LightGray)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "VOTRE CARTE : ${viewModel.premiumDreamOracleCard}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = Color(0xFFFFD700)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = viewModel.premiumDreamAnalysisResult ?: "",
                        fontSize = 12.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// --- FEATURE 5: HOLOGRAPHIC AVATAR SKINS ---
@Composable
fun PremiumAvatarSkinsPage(viewModel: AuraViewModel, isPremium: Boolean) {
    val context = LocalContext.current
    val skins = listOf(
        Pair("none", "Défaut d'Aura 👤"),
        Pair("aurora", "Aurore Boréale 🌌"),
        Pair("cyber", "Scan Cyber Matriciel 🤖"),
        Pair("royal", "Lueur Étoilée Royale 👑")
    )

    ScrollableMenuLayout {
        Text(
            "Équipez des enveloppes et trainées visuelles holographiques exclusives qui scintillent sur les écrans sociaux de l'application.",
            fontSize = 12.sp,
            color = Color.LightGray
        )

        Spacer(modifier = Modifier.height(16.dp))

        skins.forEach { (id, title) ->
            val isEquipped = viewModel.activatedAuraSkin == id
            Card(
                onClick = {
                    if (!isPremium && id != "none" && viewModel.coins < 80) {
                        Toast.makeText(context, "Requiert PRO ou un déblocage à 80 pièces !", Toast.LENGTH_SHORT).show()
                    } else {
                        if (!isPremium && id != "none" && !isEquipped) {
                            viewModel.coins -= 80
                        }
                        viewModel.selectAuraSkin(id)
                        Toast.makeText(context, "Skin d'Aura mis à jour !", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isEquipped) Color(0xFF1E1B4B) else Color(0xFF111827)
                ),
                border = BorderStroke(1.2.dp, if (isEquipped) Color(0xFF818CF8) else Color.Transparent)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (isEquipped) {
                            Text("ÉQUIPÉ ✨", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        } else {
                            Text(if (id == "none") "Gratuit" else "Déblocage (80 🪙)", color = Color.LightGray, fontSize = 11.sp)
                        }
                        Icon(
                            imageVector = if (isEquipped) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (isEquipped) Color(0xFFFFD700) else Color.Gray
                        )
                    }
                }
            }
        }
    }
}

// --- FEATURE 6: TOURNAMENT LEAGUE ARENA ---
@Composable
fun PremiumTournamentLeaguePage(viewModel: AuraViewModel, isPremium: Boolean) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isSearchingMatch by remember { mutableStateOf(false) }

    ScrollableMenuLayout {
        Text(
            "Prenez part au championnat hebdomadaire d'Aura Chess & Blitz. Gagnez de grands trophées cosmiques exclusifs.",
            fontSize = 12.sp,
            color = Color.LightGray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Big visual trophies rank summary
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
            border = BorderStroke(1.dp, Color(0xFFFFD700))
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(imageVector = Icons.Outlined.EmojiEvents, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(56.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("VOTRE RANG : LIGUE MASTERS 🏆", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.White)
                Row(modifier = Modifier.padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Trophées : ${viewModel.premiumTournamentTrophies} 🏆", fontSize = 12.sp, color = Color.LightGray)
                    Text("Parties Gagnées : ${viewModel.premiumTournamentsWon} 🥇", fontSize = 12.sp, color = Color.LightGray)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Sélecteur de Ligue Régionale :", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf("Ligue de Platine", "Arène de Diamant", "Multivers Élite 👑").forEach { league ->
                val isSel = viewModel.selectedEliteArenaLeague == league
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSel) Color(0xFFFFD700) else Color(0xFF374151))
                        .clickable { viewModel.selectedEliteArenaLeague = league }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(league, fontWeight = FontWeight.Bold, color = if (isSel) Color.Black else Color.White, fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (!isPremium) {
                    Toast.makeText(context, "Le matchmaking de Tournoi requiert un abonnement PRO/Premium !", Toast.LENGTH_SHORT).show()
                } else {
                    isSearchingMatch = true
                    coroutineScope.launch {
                        delay(2000)
                        isSearchingMatch = false
                        viewModel.premiumTournamentTrophies += 25
                        viewModel.premiumTournamentsWon += 1
                        viewModel.triggerBeep(1)
                        Toast.makeText(context, "Victoire de tournoi en direct ! +25 trophées collectés ! 🏆🥇", Toast.LENGTH_LONG).show()
                    }
                }
            },
            enabled = !isSearchingMatch,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
        ) {
            Text(if (isSearchingMatch) "Recherche d'un advesaire en direct..." else "Rejoindre l'Arène de Tournoi ⚔️", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}

// --- FEATURE 7: 3D RADAR SPIDER CHART CANVAS ---
@Composable
fun PremiumAuraSpiderGraphPage(viewModel: AuraViewModel, isPremium: Boolean) {
    ScrollableMenuLayout {
        Text(
            "Anatomie de votre toile d'Aura. Représentation multidimensionnelle de vos variations cognitives, sportives et de concentration.",
            fontSize = 12.sp,
            color = Color.LightGray
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Canvas element drawing dynamic concentric pentagon spider webs
        Box(
            modifier = Modifier
                .size(240.dp)
                .align(Alignment.CenterHorizontally)
                .background(Color(0xFF0F172A), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2
                val cy = size.height / 2
                val maxRadius = size.width * 0.42f
                val angles = listOf(0f, 72f, 144f, 216f, 288f)

                // 3 Concentric pentagons
                for (rIdx in 1..3) {
                    val radius = maxRadius * (rIdx / 3f)
                    val pentagonPath = Path()
                    angles.forEachIndexed { i, angle ->
                        val rad = Math.toRadians(angle.toDouble() - 90.0)
                        val x = (cx + radius * cos(rad)).toFloat()
                        val y = (cy + radius * sin(rad)).toFloat()
                        if (i == 0) pentagonPath.moveTo(x, y) else pentagonPath.lineTo(x, y)
                    }
                    pentagonPath.close()
                    drawPath(
                        path = pentagonPath,
                        color = Color(0xFF818CF8).copy(alpha = 0.25f * rIdx),
                        style = Stroke(width = 1.5f)
                    )
                }

                // Axis spokes
                angles.forEach { angle ->
                    val rad = Math.toRadians(angle.toDouble() - 90.0)
                    drawLine(
                        color = Color(0xFF475569),
                        start = Offset(cx, cy),
                        end = Offset((cx + maxRadius * cos(rad)).toFloat(), (cy + maxRadius * sin(rad)).toFloat()),
                        strokeWidth = 1f
                    )
                }

                // Real Player Stat Shape (e.g. 50%, 80%, 70%, 90%, 60%)
                val stats = listOf(0.70f, 0.85f, 0.65f, 0.90f, 0.75f)
                val statPath = Path()
                angles.forEachIndexed { i, angle ->
                    val rad = Math.toRadians(angle.toDouble() - 90.0)
                    val r = maxRadius * stats[i]
                    val x = (cx + r * cos(rad)).toFloat()
                    val y = (cy + r * sin(rad)).toFloat()
                    if (i == 0) statPath.moveTo(x, y) else statPath.lineTo(x, y)
                }
                statPath.close()

                drawPath(
                    path = statPath,
                    color = Color(0xFFFFD700).copy(alpha = 0.45f)
                )
                drawPath(
                    path = statPath,
                    color = Color(0xFFFFD700),
                    style = Stroke(width = 3f)
                )

                // Vertices dots
                angles.forEachIndexed { i, angle ->
                    val rad = Math.toRadians(angle.toDouble() - 90.0)
                    val r = maxRadius * stats[i]
                    drawCircle(
                        color = Color.White,
                        radius = 4f,
                        center = Offset((cx + r * cos(rad)).toFloat(), (cy + r * sin(rad)).toFloat())
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Explain stats
        val indices = listOf(
            Pair("Focus Mental 🧠", "90% • Superbe clarté cognitive"),
            Pair("Vibe Cosmique 🌌", "85% • Aura mystique de haut calibre"),
            Pair("Énergie Sociale 💬", "75% • Haute connectivité d'Aura"),
            Pair("Précision Tactique ♟️", "70% • Excellente mémoire"),
            Pair("Indicateur Zen 🍃", "65% • Niveau de stress moyen")
        )

        indices.forEach { (title, stat) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                Text(stat, color = Color(0xFFFFD700), fontSize = 12.sp)
            }
        }
    }
}

// --- FEATURE 8: PRIVATE ASTRAL VOICE SALON ---
@Composable
fun PremiumAstralSalonPage(viewModel: AuraViewModel, isPremium: Boolean) {
    val context = LocalContext.current
    var isHosting by remember { mutableStateOf(false) }

    ScrollableMenuLayout {
        Text(
            "Le Salon Privé Astral permet de diffuser sa voix dans un encodage stéréophonique spatialisé résistant aux ondes parasites avec un peak-mètre microphonique réactif.",
            fontSize = 12.sp,
            color = Color.LightGray
        )

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .size(160.dp)
                .align(Alignment.CenterHorizontally)
                .background(Color(0xFF1E1B4B), CircleShape)
                .border(3.dp, if (isHosting) Color(0xFFFF007F) else Color.Gray, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Mic,
                contentDescription = null,
                tint = if (isHosting) Color(0xFFFF007F) else Color.White,
                modifier = Modifier.size(64.dp)
            )

            if (isHosting) {
                // Outer glowing halo
                val infiniteTransition = rememberInfiniteTransition(label = "RadarBeacon")
                val scale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.35f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "BeaconHalo"
                )
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .rotate(degrees = scale * 100f)
                        .border(1.5.dp, Color(0xFFFF007F).copy(alpha = 1f - (scale - 1f) / 0.35f), CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (!isPremium) {
                    Toast.makeText(context, "L'hébergement de Salons Privés Vocaux est réservé aux VIPs !", Toast.LENGTH_SHORT).show()
                } else {
                    isHosting = !isHosting
                    viewModel.triggerBeep(if (isHosting) 1 else 2)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF007F))
        ) {
            Text(
                text = if (isHosting) "Fermer le Salon Astral Privé 🎙️" else "Créer un Salon Astral Privé (Stéréo HD)",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        if (isHosting) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("SALON EN DIRECT ✨", fontWeight = FontWeight.Bold, color = Color(0xFFFF007F), fontSize = 11.sp)
                    Text("Canal Cosmique Securisé", fontSize = 12.sp, color = Color.White)
                }
                Text("Vocal actif...", color = Color.Green, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- FEATURE 9: EMOTIONAL STATUS HALO ---
@Composable
fun PremiumEmotionalHaloPage(viewModel: AuraViewModel, isPremium: Boolean) {
    val context = LocalContext.current
    val halos = listOf("Hyper-Création 💡", "Méditation Interstellaire 🧘", "En mode Combat 🔥", "Concentration Diamant 💎", "Paix d'Émeraude 🌿")

    ScrollableMenuLayout {
        Text(
            "Configurez une aura lumineuse scintillante correspondant à votre état émotionnel actuel, qui diffuse de petits éclats circulaires sur l'en-tête de tous les onglets.",
            fontSize = 12.sp,
            color = Color.LightGray
        )

        Spacer(modifier = Modifier.height(16.dp))

        halos.forEach { halo ->
            val isSelected = viewModel.activeMoodHalo == halo
            Card(
                onClick = {
                    if (!isPremium) {
                        Toast.makeText(context, "Exige un statut PRO/Premium !", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.setMoodHaloStatus(if (isSelected) null else halo)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Color(0xFF1E3A8A) else Color(0xFF111827)
                ),
                border = BorderStroke(1.2.dp, if (isSelected) Color(0xFF3B82F6) else Color.Transparent)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(halo, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                    Icon(
                        imageVector = if (isSelected) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (isSelected) Color(0xFF3B82F6) else Color.Gray
                    )
                }
            }
        }
    }
}

// --- FEATURE 10: COSMIC CHOOSE-YOUR-OWN-ADVENTURE SPIRITUAL QUESTS ---
@Composable
fun PremiumStoryQuestPage(viewModel: AuraViewModel, isPremium: Boolean) {
    val context = LocalContext.current

    ScrollableMenuLayout {
        Text(
            "Le Générateur IA d'aventures spirituelles propose un scénario d'auto-exploration textuelle thérapeutique. Cumulez des points d'éveil cosmique pour forger d'autres pièces.",
            fontSize = 12.sp,
            color = Color.LightGray
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
            border = BorderStroke(1.dp, Color(0xFF818CF8))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "AURA ADVENTURE IA • ÉTAPE ${viewModel.premiumStoryStep}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Color(0xFFFFD700)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Render story segment text based on storyStep
                val storyText = when (viewModel.premiumStoryStep) {
                    1 -> "Vous vous réveillez au centre d'une clairière bioluminescente céleste. Trois portails stellaires d'énergie étincelante scintillent devant vos pas."
                    2 -> "Entré dans le portail cosmique, vous flottez vers une nébuleuse d'esprits anciens. Ces derniers vous proposent le grimoire de la sagesse éternelle."
                    3 -> "Dans la grotte d'émeraude, vous découvrez l'Arbre de Vie scintillant. Ses feuilles murmurent d'incroyables secrets de programmation émotionnelle."
                    else -> "Vous atteignez l'unification spirituelle d'Aura. Vos variations d'énergie sont stabilisées !"
                }

                Text(storyText, color = Color.White, fontSize = 14.sp, lineHeight = 20.sp)

                Spacer(modifier = Modifier.height(16.dp))

                Text("Score d'Émerveillement : ${viewModel.premiumStoryPoints} points", fontSize = 11.sp, color = Color.LightGray)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (viewModel.premiumStoryStep <= 3) {
            Text("Faites votre choix d'alignement spirituel :", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
            Spacer(modifier = Modifier.height(10.dp))

            // Action inputs
            when (viewModel.premiumStoryStep) {
                1 -> {
                    Button(
                        onClick = { viewModel.generatePremiumQuestChoice(2) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        border = BorderStroke(1.dp, Color.LightGray)
                    ) {
                        Text("Traverser le Portail de Saphir 🌌", color = Color.White)
                    }
                    Button(
                        onClick = { viewModel.generatePremiumQuestChoice(3) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        border = BorderStroke(1.dp, Color.LightGray)
                    ) {
                        Text("Sonder la Grotte d'Émeraude 🌿", color = Color.White)
                    }
                }
                2 -> {
                    Button(
                        onClick = { viewModel.generatePremiumQuestChoice(4) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        border = BorderStroke(1.dp, Color.LightGray)
                    ) {
                        Text("S'emparer du Grimoire du Code Éternel 📚", color = Color.White)
                    }
                    Button(
                        onClick = { viewModel.generatePremiumQuestChoice(4) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        border = BorderStroke(1.dp, Color.LightGray)
                    ) {
                        Text("Remercier poliment et continuer sa randonnée Méditative 🚶", color = Color.White)
                    }
                }
                3 -> {
                    Button(
                        onClick = { viewModel.generatePremiumQuestChoice(4) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        border = BorderStroke(1.dp, Color.LightGray)
                    ) {
                        Text("Ramasser une pomme cosmique d'Éveil 🍎", color = Color.White)
                    }
                }
            }
        } else {
            Button(
                onClick = { viewModel.resetPremiumQuest() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
            ) {
                Text("Recommencer l'Aventure Spirituelle 🗺️", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
