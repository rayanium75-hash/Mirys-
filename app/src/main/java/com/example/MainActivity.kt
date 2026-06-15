package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FeedSocialScreen
import com.example.ui.screens.GamesScreen
import com.example.ui.screens.TalkScreen
import com.example.ui.screens.AgendaScreen
import com.example.ui.screens.ShopScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AuraViewModel
import com.example.ui.components.BrandLogo
import com.example.ui.components.UserProfileSettingsDialog
import com.example.ui.components.PremiumHubDialog
import com.example.ui.components.RenderPresetAvatar
import com.example.ui.components.ProfileVfxWrapper
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.geometry.Offset
import androidx.compose.runtime.saveable.rememberSaveable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val auraViewModel: AuraViewModel = viewModel()
            val isSystemDark = isSystemInDarkTheme()
            val isDark = when (auraViewModel.appTheme) {
                "dark" -> true
                "light" -> false
                else -> isSystemDark
            }
            MyApplicationTheme(darkTheme = isDark) {
                MainAppContainer(viewModel = auraViewModel)
            }
        }
    }
}

@Composable
fun MainAppContainer(
    viewModel: AuraViewModel,
    modifier: Modifier = Modifier
) {
    var showSplash by rememberSaveable { mutableStateOf(true) }

    if (showSplash) {
        SplashScreenComponent(onFinish = { showSplash = false })
    } else if (!viewModel.isUserLoggedIn) {
        AuthScreen(viewModel = viewModel)
    } else {
        // Dialog for clear confirmation
        var showResetDialog by remember { mutableStateOf(false) }
        var showProfileDialog by remember { mutableStateOf(false) }
        var showGamesOverlay by remember { mutableStateOf(false) }
        var showPremiumHubDialog by remember { mutableStateOf(false) }

        Scaffold(
            modifier = modifier
                .fillMaxSize()
                .testTag("main_app_scaffold"),
            topBar = {
                CustomTopBar(
                    viewModel = viewModel,
                    onResetClick = { showResetDialog = true },
                    onProfileClick = { viewModel.selectTab("profile") },
                    onGamesClick = { showGamesOverlay = true },
                    onPremiumHubClick = { showPremiumHubDialog = true }
                )
            },
            bottomBar = {
                CustomBottomBar(
                    currentTab = viewModel.currentTab,
                    onTabSelected = { viewModel.selectTab(it) }
                )
            },
            contentWindowInsets = WindowInsets.safeDrawing
        ) { innerPadding ->
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (viewModel.currentTab) {
                    "dashboard" -> DashboardScreen(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                    "feed" -> FeedSocialScreen(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                    "talk" -> TalkScreen(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                    "agenda" -> AgendaScreen(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                    "profile" -> ProfileScreen(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text("Réinitialiser les données ?") },
                text = { Text("Cette action est irréversible et supprimera TOUT votre historique de journal et toutes vos tâches.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.clearAllData()
                            showResetDialog = false
                        },
                        modifier = Modifier.testTag("confirm_reset_button")
                    ) {
                        Text("Confirmer", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text("Annuler")
                    }
                }
            )
        }

        if (showPremiumHubDialog) {
            PremiumHubDialog(
                viewModel = viewModel,
                onDismiss = { showPremiumHubDialog = false }
            )
        }

        if (showProfileDialog) {
            UserProfileSettingsDialog(
                viewModel = viewModel,
                onDismissRequest = { showProfileDialog = false }
            )
        }

        if (showGamesOverlay) {
            Dialog(
                onDismissRequest = { showGamesOverlay = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        GamesScreen(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                        IconButton(
                            onClick = { showGamesOverlay = false },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                                .statusBarsPadding()
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                .testTag("close_games_overlay")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Fermer les jeux",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SplashScreenComponent(onFinish: () -> Unit) {
    val scale = remember { Animatable(0.2f) }
    val alpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val textOffset = remember { Animatable(40f) }
    val authorAlpha = remember { Animatable(0f) }
    
    val infiniteTransition = rememberInfiniteTransition(label = "VFX")
    
    // Shimmering color animation for the title and radiant glows
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlow"
    )

    // Drift offset for dust particles
    val particleDrift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particleDrift"
    )

    // Hologram Scan sweep animation
    val scanLineY by infiniteTransition.animateFloat(
        initialValue = -0.1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanLine"
    )

    // Shooting star animation progress
    val shootingStarProgress by infiniteTransition.animateFloat(
        initialValue = -0.3f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shootingStar"
    )

    // Core progression over 5 seconds (5000ms)
    val systemLoadProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Core loading speed
        launch {
            systemLoadProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(5000, easing = LinearEasing)
            )
        }

        // Logo bounce animation
        launch {
            scale.animateTo(
                targetValue = 1.15f,
                animationSpec = tween(1100, easing = FastOutSlowInEasing)
            )
            scale.animateTo(
                targetValue = 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        
        launch {
            alpha.animateTo(1f, animationSpec = tween(1000))
        }

        // Title text animation after slight delay
        launch {
            delay(400)
            textAlpha.animateTo(1f, animationSpec = tween(1100))
        }
        launch {
            delay(400)
            textOffset.animateTo(0f, animationSpec = spring(stiffness = Spring.StiffnessLow))
        }

        // Author credits fade in elegantly after
        launch {
            delay(1500)
            authorAlpha.animateTo(1f, animationSpec = tween(1200))
        }

        // Splash screen is now requested to be exactly 5 seconds (5000ms)
        delay(5000)
        onFinish()
    }

    val loadPercent = (systemLoadProgress.value * 100).toInt().coerceIn(0, 100)
    val statusText = when {
        systemLoadProgress.value < 0.16f -> "INITIALISATION DE MIRYS CORE OS v3.0..."
        systemLoadProgress.value < 0.35f -> "CHARGEMENT DE LA MATRICE NEURALE & INTELLIGENCE..."
        systemLoadProgress.value < 0.55f -> "SYNCHRONISATION DES CLASSEMENTS ET DES QUIZ [OK]"
        systemLoadProgress.value < 0.75f -> "MIGRATION DE LA COOPÉRATIVE DE SOUS-RÉGION..."
        systemLoadProgress.value < 0.90f -> "COMMUNAUTÉ, BOUTIQUE ET PROFILS INITIALISÉS"
        else -> "MIRYS EST PRÊT ! CONNEXION AU PROTOCOLE DE TOURNOI..."
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF03070E)), // Ultra-dark elegant space background
        contentAlignment = Alignment.Center
    ) {
        // --- BACKGROUND VFX: Nebula Glowing Dust, Shooting Stars and Cyber-Grid ---
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Draw dynamic organic background nebula glows (Cyan & Deep Purple blend)
            val radialGlow1 = Brush.radialGradient(
                colors = listOf(Color(0x3B1AA3FF), Color(0x000A1626)),
                center = Offset(width * 0.3f, height * 0.3f),
                radius = width * (0.7f + 0.12f * pulseGlow)
            )
            val radialGlow2 = Brush.radialGradient(
                colors = listOf(Color(0x2E6B21A8), Color(0x00000000)), // Sci-fi Purple
                center = Offset(width * 0.75f, height * 0.65f),
                radius = width * (0.6f + 0.10f * (1.0f - pulseGlow))
            )
            val radialGlow3 = Brush.radialGradient(
                colors = listOf(Color(0x223DF5FF), Color(0x00000000)), // Cyan Core
                center = Offset(width * 0.5f, height * 0.45f),
                radius = width * (0.45f + 0.05f * pulseGlow)
            )
            drawRect(radialGlow1)
            drawRect(radialGlow2)
            drawRect(radialGlow3)

            // 2. 3D Cybernetic Perspective Grid lines at base
            val horizonY = height * 0.55f
            val gridColor = Color(0xFF13223A).copy(alpha = 0.3f * pulseGlow)
            val gridHighlightColor = Color(0xFF22D3EE).copy(alpha = 0.15f * pulseGlow)

            // Horizontal perspective lines
            for (i in 0..14) {
                val ratio = i / 14f
                val y = horizonY + (height - horizonY) * (ratio * ratio)
                drawLine(
                    color = if (i % 4 == 0) gridHighlightColor else gridColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = if (i % 4 == 0) 1.5f else 0.8f
                )
            }

            // Radial perspective lines from vanish point on horizon
            val vanishX = width * 0.5f
            val vanishY = horizonY
            val verticalLinesCount = 16
            for (i in 0..verticalLinesCount) {
                val ratio = i.toFloat() / verticalLinesCount.toFloat()
                val bottomX = width * (-0.4f + ratio * 1.8f)
                drawLine(
                    color = gridColor,
                    start = Offset(vanishX, vanishY),
                    end = Offset(bottomX, height),
                    strokeWidth = 0.9f
                )
            }

            // 3. Continuous ambient floating stellar star particles
            val baseStars = listOf(
                Offset(0.12f, 0.20f) to 3.5f,
                Offset(0.85f, 0.15f) to 5.0f,
                Offset(0.28f, 0.72f) to 4.0f,
                Offset(0.74f, 0.82f) to 4.5f,
                Offset(0.48f, 0.08f) to 6.0f,
                Offset(0.18f, 0.55f) to 3.0f,
                Offset(0.82f, 0.48f) to 5.5f,
                Offset(0.50f, 0.90f) to 4.0f,
                Offset(0.35f, 0.38f) to 3.2f,
                Offset(0.65f, 0.28f) to 4.2f,
                Offset(0.15f, 0.85f) to 2.8f,
                Offset(0.88f, 0.75f) to 3.6f
            )

            baseStars.forEach { (pos, sizeVal) ->
                val rawY = pos.y - particleDrift
                val finalY = if (rawY < 0f) rawY + 1.0f else rawY
                val screenPos = Offset(pos.x * width, finalY * height)
                val pulseAlpha = 0.3f + 0.7f * kotlin.math.sin(finalY * Math.PI.toFloat() * 2f).coerceIn(0f, 1f)

                drawCircle(
                    color = Color.White.copy(alpha = pulseAlpha),
                    radius = sizeVal,
                    center = screenPos
                )
                
                drawCircle(
                    color = Color(0xFF80E5FF).copy(alpha = pulseAlpha * 0.25f),
                    radius = sizeVal * 2.8f,
                    center = screenPos
                )
            }

            // 4. Diagonal Meteor Sweep
            if (shootingStarProgress in 0f..1f) {
                val sStart = Offset(width * 1.1f, height * -0.1f)
                val sEnd = Offset(width * -0.2f, height * 0.8f)
                val currentX = sStart.x + (sEnd.x - sStart.x) * shootingStarProgress
                val currentY = sStart.y + (sEnd.y - sStart.y) * shootingStarProgress

                val hyp = kotlin.math.hypot(sEnd.x - sStart.x, sEnd.y - sStart.y)
                val tLen = 140f
                val dx = (sEnd.x - sStart.x) / hyp * tLen
                val dy = (sEnd.y - sStart.y) / hyp * tLen

                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.Transparent, Color(0xFF22D3EE), Color.White),
                        start = Offset(currentX - dx, currentY - dy),
                        end = Offset(currentX, currentY)
                    ),
                    start = Offset(currentX - dx, currentY - dy),
                    end = Offset(currentX, currentY),
                    strokeWidth = 2.5f,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }
        }

        // --- FOREGROUND CONTENT ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .graphicsLayer {
                        this.scaleX = scale.value
                        this.scaleY = scale.value
                        this.alpha = alpha.value
                    }
            ) {
                // Aura ambient glowing base circle behind logo
                Box(
                    modifier = Modifier
                        .size(175.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF1AA3FF).copy(alpha = 0.40f * pulseGlow),
                                    Color(0xFF7C3AED).copy(alpha = 0.25f * (1.0f - pulseGlow)),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Box(modifier = Modifier.size(135.dp)) {
                    BrandLogo(size = 135.dp)

                    // Laser Scanning Hologram Line Over Logo
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val side = size.width
                        val glowLineY = side * scanLineY
                        if (glowLineY in 0f..side) {
                            // Bright horizontal laser gradient trace
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0xFF3DF5FF).copy(alpha = 0.35f),
                                        Color.White.copy(alpha = 0.75f),
                                        Color(0xFF3DF5FF).copy(alpha = 0.35f),
                                        Color.Transparent
                                    ),
                                    startY = glowLineY - 10f,
                                    endY = glowLineY + 10f
                                ),
                                topLeft = Offset(0f, glowLineY - 10f),
                                size = androidx.compose.ui.geometry.Size(side, 20f)
                            )
                            // Core scanning light beam
                            drawLine(
                                color = Color.White,
                                start = Offset(0f, glowLineY),
                                end = Offset(side, glowLineY),
                                strokeWidth = 2f
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(28.dp))
            
            // Styled animated Title with shimmering gradient
            Text(
                text = "Mirys",
                fontWeight = FontWeight.Black,
                fontSize = 46.sp,
                style = TextStyle(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFFFFFF),
                            Color(0xFF22D3EE),
                            Color(0xFF0284C7),
                            Color(0xFFC084FC),
                            Color(0xFFFFFFFF)
                        ),
                        tileMode = TileMode.Clamp
                    )
                ),
                letterSpacing = (-1.5).sp,
                modifier = Modifier
                    .graphicsLayer {
                        this.alpha = textAlpha.value
                        this.translationY = textOffset.value
                    }
            )

            // Monospace high-tech slogan
            Text(
                text = "FOCUS • STRATÉGIE • INTELLIGENCE",
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 9.5.sp,
                color = Color(0xFF67E8F9),
                letterSpacing = 2.5.sp,
                modifier = Modifier
                    .padding(top = 4.dp, bottom = 22.dp)
                    .graphicsLayer {
                        this.alpha = textAlpha.value * 0.85f
                        this.translationY = textOffset.value * 1.3f
                    }
            )
            
            // Elegant framed Author Card with dual-gradient glow
            Surface(
                color = Color(0xFF091424).copy(alpha = 0.82f),
                border = BorderStroke(
                    1.2.dp, 
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF1AA3FF).copy(alpha = 0.45f),
                            Color(0xFF9333EA).copy(alpha = 0.20f),
                            Color(0xFF1AA3FF).copy(alpha = 0.45f)
                        )
                    )
                ),
                shape = RoundedCornerShape(12.dp),
                tonalElevation = 12.dp,
                modifier = Modifier
                    .graphicsLayer {
                        this.alpha = authorAlpha.value
                        scaleX = 0.94f + (authorAlpha.value * 0.06f)
                        scaleY = 0.94f + (authorAlpha.value * 0.06f)
                    }
            ) {
                Text(
                    text = "BY ALANE MENTII • FOR RAYANIUM68",
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    color = Color(0xFF80E5FF),
                    letterSpacing = 1.2.sp,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 9.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(42.dp))
            
            // High-Tech Cyber Segments Loading Progress HUD
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .graphicsLayer {
                        this.alpha = textAlpha.value
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = statusText,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontSize = 8.sp,
                        color = Color(0xFF80E5FF).copy(alpha = 0.85f),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = "[ $loadPercent% ]",
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontSize = 9.5.sp,
                        color = Color(0xFF22D3EE),
                        fontWeight = FontWeight.Black
                    )
                }

                // Cyber loader track
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF070F19))
                        .border(1.dp, Color(0xFF1B2F49), RoundedCornerShape(4.dp))
                        .padding(2.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val barWidth = size.width
                        val barHeight = size.height
                        val progressWidth = barWidth * systemLoadProgress.value
                        
                        // Glowing horizontal gradient load-bar
                        drawRoundRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF0066FF), Color(0xFF22D3EE), Color(0xFFC084FC))
                            ),
                            size = androidx.compose.ui.geometry.Size(progressWidth, barHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
                        )
                    }
                }
            }
        }

        // Actionable Skip Button placed in top-right corner above other animations
        TextButton(
            onClick = onFinish,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 16.dp, end = 16.dp)
                .testTag("skip_splash_button")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Passer",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF80E5FF).copy(alpha = 0.8f)
                )
                Icon(
                    imageVector = Icons.Outlined.ArrowForwardIos,
                    contentDescription = "Passer",
                    tint = Color(0xFF80E5FF).copy(alpha = 0.8f),
                    modifier = Modifier.size(11.dp)
                )
            }
        }
    }
}

