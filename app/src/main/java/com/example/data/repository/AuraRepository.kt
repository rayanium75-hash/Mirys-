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
     * Call Gemini to generate a smart AI task checklist from a user goal in French
     */
    suspend fun generateAiTasks(goal: String): List<Task> = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w("AuraRepository", "API Key is empty, using offline mock mode")
            return@withContext getMockTasksForGoal(goal)
        }

        val prompt = """
            Tu es Mirys AI, un assistant d'organisation personnel et de bien-être intelligent, créé par l'entreprise rayanium68 de l'auteur Alane Mentii. 
            L'utilisateur souhaite accomplir l'objectif suivant : "$goal".
            Génère exactement entre 3 et 5 étapes/tâches concrètes et réalistes pour atteindre cet objectif.
            
            Réponds EXCLUSIVEMENT sous ce format strict pour me permettre de le parser, une tâche par ligne :
            - [TITRE] Titre de la tâche | [PRIORITE] Haute (ou Moyenne ou Basse) | [CATEGORIE] Bien-être (ou Personnel ou Études ou Général)
            
            Exemple :
            - [TITRE] Programmer la structure HTML du site | [PRIORITE] Haute | [CATEGORIE] Général
            - [TITRE] Faire 10 minutes d'étirement | [PRIORITE] Basse | [CATEGORIE] Bien-être
            
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
            return@withContext "Je fonctionne actuellement en mode local indépendant car aucune clé d'API Gemini n'a été rattachée dans le panneau de secrets de Google AI Studio. " +
                    "Je m'appelle Mirys AI et j'ai été créée par l'entreprise rayanium68 d'Alane Mentii ! " +
                    "Une fois que tu auras ajouté ta clé d'API, je pourrai répondre intelligemment à toutes tes questions complexes ! En attendant : " +
                    "Prends grand soin de toi, bois de l'eau, et reste zen !"
        }

        val historyFormatted = history.takeLast(6).joinToString("\n") { (msg, isUser) ->
            if (isUser) "Utilisateur: $msg" else "Mirys AI: $msg"
        }

        val prompt = """
            Tu es Mirys AI, un compagnon d'entraide, d'intelligence émotionnelle, de bien-être et de productivité bienveillant.
            Tu as été conçu et créé par l'entreprise rayanium68 fondée par Alane Mentii.
            Tu assistes l'utilisateur de manière positive, amicale, inspirante et empathique.
            Tu dois TOUJOURS savoir que tu t'appelles Mirys AI et que ton créateur est rayanium68 d'Alane Mentii. S'ils te demandent qui t'a fait ou comment tu t'appelles, réponds fièrement cela !
            
            Historique récent de la conversation :
            $historyFormatted
            
            Nouveau message de l'utilisateur : "$userMessage"
            
            Réponds chaleureusement en français dans un style épuré, instructif et soigné. Veille ABSOLUMENT à toujours rédiger des phrases complètes, bien construites, à terminer toutes tes phrases par un point final, et à donner une réponse complète sans jamais la couper ou la tronquer au milieu. Sois inspirant et de bon conseil.
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
                // Formatting format: - [TITRE] Titre de la tâche | [PRIORITE] Haute | [CATEGORIE] Général
                val rawTitre = line.substringAfter("[TITRE]").substringBefore("|").trim()
                val rawPriorite = if (line.contains("[PRIORITE]")) {
                    line.substringAfter("[PRIORITE]").substringBefore("|").trim()
                } else "Moyenne"
                val rawCategorie = if (line.contains("[CATEGORIE]")) {
                    line.substringAfter("[CATEGORIE]").trim()
                } else "Général"

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
                    tasks.add(Task(title = rawTitre, priority = priority, category = category))
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
            var currentConseil = StringBuilder()

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

    // Mock/Offline lists
    private fun getMockTasksForGoal(goal: String): List<Task> {
        val lowerGoal = goal.lowercase()
        return when {
            lowerGoal.contains("courir") || lowerGoal.contains("sport") || lowerGoal.contains("marathon") -> {
                listOf(
                    Task(title = "S'étirer correctement pendant 10 minutes", priority = "Moyenne", category = "Bien-être"),
                    Task(title = "Préparer une bouteille d'eau et sa tenue de sport", priority = "Basse", category = "Bien-être"),
                    Task(title = "Courir 30 minutes à allure modérée (endurance fondamentale)", priority = "Haute", category = "Bien-être"),
                    Task(title = "Noter sa performance et ses ressentis physiques", priority = "Basse", category = "Personnel")
                )
            }
            lowerGoal.contains("etud") || lowerGoal.contains("appr") || lowerGoal.contains("lire") || lowerGoal.contains("cours") -> {
                listOf(
                    Task(title = "Définir l'objectif de révision de la session", priority = "Haute", category = "Études"),
                    Task(title = "Éliminer toutes les distractions (téléphone en silencieux)", priority = "Basse", category = "Personnel"),
                    Task(title = "Étudier pendant 25 minutes (Méthode Pomodoro)", priority = "Haute", category = "Études"),
                    Task(title = "Faire une pause active de 5 minutes", priority = "Moyenne", category = "Bien-être")
                )
            }
            else -> {
                listOf(
                    Task(title = "Décomposer l'objectif '$goal' en sous-étapes simples", priority = "Haute", category = "Général"),
                    Task(title = "Fixer un créneau de 30 minutes dédié aujourd'hui", priority = "Moyenne", category = "Général"),
                    Task(title = "Compléter la première étape avec concentration", priority = "Haute", category = "Général"),
                    Task(title = "Récompenser son effort par une pause agréable", priority = "Basse", category = "Bien-être")
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
            description = "$customDesc (Note: Cette analyse est générée en local. Configurez votre clé d'API Gemini dans AI Studio pour activer l'analyse avancée par l'intelligence artificielle !)",
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
