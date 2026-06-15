package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ProfileVfxWrapper
import com.example.ui.viewmodel.AuraViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DirectMessagesTab(
    viewModel: AuraViewModel,
    activePartnerHandle: String?,
    onSelectPartner: (String?) -> Unit
) {
    val localContext = LocalContext.current
    val mockPartners = emptyList<Pair<String, String>>()

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
                                imageVector = Icons.AutoMirrored.Filled.Chat,
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
            contract = ActivityResultContracts.PickVisualMedia()
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
            contract = ActivityResultContracts.PickVisualMedia()
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
            }
        }
    }
}
