package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.widget.Toast
import android.os.Vibrator
import android.os.VibrationEffect
import android.os.Build
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.JournalEntry
import com.example.data.model.Task
import com.example.data.repository.AiReport
import com.example.data.repository.AuraRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SubRegion(
    val id: String,
    val name: String,
    val flag: String,
    val currencySymbol: String,
    val currencyCode: String,
    val proPriceFormatted: String,
    val premiumPriceFormatted: String,
    val proNumericPrice: Double,
    val premiumNumericPrice: Double,
    val proRawPriceString: String,
    val premiumRawPriceString: String,
    val streamPriceFormatted: String
)

data class SocialStory(
    val id: Int,
    val authorHandle: String,
    val authorName: String,
    val isVideo: Boolean,
    val mediaUrl: String,
    val timestamp: String,
    val caption: String = ""
)

class AuraViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AuraRepository
    private var tts: android.speech.tts.TextToSpeech? = null

    // Stream for journal records
    val journalEntries: StateFlow<List<JournalEntry>>
    // Stream for tasks
    val tasks: StateFlow<List<Task>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AuraRepository(database.journalDao(), database.taskDao())
        
        try {
            tts = android.speech.tts.TextToSpeech(application) { status ->
                if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                    tts?.language = Locale.FRANCE
                }
            }
        } catch (e: Exception) {
            // Ignore if TTS not supported
        }
        
        journalEntries = repository.allJournalEntries.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        tasks = repository.allTasks.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Ticking loop for call limits and simulated voice room connections
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(1000)

                // Countdown for Free Trial of 2 days including visual label update
                if (isFreeTrialActive) {
                    if (trialSecondsRemaining > 0) {
                        trialSecondsRemaining -= 1
                        val h = trialSecondsRemaining / 3600
                        val m = (trialSecondsRemaining % 3600) / 60
                        val s = trialSecondsRemaining % 60
                        trialTimeLabel = String.format("%02d:%02d:%02d", h, m, s)
                    } else {
                        isFreeTrialActive = false
                        trialTimeLabel = "Expiré"
                        subscriptionTier = "Gratuit"
                    }
                }

                if (activeCallPartnerHandle != null && callStatus == "En direct") {
                    callDurationSeconds += 1
                    if (subscriptionTier == "Gratuit") {
                        if (callDurationSeconds >= 15) {
                            activeCallPartnerHandle = null // End the call
                            showCallLimitDialog = true
                            triggerBeep(2)
                        } else if (callDurationSeconds >= 10) {
                            callStatus = "Limite imminente (déconnexion à 15s en gratuit)"
                        }
                    } else {
                        // Premium / Pro unlimited
                    }
                }
            }
        }
    }

    // --- DEVELOPER / CREATOR / MEMBERS DIRECT SOCIAL STATE ---
    var designerAccountUnlocked by mutableStateOf(false)
    var activeUserProfileHandle by mutableStateOf<String?>(null)
    var followedHandles by mutableStateOf<Set<String>>(emptySet())
    var profileSubscribersCount by mutableStateOf<Map<String, Int>>(emptyMap())
    var profileFollowingCount by mutableStateOf<Map<String, Int>>(emptyMap())

    var userOnlineStatusMap by mutableStateOf<Map<String, Boolean>>(emptyMap())

    fun getSubscribersCount(handle: String): Int {
        return profileSubscribersCount[handle]
            ?: profileSubscribersCount[handle.lowercase()]
            ?: profileSubscribersCount[handle.replaceFirstChar { it.uppercase() }]
            ?: ((handle.hashCode() % 300) + 45)
    }

    fun getFollowingCount(handle: String): Int {
        return profileFollowingCount[handle]
            ?: profileFollowingCount[handle.lowercase()]
            ?: profileFollowingCount[handle.replaceFirstChar { it.uppercase() }]
            ?: ((handle.hashCode() % 80) + 24)
    }

    fun isUserOnline(handle: String): Boolean {
        return userOnlineStatusMap[handle]
            ?: userOnlineStatusMap[handle.lowercase()]
            ?: userOnlineStatusMap[handle.replaceFirstChar { it.uppercase() }]
            ?: false
    }

    fun toggleUserOnlineStatus(handle: String) {
        val current = isUserOnline(handle)
        val key = userOnlineStatusMap.keys.find { it.lowercase() == handle.lowercase() } ?: handle
        userOnlineStatusMap = userOnlineStatusMap + (key to !current)
        triggerBeep(3)
    }

    var storiesList by mutableStateOf<List<SocialStory>>(emptyList())

    fun addStory(isVideo: Boolean, mediaUrl: String, caption: String = "") {
        val newId = storiesList.size + 1
        val newStory = SocialStory(
            id = newId,
            authorHandle = "mon_compte",
            authorName = "Moi",
            isVideo = isVideo,
            mediaUrl = mediaUrl,
            timestamp = "À l'instant",
            caption = caption
        )
        storiesList = listOf(newStory) + storiesList
        triggerBeep(1)
    }

    // Direct Messages system
    var directMessagesMap by mutableStateOf<Map<String, List<DirectMessage>>>(emptyMap())
    // Messages initialisés vides — remplis dynamiquement par les vrais utilisateurs

    // Immersive calls states
    var activeCallPartnerHandle by mutableStateOf<String?>(null)
    var activeCallType by mutableStateOf<String?>(null) // "audio", "video", "voiceroom", "videoroom"
    var isCallMuted by mutableStateOf(false)
    var isCallVideoEnabled by mutableStateOf(true)
    var isCallSpeakerOn by mutableStateOf(false)
    var callDurationSeconds by mutableIntStateOf(0)
    var callStatus by mutableStateOf("Connexion...") // "Connexion...", "En direct", "Terminé", "Limite dépassée"

    // Host room action states
    var isRoomLocked by mutableStateOf(false)
    var isEveryoneMuted by mutableStateOf(false)
    var isScreenSharing by mutableStateOf(false)
    var pinnedRoomMessage by mutableStateOf<String?>(null)
    val roomGuests = androidx.compose.runtime.mutableStateListOf<String>()
    val roomJoinRequests = androidx.compose.runtime.mutableStateListOf<String>()

    fun approveRoomRequest(requestingUser: String) {
        roomJoinRequests.remove(requestingUser)
        addRoomGuest(requestingUser)
    }

    fun rejectRoomRequest(requestingUser: String) {
        roomJoinRequests.remove(requestingUser)
        triggerVibration(2)
        triggerBeep(2)
    }

    fun simulateMockJoinRequest() {
        val candidates = listOf(
            "samuel_pro 🇨🇬",
            "lola_sky 🇬🇦",
            "omar_dev 🇸🇳",
            "fanta_star 🇲🇱",
            "pierre_coder 🇨🇷",
            "amine_aura 🇩🇿"
        )
        val next = candidates.filter { !roomGuests.contains(it) && !roomJoinRequests.contains(it) }.randomOrNull()
        if (next != null) {
            roomJoinRequests.add(next)
            triggerBeep(3)
            Toast.makeText(getApplication(), "@$next a demandé à rejoindre le salon !", Toast.LENGTH_SHORT).show()
        }
    }

    fun kickRoomGuest(guest: String) {
        roomGuests.remove(guest)
        triggerVibration(2) // Error haptic for kick
        triggerBeep(2)      // Low sound alert
    }

    fun addRoomGuest(guest: String) {
        if (!roomGuests.contains(guest)) {
            roomGuests.add(guest)
            triggerVibration(1) // Success haptic for guest enter
            triggerBeep(3)      // Light sound alert
        }
    }

    fun toggleRoomLock() {
        isRoomLocked = !isRoomLocked
        triggerVibration(3)
    }

    fun toggleMuteEveryone() {
        isEveryoneMuted = !isEveryoneMuted
        triggerVibration(3)
    }

    fun toggleScreenSharing() {
        isScreenSharing = !isScreenSharing
        triggerVibration(3)
    }

    fun setRoomPinnedMessage(msg: String?) {
        pinnedRoomMessage = msg
        triggerVibration(1)
    }

    fun tryDesignerLogin(email: String, code: String): Boolean {
        if (false) { // Admin login disabled
            designerAccountUnlocked = true
            username = "Mirysofficiel"
            userHandle = "mirysofficiel"
            equippedBadge = "Créateur 👑"
            subscriptionTier = "Premium Ultimate 👑"
            triggerBeep(1)
            // Welcome announcement
            addNewPost(
                content = "Bonjour à toute la communauté ! C'est Alane Mentii (Mirysofficiel). Je viens de me connecter sur mon compte officiel. Retrouvez mes annonces en direct et rejoignez mes Salons Vocaux ! Badge certifié bleu & Effets de gravité activés ! ⭐🌐",
                photos = listOf("https://images.unsplash.com/photo-1545235617-9465d2a55698?q=80&w=600")
            )
            return true
        }
        triggerBeep(2)
        return false
    }

    fun logoutDesigner() {
        designerAccountUnlocked = false
        username = "Moi (Créateur)"
        userHandle = "mon_compte"
        equippedBadge = null
        subscriptionTier = "Gratuit"
        triggerBeep(3)
    }

    fun toggleFollowUser(handle: String) {
        val keysToCheck = setOf(handle, handle.lowercase(), handle.replaceFirstChar { it.uppercase() })
        val matchedInFollowed = followedHandles.intersect(keysToCheck).firstOrNull()
        
        if (matchedInFollowed != null) {
            followedHandles = followedHandles - matchedInFollowed
            val key = profileSubscribersCount.keys.find { it.lowercase() == handle.lowercase() } ?: handle
            val currentCount = profileSubscribersCount[key] ?: 100
            profileSubscribersCount = profileSubscribersCount + (key to kotlin.math.max(0, currentCount - 1))
        } else {
            followedHandles = followedHandles + handle
            val key = profileSubscribersCount.keys.find { it.lowercase() == handle.lowercase() } ?: handle
            val currentCount = profileSubscribersCount[key] ?: 100
            profileSubscribersCount = profileSubscribersCount + (key to (currentCount + 1))
        }
        triggerBeep(3)
    }

    fun openUserProfile(handle: String) {
        activeUserProfileHandle = handle
        triggerBeep(3)
    }

    fun closeUserProfile() {
        activeUserProfileHandle = null
    }

    // --- ADMINISTRATIVE & SECURITY STATE / FUNCTIONS (Forwarded to alanementii73@gmail.com) ---
    var blockedHandles by mutableStateOf<Set<String>>(emptySet())
    var restrictedHandles by mutableStateOf<Set<String>>(emptySet())
    var securityNotificationDialogText by mutableStateOf<String?>(null)

    fun blockUser(handle: String) {
        blockedHandles = blockedHandles + handle
        triggerBeep(2)
        securityNotificationDialogText = "Compte @$handle bloqué avec succès ! Une alerte de sécurité a été transmise à lanementii73@gmail.com pour signaler le blocage de cet utilisateur."
    }

    fun unblockUser(handle: String) {
        blockedHandles = blockedHandles - handle
        triggerBeep(3)
        securityNotificationDialogText = "Compte @$handle débloqué. Les publications et DMs de cet utilisateur sont à nouveau affichés."
    }

    fun reportUser(handle: String, reason: String) {
        triggerBeep(2)
        securityNotificationDialogText = "Signalement envoyé avec succès ! L'équipe de modération de Mirys a reçu votre requête. Le comportement du compte @$handle sera examiné sous 24h."
    }

    fun restrictUser(handle: String) {
        restrictedHandles = restrictedHandles + handle
        triggerBeep(1)
        securityNotificationDialogText = "Compte @$handle restreint. Ses publications et mentions sont masqués de votre flux."
    }

    fun unrestrictUser(handle: String) {
        restrictedHandles = restrictedHandles - handle
        triggerBeep(3)
        securityNotificationDialogText = "Compte @$handle n'est plus restreint. Ses publications réapparaissent."
    }

    fun shareUserProfile(handle: String) {
        triggerBeep(1)
        securityNotificationDialogText = "Lien de profil généré : https://mirys.app/user/@$handle. Transmis à vos contacts."
    }

    fun synchronizeVibratoryAura(handle: String) {
        triggerBeep(3)
        securityNotificationDialogText = "Analyse d'alignement Aura en cours avec @$handle...\n\nRésultat : Alignement Cosmique Spontané de 94% ! Vos vibrations sont hautement compatibles. Vous devriez interagir davantage !"
    }

    fun sendDirectMessage(
        partnerHandle: String,
        text: String,
        isVoiceNote: Boolean = false,
        voiceNoteDuration: Int = 0,
        photoUrl: String? = null,
        isVideo: Boolean = false,
        videoUrl: String? = null
    ) {
        if (text.isBlank() && !isVoiceNote && photoUrl == null && videoUrl == null) return
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        val timeStr = formatter.format(Date())
        
        val newMsg = DirectMessage(
            id = (directMessagesMap[partnerHandle]?.size ?: 0) + 1,
            senderHandle = userHandle,
            content = text,
            timestamp = timeStr,
            isVoiceNote = isVoiceNote,
            voiceNoteDuration = voiceNoteDuration,
            photoUrl = photoUrl,
            isVideo = isVideo,
            videoUrl = videoUrl
        )
        
        val existing = directMessagesMap[partnerHandle] ?: emptyList()
        directMessagesMap = directMessagesMap + (partnerHandle to (existing + newMsg))
        triggerBeep(3)
        
        // Custom interactive simulation responses
        viewModelScope.launch {
            kotlinx.coroutines.delay(1500)
            val replyText = when (partnerHandle) {
                "sonia_coder" -> {
                    if (photoUrl != null) {
                        "Wouah ! Magnifique ce partage visuel ! J'ai hâte de continuer la conception avec toi ! 🎨💖"
                    } else if (videoUrl != null) {
                        "Impressionnant ce clip vidéo ! C'est fluide et réaliste, bravo pour le partage !"
                    } else if (text.lowercase().contains("salut") || text.lowercase().contains("hey") || text.lowercase().contains("bonjour")) {
                        "Hey ! Ravi de t'échanger en privé des messages ! Est-ce que tu as vu les nouveaux effets de la boutique ? Ils brillent tellement !"
                    } else if (text.lowercase().contains("appel") || text.lowercase().contains("vocal") || text.lowercase().contains("salon")) {
                        "Absolument ! Démarre le salon d'appel tout de suite dans nos moments ! 🎤"
                    } else {
                        "C'est noté ! Je continue d'apprendre des tactiques sur l'échiquier de l'Aura. À bientôt ! ♟️💥"
                    }
                }
                "chess_club_yaounde" -> {
                    "Super ! Merci pour votre spontanéité. Nos salons vocaux d'échecs sont ouverts à tous les membres !"
                }
                "mirys_team" -> {
                    "Merci d'interagir avec nous ! L'équipe Mirys est toujours à votre écoute pour s'améliorer."
                }
                "mirysofficiel" -> {
                    "Merci de m'écrire ! C'est Alane Mentii (Mirysofficiel), concepteur de Mirys."
                }
                else -> {
                    if (photoUrl != null) {
                        "Quel superbe visuel ! Ton partage correspond vraiment à de bonnes ondes !"
                    } else {
                        "Fascinant ! Merci de ton partage. Passe une très bonne journée ! 🌟"
                    }
                }
            }
            
            val replyMsg = DirectMessage(
                id = (directMessagesMap[partnerHandle]?.size ?: 0) + 1,
                senderHandle = partnerHandle,
                content = replyText,
                timestamp = formatter.format(Date()),
                isVoiceNote = false
            )
            val updated = directMessagesMap[partnerHandle] ?: emptyList()
            directMessagesMap = directMessagesMap + (partnerHandle to (updated + replyMsg))
            triggerBeep(1)
        }
    }

    var shouldOfferRoomPostInMoments by mutableStateOf(true)
    var activeRoomPostId by mutableStateOf<Int?>(null)

    fun startVoiceOrVideoRoom(type: String) {
        val partner = if (type == "voiceroom") "Mon Salon Vocal 🎙️" else "Ma Salle Vidéo 🎥"
        
        // Reset host states
        isRoomLocked = false
        isEveryoneMuted = false
        isScreenSharing = false
        pinnedRoomMessage = null
        roomGuests.clear()
        // roomGuests.addAll(listOf("lucas_cyber", "luna_zen", "alicia_light"))
        
        // Initialize pending moderator join permissions
        roomJoinRequests.clear()
        // roomJoinRequests.addAll(listOf("jean_dupont 🇨🇲", "sarah_care 🇸🇳", "marc_tech 🇨🇮"))

        if (shouldOfferRoomPostInMoments) {
            val contentStr = if (type == "voiceroom") {
                "🚨 J'ai ouvert un nouveau Salon de Chat Vocal gratuit live ! Rejoignez-moi pour discuter en direct ! 🎙️✨"
            } else {
                "🎥 Salon de Chat Vidéo ouvert ! Entrez dans l'arène vidéo en direct pour échanger en face à face ! 🌐👇"
            }
            activeRoomPostId = addNewPost(content = contentStr, isVoiceRoom = true)
        } else {
            activeRoomPostId = null
        }
        
        triggerCallSession(partnerHandle = partner, type = type)

        // Launch request permission simulation loop
        viewModelScope.launch {
            kotlinx.coroutines.delay(10000) // wait 10 seconds before starting loops
            while (activeCallType == "voiceroom" || activeCallType == "videoroom") {
                simulateMockJoinRequest()
                kotlinx.coroutines.delay(15000) // every 15s
            }
        }
    }

    fun triggerCallSession(partnerHandle: String, type: String, autoPostToFeed: Boolean = false) {
        activeCallPartnerHandle = partnerHandle
        activeCallType = type
        isCallMuted = false
        isCallSpeakerOn = false
        isCallVideoEnabled = true
        callDurationSeconds = 0
        callStatus = "Connexion..."
        triggerBeep(3)
        
        viewModelScope.launch {
            kotlinx.coroutines.delay(1500)
            if (activeCallPartnerHandle != null) {
                callStatus = "En direct"
                triggerBeep(1)
            }
        }

        if (autoPostToFeed) {
            val label = when (type) {
                "voiceroom" -> "un Salon Vocal 🎤"
                "videoroom" -> "un Salon Discussion Vidéo 📹"
                "video" -> "un Appel Vidéo Privé 📹"
                else -> "un Appel Audio Privé 📞"
            }
            val contentStr = "🔴 J'ai ouvert $label en direct avec @$partnerHandle ! C'est disponible dès à présent dans nos moments et salons ! Rejoignez-nous pour discuter ! 🔥🎙️"
            activeRoomPostId = addNewPost(content = contentStr, isVoiceRoom = true)
        }
    }

    fun hangUpCall() {
        activeCallPartnerHandle = null
        activeCallType = null
        callStatus = "Terminé"
        triggerBeep(2)

        // If we opened a room post in moments, mark it as terminated / finished!
        activeRoomPostId?.let { postId ->
            postsList = postsList.map { post ->
                if (post.id == postId) {
                    val endedContent = "🔴 [Fermé - Session terminée] " + post.content.replace("🚨 ", "").replace("🎥 ", "").replace("🔴 ", "")
                    post.copy(
                        content = endedContent,
                        isVoiceRoom = false
                    )
                } else {
                    post
                }
            }
            activeRoomPostId = null
        }
    }

    // --- NAVIGATION TABS SYSTEM ---
    var currentTab by mutableStateOf("dashboard") // "dashboard", "feed", "talk", "agenda", "profile"
        private set

    fun selectTab(tab: String) {
        currentTab = tab
    }

    var activeChatPartnerHandle by mutableStateOf<String?>(null)
    var talkActiveSubTab by mutableStateOf("dms") // "dms" or "rooms"

    // --- AI COMPANION STATES (Mirys IA) ---
    var isAnalyzingMood by mutableStateOf(false)
        private set
    var aiReport: AiReport? by mutableStateOf(null)
        private set

    // Task AI state
    var isGeneratingTasks by mutableStateOf(false)
        private set

    // AI Chat state
    var isChatLoading by mutableStateOf(false)
        private set
    val chatMessages = MutableStateFlow<List<Pair<String, Boolean>>>(
        listOf(
            "Bonjour ! Je suis Mirys, votre compagnon d'intelligence émotionnelle et d'organisation. Écrivez vos ressentis dans l'onglet Journal ou demandez-moi conseil ici !" to false
        )
    )

    // --- GAMIFICATION & PROFILE SYSTEM (MIRYS v2.0) ---
    var coins by mutableIntStateOf(350) // Set default coins to 350 so that people have a playground starting balance
    var xp by mutableIntStateOf(35)
    var eloChess by mutableIntStateOf(1200)
    var equippedBadge by mutableStateOf<String?>(null)
    var ownedBadges by mutableStateOf<Set<String>>(setOf("Default"))
    var trialBadges by mutableStateOf<Map<String, Long>>(emptyMap()) // badgeName to expiry timestamp

    // --- 10 SPECTACULAR PREMIUM HUB STATES ---
    var activatedAuraSkin by mutableStateOf("none") // "none", "aurora", "cyber", "royal"
    var activeMoodHalo by mutableStateOf<String?>(null) // e.g., "Hyper-Créatif 💡"
    var premiumStoryStep by mutableStateOf(1)
    var premiumStoryPoints by mutableStateOf(0)
    var premiumDreamAnalysisResult by mutableStateOf<String?>(null)
    var premiumDreamOracleCard by mutableStateOf<String?>(null)
    var premiumDreamMoodColorHex by mutableStateOf("#9C27B0")
    var isDreamOracleAnalyzing by mutableStateOf(false)
    var premiumRadarScanning by mutableStateOf(false)
    var premiumSoundscapeTheme by mutableStateOf("none") // "none", "serenity", "nebula", "fire"
    var premiumSoundscapeVolume by mutableFloatStateOf(0.5f)
    var premiumTranslatorLanguage by mutableStateOf("Wolof  🇸🇳")
    var isAuraHaloPulsing by mutableStateOf(true)
    var selectedEliteArenaLeague by mutableStateOf("Ligue de Platine")
    var premiumTournamentTrophies by mutableStateOf(12)
    var premiumTournamentsWon by mutableStateOf(4)

    // Additive free/paid limits & monetization variables
    var showCallLimitDialog by mutableStateOf(false)
    var freeGoalGenerationsToday by mutableStateOf(0)
    var isBoostActive by mutableStateOf(false)
    var unlockedGames by mutableStateOf<Set<String>>(setOf("quiz", "memory", "lightsout"))

    // Daily tracker (Premium paid option) states & selection theme
    var trackerSelectedTheme by mutableStateOf("Mirys Cosmique 🌌") // "Mirys Cosmique 🌌", "Forêt Zen 🌿", "Océan Serein 🌊", "Volcan Créatif 🌋"
    var trackerWater by mutableIntStateOf(3) // out of 8 cups
    var trackerMeditate by mutableStateOf(false)
    var trackerSteps by mutableIntStateOf(4200) // target 10000
    var trackerSleepHours by mutableIntStateOf(7) // out of 8
    var trackerStudyTime by mutableIntStateOf(20) // minutes out of 60

    // Interconnected logical Hook Feature: Soul Resonance Core reactor
    var resonanceFrequencyName by mutableStateOf("432 Hz - Calme & Éveil")
    var resonanceCoreCharge by mutableIntStateOf(0) // active charge %
    var resonanceDailyCompleted by mutableStateOf(false)
    var resonanceEnergySeeds by mutableIntStateOf(3) // Seed currency used for forging
    var activeResonanceSparkles by mutableIntStateOf(100) // Hook engagement sparkles!
    var forgedRelicsCount by mutableIntStateOf(1) // Number of forged spiritual items
    var activeConstellationStars by mutableStateOf<Set<Int>>(setOf(1, 2)) // indices of active forged stars

    fun chargeResonanceUnitStep(power: Int): Boolean {
        if (resonanceDailyCompleted) return false
        val newCharge = resonanceCoreCharge + power
        if (newCharge >= 100) {
            resonanceCoreCharge = 100
            resonanceDailyCompleted = true
            activeResonanceSparkles += 150
            coins += 40
            xp += 20
            resonanceEnergySeeds += 1
            triggerBeep(1)
            return true
        } else {
            resonanceCoreCharge = newCharge
            if (resonanceCoreCharge % 10 == 0) {
                triggerBeep(3)
            }
        }
        return false
    }

    fun chargeResonanceCancel() {
        if (!resonanceDailyCompleted) {
            resonanceCoreCharge = 0
        }
    }

    fun forgeAstralRelic(): Boolean {
        if (resonanceEnergySeeds >= 3) {
            resonanceEnergySeeds -= 3
            forgedRelicsCount += 1
            coins += 100
            xp += 50
            val newStars = activeConstellationStars.toMutableSet()
            val nextStar = (1..6).firstOrNull { !newStars.contains(it) } ?: 1
            newStars.add(nextStar)
            activeConstellationStars = newStars
            triggerBeep(1)
            return true
        }
        return false
    }

    fun purchaseXpGoldBooster(): Boolean {
        if (coins >= 150) {
            coins -= 150
            isBoostActive = true
            triggerBeep(3)
            return true
        }
        return false
    }

    fun unlockGameWithCoins(gameId: String): Boolean {
        if (coins >= 150) {
            coins -= 150
            unlockedGames = unlockedGames + gameId
            triggerBeep(3)
            return true
        }
        return false
    }

    fun isGameUnlocked(gameId: String): Boolean {
        // If premium or pro, or 2-day free trial is active, all games are instantly unlocked!
        if (subscriptionTier != "Gratuit" || isFreeTrialActive) return true
        return unlockedGames.contains(gameId)
    }

    fun activateBadgeTrial(badgeName: String) {
        val durationMs = 5 * 24 * 60 * 60 * 1000L // 5 days free
        val expiryTime = System.currentTimeMillis() + durationMs
        trialBadges = trialBadges + (badgeName to expiryTime)
        ownedBadges = ownedBadges + badgeName
        equippedBadge = badgeName
        triggerBeep(1)
    }

    var isDailyClaimed by mutableStateOf(false)
    var dailyStreak by mutableIntStateOf(1)

    // --- USER PROFILE & SUBSCRIPTION (NEW) ---
    var username by mutableStateOf("Moi (Créateur)")
    var userHandle by mutableStateOf("mon_compte")
    var profilePhotoPreset by mutableStateOf("Default") // "Default", "Nebula", "Golden Hero", "Cyber Punk", "Cosmic", "Custom"
    var customProfilePhotoUri by mutableStateOf<String?>(null)
    var profileFilter by mutableStateOf("Normal") // "Normal", "Noir & Blanc", "Vintage", "Bleu Cyber", "Neon Rose"
    var profileBrightness by mutableStateOf(1.0f) // 0.5f to 1.5f
    var profileContrast by mutableStateOf(1.0f) // 0.5f to 1.5f
    var profileZoom by mutableStateOf(1.0f) // 1.0f to 2.0f
    var profileCropX by mutableStateOf(0.0f)
    var profileCropY by mutableStateOf(0.0f)

    // --- APP SETTINGS DEPENDENCIES ---
    var settingsNotificationsEnabled by mutableStateOf(true)
    var settingsHapticEnabled by mutableStateOf(true)
    var settingsAudioEnabled by mutableStateOf(true)
    var settingsAccountPrivate by mutableStateOf(false)
    var settingsDeepDarkTheme by mutableStateOf(false)
    var appTheme by mutableStateOf("dark") // "dark", "light", "system"
    var appLanguage by mutableStateOf("FR") // "FR", "EN", "ES", "DE"

    // --- SUBSCRIPTION TIER ---
    var subscriptionTier by mutableStateOf("Gratuit") // "Gratuit", "Pro ✨", "Premium Ultimate 👑"
    var subscriptionExpiryDate by mutableStateOf<String?>(null)

    // --- 2-DAY FREE TRIAL SYSTEM (NEW) ---
    var isFreeTrialActive by mutableStateOf(false)
    var trialSecondsRemaining by mutableStateOf(172800L) // 2 days in seconds
    var trialTimeLabel by mutableStateOf("48:00:00")

    fun start2DayFreeTrial() {
        isFreeTrialActive = true
        trialSecondsRemaining = 172800L
        trialTimeLabel = "48:00:00"
        subscriptionTier = "Premium Trial 👑"
        triggerBeep(3)
    }

    fun endFreeTrialImmediately() {
        isFreeTrialActive = false
        trialSecondsRemaining = 0L
        trialTimeLabel = "Expiré"
        subscriptionTier = "Gratuit"
        triggerBeep(2)
    }

    val subRegionsList = listOf(
        SubRegion(
            id = "EUR",
            name = "Europe (Zone Euro)",
            flag = "🇪🇺",
            currencySymbol = "€",
            currencyCode = "EUR",
            proPriceFormatted = "4,99 €",
            premiumPriceFormatted = "9,99 €",
            proNumericPrice = 4.99,
            premiumNumericPrice = 9.99,
            proRawPriceString = "4,99 €/m",
            premiumRawPriceString = "9,99 €/m",
            streamPriceFormatted = "0,49€ par minute"
        ),
        SubRegion(
            id = "XOF",
            name = "Afrique de l'Ouest (UEMOA)",
            flag = "🇨🇮 🇸🇳",
            currencySymbol = "XOF",
            currencyCode = "XOF",
            proPriceFormatted = "2 900 XOF",
            premiumPriceFormatted = "5 900 XOF",
            proNumericPrice = 2900.0,
            premiumNumericPrice = 5900.0,
            proRawPriceString = "2 900 XOF/m",
            premiumRawPriceString = "5 900 XOF/m",
            streamPriceFormatted = "300 XOF par minute"
        ),
        SubRegion(
            id = "XAF",
            name = "Afrique Centrale (CEMAC)",
            flag = "🇨🇲 🇬🇦",
            currencySymbol = "XAF",
            currencyCode = "XAF",
            proPriceFormatted = "2 900 XAF",
            premiumPriceFormatted = "5 900 XAF",
            proNumericPrice = 2900.0,
            premiumNumericPrice = 5900.0,
            proRawPriceString = "2 900 XAF/m",
            premiumRawPriceString = "5 900 XAF/m",
            streamPriceFormatted = "300 XAF par minute"
        ),
        SubRegion(
            id = "MAD",
            name = "Maroc (Maghreb)",
            flag = "🇲🇦",
            currencySymbol = "DH",
            currencyCode = "MAD",
            proPriceFormatted = "49 DH",
            premiumPriceFormatted = "99 DH",
            proNumericPrice = 49.0,
            premiumNumericPrice = 99.0,
            proRawPriceString = "49 DH/m",
            premiumRawPriceString = "99 DH/m",
            streamPriceFormatted = "4.9 DH par minute"
        ),
        SubRegion(
            id = "TND",
            name = "Tunisie (Maghreb)",
            flag = "🇹🇳",
            currencySymbol = "DT",
            currencyCode = "TND",
            proPriceFormatted = "15 DT",
            premiumPriceFormatted = "30 DT",
            proNumericPrice = 15.0,
            premiumNumericPrice = 30.0,
            proRawPriceString = "15 DT/m",
            premiumRawPriceString = "30 DT/m",
            streamPriceFormatted = "1.5 DT par minute"
        ),
        SubRegion(
            id = "DZD",
            name = "Algérie (Maghreb)",
            flag = "🇩🇿",
            currencySymbol = "DA",
            currencyCode = "DZD",
            proPriceFormatted = "690 DA",
            premiumPriceFormatted = "1 390 DA",
            proNumericPrice = 690.0,
            premiumNumericPrice = 1390.0,
            proRawPriceString = "690 DA/m",
            premiumRawPriceString = "1 390 DA/m",
            streamPriceFormatted = "69 DA par minute"
        ),
        SubRegion(
            id = "USD",
            name = "International / USA",
            flag = "🌐 🇺🇸",
            currencySymbol = "$",
            currencyCode = "USD",
            proPriceFormatted = "4.99 $",
            premiumPriceFormatted = "9.99 $",
            proNumericPrice = 4.99,
            premiumNumericPrice = 9.99,
            proRawPriceString = "4.99 $/m",
            premiumRawPriceString = "9.99 $/m",
            streamPriceFormatted = "0.49$ par minute"
        )
    )

    var selectedSubRegionId by mutableStateOf("EUR")

    val currentSubRegion: SubRegion
        get() = subRegionsList.firstOrNull { it.id == selectedSubRegionId } ?: subRegionsList.first()

    val currentLevel: Int
        get() = 1 + kotlin.math.floor(kotlin.math.sqrt(xp.toDouble() / 100.0)).toInt()

    fun claimDailyReward() {
        if (!isDailyClaimed) {
            val baseCoins = 50
            val rewardCoins = baseCoins + (dailyStreak * 10)
            val rewardXp = 20
            coins += rewardCoins
            xp += rewardXp
            isDailyClaimed = true
            dailyStreak += 1
            triggerBeep(1) // Accent confirmation sound
        }
    }

    fun purchaseBadge(badgeName: String, price: Int): Boolean {
        if (!ownedBadges.contains(badgeName) && coins >= price) {
            coins -= price
            ownedBadges = ownedBadges + badgeName
            equippedBadge = badgeName
            triggerBeep(1) // Purchase / Success
            return true
        }
        return false
    }

    fun equipBadge(badgeName: String?) {
        if (badgeName == null || ownedBadges.contains(badgeName)) {
            equippedBadge = badgeName
            triggerBeep(3)
        }
    }

    fun triggerBeep(type: Int) {
        try {
            val tg = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 85)
            when (type) {
                1 -> tg.startTone(ToneGenerator.TONE_PROP_ACK, 220) // Success
                2 -> tg.startTone(ToneGenerator.TONE_PROP_NACK, 320) // Error
                3 -> tg.startTone(ToneGenerator.TONE_PROP_BEEP, 120) // Click / Ping
            }
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                tg.release()
            }, 500)
        } catch (e: Exception) {
            // Ignore if tone generation not supported
        }
    }

    fun speakText(text: String) {
        try {
            triggerBeep(3)
            tts?.speak(text, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null)
        } catch (e: Exception) {
            // Ignore if speak fails
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
    }

    fun triggerVibration(type: Int) {
        if (!settingsHapticEnabled) return
        try {
            val appContext = getApplication<Application>()
            val vibContext = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                appContext.createAttributionContext("vibration")
            } else {
                appContext
            }
            val vibrator = vibContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    when (type) {
                        1 -> { // Success/positive tick
                            vibrator.vibrate(VibrationEffect.createOneShot(55, VibrationEffect.DEFAULT_AMPLITUDE))
                        }
                        2 -> { // Error/failure / wrong move
                            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 120, 80, 120), intArrayOf(0, 180, 0, 180), -1))
                        }
                        3 -> { // Normal tactile grid selection / move tick
                            vibrator.vibrate(VibrationEffect.createOneShot(35, 120))
                        }
                        4 -> { // High-tier level completion / victory
                            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 120, 100, 120, 200), intArrayOf(0, 255, 0, 255, 0, 255), -1))
                        }
                        5 -> { // Downward game-over vibration
                            vibrator.vibrate(VibrationEffect.createOneShot(250, 75))
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    when (type) {
                        1 -> vibrator.vibrate(55)
                        2 -> vibrator.vibrate(longArrayOf(0, 120, 80, 120), -1)
                        3 -> vibrator.vibrate(35)
                        4 -> vibrator.vibrate(longArrayOf(0, 100, 120, 100, 120, 200), -1)
                        5 -> vibrator.vibrate(250)
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore vibration errors
        }
    }

    // --- SOCIAL FEED STATE & ACTIONS ---
    var postsList by mutableStateOf<List<SocialPost>>(emptyList())

    var isAnalyzingPostVirality by mutableStateOf(false)
        private set

    fun addNewPost(content: String, photos: List<String> = emptyList(), videoUrl: String? = null, videoDuration: String? = null, isVoiceRoom: Boolean = false): Int {
        if (content.isBlank() && photos.isEmpty() && videoUrl == null) return -1
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        val timeString = "Aujourd'hui à " + formatter.format(Date())
        val newId = postsList.size + 1
        val newPost = SocialPost(
            id = newId,
            authorName = username,
            authorHandle = userHandle,
            authorBadge = equippedBadge,
            isOfficial = false,
            isVoiceRoom = isVoiceRoom,
            content = content,
            timestamp = timeString,
            likesCount = 0,
            isLikedByMe = false,
            photos = photos,
            videoUrl = videoUrl,
            videoDuration = videoDuration,
            translation = if (content.isNotBlank()) "English Translation (Mirys AI): " + content.replace("Bonjour", "Hello").replace("bonjour", "hello").replace("échecs", "chess").replace("Echecs", "Chess").replace("IA", "AI").replace("bien", "well").replace("merci", "thanks") + " [Translated by Mirys AI]" else null,
            correctedContent = if (content.contains(" ouy ") || content.contains(" a ") || content.contains(" é ") || content.contains(" échecs ")) "Publication corrigée : " + content.replace(" a ", " à ").replace(" échecs ", " échecs ") else null,
            correctionExplanation = if (content.contains(" a ")) "Correction de l'accent sur la préposition 'à'." else null
        )
        postsList = listOf(newPost) + postsList
        xp += 5 // Earn XP for posting!
        triggerBeep(3)
        return newId
    }

    fun toggleLikePost(postId: Int) {
        postsList = postsList.map { post ->
            if (post.id == postId) {
                val wasLiked = post.isLikedByMe
                val diff = if (wasLiked) -1 else 1
                if (!wasLiked) triggerBeep(3)
                post.copy(isLikedByMe = !wasLiked, likesCount = post.likesCount + diff)
            } else post
        }
    }

    fun runPostViralityAnalysis(postId: Int) {
        val post = postsList.find { it.id == postId } ?: return
        if (post.viralityScore != null) return // Already analyzed

        viewModelScope.launch {
            isAnalyzingPostVirality = true
            try {
                val report = repository.analyzePostVirality(post.content)
                postsList = postsList.map { p ->
                    if (p.id == postId) {
                        p.copy(
                            viralityScore = report.score,
                            viralityTier = report.tier,
                            viralityFeedback = report.strengths,
                            viralityAdvice = report.advice
                        )
                    } else p
                }
                
                // Reward coins and XP based on tier !
                val (earnedXp, earnedCoins) = when (report.tier) {
                    "mega_viral" -> 100 to 150
                    "viral" -> 50 to 80
                    "trending" -> 25 to 40
                    "rising" -> 10 to 15
                    else -> 2 to 5
                }
                xp += earnedXp
                coins += earnedCoins
                triggerBeep(1)
            } catch (e: Exception) {
                // Fail gracefully
            } finally {
                isAnalyzingPostVirality = false
            }
        }
    }

    // --- TRIVIA QUIZ ENGINE AND STATES ---
    var quizActiveCat by mutableStateOf<String?>(null)
    var quizActiveLevel by mutableIntStateOf(1)
    var activeQuizQuestions by mutableStateOf<List<QuizQuestion>>(emptyList())
    var quizCurrentIndex by mutableIntStateOf(0)
    var quizCurrentScore by mutableIntStateOf(0)
    var quizCompleted by mutableStateOf(false)
    var selectedOptionIdx by mutableStateOf<Int?>(null)
    var quizStreakMultiplier by mutableIntStateOf(1)
    var quizEarnedCoins by mutableIntStateOf(0)
    var quizEarnedXp by mutableIntStateOf(0)

    fun startQuizSession(category: String, level: Int) {
        val rawQuestions = com.example.data.QuizDatabase.getQuestions(category, level)
        if (rawQuestions.isEmpty()) return
        
        // Auto-translate questions based on app language
        activeQuizQuestions = rawQuestions.map { q ->
            com.example.data.QuizDatabase.getLocalizedQuestion(q, appLanguage)
        }
        
        quizActiveCat = category
        quizActiveLevel = level
        quizCurrentIndex = 0
        quizCurrentScore = 0
        quizCompleted = false
        selectedOptionIdx = null
        quizStreakMultiplier = 1
        quizEarnedCoins = 0
        quizEarnedXp = 0
        triggerBeep(3)
    }

    fun submitQuizAnswer(optIdx: Int) {
        if (selectedOptionIdx != null) return // Already answered
        selectedOptionIdx = optIdx
        val questions = activeQuizQuestions
        if (questions.isEmpty()) return
        val currentQ = questions[quizCurrentIndex]
        
        if (optIdx == currentQ.correctIdx) {
            quizCurrentScore += 1
            quizStreakMultiplier += 1
            triggerBeep(3) // High Correct Beep
            triggerVibration(1) // Light tick success vibration
        } else {
            quizStreakMultiplier = 1
            triggerBeep(2) // Low Error Buzz
            triggerVibration(2) // Rough error buzz vibration
        }
    }

    fun proceedToNextQuizOrFinish() {
        val questions = activeQuizQuestions
        if (questions.isEmpty()) return
        if (quizCurrentIndex < questions.size - 1) {
            quizCurrentIndex += 1
            selectedOptionIdx = null
            triggerVibration(3) // Next question tick
        } else {
            // End session! Calculate payout
            quizCompleted = true
            quizActiveCat = null // Clear active state container (keeps report visible)
            
            // Payout calculation
            val baseCoins = quizCurrentScore * 15
            val streakBonus = (quizCurrentScore * quizStreakMultiplier) / 2
            val totalCoins = baseCoins + streakBonus
            val totalXp = (quizCurrentScore * 20) + 10
            
            coins += totalCoins
            xp += totalXp
            quizEarnedCoins = totalCoins
            quizEarnedXp = totalXp
            triggerBeep(1) // Win Arpeggio acknowledgment
            
            if (quizCurrentScore > 0) {
                triggerVibration(4) // Game/Quiz Victory Double Pulse
            } else {
                triggerVibration(5) // Defeat/Null score downward rumble
            }
        }
    }

    fun resetQuizToHome() {
        quizActiveCat = null
        quizCompleted = false
        quizCurrentIndex = 0
        selectedOptionIdx = null
        triggerBeep(3)
    }

    // --- CHESS VS IA ENGINE STATE & ACTIONS ---
    var isChessActive by mutableStateOf(false)
    var isChessAgainstRealPlayer by mutableStateOf(false)
    var chessOpponentHandle by mutableStateOf("")
    var chessOpponentRating by mutableIntStateOf(1200)
    var selectedChessCell by mutableStateOf<Pair<Int, Int>?>(null) // row, col
    var chessBoardState by mutableStateOf<Array<Array<ChessPiece?>>>(emptyBoard())
    var chessCapturedByPlayer = mutableStateListOf<ChessPiece>()
    var chessCapturedByAi = mutableStateListOf<ChessPiece>()
    var chessPlayerTurn by mutableStateOf(true) // true = player (White), false = AI (Black)
    var moveHistoryList = mutableStateListOf<String>()
    var whiteChessTime by mutableIntStateOf(600) // 10 minutes
    var blackChessTime by mutableIntStateOf(600)
    var chessResultMessage by mutableStateOf<String?>(null)
    var chessAiDifficulty by mutableStateOf("Amateur")

    fun emptyBoard(): Array<Array<ChessPiece?>> {
        val b = Array(8) { Array<ChessPiece?>(8) { null } }
        // Setup Black pawns and pieces
        b[0][0] = ChessPiece("T", false) // Tour
        b[0][1] = ChessPiece("C", false) // Cavalier
        b[0][2] = ChessPiece("F", false) // Fou
        b[0][3] = ChessPiece("D", false) // Dame
        b[0][4] = ChessPiece("R", false) // Roi
        b[0][5] = ChessPiece("F", false) // Fou
        b[0][6] = ChessPiece("C", false) // Cavalier
        b[0][7] = ChessPiece("T", false) // Tour
        for (i in 0..7) b[1][i] = ChessPiece("P", false) // Pion

        // Setup White pawns and pieces
        for (i in 0..7) b[6][i] = ChessPiece("P", true) // Pion
        b[7][0] = ChessPiece("T", true)
        b[7][1] = ChessPiece("C", true)
        b[7][2] = ChessPiece("F", true)
        b[7][3] = ChessPiece("D", true)
        b[7][4] = ChessPiece("R", true)
        b[7][5] = ChessPiece("F", true)
        b[7][6] = ChessPiece("C", true)
        b[7][7] = ChessPiece("T", true)
        return b
    }

    fun startChessMultiplayerGame(): Boolean {
        if (subscriptionTier == "Gratuit") {
            if (coins >= 30) {
                coins -= 30
                triggerBeep(3)
            } else {
                Toast.makeText(getApplication(), "Le mode Multijoueur contre de vraies personnes requiert 30 pièces d'or ou un forfait PRO/Premium !", Toast.LENGTH_LONG).show()
                return false
            }
        }
        
        isChessAgainstRealPlayer = true
        val randomPlayers = listOf(
            Pair("Alane_Coder 🇨🇲", 1450),
            Pair("Sonia_Design 🇸🇳", 1320),
            Pair("Moussa_King 🇨🇮", 1190),
            Pair("Zia_Peace 🇬🇦", 1250)
        )
        val chosen = randomPlayers.random()
        chessOpponentHandle = chosen.first
        chessOpponentRating = chosen.second
        
        chessAiDifficulty = "Multijoueur en Direct"
        chessBoardState = emptyBoard()
        chessCapturedByPlayer.clear()
        chessCapturedByAi.clear()
        moveHistoryList.clear()
        selectedChessCell = null
        chessPlayerTurn = true
        whiteChessTime = 600
        blackChessTime = 600
        chessResultMessage = null
        isChessActive = true
        
        startChessTimers()
        
        val modeText = if (subscriptionTier == "Gratuit") "Déduction de -30 🪙 !" else "Gratuit & Illimité (Abonnement PRO/Premium) !"
        Toast.makeText(getApplication(), "Match trouvé ! Adversaire : @$chessOpponentHandle. Mode : $modeText", Toast.LENGTH_LONG).show()
        return true
    }

    fun startChessGame(difficulty: String) {
        isChessAgainstRealPlayer = false
        chessAiDifficulty = difficulty
        chessBoardState = emptyBoard()
        chessCapturedByPlayer.clear()
        chessCapturedByAi.clear()
        moveHistoryList.clear()
        selectedChessCell = null
        chessPlayerTurn = true
        whiteChessTime = 600
        blackChessTime = 600
        chessResultMessage = null
        isChessActive = true
        
        // Start timers coroutine loop
        startChessTimers()
    }

    private fun startChessTimers() {
        viewModelScope.launch {
            while (isChessActive && chessResultMessage == null) {
                kotlinx.coroutines.delay(1000)
                if (!isChessActive || chessResultMessage != null) break
                if (chessPlayerTurn) {
                    if (whiteChessTime > 0) {
                        whiteChessTime -= 1
                    } else {
                        endChessGame(false, "Temps écoulé !")
                    }
                } else {
                    if (blackChessTime > 0) {
                        blackChessTime -= 1
                    } else {
                        endChessGame(true, "L'IA a manqué de temps !")
                    }
                }
            }
        }
    }

    fun endChessGame(playerWins: Boolean, reason: String) {
        isChessActive = false
        if (playerWins) {
            val eloGain = if (isChessAgainstRealPlayer) 30 else 15
            val coinGain = if (isChessAgainstRealPlayer) 80 else 40
            val xpGain = if (isChessAgainstRealPlayer) 100 else 50
            eloChess += eloGain
            coins += coinGain
            xp += xpGain
            chessResultMessage = if (isChessAgainstRealPlayer) {
                "Victoire Classée contre @$chessOpponentHandle 🎉 (+ $eloGain ELO, + $coinGain 🪙, + $xpGain XP)\n$reason"
            } else {
                "Victoire 🎉 (+ $eloGain ELO, + $coinGain 🪙, + $xpGain XP)\n$reason"
            }
            triggerBeep(1)
            triggerVibration(4) // High double tap victory
        } else {
            val eloLoss = if (isChessAgainstRealPlayer) 20 else 10
            val coinGain = 10
            val xpGain = 20
            eloChess = kotlin.math.max(100, eloChess - eloLoss)
            coins += coinGain
            xp += xpGain
            chessResultMessage = if (isChessAgainstRealPlayer) {
                "Défaite Classée contre @$chessOpponentHandle 💀 (- $eloLoss ELO, + $coinGain 🪙, + $xpGain XP)\n$reason"
            } else {
                "Défaite 💀 (- $eloLoss ELO, + $coinGain 🪙, + $xpGain XP)\n$reason"
            }
            triggerBeep(2)
            triggerVibration(5) // Downward defeat rumble
        }
    }

    fun selectOrCreateChessCell(r: Int, c: Int) {
        val board = chessBoardState
        val selected = selectedChessCell
        if (selected == null) {
            val piece = board[r][c]
            if (piece != null && piece.isWhite && chessPlayerTurn) {
                selectedChessCell = Pair(r, c)
                triggerVibration(3) // Selection tick
            }
        } else {
            val (sr, sc) = selected
            val activePiece = board[sr][sc] ?: return
            val targetPiece = board[r][c]
            
            if (targetPiece != null && targetPiece.isWhite) {
                // Change selection
                selectedChessCell = Pair(r, c)
                triggerVibration(3) // Double click selection change tick
                return
            }
            
            // Check move feasibility
            if (isValidChessMove(sr, sc, r, c, activePiece)) {
                // Complete Move!
                val updated = board.map { it.clone() }.toTypedArray()
                updated[sr][sc] = null
                updated[r][c] = activePiece
                
                if (targetPiece != null) {
                    chessCapturedByPlayer.add(targetPiece)
                    triggerVibration(1) // Strong crunch successful capture vibration
                    if (targetPiece.type == "R") {
                        chessBoardState = updated
                        endChessGame(true, "Vous avez capturé le Roi adverse !")
                        return
                    }
                } else {
                    triggerVibration(3) // Normal move click vibration
                }
                
                // Add to history
                val colNames = listOf("a", "b", "c", "d", "e", "f", "g", "h")
                val moveLog = "${activePiece.type}${colNames[sc]}${8-sr} ➔ ${colNames[c]}${8-r}"
                moveHistoryList.add(moveLog)
                
                selectedChessCell = null
                chessBoardState = updated
                chessPlayerTurn = false
                triggerBeep(3)
                
                // Trigger AI Play
                triggerSelfChessAi()
            } else {
                selectedChessCell = null // Reset selection on invalid click
                triggerVibration(2) // Error invalid target move vibration
                Toast.makeText(getApplication(), "Déplacement incorrect pour cette pièce ! ♟️ Recommencez.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun isPathClear(sr: Int, sc: Int, tr: Int, tc: Int): Boolean {
        val dr = tr - sr
        val dc = tc - sc
        val stepR = if (dr == 0) 0 else dr / kotlin.math.abs(dr)
        val stepC = if (dc == 0) 0 else dc / kotlin.math.abs(dc)
        
        var currR = sr + stepR
        var currC = sc + stepC
        while (currR != tr || currC != tc) {
            if (currR in 0..7 && currC in 0..7) {
                if (chessBoardState[currR][currC] != null) {
                    return false
                }
            } else {
                return false
            }
            currR += stepR
            currC += stepC
        }
        return true
    }

    fun isValidChessMove(sr: Int, sc: Int, tr: Int, tc: Int, piece: ChessPiece): Boolean {
        val dr = kotlin.math.abs(tr - sr)
        val dc = kotlin.math.abs(tc - sc)
        return when (piece.type) {
            "P" -> { // Pawn
                if (piece.isWhite) {
                    val isSingleForward = tc == sc && tr == sr - 1 && chessBoardState[tr][tc] == null
                    val isDoubleForward = tc == sc && sr == 6 && tr == 4 && chessBoardState[5][sc] == null && chessBoardState[4][sc] == null
                    val isDiagonalCapture = dc == 1 && tr == sr - 1 && chessBoardState[tr][tc]?.isWhite == false
                    isSingleForward || isDoubleForward || isDiagonalCapture
                } else {
                    val isSingleForward = tc == sc && tr == sr + 1 && chessBoardState[tr][tc] == null
                    val isDoubleForward = tc == sc && sr == 1 && tr == 3 && chessBoardState[2][sc] == null && chessBoardState[3][sc] == null
                    val isDiagonalCapture = dc == 1 && tr == sr + 1 && chessBoardState[tr][tc]?.isWhite == true
                    isSingleForward || isDoubleForward || isDiagonalCapture
                }
            }
            "T" -> (dr == 0 || dc == 0) && isPathClear(sr, sc, tr, tc) // Rook
            "F" -> (dr == dc) && isPathClear(sr, sc, tr, tc) // Bishop
            "C" -> (dr == 2 && dc == 1) || (dr == 1 && dc == 2) // Knight
            "D" -> (dr == 0 || dc == 0 || dr == dc) && isPathClear(sr, sc, tr, tc) // Queen
            "R" -> dr <= 1 && dc <= 1 // King
            else -> true
        }
    }

    private fun triggerSelfChessAi() {
        viewModelScope.launch {
            kotlinx.coroutines.delay(1000)
            if (chessResultMessage != null || !isChessActive) return@launch
            
            // AI simple greedy search logic!
            val board = chessBoardState
            val possibleMoves = mutableListOf<Triple<Pair<Int, Int>, Pair<Int, Int>, ChessPiece>>()
            
            // Collect all possible AI pieces (Black)
            for (r in 0..7) {
                for (c in 0..7) {
                    val p = board[r][c]
                    if (p != null && !p.isWhite) {
                        // Scan potential target options
                        for (tr in 0..7) {
                            for (tc in 0..7) {
                                if (r == tr && c == tc) continue
                                if (board[tr][tc]?.isWhite == false) continue // Block friendly fire
                                if (isValidChessMove(r, c, tr, tc, p)) {
                                    possibleMoves.add(Triple(Pair(r, c), Pair(tr, tc), p))
                                }
                            }
                        }
                    }
                }
            }
            
            if (possibleMoves.isEmpty()) {
                endChessGame(true, "L'IA n'a plus de mouvements légaux (Pat) !")
                return@launch
            }
            
            // AI strategy: Prioritize capturing player's high value pieces!
            val captureOption = possibleMoves.filter { board[it.second.first][it.second.second]?.isWhite == true }
            val chosenMove = if (captureOption.isNotEmpty()) {
                captureOption.maxByOrNull {
                    val targetType = board[it.second.first][it.second.second]?.type ?: ""
                    when (targetType) {
                        "R" -> 1000
                        "D" -> 90
                        "T" -> 50
                        "C", "F" -> 30
                        else -> 10
                    }
                } ?: captureOption.random()
            } else {
                possibleMoves.random()
            }
            
            val (src, dst, piece) = chosenMove
            val (sr, sc) = src
            val (tr, tc) = dst
            val target = board[tr][tc]
            
            val updated = board.map { it.clone() }.toTypedArray()
            updated[sr][sc] = null
            updated[tr][tc] = piece
            
            if (target != null) {
                chessCapturedByAi.add(target)
                triggerVibration(2) // Alert rumble to notify player of piece loss
                if (target.type == "R") {
                    chessBoardState = updated
                    endChessGame(false, "L'IA a capturé votre Roi !")
                    return@launch
                }
            } else {
                triggerVibration(3) // Tactile move tick for AI's move
            }
            
            val colNames = listOf("a", "b", "c", "d", "e", "f", "g", "h")
            val moveLog = "${piece.type}${colNames[sc]}${8-sr} ➔ ${colNames[tc]}${8-tr}"
            moveHistoryList.add(moveLog)
            
            chessBoardState = updated
            chessPlayerTurn = true
            triggerBeep(3)
        }
    }

    fun quitChessGame() {
        isChessActive = false
        chessResultMessage = null
        triggerBeep(3)
    }

    // --- MEMORY MATCH GAMES STATE & ACTIONS ---
    var isMemoryActive by mutableStateOf(false)
    val memoryCards = androidx.compose.runtime.mutableStateListOf<MemoryCard>()
    val memorySelectedIndices = androidx.compose.runtime.mutableStateListOf<Int>()
    var memoryMovesCount by mutableIntStateOf(0)
    var memoryCompleted by mutableStateOf(false)
    var memoryEarnedCoins by mutableIntStateOf(0)
    var memoryEarnedXp by mutableIntStateOf(0)
    var memoryResultMessage by mutableStateOf<String?>(null)

    fun startMemoryGame() {
        val baseIcons = listOf(
            "Whatshot", "AutoAwesome", "Diamond", "Star", 
            "SmartToy", "Lightbulb", "Favorite", "MusicNote"
        )
        val list = (baseIcons + baseIcons).shuffled().mapIndexed { index, iconName ->
            MemoryCard(
                id = index,
                iconName = iconName,
                isFlipped = false,
                isMatched = false
            )
        }
        memoryCards.clear()
        memoryCards.addAll(list)
        memorySelectedIndices.clear()
        memoryMovesCount = 0
        memoryCompleted = false
        memoryEarnedCoins = 0
        memoryEarnedXp = 0
        memoryResultMessage = null
        isMemoryActive = true
        triggerBeep(3)
    }

    fun selectMemoryCard(index: Int) {
        if (memoryCompleted) return
        if (memorySelectedIndices.size >= 2) return
        if (index in memorySelectedIndices) return
        if (memoryCards[index].isMatched) return
        if (memoryCards[index].isFlipped) return

        memoryCards[index] = memoryCards[index].copy(isFlipped = true)
        memorySelectedIndices.add(index)
        triggerBeep(3)
        triggerVibration(1) // Flipping card crisp tick

        if (memorySelectedIndices.size == 2) {
            memoryMovesCount += 1
            val firstIdx = memorySelectedIndices[0]
            val secondIdx = memorySelectedIndices[1]
            val firstCard = memoryCards[firstIdx]
            val secondCard = memoryCards[secondIdx]

            if (firstCard.iconName == secondCard.iconName) {
                viewModelScope.launch {
                    kotlinx.coroutines.delay(400)
                    memoryCards[firstIdx] = memoryCards[firstIdx].copy(isMatched = true)
                    memoryCards[secondIdx] = memoryCards[secondIdx].copy(isMatched = true)
                    memorySelectedIndices.clear()
                    triggerBeep(1)
                    triggerVibration(1) // Match success pulse

                    if (memoryCards.all { it.isMatched }) {
                        memoryCompleted = true
                        
                        val moves = memoryMovesCount
                        val baseCoins = 30
                        val movesBonus = kotlin.math.max(5, 25 - (moves - 8))
                        val totalCoins = baseCoins + movesBonus
                        val totalXp = 40
                        
                        coins += totalCoins
                        xp += totalXp
                        memoryEarnedCoins = totalCoins
                        memoryEarnedXp = totalXp
                        memoryResultMessage = "Victoire ! Vous avez trouvé toutes les paires en $moves coups ! 🎉 (+ $totalCoins 🪙, + $totalXp XP)"
                        triggerBeep(1)
                        triggerVibration(4) // High level victory double pulse
                    }
                }
            } else {
                viewModelScope.launch {
                    kotlinx.coroutines.delay(1000)
                    if (isMemoryActive) {
                        memoryCards[firstIdx] = memoryCards[firstIdx].copy(isFlipped = false)
                        memoryCards[secondIdx] = memoryCards[secondIdx].copy(isFlipped = false)
                    }
                    memorySelectedIndices.clear()
                    triggerBeep(2)
                    triggerVibration(2) // Mistake error rumble
                }
            }
        }
    }

    fun quitMemoryGame() {
        isMemoryActive = false
        memoryCompleted = false
        memoryResultMessage = null
        triggerBeep(3)
    }

    // --- DB OPERATIONS ---
    fun addJournalEntry(title: String, content: String, moodEmoji: String, moodScore: Int) {
        viewModelScope.launch {
            if (title.isNotBlank() && content.isNotBlank()) {
                val entry = JournalEntry(
                    title = title,
                    content = content,
                    moodEmoji = moodEmoji,
                    moodScore = moodScore
                )
                repository.insertJournalEntry(entry)
                // Proactively analyze mood if list is populated
                analyzeRecentMood()
            }
        }
    }

    fun deleteJournalEntry(id: Int) {
        viewModelScope.launch {
            repository.deleteJournalEntryById(id)
            analyzeRecentMood()
        }
    }

    fun addTask(title: String, priority: String, category: String) {
        viewModelScope.launch {
            if (title.isNotBlank()) {
                val task = Task(title = title, priority = priority, category = category)
                repository.insertTask(task)
            }
        }
    }

    fun toggleTask(id: Int, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.updateTaskStatus(id, isCompleted)
        }
    }

    fun updateTaskCustomColor(id: Int, hexColor: String?) {
        viewModelScope.launch {
            val taskToUpdate = tasks.value.find { it.id == id }
            if (taskToUpdate != null) {
                val updated = taskToUpdate.copy(customBgColorHex = hexColor)
                repository.insertTask(updated)
            }
        }
    }

    fun deleteTask(id: Int) {
        viewModelScope.launch {
            repository.deleteTaskById(id)
        }
    }

    fun deleteCompletedTasks() {
        viewModelScope.launch {
            repository.deleteCompletedTasks()
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            val resetText = when (appLanguage) {
                "EN" -> "All data reset successfully. How can I help you get started again?"
                "ES" -> "Todos los datos se han restablecido con éxito. ¿Cómo puedo ayudarte a comenzar de nuevo?"
                "DE" -> "Alle Daten wurden erfolgreich zurückgesetzt. Wie kann ich dir beim Neustart helfen?"
                "ZH" -> "所有数据已成功重置！我该如何协助您重新出发？"
                "HI" -> "सभी डेटा सफलतापूर्वक रीसेट हो गया। फिर से शुरू करने में मैं आपकी क्या मदद कर सकता हूँ?"
                else -> "Toutes les données ont été réinitialisées avec succès. Comment puis-je vous aider à démarrer à nouveau ?"
            }
            chatMessages.value = listOf(resetText to false)
            aiReport = null
        }
    }

    // AI Operations
    fun generateTasksWithAi(goal: String, durationDays: Int? = null) {
        if (goal.isBlank()) return
        
        // Fee Tier limit check: 5 free designs per day
        if (subscriptionTier == "Gratuit" && freeGoalGenerationsToday >= 5) {
            if (coins >= 50) {
                coins -= 50
                triggerBeep(3)
                // Continue execution below, spending 50 coins
            } else {
                Toast.makeText(getApplication(), "Limite gratuite atteinte (5/jour). Requis: 50 pièces d'or ou abonnement Premium/Pro pour débloquer de futures décompositions !", Toast.LENGTH_LONG).show()
                return
            }
        }

        viewModelScope.launch {
            isGeneratingTasks = true
            try {
                val newTasks = repository.generateAiTasks(goal, durationDays)
                if (newTasks.isNotEmpty()) {
                    repository.insertTasks(newTasks)
                    if (subscriptionTier == "Gratuit") {
                        freeGoalGenerationsToday += 1
                    }
                    val msgText = if (subscriptionTier == "Gratuit") {
                        if (freeGoalGenerationsToday > 5) {
                            "Objectif décomposé par Mirys IA ! (-50 🪙 dépensés)"
                        } else {
                            "Objectif décomposé par Mirys IA ! ($freeGoalGenerationsToday/5 gratuit(s) aujourd'hui)"
                        }
                    } else {
                        "Objectif décomposé par Mirys IA (Premium Illimité) ! ✨"
                    }
                    Toast.makeText(getApplication(), msgText, Toast.LENGTH_LONG).show()
                    triggerBeep(1)
                } else {
                    Toast.makeText(getApplication(), "Mirys IA n'a pas pu structurer d'étapes. Veuillez réessayer avec un objectif plus clair.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(getApplication(), "Une erreur s'est produite lors de l'appel à Mirys IA.", Toast.LENGTH_SHORT).show()
            } finally {
                isGeneratingTasks = false
            }
        }
    }

    fun analyzeRecentMood() {
        viewModelScope.launch {
            val entries = journalEntries.value
            if (entries.isNotEmpty()) {
                isAnalyzingMood = true
                try {
                    val report = repository.analyzeMoodAndJournal(entries)
                    aiReport = report
                } catch (e: Exception) {
                    // Fail gracefully
                } finally {
                    isAnalyzingMood = false
                }
            } else {
                aiReport = null
            }
        }
    }

    fun sendMessageToAi(message: String) {
        if (message.isBlank()) return
        viewModelScope.launch {
            val currentList = chatMessages.value.toMutableList()
            currentList.add(message to true) // user message
            chatMessages.value = currentList
            
            isChatLoading = true
            try {
                val response = repository.chatWithAi(message, currentList)
                val updatedList = chatMessages.value.toMutableList()
                updatedList.add(response to false) // system response
                chatMessages.value = updatedList
            } catch (e: Exception) {
                val updatedList = chatMessages.value.toMutableList()
                val errText = when (appLanguage) {
                    "EN" -> "Sorry, I encountered an internet connection issue."
                    "ES" -> "Lo siento, encontré un problema con la conexión a internet."
                    "DE" -> "Entschuldigung, es gab ein Problem mit der Internetverbindung."
                    "ZH" -> "抱歉，遇到网络连接问题，请稍后重试。"
                    "HI" -> "क्षमा करें, मुझे नेटवर्क एक्सेस में समस्या आ रही है।"
                    else -> "Désolé, j'ai rencontré un problème d'accès réseau."
                }
                updatedList.add(errText to false)
                chatMessages.value = updatedList
            } finally {
                isChatLoading = false
            }
        }
    }

    fun clearChat() {
        val clearText = when (appLanguage) {
            "EN" -> "Chat history cleared. I'm ready for our next conversation!"
            "ES" -> "Historial borrado. ¡Estoy listo para una nueva conversación!"
            "DE" -> "Chatverlauf gelöscht. Ich bin bereit für ein neues Gespräch!"
            "ZH" -> "聊天历史已清除。我已经准备好进行新的对话了！"
            "HI" -> "चैट इतिहास हटा दिया गया। मैं एक नई बातचीत के लिए तैयार हूँ!"
            else -> "Historique effacé. Je suis prêt pour une nouvelle discussion !"
        }
        chatMessages.value = listOf(clearText to false)
    }

    // --- 10 PREMIUM HUB HELPER METHODS ---
    fun selectAuraSkin(skinName: String) {
        activatedAuraSkin = skinName
        triggerBeep(1)
        triggerVibration(2) // tactile click
    }

    fun setMoodHaloStatus(haloName: String?) {
        activeMoodHalo = haloName
        triggerBeep(1)
        triggerVibration(1)
    }

    fun triggerPremiumDreamOracle(dreamText: String) {
        if (dreamText.isBlank()) return
        isDreamOracleAnalyzing = true
        viewModelScope.launch {
            try {
                val result = repository.analyzeDreamWithIA(dreamText)
                premiumDreamAnalysisResult = result.first
                premiumDreamOracleCard = result.second
                premiumDreamMoodColorHex = result.third
                triggerBeep(2)
                triggerVibration(4)
            } catch (e: Exception) {
                premiumDreamAnalysisResult = "Une vibration céleste a perturbé notre Oracle. Veuillez réessayer."
                premiumDreamOracleCard = "La Tempête ⚡"
                premiumDreamMoodColorHex = "#EF4444"
            } finally {
                isDreamOracleAnalyzing = false
            }
        }
    }

    suspend fun runPremiumTranslation(text: String, targetLang: String): String {
        return repository.translateText(text, targetLang)
    }

    fun scanPremiumRadar(onDone: () -> Unit) {
        premiumRadarScanning = true
        viewModelScope.launch {
            kotlinx.coroutines.delay(2500)
            premiumRadarScanning = false
            onDone()
            triggerBeep(1)
            triggerVibration(5)
        }
    }

    fun generatePremiumQuestChoice(choiceIdx: Int) {
        premiumStoryStep = choiceIdx
        premiumStoryPoints += 15
        coins += 10
        triggerBeep(1)
        triggerVibration(3)
    }

    fun playPremiumAmbientSound(theme: String) {
        premiumSoundscapeTheme = theme
        if (theme != "none") {
            when (theme) {
                "serenity" -> {
                    triggerBeep(2) // Play Serene 440hz tone
                }
                "nebula" -> {
                    triggerBeep(3) // Cosmic 660hz tone
                }
                "fire" -> {
                    triggerBeep(1) // High energy tone
                }
            }
        }
    }

    fun resetPremiumQuest() {
        premiumStoryStep = 1
        premiumStoryPoints = 0
        triggerBeep(3)
    }
}

// --- DATA MODELS FOR MIRYS v2.0 GAME ENGINE ---
data class SocialPost(
    val id: Int,
    val authorName: String,
    val authorHandle: String,
    val authorBadge: String?,
    val isOfficial: Boolean,
    val isVoiceRoom: Boolean,
    val content: String,
    val timestamp: String,
    val likesCount: Int,
    val isLikedByMe: Boolean,
    val viralityScore: Int? = null,
    val viralityTier: String? = null,
    val viralityFeedback: String? = null,
    val viralityAdvice: String? = null,
    val photos: List<String> = emptyList(),
    val videoUrl: String? = null,
    val videoDuration: String? = null,
    val translation: String? = null,
    val correctedContent: String? = null,
    val correctionExplanation: String? = null
)

data class QuizQuestion(
    val qText: String,
    val options: List<String>,
    val correctIdx: Int,
    val explanation: String
)

data class ChessPiece(
    val type: String, // "P" (Pawn), "T" (Rook), "C" (Knight), "F" (Bishop), "D" (Queen), "R" (King)
    val isWhite: Boolean
)

data class MemoryCard(
    val id: Int,
    val iconName: String,
    val isFlipped: Boolean = false,
    val isMatched: Boolean = false
)

data class DirectMessage(
    val id: Int,
    val senderHandle: String,
    val content: String,
    val timestamp: String,
    val isVoiceNote: Boolean = false,
    val voiceNoteDuration: Int = 0,
    val photoUrl: String? = null,
    val isVideo: Boolean = false,
    val videoUrl: String? = null
)

