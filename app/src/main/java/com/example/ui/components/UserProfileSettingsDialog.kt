package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.AuraViewModel
import com.example.ui.viewmodel.SubRegion
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import coil.compose.AsyncImage
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileSettingsDialog(
    viewModel: AuraViewModel,
    onDismissRequest: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var activeTab by remember { mutableStateOf("profile") } // "profile", "settings", "premium"
    
    // Local copy of profile data (can b saved on confirmation click)
    var localUsername by remember { mutableStateOf(viewModel.username) }
    var localHandle by remember { mutableStateOf(viewModel.userHandle) }

    // Preset avatars config
    val presets = listOf(
        "Default" to "Défaut Silencieux",
        "Nebula" to "Astéroïde Cosmique",
        "Golden Hero" to "Souverain Doré",
        "Cyber Punk" to "Hacker Synthwave",
        "Cosmic" to "Nébuleuse Émeraude"
    )

    // Filters config
    val filters = listOf(
        "Normal" to "Normal",
        "Noir & Blanc" to "N&B",
        "Vintage" to "Vintage ✨",
        "Bleu Cyber" to "Cyber 🔵",
        "Neon Rose" to "Néon 🟣"
    )

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF03070E)) // Consistent ultra dark space atmosphere
            ) {
                // Header row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.testTag("close_profile_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "Fermer le profil",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = getTranslatedText("profile_title", viewModel.appLanguage),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = (-0.5).sp
                    )

                    Button(
                        onClick = {
                            viewModel.username = localUsername
                            viewModel.userHandle = localHandle
                            viewModel.triggerBeep(1)
                            onDismissRequest()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("save_profile_button")
                    ) {
                        Text("Enregistrer", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                // Navigation Tabs (M3 style)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF08101C))
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val tabItems = listOf(
                        "profile" to "Profil",
                        "settings" to "Paramètres",
                        "premium" to "Abonnement ✨"
                    )
                    tabItems.forEach { (id, label) ->
                        val isSelected = activeTab == id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    else Color.Transparent
                                )
                                .clickable {
                                    activeTab = id
                                    viewModel.triggerBeep(3)
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .testTag("profile_tab_$id"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        }
                    }
                }

                // Scrollable Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    AnimatedContent(
                        targetState = activeTab,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(2200)) togetherWith fadeOut(animationSpec = tween(2200))
                        },
                        label = "ProfileTabTransition"
                    ) { targetTab ->
                        when (targetTab) {
                            "profile" -> ProfileEditorSection(
                                viewModel = viewModel,
                                username = localUsername,
                                onUsernameChange = { localUsername = it },
                                handle = localHandle,
                                onHandleChange = { localHandle = it },
                                presets = presets,
                                filters = filters
                            )
                            "settings" -> SettingsSection(viewModel = viewModel)
                            "premium" -> SubscriptionSection(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileEditorSection(
    viewModel: AuraViewModel,
    username: String,
    onUsernameChange: (String) -> Unit,
    handle: String,
    onHandleChange: (String) -> Unit,
    presets: List<Pair<String, String>>,
    filters: List<Pair<String, String>>
) {
    val scrollState = rememberScrollState()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Touch-up options states
    var showCropOverlay by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Statistics Card for current user
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1625)),
            border = BorderStroke(1.2.dp, Color(0xFF16253B)),
            modifier = Modifier.fillMaxWidth().testTag("my_profile_stats_card")
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Mes Statistiques d'Aura 🌌",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val handle = viewModel.userHandle
                    val subscribersCount = viewModel.getSubscribersCount(handle)
                    val followingCount = viewModel.getFollowingCount(handle)
                    val myPosts = viewModel.postsList.filter { it.authorHandle.lowercase() == handle.lowercase() }
                    val publicationsCount = myPosts.size

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format("%,d", subscribersCount),
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = "Abonnés 👥", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }

                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0xFF16253B)))

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format("%,d", followingCount),
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = "Abonnements 🔗", fontSize = 10.sp, color = Color.LightGray, fontWeight = FontWeight.Bold)
                    }

                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0xFF16253B)))

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$publicationsCount",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = "Publications 📝", fontSize = 10.sp, color = Color.LightGray, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Core interactive Photo Editor Frame
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1625)),
            border = BorderStroke(1.2.dp, Color(0xFF16253B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Ajustement & Retouche Photo 📸",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )

                // Visual Picture Canvas with realtime Filters, Scale and Contrast
                ProfileVfxWrapper(
                    badgeName = viewModel.equippedBadge,
                    modifier = Modifier.size(150.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .border(
                                2.dp,
                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                ),
                                CircleShape
                            ),
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
                            username = username,
                            modifier = Modifier.fillMaxSize(),
                            customUri = viewModel.customProfilePhotoUri
                        )

                        // Virtual Cropping Frame Overlay
                        if (showCropOverlay) {
                            Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.25f))
                        ) {
                            // Cropping grid lines
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height
                                // Draw typical crop reticle
                                drawRect(
                                    color = Color(0xFF3DF5FF).copy(alpha = 0.45f),
                                    topLeft = Offset(w * 0.15f, h * 0.15f),
                                    size = androidx.compose.ui.geometry.Size(w * 0.7f, h * 0.7f),
                                    style = Stroke(width = 2f)
                                )
                                drawLine(
                                    color = Color(0xFF3DF5FF).copy(alpha = 0.25f),
                                    start = Offset(w * 0.38f, 0f),
                                    end = Offset(w * 0.38f, h),
                                    strokeWidth = 1f
                                )
                                drawLine(
                                    color = Color(0xFF3DF5FF).copy(alpha = 0.25f),
                                    start = Offset(w * 0.62f, 0f),
                                    end = Offset(w * 0.62f, h),
                                    strokeWidth = 1f
                                )
                                drawLine(
                                    color = Color(0xFF3DF5FF).copy(alpha = 0.25f),
                                    start = Offset(0f, h * 0.38f),
                                    end = Offset(w, h * 0.38f),
                                    strokeWidth = 1f
                                )
                                drawLine(
                                    color = Color(0xFF3DF5FF).copy(alpha = 0.25f),
                                    start = Offset(0f, h * 0.62f),
                                    end = Offset(w, h * 0.62f),
                                    strokeWidth = 1f
                                )
                            }
                            Text(
                                text = "ROGNAGE",
                                color = Color(0xFF3DF5FF),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

                // Interactive Crop and Upload Actions Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val context = LocalContext.current
                    val imageLauncher = rememberLauncherForActivityResult(
                        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
                    ) { uri ->
                        if (uri != null) {
                            try {
                                val inputStream = context.contentResolver.openInputStream(uri)
                                if (inputStream != null) {
                                    val outFile = java.io.File(context.filesDir, "custom_profile_avatar.jpg")
                                    val outputStream = java.io.FileOutputStream(outFile)
                                    inputStream.use { input ->
                                        outputStream.use { output ->
                                            input.copyTo(output)
                                        }
                                    }
                                    viewModel.customProfilePhotoUri = outFile.absolutePath
                                    viewModel.profilePhotoPreset = "Custom"
                                    viewModel.triggerBeep(1)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                    // Importer Button
                    FilledTonalButton(
                        onClick = {
                            viewModel.triggerBeep(3)
                            imageLauncher.launch("image/*")
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (viewModel.profilePhotoPreset == "Custom") MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("upload_custom_photo_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CloudUpload,
                            contentDescription = "Upload",
                            modifier = Modifier.size(14.dp),
                            tint = if (viewModel.profilePhotoPreset == "Custom") MaterialTheme.colorScheme.primary else Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (viewModel.profilePhotoPreset == "Custom") "Photo Active ✨" else "Importer Photo",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (viewModel.profilePhotoPreset == "Custom") MaterialTheme.colorScheme.primary else Color.White
                        )
                    }

                    // Ajuster / Rogner button
                    FilledTonalButton(
                        onClick = {
                            showCropOverlay = !showCropOverlay
                            viewModel.triggerBeep(3)
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (showCropOverlay) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("toggle_crop_trigger")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Crop,
                            contentDescription = "Couper",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (showCropOverlay) "Valider" else "Ajuster / Rogner",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Reset adjustments button
                    IconButton(
                        onClick = {
                            viewModel.profileFilter = "Normal"
                            viewModel.profileBrightness = 1.0f
                            viewModel.profileContrast = 1.0f
                            viewModel.profileZoom = 1.0f
                            viewModel.profileCropX = 0f
                            viewModel.profileCropY = 0f
                            showCropOverlay = false
                            viewModel.triggerBeep(2)
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.RestartAlt,
                            contentDescription = "Réinitialiser",
                            tint = Color.LightGray
                        )
                    }
                }

                Divider(color = Color(0xFF16253B))

                // Brightness Slider
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Luminosité ☀️", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text("${(viewModel.profileBrightness * 100).toInt()}%", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = viewModel.profileBrightness,
                        onValueChange = { viewModel.profileBrightness = it },
                        valueRange = 0.5f..1.5f,
                        modifier = Modifier.testTag("brightness_slider")
                    )
                }

                // Contrast Slider
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Contraste 🌗", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text("${(viewModel.profileContrast * 100).toInt()}%", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = viewModel.profileContrast,
                        onValueChange = { viewModel.profileContrast = it },
                        valueRange = 0.5f..1.5f,
                        modifier = Modifier.testTag("contrast_slider")
                    )
                }

                // Zoom Scaling Slider (crop depth)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Zoom / Taille du cadrage 🔍", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text("x${String.format("%.1f", viewModel.profileZoom)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = viewModel.profileZoom,
                        onValueChange = { viewModel.profileZoom = it },
                        valueRange = 1.0f..2.5f,
                        modifier = Modifier.testTag("zoom_slider")
                    )
                }

                // Simulated Horizontal Crop Offset
                if (showCropOverlay) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Décalage de rognage (X, Y) 📐", fontSize = 11.sp, color = Color.Gray)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Slider(
                                value = viewModel.profileCropX,
                                onValueChange = { viewModel.profileCropX = it },
                                valueRange = -50f..50f,
                                modifier = Modifier.weight(1f).testTag("crop_offset_x")
                            )
                            Slider(
                                value = viewModel.profileCropY,
                                onValueChange = { viewModel.profileCropY = it },
                                valueRange = -50f..50f,
                                modifier = Modifier.weight(1f).testTag("crop_offset_y")
                            )
                        }
                    }
                }
            }
        }

        // 1. Selector of preset illustrations
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF08101C)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Choisir un thème de photo stellaire",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.LightGray
                )

                val activePresetsList = remember(viewModel.customProfilePhotoUri, presets) {
                    if (viewModel.customProfilePhotoUri != null) {
                        presets + ("Custom" to "Photo Importée 👤")
                    } else {
                        presets
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    activePresetsList.forEach { (presetId, presetLabel) ->
                        val isSelected = viewModel.profilePhotoPreset == presetId
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    else Color(0xFF131D2E)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    viewModel.profilePhotoPreset = presetId
                                    viewModel.triggerBeep(3)
                                }
                                .padding(12.dp)
                                .testTag("preset_select_$presetId"),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(45.dp)
                                    .clip(CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                RenderPresetAvatar(
                                    preset = presetId,
                                    filter = "Normal",
                                    brightness = 1f,
                                    contrast = 1f,
                                    zoom = 1f,
                                    cropX = 0f,
                                    cropY = 0f,
                                    username = "M",
                                    modifier = Modifier.fillMaxSize(),
                                    customUri = if (presetId == "Custom") viewModel.customProfilePhotoUri else null
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(presetLabel, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 2. Filter Matrix Preset Selectors
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF08101C)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Filtres artistiques appliqués 🎨",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.LightGray
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    filters.forEach { (filterId, labelDesc) ->
                        val isSelected = viewModel.profileFilter == filterId
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else Color(0xFF131D2E)
                                )
                                .clickable {
                                    viewModel.profileFilter = filterId
                                    viewModel.triggerBeep(3)
                                }
                                .padding(vertical = 8.dp)
                                .testTag("filter_select_$filterId"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = labelDesc,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.Black else Color.White
                            )
                        }
                    }
                }
            }
        }

        // 3. Form input details
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF08101C)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Détails d'identité",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.LightGray
                )

                // Nom
                Column {
                    Text("Nom d'affichage", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 4.dp))
                    OutlinedTextField(
                        value = username,
                        onValueChange = onUsernameChange,
                        singleLine = true,
                        placeholder = { Text("Votre nom...") },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF03070E),
                            unfocusedContainerColor = Color(0xFF03070E),
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color(0xFF1E2E4A)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("username_input_field"),
                        textStyle = TextStyle(fontSize = 14.sp)
                    )
                }

                // Handle alias
                Column {
                    Text("Alias utilisateur (@)", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 4.dp))
                    OutlinedTextField(
                        value = handle,
                        onValueChange = onHandleChange,
                        singleLine = true,
                        placeholder = { Text("votre_id") },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF03070E),
                            unfocusedContainerColor = Color(0xFF03070E),
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color(0xFF1E2E4A)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("handle_input_field"),
                        textStyle = TextStyle(fontSize = 14.sp)
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsSection(viewModel: AuraViewModel) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Paramètres de l'application ⚙️",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.primary
        )

        // Toggles box
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF08101C)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                // Notifications switch
                SettingToggleRow(
                    title = getTranslatedText("notification_title", viewModel.appLanguage),
                    subtitle = getTranslatedText("notification_desc", viewModel.appLanguage),
                    enabledState = viewModel.settingsNotificationsEnabled,
                    onToggle = { viewModel.settingsNotificationsEnabled = it },
                    iconElement = Icons.Outlined.Notifications,
                    tag = "toggle_notifications"
                )

                Divider(color = Color(0xFF16253B))

                // Sound feedback switch
                SettingToggleRow(
                    title = getTranslatedText("sound_title", viewModel.appLanguage),
                    subtitle = getTranslatedText("sound_desc", viewModel.appLanguage),
                    enabledState = viewModel.settingsAudioEnabled,
                    onToggle = { viewModel.settingsAudioEnabled = it },
                    iconElement = Icons.Outlined.VolumeUp,
                    tag = "toggle_audio"
                )

                Divider(color = Color(0xFF16253B))

                // Haptic feedback switch
                SettingToggleRow(
                    title = getTranslatedText("haptic_title", viewModel.appLanguage),
                    subtitle = getTranslatedText("haptic_desc", viewModel.appLanguage),
                    enabledState = viewModel.settingsHapticEnabled,
                    onToggle = { viewModel.settingsHapticEnabled = it },
                    iconElement = Icons.Outlined.Vibration,
                    tag = "toggle_haptic"
                )

                Divider(color = Color(0xFF16253B))

                // Account Privacy switch
                SettingToggleRow(
                    title = getTranslatedText("privacy_title", viewModel.appLanguage),
                    subtitle = getTranslatedText("privacy_desc", viewModel.appLanguage),
                    enabledState = viewModel.settingsAccountPrivate,
                    onToggle = { viewModel.settingsAccountPrivate = it },
                    iconElement = Icons.Outlined.Lock,
                    tag = "toggle_privacy"
                )

                Divider(color = Color(0xFF16253B))

                // Deep dark theme accent settings
                SettingToggleRow(
                    title = getTranslatedText("deep_dark_title", viewModel.appLanguage),
                    subtitle = getTranslatedText("deep_dark_desc", viewModel.appLanguage),
                    enabledState = viewModel.settingsDeepDarkTheme,
                    onToggle = { viewModel.settingsDeepDarkTheme = it },
                    iconElement = Icons.Outlined.NightsStay,
                    tag = "toggle_space_theme"
                )
            }
        }

        Text(
            text = getTranslatedText("theme_option_title", viewModel.appLanguage),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp)
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF08101C)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("dark", "light", "system").forEach { mode ->
                    val isSelected = viewModel.appTheme == mode
                    val textMode = when (mode) {
                        "dark" -> getTranslatedText("theme_dark", viewModel.appLanguage)
                        "light" -> getTranslatedText("theme_light", viewModel.appLanguage)
                        else -> getTranslatedText("theme_system", viewModel.appLanguage)
                    }
                    Button(
                        onClick = {
                            viewModel.appTheme = mode
                            viewModel.triggerBeep(3)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF131F37)
                        ),
                        modifier = Modifier.weight(1f).testTag("theme_btn_$mode"),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text(text = textMode, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        Text(
            text = getTranslatedText("lang_option_title", viewModel.appLanguage),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp)
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF08101C)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("FR" to "Français", "EN" to "English", "ES" to "Español").forEach { (code, name) ->
                        val isSelected = viewModel.appLanguage == code
                        Button(
                            onClick = {
                                viewModel.appLanguage = code
                                viewModel.triggerBeep(3)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF131F37)
                            ),
                            modifier = Modifier.weight(1f).testTag("lang_btn_$code"),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            Text(text = name, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("ZH" to "中文", "HI" to "हिन्दी", "DE" to "Deutsch").forEach { (code, name) ->
                        val isSelected = viewModel.appLanguage == code
                        Button(
                            onClick = {
                                viewModel.appLanguage = code
                                viewModel.triggerBeep(3)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF131F37)
                            ),
                            modifier = Modifier.weight(1f).testTag("lang_btn_$code"),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            Text(text = name, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        var showPolicyDialog by remember { mutableStateOf(false) }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1C2A)),
            border = BorderStroke(1.dp, Color(0xFF2196F3).copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    showPolicyDialog = true
                    viewModel.triggerBeep(3)
                }
                .testTag("app_policy_card")
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = Color(0xFF00FFCC),
                    modifier = Modifier.size(24.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Politique de l'App & Charte d'Utilisation 📜",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Règles, déontologie des Salons Vocaux & Échecs Équitables. Cliquez pour lire.",
                        fontSize = 10.sp,
                        color = Color.LightGray
                    )
                }
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        if (showPolicyDialog) {
            Dialog(onDismissRequest = { showPolicyDialog = false }) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF07101C)),
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(18.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Charte d'Utilisation Mirys 🧘",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = Color(0xFF00FFCC)
                        )

                        Text(
                            text = "Mirys est une plateforme harmonieuse de développement personnel connectant socialement les utilisateurs à travers l'art, les échecs et la parole sage.",
                            fontSize = 11.sp,
                            color = Color.LightGray
                        )

                        Divider(color = Color.White.copy(alpha = 0.1f))

                        Text(
                            text = "1. Respect & Modération Mutuelle",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Tout propos agressif, harcèlement ou comportement toxique est strictement banni. La modération des discussions directes et publiques est assurée par IA.",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )

                        Text(
                            text = "2. Salons Vocaux (VoiceRooms)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Les VoiceRooms sont gratuites jusqu'à 30 minutes de session avec un maximum de 8 auditeurs. L'accès illimité nécessite un abonnement ou un déverrouillage boutique.",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )

                        Text(
                            text = "3. Équité de l'intelligence artificielle aux Échecs",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Les parties d'échecs tactiques dans l'onglet Jeux doivent être jouées sans assistance de moteurs tiers extérieurs. Le respect de l'adversaire est obligatoire.",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )

                        Text(
                            text = "4. Effets VFX Premium",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Les effets visuels de profil sont payants en fonction de la beauté et de la complexité. L'auteur créateur Mirysofficiel dispose seul du VFX de Concepteur Sceau Divin (badges de photo modifiable dans le profil).",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )

                        Button(
                            onClick = { showPolicyDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)
                        ) {
                            Text("J'ai compris et j'accepte", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Version & Developer credits
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1625).copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Mirys v2.4 Standard Edition", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                Text("Conçu par rayanium68, développé par Alane Mentii", fontSize = 10.sp, color = Color.Gray)
                Text("ID d'appareil: MY-9FE02E-F411", fontSize = 9.sp, color = Color.DarkGray)
            }
        }

        // Connexion Créateur Alane
        var loginEmail by remember { mutableStateOf("") }
        var loginCode by remember { mutableStateOf("") }
        var loginError by remember { mutableStateOf<String?>(null) }

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF08101C)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.WorkspacePremium,
                        contentDescription = null,
                        tint = Color(0xFFFFD700)
                    )
                    Text(
                        text = "Espace Développeur & Créateur",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }

                if (viewModel.designerAccountUnlocked) {
                    Text(
                        text = "Identité active : Mirysofficiel 👑 (Alane Mentii)",
                        color = Color(0xFF00FFCC),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Statut : Designer Certifié. Fonctionnalités d'annonces et salons vocaux administratifs entièrement débloquées.",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                    
                    Button(
                        onClick = { viewModel.logoutDesigner() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Déconnexion du compte", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text(
                        text = "Connectez-vous avec vos identifiants administrateur pour débloquer votre badge officiel bleu, vos bannières et vos effets VFX gravitationnels exclusifs.",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )

                    OutlinedTextField(
                        value = loginEmail,
                        onValueChange = { loginEmail = it; loginError = null },
                        label = { Text("Adresse Email Professionnelle", fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color(0xFF16253B),
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = Color.Gray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = loginCode,
                        onValueChange = { loginCode = it; loginError = null },
                        label = { Text("Code de Sécurité d'Accès", fontSize = 11.sp) },
                        singleLine = true,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color(0xFF16253B),
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = Color.Gray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (loginError != null) {
                        Text(loginError!!, color = Color(0xFFD32F2F), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val success = viewModel.tryDesignerLogin(loginEmail, loginCode)
                            if (success) {
                                loginEmail = ""
                                loginCode = ""
                            } else {
                                loginError = "Email ou code de sécurité erroné."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("S'authentifier en Créateur 👑", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SettingToggleRow(
    title: String,
    subtitle: String,
    enabledState: Boolean,
    onToggle: (Boolean) -> Unit,
    iconElement: androidx.compose.ui.graphics.vector.ImageVector,
    tag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(!enabledState) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = iconElement, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, fontSize = 10.sp, color = Color.Gray, lineHeight = 14.sp)
        }

        Switch(
            checked = enabledState,
            onCheckedChange = onToggle,
            modifier = Modifier.testTag(tag)
        )
    }
}

@Composable
fun SubscriptionSection(viewModel: AuraViewModel) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    var showCheckoutPanel by remember { mutableStateOf(false) }
    var checkoutTierSelected by remember { mutableStateOf("Pro ✨") }
    var checkoutPriceSelected by remember { mutableStateOf("4,99 €/m") }

    // Checkout form parameters
    var promoCode by remember { mutableStateOf("") }
    var promoApplied by remember { mutableStateOf(false) }
    var promoError by remember { mutableStateOf(false) }
    var cardNumber by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCvc by remember { mutableStateOf("") }

    var isProcessingPayment by remember { mutableStateOf(false) }
    var paymentSuccessResult by remember { mutableStateOf(false) }

    val benefitsPro = listOf(
        "Bilan Émotionnel IA illimité (au lieu de 3/jour)",
        "Générateur de tâches IA augmenté",
        "Multiplicateur de gain XP +25%",
        "Accès au premier badge boutique Premium"
    )

    val benefitsPremium = listOf(
        "Rapports IA d'études hebdomadaires exclusifs",
        "Salons Vocaux et micro illimités",
        "Multiplicateur de gain XP & Pièces Double (+100%)",
        "Badges de profil holographiques uniques",
        "Clé API autonome optionnelle prioritaire"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Current Plan Box
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Statut du compte", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        text = "Abonnement : ${viewModel.subscriptionTier}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Prochain renouvellement: " + (viewModel.subscriptionExpiryDate ?: "Aucun engagement actif"),
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (viewModel.subscriptionTier == "Gratuit") Icons.Outlined.Person
                                      else Icons.Outlined.WorkspacePremium,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }

        // Offers container
        if (!showCheckoutPanel) {
            Text(
                text = "Améliorez vos performances mentales 👑",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.White
            )

            // Selection of regional currency (Sous-région)
            Text(
                text = "Sélectionnez votre sous-région pour la facturation :",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
            )

            // Horizontal row of sub-region buttons
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().testTag("sub_region_selector")
            ) {
                items(viewModel.subRegionsList) { subRegion ->
                    val isSelected = viewModel.selectedSubRegionId == subRegion.id
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color(0xFF0F1B2F)
                        ),
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF1E2E4A)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.clickable {
                            viewModel.selectedSubRegionId = subRegion.id
                            viewModel.triggerBeep(1)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(subRegion.flag, fontSize = 16.sp)
                            Column {
                                java.lang.String.valueOf(subRegion.id) // keep compile clean
                                Text(
                                    text = subRegion.id,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else Color.White
                                )
                                Text(
                                    text = subRegion.currencySymbol,
                                    fontSize = 9.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else Color.Gray
                                )
                            }
                        }
                    }
                }
            }

            // Offer Pro Plan
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1625)),
                border = BorderStroke(1.dp, Color(0xFF1E2E4A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Formule MIRYS PRO", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                            Text("Idéal pour s'organiser sereinement", fontSize = 11.sp, color = Color.Gray)
                        }
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF0073E6), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Popular", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(viewModel.currentSubRegion.proPriceFormatted, fontSize = 28.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        Text("/ mois", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 4.dp))
                    }

                    Divider(color = Color(0xFF1E2E4A))

                    benefitsPro.forEach { benefit ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Check, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(benefit, fontSize = 12.sp, color = Color.LightGray)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = {
                            checkoutTierSelected = "Pro ✨"
                            checkoutPriceSelected = viewModel.currentSubRegion.proRawPriceString
                            showCheckoutPanel = true
                            viewModel.triggerBeep(3)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("select_pro_plan")
                    ) {
                        Text("Passer à Pro", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Offer Premium Plan
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF120C1F)),
                border = BorderStroke(1.dp, Color(0xFF3B1E4E)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Formule ULTIMATE PREMIUM", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFFFD700))
                            Text("Intelligence et fonctionnalités cosmiques", fontSize = 11.sp, color = Color.Gray)
                        }
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF8E24AA), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Ultimate Elite", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(viewModel.currentSubRegion.premiumPriceFormatted, fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFFFFD700))
                        Text("/ mois", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 4.dp))
                    }

                    Divider(color = Color(0xFF3B1E4E))

                    benefitsPremium.forEach { benefit ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Check, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(benefit, fontSize = 12.sp, color = Color.LightGray)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = {
                            checkoutTierSelected = "Premium Ultimate 👑"
                            checkoutPriceSelected = viewModel.currentSubRegion.premiumRawPriceString
                            showCheckoutPanel = true
                            viewModel.triggerBeep(3)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E24AA)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("select_premium_plan")
                    ) {
                        Text("Devenir Premium Ultimate", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        } else {
            // Checkout UI component (Payment simulation)
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A111E)),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
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
                        Text("Paiement Sécurisé", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        TextButton(onClick = { showCheckoutPanel = false }) {
                            Text("Annuler", color = Color.Gray)
                        }
                    }

                    Text(
                        text = "Vous vous abonnez à la formule $checkoutTierSelected pour $checkoutPriceSelected.",
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )

                    Divider(color = Color(0xFF20324D))

                    if (!paymentSuccessResult) {
                        // Card Number input
                        Column {
                            Text("Numéro de carte bancaire", fontSize = 11.sp, color = Color.Gray)
                            OutlinedTextField(
                                value = cardNumber,
                                onValueChange = {
                                    if (it.length <= 16) cardNumber = it.filter { c -> c.isDigit() }
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                                placeholder = { Text("4000 1234 5678 9010", fontSize = 13.sp) },
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFF03070E),
                                    unfocusedContainerColor = Color(0xFF03070E)
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("card_number_input")
                            )
                        }

                        // Exp & CVC Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Date d'expiration", fontSize = 11.sp, color = Color.Gray)
                                OutlinedTextField(
                                    value = cardExpiry,
                                    onValueChange = {
                                        if (it.length <= 4) cardExpiry = it
                                    },
                                    singleLine = true,
                                    placeholder = { Text("MM/AA") },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color(0xFF03070E),
                                        unfocusedContainerColor = Color(0xFF03070E)
                                    ),
                                    modifier = Modifier.fillMaxWidth().testTag("card_expiry_input")
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Code (CVC)", fontSize = 11.sp, color = Color.Gray)
                                OutlinedTextField(
                                    value = cardCvc,
                                    onValueChange = {
                                        if (it.length <= 3) cardCvc = it.filter { c -> c.isDigit() }
                                    },
                                    singleLine = true,
                                    placeholder = { Text("123") },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color(0xFF03070E),
                                        unfocusedContainerColor = Color(0xFF03070E)
                                    ),
                                    modifier = Modifier.fillMaxWidth().testTag("card_cvc_input")
                                )
                            }
                        }

                        // Code Promotionnel Row
                        Column {
                            Text("Code Promotionnel", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = promoCode,
                                    onValueChange = { promoCode = it.uppercase() },
                                    singleLine = true,
                                    placeholder = { Text("Ex: SFREE50", fontSize = 13.sp) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color(0xFF03070E),
                                        unfocusedContainerColor = Color(0xFF03070E)
                                    ),
                                    modifier = Modifier.weight(1f).testTag("promo_code_input")
                                )

                                Button(
                                    onClick = {
                                        if (promoCode == "MIRYS2026" || promoCode == "SFREE50") {
                                            promoApplied = true
                                            promoError = false
                                            viewModel.triggerBeep(1)
                                        } else {
                                            promoApplied = false
                                            promoError = true
                                            viewModel.triggerBeep(2)
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    modifier = Modifier.height(48.dp).testTag("apply_promo_button")
                                ) {
                                    Text("Appliquer", fontSize = 12.sp)
                                }
                            }

                            if (promoApplied) {
                                Text("Code promo valide ! -50% appliqué sur la facture.", fontSize = 11.sp, color = Color(0xFF00E676))
                            } else if (promoError) {
                                Text("Code promotionnel invalide ou expiré.", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Checkout Submit Button
                        Button(
                            onClick = {
                                if (cardNumber.length >= 12 && cardExpiry.isNotBlank() && cardCvc.length == 3) {
                                    coroutineScope.launch {
                                        isProcessingPayment = true
                                        viewModel.triggerBeep(3)
                                        // Fake latency for payment gateway validation
                                        delay(2600)
                                        isProcessingPayment = false
                                        paymentSuccessResult = true
                                        
                                        // Update state
                                        viewModel.subscriptionTier = checkoutTierSelected
                                        viewModel.subscriptionExpiryDate = "10 Juillet 2026"
                                        viewModel.triggerBeep(1)
                                    }
                                } else {
                                    viewModel.triggerBeep(2)
                                }
                            },
                            enabled = !isProcessingPayment,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("submit_checkout")
                        ) {
                            if (isProcessingPayment) {
                                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp, color = Color.Black)
                            } else {
                                val numericPrice = if (checkoutTierSelected.contains("Pro")) viewModel.currentSubRegion.proNumericPrice else viewModel.currentSubRegion.premiumNumericPrice
                                val currencySymbol = viewModel.currentSubRegion.currencySymbol
                                val finalPrice = if (promoApplied) numericPrice * 0.5 else numericPrice
                                val formattedFinalPrice = if (finalPrice % 1 == 0.0) {
                                    "${finalPrice.toInt()} $currencySymbol"
                                } else {
                                    String.format(java.util.Locale.US, "%.2f", finalPrice).replace(".", ",") + " " + currencySymbol
                                }
                                val valueToPay = if (promoApplied) "Payer $formattedFinalPrice" else "Confirmer & Payer $formattedFinalPrice"
                                Text(valueToPay, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        // Payment Success Screen Block
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Outlined.TaskAlt, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(56.dp))

                            Text("Souscription réussie ! 🎉", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            Text(
                                "Merci d'avoir accordé votre confiance à Mirys. Vos fonctionnalités $checkoutTierSelected sont maintenant débloquées.",
                                fontSize = 12.sp,
                                color = Color.LightGray,
                                textAlign = TextAlign.Center
                            )

                            Button(
                                onClick = {
                                    paymentSuccessResult = false
                                    showCheckoutPanel = false
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.padding(top = 10.dp)
                            ) {
                                Text("Retour à l'espace")
                            }
                        }
                    }
                }
            }
        }
    }
}