@Composable
fun CustomTopBar(
    viewModel: AuraViewModel,
    onResetClick: () -> Unit,
    onProfileClick: () -> Unit,
    onGamesClick: () -> Unit,
    onPremiumHubClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.clickable { onProfileClick() }
            ) {
                ProfileVfxWrapper(
                    badgeName = viewModel.equippedBadge,
                    modifier = Modifier.size(50.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                            .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        RenderPresetAvatar(
                            preset = viewModel.profilePhotoPreset,
                            filter = viewModel.profileFilter,
                            brightness = viewModel.profileBrightness,
                            contrast = viewModel.profileContrast,
                            zoom = viewModel.profileZoom,
                            cropX = viewModel.profileCropX,
                            cropY = viewModel.profileCropY,
                            username = viewModel.username,
                            modifier = Modifier.fillMaxSize(),
                            customUri = viewModel.customProfilePhotoUri
                        )
                    }
                }
                
                Column {
                    Text(
                        text = "Mirys",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = viewModel.username,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.LightGray
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Premium Hub Gold Crown Button
                val isTrial = viewModel.isFreeTrialActive
                val premiumGlowColor = if (isTrial) Color(0xFF00FFCC) else Color(0xFFFFD700)
                IconButton(
                    onClick = onPremiumHubClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isTrial) Color(0xFF022C22) else Color(0xFF2E1A47))
                        .border(
                            width = if (isTrial) 2.dp else 1.5.dp, 
                            brush = if (isTrial) Brush.sweepGradient(listOf(Color(0xFF00FFCC), Color(0xFF8B5CF6), Color(0xFF00FFCC))) else Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA000))), 
                            shape = CircleShape
                        )
                        .testTag("premium_hub_top_bar_btn")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.WorkspacePremium,
                        contentDescription = "Club Premium Multivers",
                        tint = premiumGlowColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Cyberpunk styled High-energy Glowing Games icon button to play!
                IconButton(
                    onClick = onGamesClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0F1B2F))
                        .border(
                            width = 1.5.dp, 
                            brush = Brush.linearGradient(listOf(Color(0xFF00E676), Color(0xFF00FFCC))), 
                            shape = CircleShape
                        )
                        .testTag("games_top_bar_btn")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.SportsEsports,
                        contentDescription = "Ouvrir les Jeux",
                        tint = Color(0xFF00FFCC),
                        modifier = Modifier.size(22.dp)
                    )
                }

                if (viewModel.subscriptionTier != "Gratuit" || viewModel.isFreeTrialActive) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (viewModel.isFreeTrialActive) Color(0xFF00FFCC).copy(alpha = 0.2f)
                                else Color(0xFFFFD700).copy(alpha = 0.2f)
                            )
                            .border(
                                width = 1.dp, 
                                color = if (viewModel.isFreeTrialActive) Color(0xFF00FFCC) else Color(0xFFFFD700), 
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                            .clickable { onProfileClick() }
                    ) {
                        Text(
                            text = if (viewModel.isFreeTrialActive) "ESSAI ⚡" else if (viewModel.subscriptionTier.contains("Pro")) "PRO ✨" else "PREMIUM 👑",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = if (viewModel.isFreeTrialActive) Color(0xFF00FFCC) else Color(0xFFFFD700)
                        )
                    }
                }

                IconButton(
                    onClick = onProfileClick,
                    modifier = Modifier.testTag("open_profile_topbar")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccountCircle,
                        contentDescription = "Ouvrir votre profil",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = onResetClick,
                    modifier = Modifier.testTag("reset_data_topbar")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteForever,
                        contentDescription = "Effacer toutes les données",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun CustomBottomBar(
    currentTab: String,
    onTabSelected: (String) -> Unit
) {
    NavigationBar(
        tonalElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bottom_nav_bar")
    ) {
        NavigationBarItem(
            selected = currentTab == "dashboard",
            onClick = { onTabSelected("dashboard") },
            label = { Text("Mirys IA", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = "Onglet Mirys IA"
                )
            },
            modifier = Modifier.testTag("nav_dashboard")
        )

        NavigationBarItem(
            selected = currentTab == "feed",
            onClick = { onTabSelected("feed") },
            label = { Text("Fil Social", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Feed,
                    contentDescription = "Onglet Fil Social"
                )
            },
            modifier = Modifier.testTag("nav_feed")
        )

        NavigationBarItem(
            selected = currentTab == "talk",
            onClick = { onTabSelected("talk") },
            label = { Text("Talk", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Forum,
                    contentDescription = "Onglet Talk"
                )
            },
            modifier = Modifier.testTag("nav_talk")
        )

        NavigationBarItem(
            selected = currentTab == "agenda",
            onClick = { onTabSelected("agenda") },
            label = { Text("Agenda", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.EventNote,
                    contentDescription = "Onglet Agenda"
                )
            },
            modifier = Modifier.testTag("nav_agenda")
        )

        NavigationBarItem(
            selected = currentTab == "profile",
            onClick = { onTabSelected("profile") },
            label = { Text("Profil", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = "Onglet Profil"
                )
            },
            modifier = Modifier.testTag("nav_profile")
        )
    }
}
