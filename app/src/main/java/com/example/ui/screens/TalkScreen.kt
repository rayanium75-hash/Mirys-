package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ProfileVfxWrapper
import com.example.ui.viewmodel.AuraViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ==========================================
// TALK SCREEN — Messagerie, Salons & Appels
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TalkScreen(
    viewModel: AuraViewModel,
    modifier: Modifier = Modifier
) {
    var activeSubTab by remember { mutableStateOf("messages") }
    var activePartnerHandle by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("talk_screen")
    ) {
        // ─── TOP BANNER ──────────────────────────────────────────────
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
                        text = "Talk",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Messages, salons vocaux & appels",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
                Icon(
                    imageVector = Icons.Default.Forum,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // ─── SUB-TABS ────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF060D1A))
                .padding(vertical = 8.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            data class TalkTab(val id: String, val label: String)
            val tabs = listOf(
                TalkTab("messages", "Messages"),
                TalkTab("rooms",    "Salons Vocaux"),
                TalkTab("video",    "Salons Vidéo"),
                TalkTab("calls",    "Appels")
            )
            tabs.forEach { tab ->
                val isSelected = activeSubTab == tab.id
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(30.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else Color(0xFF0F1B2F)
                        )
                        .clickable {
                            activeSubTab = tab.id
                            if (tab.id != "messages") activePartnerHandle = null
                            viewModel.triggerBeep(3)
                        }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                        .testTag("talk_subtab_${tab.id}")
                ) {
                    Text(
                        text = tab.label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else Color.Gray
                    )
                }
            }
        }

        // ─── CONTENT ─────────────────────────────────────────────────
        Box(modifier = Modifier.fillMaxSize().weight(1f)) {
            when (activeSubTab) {
                "messages" -> TalkDirectMessagesTab(
                    viewModel = viewModel,
                    activePartnerHandle = activePartnerHandle,
                    onSelectPartner = { activePartnerHandle = it }
                )
                "rooms"    -> TalkVoiceRoomsTab(viewModel = viewModel)
                "video"    -> TalkVideoRoomsTab(viewModel = viewModel)
                "calls"    -> TalkCallsTab(viewModel = viewModel)
            }
        }
    }
}

// ==========================================
// ONGLET MESSAGES DIRECTS
// ==========================================

