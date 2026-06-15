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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.example.ui.viewmodel.AuraViewModel
import com.example.ui.components.RenderPresetAvatar
import com.example.ui.components.ProfileVfxWrapper
import androidx.compose.foundation.BorderStroke
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

    // Coins Purchase local states
    var showCoinsCheckout by remember { mutableStateOf(false) }
    var selectedCoinsAmount by remember { mutableStateOf(0) }
    var selectedCoinsPrice by remember { mutableStateOf("") }
    
    // Checkout inputs
    var paymentMethod by remember { mutableStateOf("card") } // "card", "paypal", "momo"
    var cardNumber by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCvc by remember { mutableStateOf("") }
    var paypalEmail by remember { mutableStateOf("") }
    var momoOperator by remember { mutableStateOf("Orange Money") } // "Orange", "MTN", "Wave"
    var momoPhoneNumber by remember { mutableStateOf("") }
    var isProcessingPayment by remember { mutableStateOf(false) }
    var paymentSuccessResult by remember { mutableStateOf(false) }

    // Boutique Badges Database !
    val badgesDbList = listOf(
        ShopBadgeItem("flame", "Flame", 350, "Faisceau vibrant d'étincelles pyro-tactiques.", "🔥", "Faisceau de feu autour de soi", Color(0xFFFF5722)),
        ShopBadgeItem("galaxy", "Galaxy", 800, "Ambiance stellaire avec particules en orbite cosmique.", "🌌", "Points en orbite cosmique", Color(0xFF3F51B5)),
        ShopBadgeItem("crystal", "Crystal", 1500, "Éclat prismatique aux reflets cristallins.", "💎", "Reflets prismatiques", Color(0xFF00BCD4)),
        ShopBadgeItem("neon", "Neon", 2200, "Glow pulsant rose-violet d'effet cyberpunk.", "🟣", "Éclat rose-violet clignotant", Color(0xFFFF007F)),
        ShopBadgeItem("legendary", "Legendary", 4500, "Couronne dorée étincelante des champions éternels.", "⭐", "Couronne dorée suprême", Color(0xFFFFD700)),
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
                        ProfileVfxWrapper(
                            badgeName = equipped,
                            modifier = Modifier.size(72.dp)
                        ) {
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
                                        imageVector = Icons.Outlined.WorkspacePremium,
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
                                imageVector = Icons.Outlined.Paid,
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
                                imageVector = Icons.Outlined.Shield,
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

        // --- SECTION ACHAT DE PIÈCES D'OR ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .testTag("coins_store_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Paid,
                            contentDescription = "Pièces d'Or Store",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Boutique de Pièces (Argent Réel)",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                    
                    Text(
                        text = "Utilisez vos pièces d'or pour débloquer des défis complexes ou acquérir des badges de profils VFX animés. Vos bonus de l'agenda ou les quêtes quotidiennes vous en rapportent également !",
                        fontSize = 11.sp,
                        color = Color.LightGray,
                        lineHeight = 15.sp
                    )

                    val currency = viewModel.currentSubRegion.currencySymbol
                    val isMobileMoneyDefault = viewModel.currentSubRegion.id == "XOF" || viewModel.currentSubRegion.id == "XAF"
                    
                    val coinPacks = listOf(
                        Triple(150, if (isMobileMoneyDefault) "500 $currency" else "0,99 $currency", "Sachet d'Initiation"),
                        Triple(500, if (isMobileMoneyDefault) "1 500 $currency" else "2,99 $currency", "Sac d'Aventurier ✨"),
                        Triple(1200, if (isMobileMoneyDefault) "3 000 $currency" else "5,99 $currency", "Coffre Spirituel (+10% Bonus) 🔥"),
                        Triple(4000, if (isMobileMoneyDefault) "9 000 $currency" else "16,99 $currency", "Trésor Impérial (+25% de bonus) 👑")
                    )

                    coinPacks.forEach { (amount, priceText, title) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Normal, color = Color.LightGray)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(imageVector = Icons.Outlined.Paid, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(14.dp))
                                    Text(
                                        text = "$amount Pièces",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    selectedCoinsAmount = amount
                                    selectedCoinsPrice = priceText
                                    paymentMethod = if (isMobileMoneyDefault) "momo" else "card"
                                    paymentSuccessResult = false
                                    isProcessingPayment = false
                                    showCoinsCheckout = true
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .height(36.dp)
                                    .testTag("buy_pack_${amount}")
                            ) {
                                Text(text = priceText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                                imageVector = Icons.Outlined.CalendarToday,
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
                                imageVector = Icons.Outlined.CheckCircle,
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
                Icon(Icons.Outlined.Store, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
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
                    ProfileVfxWrapper(
                        badgeName = badge.name,
                        modifier = Modifier.size(62.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(badge.themeColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when(badge.id) {
                                    "flame" -> Icons.Outlined.Whatshot
                                    "galaxy" -> Icons.Outlined.AutoAwesome
                                    "crystal" -> Icons.Outlined.Diamond
                                    "neon" -> Icons.Outlined.Palette
                                    "legendary" -> Icons.Outlined.Star
                                    "champion" -> Icons.Outlined.WorkspacePremium
                                    "futuristic" -> Icons.Outlined.SmartToy
                                    else -> Icons.Outlined.Face
                                },
                                contentDescription = badge.name,
                                tint = badge.themeColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
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
                            if (viewModel.trialBadges.containsKey(badge.name)) {
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text("Essai Gratuit ⚡", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700)) },
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
                        if (viewModel.trialBadges.containsKey(badge.name)) {
                            Text(
                                text = "🎁 Fin de l'essai dans : 5 jours",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFFFC107)
                            )
                        }
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
                                        .width(110.dp)
                                        .height(34.dp)
                                        .testTag("shop_badge_unequip_${badge.id}")
                                ) {
                                    Text("Retirer", fontSize = 11.sp)
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.equipBadge(badge.name) },
                                    modifier = Modifier
                                        .width(110.dp)
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
                                    .width(110.dp)
                                    .height(34.dp)
                                    .testTag("shop_badge_buy_${badge.id}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Paid,
                                        contentDescription = "Pièces",
                                        tint = Color(0xFFFFC107),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text("${badge.price}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // 5 days free trial button
                            OutlinedButton(
                                onClick = {
                                    viewModel.activateBadgeTrial(badge.name)
                                    Toast.makeText(context, "Essai de 5 jours activé pour le badge ${badge.name} ! 🤩", Toast.LENGTH_LONG).show()
                                },
                                border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .width(110.dp)
                                    .height(32.dp)
                                    .testTag("shop_badge_trial_${badge.id}")
                            ) {
                                Text("Essai 5j 🎁", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCoinsCheckout && selectedCoinsAmount > 0) {
        val coroutineScope = rememberCoroutineScope()
        AlertDialog(
            onDismissRequest = { 
                if (!isProcessingPayment) showCoinsCheckout = false 
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Outlined.ShoppingCart, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Caisse Sécurisée Mirys", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (paymentSuccessResult) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(Color(0xFF4CAF50).copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = "Succès",
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Text(
                                "Paiement Reçu ! 🎉",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                "Votre compte a été crédité de $selectedCoinsAmount pièces d'or avec succès !",
                                fontSize = 12.sp,
                                color = Color.LightGray,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "Nouveau solde : ${viewModel.coins} 🪙",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFFD700)
                            )
                        }
                    } else if (isProcessingPayment) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Text(
                                "Authentification 3D Secure en cours...",
                                fontSize = 12.sp,
                                color = Color.LightGray,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "Sécurisation bancaire via Stripe & PayTech...",
                                fontSize = 10.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        // Header info
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Achat de Pièces", fontSize = 11.sp, color = Color.LightGray)
                                Text("Pack: +$selectedCoinsAmount Or 🪙", fontWeight = FontWeight.Black, fontSize = 14.sp)
                            }
                            Text(
                                text = selectedCoinsPrice,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Method selectors
                        Text("Choisissez votre moyen de paiement :", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val methods = listOf(
                                "card" to "💳 Carte",
                                "paypal" to "🅿️ PayPal",
                                "momo" to "📱 Money"
                            )
                            methods.forEach { (id, label) ->
                                val isSelected = paymentMethod == id
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.1f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { paymentMethod = id }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else Color.Gray)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Form switcher
                        when (paymentMethod) {
                            "card" -> {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = cardNumber,
                                        onValueChange = { if (it.length <= 16) cardNumber = it },
                                        placeholder = { Text("Numéro de Carte (16 chiffres)", fontSize = 12.sp) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().testTag("checkout_card_num"),
                                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = cardExpiry,
                                            onValueChange = { if (it.length <= 5) cardExpiry = it },
                                            placeholder = { Text("MM/AA", fontSize = 12.sp) },
                                            singleLine = true,
                                            modifier = Modifier.weight(1f).testTag("checkout_card_expiry"),
                                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                                        )
                                        OutlinedTextField(
                                            value = cardCvc,
                                            onValueChange = { if (it.length <= 3) cardCvc = it },
                                            placeholder = { Text("CVC", fontSize = 12.sp) },
                                            singleLine = true,
                                            modifier = Modifier.weight(1f).testTag("checkout_card_cvc"),
                                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                                        )
                                    }
                                }
                            }
                            "paypal" -> {
                                OutlinedTextField(
                                    value = paypalEmail,
                                    onValueChange = { paypalEmail = it },
                                    label = { Text("Adresse Email PayPal", fontSize = 12.sp) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("checkout_paypal_email"),
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                                )
                            }
                            "momo" -> {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // Operators selection
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        listOf("Orange Money", "MTN MoMo", "Wave").forEach { op ->
                                            val isChosen = momoOperator == op
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .background(
                                                        if (isChosen) Color(0xFFFF9800).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .border(
                                                        1.dp,
                                                        if (isChosen) Color(0xFFFF9800) else Color.White.copy(alpha = 0.1f),
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .clickable { momoOperator = op }
                                                    .padding(vertical = 6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(op, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isChosen) Color.White else Color.Gray)
                                            }
                                        }
                                    }
                                    OutlinedTextField(
                                        value = momoPhoneNumber,
                                        onValueChange = { momoPhoneNumber = it },
                                        placeholder = { Text("Ex: +225 07 00 00 00 00", fontSize = 12.sp) },
                                        label = { Text("Numéro Mobile Money", fontSize = 11.sp) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().testTag("checkout_momo_phone"),
                                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (paymentSuccessResult) {
                    Button(
                        onClick = { showCoinsCheckout = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("Terminer", fontSize = 12.sp)
                    }
                } else if (!isProcessingPayment) {
                    val label = when (paymentMethod) {
                        "card" -> "Débiter $selectedCoinsPrice 💳"
                        "paypal" -> "Se connecter à PayPal 🅿️"
                        else -> "Payer via $momoOperator 📱"
                    }
                    Button(
                        onClick = {
                            isProcessingPayment = true
                            coroutineScope.launch {
                                delay(2500) // simulation secure latency
                                viewModel.coins += selectedCoinsAmount
                                viewModel.triggerBeep(3)
                                isProcessingPayment = false
                                paymentSuccessResult = true
                            }
                        }
                    ) {
                        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                if (!isProcessingPayment && !paymentSuccessResult) {
                    TextButton(onClick = { showCoinsCheckout = false }) {
                        Text("Annuler", fontSize = 12.sp)
                    }
                }
            }
        )
    }
}
