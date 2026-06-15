package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.viewmodel.AuraViewModel

@Composable
fun VoiceRoomsTab(viewModel: AuraViewModel, isVideoOnly: Boolean = false) {
    val localContext = LocalContext.current
    val mockRooms = emptyList<Triple<String, String, Int>>()

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
                    }
                }
            }
        }
    }
}
