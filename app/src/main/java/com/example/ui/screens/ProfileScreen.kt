package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import com.example.ui.viewmodel.AuraViewModel
import com.example.ui.components.* // Core reusable modules like ProfileEditorSection, SettingsSection, SubscriptionSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: AuraViewModel,
    modifier: Modifier = Modifier
) {
    val localContext = LocalContext.current
    var activeTab by remember { mutableStateOf("profile") } // "profile", "shop", "settings", "premium"
    
    // Hold local temporary states for editing
    var localUsername by remember { mutableStateOf(viewModel.username) }
    var localHandle by remember { mutableStateOf(viewModel.userHandle) }

    // Synchronize local states when viewModel fields change externally
    LaunchedEffect(viewModel.username, viewModel.userHandle) {
        localUsername = viewModel.username
        localHandle = viewModel.userHandle
    }

    // Preset configurations for avatars and filters
    val presets = listOf(
        "Default" to "Défaut Silencieux",
        "Nebula" to "Astéroïde Cosmique",
        "Golden Hero" to "Souverain Doré",
        "Cyber Punk" to "Hacker Synthwave",
        "Cosmic" to "Nébuleuse Émeraude"
    )

    val filters = listOf(
        "Normal" to "Normal",
        "Noir & Blanc" to "N&B",
        "Vintage" to "Vintage ✨",
        "Bleu Cyber" to "Cyber 🔵",
        "Neon Rose" to "Néon 🟣"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF03070E)) // Consistent ultra dark space atmosphere
            .testTag("profile_screen")
    ) {
        // High fidelity Profile Tab Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF08101C))
                .padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val tabItems = listOf(
                "profile" to Pair("Profil", Icons.Outlined.Person),
                "shop" to Pair("Boutique 🪙", Icons.Outlined.Store),
                "settings" to Pair("Paramètres", Icons.Outlined.Settings),
                "premium" to Pair("Abonnement ✨", Icons.Outlined.WorkspacePremium)
            )

            tabItems.forEach { (id, pair) ->
                val (label, icon) = pair
                val isSelected = activeTab == id
                Column(
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
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("profile_tab_$id"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }
            }
        }

        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

        // Dynamic Inner Animated Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                },
                label = "ProfileTabScreenTransition"
            ) { targetTab ->
                when (targetTab) {
                    "profile" -> {
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Persistent Live Save Action Banner for edits
                            if (localUsername != viewModel.username || localHandle != viewModel.userHandle) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                        .testTag("save_profile_changes_banner")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Modifications non enregistrées ✍️",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "Cliquez ci-dessous pour appliquer votre nouveau nom d'utilisateur.",
                                                fontSize = 10.sp,
                                                color = Color.White.copy(alpha = 0.8f)
                                            )
                                        }
                                        Button(
                                            onClick = {
                                                viewModel.username = localUsername
                                                viewModel.userHandle = localHandle
                                                viewModel.triggerBeep(1)
                                                Toast.makeText(localContext, "Profil mis à jour avec succès !", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Enregistrer", color = Color(0xFF03070E), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                                        }
                                    }
                                }
                            }

                            // Embed the original ProfileEditorSection
                            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                ProfileEditorSection(
                                    viewModel = viewModel,
                                    username = localUsername,
                                    onUsernameChange = { localUsername = it },
                                    handle = localHandle,
                                    onHandleChange = { localHandle = it },
                                    presets = presets,
                                    filters = filters
                                )
                            }
                        }
                    }
                    "shop" -> {
                        // Embed the complete ShopScreen directly!
                        ShopScreen(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize(),
                            onProfileClick = {
                                // Navigate to the local "Profile" sub-tab!
                                activeTab = "profile"
                                viewModel.triggerBeep(3)
                            }
                        )
                    }
                    "settings" -> {
                        // Embed original SettingsSection
                        Box(modifier = Modifier.fillMaxSize()) {
                            SettingsSection(viewModel = viewModel)
                        }
                    }
                    "premium" -> {
                        // Embed original SubscriptionSection 
                        Box(modifier = Modifier.fillMaxSize()) {
                            SubscriptionSection(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
