package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.components.ProfileVfxWrapper
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.animation.core.*
import coil.compose.AsyncImage
import com.example.ui.viewmodel.AuraViewModel
import com.example.ui.viewmodel.SocialPost
import com.example.ui.viewmodel.DirectMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.VideoView
import android.widget.MediaController
import android.net.Uri
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.rotate
import androidx.compose.foundation.lazy.itemsIndexed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedSocialScreen(
    viewModel: AuraViewModel,
    modifier: Modifier = Modifier
) {
    var newPostText by remember { mutableStateOf("") }
    var selectedPhotos by remember { mutableStateOf(listOf<String>()) }
    var selectedVideoUrl by remember { mutableStateOf<String?>(null) }
    var selectedVideoDuration by remember { mutableStateOf<String?>(null) }

    var showPhotoPicker by remember { mutableStateOf(false) }
    var showVideoPicker by remember { mutableStateOf(false) }
    var activeLightboxPhoto by remember { mutableStateOf<String?>(null) }
    var activeVideoUrl by remember { mutableStateOf<String?>(null) }
    var activeVideoDuration by remember { mutableStateOf<String?>(null) }

    val localContext = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val posts = viewModel.postsList.filter { 
        !viewModel.blockedHandles.contains(it.authorHandle) && 
        !viewModel.restrictedHandles.contains(it.authorHandle) 
    }
    val equipped = viewModel.equippedBadge

    // Launchers for picking from device storage (Photo & Video Picker)
    val pickPhotosLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 6)
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedPhotos = selectedPhotos + uris.map { it.toString() }
        }
    }

    val pickVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedVideoUrl = uri.toString()
            selectedVideoDuration = "0:12" // Assign approximate duration for visual feedback
        }
    }

    var feedActiveSubTab by remember { mutableStateOf("moments") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("feed_social_screen")
    ) {
        // Upper banner introducing the feed & showing user statistics
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Fil Social de Mirys",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Validez l'impact de vos publications avec notre IA !",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }

                // Small circular coins showcase
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("feed_coins_tag")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Paid,
                            contentDescription = "Pièces d'Or",
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${viewModel.coins}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }

        // Pill SubTabs Selector Row (No emojis, real vector icons!)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF060D1A))
                .padding(vertical = 10.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val subTabs = listOf(
                "moments" to Pair("Moments & Fil", Icons.Default.DynamicFeed),
                "policy" to Pair("Charte & Politique", Icons.Default.Info)
            )
            subTabs.forEach { (tabId, pair) ->
                val (label, icon) = pair
                val isSelected = feedActiveSubTab == tabId
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(30.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else Color(0xFF0F1B2F)
                        )
                        .clickable {
                            feedActiveSubTab = tabId
                            viewModel.triggerBeep(3)
                        }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                        .testTag("feed_subtab_$tabId"),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) Color.White else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else Color.Gray
                    )
                }
            }
        }

        // Active Screen render container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (feedActiveSubTab) {
                "moments" -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Stories Carousel Row
                        item {
                            StoriesSection(viewModel = viewModel)
                        }

                        // New Post Creator Box
                        item {
                            Card(
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("post_creator_card")
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        // User Avatar with potential Badger VFX
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                                .then(
                                                    if (equipped != null) {
                                                        Modifier.border(
                                                            2.dp,
                                                            getBadgeBorderBrush(equipped),
                                                            CircleShape
                                                        )
                                                    } else Modifier
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = getAvatarText("Moi"),
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontSize = 14.sp
                                            )
                                        }

                                        OutlinedTextField(
                                            value = newPostText,
                                            onValueChange = { newPostText = it },
                                            placeholder = { Text("Partagez vos moments, pensées de sagesse ou idées...", fontSize = 14.sp) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .heightIn(min = 72.dp)
                                                .testTag("new_post_input"),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                            )
                                        )
                                    }

                                    // Horizontal thumbnail roll for selected photos
                                    if (selectedPhotos.isNotEmpty()) {
                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                        ) {
                                            items(selectedPhotos) { url ->
                                                Box(
                                                    modifier = Modifier
                                                        .size(64.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                                ) {
                                                    AsyncImage(
                                                        model = url,
                                                        contentDescription = "Photo sélectionnée",
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                    
                                                    // Cross button helper to delete selection
                                                    Box(
                                                        modifier = Modifier
                                                            .size(18.dp)
                                                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                                            .align(Alignment.TopEnd)
                                                            .clickable { selectedPhotos = selectedPhotos - url }
                                                            .padding(2.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Close,
                                                            contentDescription = "Supprimer",
                                                            tint = Color.White,
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Selected Video strip indicator
                                    if (selectedVideoUrl != null) {
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(64.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxSize().padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(6.dp))
                                                    ) {
                                                        AsyncImage(
                                                            model = "https://images.unsplash.com/photo-1519681393784-d120267933ba?q=80&w=200",
                                                            contentDescription = null,
                                                            contentScale = ContentScale.Crop,
                                                            modifier = Modifier.fillMaxSize()
                                                        )
                                                        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))
                                                        Icon(
                                                            imageVector = Icons.Default.PlayCircle,
                                                            contentDescription = null,
                                                            tint = Color.White,
                                                            modifier = Modifier.size(20.dp).align(Alignment.Center)
                                                        )
                                                    }
                                                    Column {
                                                        Text("Vidéo jointe", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                        Text("Durée : $selectedVideoDuration", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    }
                                                }

                                                IconButton(
                                                    onClick = {
                                                        selectedVideoUrl = null
                                                        selectedVideoDuration = null
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Supprimer la vidéo",
                                                        tint = MaterialTheme.colorScheme.error
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Attachment Trigger Actions and Submit Button
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Attributing tools
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    if (selectedVideoUrl != null) {
                                                        Toast.makeText(localContext, "Veuillez d'abord enlever la vidéo sélectionnée !", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        showPhotoPicker = true
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PhotoLibrary,
                                                    contentDescription = "Ajouter Photos",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }

                                            IconButton(
                                                onClick = {
                                                    if (selectedPhotos.isNotEmpty()) {
                                                        Toast.makeText(localContext, "Veuillez d'abord enlever les photos sélectionnées !", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        showVideoPicker = true
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.VideoLibrary,
                                                    contentDescription = "Ajouter Vidéo",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }

                                        Button(
                                            onClick = {
                                                if (newPostText.isNotBlank() || selectedPhotos.isNotEmpty() || selectedVideoUrl != null) {
                                                    viewModel.addNewPost(
                                                        content = newPostText,
                                                        photos = selectedPhotos,
                                                        videoUrl = selectedVideoUrl,
                                                        videoDuration = selectedVideoDuration
                                                    )
                                                    newPostText = ""
                                                    selectedPhotos = emptyList()
                                                    selectedVideoUrl = null
                                                    selectedVideoDuration = null
                                                    Toast.makeText(localContext, "Moment publié ! (+5 XP)", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(localContext, "Veuillez saisir du texte ou ajouter un média !", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            modifier = Modifier.testTag("btn_share_post")
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.Send,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Publier", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        // Title for historical posts
                        item {
                            Text(
                                text = "Publications Récentes",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                            )
                        }

                        if (posts.isEmpty()) {
                            item {
                                Text(
                                    text = "Aucun post disponible pour le moment.",
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            items(posts, key = { it.id }) { post ->
                                SocialPostCard(
                                    post = post,
                                    viewModel = viewModel,
                                    onLikeClick = { viewModel.toggleLikePost(post.id) },
                                    onShareClick = {
                                        clipboardManager.setText(AnnotatedString(post.content))
                                        Toast.makeText(localContext, "Lien copié dans le presse-papiers !", Toast.LENGTH_SHORT).show()
                                    },
                                    onCommentClick = {
                                        Toast.makeText(localContext, "Commentaires ouverts dans votre fil d'activités !", Toast.LENGTH_SHORT).show()
                                    },
                                    onAnalyzeClick = {
                                        viewModel.runPostViralityAnalysis(post.id)
                                    },
                                    onPhotoClick = { url -> activeLightboxPhoto = url },
                                    onPlayClick = { url, duration ->
                                        activeVideoUrl = url
                                        activeVideoDuration = duration
                                    }
                                )
                            }
                        }
                    }
                }

                "policy" -> {
                    AppPolicySection(viewModel)
                }
            }
        }
    }

    // Media Picker overlay for photos
    if (showPhotoPicker) {
        MediaPickerDialog(
            isPickerForPhoto = true,
            onMediaSelected = { url ->
                selectedPhotos = selectedPhotos + url
            },
            onDevicePickSelected = {
                pickPhotosLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onDismiss = { showPhotoPicker = false }
        )
    }

    // Media Picker overlay for videos
    if (showVideoPicker) {
        MediaPickerDialog(
            isPickerForPhoto = false,
            onMediaSelected = { url ->
                selectedVideoUrl = url
                selectedVideoDuration = when (url) {
                    "https://assets.mixkit.co/videos/preview/mixkit-starry-night-sky-over-mountains-34440-large.mp4" -> "0:24"
                    "https://assets.mixkit.co/videos/preview/mixkit-forest-stream-in-the-sunlight-529-large.mp4" -> "0:15"
                    else -> "0:12"
                }
            },
            onDevicePickSelected = {
                pickVideoLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                )
            },
            onDismiss = { showVideoPicker = false }
        )
    }

    // Lightbox for full screen image viewing
    if (activeLightboxPhoto != null) {
        PhotoLightboxDialog(
            photoUrl = activeLightboxPhoto!!,
            onDismiss = { activeLightboxPhoto = null }
        )
    }

    // Custom Interactive Video Player overlay
    if (activeVideoUrl != null) {
        VideoPlayerDialog(
            videoUrl = activeVideoUrl!!,
            duration = activeVideoDuration ?: "0:15",
            onDismiss = { activeVideoUrl = null }
        )
    }

    // Interactive active calling screen overlay! (Appel video, audio, audio note)
    if (viewModel.activeCallPartnerHandle != null) {
        ImmersiveCommunicationHub(viewModel = viewModel)
    }

    // Floating Active Profile inspector!
    val activeUserProfileHandle = viewModel.activeUserProfileHandle
    if (activeUserProfileHandle != null) {
        SocialUserProfileDialog(
            handle = activeUserProfileHandle,
            viewModel = viewModel,
            onSendMessageClick = {
                viewModel.closeUserProfile()
                viewModel.selectTab("talk")
                viewModel.talkActiveSubTab = "dms"
                viewModel.activeChatPartnerHandle = activeUserProfileHandle
            },
            onDismiss = { viewModel.closeUserProfile() }
        )
    }

    // Interactive global Security and Administration dialog notifications (Forwarded to alanementii73@gmail.com)
    val securityNotice = viewModel.securityNotificationDialogText
    if (securityNotice != null) {
        AlertDialog(
            onDismissRequest = { viewModel.securityNotificationDialogText = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Alerte de Sécurité & Modération",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Text(
                    text = securityNotice,
                    fontSize = 13.sp,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.securityNotificationDialogText = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Compris", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            },
            containerColor = Color(0xFF0F172A),
            titleContentColor = Color.White,
            textContentColor = Color.LightGray
        )
    }
}

@Composable
fun SocialPostCard(
    post: SocialPost,
    viewModel: AuraViewModel,
    onLikeClick: () -> Unit,
    onShareClick: () -> Unit,
    onCommentClick: () -> Unit,
    onAnalyzeClick: () -> Unit,
    onPhotoClick: (String) -> Unit,
    onPlayClick: (String, String) -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("post_card_${post.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { viewModel.openUserProfile(post.authorHandle) }
                        .padding(4.dp)
                ) {
                    // Profile Avatar with user Badge styling and VFX particles!
                    val badgeName = post.authorBadge ?: when (post.authorHandle.lowercase()) {
                        "rayanium" -> "Legendary"
                        "alanementii" -> "Galaxy"
                        "sophie_zen" -> "Crystal"
                        "lucas_heart" -> "Neon"
                        "sonia_coder" -> "Flame"
                        else -> null
                    }
                    val isOfficial = post.isOfficial || post.authorHandle.lowercase() == "mirysofficiel"
                    ProfileVfxWrapper(
                        badgeName = badgeName,
                        isOfficial = isOfficial,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isOfficial) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = getAvatarText(post.authorName),
                                fontWeight = FontWeight.Bold,
                                color = if (isOfficial) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = post.authorName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            if (post.isOfficial) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Officiel",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "@${post.authorHandle}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "•",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = post.timestamp,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Tiny active user badge decoration or voice note icon
                if (post.authorBadge != null) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text(post.authorBadge, fontSize = 9.sp) },
                        modifier = Modifier.height(24.dp)
                    )
                } else if (post.isVoiceRoom) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text("Salon Vocal", fontSize = 9.sp, fontWeight = FontWeight.Bold) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            labelColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        modifier = Modifier.height(24.dp)
                    )
                }
            }

            // Post Content
            Text(
                text = post.content,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            // HelloTalk-style Inline Translation and Grammar Correction helpers
            var showTranslation by remember { mutableStateOf(false) }
            var showCorrection by remember { mutableStateOf(false) }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                // Traduire button
                AssistChip(
                    onClick = { showTranslation = !showTranslation },
                    label = { Text(if (showTranslation) "Masquer Trad." else "Traduire", fontSize = 11.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (showTranslation) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else Color.Transparent
                    ),
                    modifier = Modifier.height(28.dp)
                )

                // Sentence Correction button (if correction exists or is generated)
                AssistChip(
                    onClick = { showCorrection = !showCorrection },
                    label = { Text(if (showCorrection) "Masquer Correct." else "Correction Linguistique", fontSize = 11.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (showCorrection) MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f) else Color.Transparent
                    ),
                    modifier = Modifier.height(28.dp)
                )
            }

            AnimatedVisibility(visible = showTranslation) {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "TRADUCTION IA",
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = post.translation ?: "English translation (Mirys AI): \"${post.content}\" [L'IA n'a pas détecté d'autre langue]",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            AnimatedVisibility(visible = showCorrection) {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.15f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Spellcheck,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "CORRECTION LINGUISTIQUE MIRYS",
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.secondary,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Text(
                                text = "Texte Original :",
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = post.content,
                                fontSize = 12.sp,
                                style = androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Text(
                                text = "Suggestion Corrigée :",
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = Color(0xFF2E7D32)
                            )
                            Text(
                                text = post.correctedContent ?: "Votre phrase est déjà 100% correcte d'après l'IA de Mirys ! Excellent niveau ! ✨",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF2E7D32)
                            )
                        }

                        if (post.correctionExplanation != null) {
                            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                Text(
                                    text = "Explication Pédagogique :",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = post.correctionExplanation,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }

            // Dynamic Photo / Video attachments (HelloTalk layout inspired)
            if (post.photos.isNotEmpty()) {
                PostPhotosGrid(
                    photos = post.photos,
                    onPhotoClick = onPhotoClick
                )
            }

            if (post.videoUrl != null) {
                PostVideoPlayer(
                    videoUrl = post.videoUrl,
                    duration = post.videoDuration,
                    onPlayClick = { onPlayClick(post.videoUrl, post.videoDuration ?: "0:15") }
                )
            }

            // Interaction Bar
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Like Action
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onLikeClick() }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (post.isLikedByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Aimer",
                        tint = if (post.isLikedByMe) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "${post.likesCount}",
                        fontSize = 12.sp,
                        color = if (post.isLikedByMe) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Comment Action
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onCommentClick() }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = "Commenter",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "${post.id * 3 + 1}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Share Action
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onShareClick() }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Partager",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Partager",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Virality Analysis Module Block !
            Column(modifier = Modifier.fillMaxWidth()) {
                if (post.viralityScore != null) {
                    // Show full beautifully analyzed virality report card !
                    ViralityReportWidget(post = post)
                } else {
                    // Display triggering action CTA for analysis if it's user post
                    if (post.authorHandle == "mon_compte") {
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = onAnalyzeClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary,
                                contentColor = MaterialTheme.colorScheme.onTertiary
                            ),
                            enabled = !viewModel.isAnalyzingPostVirality,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_analyze_virality")
                        ) {
                            if (viewModel.isAnalyzingPostVirality) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = MaterialTheme.colorScheme.onTertiary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Analyse IA en cours...", fontSize = 13.sp)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.ElectricBolt,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Analyser l'impact de mon post par l'IA", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ViralityReportWidget(post: SocialPost) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f)
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header with score badge & index tier vfx
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Rapport de Viralité IA",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }

                // Score Indicator Pin Badge
                Surface(
                    color = getTierColor(post.viralityTier),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(24.dp)
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${post.viralityScore}% • ${getTierEmojiAndText(post.viralityTier)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color.White
                        )
                    }
                }
            }

            // Forces
            if (post.viralityFeedback != null) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ThumbUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Forces du message :",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = post.viralityFeedback,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }

            // Conseils
            if (post.viralityAdvice != null) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Conseils d'optimisation :",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = post.viralityAdvice,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }

            // Payout info footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                val score = post.viralityScore ?: 50
                val payoutCoins = when {
                    score >= 86 -> 150
                    score >= 71 -> 80
                    score >= 51 -> 40
                    score >= 31 -> 15
                    else -> 5
                }
                val payoutXp = when {
                    score >= 86 -> 100
                    score >= 71 -> 50
                    score >= 51 -> 25
                    score >= 31 -> 10
                    else -> 2
                }
                Icon(
                    imageVector = Icons.Default.Paid,
                    contentDescription = null,
                    tint = Color(0xFFFFC107),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "+$payoutCoins",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "+$payoutXp XP",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "crédités !",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

// Utility formatting tools
fun getAvatarText(name: String): String {
    if (name.isBlank()) return "M"
    val parts = name.trim().split(" ")
    return if (parts.size >= 2) {
        (parts[0].take(1) + parts[1].take(1)).uppercase()
    } else {
        name.take(2).uppercase()
    }
}

fun getTierColor(tier: String?): Color {
    return when (tier?.lowercase()) {
        "mega_viral" -> Color(0xFFD32F2F) // Deep Red
        "viral" -> Color(0xFFF57C00) // Orange
        "trending" -> Color(0xFF388E3C) // Green
        "rising" -> Color(0xFF1976D2) // Blue
        else -> Color(0xFF616161) // Grey
    }
}

fun getTierEmojiAndText(tier: String?): String {
    return when (tier?.lowercase()) {
        "mega_viral" -> "Mega Viral"
        "viral" -> "Viral"
        "trending" -> "Trending"
        "rising" -> "Rising"
        else -> "Normal"
    }
}

fun getBadgeBorderBrush(badgeName: String): Brush {
    return when {
        badgeName.contains("Flame", true) -> Brush.linearGradient(listOf(Color(0xFFFF5722), Color(0xFFFF9800)))
        badgeName.contains("Galaxy", true) -> Brush.linearGradient(listOf(Color(0xFF3F51B5), Color(0xFF9C27B0)))
        badgeName.contains("Crystal", true) -> Brush.linearGradient(listOf(Color(0xFF00BCD4), Color(0xFFE91E63)))
        badgeName.contains("Neon", true) -> Brush.linearGradient(listOf(Color(0xFFFF007F), Color(0xFF7F00FF)))
        badgeName.contains("Legendary", true) -> Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFFF5722)))
        badgeName.contains("Champion", true) -> Brush.linearGradient(listOf(Color(0xFF00FFCC), Color(0xFF0066FF)))
        badgeName.contains("Futuristic", true) -> Brush.linearGradient(listOf(Color(0xFF00FF00), Color(0xFF003300)))
        else -> Brush.linearGradient(listOf(Color.LightGray, Color.DarkGray))
    }
}

// --- CURATED EXTRA SUPPORT DIALOGS FOR PHOTO/VIDEO FEED ---

@Composable
fun PhotoLightboxDialog(
    photoUrl: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Photo en grand",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 500.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }
            
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(24.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Fermer",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun VideoPlayerDialog(
    videoUrl: String,
    duration: String,
    onDismiss: () -> Unit
) {
    var isPlaying by remember { mutableStateOf(true) }
    var isMuted by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.96f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Video Screen Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    AndroidView(
                        factory = { ctx ->
                            VideoView(ctx).apply {
                                val mediaController = MediaController(ctx)
                                mediaController.setAnchorView(this)
                                setMediaController(mediaController)
                                setVideoURI(Uri.parse(videoUrl))
                                setOnPreparedListener { mediaPlayer ->
                                    mediaPlayer.isLooping = true
                                    if (isMuted) {
                                        mediaPlayer.setVolume(0f, 0f)
                                    } else {
                                        mediaPlayer.setVolume(1f, 1f)
                                    }
                                    mediaPlayer.start()
                                }
                            }
                        },
                        update = { view ->
                            if (isPlaying) {
                                view.start()
                            } else {
                                view.pause()
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fermer",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)),
                    shape = RoundedCornerShape(16.dp),
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
                            Text(
                                text = "Lecteur Média Mirys v2.0",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (isPlaying) "Lecture en cours" else "En pause",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Durée : $duration",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { isPlaying = !isPlaying }) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "Contrôle Lecture",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                IconButton(onClick = { isMuted = !isMuted }) {
                                    Icon(
                                        imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                        contentDescription = "Volume",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
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

@Composable
fun MediaPickerDialog(
    isPickerForPhoto: Boolean,
    onMediaSelected: (String) -> Unit,
    onDevicePickSelected: () -> Unit,
    onDismiss: () -> Unit
) {
    val photoPresets = listOf(
        "https://images.unsplash.com/photo-1506126613408-eca07ce68773?q=80&w=400",
        "https://images.unsplash.com/photo-1518241353330-0f7941c2d9b5?q=80&w=400",
        "https://images.unsplash.com/photo-1519681393784-d120267933ba?q=80&w=400",
        "https://images.unsplash.com/photo-1529156069898-49953e39b3ac?q=80&w=400",
        "https://images.unsplash.com/photo-1528459801416-a9e53bbf4e17?q=80&w=400",
        "https://images.unsplash.com/photo-1529699211952-734e80c4d42b?q=80&w=400"
    )

    val videoPresets = listOf(
        "https://assets.mixkit.co/videos/preview/mixkit-starry-night-sky-over-mountains-34440-large.mp4" to "0:24",
        "https://assets.mixkit.co/videos/preview/mixkit-forest-stream-in-the-sunlight-529-large.mp4" to "0:15",
        "https://assets.mixkit.co/videos/preview/mixkit-waves-breaking-in-the-ocean-1527-large.mp4" to "0:12"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isPickerForPhoto) "Sélectionner une Photo" else "Sélectionner une Vidéo",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Device Storage direct import choice
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onDevicePickSelected()
                            onDismiss()
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPickerForPhoto) Icons.Default.Image else Icons.Default.Movie,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Column {
                            Text(
                                text = "Importer de l'appareil",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = if (isPickerForPhoto) "Ouvrir votre galerie de photos locales" else "Parcourir vos vidéos locales",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                Text(
                    text = if (isPickerForPhoto) 
                        "Ou bien, choisissez parmi nos exemples d'inspiration zen :" 
                        else "Ou bien, choisissez un de nos clips apaisants :",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (isPickerForPhoto) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        items(photoPresets) { url ->
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        onMediaSelected(url)
                                        onDismiss()
                                    }
                            ) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = "Thème d'image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        videoPresets.forEachIndexed { idx, (url, duration) ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onMediaSelected(url)
                                        onDismiss()
                                    },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = when(idx) {
                                                0 -> "Ciel Nocturne Cosmos"
                                                1 -> "Source en Forêt"
                                                else -> "Vagues de l'Océan"
                                            },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "Durée : $duration",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
fun PostPhotosGrid(
    photos: List<String>,
    onPhotoClick: (String) -> Unit
) {
    if (photos.isEmpty()) return

    Spacer(modifier = Modifier.height(4.dp))
    
    when (photos.size) {
        1 -> {
            AsyncImage(
                model = photos[0],
                contentDescription = "Photo moment",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 220.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onPhotoClick(photos[0]) }
            )
        }
        2 -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                photos.forEach { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = "Photo moment",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1.2f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onPhotoClick(url) }
                    )
                }
            }
        }
        else -> {
            val rows = (photos.size + 2) / 3
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (r in 0 until rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (c in 0..2) {
                            val index = r * 3 + c
                            if (index < photos.size) {
                                val url = photos[index]
                                AsyncImage(
                                    model = url,
                                    contentDescription = "Photo moment",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onPhotoClick(url) }
                                )
                            } else {
                                Box(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PostVideoPlayer(
    videoUrl: String,
    duration: String?,
    onPlayClick: () -> Unit
) {
    Spacer(modifier = Modifier.height(4.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onPlayClick() }
    ) {
        AsyncImage(
            model = "https://images.unsplash.com/photo-1519681393784-d120267933ba?q=80&w=600",
            contentDescription = "Aperçu de la vidéo",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f))
                    )
                )
        )

        Box(
            modifier = Modifier
                .size(54.dp)
                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                .align(Alignment.Center),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Lire la vidéo",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }

        if (duration != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(10.dp)
                    )
                    Text(
                        text = duration,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ==========================================
// DIRECT MESSAGES TAB & SUB-COMPONENTS
// ==========================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DirectMessagesTab(
    viewModel: AuraViewModel,
    activePartnerHandle: String?,
    onSelectPartner: (String?) -> Unit
) {
    val localContext = LocalContext.current
    val mockPartners = listOf(
        Pair("Mirysofficiel", "Le Concepteur Officiel ✨"),
        Pair("rayanium", "Expert IA & Designer 🎨"),
        Pair("alanementii", "Consultant Scientifique 🔬"),
        Pair("sophie_zen", "Médiatrice Spirituelle 🌸"),
        Pair("lucas_heart", "Joueur & Créateur de Jeux 🎮")
    )

    if (activePartnerHandle == null) {
        // Render chat lobby list
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF030712))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Conversations Directes",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Text(
                text = "Sélectionnez un membre pour démarrer un chat direct sécurisé, un appel ou envoyer des notes vocales.",
                fontSize = 11.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(mockPartners) { (handle, description) ->
                    val isPartnerOfficial = handle == "Mirysofficiel"
                    Card(
                        onClick = { onSelectPartner(handle) },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1527)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dm_partner_card_$handle")
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Avatar wrapping visual VFX if equipped or official account
                                ProfileVfxWrapper(
                                    badgeName = if (isPartnerOfficial) "designer_badge" else null,
                                    isOfficial = isPartnerOfficial,
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF334155)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = handle.take(2).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 13.sp
                                        )
                                    }
                                }

                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = handle,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color.White
                                        )
                                        if (isPartnerOfficial) {
                                            Icon(
                                                imageVector = Icons.Default.Verified,
                                                contentDescription = "Badge Bleu Officiel",
                                                tint = Color(0xFF2196F3),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        
                                        val isOnlineItem = viewModel.isUserOnline(handle)
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(if (isOnlineItem) Color(0xFF10B981) else Color(0xFF6B7280))
                                        )
                                    }
                                    Text(
                                        text = description,
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                    
                                    val isOnlineItem = viewModel.isUserOnline(handle)
                                    Text(
                                        text = if (isOnlineItem) "Actif • En ligne 🚀" else "Hors ligne • S'est déconnecté 💤",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isOnlineItem) Color(0xFF10B981) else Color(0xFF6B7280)
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = "Ouvrir",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    } else {
        // Individual active messaging thread
        val dmsList = viewModel.directMessagesMap[activePartnerHandle] ?: emptyList()
        var msgInputText by remember { mutableStateOf("") }
        var showPhotoPresetsDialog by remember { mutableStateOf(false) }
        var showVideoPresetsDialog by remember { mutableStateOf(false) }
        var showVoiceRecorderDialog by remember { mutableStateOf(false) }
        var isRecordingVoice by remember { mutableStateOf(false) }
        var recordingSeconds by remember { mutableIntStateOf(0) }
        val waveHeights = remember { mutableStateListOf<Float>() }

        LaunchedEffect(isRecordingVoice) {
            if (isRecordingVoice) {
                recordingSeconds = 0
                while (isRecordingVoice) {
                    kotlinx.coroutines.delay(1000)
                    recordingSeconds++
                }
            }
        }

        LaunchedEffect(isRecordingVoice) {
            if (isRecordingVoice) {
                while (isRecordingVoice) {
                    kotlinx.coroutines.delay(100)
                    if (waveHeights.size > 22) {
                        waveHeights.removeAt(0)
                    }
                    waveHeights.add((15..55).random().toFloat())
                }
            } else {
                waveHeights.clear()
            }
        }
        val listState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()

        val photoPickerLauncher = rememberLauncherForActivityResult(
            contract = androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            if (uri != null) {
                try {
                    val inputStream = localContext.contentResolver.openInputStream(uri)
                    if (inputStream != null) {
                        val file = java.io.File(localContext.filesDir, "chat_captured_photo_${System.currentTimeMillis()}.jpg")
                        val outputStream = java.io.FileOutputStream(file)
                        inputStream.use { input ->
                            outputStream.use { output ->
                                input.copyTo(output)
                            }
                        }
                        viewModel.sendDirectMessage(activePartnerHandle, "Photo du téléphone 📷", photoUrl = file.absolutePath)
                        showPhotoPresetsDialog = false
                        Toast.makeText(localContext, "Photo réelle envoyée ! 📸", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    viewModel.sendDirectMessage(activePartnerHandle, "Photo du téléphone 📷", photoUrl = uri.toString())
                    showPhotoPresetsDialog = false
                }
            }
        }

        val videoPickerLauncher = rememberLauncherForActivityResult(
            contract = androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            if (uri != null) {
                try {
                    val inputStream = localContext.contentResolver.openInputStream(uri)
                    if (inputStream != null) {
                        val file = java.io.File(localContext.filesDir, "chat_captured_video_${System.currentTimeMillis()}.mp4")
                        val outputStream = java.io.FileOutputStream(file)
                        inputStream.use { input ->
                            outputStream.use { output ->
                                input.copyTo(output)
                            }
                        }
                        viewModel.sendDirectMessage(activePartnerHandle, "Vidéo du téléphone 🎬", isVideo = true, videoUrl = file.absolutePath)
                        showVideoPresetsDialog = false
                        Toast.makeText(localContext, "Vidéo réelle envoyée ! 🎥", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    viewModel.sendDirectMessage(activePartnerHandle, "Vidéo du téléphone 🎬", isVideo = true, videoUrl = uri.toString())
                    showVideoPresetsDialog = false
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF030712))
        ) {
            // Chat header row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0D1527))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IconButton(
                        onClick = { onSelectPartner(null) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = Color.White
                        )
                    }

                    // mini profile badge avatar
                    val isPartnerOfficial = activePartnerHandle == "Mirysofficiel"
                    ProfileVfxWrapper(
                        badgeName = if (isPartnerOfficial) "designer_badge" else null,
                        isOfficial = isPartnerOfficial,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF334155)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = activePartnerHandle.take(2).uppercase(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color.White
                            )
                        }
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = activePartnerHandle,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                            if (isPartnerOfficial) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Vérifié",
                                    tint = Color(0xFF2196F3),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        val isPartnerOnline = viewModel.isUserOnline(activePartnerHandle)
                        Text(
                            text = if (isPartnerOnline) "En ligne (Temps Réel) 📱" else "Hors ligne (Temps Réel) 💤",
                            fontSize = 9.sp,
                            color = if (isPartnerOnline) Color(0xFF10B981) else Color(0xFF9CA3AF),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Header Action tools (Audio, Video calls)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.triggerCallSession(activePartnerHandle, "audio") },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = "Appel Vocal", tint = Color.LightGray, modifier = Modifier.size(20.dp))
                    }
                    IconButton(
                        onClick = { viewModel.triggerCallSession(activePartnerHandle, "video") },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Videocam, contentDescription = "Appel Vidéo", tint = Color.LightGray, modifier = Modifier.size(20.dp))
                    }
                }
            }

            // Message thread lists
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (dmsList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Début de la conversation sécurisée avec @$activePartnerHandle.\nLes messages sont chiffrés en local.",
                                fontSize = 10.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                items(dmsList) { dmyItem ->
                    val isMine = dmyItem.senderHandle == viewModel.userHandle || dmyItem.senderHandle == "mon_compte"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
                    ) {
                        Card(
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isMine) 16.dp else 2.dp,
                                bottomEnd = if (isMine) 2.dp else 16.dp
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isMine) MaterialTheme.colorScheme.primary else Color(0xFF1E293B)
                            ),
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                if (dmyItem.isVoiceNote) {
                                    // Audio Note styling card with realistic device Speak (TTS)
                                    var isHearing by remember { mutableStateOf(false) }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.clickable {
                                            isHearing = true
                                            viewModel.speakText(dmyItem.content)
                                            coroutineScope.launch {
                                                kotlinx.coroutines.delay(5000)
                                                isHearing = false
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (isHearing) Icons.Default.VolumeUp else Icons.Default.PlayCircle,
                                            contentDescription = "Play note",
                                            tint = Color.White,
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Column {
                                            Text("Message Vocal • ${dmyItem.voiceNoteDuration} s 🎙️", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            Text(
                                                text = if (isHearing) "Lecture : \"${dmyItem.content}\"" else "🎤 Appuyez pour écouter",
                                                fontSize = 9.sp,
                                                color = Color.White.copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                } else if (dmyItem.photoUrl != null) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        AsyncImage(
                                            model = dmyItem.photoUrl,
                                            contentDescription = "Photo jointe",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(150.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        if (dmyItem.content.isNotBlank()) {
                                            Text(
                                                text = dmyItem.content,
                                                fontSize = 13.sp,
                                                color = Color.White
                                            )
                                        }
                                    }
                                } else if (dmyItem.videoUrl != null) {
                                    var isVideoActive by remember { mutableStateOf(false) }
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(150.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.Black),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isVideoActive) {
                                                AndroidView(
                                                    factory = { ctx ->
                                                        VideoView(ctx).apply {
                                                            setVideoURI(Uri.parse(dmyItem.videoUrl))
                                                            setOnPreparedListener { mp ->
                                                                mp.isLooping = true
                                                                start()
                                                            }
                                                        }
                                                    },
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .clickable { isVideoActive = false }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.PauseCircle,
                                                        contentDescription = "Stop",
                                                        tint = Color.White.copy(alpha = 0.5f),
                                                        modifier = Modifier.size(36.dp).align(Alignment.Center)
                                                    )
                                                }
                                            } else {
                                                AsyncImage(
                                                    model = "https://images.unsplash.com/photo-1516280440614-37939bbacd6a?q=80&w=400",
                                                    contentDescription = "Vidéo miniature",
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .size(44.dp)
                                                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                                        .clickable { isVideoActive = true },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.PlayArrow,
                                                        contentDescription = "Lire la vidéo",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = if (dmyItem.content.isNotBlank()) dmyItem.content else "Clip Vidéo Réel 🎬",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                } else {
                                    Text(
                                        text = dmyItem.content,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Chat entry writing tools
            Surface(
                color = Color(0xFF0D1527),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (isRecordingVoice) {
                        // WhatsApp-style Morph Recording Bar!
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Cancel Trash Can Icon
                            IconButton(
                                onClick = {
                                    isRecordingVoice = false
                                    viewModel.triggerBeep(2)
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Annuler l'enregistrement",
                                    tint = Color.Red
                                )
                            }

                            // Blinking Red Dot
                            val infiniteTransition = rememberInfiniteTransition(label = "BlinkRecording")
                            val blinkAlpha by infiniteTransition.animateFloat(
                                initialValue = 0.2f,
                                targetValue = 1.0f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(600),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "BlinkAlpha"
                            )
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red.copy(alpha = blinkAlpha))
                            )

                            // Timer Label
                            Text(
                                text = "Enregistrement • 0:${recordingSeconds.toString().padStart(2, '0')}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Box(modifier = Modifier.width(1.dp).height(20.dp).background(Color.White.copy(alpha = 0.12f)))

                            // Fluctuating voice wave track
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(30.dp)
                                    .padding(horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start
                            ) {
                                waveHeights.forEach { waveHeight ->
                                    Box(
                                        modifier = Modifier
                                            .width(2.5.dp)
                                            .height(waveHeight.dp)
                                            .padding(horizontal = 0.5.dp)
                                            .clip(RoundedCornerShape(1.dp))
                                            .background(Color(0xFF10B981))
                                    )
                                }
                            }

                            // Stop & Send Button
                            IconButton(
                                onClick = {
                                    val simulatedSpiritualMessages = listOf(
                                        "Bonjour ! Je voulais juste t'envoyer de bonnes vibrations cosmiques d'Aura. Passe une journée paisible ! ✨",
                                        "Salut ! J'ai hâte d'analyser l'alignement de nos Auras ensemble ou de faire une partie de Quizz !",
                                        "Coucou ! Je t'envoie ces douces ondes sonores. Que ton esprit reste concentré et serein. 🧘",
                                        "Hello mon ami ! Que la paix cosmique et céleste accompagne chacun de tes pas aujourd'hui ! 🌌",
                                        "Salut ! Nos énergies spirituelles se connectent à merveille. Excellente continuation de journée !",
                                        "Coucou ! Message vocal express de pure sérénité céleste. Respire, souris et profite !"
                                    )
                                    val chosenMessage = simulatedSpiritualMessages.random()
                                    val duration = if (recordingSeconds == 0) 3 else recordingSeconds
                                    viewModel.sendDirectMessage(activePartnerHandle, chosenMessage, isVoiceNote = true, voiceNoteDuration = duration)
                                    
                                    isRecordingVoice = false
                                    viewModel.triggerBeep(1)
                                    viewModel.triggerVibration(4)
                                    Toast.makeText(localContext, "Message audio envoyé avec succès ! 🎤", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(Color(0xFF10B981), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Envoyer le message audio",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    } else {
                        // Normal input line (where the voice button triggers recorders)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Media attachment trigger (Real Photo)
                            IconButton(
                                onClick = { showPhotoPresetsDialog = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = "Envoyer une Photo",
                                    tint = Color(0xFF38BDF8)
                                )
                            }

                            // Media attachment trigger (Real Video)
                            IconButton(
                                onClick = { showVideoPresetsDialog = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VideoCall,
                                    contentDescription = "Envoyer une Vidéo",
                                    tint = Color(0xFFA855F7)
                                )
                            }

                            // WhatsApp-style Voice Recording Trigger
                            IconButton(
                                onClick = {
                                    isRecordingVoice = true
                                    viewModel.triggerBeep(3)
                                    viewModel.triggerVibration(2)
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Enregistrer Message Vocal",
                                    tint = Color(0xFF10B981)
                                )
                            }

                            OutlinedTextField(
                                value = msgInputText,
                                onValueChange = { msgInputText = it },
                                placeholder = { Text("Écrire un message...", fontSize = 13.sp) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("dm_text_textfield"),
                                maxLines = 2,
                                textStyle = LocalTextStyle.current.copy(fontSize = 13.sp, color = Color.White),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
                                )
                            )

                            // Option to open Advanced AI script engine
                            IconButton(
                                onClick = { showVoiceRecorderDialog = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Synthetiseur Vocal AI",
                                    tint = Color(0xFFFBBF24)
                                )
                            }

                            IconButton(
                                onClick = {
                                    if (msgInputText.isNotBlank()) {
                                        viewModel.sendDirectMessage(activePartnerHandle, msgInputText, isVoiceNote = false)
                                        msgInputText = ""
                                    }
                                },
                                enabled = msgInputText.isNotBlank(),
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(
                                        if (msgInputText.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.2f),
                                        CircleShape
                                    )
                                    .testTag("dm_send_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Envoyer",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Real Photo Presets Dialog
            if (showPhotoPresetsDialog) {
                AlertDialog(
                    onDismissRequest = { showPhotoPresetsDialog = false },
                    title = { Text("Partager une Photo Réelle 📸", color = Color.White, fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        androidx.activity.result.PickVisualMediaRequest(
                                            androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                },
                                modifier = Modifier.fillMaxWidth().testTag("pick_real_photo_from_disk"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Depuis mon téléphone 📱", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            
                            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                            Text("Ou sélectionnez une image d'ambiance ci-dessous ou un lien personnalisé :", fontSize = 11.sp, color = Color.LightGray)
                            
                            val photoPresets = listOf(
                                Pair("Paysage Zen d'Automne 🍁", "https://images.unsplash.com/photo-1545239351-ef35f43d514b?q=80&w=400"),
                                Pair("Aura d'étoiles cosmiques ✨", "https://images.unsplash.com/photo-1464802686167-b939a6910659?q=80&w=400"),
                                Pair("Montagne & Calme Spirituel 🏔️", "https://images.unsplash.com/photo-1506744038136-46273834b3fb?q=80&w=400")
                            )

                            photoPresets.forEach { (name, url) ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.sendDirectMessage(activePartnerHandle, name, photoUrl = url)
                                            showPhotoPresetsDialog = false
                                            Toast.makeText(localContext, "Photo envoyée avec succès !", Toast.LENGTH_SHORT).show()
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AsyncImage(model = url, contentDescription = null, modifier = Modifier.size(45.dp).clip(RoundedCornerShape(6.dp)), contentScale = ContentScale.Crop)
                                        Text(name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            var customLink by remember { mutableStateOf("") }
                            OutlinedTextField(
                                value = customLink,
                                onValueChange = { customLink = it },
                                placeholder = { Text("Ou collez un lien d'image direct...", fontSize = 11.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 11.sp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = Color.Gray)
                            )
                            if (customLink.isNotBlank()) {
                                Button(
                                    onClick = {
                                        viewModel.sendDirectMessage(activePartnerHandle, "Photo partagée de l'Aura", photoUrl = customLink)
                                        showPhotoPresetsDialog = false
                                        Toast.makeText(localContext, "Lien photo personnalisé envoyé !", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Envoyer ce lien", fontSize = 11.sp)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showPhotoPresetsDialog = false }) {
                            Text("Annuler", color = Color.Red)
                        }
                    },
                    containerColor = Color(0xFF0D1527)
                )
            }

            // Real Video Presets Dialog
            if (showVideoPresetsDialog) {
                AlertDialog(
                    onDismissRequest = { showVideoPresetsDialog = false },
                    title = { Text("Partager une Vidéo Réelle 🎬", color = Color.White, fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    videoPickerLauncher.launch(
                                        androidx.activity.result.PickVisualMediaRequest(
                                            androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.VideoOnly
                                        )
                                    )
                                },
                                modifier = Modifier.fillMaxWidth().testTag("pick_real_video_from_disk"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA855F7))
                            ) {
                                Icon(Icons.Default.VideoCall, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Depuis mon téléphone 📱", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            
                            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                            Text("Ou choisissez l'une de ces véritables séquences d'ambiance à lire instantanément dans le chat :", fontSize = 11.sp, color = Color.LightGray)
                            
                            val videoPresets = listOf(
                                Triple("Pluie de relax d'automne ☔", "https://assets.mixkit.co/videos/preview/mixkit-rain-on-window-pane-of-a-parked-car-view-4537-large.mp4", "https://images.unsplash.com/photo-1534274988757-a28bf1a57c17?q=80&w=400"),
                                Triple("Feu de cheminée chaleureux 🔥", "https://assets.mixkit.co/videos/preview/mixkit-fire-in-a-fireplace-in-close-up-vivid-glowing-coals-and-burning-logs-48197-large.mp4", "https://images.unsplash.com/photo-1473580044384-7ba9967e16a0?q=80&w=400"),
                                Triple("Ruisseau de forêt tranquille 🌲", "https://assets.mixkit.co/videos/preview/mixkit-river-in-forest-455-large.mp4", "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?q=80&w=400")
                            )

                            videoPresets.forEach { (name, link, thumbnail) ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.sendDirectMessage(activePartnerHandle, name, isVideo = true, videoUrl = link)
                                            showVideoPresetsDialog = false
                                            Toast.makeText(localContext, "Vidéo envoyée !", Toast.LENGTH_SHORT).show()
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AsyncImage(model = thumbnail, contentDescription = null, modifier = Modifier.size(45.dp).clip(RoundedCornerShape(6.dp)), contentScale = ContentScale.Crop)
                                        Text(name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showVideoPresetsDialog = false }) {
                            Text("Annuler", color = Color.Red)
                        }
                    },
                    containerColor = Color(0xFF0D1527)
                )
            }

            // Real Scripted Voice Recorder Dialog using dynamic preview and TextToSpeech
            if (showVoiceRecorderDialog) {
                var selectedScript by remember { mutableStateOf("Bonjour ! Je voulais juste t'envoyer de bonnes vibrations cosmiques d'Aura. Passe une journée paisible ! ✨") }
                var isPreviewSpeaking by remember { mutableStateOf(false) }

                AlertDialog(
                    onDismissRequest = { showVoiceRecorderDialog = false },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Mic, contentDescription = null, tint = Color.Green)
                            Text("Enregistreur Vocal Réel de Mirys 🎙️", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Choisissez ou composez le script de votre message vocal. La synthèse vocale Android le lira de vive voix au destinataire :", fontSize = 11.sp, color = Color.LightGray)
                            
                            val voiceScripts = listOf(
                                "Bonjour ! Je voulais juste t'envoyer de bonnes vibrations cosmiques d'Aura. Passe une journée paisible ! ✨",
                                "Salut ! J'ai hâte d'analyser l'alignement de nos Auras ensemble ou de faire une partie d'échecs !",
                                "Coucou ! Je t'envoie ces douces ondes sonores. Que ton esprit reste concentré et relax."
                            )

                            voiceScripts.forEach { script ->
                                val isSelected = selectedScript == script
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color(0xFF0F172A)
                                    ),
                                    border = if (isSelected) BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary) else null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedScript = script }
                                ) {
                                    Text(
                                        text = script,
                                        modifier = Modifier.padding(10.dp),
                                        fontSize = 11.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else Color.White
                                    )
                                }
                            }

                            // Custom script edit box
                            OutlinedTextField(
                                value = selectedScript,
                                onValueChange = { selectedScript = it },
                                label = { Text("Écrire un script personnalisé...", fontSize = 10.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 11.sp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                            )

                            // Waveform feedback preview row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        isPreviewSpeaking = true
                                        viewModel.speakText(selectedScript)
                                        coroutineScope.launch {
                                            kotlinx.coroutines.delay(6000)
                                            isPreviewSpeaking = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (isPreviewSpeaking) Color.Red else MaterialTheme.colorScheme.secondary),
                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = if (isPreviewSpeaking) Icons.Default.VolumeUp else Icons.Default.VolumeMute, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Text("Tester l'écoute", fontSize = 10.sp)
                                    }
                                }
                                Text(
                                    text = if (isPreviewSpeaking) "Onde active : WWWWwwwww" else "Cliquez pour tester l'élocution réelle.",
                                    fontSize = 10.sp,
                                    color = Color.White
                                )
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showVoiceRecorderDialog = false }) {
                            Text("Annuler", color = Color.Gray)
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.sendDirectMessage(activePartnerHandle, selectedScript, isVoiceNote = true, voiceNoteDuration = selectedScript.length / 10)
                                showVoiceRecorderDialog = false
                                Toast.makeText(localContext, "Votre message vocal a été synthétisé et envoyé !", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Text("Enregistrer & Envoyer", fontSize = 11.sp)
                        }
                    },
                    containerColor = Color(0xFF0D1527)
                )
            }
        }
    }
}

// ==========================================
// VOICE ROOMS TAB & SUB-COMPONENTS
// ==========================================

@Composable
fun VoiceRoomsTab(viewModel: AuraViewModel, isVideoOnly: Boolean = false) {
    val localContext = LocalContext.current
    val mockRooms = if (isVideoOnly) {
        listOf(
            Triple("Salon Cinéma Aura & VFX de nuit 🎬", "rayanium", 18),
            Triple("Démo en direct des filtres Mirys ✨", "Mirysofficiel", 31)
        )
    } else {
        listOf(
            Triple("Café des esprits libres ☕", "Mirysofficiel", 14),
            Triple("Méditation & Silence profond 🧘", "sophie_zen", 8),
            Triple("Décryptage Astro-Finance 💰", "lucas_heart", 23)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030712))
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome and info
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1527)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (isVideoOnly) "Salons de Conversation Vidéo 🎥" else "Salons de Conversation Audio 🎙️",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (isVideoOnly) {
                        "Lancez une visioconférence ou un appel vidéo public de groupe. Les 15 premières minutes sont gratuites ! Au-delà, un tarif minime de ${viewModel.currentSubRegion.streamPriceFormatted} s'applique."
                    } else {
                        "Lancez un salon audio en direct. Les 15 premières minutes sont gratuites pour tous les membres ! Au-delà, un tarif minime de ${viewModel.currentSubRegion.streamPriceFormatted} s'applique."
                    },
                    fontSize = 11.sp,
                    color = Color.LightGray,
                    lineHeight = 16.sp
                )
            }
        }

        // Toggle card to decide whether to publish an announcement card in Moments
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "Annoncer dans les Moments 📢",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Publie automatiquement une carte d'invitation dans le fil d'actualité. (Désactivez pour garder privé)",
                            fontSize = 10.sp,
                            color = Color.LightGray,
                            lineHeight = 14.sp
                        )
                    }
                }
                Switch(
                    modifier = Modifier.testTag("toggle_auto_publish_room"),
                    checked = viewModel.shouldOfferRoomPostInMoments,
                    onCheckedChange = { viewModel.shouldOfferRoomPostInMoments = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                )
            }
        }

        // Room generator quick actions based on selected tab mode
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (!isVideoOnly) {
                Button(
                    onClick = {
                        viewModel.startVoiceOrVideoRoom("voiceroom")
                        val publishMsg = if (viewModel.shouldOfferRoomPostInMoments) "avec annonce partagée !" else "(gardé privé)"
                        Toast.makeText(localContext, "Salon Vocal ouvert $publishMsg !", Toast.LENGTH_LONG).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_create_audio_room"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ouvrir un Salon Vocal 🎙️", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = {
                        viewModel.startVoiceOrVideoRoom("videoroom")
                        val publishMsg = if (viewModel.shouldOfferRoomPostInMoments) "avec annonce partagée !" else "(gardé privé)"
                        Toast.makeText(localContext, "Salle Vidéo ouverte $publishMsg !", Toast.LENGTH_LONG).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_create_video_room"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ouvrir un Salon Vidéo 🎥", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Section Title
        Text(
            text = if (isVideoOnly) "Salons Vidéos en Cours de Diffusion" else "Salons Vocaux en Cours de Diffusion",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(top = 8.dp)
        )

        mockRooms.forEach { (topic, creator, participants) ->
            val isDesignerRoom = creator == "Mirysofficiel"
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFE11D48), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("LIVE 🎙️", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }

                            Text(
                                text = "Créé par @$creator",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                            if (isDesignerRoom) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Badge",
                                    tint = Color(0xFF2196F3),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Group, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                            Text("$participants présents", fontSize = 11.sp, color = Color.Gray)
                        }
                    }

                    Text(
                        text = topic,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )

                    Button(
                        onClick = { viewModel.triggerCallSession(creator, "audio") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Rejoindre en direct", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

// ==========================================
// APP CHARTER / POLICY SECTION
// ==========================================

@Composable
fun AppPolicySection(viewModel: AuraViewModel) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Charte de Communauté & Conditions 📜",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Bienvenue dans l'espace d'expression bienveillant de Mirys. Pour préserver un climat d'échange créatif universel, chaque membre s'engage à respecter notre règlement officiel :",
                fontSize = 12.sp,
                color = Color.LightGray,
                lineHeight = 18.sp
            )

            HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))

            PolicyPoint(
                number = "1",
                title = "Bienveillance & Respect",
                description = "Les insultes, le harcèlement, la diffamation et les propos haineux sont strictement proscrits. Les contrevenants s'exposent à un bannissement irréversible."
            )

            PolicyPoint(
                number = "2",
                title = "Copyright & Propriété Intellectuelle",
                description = "Seules vos créations artistiques originales, pensées de sagesse ou instantanés personnels peuvent être publiés sur le feed."
            )

            PolicyPoint(
                number = "3",
                title = "DMs & Salles Sécurisées",
                description = "Vos échanges privés par messagerie chiffrée, appels audio/vidéo et messagerie vocale respectent la confidentialité absolue des données."
            )

            PolicyPoint(
                number = "4",
                title = "Salons Vocaux Gratuits & Tarification",
                description = "Tout salon audio de groupe est 100% gratuit d'accès pour les 15 premières minutes. Les utilisateurs prolongés participent aux frais d'infrastructure au tarif de ${viewModel.currentSubRegion.streamPriceFormatted} additionnelle."
            )

            PolicyPoint(
                number = "5",
                title = "Compte Officiel Mirysofficiel",
                description = "Le badge bleu d'authenticité et les effets VFX orbitaux d'orbe sont exclusifs au Concepteur de cette application (@Mirysofficiel). Aucun autre utilisateur ne peut simuler ces habilitations privilégiées."
            )
        }
    }
}

@Composable
fun PolicyPoint(number: String, title: String, description: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = number, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
        }
        Column {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
            Text(text = description, fontSize = 11.sp, color = Color.Gray, lineHeight = 16.sp)
        }
    }
}

// ==========================================
// IMMERSIVE CALLING SCREEN OVERLAY (UI)
// ==========================================

@Composable
fun ImmersiveCommunicationHub(viewModel: AuraViewModel) {
    val partner = viewModel.activeCallPartnerHandle ?: "Inconnu"
    val durationSeconds = viewModel.callDurationSeconds
    val activeCallType = viewModel.activeCallType
    val isRoom = activeCallType == "voiceroom" || activeCallType == "videoroom"
    val isVideo = activeCallType == "video" || activeCallType == "videoroom"
    val isMuted = viewModel.isCallMuted
    val speakerOn = viewModel.isCallSpeakerOn
    val context = LocalContext.current

    // Format duration
    val minutes = durationSeconds / 60
    val seconds = durationSeconds % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    // Breathing pulse for visual audio feedback
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Local state for editing pinned message
    var editPinnedMsg by remember { mutableStateOf("") }
    var showPinnedEditor by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xE6020617)) // semi-transparent deep dark background overlay
            .clickable(enabled = false) {} // block background clicks
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header information & premium visual notice
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = Color.Green, modifier = Modifier.size(12.dp))
                        Text(if (isRoom) "Salon Public Modéré [HÔTE] 👑" else "Appel Privé de Bout en Bout", color = Color.Green, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Text(
                    text = when (activeCallType) {
                        "voiceroom" -> "SALON CHAT VOCAL EN COURS 🎙️"
                        "videoroom" -> "SALON DISCUSSION VIDÉO EN COURS 🎥"
                        "video" -> "APPEL VIDÉO PRIVÉ EN COURS 📹"
                        else -> "APPEL AUDIO PRIVÉ EN COURS 📞"
                    },
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )

                Text(
                    text = if (isRoom) "Mon Salon de Discussion" else "@$partner",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Text(
                    text = "$formattedTime - Connecté(s): ${if (isRoom) viewModel.roomGuests.size + 1 else 2}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.8f)
                )

                if (durationSeconds > 600) {
                    Text(
                        text = "Limite gratuite de 10 min dépassée : ${viewModel.currentSubRegion.streamPriceFormatted} actif",
                        fontSize = 10.sp,
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.Bold
                    )
                }

                // Display pinned message if exists
                viewModel.pinnedRoomMessage?.let { pinMsg ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 4.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.PushPin, contentDescription = null, tint = Color(0xFF3DF5FF), modifier = Modifier.size(16.dp))
                            Text(
                                text = "Message Épinglé: \"$pinMsg\"",
                                fontSize = 11.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { viewModel.setRoomPinnedMessage(null) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Unpin", tint = Color.LightGray, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Central holographic avatar view or pseudovideo
            Box(
                modifier = Modifier
                    .size(if (isRoom) 100.dp else 200.dp)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                if (isVideo) {
                    // pseudo video stream simulation
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?q=80&w=300",
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            if (viewModel.isScreenSharing) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.82f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.ScreenShare, contentDescription = null, tint = Color(0xFF22D3EE), modifier = Modifier.size(24.dp))
                                        Text("PARTAGE D'ÉCRAN ACTIF", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .align(Alignment.BottomEnd)
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.Black)
                            ) {
                                // Mini selfie picture
                                Text("Moi", color = Color.White, fontSize = 8.sp, modifier = Modifier.align(Alignment.Center))
                            }
                        }
                    }
                } else {
                    // animated acoustic orb
                    Box(
                        modifier = Modifier
                            .size(if (isRoom) 85.dp * pulseScale else 150.dp * pulseScale)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(if (isRoom) 70.dp else 110.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (viewModel.isScreenSharing) Icons.Default.ScreenShare else Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(if (isRoom) 28.dp else 42.dp)
                        )
                    }
                }
            }

            // ADD HOST SPECIFIC PANEL IF IT IS A VOICE/VIDEO ROOM
            if (isRoom) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.9f)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.2.dp, Brush.horizontalGradient(listOf(Color(0xFF0284C7), Color(0xFF7C3AED)))),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "PANNEAU DE L'HÔTE 👑",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF38BDF8),
                                letterSpacing = 1.sp
                            )
                            Button(
                                onClick = { showPinnedEditor = !showPinnedEditor },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PushPin, contentDescription = null, modifier = Modifier.size(10.dp))
                                    Text("Épingler", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (showPinnedEditor) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = editPinnedMsg,
                                    onValueChange = { editPinnedMsg = it },
                                    label = { Text("Tapez l'annonce", fontSize = 10.sp) },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedLabelColor = Color(0xFF38BDF8),
                                        unfocusedLabelColor = Color.Gray
                                    )
                                )
                                Button(
                                    onClick = {
                                        if (editPinnedMsg.isNotBlank()) {
                                            viewModel.setRoomPinnedMessage(editPinnedMsg)
                                            editPinnedMsg = ""
                                            showPinnedEditor = false
                                        }
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                    modifier = Modifier.height(42.dp)
                                ) {
                                    Text("Confirmer", fontSize = 9.sp)
                                }
                            }
                        }

                        // Host Action Buttons Group 1
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Lock Salon Button
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (viewModel.isRoomLocked) Color(0xFFEF4444).copy(alpha = 0.22f) else Color.White.copy(alpha = 0.08f))
                                    .border(1.dp, if (viewModel.isRoomLocked) Color(0xFFEF4444) else Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                    .clickable { viewModel.toggleRoomLock() }
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (viewModel.isRoomLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                    contentDescription = null,
                                    tint = if (viewModel.isRoomLocked) Color.Red else Color.Green,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (viewModel.isRoomLocked) "Salon Fermé" else "Salon Libre", 
                                    fontSize = 10.sp, 
                                    color = Color.White, 
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Mute all guests Toggle Button
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (viewModel.isEveryoneMuted) Color(0xFFF59E0B).copy(alpha = 0.22f) else Color.White.copy(alpha = 0.08f))
                                    .border(1.dp, if (viewModel.isEveryoneMuted) Color(0xFFF59E0B) else Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                    .clickable { viewModel.toggleMuteEveryone() }
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (viewModel.isEveryoneMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                    contentDescription = null,
                                    tint = if (viewModel.isEveryoneMuted) Color(0xFFF59E0B) else Color.Green,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (viewModel.isEveryoneMuted) "Micro Coupé" else "Micro Libre", 
                                    fontSize = 10.sp, 
                                    color = Color.White, 
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Host Action Buttons Group 2
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Partage Ecran Button
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (viewModel.isScreenSharing) Color(0xFF10B981).copy(alpha = 0.22f) else Color.White.copy(alpha = 0.08f))
                                    .border(1.dp, if (viewModel.isScreenSharing) Color(0xFF10B981) else Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                    .clickable { viewModel.toggleScreenSharing() }
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ScreenShare,
                                    contentDescription = null,
                                    tint = if (viewModel.isScreenSharing) Color.Green else Color.LightGray,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (viewModel.isScreenSharing) "Écran Actif" else "Partager Écran", 
                                    fontSize = 10.sp, 
                                    color = Color.White, 
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Quick add simulated visitor
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                    .clickable {
                                        val possible = listOf("alicia_light", "luna_zen", "lucas_cyber", "sonia_coder", "jean_chess")
                                        val current = viewModel.roomGuests.toList()
                                        val nextToAdd = possible.firstOrNull { !current.contains(it) }
                                        if (nextToAdd != null) {
                                            viewModel.addRoomGuest(nextToAdd)
                                        } else {
                                            Toast.makeText(context, "Tous les invités de la liste sont connectés", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PersonAdd,
                                    contentDescription = null,
                                    tint = Color.Cyan,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Ajouter Invité", 
                                    fontSize = 10.sp, 
                                    color = Color.White, 
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Divider(color = Color.White.copy(alpha = 0.12f), thickness = 1.dp)

                        // Moderator Request list section
                        Text(
                            "DEMANDES DE PARTICIPATION (${viewModel.roomJoinRequests.size}) [MODÉRATEUR] 👑",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFBBF24),
                            letterSpacing = 0.5.sp
                        )

                        if (viewModel.roomJoinRequests.isEmpty()) {
                            Text(
                                "Aucune demande en attente.",
                                fontSize = 9.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            )
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                viewModel.roomJoinRequests.forEach { requestUser ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .background(Color(0xFFFBBF24), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = requestUser.take(1).uppercase(),
                                                    color = Color.Black,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Text("@$requestUser", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        }

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // REJECT ACTION
                                            TextButton(
                                                onClick = { viewModel.rejectRoomRequest(requestUser) },
                                                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444)),
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                modifier = Modifier.height(24.dp)
                                            ) {
                                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text("Refuser", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }

                                            // APPROVE ACTION
                                            Button(
                                                onClick = { viewModel.approveRoomRequest(requestUser) },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                modifier = Modifier.height(24.dp)
                                            ) {
                                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.White)
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text("Autoriser", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Divider(color = Color.White.copy(alpha = 0.12f), thickness = 1.dp)

                        // Guest list label
                        Text(
                            "INVITÉS DANS LE SALON (${viewModel.roomGuests.size})",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.LightGray,
                            letterSpacing = 0.5.sp
                        )

                        if (viewModel.roomGuests.isEmpty()) {
                            Text(
                                "Aucun invité connecté pour le moment. (Déverrouillez le salon ou cliquez sur 'Ajouter Invité')",
                                fontSize = 9.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            )
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                viewModel.roomGuests.forEach { guest ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .background(Color(0xFF38BDF8), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = guest.take(1).uppercase(),
                                                    color = Color.Black,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Text("@$guest", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                            if (viewModel.isEveryoneMuted) {
                                                Icon(Icons.Default.MicOff, contentDescription = "Muted", tint = Color.Red, modifier = Modifier.size(10.dp))
                                            }
                                        }
                                        
                                        // EXPULSER (KICK) BUTTON
                                        TextButton(
                                            onClick = { viewModel.kickRoomGuest(guest) },
                                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444)),
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                            modifier = Modifier.height(24.dp)
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.RemoveCircleOutline, contentDescription = null, modifier = Modifier.size(12.dp))
                                                Text("Expulser", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom control buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mute Action button
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(if (isMuted) Color.White else Color.White.copy(alpha = 0.1f), CircleShape)
                            .clickable { viewModel.isCallMuted = !viewModel.isCallMuted },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Muter",
                            tint = if (isMuted) Color.Black else Color.White
                        )
                    }

                    // Hangup button
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color(0xFFEF4444), CircleShape)
                            .clickable { viewModel.hangUpCall() }
                            .testTag("btn_hang_up"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "Raccrocher",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    // Speaker Action button
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(if (speakerOn) Color.White else Color.White.copy(alpha = 0.1f), CircleShape)
                            .clickable { viewModel.isCallSpeakerOn = !viewModel.isCallSpeakerOn },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (speakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                            contentDescription = "Haut-Parleur",
                            tint = if (speakerOn) Color.Black else Color.White
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// USER SOCIAL PROFILE DIALOG (INSPECTION)
// ==========================================

@Composable
fun SocialUserProfileDialog(
    handle: String,
    viewModel: AuraViewModel,
    onSendMessageClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val isOfficial = handle == "Mirysofficiel" || handle.lowercase() == "mirysofficiel"
    val isFollowing = viewModel.followedHandles.contains(handle) || viewModel.followedHandles.contains(handle.lowercase()) || viewModel.followedHandles.contains(handle.replaceFirstChar { it.uppercase() })
    val subscribersCount = viewModel.getSubscribersCount(handle)
    val followingCount = viewModel.getFollowingCount(handle)
    val isOnline = viewModel.isUserOnline(handle)
    val dialogAuthorPosts = viewModel.postsList.filter { it.authorHandle.lowercase() == handle.lowercase() }

    val badgeName = when (handle.lowercase()) {
        "mirysofficiel" -> "Designer"
        "rayanium" -> "Legendary"
        "alanementii" -> "Galaxy"
        "sophie_zen" -> "Crystal"
        "lucas_heart" -> "Neon"
        "sonia_coder" -> "Flame"
        else -> null
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF070F1E)),
            border = BorderStroke(1.2.dp, Color(0xFF1E293B)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .testTag("profile_inspect_dialog_$handle")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top header with Online/Offline real indicator & direct tester toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isOnline) Color(0xFF10B981) else Color(0xFF6B7280))
                        )
                        Text(
                            text = if (isOnline) "EN LIGNE 🟢" else "HORS LIGNE 🔴",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isOnline) Color(0xFF10B981) else Color(0xFF9CA3AF)
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clickable {
                                viewModel.toggleUserOnlineStatus(handle)
                            }
                            .background(Color(0xFF111827), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Simuler État 🔌", fontSize = 9.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }

                // Profile Avatar with high fidelity dynamic orb gravity VFX orbiting
                ProfileVfxWrapper(
                    badgeName = badgeName,
                    isOfficial = isOfficial,
                    modifier = Modifier.size(92.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = handle.take(2).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 22.sp
                        )
                    }
                }

                // Name & Handle
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (isOfficial) "Mirysofficiel" else handle.replaceFirstChar { it.uppercase() },
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        if (isOfficial) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Sceau Officiel",
                                tint = Color(0xFF2196F3),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Text(
                        text = "@$handle",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                // Statistics card (subscribers & following explicitly layouted)
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1527)),
                    border = BorderStroke(1.dp, Color(0xFF1F2937)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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
                        
                        Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0xFF1F2937)))

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

                        Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0xFF1F2937)))

                        val publicationsCount = if (isOfficial) 8 else dialogAuthorPosts.size
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

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                // Bio
                Text(
                    text = if (isOfficial) {
                        "Bonjour ! Je suis le concepteur officiel de Mirys. Suivez-moi pour recevoir nos annonces officielles, nouveautés VFX et événements magiques ! ✨🧘"
                    } else "Membre passionné de la communauté Mirys. Adepte des salons vocaux et des moments de partage sages.",
                    fontSize = 12.sp,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )

                // Premium VFX Details Column
                if (badgeName != null) {
                    val price = when (badgeName) {
                        "Flame" -> "350"
                        "Galaxy" -> "800"
                        "Crystal" -> "1500"
                        "Neon" -> "2200"
                        "Legendary" -> "4500"
                        else -> "Exclusif"
                    }
                    val beautyRating = when (badgeName) {
                        "Flame" -> "★★★☆☆ (Beauté Modérée, Complexité Simple)"
                        "Galaxy" -> "★★★★☆ (Beauté Cosmique, Orbites Cosmiques)"
                        "Crystal" -> "★★★★☆ (Éclats Prisme, Transparence Cristalline)"
                        "Neon" -> "★★★★★ (Glow Pulsant Rose, Cyberpunk)"
                        "Legendary" -> "★★★★★ (Couronne Dorée, Résonance Suprême)"
                        else -> "★★★★★★ (Créateur Officiel - Sceau Divin)"
                    }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131F37)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("✨ Spécifications de son VFX Premium :", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            Text("Modèle : $badgeName Effect", fontSize = 11.sp, color = Color.LightGray)
                            Text("Esthétique : $beautyRating", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            Text("Coût Boutique : ${if (price == "Exclusif") "Exclusif unique" else "$price 🪙"}", fontSize = 11.sp, color = Color.Yellow)
                        }
                    }
                }

                // Proposition/Suggestion de Follow (Recommendation dynamically offered based on common aura match)
                if (!isFollowing) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)), // deep indigo/purple aura
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("follow_suggestion_banner"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Recommend,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(
                                    text = "Proposition d'abonnement ✨",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Compatibilité Mirys très forte ! Suivez ce profil pour enrichir votre flux quotidien.",
                                    fontSize = 10.sp,
                                    color = Color.LightGray,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }

                // TWO EXTREMELY BEAUTIFUL WELL-CONFIGURED BUTTONS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val subscriptionLabel = if (isFollowing) "Se désabonner 🔕" else "S'abonner 🔔"
                    
                    // Button 1: Beautiful custom configured Subscribe button with glowing outline or fill
                    Button(
                        onClick = { viewModel.toggleFollowUser(handle) },
                        modifier = Modifier
                            .weight(1.2f)
                            .height(44.dp)
                            .testTag("follow_user_button"),
                        shape = RoundedCornerShape(22.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFollowing) Color(0xFF1E293B) else MaterialTheme.colorScheme.primary
                        ),
                        border = if (isFollowing) BorderStroke(1.2.dp, Color(0xFF334155)) else null
                    ) {
                        Text(
                            text = subscriptionLabel,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }

                    // Button 2: Beautiful custom configured Message button with dark gradient borders
                    Button(
                        onClick = onSendMessageClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("dm_user_button"),
                        shape = RoundedCornerShape(22.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0F172A)
                        ),
                        border = BorderStroke(1.2.dp, Color(0xFF3B82F6).copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFF3B82F6)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Message",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF3B82F6)
                        )
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                // Profile Administrative actions section
                Text(
                    text = "Actions de protection 🛡️",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Start)
                )

                val isBlocked = viewModel.blockedHandles.contains(handle)
                val isRestricted = viewModel.restrictedHandles.contains(handle)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // 1. Signaler
                    Button(
                        onClick = {
                            viewModel.reportUser(handle, "Comportement inapproprié ou propos malveillants")
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("report_user_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(text = "Signaler", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    // 2. Bloquer / Débloquer
                    Button(
                        onClick = {
                            if (isBlocked) {
                                viewModel.unblockUser(handle)
                            } else {
                                viewModel.blockUser(handle)
                            }
                        },
                        modifier = Modifier
                            .weight(1.1f)
                            .testTag("block_user_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isBlocked) Color(0xFF2E7D32) else Color(0xFF4E342E)
                        ),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = if (isBlocked) Icons.Default.LockOpen else Icons.Default.Block,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (isBlocked) "Débloquer" else "Bloquer",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // 3. Masquer / Démasquer
                    Button(
                        onClick = {
                            if (isRestricted) {
                                viewModel.unrestrictUser(handle)
                            } else {
                                viewModel.restrictUser(handle)
                            }
                        },
                        modifier = Modifier
                            .weight(1.1f)
                            .testTag("restrict_user_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRestricted) Color(0xFF1565C0) else Color(0xFF37474F)
                        ),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = if (isRestricted) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (isRestricted) "Démasquer" else "Masquer",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // ... ET BIEN PLUS Menu!
                var showAddonActions by remember { mutableStateOf(false) }
                OutlinedButton(
                    onClick = { showAddonActions = !showAddonActions },
                    modifier = Modifier.fillMaxWidth().testTag("btn_more_profile_actions"),
                    border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (showAddonActions) "... Moins d'options 🔼" else "... Et bien plus 🔽",
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )
                }

                if (showAddonActions) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF131F37), RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Restrict action
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.restrictUser(handle) }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.RemoveCircle, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                            Column {
                                Text("Restreindre le compte", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Masquer de façon invisible. Envoyé à alanementii73@gmail.com", fontSize = 9.sp, color = Color.Gray)
                            }
                        }

                        // Share action
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.shareUserProfile(handle) }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                            Column {
                                Text("Partager ce profil", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Générer un lien d'export de profil", fontSize = 9.sp, color = Color.Gray)
                            }
                        }

                        // Aura Sync action
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.synchronizeVibratoryAura(handle) }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Sync, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                            Column {
                                Text("Consulter l'Alignement Mirys", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Analyser la synergie cosmique de vos pensées", fontSize = 9.sp, color = Color.Gray)
                            }
                        }
                    }
                }

                // List of Publications inside profile dialog
                if (dialogAuthorPosts.isNotEmpty()) {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(top = 8.dp))
                    Text(
                        text = "Publications de @$handle 📝",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    
                    dialogAuthorPosts.forEach { post ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF131F37)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = post.content,
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                                if (post.photos.isNotEmpty()) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        post.photos.take(3).forEach { photoUrl ->
                                            AsyncImage(
                                                model = photoUrl,
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(50.dp)
                                                    .clip(RoundedCornerShape(4.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Icon(imageVector = Icons.Default.Favorite, contentDescription = null, tint = Color.Red, modifier = Modifier.size(12.dp))
                                        Text("${post.likesCount} J'aime", fontSize = 10.sp, color = Color.Gray)
                                    }
                                    Text("2 comm.", fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }

                // Close button
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_inspect_dialog")
                ) {
                    Text("Fermer", color = Color.LightGray, fontSize = 12.sp)
                }
            }
        }
    }
}

// ==========================================
// DISTINCTIVE SUB-COMPONENTS FOR STORIES FEATURE
// ==========================================

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
                        items(demoPresets) { (label, isVid, url) ->
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

