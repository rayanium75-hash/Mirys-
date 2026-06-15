package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.ui.viewmodel.AuraViewModel
import kotlinx.coroutines.delay
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.VideoView
import android.widget.MediaController

@Composable
fun StoriesSection(viewModel: AuraViewModel) {
    var activeStoryIndex by remember { mutableStateOf<Int?>(null) }
    var showCreateStoryDialog by remember { mutableStateOf(false) }

    val stories = viewModel.storiesList
    
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1B2F)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("stories_section_card")
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Stories Mirys ✨",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }
                Text(
                    text = "Glisser pour voir ➡️",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Item creator
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.clickable { showCreateStoryDialog = true }
                    ) {
                        Box(
                            modifier = Modifier.size(72.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .border(BorderStroke(1.5.dp, Color(0xFF00E5FF).copy(alpha = 0.5f)), CircleShape)
                                    .padding(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(Color(0xFF1E293B)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Moi", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .align(Alignment.BottomEnd)
                                    .background(Color(0xFF00E5FF), CircleShape)
                                    .border(1.5.dp, Color(0xFF0F1B2F), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Ajouter Story",
                                    tint = Color.Black,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Text(
                            text = "Ma Story",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.LightGray
                        )
                    }
                }

                // Stories list
                itemsIndexed(stories) { idx, story ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.clickable { activeStoryIndex = idx }
                    ) {
                        SparklingStoryRing(modifier = Modifier.size(72.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(Color(0xFF2D3748)),
                                contentAlignment = Alignment.Center
                            ) {
                                val initials = story.authorHandle.take(2).uppercase()
                                if (story.isVideo) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Vidéo", tint = Color(0xFF00E5FF).copy(alpha = 0.6f), modifier = Modifier.size(32.dp))
                                        Text(initials, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                                    }
                                } else {
                                    AsyncImage(
                                        model = story.mediaUrl,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.15f))
                                    )
                                }
                            }
                        }
                        
                        Text(
                            text = if (story.authorHandle == "mon_compte") "Mon histoire" else story.authorName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }

    // Story viewer dialog overlay
    activeStoryIndex?.let { startingIndex ->
        StoryViewerOverlay(
            stories = stories,
            initialIndex = startingIndex,
            onDismiss = { activeStoryIndex = null }
        )
    }

    // Story posting dialog
    if (showCreateStoryDialog) {
        CreateStoryDialogue(
            viewModel = viewModel,
            onDismiss = { showCreateStoryDialog = false }
        )
    }
}

@Composable
fun SparklingStoryRing(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sparkleRotation")
    
    // Rotating gradient for "scintillant bleu ciel et autres choses" (pulsing sky blue/cyan/neon purple aura)
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )
    
    // Pulse scale for outline glow
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Cyan, Sky Blue, Magenta gradient
    val brush = Brush.sweepGradient(
        colors = listOf(
            Color(0xFF00E5FF), // Sky Blue
            Color(0xFF00B0FF), // Cyan
            Color(0xFFE040FB), // Fuchsia / Magenta aura
            Color(0xFF00E5FF)  // Finish loop
        )
    )

    Box(
        modifier = modifier.scale(pulseScale),
        contentAlignment = Alignment.Center
    ) {
        // Sparkling rotating ring border
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(3.dp)
                .rotate(angle)
                .border(
                    BorderStroke(2.5.dp, brush),
                    CircleShape
                )
        )
        
        // Canvas drawing celestial shining sparkles inside standard frame
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.width / 2f
            val anglesRad = listOf(
                Math.toRadians(angle.toDouble()),
                Math.toRadians((angle + 90f).toDouble()),
                Math.toRadians((angle + 180f).toDouble()),
                Math.toRadians((angle + 270f).toDouble())
            )
            
            anglesRad.forEach { rad ->
                val x = center.x + (radius - 1.dp.toPx()) * Math.cos(rad).toFloat()
                val y = center.y + (radius - 1.dp.toPx()) * Math.sin(rad).toFloat()
                
                // Pure stars visual glow
                drawCircle(
                    color = Color.White,
                    radius = 3.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(x, y)
                )
                drawCircle(
                    color = Color(0xFF00E5FF).copy(alpha = 0.6f),
                    radius = 6.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(x, y),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
                )
            }
        }
        
        // Circular Avatar container
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(7.dp)
        ) {
            content()
        }
    }
}

