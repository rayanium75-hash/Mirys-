package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.AuraViewModel
import com.example.ui.components.RenderPresetAvatar
import androidx.compose.foundation.clickable

data class ShopBadgeItem(
    val id: String,
    val name: String,
    val price: Int,
    val info: String,
    val iconEmoji: String,
    val vfxDesc: String,
    val themeColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(
    viewModel: AuraViewModel,
    modifier: Modifier = Modifier,
    onProfileClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val owned = viewModel.ownedBadges
    val equipped = viewModel.equippedBadge

    // Boutique Badges Database !
    val badgesDbList = listOf(
        ShopBadgeItem("flame", "Flame", 350, "Aura vibrante d'étincelles pyro-tactiques.", "🔥", "Aura de feu autour de soi", Color(0xFFFF5722)),
        ShopBadgeItem("galaxy", "Galaxy", 800, "Ambiance stellaire avec particules en orbite cosmique.", "🌌", "Points en orbite cosmique", Color(0xFF3F51B5)),
        ShopBadgeItem("crystal", "Crystal", 1500, "Éclat prismatique aux reflets cristallins.", "💎", "Reflets prismatiques", Color(0xFF00BCD4)),
        ShopBadgeItem("neon", "Neon", 2200, "Glow pulsant rose-violet d'effet cyberpunk.", "🟣", "Éclat rose-violet clignotant", Color(0xFFFF007F)),
        ShopBadgeItem("legendary", "Legendary", 4500, "Couronne dorée étincelante des champions éternels.", "⭐", "Aura dorée suprême", Color(0xFFFFD700)),
        ShopBadgeItem("champion", "Champion", 6000, "Vents électriques bleutés d'élite tactique.", "👑", "Couronne d'éclats électriques", Color(0xFF00E5FF)),
        ShopBadgeItem("futuristic", "Futuristic", 7500, "Hologramme matriciel avec effet de scan cyber.", "🤖", "Scan holographique numérique", Color(0xFF00E676))
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("shop_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Upper Profile Header showing player progress / ELO / level / coins
        item {
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        2.dp,
                        if (equipped != null) getBadgeBorderBrush(equipped) else getBadgeBorderBrush("Default"),
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onProfileClick() }
                    .testTag("shop_profile_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Profile details row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                                .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape),
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
                                username = viewModel.username,
                                modifier = Modifier.fillMaxSize(),
                                customUri = viewModel.customProfilePhotoUri
                            )
                        }

                        // Username & active badge
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = viewModel.username,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (viewModel.subscriptionTier != "Gratuit") {
                                    Icon(
                                        imageVector = Icons.Default.WorkspacePremium,
                                        contentDescription = "Premium Status",
                                        tint = Color(0xFFFFD700),
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (equipped != null) "Badge actif : $equipped" else "Aucun badge équipé",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Level and XP progress row
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Niveau ${viewModel.currentLevel}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            val nextLevelXp = 100
                            val relativeXp = viewModel.xp % 100
                            Text(
                                text = "$relativeXp / $nextLevelXp XP",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        val relativeProgress = (viewModel.xp % 100).toFloat() / 100f
                        LinearProgressIndicator(
                            progress = relativeProgress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // Coins and ELO row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Coins
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Paid,
                                contentDescription = "Pièces",
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text("Pièces d'Or", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${viewModel.coins}", fontWeight = FontWeight.Black, fontSize = 15.sp)
                            }
                        }

                        // Chess ELO
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "ELO",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text("Classement Chess", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${viewModel.eloChess} ELO", fontWeight = FontWeight.Black, fontSize = 15.sp)
                            }
                        }
                    }
                }
            }
        }

        // Streak & Daily Reward Card CTA !
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("shop_daily_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Série",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text("Récompense Quotidienne", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Série Active : ${viewModel.dailyStreak} jours consécutifs", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                            }
                        }

                        if (viewModel.isDailyClaimed) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Réclamé",
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (!viewModel.isDailyClaimed) {
                                viewModel.claimDailyReward()
                                Toast.makeText(context, "Récompense réclamée ! (+50% bonus de série)", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = !viewModel.isDailyClaimed,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("btn_claim_daily")
                    ) {
                        Text(
                            text = if (viewModel.isDailyClaimed) "Réclamer demain !" else "Récupérer ma récompense quotidienne",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Section Title Badges Boutique
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Store, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    text = "Boutique de Badges Animés (VFX)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // List displaying badges
        items(badgesDbList) { badge ->
            val isOwned = owned.contains(badge.name)
            val isEquipped = equipped == badge.name

            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = if (isEquipped) 2.dp else 1.dp,
                        color = if (isEquipped) badge.themeColor else MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .testTag("shop_badge_item_${badge.id}")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Vector Custom Icon representation
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(badge.themeColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when(badge.id) {
                                "flame" -> Icons.Default.Whatshot
                                "galaxy" -> Icons.Default.AutoAwesome
                                "crystal" -> Icons.Default.Diamond
                                "neon" -> Icons.Default.Palette
                                "legendary" -> Icons.Default.Star
                                "champion" -> Icons.Default.WorkspacePremium
                                "futuristic" -> Icons.Default.SmartToy
                                else -> Icons.Default.Face
                            },
                            contentDescription = badge.name,
                            tint = badge.themeColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Metadata detail description
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = badge.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            if (isEquipped) {
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text("Équipé", fontSize = 8.sp, fontWeight = FontWeight.Bold) },
                                    modifier = Modifier.height(20.dp)
                                )
                            }
                        }
                        
                        Text(
                            text = badge.info,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 15.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Effet visuel: ${badge.vfxDesc}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = badge.themeColor
                        )
                    }

                    // Button Action state buy/equip/equipped
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (isOwned) {
                            if (isEquipped) {
                                OutlinedButton(
                                    onClick = { viewModel.equipBadge(null) },
                                    modifier = Modifier
                                        .width(96.dp)
                                        .height(34.dp)
                                        .testTag("shop_badge_unequip_${badge.id}")
                                ) {
                                    Text("Retirer", fontSize = 11.sp)
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.equipBadge(badge.name) },
                                    modifier = Modifier
                                        .width(96.dp)
                                        .height(34.dp)
                                        .testTag("shop_badge_equip_${badge.id}")
                                ) {
                                    Text("Équiper", fontSize = 11.sp)
                                }
                            }
                        } else {
                            Button(
                                onClick = {
                                    val success = viewModel.purchaseBadge(badge.name, badge.price)
                                    if (success) {
                                        Toast.makeText(context, "Badge ${badge.name} débloqué et équipé ! 🎉", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Pièces insuffisantes ! Remplissez des tâches ou jouez aux Quiz.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary,
                                    contentColor = MaterialTheme.colorScheme.onTertiary
                                ),
                                modifier = Modifier
                                    .width(96.dp)
                                    .height(34.dp)
                                    .testTag("shop_badge_buy_${badge.id}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Paid,
                                        contentDescription = "Pièces",
                                        tint = Color(0xFFFFC107),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text("${badge.price}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
