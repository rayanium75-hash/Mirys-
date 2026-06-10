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
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FeedSocialScreen
import com.example.ui.screens.GamesScreen
import com.example.ui.screens.AgendaScreen
import com.example.ui.screens.ShopScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AuraViewModel
import com.example.ui.components.BrandLogo
import com.example.ui.components.UserProfileSettingsDialog
import com.example.ui.components.RenderPresetAvatar
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
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
    var showSplash by remember { mutableStateOf(true) }

    if (showSplash) {
        SplashScreenComponent(onFinish = { showSplash = false })
    } else {
        // Dialog for clear confirmation
        var showResetDialog by remember { mutableStateOf(false) }
        var showProfileDialog by remember { mutableStateOf(false) }

        Scaffold(
            modifier = modifier
                .fillMaxSize()
                .testTag("main_app_scaffold"),
            topBar = {
                CustomTopBar(
                    viewModel = viewModel,
                    onResetClick = { showResetDialog = true },
                    onProfileClick = { showProfileDialog = true }
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
                    "games" -> GamesScreen(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                    "agenda" -> AgendaScreen(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                    "shop" -> ShopScreen(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize(),
                        onProfileClick = { showProfileDialog = true }
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

        if (showProfileDialog) {
            UserProfileSettingsDialog(
                viewModel = viewModel,
                onDismissRequest = { showProfileDialog = false }
            )
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

    LaunchedEffect(Unit) {
        // Logo bounce animation
        launch {
            scale.animateTo(
                targetValue = 1.15f,
                animationSpec = tween(1000, easing = FastOutSlowInEasing)
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
            alpha.animateTo(1f, animationSpec = tween(900))
        }

        // Title text animation after slight delay
        launch {
            delay(300)
            textAlpha.animateTo(1f, animationSpec = tween(900))
        }
        launch {
            delay(300)
            textOffset.animateTo(0f, animationSpec = spring(stiffness = Spring.StiffnessLow))
        }

        // Author credits fade in elegantly after
        launch {
            delay(1000)
            authorAlpha.animateTo(1f, animationSpec = tween(1000))
        }

        // Splash screen is now requested to be exactly 4 seconds (4000ms)
        delay(4000)
        onFinish()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF03070E)), // Ultra-dark elegant space background
        contentAlignment = Alignment.Center
    ) {
        // --- BACKGROUND VFX: Nebula Glowing Dust and Stars ---
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Draw dynamic organic background nebula glows
            val radialGlow1 = Brush.radialGradient(
                colors = listOf(Color(0x331AA3FF), Color(0x000A1626)),
                center = Offset(width * 0.4f, height * 0.35f),
                radius = width * (0.6f + 0.1f * pulseGlow)
            )
            val radialGlow2 = Brush.radialGradient(
                colors = listOf(Color(0x223DF5FF), Color(0x00060B13)),
                center = Offset(width * 0.7f, height * 0.65f),
                radius = width * (0.5f + 0.08f * (1.0f - pulseGlow))
            )
            drawRect(radialGlow1)
            drawRect(radialGlow2)

            // 2. Continuous ambient floating stellar star particles
            val baseStars = listOf(
                Offset(0.12f, 0.20f) to 3.5f,
                Offset(0.85f, 0.15f) to 5f,
                Offset(0.28f, 0.72f) to 4f,
                Offset(0.74f, 0.82f) to 4.5f,
                Offset(0.48f, 0.08f) to 6f,
                Offset(0.18f, 0.55f) to 3f,
                Offset(0.82f, 0.48f) to 5.5f,
                Offset(0.50f, 0.90f) to 4f,
                Offset(0.35f, 0.38f) to 3.2f,
                Offset(0.65f, 0.28f) to 4.2f
            )

            baseStars.forEach { (pos, sizeVal) ->
                // Drift calculations with wrapping so particles slowly rise and reset
                val rawY = pos.y - particleDrift
                val finalY = if (rawY < 0f) rawY + 1.0f else rawY
                val screenPos = Offset(pos.x * width, finalY * height)
                
                // Pulsate transparency based on drift position
                val pulseAlpha = 0.3f + 0.7f * kotlin.math.sin(finalY * Math.PI.toFloat() * 2f).coerceIn(0f, 1f)

                drawCircle(
                    color = Color.White.copy(alpha = pulseAlpha),
                    radius = sizeVal,
                    center = screenPos
                )
                
                // Star glow aura
                drawCircle(
                    color = Color(0xFF80E5FF).copy(alpha = pulseAlpha * 0.25f),
                    radius = sizeVal * 2.8f,
                    center = screenPos
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
                        .size(160.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF1AA3FF).copy(alpha = 0.35f * pulseGlow),
                                    Color.Transparent
                                )
                            )
                        )
                )

                BrandLogo(size = 135.dp)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Styled animated Title with shimmering gradient
            Text(
                text = "Mirys",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 44.sp,
                style = TextStyle(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFFFFFF),
                            Color(0xFF3DF5FF),
                            Color(0xFF1AA3FF),
                            Color(0xFF0073E6)
                        ),
                        tileMode = TileMode.Clamp
                    )
                ),
                letterSpacing = (-1).sp,
                modifier = Modifier
                    .graphicsLayer {
                        this.alpha = textAlpha.value
                        this.translationY = textOffset.value
                    }
            )
            
            Spacer(modifier = Modifier.height(18.dp))
            
            // Elegant framed Author Card with dual-gradient glow
            Surface(
                color = Color(0xFF091424).copy(alpha = 0.75f),
                border = BorderStroke(
                    1.2.dp, 
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF1AA3FF).copy(alpha = 0.35f),
                            Color(0xFF3DF5FF).copy(alpha = 0.12f),
                            Color(0xFF1AA3FF).copy(alpha = 0.35f)
                        )
                    )
                ),
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 10.dp,
                modifier = Modifier
                    .graphicsLayer {
                        this.alpha = authorAlpha.value
                        scaleX = 0.9f + (authorAlpha.value * 0.1f)
                        scaleY = 0.9f + (authorAlpha.value * 0.1f)
                    }
            ) {
                Text(
                    text = "From rayanium68, by Alane Mentii",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF80E5FF),
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 11.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Elegant micro progress bar or ring
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        this.alpha = textAlpha.value
                    }
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color(0xFF3DF5FF),
                    strokeWidth = 2.5.dp
                )
            }
        }
    }
}