@Composable
fun TalkDirectMessagesTab(
    viewModel: AuraViewModel,
    activePartnerHandle: String?,
    onSelectPartner: (String?) -> Unit
) {
    val localContext = LocalContext.current
    val mockPartners = listOf(
        Triple("Mirysofficiel",  "Concepteur Officiel de l'application",  true),
        Triple("rayanium",       "Expert IA & Design",                     false),
        Triple("sonia_coder",    "Développeuse & passionnée de bien-être", false),
        Triple("chess_club_yaounde", "Club Échecs de Yaoundé",             false),
        Triple("mirys_team",     "Équipe Mirys",                           true)
    )

    if (activePartnerHandle == null) {
        // ── Liste des conversations ──
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF030712))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = "Conversations",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Sélectionnez un contact pour démarrer ou reprendre une conversation.",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            items(mockPartners) { (handle, description, isOfficial) ->
                Card(
                    onClick = { onSelectPartner(handle); viewModel.triggerBeep(3) },
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
                            ProfileVfxWrapper(
                                badgeName = if (isOfficial) "designer_badge" else null,
                                isOfficial = isOfficial,
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
                                    if (isOfficial) {
                                        Icon(
                                            imageVector = Icons.Default.Verified,
                                            contentDescription = "Compte vérifié",
                                            tint = Color(0xFF2196F3),
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                                Text(text = description, fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Ouvrir conversation",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    } else {
        // ── Conversation individuelle ──
        val dmsList   = viewModel.directMessagesMap[activePartnerHandle] ?: emptyList()
        var inputText by remember { mutableStateOf("") }
        val listState = rememberLazyListState()
        val scope     = rememberCoroutineScope()

        LaunchedEffect(dmsList.size) {
            if (dmsList.isNotEmpty()) listState.animateScrollToItem(dmsList.size - 1)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF030712))
        ) {
            // Header
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
                    IconButton(onClick = { onSelectPartner(null) }, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = Color.White
                        )
                    }
                    val isOfficial = activePartnerHandle == "Mirysofficiel" || activePartnerHandle == "mirys_team"
                    ProfileVfxWrapper(
                        badgeName = if (isOfficial) "designer_badge" else null,
                        isOfficial = isOfficial,
                        modifier = Modifier.size(36.dp)
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = activePartnerHandle,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                            if (isOfficial) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Vérifié",
                                    tint = Color(0xFF2196F3),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                        Text(text = "En ligne", fontSize = 9.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { viewModel.triggerCallSession(activePartnerHandle, "audio") },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = "Appel vocal", tint = Color.LightGray, modifier = Modifier.size(20.dp))
                    }
                    IconButton(
                        onClick = { viewModel.triggerCallSession(activePartnerHandle, "video") },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Videocam, contentDescription = "Appel vidéo", tint = Color.LightGray, modifier = Modifier.size(20.dp))
                    }
                }
            }

            // Messages list
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
                            modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Début de la conversation avec @$activePartnerHandle.",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                items(dmsList) { msg ->
                    val isMine = msg.senderHandle == viewModel.userHandle || msg.senderHandle == "mon_compte"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
                    ) {
                        Card(
                            shape = RoundedCornerShape(
                                topStart = 16.dp, topEnd = 16.dp,
                                bottomStart = if (isMine) 16.dp else 2.dp,
                                bottomEnd   = if (isMine) 2.dp  else 16.dp
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isMine) MaterialTheme.colorScheme.primary else Color(0xFF1E293B)
                            ),
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                if (msg.isVoiceNote) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayCircle,
                                            contentDescription = "Écouter",
                                            tint = Color.White,
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clickable {
                                                    Toast.makeText(localContext, "Lecture du message vocal...", Toast.LENGTH_SHORT).show()
                                                }
                                        )
                                        Column {
                                            Text("Message Audio", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            Text("0:08 · Cliquez pour écouter", fontSize = 9.sp, color = Color.White.copy(alpha = 0.7f))
                                        }
                                    }
                                } else {
                                    Text(text = msg.content, fontSize = 13.sp, color = Color.White)
                                }
                                Text(
                                    text = msg.timestamp,
                                    fontSize = 9.sp,
                                    color = Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Input bar
            Surface(
                color = Color(0xFF0D1527),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Voice note button
                    IconButton(
                        onClick = {
                            viewModel.sendDirectMessage(activePartnerHandle, "", isVoiceNote = true, voiceNoteDuration = 8)
                            Toast.makeText(localContext, "Note vocale envoyée !", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Mic, contentDescription = "Note vocale", tint = Color.Gray)
                    }

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Message...", fontSize = 13.sp, color = Color.Gray) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor   = Color(0xFF1E293B),
                            unfocusedContainerColor = Color(0xFF1E293B),
                            focusedBorderColor      = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor    = Color.Transparent
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (inputText.isNotBlank()) {
                                    viewModel.sendDirectMessage(activePartnerHandle, inputText)
                                    inputText = ""
                                }
                            }
                        )
                    )

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                viewModel.sendDirectMessage(activePartnerHandle, inputText)
                                inputText = ""
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                if (inputText.isNotBlank()) MaterialTheme.colorScheme.primary else Color(0xFF1E293B),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Envoyer",
                            tint = if (inputText.isNotBlank()) Color.White else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// ONGLET SALONS VOCAUX
// ==========================================

