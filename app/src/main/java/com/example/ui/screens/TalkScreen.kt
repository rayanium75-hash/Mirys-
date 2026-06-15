package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.ui.viewmodel.AuraViewModel

@Composable
fun TalkScreen(
    viewModel: AuraViewModel,
    modifier: Modifier = Modifier
) {
    val localContext = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("talk_screen")
    ) {
        // Upper banner introducing the Talk Espace and details
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
                        text = "Espace Talk de Mirys",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Exprimez-vous librement ou bavardez en toute intimité.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }

                // Small circular coins showcase (to keep aesthetic consistent)
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Paid,
                            contentDescription = "Pièces",
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

        // Pill SubTabs Selector Row (No Emojis, Real Icons!)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF060D1A))
                .padding(vertical = 10.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val subTabs = listOf(
                "dms" to Pair("Messages Directs", Icons.Outlined.Chat),
                "rooms_voice" to Pair("Salons Vocaux", Icons.Outlined.Mic),
                "rooms_video" to Pair("Salons Vidéos", Icons.Outlined.Videocam)
            )
            subTabs.forEach { (tabId, pair) ->
                val (label, icon) = pair
                // Handle fallback checks gracefully in case the old TabID "rooms" exists in state
                val isSelected = viewModel.talkActiveSubTab == tabId || (tabId == "rooms_voice" && viewModel.talkActiveSubTab == "rooms")
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(30.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else Color(0xFF0F1B2F)
                        )
                        .clickable {
                            viewModel.talkActiveSubTab = tabId
                            if (tabId != "dms") {
                                viewModel.activeChatPartnerHandle = null
                            }
                            viewModel.triggerBeep(3)
                        }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                        .testTag("talk_subtab_$tabId"),
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

        // Active screen render container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (viewModel.talkActiveSubTab) {
                "dms" -> {
                    DirectMessagesTab(
                        viewModel = viewModel,
                        activePartnerHandle = viewModel.activeChatPartnerHandle,
                        onSelectPartner = { partner ->
                            viewModel.activeChatPartnerHandle = partner
                        }
                    )
                }
                "rooms_voice", "rooms" -> {
                    VoiceRoomsTab(viewModel = viewModel, isVideoOnly = false)
                }
                "rooms_video" -> {
                    VoiceRoomsTab(viewModel = viewModel, isVideoOnly = true)
                }
            }
        }
    }

    // Call overlay
    if (viewModel.activeCallPartnerHandle != null) {
        ImmersiveCommunicationHub(viewModel = viewModel)
    }

    // Direct Profile messaging overlay connection
    val activeUserProfileHandle = viewModel.activeUserProfileHandle
    if (activeUserProfileHandle != null) {
        SocialUserProfileDialog(
            handle = activeUserProfileHandle,
            viewModel = viewModel,
            onSendMessageClick = {
                viewModel.closeUserProfile()
                viewModel.talkActiveSubTab = "dms"
                viewModel.activeChatPartnerHandle = activeUserProfileHandle
            },
            onDismiss = { viewModel.closeUserProfile() }
        )
    }
}