@Composable
fun CustomTopBar(
    viewModel: AuraViewModel,
    onResetClick: () -> Unit,
    onProfileClick: () -> Unit
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
                if (viewModel.subscriptionTier != "Gratuit") {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFFD700).copy(alpha = 0.2f))
                            .border(1.dp, Color(0xFFFFD700), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                            .clickable { onProfileClick() }
                    ) {
                        Text(
                            text = if (viewModel.subscriptionTier.contains("Pro")) "PRO ✨" else "PREMIUM 👑",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFFD700)
                        )
                    }
                }

                IconButton(
                    onClick = onProfileClick,
                    modifier = Modifier.testTag("open_profile_topbar")
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Ouvrir votre profil",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = onResetClick,
                    modifier = Modifier.testTag("reset_data_topbar")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
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
                    imageVector = Icons.Default.AutoAwesome,
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
                    imageVector = Icons.Default.Forum,
                    contentDescription = "Onglet Fil Social"
                )
            },
            modifier = Modifier.testTag("nav_feed")
        )

        NavigationBarItem(
            selected = currentTab == "games",
            onClick = { onTabSelected("games") },
            label = { Text("Jeux", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
            icon = {
                Icon(
                    imageVector = Icons.Default.SportsEsports,
                    contentDescription = "Onglet Jeux"
                )
            },
            modifier = Modifier.testTag("nav_games")
        )

        NavigationBarItem(
            selected = currentTab == "agenda",
            onClick = { onTabSelected("agenda") },
            label = { Text("Agenda", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
            icon = {
                Icon(
                    imageVector = Icons.Default.EventNote,
                    contentDescription = "Onglet Agenda"
                )
            },
            modifier = Modifier.testTag("nav_agenda")
        )

        NavigationBarItem(
            selected = currentTab == "shop",
            onClick = { onTabSelected("shop") },
            label = { Text("Boutique", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Store,
                    contentDescription = "Onglet Boutique"
                )
            },
            modifier = Modifier.testTag("nav_shop")
        )
    }
}
