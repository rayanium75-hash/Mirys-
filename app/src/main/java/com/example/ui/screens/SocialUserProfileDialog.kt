package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.components.ProfileVfxWrapper
import com.example.ui.viewmodel.AuraViewModel

@Composable
fun SocialUserProfileDialog(
    handle: String,
    viewModel: AuraViewModel,
    onDismiss: () -> Unit,
    onChatClick: (String) -> Unit
) {
    val isOfficial = handle == "Mirysofficiel"
    val isFollowing = viewModel.followedHandles.contains(handle)
    val isBlocked = viewModel.blockedHandles.contains(handle)
    val isRestricted = viewModel.restrictedHandles.contains(handle)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Large Avatar with badge
                ProfileVfxWrapper(
                    badgeName = if (isOfficial) "designer_badge" else null,
                    isOfficial = isOfficial,
                    modifier = Modifier.size(90.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF334155)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = handle.take(2).uppercase(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = Color.White
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "@$handle",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        if (isOfficial) {
                            Icon(imageVector = Icons.Default.Verified, contentDescription = null, tint = Color(0xFF2196F3), modifier = Modifier.size(20.dp))
                        }
                    }
                    Text(
                        text = if (isOfficial) "Compte Officiel Mirys ✨" else "Membre de la Communauté",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("1.2k", fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Abonnés", fontSize = 10.sp, color = Color.Gray)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("450", fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Abonnements", fontSize = 10.sp, color = Color.Gray)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("8.4k", fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Aura Score", fontSize = 10.sp, color = Color.Gray)
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                // Actions
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            if (isFollowing) viewModel.unfollowUser(handle) else viewModel.followUser(handle)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFollowing) Color.DarkGray else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(imageVector = if (isFollowing) Icons.Default.PersonRemove else Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isFollowing) "Ne plus suivre" else "Suivre ce membre")
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = { onChatClick(handle) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Icon(imageVector = Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Message")
                        }
                        OutlinedButton(
                            onClick = { viewModel.triggerCallSession(handle, "audio") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Appeler")
                        }
                    }
                }

                // Security/Safety Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            if (isRestricted) viewModel.unrestrictUser(handle) else viewModel.restrictUser(handle)
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = if (isRestricted) Color.Green else Color.Yellow)
                    ) {
                        Text(if (isRestricted) "Lever Restriction" else "Restreindre", fontSize = 11.sp)
                    }
                    TextButton(
                        onClick = {
                            if (isBlocked) viewModel.unblockUser(handle) else viewModel.blockUser(handle)
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                    ) {
                        Text(if (isBlocked) "Débloquer" else "Bloquer le profil", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
