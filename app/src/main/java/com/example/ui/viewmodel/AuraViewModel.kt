package com.example.ui.viewmodel

import android.app.Application
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
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

class AuraViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AuraRepository

    // Stream for journal records
    val journalEntries: StateFlow<List<JournalEntry>>
    // Stream for tasks
    val tasks: StateFlow<List<Task>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AuraRepository(database.journalDao(), database.taskDao())
        
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
                if (activeCallPartnerHandle != null && callStatus == "En direct") {
                    callDurationSeconds += 1
                    if (callDurationSeconds >= 12 && subscriptionTier == "Gratuit") {
                        callStatus = "Limite standard dépassée ! Mises à niveau premium requises pour les communications vocales et vidéo prolongées (+3 min)."
                        triggerBeep(2)
                    }
                }
            }
        }
    }

    // --- DEVELOPER / CREATOR / MEMBERS DIRECT SOCIAL STATE ---
    var designerAccountUnlocked by mutableStateOf(false)
    var activeUserProfileHandle by mutableStateOf<String?>(null)
    var followedHandles by mutableStateOf<Set<String>>(setOf("mirys_team"))
    var profileSubscribersCount by mutableStateOf<Map<String, Int>>(mapOf(
        "mirys_team" to 8240,
        "sonia_coder" to 542,
        "chess_club_yaounde" to 198,
        "mirysofficiel" to 99999
    ))

    // Direct Messages system
    var directMessagesMap by mutableStateOf<Map<String, List<DirectMessage>>>(mapOf(
        "sonia_coder" to listOf(
            DirectMessage(1, "sonia_coder", "Salut ! Bravo pour ton score au quiz d'histoire hier ! Tu es super fort ! 🧠", "14:15"),
            DirectMessage(2, "mon_compte", "Ah merci Sonia ! Je révisais un peu en fait.", "14:18"),
            DirectMessage(3, "sonia_coder", "Génial ! n'hésite pas à me lancer un appel vidéo ou audio si tu souhaites qu'on révise ensemble, ou on peut démarrer un Salon Vocal dans les Moments !", "14:20")
        ),
        "chess_club_yaounde" to listOf(
            DirectMessage(1, "chess_club_yaounde", "Bonjour ! Êtes-vous intéressé par notre prochain tournoi de Blitz ce samedi ?", "09:30")
        ),
        "mirys_team" to listOf(
            DirectMessage(1, "mirys_team", "Bienvenue sur l'onglet messagerie ! Vous pouvez discuter, faire des appels audio/vidéo ou même héberger des salons vocaux interactifs ! 😉", "Hier")
        )
    ))

    // Immersive calls states
    var activeCallPartnerHandle by mutableStateOf<String?>(null)
    var activeCallType by mutableStateOf<String?>(null) // "audio", "video", "voiceroom", "videoroom"
    var isCallMuted by mutableStateOf(false)
    var isCallVideoEnabled by mutableStateOf(true)
    var isCallSpeakerOn by mutableStateOf(false)
    var callDurationSeconds by mutableIntStateOf(0)
    var callStatus by mutableStateOf("Connexion...") // "Connexion...", "En direct", "Terminé", "Limite dépassée"

    fun tryDesignerLogin(email: String, code: String): Boolean {
        if (email.trim().lowercase() == "alanementii73@gmail.com" && code == "#Einstein68") {
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
        if (followedHandles.contains(handle)) {
            followedHandles = followedHandles - handle
            val currentCount = profileSubscribersCount[handle] ?: 0
            profileSubscribersCount = profileSubscribersCount + (handle to kotlin.math.max(0, currentCount - 1))
        } else {
            followedHandles = followedHandles + handle
            val currentCount = profileSubscribersCount[handle] ?: 0
            profileSubscribersCount = profileSubscribersCount + (handle to (currentCount + 1))
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

    fun sendDirectMessage(partnerHandle: String, text: String, isVoiceNote: Boolean = false, voiceNoteDuration: Int = 0) {
        if (text.isBlank() && !isVoiceNote) return
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        val timeStr = formatter.format(Date())
        
        val newMsg = DirectMessage(
            id = (directMessagesMap[partnerHandle]?.size ?: 0) + 1,
            senderHandle = userHandle,
            content = if (isVoiceNote) "🎤 Message vocal ($voiceNoteDuration s)" else text,
            timestamp = timeStr,
            isVoiceNote = isVoiceNote,
            voiceNoteDuration = voiceNoteDuration
        )
        
        val existing = directMessagesMap[partnerHandle] ?: emptyList()
        directMessagesMap = directMessagesMap + (partnerHandle to (existing + newMsg))
        triggerBeep(3)
        
        // Custom interactive simulation responses
        viewModelScope.launch {
            kotlinx.coroutines.delay(1500)
            val replyText = when (partnerHandle) {
                "sonia_coder" -> {
                    if (text.lowercase().contains("salut") || text.lowercase().contains("hey") || text.lowercase().contains("bonjour")) {
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
                else -> "Fascinant ! Merci de ton partage. Passe une très bonne journée ! 🌟"
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
            addNewPost(content = contentStr)
        }
    }

    fun hangUpCall() {
        activeCallPartnerHandle = null
        activeCallType = null
        callStatus = "Terminé"
        triggerBeep(2)
    }

    // --- NAVIGATION TABS SYSTEM ---
    var currentTab by mutableStateOf("dashboard") // "dashboard", "feed", "games", "talk", "agenda", "shop"
        private set

    fun selectTab(tab: String) {
        currentTab = tab
    }

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
    var coins by mutableIntStateOf(200)
    var xp by mutableIntStateOf(35)
    var eloChess by mutableIntStateOf(1200)
    var equippedBadge by mutableStateOf<String?>(null)
    var ownedBadges by mutableStateOf<Set<String>>(setOf("Default"))
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

    // --- SOCIAL FEED STATE & ACTIONS ---
    var postsList by mutableStateOf<List<SocialPost>>(
        listOf(
            SocialPost(
                id = 1,
                authorName = "Annonce Officielle Mirys",
                authorHandle = "mirys_team",
                authorBadge = "Champion 👑",
                isOfficial = true,
                isVoiceRoom = false,
                content = "Bienvenue sur Mirys v2.0 ! 🎉 Préparez-vous à relever des défis incroyables. Testez votre culture dans l'onglet des Quiz Trivia, affrontez notre IA aux Échecs tactiques et collectez des pièces pour débloquer de magnifiques badges animés dans la Boutique !",
                timestamp = "A l'instant",
                likesCount = 124,
                isLikedByMe = false,
                viralityScore = 98,
                viralityTier = "mega_viral",
                viralityFeedback = "Une annonce majeure et engageante avec une structure impeccable de communication.",
                photos = listOf("https://images.unsplash.com/photo-1545235617-9465d2a55698?q=80&w=600"),
                translation = "Welcome to Mirys v2.0! 🎉 Get ready to take on amazing challenges. Test your knowledge in the Trivia Quiz section, play against our AI in tactical Chess, and collect coins to unlock gorgeous badges in the Shop!"
            ),
            SocialPost(
                id = 2,
                authorName = "Sonia Tech",
                authorHandle = "sonia_coder",
                authorBadge = "Flame 🔥",
                isOfficial = false,
                isVoiceRoom = false,
                content = "J'adore l'analyse de bien-être de Mirys. Après avoir rédigé mes notes de journal, le débrief par l'IA me donne exactement les bons conseils pour ma journée ! Qui d'autre a testé l'IA aujourd'hui ? ⚡ #Aura #MirysIA",
                timestamp = "Il y a 2 heures",
                likesCount = 38,
                isLikedByMe = false,
                viralityScore = 54,
                viralityTier = "trending",
                viralityFeedback = "Post cibé avec hashtags actifs et engagement authentique de communauté.",
                photos = listOf(
                    "https://images.unsplash.com/photo-1506126613408-eca07ce68773?q=80&w=600",
                    "https://images.unsplash.com/photo-1518241353330-0f7941c2d9b5?q=80&w=600"
                ),
                translation = "I absolutely love Mirys' wellness analysis. After writing my journal entries, the AI debrief gives me the exact advice of the day! Who else tried the AI today? ⚡ #Aura #MirysIA"
            ),
            SocialPost(
                id = 3,
                authorName = "Lycée National Chess Club",
                authorHandle = "chess_club_yaounde",
                authorBadge = "Galaxy 🌌",
                isOfficial = false,
                isVoiceRoom = true,
                content = "Room Vocale active chez Mirys ! Thème : Les ouvertures d'échecs légendaires. Rejoignez le micro ou posez vos questions en direct sur l'app ! 🎙️♟️ #EchecsTactiques",
                timestamp = "Il y a 4 heures",
                likesCount = 17,
                isLikedByMe = false,
                viralityScore = 42,
                viralityTier = "rising",
                viralityFeedback = "Idée thématique intéressante pour animer des groupes d'apprentissage.",
                videoUrl = "https://assets.mixkit.co/videos/preview/mixkit-starry-night-sky-over-mountains-34440-large.mp4",
                videoDuration = "0:24",
                translation = "Active Voice Room on Mirys! Theme: Legendary chess openings. Join the microphone or ask your questions live on the app! 🎙️♟️ #TacticalChess",
                correctedContent = "Salon vocal actif chez Mirys ! Thème : Les ouvertures d'échecs de légende. Rejoignez-nous au micro ou posez vos questions en direct sur l’application ! 🎙️♟️ #EchecsTactiques",
                correctionExplanation = "Remplacement de l'anglicisme 'Room Vocale' par 'Salon vocal', accord naturel 'ouvertures d'échecs de légende' au lieu de 'ouvertures légendaires', et 'l'application' au lieu de 'l'app'."
            )
        )
    )

    var isAnalyzingPostVirality by mutableStateOf(false)
        private set

    fun addNewPost(content: String, photos: List<String> = emptyList(), videoUrl: String? = null, videoDuration: String? = null) {
        if (content.isBlank() && photos.isEmpty() && videoUrl == null) return
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        val timeString = "Aujourd'hui à " + formatter.format(Date())
        val newPost = SocialPost(
            id = postsList.size + 1,
            authorName = username,
            authorHandle = userHandle,
            authorBadge = equippedBadge,
            isOfficial = false,
            isVoiceRoom = false,
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
    var quizCurrentIndex by mutableIntStateOf(0)
    var quizCurrentScore by mutableIntStateOf(0)
    var quizCompleted by mutableStateOf(false)
    var selectedOptionIdx by mutableStateOf<Int?>(null)
    var quizStreakMultiplier by mutableIntStateOf(1)
    var quizEarnedCoins by mutableIntStateOf(0)
    var quizEarnedXp by mutableIntStateOf(0)

    val quizQuestions = mapOf(
        "Sciences" to listOf(
            QuizQuestion(
                "Quelle particule élémentaire tourne autour du noyau de l'atome ?",
                listOf("Le proton", "L'électron", "Le neutron", "Le quark"),
                1,
                "L'électron est une particule chargée négativement qui orbite autour du noyau atomique."
            ),
            QuizQuestion(
                "Combien de temps faut-il environ à la lumière du Soleil pour atteindre la Terre ?",
                listOf("8 secondes", "8 minutes", "8 heures", "8 jours"),
                1,
                "La lumière voyageant à 300 000 km/s met environ 8 minutes et 20 secondes pour parcourir la distance Terre-Soleil."
            ),
            QuizQuestion(
                "Quelle planète du système solaire possède le plus de lunes ?",
                listOf("Mars", "Jupiter", "Saturne", "Neptune"),
                2,
                "Saturne compte plus de 140 lunes confirmées, dépassant Jupiter."
            ),
            QuizQuestion(
                "Quel organe produit l'insuline dans le corps humain ?",
                listOf("Le foie", "Le pancréas", "Les reins", "La rate"),
                1,
                "Le pancréas sécrète l'insuline régulant le taux de glucose sanguin."
            ),
            QuizQuestion(
                "Quel gaz est le principal constituant de l'atmosphère terrestre ?",
                listOf("L'oxygène", "Le dioxyde de carbone", "L'azote (Diazote)", "L'argon"),
                2,
                "L'azote constitue environ 78% de l'atmosphère de la Terre, devant l'oxygène (21%)."
            )
        ),
        "Histoire" to listOf(
            QuizQuestion(
                "En quelle année la Révolution française a-t-elle commencé ?",
                listOf("1776", "1789", "1804", "1815"),
                1,
                "La Révolution française a commencé en 1789 avec l'ouverture des États généraux."
            ),
            QuizQuestion(
                "Qui était le premier président des États-Unis ?",
                listOf("Thomas Jefferson", "Abraham Lincoln", "George Washington", "Benjamin Franklin"),
                2,
                "George Washington a été élu premier président américain en 1789."
            ),
            QuizQuestion(
                "Quelle civilisation antique a construit le temple du Parthénon ?",
                listOf("Les Égyptiens", "Les Grecs", "Les Romains", "Les Mayas"),
                1,
                "Les Athéniens ont érigé le Parthénon sur l'Acropole d'Athènes au Ve siècle av. J.-C."
            ),
            QuizQuestion(
                "Quel navigateur a mené le premier voyage de circumnavigation de la Terre ?",
                listOf("Christophe Colomb", "Vasco de Gama", "Fernand de Magellan", "Jacques Cartier"),
                2,
                "Magellan a initié l'expédition en 1519, achevée par Elcano en 1522."
            ),
            QuizQuestion(
                "Quelle reine régnait sur le Royaume-Uni pendant la majeure partie du XIXe siècle ?",
                listOf("Reine Élisabeth Ire", "Reine Victoria", "Reine Élisabeth II", "Reine Anne"),
                1,
                "Le règne de la reine Victoria s'est étendu de 1837 à 1901 (époque victorienne)."
            )
        ),
        "Echecs" to listOf(
            QuizQuestion(
                "Quelle pièce a pour particularité de pouvoir sauter par-dessus d'autres pièces ?",
                listOf("La dame", "Le fou", "La tour", "Le cavalier"),
                3,
                "Le cavalier est la seule pièce capable de passer au-dessus des autres lors de ses mouvements en L."
            ),
            QuizQuestion(
                "Combien de cases y a-t-il sur un échiquier standard ?",
                listOf("32", "48", "64", "81"),
                2,
                "Un échiquier se compose de 8 colonnes et 8 rangées, soit 64 cases alternatives claires et sombres."
            ),
            QuizQuestion(
                "Comment appelle-t-on le coup spécial impliquant simultanément le Roi et une Tour ?",
                listOf("Le roque", "La promotion", "La prise en passant", "La fourchette"),
                0,
                "Le roque permet d'abriter le Roi et d'activer la Tour d'un seul coup sous conditions."
            ),
            QuizQuestion(
                "Quelle pièce ne peut jamais se déplacer sur une case de couleur différente ?",
                listOf("Le roi", "Le pion", "Le fou", "La tour"),
                2,
                "Les fous restent à vie fidèles à leur couleur de case d'origine."
            ),
            QuizQuestion(
                "Quelle pièce possède la plus grande liberté de mouvement et de valeur tactique ?",
                listOf("Le roi", "La tour", "Le cavalier", "La dame"),
                3,
                "La dame combine les mouvements latéraux de la tour et diagonaux du fou."
            )
        ),
        "Culture" to listOf(
            QuizQuestion(
                "Qui a peint la Joconde ?",
                listOf("Michel-Ange", "Léonard de Vinci", "Pablo Picasso", "Vincent van Gogh"),
                1,
                "L'œuvre iconique Mona Lisa a été réalisée par Léonard de Vinci au début du XVIe siècle."
            ),
            QuizQuestion(
                "Dans quel pays se trouve la célèbre attraction de la Tour de Pise ?",
                listOf("En France", "En Espagne", "En Italie", "En Grèce"),
                2,
                "La tour penchée est le campanile de la cathédrale de Pise, en Toscane, Italie."
            ),
            QuizQuestion(
                "Quel écrivain français a rédigé 'Les Misérables' ?",
                listOf("Émile Zola", "Gustave Flaubert", "Victor Hugo", "Albert Camus"),
                2,
                "Victor Hugo est le romancier phare ayant brossé le portrait social de Jean Valjean en 1862."
            ),
            QuizQuestion(
                "De quel groupe de musique légendaire faisaient partie John Lennon et Paul McCartney ?",
                listOf("The Rolling Stones", "The Beatles", "Pink Floyd", "Led Zeppelin"),
                1,
                "The Beatles, formés à Liverpool, sont entrés dans la légende du rock mondial."
            ),
            QuizQuestion(
                "Quel est le plus grand océan de notre planète ?",
                listOf("L'océan Atlantique", "L'océan Indien", "L'océan Pacifique", "L'océan Arctique"),
                2,
                "L'océan Pacifique couvre à lui seul plus de 30% de la surface mondiale."
            )
        )
    )

    fun startQuizSession(category: String) {
        quizQuestions[category] ?: return
        quizActiveCat = category
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
        val questions = quizQuestions[quizActiveCat] ?: return
        val currentQ = questions[quizCurrentIndex]
        
        if (optIdx == currentQ.correctIdx) {
            quizCurrentScore += 1
            quizStreakMultiplier += 1
            triggerBeep(3) // High Correct Beep
        } else {
            quizStreakMultiplier = 1
            triggerBeep(2) // Low Error Buzz
        }
    }

    fun proceedToNextQuizOrFinish() {
        val questions = quizQuestions[quizActiveCat] ?: return
        if (quizCurrentIndex < questions.size - 1) {
            quizCurrentIndex += 1
            selectedOptionIdx = null
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

    fun startChessGame(difficulty: String) {
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
            val eloGain = 15
            val coinGain = 40
            val xpGain = 50
            eloChess += eloGain
            coins += coinGain
            xp += xpGain
            chessResultMessage = "Victoire 🎉 (+ $eloGain ELO, + $coinGain 🪙, + $xpGain XP)\n$reason"
            triggerBeep(1)
        } else {
            val eloLoss = 10
            val coinGain = 5
            val xpGain = 10
            eloChess = kotlin.math.max(100, eloChess - eloLoss)
            coins += coinGain
            xp += xpGain
            chessResultMessage = "Défaite 💀 (- $eloLoss ELO, + $coinGain 🪙, + $xpGain XP)\n$reason"
            triggerBeep(2)
        }
    }

    fun selectOrCreateChessCell(r: Int, c: Int) {
        val board = chessBoardState
        val selected = selectedChessCell
        if (selected == null) {
            val piece = board[r][c]
            if (piece != null && piece.isWhite && chessPlayerTurn) {
                selectedChessCell = Pair(r, c)
            }
        } else {
            val (sr, sc) = selected
            val activePiece = board[sr][sc] ?: return
            val targetPiece = board[r][c]
            
            if (targetPiece != null && targetPiece.isWhite) {
                // Change selection
                selectedChessCell = Pair(r, c)
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
                    if (targetPiece.type == "R") {
                        chessBoardState = updated
                        endChessGame(true, "Vous avez capturé le Roi adverse !")
                        return
                    }
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
                if (target.type == "R") {
                    chessBoardState = updated
                    endChessGame(false, "L'IA a capturé votre Roi !")
                    return@launch
                }
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
            chatMessages.value = listOf(
                "Toutes les données ont été réinitialisées avec succès. Comment puis-je vous aider à démarrer à nouveau ?" to false
            )
            aiReport = null
        }
    }

    // AI Operations
    fun generateTasksWithAi(goal: String) {
        if (goal.isBlank()) return
        viewModelScope.launch {
            isGeneratingTasks = true
            try {
                val newTasks = repository.generateAiTasks(goal)
                if (newTasks.isNotEmpty()) {
                    repository.insertTasks(newTasks)
                }
            } catch (e: Exception) {
                // Handled in repository, just clear generating state
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
                updatedList.add("Désolé, j'ai rencontré un problème d'accès réseau." to false)
                chatMessages.value = updatedList
            } finally {
                isChatLoading = false
            }
        }
    }

    fun clearChat() {
        chatMessages.value = listOf(
            "Historique effacé. Je suis prêt pour une nouvelle discussion !" to false
        )
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
    val voiceNoteDuration: Int = 0
)

