package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.AuraViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedSocialScreen(viewModel: AuraViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val posts = viewModel.postsList
    val context = LocalContext.current

    // Dialog state for full-screen media viewing
    var lightboxPhotoUrl by remember { mutableStateOf<String?>(null) }
    var lightboxVideoUrl by remember { mutableStateOf<String?>(null) }
    
    // Social interactions
    var activeUserProfileHandle by remember { mutableStateOf<String?>(null) }
    var activeChatPartnerHandle by remember { mutableStateOf<String?>(null) }

    // Synchronize local UI state with ViewModel global state for navigation
    LaunchedEffect(viewModel.activeUserProfileHandle) {
        activeUserProfileHandle = viewModel.activeUserProfileHandle
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                CenterAlignedTopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                "Mirys Moments",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                letterSpacing = (-0.5).sp
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { Toast.makeText(context, "Recherche de moments...", Toast.LENGTH_SHORT).show() }) {
                            Icon(Icons.Default.Search, contentDescription = "Rechercher")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )

                // Navigation Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 16.dp,
                    containerColor = Color.Transparent,
                    divider = {},
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = MaterialTheme.colorScheme.primary,
                            height = 3.dp
                        )
                    }
                ) {
                    val tabs = listOf("Moments ✨", "Messages 💬", "Salons Vocaux 🎙️", "Salons Vidéos 🎥", "Charte ⚖️")
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 13.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { Toast.makeText(context, "Créer un nouveau post...", Toast.LENGTH_SHORT).show() },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nouveau Post")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (selectedTab) {
                0 -> { // Moments Feed
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            StoriesSection(viewModel = viewModel)
                        }

                        items(posts) { post ->
                            SocialPostCard(
                                post = post,
                                viewModel = viewModel,
                                onLikeClick = { viewModel.toggleLike(post.id) },
                                onShareClick = { Toast.makeText(context, "Partage du post...", Toast.LENGTH_SHORT).show() },
                                onCommentClick = { Toast.makeText(context, "Ouverture des commentaires...", Toast.LENGTH_SHORT).show() },
                                onAnalyzeClick = { viewModel.analyzePostViralPotential(post.id) },
                                onPhotoClick = { url -> lightboxPhotoUrl = url },
                                onPlayClick = { url, _ -> lightboxVideoUrl = url }
                            )
                        }
                    }
                }
                1 -> { // Direct Messages
                    DirectMessagesTab(
                        viewModel = viewModel,
                        activePartnerHandle = activeChatPartnerHandle,
                        onSelectPartner = { activeChatPartnerHandle = it }
                    )
                }
                2 -> { // Voice Rooms
                    VoiceRoomsTab(viewModel = viewModel, isVideoOnly = false)
                }
                3 -> { // Video Rooms
                    VoiceRoomsTab(viewModel = viewModel, isVideoOnly = true)
                }
                4 -> { // Charte / Policy
                    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        AppPolicySection()
                    }
                }
            }

            // Overlay Dialogs
            activeUserProfileHandle?.let { handle ->
                SocialUserProfileDialog(
                    handle = handle,
                    viewModel = viewModel,
                    onDismiss = { viewModel.closeUserProfile() },
                    onChatClick = { partner ->
                        activeChatPartnerHandle = partner
                        selectedTab = 1
                        viewModel.closeUserProfile()
                    }
                )
            }

            lightboxPhotoUrl?.let { url ->
                PhotoLightboxDialog(photoUrl = url, onDismiss = { lightboxPhotoUrl = null })
            }

            lightboxVideoUrl?.let { url ->
                VideoPlayerDialog(videoUrl = url, onDismiss = { lightboxVideoUrl = null })
            }

            // Global active call overlay
            if (viewModel.activeCallPartnerHandle != null) {
                ImmersiveCommunicationHub(viewModel = viewModel)
            }
        }
    }
}