// Customized Realtime Paint algorithm of preset avatars in Jetpack Compose
@Composable
fun RenderPresetAvatar(
    preset: String,
    filter: String,
    brightness: Float,
    contrast: Float,
    zoom: Float,
    cropX: Float,
    cropY: Float,
    username: String,
    modifier: Modifier = Modifier,
    customUri: String? = null
) {
    // Determine ColorFilter matrix
    val colorMatrixState = remember(filter) {
        val mat = ColorMatrix()
        var hasFilter = false
        when (filter) {
            "Noir & Blanc" -> { mat.setToSaturation(0f); hasFilter = true }
            "Vintage" -> { mat.set(ColorMatrix(floatArrayOf(
                0.393f, 0.769f, 0.189f, 0f, 0f,
                0.349f, 0.686f, 0.168f, 0f, 0f,
                0.272f, 0.534f, 0.131f, 0f, 0f,
                0f,     0f,     0f,     1f, 0f
            ))); hasFilter = true }
            "Bleu Cyber" -> { mat.set(ColorMatrix(floatArrayOf(
                0.1f, 0.2f, 0.4f, 0f, 0f,
                0.1f, 0.3f, 0.7f, 0f, 0f,
                0.3f, 0.5f, 1.2f, 0f, 0f,
                0f,   0f,   0f,   1f, 0f
            ))); hasFilter = true }
            "Neon Rose" -> { mat.set(ColorMatrix(floatArrayOf(
                1.1f, 0.1f, 0.4f, 0f, 0f,
                0.1f, 0.6f, 0.1f, 0f, 0f,
                0.6f, 0.1f, 1.1f, 0f, 0f,
                0f,   0f,   0f,   1f, 0f
            ))); hasFilter = true }
        }
        if (hasFilter) ColorFilter.colorMatrix(mat) else null
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (preset == "Custom" && customUri != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            ) {
                AsyncImage(
                    model = customUri,
                    contentDescription = "Photo personnalisée",
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    colorFilter = colorMatrixState,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = zoom,
                            scaleY = zoom,
                            translationX = cropX,
                            translationY = cropY
                        )
                )

                // Apply Brightness & Contrast overlay same as original:
                if (brightness > 1.0f) {
                    val alphaWhite = ((brightness - 1.0f) * 0.6f).coerceIn(0f, 0.6f)
                    Box(modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = alphaWhite)))
                } else if (brightness < 1.0f) {
                    val alphaBlack = ((1.0f - brightness) * 0.8f).coerceIn(0f, 0.8f)
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = alphaBlack)))
                }

                if (contrast > 1.0f) {
                    val highContrastOverlay = Color.White.copy(alpha = (contrast - 1f) * 0.15f)
                    Box(
                        modifier = Modifier
                            .fillMaxSize(0.5f)
                            .align(Alignment.Center)
                            .clip(CircleShape)
                            .background(highContrastOverlay)
                    )
                } else if (contrast < 1.0f) {
                    val lowContrastOverlay = Color.Gray.copy(alpha = (1f - contrast) * 0.2f)
                    Box(modifier = Modifier.fillMaxSize().background(lowContrastOverlay))
                }
            }
        } else {
            // Raw Canvas preset
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val w = size.width
                val h = size.height

                // Apply scale & crop offsets simulating touch-up positioning
                val cx = w/2f + cropX
                val cy = h/2f + cropY
                val radius = (w/2f) * zoom

                // 1. Draw base atmospheric circles
                val bgBrush = when (preset) {
                    "Nebula" -> Brush.radialGradient(
                        colors = listOf(Color(0xFF3F51B5), Color(0xFF03070E)),
                        center = Offset(cx, cy),
                        radius = radius
                    )
                    "Golden Hero" -> Brush.radialGradient(
                        colors = listOf(Color(0xFFFFA000), Color(0xFF03070E)),
                        center = Offset(cx, cy),
                        radius = radius
                    )
                    "Cyber Punk" -> Brush.radialGradient(
                        colors = listOf(Color(0xFFFF007F), Color(0xFF03070E)),
                        center = Offset(cx, cy),
                        radius = radius
                    )
                    "Cosmic" -> Brush.radialGradient(
                        colors = listOf(Color(0xFF00E676), Color(0xFF03070E)),
                        center = Offset(cx, cy),
                        radius = radius
                    )
                    else -> Brush.radialGradient(
                        colors = listOf(Color(0xFF1AA3FF), Color(0xFF03070E)),
                        center = Offset(cx, cy),
                        radius = radius
                    )
                }

                drawCircle(
                    brush = bgBrush,
                    center = Offset(cx, cy),
                    radius = radius
                )

                // Draw illustrative figures inside based on preset
                when (preset) {
                    "Nebula" -> {
                        // Saturn style planet with rings
                        drawCircle(color = Color(0xFF80E5FF), radius = radius * 0.4f, center = Offset(cx, cy))
                        // Rings
                        val ringPath = Path().apply {
                            val rWidth = radius * 0.8f
                            val rHeight = radius * 0.12f
                            addOval(androidx.compose.ui.geometry.Rect(cx - rWidth, cy - rHeight, cx + rWidth, cy + rHeight))
                        }
                        drawPath(ringPath, Color.White.copy(alpha = 0.5f), style = Stroke(width = radius * 0.05f))
                    }
                    "Golden Hero" -> {
                        // Shiny crown & crown spires
                        val shinyB = Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA000)))
                        drawCircle(color = Color(0x33FFD700), radius = radius * 0.55f, center = Offset(cx, cy))
                        // Simple vector crown
                        val cPath = Path().apply {
                            moveTo(cx - radius * 0.25f, cy + radius * 0.15f)
                            lineTo(cx + radius * 0.25f, cy + radius * 0.15f)
                            lineTo(cx + radius * 0.3f, cy - radius * 0.10f)
                            lineTo(cx + radius * 0.12f, cy - radius * 0.02f)
                            lineTo(cx, cy - radius * 0.22f)
                            lineTo(cx - radius * 0.12f, cy - radius * 0.02f)
                            lineTo(cx - radius * 0.3f, cy - radius * 0.10f)
                            close()
                        }
                        drawPath(cPath, shinyB)
                    }
                    "Cyber Punk" -> {
                        // Glowing Cyber grid & mask
                        val neonB = Brush.linearGradient(listOf(Color(0xFFFF007F), Color(0xFFD500F9)))
                        drawLine(
                            color = Color(0x33FF007F),
                            start = Offset(cx - radius, cy),
                            end = Offset(cx + radius, cy),
                            strokeWidth = 3f
                        )
                        drawLine(
                            color = Color(0x33FF007F),
                            start = Offset(cx, cy - radius),
                            end = Offset(cx, cy + radius),
                            strokeWidth = 3f
                        )
                        drawCircle(color = Color(0xFFE040FB), radius = radius * 0.32f, center = Offset(cx, cy))
                        // Visor
                        drawRect(
                            color = Color.Cyan,
                            topLeft = Offset(cx - radius * 0.25f, cy - radius * 0.08f),
                            size = androidx.compose.ui.geometry.Size(radius * 0.5f, radius * 0.15f)
                        )
                    }
                    "Cosmic" -> {
                        // Floating galaxy spirals / stars
                        val greenB = Brush.linearGradient(listOf(Color(0xFF00E676), Color(0xFF00B0FF)))
                        drawCircle(brush = greenB, radius = radius * 0.35f, center = Offset(cx, cy))
                        // Orbit path
                        val oPath = Path().apply {
                            val rSize = radius * 0.7f
                            addOval(androidx.compose.ui.geometry.Rect(cx - rSize/2, cy - rSize, cx + rSize/2, cy + rSize))
                        }
                        drawPath(oPath, Color.White.copy(alpha = 0.4f), style = Stroke(width = 2f))
                    }
                    else -> {
                        // Default: Beautiful bold monogram of the user name initials
                        val initials = if (username.length >= 2) username.substring(0, 2).uppercase()
                                       else if (username.isNotEmpty()) username.substring(0, 1).uppercase()
                                       else "M"

                        // We can't direct drawText easily in raw Canvas without NativePaint on Android,
                        // but we can draw a beautiful abstract grid motif (3 circles overlay)
                        drawCircle(color = Color(0xFF80E5FF).copy(alpha = 0.4f), radius = radius * 0.45f, center = Offset(cx - radius*0.12f, cy - radius*0.12f))
                        drawCircle(color = Color(0xFF1AA3FF).copy(alpha = 0.4f), radius = radius * 0.45f, center = Offset(cx + radius*0.12f, cy + radius*0.12f))
                        drawCircle(color = Color.White.copy(alpha = 0.7f), radius = radius * 0.32f, center = Offset(cx, cy))
                    }
                }

                // 2. Apply artistic filters using drawLayer / color filter overlays
                if (filter != "Normal") {
                    // Apply overlay filter tint directly
                    val overlayColor = when (filter) {
                        "Noir & Blanc" -> Color.Black.copy(alpha = 0.1f) // desaturated shading
                        "Vintage" -> Color(0xFFE4A853).copy(alpha = 0.25f) // Sepia yellow-brown glow
                        "Bleu Cyber" -> Color(0xFF00E5FF).copy(alpha = 0.25f) // Futuristic cyan cyan tint
                        "Neon Rose" -> Color(0xFFFF007F).copy(alpha = 0.25f) // Hot pink tint
                        else -> Color.Transparent
                    }
                    drawCircle(
                        color = overlayColor,
                        center = Offset(cx, cy),
                        radius = radius
                    )
                }

                // 3. Apply Brightness (overlay relative white/black layers)
                if (brightness > 1.0f) {
                    val alphaWhite = ((brightness - 1.0f) * 0.6f).coerceIn(0f, 0.6f)
                    drawCircle(
                        color = Color.White.copy(alpha = alphaWhite),
                        center = Offset(cx, cy),
                        radius = radius
                    )
                } else if (brightness < 1.0f) {
                    val alphaBlack = ((1.0f - brightness) * 0.8f).coerceIn(0f, 0.8f)
                    drawCircle(
                        color = Color.Black.copy(alpha = alphaBlack),
                        center = Offset(cx, cy),
                        radius = radius
                    )
                }

                // 4. Apply Contrast adjustments visually (scale brightness overlay)
                if (contrast > 1.0f) {
                    // High contrast overlay
                    val highContrastOverlay = Color.White.copy(alpha = (contrast - 1f) * 0.15f)
                    drawCircle(
                        color = highContrastOverlay,
                        center = Offset(cx, cy),
                        radius = radius * 0.5f
                    )
                } else if (contrast < 1.0f) {
                    // Low contrast shading (flattened black shading mask)
                    val lowContrastOverlay = Color.Gray.copy(alpha = (1f - contrast) * 0.2f)
                    drawCircle(
                        color = lowContrastOverlay,
                        center = Offset(cx, cy),
                        radius = radius
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileVfxWrapper(
    badgeName: String?,
    isOfficial: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "profile_vfx")
    
    // Ring 1 angle (linear, 6s period)
    val angle1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin_6s"
    )

    // Ring 2 angle (linear reverse, 9s period)
    val angle2 by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin_reverse_9s"
    )

    // Ring 3 angle (linear, 18s period)
    val angle3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin_18s"
    )

    // Ring 4 angle (linear reverse, 13s period)
    val angle4 by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(13000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin_reverse_13s"
    )

    // Ring 5 angle (alternate ease-in-out, 4s period)
    val angle5 by infiniteTransition.animateFloat(
        initialValue = -60f,
        targetValue = 120f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "spin_alternate_4s"
    )

    // Glow pulse (4s period)
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    // Floating offset for the avatar content (6s period)
    val floatyOffset by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "avatar_floaty"
    )

    // Shine sweep progress
    val shineProgress by infiniteTransition.animateFloat(
        initialValue = -1.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shine_sweep"
    )

    val now = System.currentTimeMillis()

    // Determine premium color palette based on equipped badge
    val priceTierColors = when {
        badgeName?.contains("Flame", true) == true -> {
            listOf(Color(0xFFFF3000), Color(0xFFFF7A00), Color(0xFFFFD830), Color(0xFFFF3D00)) // Fiery / Flame
        }
        badgeName?.contains("Galaxy", true) == true -> {
            listOf(Color(0xFFB14EFF), Color(0xFFE040FB), Color(0xFF00E5FF), Color(0xFF3F51B5)) // Cosmic / Galaxy
        }
        badgeName?.contains("Crystal", true) == true -> {
            listOf(Color(0xFF00E5FF), Color(0xFFE0F7FA), Color(0xFFFF7AFF), Color(0xFF0097A7)) // Opal / Crystal
        }
        badgeName?.contains("Neon", true) == true -> {
            listOf(Color(0xFFFF2D75), Color(0xFF00FF9D), Color(0xFFD500F9), Color(0xFF00FFCC)) // Cyberpunk Pink / Neon
        }
        badgeName?.contains("Legendary", true) == true -> {
            listOf(Color(0xFFFFD83D), Color(0xFFFFF9C4), Color(0xFFFF7A00), Color(0xFFFFD700)) // Golden Sovereign
        }
        badgeName?.contains("Champion", true) == true -> {
            listOf(Color(0xFF00E5FF), Color(0xFF2979FF), Color(0xFFFFFFFF), Color(0xFF00FF9D)) // Blue Vents
        }
        badgeName?.contains("Futuristic", true) == true -> {
            listOf(Color(0xFF00FF9D), Color(0xFF00E5FF), Color(0xFF60EFFF), Color(0xFF00E676)) // Matrix/Futur
        }
        badgeName?.isNotBlank() == true || isOfficial -> {
            listOf(
                Color(0xFFFF2D75),
                Color(0xFF00E5FF),
                Color(0xFFB14EFF),
                Color(0xFFFFD83D),
                Color(0xFF00FF9D),
                Color(0xFFFF7A00)
            ) // Creator dynamic rainbow aurora
        }
        else -> null
    }

    Box(
        modifier = modifier.padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        if (priceTierColors != null) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val center = Offset(w / 2f, h / 2f)
                val radius = w * 0.38f // Allocate padding for outer halos, sparkles, and particle physics

                // 1. CINEMA BACKGROUND RADIATION ATMOSPHERIC HALO GLOW
                val pulseColor = priceTierColors.getOrElse(0) { Color.Cyan }.copy(alpha = 0.35f * glowScale)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(pulseColor, Color.Transparent),
                        center = center,
                        radius = radius * 1.6f
                    ),
                    radius = radius * 1.6f
                )

                // 2. CONCENTRIC CINEMATIC ROTATING VFX RINGS
                // Ring 1: Conic Sweep Ring (Spin 6s)
                rotate(angle1) {
                    drawCircle(
                        brush = Brush.sweepGradient(
                            colors = priceTierColors + priceTierColors.first(),
                            center = center
                        ),
                        radius = radius,
                        style = Stroke(width = 3.dp.toPx() * (0.8f + 0.2f * glowScale))
                    )
                }

                // Ring 2: Conic Reverse Ring (Spin reverse 9s)
                rotate(angle2) {
                    drawCircle(
                        brush = Brush.sweepGradient(
                            colors = priceTierColors.reversed() + priceTierColors.reversed().first(),
                            center = center
                        ),
                        radius = radius - 5.dp.toPx(),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                // Ring 3: Technical dashed ring (Spin 18s)
                rotate(angle3) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.22f),
                        radius = radius - 10.dp.toPx(),
                        style = Stroke(
                            width = 1.2f.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                        )
                    )
                }

                // Ring 4: Outer flare beam sweep (Spin reverse 13s)
                rotate(angle4) {
                    drawCircle(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                Color.Transparent,
                                priceTierColors.last().copy(alpha = 0.5f),
                                Color.Transparent,
                                Color.Transparent,
                                priceTierColors.first().copy(alpha = 0.5f),
                                Color.Transparent
                            ),
                            center = center
                        ),
                        radius = radius + 6.dp.toPx(),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }

                // Ring 5: Opposing Arc sections (Alternate ease 4s)
                rotate(angle5) {
                    val arcRadius = radius - 15.dp.toPx()
                    drawArc(
                        color = priceTierColors.first().copy(alpha = 0.7f),
                        startAngle = 0f,
                        sweepAngle = 100f,
                        useCenter = false,
                        topLeft = center - Offset(arcRadius, arcRadius),
                        size = Size(arcRadius * 2f, arcRadius * 2f),
                        style = Stroke(width = 1.8f.dp.toPx())
                    )
                    drawArc(
                        color = priceTierColors.getOrElse(2 % priceTierColors.size) { Color.Magenta }.copy(alpha = 0.7f),
                        startAngle = 180f,
                        sweepAngle = 100f,
                        useCenter = false,
                        topLeft = center - Offset(arcRadius, arcRadius),
                        size = Size(arcRadius * 2f, arcRadius * 2f),
                        style = Stroke(width = 1.8f.dp.toPx())
                    )
                }

                // 3. ORBITAL FLYING SPARKS (6 sparks in clockwise and counter-clockwise rotation)
                val sparkColors = listOf(
                    priceTierColors.getOrElse(0) { Color.Red },
                    priceTierColors.getOrElse(1 % priceTierColors.size) { Color.Cyan },
                    priceTierColors.getOrElse(2 % priceTierColors.size) { Color.Magenta },
                    priceTierColors.getOrElse(3 % priceTierColors.size) { Color.Yellow },
                    priceTierColors.getOrElse(4 % priceTierColors.size) { Color.Green },
                    priceTierColors.getOrElse(5 % priceTierColors.size) { Color.LightGray }
                )
                val sparkAngles = listOf(
                    (now % 4000).toFloat() / 4000f * 360f,
                    -(now % 6000).toFloat() / 6000f * 360f,
                    ((now + 2000) % 5000).toFloat() / 5000f * 360f,
                    -((now + 3000) % 8000).toFloat() / 8000f * 360f,
                    ((now + 1000) % 7000).toFloat() / 7000f * 360f,
                    -((now + 4000) % 9500).toFloat() / 9500f * 360f
                )

                for (idx in 0 until 6) {
                    val sa = Math.toRadians(sparkAngles[idx].toDouble())
                    val sx = center.x + Math.cos(sa).toFloat() * (radius * 1.15f)
                    val sy = center.y + Math.sin(sa).toFloat() * (radius * 1.15f)
                    val scolor = sparkColors[idx]
                    
                    drawCircle(
                        color = scolor.copy(alpha = 0.45f),
                        radius = 4.dp.toPx(),
                        center = Offset(sx, sy)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 1.8f.dp.toPx(),
                        center = Offset(sx, sy)
                    )
                }

                // 4. FLOATING PROCEDURAL AMBIENT COSMIC DUST
                for (idx in 0 until 20) {
                    val seed = idx * 100L
                    val rSeed = java.util.Random(seed)
                    val distFactor = rSeed.nextFloat() * 0.9f + 0.1f
                    val radiusOrbit = radius * 1.25f * distFactor
                    
                    val rSpeed = (rSeed.nextFloat() * 0.4f + 0.1f) * (if (idx % 2 == 0) 1 else -1)
                    val baseAngle = rSeed.nextFloat() * 360f
                    
                    val currentAngle = baseAngle + (now % 30000).toFloat() / 30000f * 360f * rSpeed
                    val rad = Math.toRadians(currentAngle.toDouble())
                    val px = center.x + Math.cos(rad).toFloat() * radiusOrbit
                    val py = center.y + Math.sin(rad).toFloat() * radiusOrbit
                    
                    val pColor = priceTierColors[idx % priceTierColors.size].copy(alpha = rSeed.nextFloat() * 0.4f + 0.15f)
                    val pSize = (rSeed.nextFloat() * 1.5f + 0.5f).dp.toPx()
                    
                    drawCircle(
                        color = pColor,
                        radius = pSize,
                        center = Offset(px, py)
                    )
                }

                // 5. REGULAR DETERMINISTIC SPARK EXPLOSIONS ALONG THE RING
                val explosionInterval = 850L
                val currentEpisode = now / explosionInterval
                val progress = (now % explosionInterval).toFloat() / explosionInterval.toFloat()

                val expIdSeed = currentEpisode * 12345L
                val expRandom = java.util.Random(expIdSeed)

                val expAngle = expRandom.nextFloat() * 360f
                val expRad = Math.toRadians(expAngle.toDouble())
                val expX = center.x + Math.cos(expRad).toFloat() * radius
                val expY = center.y + Math.sin(expRad).toFloat() * radius
                val expColor = priceTierColors[expRandom.nextInt(priceTierColors.size)]

                if (progress < 0.5f) {
                    val flashRadius = 14.dp.toPx() * (1f - progress * 2f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(expColor.copy(alpha = 0.5f * (1f - progress * 2f)), Color.Transparent),
                            center = Offset(expX, expY),
                            radius = flashRadius
                        ),
                        radius = flashRadius,
                        center = Offset(expX, expY)
                    )
                }

                val sparkCount = 10
                for (pIdx in 0 until sparkCount) {
                    val pAngle = (pIdx.toFloat() / sparkCount) * (2 * Math.PI) + expRandom.nextFloat() * 0.4f
                    val pSpeed = 12f + expRandom.nextFloat() * 25f
                    val distance = pSpeed * progress * 3.5f
                    
                    val px = expX + Math.cos(pAngle).toFloat() * distance
                    val py = expY + Math.sin(pAngle).toFloat() * distance
                    
                    val pLife = 1f - progress
                    if (pLife > 0f) {
                        drawCircle(
                            color = expColor.copy(alpha = pLife),
                            radius = 1.5.dp.toPx() * pLife,
                            center = Offset(px, py)
                        )
                    }
                }
            }
        }

        // Apply floating animation and diagonal metallic lens sweep shine to the clipped avatar picture
        Box(
            modifier = Modifier
                .graphicsLayer {
                    translationY = if (priceTierColors != null) floatyOffset.dp.toPx() else 0f
                }
                .clip(CircleShape)
                .drawWithCache {
                    onDrawWithContent {
                        drawContent() // First draw child preset avatar
                        
                        if (priceTierColors != null) {
                            // Lay beautiful diagonal shimmering gloss on top of the circle content
                            val w = size.width
                            val h = size.height
                            val startX = w * shineProgress
                            val endX = startX + w * 0.4f
                            
                            drawRect(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.12f),
                                        Color.White.copy(alpha = 0.45f),
                                        Color.White.copy(alpha = 0.12f),
                                        Color.Transparent
                                    ),
                                    start = Offset(startX, 0f),
                                    end = Offset(endX, h)
                                ),
                                blendMode = BlendMode.Overlay
                            )
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

fun getTranslatedText(tag: String, lang: String): String {
    val translations = mapOf(
        "profile_title" to mapOf(
            "FR" to "Mon Compte & Profil",
            "EN" to "My Account & Profile",
            "ES" to "Mi Cuenta y Perfil",
            "DE" to "Mein Konto & Profil",
            "ZH" to "我的账户与个人资料",
            "HI" to "मेरा खाता और प्रोफ़ाइल"
        ),
        "settings_heading" to mapOf(
            "FR" to "Paramètres de l'application ⚙️",
            "EN" to "Application Settings ⚙️",
            "ES" to "Ajustes de la Aplicación ⚙️",
            "DE" to "Anwendungseinstellungen ⚙️",
            "ZH" to "应用程序设置 ⚙️",
            "HI" to "एप्लिकेशन सेटिंग्स ⚙️"
        ),
        "notification_title" to mapOf(
            "FR" to "Activer les Notifications",
            "EN" to "Enable Notifications",
            "ES" to "Habilitar Notificaciones",
            "DE" to "Benachrichtigungen Aktivieren",
            "ZH" to "启用通知",
            "HI" to "सूचनाएं सक्षम करें"
        ),
        "notification_desc" to mapOf(
            "FR" to "Rappels quotidiens d'agenda et bilan de vie émotionnel",
            "EN" to "Daily calendar reminders and emotional review updates",
            "ES" to "Recordatorios diarios de agenda y salud de ánimo",
            "DE" to "Tägliche Kalendererinnerungen und emotionale Berichte",
            "ZH" to "每日日程提醒和情绪生活评估",
            "HI" to "दैनिक कैलेंडर अनुस्मारक और भावनात्मक जीवन समीक्षा"
        ),
        "sound_title" to mapOf(
            "FR" to "Générateur Audio & Effets Sonores",
            "EN" to "Audio Synthesis & Sound Effects",
            "ES" to "Efectos de Sonido y Audio",
            "DE" to "Audio-Synthese & Soundeffekte",
            "ZH" to "音频发生器和音效",
            "HI" to "ऑडियो सिंथेसाइज़र और ध्वनि प्रभाव"
        ),
        "sound_desc" to mapOf(
            "FR" to "Bips synthétiseurs tactiques d'échecs, trivia et boutique",
            "EN" to "Synthesizer sound beeps for trivia tests, chess moves, and shop transactions",
            "ES" to "Sonidos del sintetizador para ajedrez, trivia y tienda",
            "DE" to "Synthesizer-Signaltöne für Schachzüge, Trivia und Shop",
            "ZH" to "用于国际象棋、问答游戏和商店的合成器提示音",
            "HI" to "शतरंज, सामान्य ज्ञान परीक्षा और स्टोर के लिए सिंथेसाइज़र बीप"
        ),
        "haptic_title" to mapOf(
            "FR" to "Retour Haptique",
            "EN" to "Haptic Feedback",
            "ES" to "Comentarios Hápticos",
            "DE" to "Haptisches Feedback",
            "ZH" to "触觉反馈",
            "HI" to "हैप्टिक फीडबैक"
        ),
        "haptic_desc" to mapOf(
            "FR" to "Vibrations tactiles d'immersion mécanique",
            "EN" to "Tactile controller vibrations for physical immersive feel",
            "ES" to "Vibraciones táctiles para una sensación inmersiva",
            "DE" to "Taktile Vibrationen für verbessertes physisches Feedback",
            "ZH" to "机械沉浸式触感震动",
            "HI" to "शारीरिक विसर्जन महसूस करने के लिए स्पर्शनीय कंपन"
        ),
        "privacy_title" to mapOf(
            "FR" to "Compte Privé",
            "EN" to "Private Account",
            "ES" to "Cuenta Privada",
            "DE" to "Privates Konto",
            "ZH" to "私有账户",
            "HI" to "निजी खाता"
        ),
        "privacy_desc" to mapOf(
            "FR" to "Masquer votre ELO, vos badges et l'historique de posts dans le fil social",
            "EN" to "Hide your ELO rating, collected badges, and posts from the direct social stream",
            "ES" to "Ocultar su calificación ELO, insignias y publicaciones de la red social",
            "DE" to "Verberge ELO-Wertung, Abzeichen und Beiträge aus der sozialen Timeline",
            "ZH" to "隐藏您的ELO积分、收集的徽章以及社交动态中的发布历史",
            "HI" to "सोशल फीड में अपना ELO रेटिंग, बैज और पोस्ट इतिहास छुपाएं"
        ),
        "deep_dark_title" to mapOf(
            "FR" to "Thème Sombre Spatial Réel",
            "EN" to "Deep Realistic Space Dark Theme",
            "ES" to "Tema Oscuro Espacial Real",
            "DE" to "Deep Space Dunkles Design",
            "ZH" to "真实太空深色主题",
            "HI" to "गहन यथार्थवादी अंतरिक्ष डार्क थीम"
        ),
        "deep_dark_desc" to mapOf(
            "FR" to "Économie d'énergie OLED absolue avec contrastes stellaires",
            "EN" to "Pure AMOLED black background to power save battery with stars color contrast",
            "ES" to "Ahorro de energía OLED absoluto con alto contraste estelar",
            "DE" to "Absolute OLED-Energieeinsparung mit sternenklaren Kontrasten",
            "ZH" to "纯净的AMOLED黑色背景，极致省电与星空格调",
            "HI" to "तारों के रंग विपरीत के साथ शुद्ध AMOLED काला पृष्ठभूमि"
        ),
        "theme_option_title" to mapOf(
            "FR" to "Sélecteur de Thème Visuel 🎨",
            "EN" to "Visual Theme Selector 🎨",
            "ES" to "Selector de Tema Visual 🎨",
            "DE" to "Design-Auswahl 🎨",
            "ZH" to "视觉主题选择器 🎨",
            "HI" to "दृश्य थीम चयनकर्ता 🎨"
        ),
        "theme_dark" to mapOf(
            "FR" to "Sombre",
            "EN" to "Dark",
            "ES" to "Oscuro",
            "DE" to "Dunkel",
            "ZH" to "深色",
            "HI" to "डार्क"
        ),
        "theme_light" to mapOf(
            "FR" to "Clair",
            "EN" to "Light",
            "ES" to "Claro",
            "DE" to "Hell",
            "ZH" to "浅色",
            "HI" to "लाइट"
        ),
        "theme_system" to mapOf(
            "FR" to "Système",
            "EN" to "System",
            "ES" to "Sistema",
            "DE" to "System",
            "ZH" to "系统默认",
            "HI" to "सिस्टम"
        ),
        "lang_option_title" to mapOf(
            "FR" to "Paramètre de Langue de l'App 🌐",
            "EN" to "App Language Preference 🌐",
            "ES" to "Preferencia de Idioma 🌐",
            "DE" to "App-Spracheinstellung 🌐",
            "ZH" to "应用语言设置 🌐",
            "HI" to "ऐप भाषा प्राथमिकता 🌐"
        )
    )
    return translations[tag]?.get(lang) ?: translations[tag]?.get("EN") ?: tag
}