@Composable
fun StoryViewerOverlay(
    stories: List<com.example.ui.viewmodel.SocialStory>,
    initialIndex: Int,
    onDismiss: () -> Unit
) {
    var currentIndex by remember { mutableStateOf(initialIndex) }
    val currentStory = stories.getOrNull(currentIndex) ?: return
    
    var progress by remember { mutableStateOf(0f) }
    
    LaunchedEffect(currentIndex) {
        progress = 0f
        for (step in 1..70) {
            delay(100)
            progress = step / 70f
        }
        if (currentIndex < stories.size - 1) {
            currentIndex += 1
        } else {
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Underlay content
            if (currentStory.isVideo) {
                AndroidView(
                    factory = { ctx ->
                        VideoView(ctx).apply {
                            setVideoPath(currentStory.mediaUrl)
                            val mediaController = MediaController(ctx)
                            mediaController.setAnchorView(this)
                            setMediaController(mediaController)
                            setOnPreparedListener { mp ->
                                mp.isLooping = true
                                start()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                )
            } else {
                AsyncImage(
                    model = currentStory.mediaUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.85f)
                        .align(Alignment.Center),
                    contentScale = ContentScale.Fit
                )
            }

            // Top segment info bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Segment lines progress indicators
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    stories.forEachIndexed { idx, _ ->
                        val barProgress = when {
                            idx < currentIndex -> 1f
                            idx == currentIndex -> progress
                            else -> 0f
                        }
                        LinearProgressIndicator(
                            progress = { barProgress },
                            color = Color(0xFF00E5FF),
                            trackColor = Color.White.copy(alpha = 0.25f),
                            modifier = Modifier
                                .weight(1f)
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp))
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00E5FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentStory.authorHandle.take(2).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                fontSize = 12.sp
                            )
                        }
                        Column {
                            Text(
                                text = currentStory.authorName,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 13.sp
                            )
                            Text(
                                text = currentStory.timestamp,
                                color = Color.LightGray,
                                fontSize = 10.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Fermer", tint = Color.White)
                    }
                }
            }

            // Hot Zone Click Areas (Tap Left/Right to browse)
            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable {
                            if (currentIndex > 0) {
                                currentIndex -= 1
                            } else {
                                onDismiss()
                            }
                        }
                )
                Box(
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxHeight()
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable {
                            if (currentIndex < stories.size - 1) {
                                currentIndex += 1
                            } else {
                                onDismiss()
                            }
                        }
                )
            }

            // Caption Section
            if (currentStory.caption.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.82f))
                            )
                        )
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp, top = 24.dp, start = 16.dp, end = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentStory.caption,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateStoryDialogue(
    viewModel: AuraViewModel,
    onDismiss: () -> Unit
) {
    var storyCaption by remember { mutableStateOf("") }
    var selectedMediaUrl by remember { mutableStateOf("https://images.unsplash.com/photo-1549880181-56a44cf8a4a1") }
    var isVideoMedia by remember { mutableStateOf(false) }

    val demoPresets = listOf(
        Triple("Collines Vertes 🏞️", false, "https://images.unsplash.com/photo-1506744038136-46273834b3fb"),
        Triple("Nuit d'Or 🌌", false, "https://images.unsplash.com/photo-1419242902214-272b3f66ee7a"),
        Triple("Waterfall 🌿 (Vidéo)", true, "https://assets.mixkit.co/videos/preview/mixkit-small-waterfall-in-a-forest-creek-531-large.mp4"),
        Triple("Space Abstract 🪐", false, "https://images.unsplash.com/photo-1464802686167-b939a6910659"),
        Triple("Mer d'Azur 🌊 (Vidéo)", true, "https://assets.mixkit.co/videos/preview/mixkit-glorious-sun-rising-over-the-ocean-43183-large.mp4")
    )

    val pickStoryPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedMediaUrl = uri.toString()
            isVideoMedia = false
        }
    }

    val pickStoryVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedMediaUrl = uri.toString()
            isVideoMedia = true
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0A111E)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(24.dp))
                    Text(
                        text = "Créer une Story Mirys",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black)
                        .border(1.5.dp, Color(0xFF00E5FF).copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isVideoMedia) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Default.PlayCircle, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(36.dp))
                            Text("Vidéo active : ${selectedMediaUrl.take(20)}...", color = Color.White, fontSize = 10.sp)
                        }
                    } else {
                        AsyncImage(
                            model = selectedMediaUrl,
                            contentDescription = "Prévisualisation",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                OutlinedTextField(
                    value = storyCaption,
                    onValueChange = { storyCaption = it },
                    label = { Text("Légende / Texte de la story", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.LightGray,
                        focusedBorderColor = Color(0xFF00E5FF)
                    )
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Importer un fichier de la galerie 📱 :", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { pickStoryPhotoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Sélectionner Photo", fontSize = 10.sp, color = Color.White)
                        }
                        Button(
                            onClick = { pickStoryVideoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Sélectionner Vidéo", fontSize = 10.sp, color = Color.White)
                        }
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Ou utiliser un modèle d'ambiance 🌟 :", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(demoPresets.size) { index ->
                            val (label, isVid, url) = demoPresets[index]
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (selectedMediaUrl == url) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.05f)
                                    )
                                    .clickable {
                                        selectedMediaUrl = url
                                        isVideoMedia = isVid
                                    }
                                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedMediaUrl == url) Color.Black else Color.White
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Annuler", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            viewModel.addStory(isVideoMedia, selectedMediaUrl, storyCaption)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF), contentColor = Color.Black),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Publier Story 🚀", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}
