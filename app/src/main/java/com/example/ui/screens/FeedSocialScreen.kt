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

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.VideoView
import android.widget.MediaController
import android.net.Uri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedSocialScreen(
    viewModel: AuraViewModel,
    onNavigateToGames: () -> Unit = {},
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
    val posts = viewModel.postsList
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
    var isChattingPartnerHandle by remember { mutableStateOf<String?>(null) }

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

                // Small circular coins showcase + Games shortcut
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
                    IconButton(
                        onClick = onNavigateToGames,
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer,
                                RoundedCornerShape(12.dp)
                            )
                            .testTag("feed_games_shortcut")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SportsEsports,
                            contentDescription = "Aller aux Jeux",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // Pill SubTabs Selector Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF060D1A))
                .padding(vertical = 10.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val subTabs = listOf(
                "moments" to "Moments & Fil",
                "policy"  to "Charte & Politique"
            )
            subTabs.forEach { (tabId, label) ->
                val isSelected = feedActiveSubTab == tabId
                Box(
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
                        .testTag("feed_subtab_$tabId")
                ) {
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
                    AppPolicySection()
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
                feedActiveSubTab = "dms"
                isChattingPartnerHandle = activeUserProfileHandle
            },
            onDismiss = { viewModel.closeUserProfile() }
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
                                    }
                                    Text(
                                        text = description,
                                        fontSize = 11.sp,
                                        color = Color.Gray
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
        val listState = rememberLazyListState()

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
                        Text(
                            text = "En ligne",
                            fontSize = 9.sp,
                            color = Color.Green,
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
                                    // Audio Note styling card
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayCircle,
                                            contentDescription = "Play note",
                                            tint = Color.White,
                                            modifier = Modifier.size(28.dp).clickable {
                                                Toast.makeText(localContext, "Lecture du message vocal...", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                        Column {
                                            Text("Message Audio", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Text("0:08 • Cliquez pour écouter", fontSize = 9.sp, color = Color.White.copy(alpha = 0.7f))
                                        }
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Send voice note trigger button
                    IconButton(
                        onClick = {
                            viewModel.sendDirectMessage(activePartnerHandle, "", isVoiceNote = true, voiceNoteDuration = 8)
                            Toast.makeText(localContext, "Message Vocal de 8s envoyé !", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Envoyer Note Vocale",
                            tint = Color.LightGray
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
}

// ==========================================
// VOICE ROOMS TAB & SUB-COMPONENTS
// ==========================================

@Composable
fun VoiceRoomsTab(viewModel: AuraViewModel) {
    val localContext = LocalContext.current
    val mockRooms = listOf(
        Triple("Café des esprits libres ☕", "Mirysofficiel", 14),
        Triple("Méditation & Silence profond 🧘", "sophie_zen", 8),
        Triple("Décryptage Astro-Finance 💰", "lucas_heart", 23)
    )

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
                    text = "Salons de Conversation Audio & Vidéo 🎙️",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Lancez un salon public. Les 15 premières minutes sont gratuites pour tous les membres ! Au-delà, un tarif minime de 0,49€ par minute s'applique pour rémunérer la bande passante et l'hébergeur.",
                    fontSize = 11.sp,
                    color = Color.LightGray,
                    lineHeight = 16.sp
                )
            }
        }

        // Room generator quick actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    // Create an audio room and share automatically to the moments feed!
                    viewModel.addNewPost(
                        content = "🚨 J'ai ouvert un nouveau Salon de Chat Vocal gratuit live ! Rejoignez-moi pour discuter en direct ! 🎙️✨",
                        photos = emptyList(),
                        videoUrl = null,
                        videoDuration = null
                    )
                    Toast.makeText(localContext, "Salon Vocal ouvert ! Publication postée automatiquement dans les moments !", Toast.LENGTH_LONG).show()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("btn_create_audio_room"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Salon Vocal", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    // Create a video room and share automatically to the moments feed!
                    viewModel.addNewPost(
                        content = "🎥 Salon de Chat Vidéo ouvert ! Entrez dans l'arène vidéo en direct pour échanger en face à face ! 🌐👇",
                        photos = emptyList(),
                        videoUrl = null,
                        videoDuration = null
                    )
                    Toast.makeText(localContext, "Salle de Chat Vidéo ouverte ! Annonce postée automatiquement !", Toast.LENGTH_LONG).show()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("btn_create_video_room"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Salle Vidéo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Section Title
        Text(
            text = "Salons en Cours de Diffusion",
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
                                Text("LIVE", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
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
fun AppPolicySection() {
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
                description = "Tout salon audio de groupe est 100% gratuit d'accès pour les 15 premières minutes. Les utilisateurs prolongés participent aux frais d'infrastructure au tarif de 0,49€ par minute additionnelle."
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
    val isVideo = viewModel.activeCallType == "video" || viewModel.activeCallType == "videoroom"
    val isMuted = viewModel.isCallMuted
    val speakerOn = viewModel.isCallSpeakerOn

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xE6020617)) // semi-transparent deep dark background overlay
            .clickable(enabled = false) {} // block background clicks
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header information & premium visual notice
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 28.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = Color.Green, modifier = Modifier.size(12.dp))
                        Text("Appel Chiffré de Bout en Bout", color = Color.Green, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Text(
                    text = if (isVideo) "APPEL VIDÉO EN COURS" else "APPEL AUDIO EN COURS",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )

                Text(
                    text = "@$partner",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Text(
                    text = formattedTime,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.8f)
                )

                if (durationSeconds > 600) {
                    Text(
                        text = "Limite gratuite de 10 min dépassée : 0,49€/min actif",
                        fontSize = 10.sp,
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Central holographic avatar view or pseudovideo
            Box(
                modifier = Modifier
                    .size(200.dp)
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
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black)
                            ) {
                                // Mini selfie picture
                                Text("Moi", color = Color.White, fontSize = 9.sp, modifier = Modifier.align(Alignment.Center))
                            }
                        }
                    }
                } else {
                    // animated acoustic orb
                    Box(
                        modifier = Modifier
                            .size(150.dp * pulseScale)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(42.dp)
                        )
                    }
                }
            }

            // Bottom control buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(bottom = 24.dp)
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
    val isOfficial = handle == "Mirysofficiel"
    val isFollowing = viewModel.followedHandles.contains(handle)
    val subscribersCount = viewModel.profileSubscribersCount[handle] ?: 0

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
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1527)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .testTag("profile_inspect_dialog_$handle")
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
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

                // Statistics rows (subscribers / publications)
                val authorPosts = viewModel.postsList.filter { it.authorHandle.lowercase() == handle.lowercase() }
                val publicationsCount = if (isOfficial) 8 else authorPosts.size

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "$subscribersCount", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color.White)
                        Text(text = "Abonnés", fontSize = 10.sp, color = Color.Gray)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "$publicationsCount", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color.White)
                        Text(text = "Publications", fontSize = 10.sp, color = Color.Gray)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = if (isOfficial) "Concepteur App" else "Membre", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                        Text(text = "Statut", fontSize = 10.sp, color = Color.Gray)
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
                        "Legendary" -> "★★★★★ (Couronne Dorée, Aura Suprême)"
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

                // Interactive buttons (S'abonner / Message)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val subscriptionLabel = if (isFollowing) "Se désabonner 🔕" else "S'abonner 🔔"
                    Button(
                        onClick = { viewModel.toggleFollowUser(handle) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("follow_user_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFollowing) Color.Gray.copy(alpha = 0.3f) else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(text = subscriptionLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onSendMessageClick,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dm_user_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
                    ) {
                        Icon(imageVector = Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Message", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                // List of Publications inside profile dialog
                if (authorPosts.isNotEmpty()) {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(top = 8.dp))
                    Text(
                        text = "Publications de @$handle 📝",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    
                    authorPosts.forEach { post ->
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