@Composable
fun TalkVoiceRoomsTab(viewModel: AuraViewModel) {
    val localContext = LocalContext.current
    val mockRooms = listOf(
        Triple("Café des esprits libres", "Mirysofficiel", 14),
        Triple("Méditation & Silence profond", "sophie_zen", 8),
        Triple("Décryptage Finance & Tech", "lucas_heart", 23)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030712))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1527)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Default.Mic, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Text(
                        text = "Salons Vocaux en Direct",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = "Lancez un salon public. Les 15 premières minutes sont gratuites. Au-delà, un tarif de 0,49 € par minute s'applique.",
                    fontSize = 11.sp,
                    color = Color.LightGray,
                    lineHeight = 16.sp
                )
            }
        }

        Button(
            onClick = {
                viewModel.addNewPost(
                    content = "J'ai ouvert un Salon Vocal gratuit en direct ! Rejoignez-moi pour discuter !",
                    photos = emptyList(),
                    videoUrl = null,
                    videoDuration = null
                )
                Toast.makeText(localContext, "Salon Vocal ouvert ! Annonce publiée automatiquement.", Toast.LENGTH_LONG).show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("btn_create_audio_room"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Créer un Salon Vocal", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        Text(
            text = "Salons actifs",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        mockRooms.forEach { (topic, creator, participants) ->
            val isDesignerRoom = creator == "Mirysofficiel"
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFE11D48), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("LIVE", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                            }
                            Text("@$creator", fontSize = 11.sp, color = Color.Gray)
                            if (isDesignerRoom) {
                                Icon(imageVector = Icons.Default.Verified, contentDescription = null, tint = Color(0xFF2196F3), modifier = Modifier.size(13.dp))
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(imageVector = Icons.Default.Group, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                            Text("$participants", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                    Text(text = topic, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                    Button(
                        onClick = { viewModel.triggerCallSession(creator, "voiceroom") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A5F)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Headset, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Rejoindre", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// ONGLET SALONS VIDÉO
// ==========================================

@Composable
fun TalkVideoRoomsTab(viewModel: AuraViewModel) {
    val localContext = LocalContext.current
    val mockRooms = listOf(
        Triple("Session créative Mirys", "Mirysofficiel", 7),
        Triple("Atelier dessin numérique", "rayanium", 4)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030712))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1527)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Default.Videocam, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(20.dp))
                    Text(
                        text = "Salles de Discussion Vidéo",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFFD32F2F)
                    )
                }
                Text(
                    text = "Échangez en face à face dans des salles vidéo interactives. Accessible à tous les membres.",
                    fontSize = 11.sp,
                    color = Color.LightGray,
                    lineHeight = 16.sp
                )
            }
        }

        Button(
            onClick = {
                viewModel.addNewPost(
                    content = "Salle de Chat Vidéo ouverte ! Rejoignez l'arène vidéo en direct pour échanger en face à face !",
                    photos = emptyList(),
                    videoUrl = null,
                    videoDuration = null
                )
                Toast.makeText(localContext, "Salle Vidéo ouverte ! Annonce publiée automatiquement.", Toast.LENGTH_LONG).show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("btn_create_video_room"),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Créer une Salle Vidéo", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        Text(text = "Salles actives", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)

        mockRooms.forEach { (topic, creator, participants) ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFD32F2F), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("VIDEO", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                            }
                            Text("@$creator", fontSize = 11.sp, color = Color.Gray)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(imageVector = Icons.Default.Group, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                            Text("$participants", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                    Text(text = topic, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                    Button(
                        onClick = { viewModel.triggerCallSession(creator, "videoroom") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3D1010)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Rejoindre", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// ONGLET APPELS RÉCENTS
// ==========================================

@Composable
fun TalkCallsTab(viewModel: AuraViewModel) {
    val recentCalls = listOf(
        Triple("Mirysofficiel", "audio",  "Il y a 2h"),
        Triple("sonia_coder",   "video",  "Hier"),
        Triple("rayanium",      "audio",  "Il y a 3 jours")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030712))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Appels récents",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        items(recentCalls) { (handle, type, time) ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1527)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E293B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (type == "video") Icons.Default.Videocam else Icons.Default.Call,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(text = handle, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                            Text(
                                text = if (type == "video") "Appel Vidéo · $time" else "Appel Audio · $time",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    IconButton(onClick = { viewModel.triggerCallSession(handle, type) }) {
                        Icon(
                            imageVector = if (type == "video") Icons.Default.Videocam else Icons.Default.Call,
                            contentDescription = "Rappeler",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
