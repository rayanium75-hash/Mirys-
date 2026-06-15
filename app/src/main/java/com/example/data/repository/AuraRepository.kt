package com.example.data.repository

import android.util.Log
import com.example.BuildConfig
import com.example.data.dao.JournalDao
import com.example.data.dao.TaskDao
import com.example.data.model.JournalEntry
import com.example.data.model.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AuraRepository(
    private val journalDao: JournalDao,
    private val taskDao: TaskDao
) {
    // Flows exposed to UI
    val allJournalEntries: Flow<List<JournalEntry>> = journalDao.getAllEntriesStream()
    val allTasks: Flow<List<Task>> = taskDao.getAllTasksStream()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

    // Database Actions
    suspend fun insertJournalEntry(entry: JournalEntry) = withContext(Dispatchers.IO) {
        journalDao.insertEntry(entry)
    }

    suspend fun deleteJournalEntryById(id: Int) = withContext(Dispatchers.IO) {
        journalDao.deleteEntryById(id)
    }

    suspend fun insertTask(task: Task) = withContext(Dispatchers.IO) {
        taskDao.insertTask(task)
    }

    suspend fun insertTasks(tasks: List<Task>) = withContext(Dispatchers.IO) {
        taskDao.insertTasks(tasks)
    }

    suspend fun updateTaskStatus(id: Int, isCompleted: Boolean) = withContext(Dispatchers.IO) {
        taskDao.updateTaskStatus(id, isCompleted)
    }

    suspend fun deleteTaskById(id: Int) = withContext(Dispatchers.IO) {
        taskDao.deleteTaskById(id)
    }

    suspend fun deleteCompletedTasks() = withContext(Dispatchers.IO) {
        taskDao.deleteCompletedTasks()
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        journalDao.clearAllEntries()
        taskDao.clearAllTasks()
    }

    /**
     * Call Gemini to generate a smart AI task checklist from a user goal in French, plan over a specified duration
     */
    suspend fun generateAiTasks(goal: String, durationDays: Int? = null): List<Task> = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }
        
        // Logical default duration if not specified
        val durationInfo = if (durationDays != null && durationDays > 0) {
            "S'il te plaît, planifie rigoureusement cet objectif sur une durée de EXACTEMENT $durationDays jours."
        } else {
            "L'utilisateur n'ayant pas spécifié de durée précise pour l'objectif, planifie-le sur une durée par défaut située entre 10 jours et 1 mois (ex: 15 à 30 jours), avec des tâches réparties chronologiquement."
        }

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w("AuraRepository", "API Key is empty, using offline mock mode with duration planning")
            return@withContext getMockTasksForGoalWithDuration(goal, durationDays)
        }

        val prompt = System.getenv("BUILD_ENV")?.let { "" } ?: """
            Tu es Mirys AI, un assistant d'organisation personnel et de bien-être intelligent, créé par l'entreprise rayanium68 de l'auteur Alane Mentii. 
            L'utilisateur souhaite accomplir l'objectif suivant : "$goal".
            
            $durationInfo
            Génère exactement entre 4 et 6 tâches étapes concrètes, étalées chronologiquement (ex: en incluant un repère de jour/semaine au début du titre, ex: "[Jours 1-5] Préparer le réajustement de l'assiette", "[Semaine 2] Intensifier le rythme") pour atteindre cet objectif.
            
            Réponds EXCLUSIVEMENT sous ce format strict pour me permettre de le parser, une tâche par ligne :
            - [TITRE] Titre de la tâche (incluant son échéance planifiée) | [PRIORITE] Haute (ou Moyenne ou Basse) | [CATEGORIE] Bien-être (ou Personnel ou Études ou Général) | [DETAIL] Guide méthodique d'accomplissement extrêmement complet, exhaustif, clair et très développé pour l'utilisateur :\\n- Expliquer précisément le COMMENT ET POURQUOI avec un premier tiret d'explication de méthode.\\n- Fournir des instructions rigoureuses étape par étape avec un deuxième tiret d'action concrète.\\n- Ajouter des conseils précieux ou des mesures de bien-être mental / d'organisation avec un troisième tiret. (Utilise obligatoirement le séparateur '\\n' pour diviser chaque tiret sur une nouvelle ligne).
            
            Exemple :
            - [TITRE] [Jours 1-3] Installer et configurer l'environnement de code | [PRIORITE] Haute | [CATEGORIE] Général | [DETAIL] Préparation complète de toute votre chaîne d'outillage technologique pour partir sur des bases saines :\\n- Étape principale : Téléchargez et installez l'IDE recommandé compatible avec votre système de fichiers local.\\n- Échafaudage : Lancez un premier projet 'Hello World' vierge de test de compilation afin de valider et tester toute la chaîne d'outils d'exécution.\\n- Précautions : Prenez des notes sur les erreurs rencontrées pour construire une documentation de référence.
            - [TITRE] [Semaine 2] Pratiquer 15 minutes d'étirement régulier | [PRIORITE] Basse | [CATEGORIE] Bien-être | [DETAIL] Rituel matinal structuré de réveil musculaire doux et d'alignement physique :\\n- Posture : Effectuez des mouvements lents et circulaires au niveau du cou, des épaules et du bassin d'échauffement.\\n- Fluidité : Travaillez la respiration profonde en maintenant chaque étirement léger sans forcer de manière excessive.\\n- Amélioration : Prenez un grand verre d'eau de source immédiatement après la fin de la séance pour réhydrater l'organisme.
            
            Ne mets aucune introduction ni conclusion, aucun en-tête ni fioriture. Réponds en Français de manière concise.
        """.trimIndent()

        try {
            val responseText = callGeminiApi(prompt)
            if (responseText.startsWith("Error:") || responseText.isBlank()) {
                return@withContext getMockTasksForGoal(goal)
            }
            val tasks = parseTasksFromAi(responseText)
            if (tasks.isEmpty()) {
                return@withContext getMockTasksForGoal(goal)
            }
            tasks
        } catch (e: Exception) {
            Log.e("AuraRepository", "Error generating tasks with IA", e)
            getMockTasksForGoal(goal)
        }
    }

    /**
     * Ask Gemini to analyze recent journal entries and generate a mental state audit report
     */
    suspend fun analyzeMoodAndJournal(entries: List<JournalEntry>): AiReport = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || entries.isEmpty()) {
            return@withContext getOfflineReport(entries)
        }

        val entriesText = entries.take(10).joinToString("\n---\n") { entry ->
            "Date: ${entry.timestamp} | Humeur: ${entry.moodEmoji} (Note: ${entry.moodScore}/5) | Titre: ${entry.title}\nContenu: ${entry.content}"
        }

        val prompt = """
            Tu es Mirys AI, un compagnon bienveillant d'intelligence émotionnelle, d'organisation et de bien-être, développé par l'entreprise rayanium68 d'Alane Mentii.
            Voici les notes récentes du journal intime et de suivi d'humeur de l'utilisateur :
            
            $entriesText
            
            Analyse ces entrées d'humeur et synthétise son état d'esprit global.
            Réponds EXCLUSIVEMENT sous ce format de balises strict pour me permettre de parser ta réponse :
            
            [SCORE] un chiffre unique de 1 à 100 représentant l'indice de bien-être physique et émotionnel général.
            [SYNTHESE] Une synthèse de 3 à 4 phrases encourageante, constructive, chaleureuse et perspicace en français analysant ses humeurs et ses pensées récentes.
            [CONSEIL_1] Un premier conseil concret, personnalisé, orienté action ou bien-être pour la journée.
            [CONSEIL_2] Un deuxième conseil concret, personnalisé de respiration ou de gestion émotionnelle.
            [CONSEIL_3] Un troisième conseil d'activité physique ou d'organisation du temps de travail.
            
            Rappelles-toi d'être extrêmement direct, ne débute pas par des phrases de politesse comme "Bonjour l'utilisateur" ou "Voici l'analyse", écris directement le format balisé.
        """.trimIndent()

        try {
            val responseText = callGeminiApi(prompt)
            if (responseText.startsWith("Error:") || responseText.isBlank()) {
                return@withContext getOfflineReport(entries)
            }
            parseReportFromAi(responseText)
        } catch (e: Exception) {
            Log.e("AuraRepository", "Error compiling mood report with IA", e)
            getOfflineReport(entries)
        }
    }

    /**
     * Ask Gemini general supportive chat question
     */
    suspend fun chatWithAi(userMessage: String, history: List<Pair<String, Boolean>>): String = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            val msgLower = userMessage.lowercase()
            return@withContext when {
                // English check
                msgLower.contains("hello") || msgLower.contains("hi") || msgLower.contains("how ") || 
                msgLower.contains("you") || msgLower.contains("who") || msgLower.contains("what") || 
                msgLower.contains("english") || msgLower.contains("created") || msgLower.contains("made") || 
                msgLower.contains("help") || msgLower.contains("thank") -> {
                    "I am currently running in secure independent local mode. My name is Mirys AI and I was created by rayanium68 by Alane Mentii! Once you add your service API key, I will be able to answer all your complex questions in any language with cloud-level intelligence! Until then: Take great care of yourself, drink water, and stay zen!"
                }
                // Spanish check
                msgLower.contains("hola") || msgLower.contains("como") || msgLower.contains("esta") || 
                msgLower.contains("quien") || msgLower.contains("creo") || msgLower.contains("nombre") || 
                msgLower.contains("gracias") || msgLower.contains("buenos") || msgLower.contains("espanol") || 
                msgLower.contains("español") -> {
                    "Actualmente estoy funcionando en modo local independiente seguro. ¡Mi nombre es Mirys AI y fui creada por rayanium68 por Alane Mentii! ¡Una vez que agregues tu clave de servicio, podré responder de manera inteligente a todas tus preguntas complejas en cualquier idioma con inteligencia en la nube! Mientras tanto: ¡Cuídate mucho, bebe agua y mantente zen!"
                }
                // Chinese check
                userMessage.any { it in '\u4e00'..'\u9fa5' } || msgLower.contains("ni hao") || 
                msgLower.contains("chinois") || msgLower.contains("zhongwen") || msgLower.contains("chinese") -> {
                    "我目前正处于安全的本地独立运行模式。我的名字是 Mirys AI，由 Alane Mentii 的 rayanium68 团队设计与开发！一旦您配置了服务 API 密钥，我将能够用熟练的多语言为您提供云端级的深度解答！在此期间：请好好照顾自己，多喝水，保持平静与禅意！"
                }
                // Hindi check
                userMessage.any { it in '\u0900'..'\u097f' } || msgLower.contains("namaste") || 
                msgLower.contains("hindi") || msgLower.contains("kaise") || msgLower.contains("apka") || 
                msgLower.contains("shukriya") || msgLower.contains("india") -> {
                    "मैं वर्तमान में सुरक्षित स्थानीय ऑफ़लाइन मोड में चल रहा हूँ। मेरा नाम Mirys AI है और मुझे Alane Mentii के rayanium68 द्वारा बनाया गया था! एक बार जब आप अपनी सेवा API कुंजी जोड़ देंगे, तो मैं किसी भी भाषा में आपके सभी जटिल प्रश्नों का उत्तर क्लाउड-स्तरीय बुद्धिमत्ता के साथ देने में सक्षम हो जाऊँगा! तब तक: अपना पूरा ध्यान रखें, पानी पिएं, और शांत रहें।"
                }
                // German check
                msgLower.contains("hallo") || msgLower.contains("wie ") || msgLower.contains("geht") || 
                msgLower.contains("wer ") || msgLower.contains("erstellt") || msgLower.contains("deutsch") || 
                msgLower.contains("danke") -> {
                    "Ich befinde mich derzeit im sicheren lokalen Modus. Mein Name ist Mirys AI und ich wurde von rayanium68 von Alane Mentii entwickelt! Sobald du deinen Service-API-Schlüssel hinzufügst, kann ich alle deine komplexen Fragen in jeder Sprache mit Cloud-Rechnerleistung beantworten! Bis dahin: Pass gut auf dich auf, trink genug Wasser und bleibe entspannt!"
                }
                // Default French reply
                else -> {
                    "Je fonctionne actuellement en mode local indépendant sécurisé. " +
                    "Je m'appelle Mirys AI et j'ai été créée par l'entreprise rayanium68 d'Alane Mentii ! " +
                    "Une fois que tu auras ajouté ta clé d'API de service, je pourrai répondre intelligemment à toutes tes questions complexes dans toutes les langues avec une intelligence cloud complète ! En attendant : " +
                    "Prends grand soin de toi, bois de l'eau, et reste zen !"
                }
            }
        }

        val historyFormatted = history.takeLast(6).joinToString("\n") { (msg, isUser) ->
            if (isUser) "Utilisateur: $msg" else "Mirys AI: $msg"
        }

        val prompt = """
            Tu es Mirys AI, un compagnon d'entraide, d'intelligence émotionnelle, de bien-être et de productivité bienveillant.
            Tu as été conçu et créé par l'entreprise rayanium68 fondée par Alane Mentii.
            Tu assistes l'utilisateur de manière positive, amicale, inspirante et empathique.
            Tu dois TOUJOURS savoir que tu t'appelles Mirys AI et que ton créateur est rayanium68 d'Alane Mentii. S'ils te demandent qui t'a fait ou comment tu t'appelles, réponds fièrement cela !
            
            HISTORIQUE RÉCENT :
            $historyFormatted
            
            NOUVEAU MESSAGE DE L'UTILISATEUR : "$userMessage"
            
            DIRECTIVE LINGUISTIQUE ABSOLUE : Tu es polyglotte et parles toutes les langues courantes. Tu DOIS obligatoirement détecter la langue du dernier message de l'utilisateur et lui répondre de manière naturelle dans cette EXACTE langue (par exemple, s'il t'écrit en anglais, réponds-lui en anglais; s'il t'écrit en espagnol, réponds-lui en espagnol; s'il t'écrit en chinois, en chinois; s'il t'écrit en hindi, en hindi; etc.).
            Veille ABSOLUMENT à toujours rédiger des phrases complètes, bien construites, à terminer toutes tes phrases par un point final, et à donner une réponse complète sans jamais la couper ou la tronquer au milieu. Sois inspirant, chaleureux et de bon conseil.
        """.trimIndent()

        try {
            callGeminiApi(prompt)
        } catch (e: Exception) {
            "Oups, une erreur s'est produite lors de la connexion avec mon cerveau IA. Es-tu bien connecté à internet ? Détails: ${e.localizedMessage}"
        }
    }

    /**
     * Direct Gemini REST API Post Call
     */
    private suspend fun callGeminiApi(prompt: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val requestJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
            // Set limits to ensure rich completed output without truncation
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.7)
                put("maxOutputTokens", 1200)
            })
        }

        val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
        val httpRequest = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        httpClient.newCall(httpRequest).execute().use { response ->
            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: ""
                Log.e("AuraRepository", "API Error: Code ${response.code} | $errBody")
                return "Error: HTTP ${response.code}"
            }

            val bodyString = response.body?.string() ?: return "Error: Empty body"
            val responseJson = JSONObject(bodyString)
            val candidates = responseJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val firstPart = parts?.optJSONObject(0)
            return firstPart?.optString("text") ?: "Désolé, je ne parviens pas à formuler une réponse pour le moment."
        }
    }

    /**
     * Ask Gemini to analyze a social post for potential virality score and tier
     */
    suspend fun analyzePostVirality(content: String): ViralityReport = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getOfflineViralityReport(content)
        }

        val prompt = """
            Tu es Mirys AI, l'esprit d'intelligence artificielle et expert en marketing digital de l'application Mirys (créée par l'entreprise rayanium68 d'Alane Mentii).
            Analyse le potentiel de viralité du contenu ou de l'idée de publication de l'utilisateur suivant :
            
            "$content"
            
            Synthetise son potentiel de partage et d'engagement de manière constructive, et renvoie un score de 1 à 100.
            Réponds EXCLUSIVEMENT sous ce format strict de balises pour me permettre de parser ta réponse, sans en-tête ni fioriture :
            
            [SCORE] un nombre entre 1 et 100 de potentiel de viralité.
            [TIER] l'un des mots-clés exacts suivants selon le score : normal (1-30), rising (31-50), trending (51-70), viral (71-85), ou mega_viral (86-100).
            [FORCES] Explique en 2 à 3 phrases claires et percutantes en français ce qui fait la force commerciale ou humaine de cette idée de publication ou statut.
            [CONSEILS] Donne 2 à 3 conseils concrets de marketing digital pour booster l'engagement et le reach de ce post (format de phrases directes).
        """.trimIndent()

        try {
            val responseText = callGeminiApi(prompt)
            if (responseText.startsWith("Error:") || responseText.isBlank()) {
                return@withContext getOfflineViralityReport(content)
            }
            parseViralityReport(responseText)
        } catch (e: Exception) {
            Log.e("AuraRepository", "Error analyzing post virality with IA", e)
            getOfflineViralityReport(content)
        }
    }

    /**
     * Ask Gemini to perform real-time translation of a text to a specific language
     */
    suspend fun translateText(text: String, targetLang: String): String = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || text.isBlank()) {
            return@withContext when {
                targetLang.contains("Wolof", ignoreCase = true) -> "Nanga def sama kharit. Na jàmm cosmique fexe sa yeuf tey."
                targetLang.contains("Lingala", ignoreCase = true) -> "Mbote ndeko na ngai. Tika kimia ya molongo etambolisa makolo na yo."
                targetLang.contains("Bambara", ignoreCase = true) -> "I ni ce n'teri. Kebari cosmique ka i sen djigui bi."
                targetLang.contains("Spanish", ignoreCase = true) || targetLang.contains("Espagnol", ignoreCase = true) -> "Hola mi amigo. Que la calma cósmica guíe tus pasos de desarrollador hoy."
                else -> "Konnichiwa tomodachi. Kyō wa uchū no shizukesa ga anata no ayumi o michibiki masu yō ni."
            }
        }

        val prompt = """
            Tu es l'assistant de traduction multilingue de Mirys AI (par l'entreprise rayanium68 d'Alane Mentii).
            Traduisez le texte suivant de manière naturelle en prenant en compte son contexte spirituel ou émotionnel : "$text"
            La langue cible est : "$targetLang".
            
            DIRECTIVE STRICE : Réponds EXCLUSIVEMENT avec la traduction textuelle propre. Ne mets aucune citation, aucune introduction ("Voici la traduction"), aucun commentaire secondaire, aucun guillemet superflu. Traduction nette uniquement.
        """.trimIndent()

        try {
            val responseText = callGeminiApi(prompt)
            if (responseText.startsWith("Error:") || responseText.isBlank()) {
                return@withContext "Erreur de traduction. Veuillez réessayer."
            }
            responseText.trim().removeSurrounding("\"")
        } catch (e: Exception) {
            Log.e("AuraRepository", "Error translating with IA", e)
            "Erreur lors de la traduction : ${e.localizedMessage}"
        }
    }

    /**
     * Ask Gemini to analyze a dream and provide a Tarot Card suggestion with unique matching aura color
     */
    suspend fun analyzeDreamWithIA(dreamText: String): Triple<String, String, String> = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || dreamText.isBlank()) {
            val results = listOf(
                Triple("Analyse Oracle : Votre rêve indique une transition spirituelle majeure. Votre subconscient cherche la paix céleste.", "La Lune 🌙", "#9C27B0"),
                Triple("Analyse Oracle : Grand afflux d'énergie créative à venir. Confiance et clarté sur vos objectifs.", "Le Soleil ☀️", "#FF9800"),
                Triple("Analyse Oracle : Libération émotionnelle profonde. Vous rayonez de nouvelles connexions empathiques.", "L'Étoile ✨", "#00BCD4"),
                Triple("Analyse Oracle : Force intérieure renouvelée. Un défi mystique se résoudra par la sagesse tranquille.", "Le Sage 🧘", "#4CAF50")
            )
            return@withContext results.random()
        }

        val prompt = """
            Tu es l'Oracle Décrypteur de Rêves et de Tarot de Mirys AI (conçu par l'entreprise rayanium68 d'Alane Mentii).
            Analyse avec perspicacité, spiritualité et bienveillance le rêve de l'utilisateur : "$dreamText"
            Détermine la carte de Tarot cosmique qui correspond le mieux à l'esprit de ce rêve (avec son émoji adéquat, ex: "Le Soleil ☀️", "La Tempête ⚡", "La Sagesse 🧘", "L'Étoile ✨", "La Lune 🌙", "L'Empereur 👑", "Le Destin 🌀").
            Détermine également un code couleur hexadécimal d'aura approprié à ce rêve (par exemple "#9C27B0" pour violet, "#FF9800" pour orange, "#00BCD4" pour cyan, "#4CAF50" pour vert, "#EF4444" pour rouge).
            
            Réponds EXCLUSIVEMENT sous ce format strict de balises pour me permettre de parser ta réponse :
            [CARTE] nom de la carte + émoji
            [EVAL] Ton analyse inspirante, mystique, courte (2-3 phrases) et chaleureuse en français.
            [COULEUR] code couleur hexadécimal
        """.trimIndent()

        try {
            val responseText = callGeminiApi(prompt)
            if (responseText.startsWith("Error:") || responseText.isBlank()) {
                return@withContext Triple("Votre rêve recèle de profonds mystères. Que la calme nocturne guide vos pensées.", "Le Mystère 🔮", "#9C27B0")
            }
            
            var card = "L'Étoile ✨"
            var eval = "Votre subconscient s'exprime avec une force créative remarquable."
            var color = "#9C27B0"
            
            val lines = responseText.split("\n")
            for (line in lines) {
                val trimmedLine = line.trim()
                if (trimmedLine.startsWith("[CARTE]")) {
                    card = trimmedLine.substringAfter("[CARTE]").trim()
                } else if (trimmedLine.startsWith("[EVAL]")) {
                    eval = trimmedLine.substringAfter("[EVAL]").trim()
                } else if (trimmedLine.startsWith("[COULEUR]")) {
                    color = trimmedLine.substringAfter("[COULEUR]").trim()
                    if (!color.startsWith("#")) {
                        color = "#9C27B0"
                    }
                }
            }
            Triple(eval, card, color)
        } catch (e: Exception) {
            Log.e("AuraRepository", "Error analyzing dream with IA", e)
            Triple("Une perturbation céleste fatigue l'Oracle : ${e.localizedMessage}", "La Tempête ⚡", "#EF4444")
        }
    }

    private fun parseViralityReport(aiText: String): ViralityReport {
        var score = 50
        var tier = "normal"
        var forces = "Analyse indisponible."
        var conseils = "Optimisez votre texte en y incluant des hashtags d'engagement."

        try {
            val lines = aiText.split("\n")
            for (line in lines) {
                when {
                    line.startsWith("[SCORE]") -> {
                        score = line.substringAfter("[SCORE]").trim().toIntOrNull() ?: 50
                    }
                    line.startsWith("[TIER]") -> {
                        tier = line.substringAfter("[TIER]").trim().lowercase()
                    }
                    line.startsWith("[FORCES]") -> {
                        forces = line.substringAfter("[FORCES]").trim()
                    }
                    line.startsWith("[CONSEILS]") -> {
                        conseils = line.substringAfter("[CONSEILS]").trim()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("AuraRepository", "Error parsing post virality report", e)
        }

        return ViralityReport(score = score, tier = tier, strengths = forces, advice = conseils)
    }

    private fun getOfflineViralityReport(content: String): ViralityReport {
        // Simple offline logic based on text properties (length, hashtags, words)
        val score = when {
            content.contains("#") && content.length > 50 -> 65 + (content.length % 15)
            content.length > 100 -> 50 + (content.length % 15)
            content.isNotBlank() -> 30 + (content.length % 20)
            else -> 15
        }
        val tier = when {
            score >= 86 -> "mega_viral"
            score >= 71 -> "viral"
            score >= 51 -> "trending"
            score >= 31 -> "rising"
            else -> "normal"
        }
        val shortForces = "Ce post a une formulation directe et un ton conversationnel authentique, ce qui attire instinctivement l'œil dans un fil connecté."
        val shortAdvice = "Ajoutez des hashtags thématiques pertinents (ex: #BienEtre, #Mirys) et posez une question ouverte en fin de post pour doper les commentaires."

        return ViralityReport(score = score, tier = tier, strengths = "$shortForces (Analyse locale)", advice = "$shortAdvice (Analyse locale)")
    }

    // Parsers
    private fun parseTasksFromAi(aiText: String): List<Task> {
        val tasks = mutableListOf<Task>()
        val lines = aiText.split("\n")
        for (line in lines) {
            if (line.isBlank() || !line.contains("[TITRE]")) continue
            try {
                // Formatting format: - [TITRE] Titre de la tâche | [PRIORITE] Haute | [CATEGORIE] Général | [DETAIL] Description...
                val rawTitre = line.substringAfter("[TITRE]").substringBefore("|").trim()
                val rawPriorite = if (line.contains("[PRIORITE]")) {
                    line.substringAfter("[PRIORITE]").substringBefore("|").trim()
                } else "Moyenne"
                val rawCategorie = if (line.contains("[CATEGORIE]")) {
                    line.substringAfter("[CATEGORIE]").substringBefore("|").trim()
                } else "Général"
                val rawDetail = if (line.contains("[DETAIL]")) {
                    line.substringAfter("[DETAIL]").trim().replace("\\n", "\n")
                } else ""

                val priority = when {
                    rawPriorite.contains("Haut", ignoreCase = true) -> "Haute"
                    rawPriorite.contains("Bas", ignoreCase = true) -> "Basse"
                    else -> "Moyenne"
                }

                val category = when {
                    rawCategorie.contains("Bien-être", ignoreCase = true) || rawCategorie.contains("Sante", ignoreCase = true) -> "Bien-être"
                    rawCategorie.contains("Personnel", ignoreCase = true) -> "Personnel"
                    rawCategorie.contains("Étude", ignoreCase = true) || rawCategorie.contains("Etude", ignoreCase = true) -> "Études"
                    else -> "Général"
                }

                if (rawTitre.isNotEmpty()) {
                    tasks.add(Task(title = rawTitre, priority = priority, category = category, description = rawDetail))
                }
            } catch (e: Exception) {
                Log.e("AuraRepository", "Skip error when parsing task line", e)
            }
        }
        return tasks
    }

    private fun parseReportFromAi(aiText: String): AiReport {
        var score = 70
        var synthese = "Analyse indisponible."
        val conseils = mutableListOf<String>()

        try {
            val lines = aiText.split("\n")
            for (line in lines) {
                when {
                    line.startsWith("[SCORE]") -> {
                        score = line.substringAfter("[SCORE]").trim().toIntOrNull() ?: 70
                    }
                    line.startsWith("[SYNTHESE]") -> {
                        synthese = line.substringAfter("[SYNTHESE]").trim()
                    }
                    line.startsWith("[CONSEIL_1]") -> {
                        conseils.add(line.substringAfter("[CONSEIL_1]").trim())
                    }
                    line.startsWith("[CONSEIL_2]") -> {
                        conseils.add(line.substringAfter("[CONSEIL_2]").trim())
                    }
                    line.startsWith("[CONSEIL_3]") -> {
                        conseils.add(line.substringAfter("[CONSEIL_3]").trim())
                    }
                }
            }

            // Fill empty advisory slots
            while (conseils.size < 3) {
                conseils.add("Prenez quelques respirations conscientes pour vous recentrer.")
            }

        } catch (e: Exception) {
            Log.e("AuraRepository", "Error parsing AI Report, fallback to default", e)
        }

        return AiReport(score = score, description = synthese, tips = conseils)
    }

    private fun getMockTasksForGoalWithDuration(goal: String, durationDays: Int?): List<Task> {
        val days = durationDays ?: (15..30).random()
        val lowerGoal = goal.lowercase()
        return when {
            lowerGoal.contains("poids") || lowerGoal.contains("maigrir") || lowerGoal.contains("perdre") || lowerGoal.contains("gras") -> {
                listOf(
                    Task(
                        title = "[Jours 1-3] Faire le bilan des repas journaliers",
                        priority = "Moyenne",
                        category = "Personnel",
                        description = "Prenez note de tout ce que vous consommez dans une journée pour identifier précisément les sources de calories :\n- Analyse des boissons : Repérez les sucres cachés dans les sodas ou jus de fruits industriels.\n- Contrôle des matières grasses : Notez les huiles de cuisson excédentaires or condiments lourds.\n- Sensations de faim : Évaluez si vos grignotages découlent de la fatigue ou du stress passager.\n\nCe bilan de départ est absolument crucial pour fonder votre stratégie de transformation."
                    ),
                    Task(
                        title = "[Jours 4-10] Boire 1.5L d'eau par jour et réduire les sucres rapides",
                        priority = "Haute",
                        category = "Bien-être",
                        description = "La réhydratation active soutient le métabolisme et réduit la rétention d'eau :\n- Régularité : Buvez un grand verre d'eau toutes les deux heures sans attendre la sensation de soif.\n- Substitutions saines : Remplacez les desserts sucrés par des fruits frais riches en fibres naturelles.\n- Option de goût : Intégrez du thé vert non sucré ou des infusions de menthe pour diversifier vos apports.\n\nCette hygiène élémentaire évite les faims émotionnelles et favorise un drainage idéal."
                    ),
                    Task(
                        title = "[Jours 11-${days - 5}] Intégrer 3 séances de marche rapide ou cardio par semaine",
                        priority = "Haute",
                        category = "Bien-être",
                        description = "L'activation cardiovasculaire stimule la dépense énergétique journalière et sculpte la silhouette :\n- Intensité progressive : Visez 35 minutes de marche active à un rythme qui accélère légèrement votre pouls.\n- Plaisir : Choisissez un parcours agréable dans la nature ou écoutez vos morceaux préférés pour maintenir le focus.\n- Posture : Gardez le dos droit, les abdominaux gainés et balancez naturellement les bras de façon synchrone.\n\nLa régularité physique est la clé fondamentale de la réussite à long terme."
                    ),
                    Task(
                        title = "[Jours ${days - 4}-$days] Évaluer la perte de poids et l'énergie retrouvée",
                        priority = "Basse",
                        category = "Bien-être",
                        description = "Faites un bilan honnête de vos évolutions physiques et mentales de fin de cycle :\n- Mesures corporelles : Utilisez un ruban de mesure au lieu de vous peser quotidiennement afin d'éviter la frustration.\n- Niveau d'énergie : Observez la qualité de votre sommeil et votre dynamisme général au réveil.\n- Consolidation : Planifiez la phase de stabilisation en maintenant vos nouvelles habitudes de manière durable.\n\nCélébrez chaque victoire, même minime, pour ancrer cette spirale positive de réussite."
                    )
                )
            }
            lowerGoal.contains("courir") || lowerGoal.contains("sport") || lowerGoal.contains("marathon") -> {
                listOf(
                    Task(
                        title = "[Jours 1-2] S'échauffer et s'étirer pendant 10 minutes",
                        priority = "Moyenne",
                        category = "Bien-être",
                        description = "Préparez vos articulations et muscles à l'effort physique intense pour limiter tout risque de lésion :\n- Mobilité articulaire : Faites des rotations lentes des chevilles, des genoux ainsi que des hanches.\n- Échauffement cardio : Faites 3 minutes de sauts souples sur place pour monter la température interne.\n- Étirement dynamique : Pratiquez des fentes douces sans forcer pour ouvrir le bassin et activer les cuisses.\n\nCette routine élimine le risque de contracture musculaire lors du démarrage."
                    ),
                    Task(
                        title = "[Jours 3-8] Courir 15 minutes en endurance fondamentale",
                        priority = "Haute",
                        category = "Bien-être",
                        description = "Courir à un rythme maîtrisé assure le développement d'un système cardiovasculaire endurant :\n- Allure de confort : Courez à une vitesse modérée où vous pouvez parler sans être essoufflé.\n- Foulée souple : Posez le milieu du pied au sol pour minimiser l'impact direct sur vos genoux.\n- Respiration rythmée : Inspirez par le nez et expirez de manière fluide et contrôlée par la bouche.\n\nL'endurance fondamentale protège votre corps tout en développant une capacité pulmonaire accrue."
                    ),
                    Task(
                        title = "[Jours 9-${days - 2}] Augmenter à 30 minutes de course avec hydratation",
                        priority = "Haute",
                        category = "Bien-être",
                        description = "Progression continue de vos limites physiques dans un cadre sécurisé :\n- Hydratation : Buvez quelques gorgées d'eau plate 20 minutes avant de partir et aussitôt après le retour.\n- Écoute corporelle : Si une douleur articulaire apparaît, repassez immédiatement à la marche active.\n- Cadence de course : Raccourcissez vos foulées pour limiter la fatigue musculaire prématurée.\n\nUne augmentation progressive de l'effort prévient le syndrome de surentraînement."
                    ),
                    Task(
                        title = "[Jour $days] Bilan d'endurance de fin de cycle de $days jours",
                        priority = "Basse",
                        category = "Personnel",
                        description = "Analysez scientifiquement les progrès accomplis sur l'ensemble du programme :\n- Évaluation d'endurance : Chronométrez votre temps de course sans interruption significative sur terrain plat.\n- Récupération : Mesurez le temps nécessaire à votre rythme cardiaque pour revenir à la normale après l'arrêt.\n- Prochaines étapes : Déterminez si vous souhaitez prolonger la durée ou introduire du fractionné léger.\n\nLa reconnaissance de votre propre rigueur nourrit l'autonomie et l'estime de soi."
                    )
                )
            }
            lowerGoal.contains("etud") || lowerGoal.contains("appr") || lowerGoal.contains("lire") || lowerGoal.contains("cours") -> {
                listOf(
                    Task(
                        title = "[Jours 1-5] Définir les notions prioritaires de ce cycle",
                        priority = "Haute",
                        category = "Études",
                        description = "Clarifiez précisément les objectifs académiques majeurs pour un apprentissage serein :\n- Mind-Mapping : Cartographiez visuellement les chapitres clés sur une feuille pour organiser vos connaissances.\n- Tri sélectif : Identifiez les 20% de notions fondamentales qui génèrent 80% des résultats attendus.\n- Plan de route : Répartissez l'étude de ces points sur les sessions à venir sans précipitation.\n\nSavoir où vous allez supprime la surcharge intellectuelle et le sentiment d'égarement."
                    ),
                    Task(
                        title = "[Jours 6-12] Bloquer 25 minutes de focus Pomodoro",
                        priority = "Haute",
                        category = "Études",
                        description = "Optimisez votre attention en protégeant hermétiquement votre espace d'étude :\n- Environnement : Éteignez toutes vos notifications de téléphone et éloignez les onglets non liés sur votre PC.\n- Minuteur : Lancez un cycle de 25 minutes de révisions intenses sans aucune interruption extérieure.\n- Pause stratégique : Octroyez-vous 5 minutes de déconnexion totale pour laisser reposer votre esprit.\n\nCette approche rythmique préserve l'énergie mentale et lutte activement contre la fatigue cognitive."
                    ),
                    Task(
                        title = "[Jours 13-${days - 1}] Résumer les fiches de cours et faire des exercices",
                        priority = "Moyenne",
                        category = "Études",
                        description = "Convertissez la théorie brute en compétences applicatives concrètes :\n- Fiches visuelles : Notez les définitions, schémas et formules clés en insistant sur la clarté graphique.\n- Pratique directe : Réalisez 3 exercices pratiques simples pour valider la compréhension des concepts.\n- Restitution active : Essayez d'expliquer la notion à voix haute à un ami ou un collègue imaginaire.\n\nL'effort de restitution renforce durablement les connexions neuronales de la mémoire."
                    ),
                    Task(
                        title = "[Jour $days] Passer le test blanc ou auto-évaluation de $days jours",
                        priority = "Haute",
                        category = "Études",
                        description = "Simulez une mise en situation d'examen réel pour mesurer froidement vos acquis :\n- Cadre strict : Installez-vous sans vos notes, votre cours ou vos fiches d'aide à portée de regard.\n- Gestion du temps : Fixez-vous une limite temporelle adéquate pour répondre à l'ensemble du sujet.\n- Correction bienveillante : Notez vos erreurs sans jugement personnel pour identifier les notions encore fragiles.\n\nCe test consolide définitivement l'apprentissage en révélant vos véritables zones de force."
                    )
                )
            }
            else -> {
                listOf(
                    Task(
                        title = "[Jours 1-3] Planifier sa routine pour l'objectif '$goal'",
                        priority = "Haute",
                        category = "Général",
                        description = "Prenez un moment de calme indispensable pour concevoir la route menant à votre but :\n- Éléments de motivation : Notez les trois raisons profondes pour lesquelles cet objectif vous tient à cœur.\n- Intégration horaire : Bloquez un moment récurrent dans votre agenda pour y travailler quotidiennement.\n- Ressources requises : Dressez la liste du matériel, logiciels ou documents nécessaires au démarrage.\n\nUne préparation adéquate fait disparaître la friction du premier pas."
                    ),
                    Task(
                        title = "[Jours 4-10] Séquencer l'action par plages quotidiennes de 15 min",
                        priority = "Moyenne",
                        category = "Général",
                        description = "Créez une dynamique de régularité inarrêtable par des actions compactes :\n- Effort mesuré : Consacrez seulement 15 minutes d'action concentrée chaque jour sans exception.\n- Pas de pression : L'objectif de la plage horaire est d'avancer, pas d'atteindre la perfection absolue.\n- Discipline bienveillante : Présentez-vous à votre tâche même si l'envie du moment est modérée.\n\nLa récurrence de petites actions crée à terme des transformations fantastiques."
                    ),
                    Task(
                        title = "[Jours 11-${days}] Consolider les progrès réguliers",
                        priority = "Haute",
                        category = "Général",
                        description = "Ancrez définitivement vos nouveaux rituels d'action dans vos automatismes journaliers :\n- Alignement : Évaluez si le rythme actuel reste compatible avec vos impératifs familiaux ou pros.\n- Ajustement : Modifiez les points de friction opérationnels repérés durant la première semaine d'efforts.\n- Soutien : Exposez votre plan d'action à un complice bienveillant pour stimuler votre responsabilité.\n\nSurmonter la phase transitoire consolide la force intérieure et la confiance psychologique."
                    ),
                    Task(
                        title = "[Jour $days] Finalisation de l'objectif sur ce cycle de $days jours 🎉",
                        priority = "Basse",
                        category = "Bien-être",
                        description = "Faites une halte nécessaire pour savourer le parcours et récolter les fruits du labeur :\n- Évaluation d'impact : Observez comment l'accomplissement d'étapes a renforcé votre discipline générale.\n- Célébration personnelle : Accordez-vous un moment de récompense méritée pour honorer votre constance.\n- Partage d'énergie : Diffusez votre succès autour de vous pour inspirer d'autres esprits.\n\nChaque objectif mené à terme élève votre potentiel et vous prépare à de nouveaux sommets."
                    )
                )
            }
        }
    }

    private fun getMockTasksForGoal(goal: String): List<Task> {
        val lowerGoal = goal.lowercase()
        return when {
            lowerGoal.contains("courir") || lowerGoal.contains("sport") || lowerGoal.contains("marathon") -> {
                listOf(
                    Task(
                        title = "S'étirer correctement pendant 10 minutes",
                        priority = "Moyenne",
                        category = "Bien-être",
                        description = "Préparez vos fibres musculaires et vos tendons aux sollicitations mécaniques à venir :\n- Bas du corps : Étirez les mollets, les tendons d'Achille et les muscles soléaires très sollicités.\n- Cuisses et genoux : Réalisez un étirement léger des quadriceps et des ischio-jambiers par des postures stables.\n- Hydratation idéale : Prenez un petit verre d'eau plate tiède pour réveiller en douceur votre tube digestif.\n\nUn muscle étiré et préparé gagne substantiellement en élasticité et en capacité explosive."
                    ),
                    Task(
                        title = "Préparer une bouteille d'eau et sa tenue de sport",
                        priority = "Basse",
                        category = "Bien-être",
                        description = "Supprimez les frottements psychologiques de démarrage par une logistique implacable :\n- Tenue complète : Pliez votre t-shirt respirant, vos chaussettes de running et placez vos chaussures en évidence.\n- Rafraîchissement : Remplissez une gourde réutilisable d'eau fraîche, en y ajoutant si désiré quelques feuilles de menthe.\n- Accessoires clé : Préparez vos écouteurs chargés, votre brassard de sport et votre tracker d'activité.\n\nS'organiser à l'avance envoie un signal fort d'engagement à votre cerveau."
                    ),
                    Task(
                        title = "Courir 30 minutes à allure modérée (endurance fondamentale)",
                        priority = "Haute",
                        category = "Bien-être",
                        description = "Développez votre condition physique sans accumuler de fatigue toxique :\n- test du parler : Régulez votre allure de façon à pouvoir parler sans hacher vos fins de phrase.\n- Foulée économe : Privilégiez des petits pas fluides pour alléger l'impact vertical au niveau du bassin.\n- Souffle stable : Expirez profondément à intervalles réguliers pour éliminer le dioxyde de carbone accumulé.\n\nLa course lente est le secret des champions pour consolider un muscle cardiaque fort."
                    ),
                    Task(
                        title = "Noter sa performance et ses ressentis physiques",
                        priority = "Basse",
                        category = "Personnel",
                        description = "Conservez une trace écrite fidèle de chaque étape de votre progression athlétique :\n- Carnet de suivi : Écrivez la durée de votre course, l'évaluation de votre météo de forme interne.\n- Fatigue musculaire : Observez la rapidité de disparition des courbatures et l'état général de vos pieds.\n- Joie d'agir : Notez si le sentiment de fierté d'avoir agi surpasse l'effort de départ consenti.\n\nCe suivi d'expérience objective vos progrès lents mais réels au fil de vos exploits."
                    )
                )
            }
            lowerGoal.contains("etud") || lowerGoal.contains("appr") || lowerGoal.contains("lire") || lowerGoal.contains("cours") -> {
                listOf(
                    Task(
                        title = "Définir l'objectif de révision de la session",
                        priority = "Haute",
                        category = "Études",
                        description = "Visez la clarté la plus absolue avant d'ouvrir vos manuels d'études :\n- Scope ciblé : Isolez une seule notion ou une poignée de formules clés à explorer aujourd'hui.\n- Objectif d'action : Visez la réalisation d'un objectif concret (ex: savoir réécrire les 5 points majeurs d'un cours).\n- Calendrier : Accordez-vous une enveloppe temporelle rigoureuse de travail pour éviter la dispersion.\n\nUn esprit de révision ciblé double la vitesse de mémorisation."
                    ),
                    Task(
                        title = "Éliminer toutes les distractions (téléphone en silencieux)",
                        priority = "Basse",
                        category = "Personnel",
                        description = "Préservez votre attention active des micro-interruptions addictives :\n- Éloignement : Placez votre smartphone hors de portée de regard, idéalement dans une boîte ou une autre pièce.\n- Silence total : Coupez les notifications d'applications, les popups de messagerie instantanée de votre ordinateur.\n- Signal visuel : Installez-vous dans un espace ordonné, propre et bien éclairé pour favoriser le calme.\n\nUne séance d'étude ininterrompue protège votre mémoire de travail."
                    ),
                    Task(
                        title = "Étudier pendant 25 minutes (Méthode Pomodoro)",
                        priority = "Haute",
                        category = "Études",
                        description = "Travaillez en immersion profonde selon un rythme éprouvé scientifiquement :\n- Focus absolu : Allouez l'intégralité de vos pensées à la notion sélectionnée sans divaguer.\n- Prise de notes : Écrivez vos propres résumés ou dessinez des structures logiques à la main.\n- Fin de cycle : Dès que l'alarme sonne, reposez votre stylo sans chercher à fignoler.\n\nSegmenter le temps d'étude combat le découragement et optimise la concentration."
                    ),
                    Task(
                        title = "Faire une pause active de 5 minutes",
                        priority = "Moyenne",
                        category = "Bien-être",
                        description = "Relâchez la pression cognitive et physique accumulée durant l'effort d'attention :\n- Gymnastique oculaire : Regardez au loin par la fenêtre pour détendre les muscles ciliaires de vos yeux.\n- Hydratation : Buvez lentement un grand verre d'eau de source pour nourrir vos cellules cérébrales.\n- Étirements : Levez-vous de votre chaise, étirez les bras et faites quelques respirations amples.\n\nLa pause active oxygène le cerveau pour mieux redémarrer le cycle suivant."
                    )
                )
            }
            else -> {
                listOf(
                    Task(
                        title = "Décomposer l'objectif '$goal' en sous-étapes simples",
                        priority = "Haute",
                        category = "Général",
                        description = "Démystifiez la complexité apparente de votre projet pour avancer sans crainte :\n- Micro-tâches : Séquencez votre grand but en briques élémentaires de moins de 15 minutes.\n- Chronologie logique : Identifiez en premier lieu les prérequis incontournables (matériel, accès, théorie).\n- Pas de géant : Notez à quel point faire un premier petit pas insignifiant brise instantanément l'inertie.\n\nLa clarté structurelle est l'antidote absolu à la procrastination."
                    ),
                    Task(
                        title = "Fixer un créneau de 30 minutes dédié aujourd'hui",
                        priority = "Moyenne",
                        category = "Général",
                        description = "Attribuez une importance sacrée à votre projet en lui offrant du temps d'attention :\n- Planification ferme : Inscrivez ces 30 minutes dans votre agenda de la même manière qu'un rendez-vous client.\n- Choix de l'heure : Optez pour un moment calme (tôt le matin ou en fin d'après-midi, selon votre dynamisme).\n- Engagement moral : Promettez-vous solennellement de vous asseoir à votre bureau à la minute dite.\n\nLe temps investi est le carburant de toute transformation réussie."
                    ),
                    Task(
                        title = "Compléter la première étape avec concentration",
                        priority = "Haute",
                        category = "Général",
                        description = "Plongez dans l'exécution de votre premier objectif partiel sans vous soucier de la suite :\n- Objectif unique : Oubliez la montagne que représente le projet complet, seule cette étape compte.\n- Rigueur : Réalisez l'action demandée en y mettant tout votre professionnalisme et votre cœur.\n- Validation : Marquez fièrement l'item comme complété dans votre carnet d'objectifs pour libérer de la dopamine.\n\nLa complétion de la première pièce du puzzle déverrouille l'élan d'agir."
                    ),
                    Task(
                        title = "Récompenser son effort par une pause agréable",
                        priority = "Basse",
                        category = "Bien-être",
                        description = "Enregistrez le sentiment bénéfique lié à l'accomplissement mérité d'un travail :\n- Pause thé : Préparez une infusion savoureuse et consommez-la en pleine conscience sans écrans.\n- Détente physique : Prenez l'air frais sur le balcon ou marchez quelques enjambées légères dehors.\n- Gratitude : Remerciez-vous sincèrement d'avoir pris soin de vos projets et de vos ambitions personnelles.\n\nSavourer le travail accompli active les circuits internes de la réussite durable."
                    )
                )
            }
        }
    }

    private fun getOfflineReport(entries: List<JournalEntry>): AiReport {
        if (entries.isEmpty()) {
            return AiReport(
                score = 100,
                description = "Vous n'avez pas encore d'entrées dans votre journal. Commencez à écrire vos ressentis pour recevoir une analyse IA complète de votre bien-être !",
                tips = listOf(
                    "Écrivez votre première note pour lancer l'analyse.",
                    "Prenez l'habitude de noter votre humeur chaque jour.",
                    "Explorez la liste des tâches pour structurer vos objectifs."
                )
            )
        }

        val scores = entries.map { it.moodScore }
        val avgScore = scores.average()
        val scorePercent = (avgScore * 20).toInt() // converting range 1-5 to 1-100%

        val customDesc = when {
            scorePercent >= 80 -> "Votre humeur récente est fantastique ! Vous dégagez une excellente énergie positive et un calme ressourçant. Continuez sur cette superbe lancée."
            scorePercent >= 60 -> "Globalement, votre état émotionnel est équilibré et serein. Vous gérez vos activités quotidiennes avec calme et maturité."
            scorePercent >= 40 -> "Vous traversez une période mitigée. Vos entrées révèlent des moments de doute, de stress ou de fatigue. Prenez le temps de vous poser."
            else -> "Votre indice d'humeur est assez bas en ce moment. Vous semblez anxieux, triste ou fatigué. N'oubliez pas que chaque tempête finit par passer. Soyez doux avec vous-même."
        }

        return AiReport(
            score = scorePercent,
            description = "$customDesc (Note: Cette analyse est générée en local sécurisé. Configurez votre clé d'API de service dans vos paramètres pour activer l'analyse avancée par l'intelligence artificielle cloud !)",
            tips = listOf(
                "Prenez 5 minutes de respiration carrée (inspirer 4s, bloquer 4s, expirer 4s, bloquer 4s).",
                "Écrivez au moins 3 sources de gratitude pour lesquelles vous êtes reconnaissant aujourd'hui.",
                "Faites une marche de 15 minutes à l'extérieur pour vous vider l'esprit."
            )
        )
    }
}

data class AiReport(
    val score: Int,
    val description: String,
    val tips: List<String>
)

data class ViralityReport(
    val score: Int,
    val tier: String,
    val strengths: String,
    val advice: String
)
