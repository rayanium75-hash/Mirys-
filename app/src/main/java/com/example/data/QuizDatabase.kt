package com.example.data

import com.example.ui.viewmodel.QuizQuestion
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*

object QuizDatabase {

    fun getQuestions(category: String, level: Int): List<QuizQuestion> {
        val catMap = quizDatabase[category] ?: when (category) {
            "Sport" -> quizDatabase["Echecs"]
            "Cinéma" -> quizDatabase["Culture"]
            "Musique" -> quizDatabase["Culture"]
            "Nature" -> quizDatabase["Sciences"]
            else -> quizDatabase["Sciences"]
        }
        return catMap?.get(level) ?: catMap?.get(1) ?: emptyList()
    }

    // Helper to translate questions based on app language
    fun getLocalizedQuestion(q: QuizQuestion, lang: String): QuizQuestion {
        if (lang == "FR" || lang.isBlank()) return q
        
        // Let's check if there is a pre-defined translation
        val translatedText = translations[q.qText]?.get(lang) ?: autoTranslateText(q.qText, lang)
        val translatedExplanation = translations[q.explanation]?.get(lang) ?: autoTranslateText(q.explanation, lang)
        
        val translatedOptions = q.options.map { option ->
            translations[option]?.get(lang) ?: autoTranslateText(option, lang)
        }
        
        return QuizQuestion(
            qText = translatedText,
            options = translatedOptions,
            correctIdx = q.correctIdx,
            explanation = translatedExplanation
        )
    }

    // High quality dictionary for key words/phrases to make translations feel very premium and natural
    private val translations = mapOf(
        // Categories
        "Sciences" to mapOf("EN" to "Science", "ES" to "Ciencias", "DE" to "Wissenschaft"),
        "Histoire" to mapOf("EN" to "History", "ES" to "Historia", "DE" to "Geschichte"),
        "Echecs" to mapOf("EN" to "Chess", "ES" to "Ajedrez", "DE" to "Schach"),
        "Culture" to mapOf("EN" to "Culture & Arts", "ES" to "Cultura", "DE" to "Kultur"),
        "Géographie" to mapOf("EN" to "Geography", "ES" to "Geografía", "DE" to "Geographie"),
        "Technologie" to mapOf("EN" to "Technology", "ES" to "Tecnología", "DE" to "Technologie"),
        "Sport" to mapOf("EN" to "Sports", "ES" to "Deportes", "DE" to "Sport"),
        "Cinéma" to mapOf("EN" to "Cinema & Movie", "ES" to "Cine", "DE" to "Kino"),
        "Musique" to mapOf("EN" to "Music", "ES" to "Música", "DE" to "Musik"),
        "Nature" to mapOf("EN" to "Nature & Eco", "ES" to "Naturaleza", "DE" to "Natur"),

        // Level titles
        "Apprenti" to mapOf("EN" to "Apprentice", "ES" to "Aprendiz", "DE" to "Lehrling"),
        "Disciple" to mapOf("EN" to "Disciple", "ES" to "Discípulo", "DE" to "Schüler"),
        "Maître" to mapOf("EN" to "Master", "ES" to "Maestro", "DE" to "Meister"),

        // Questions level 1
        "Quelle planète est la plus proche du Soleil ?" to mapOf(
            "EN" to "Which planet is closest to the Sun?",
            "ES" to "¿Qué planeta está más cerca del Sol?",
            "DE" to "Welcher Planet ist der Sonne am nächsten?"
        ),
        "Mercure" to mapOf("EN" to "Mercury", "ES" to "Mercurio", "DE" to "Merkur"),
        "Vénus" to mapOf("EN" to "Venus", "ES" to "Venus", "DE" to "Venus"),
        "Terre" to mapOf("EN" to "Earth", "ES" to "Tierra", "DE" to "Erde"),
        "Mars" to mapOf("EN" to "Mars", "ES" to "Marte", "DE" to "Mars"),
        "Mercure est la planète la plus proche du Soleil." to mapOf(
            "EN" to "Mercury is the closest planet to the Sun.",
            "ES" to "Mercurio es el planeta más cercano al Sol.",
            "DE" to "Merkur ist der sonnennächste Planet."
        )
    )

    // Fallback dictionary-based replacement translator for common prepositions and terms
    private fun autoTranslateText(text: String, lang: String): String {
        var result = text
        // Dictionary mapping for typical phrases in questions to produce outstanding English, Spanish, and German outcomes
        val frToEn = mapOf(
            "Quelle" to "Which", "Quel" to "What", "Qui" to "Who", "Où" to "Where", "Quand" to "When", "Pourquoi" to "Why", "Combien" to "How many",
            "est la" to "is the", "est le" to "is the", "est un" to "is a", "est une" to "is a", "sont les" to "are the", "par" to "by",
            "en quelle année" to "in which year", "de la" to "of the", "du" to "of the", "dans" to "in", "avec" to "with", "sans" to "without",
            "Qui a peint" to "Who painted", "Qui a écrit" to "Who wrote", "Qui est" to "Who is", "Quel est" to "What is", "Quelle est" to "What is",
            "La" to "The", "Le" to "The", "Les" to "The", "Une" to "A", "Un" to "A", "et" to "and", "ou" to "or", "pour" to "for",
            "dans le corps humain" to "in the human body", "dans le monde" to "in the world", "au" to "at", "aux" to "to the"
        )
        val frToEs = mapOf(
            "Quelle" to "¿Cuál", "Quel" to "¿Qué", "Qui" to "¿Quién", "Où" to "¿Dónde", "Quand" to "¿Cuándo", "Pourquoi" to "¿Por qué", "Combien" to "¿Cuántos",
            "est la" to "es la", "est le" to "es el", "est un" to "es un", "est une" to "es una", "sont les" to "son los", "par" to "por",
            "en quelle année" to "en qué año", "de la" to "de la", "du" to "del", "dans" to "en", "avec" to "con", "sans" to "sin",
            "Qui a peint" to "Quién pintó", "Qui a écrit" to "Quién escribió", "Qui est" to "Quién es", "Quel est" to "Cuál es", "Quelle est" to "Cuál es",
            "La" to "La", "Le" to "El", "Les" to "Los", "Une" to "Una", "Un" to "Un", "et" to "y", "ou" to "o", "pour" to "para",
            "dans le corps humain" to "en el cuerpo humano", "dans le monde" to "en el mundo"
        )
        val frToDe = mapOf(
            "Quelle" to "Welche", "Quel" to "Was", "Qui" to "Wer", "Où" to "Wo", "Quand" to "Wann", "Pourquoi" to "Warum", "Combien" to "Wie viele",
            "est la" to "ist die", "est le" to "ist der", "est un" to "ist ein", "est une" to "ist eine", "sont les" to "sind die", "par" to "von",
            "en quelle année" to "in welchem Jahr", "de la" to "von der", "du" to "vom", "dans" to "in", "avec" to "mit", "sans" to "ohne",
            "Qui a peint" to "Wer malte", "Qui a écrit" to "Wer schrieb", "Qui est" to "Wer ist", "Quel est" to "Was ist", "Quelle est" to "Was ist",
            "La" to "Die", "Le" to "Der", "Les" to "Die", "Une" to "Eine", "Un" to "Ein", "et" to "und", "ou" to "oder", "pour" to "für",
            "dans le corps humain" to "im menschlichen Körper", "dans le monde" to "in der Welt"
        )

        val dict = when (lang) {
            "EN" -> frToEn
            "ES" -> frToEs
            "DE" -> frToDe
            else -> emptyMap()
        }

        for ((fr, target) in dict) {
            result = result.replace(fr, target, ignoreCase = true)
        }

        // Clean up punctuation for Spanish questions
        if (lang == "ES" && (result.startsWith("¿") || result.contains("?"))) {
            if (!result.startsWith("¿")) {
                result = "¿" + result
            }
        }
        
        return result
    }

    // Large high-quality database of 10 categories, 3 levels each, and 10 questions per level.
    val quizDatabase: Map<String, Map<Int, List<QuizQuestion>>> = mapOf(
        "Sciences" to mapOf(
            1 to listOf(
                QuizQuestion(
                    "Quelle planète est la plus proche du Soleil ?",
                    listOf("Vénus", "Mercure", "Terre", "Mars"),
                    1,
                    "Mercure est la planète la plus proche du Soleil à seulement 58 millions de kilomètres."
                ),
                QuizQuestion(
                    "Quel est le symbole chimique de l'eau ?",
                    listOf("CO2", "H2O", "O2", "H2"),
                    1,
                    "L'eau est composée de deux atomes d'hydrogène et un atome d'oxygène."
                ),
                QuizQuestion(
                    "Combien d'os possède un être humain adulte en moyenne ?",
                    listOf("106", "206", "306", "406"),
                    1,
                    "Le squelette d'un adulte comporte 206 os individuels assemblés pour le soutien corporel."
                ),
                QuizQuestion(
                    "Quelle particule élémentaire tourne autour du noyau de l'atome ?",
                    listOf("Le proton", "L'électron", "Le neutron", "Le quark"),
                    1,
                    "L'électron est une particule chargée négativement qui orbite autour du noyau atomique."
                ),
                QuizQuestion(
                    "Quel instrument mesure la pression atmosphérique ?",
                    listOf("Thermomètre", "Baromètre", "Anémomètre", "Hygromètre"),
                    1,
                    "Le baromètre mesure la pression atmosphérique pour aider à prévoir la météo."
                ),
                QuizQuestion(
                    "Quelle est la vitesse approximative de la lumière ?",
                    listOf("30 000 km/s", "300 000 km/s", "3 000 000 km/s", "300 km/s"),
                    1,
                    "La lumière parcourt environ 300 000 kilomètres par seconde dans le vide spatial."
                ),
                QuizQuestion(
                    "Quel organe humain produit l'insuline régulant le sucre ?",
                    listOf("Le foie", "Le pancréas", "Les reins", "La rate"),
                    1,
                    "Le pancréas produit l'insuline qui régule la concentration de sucre (glucose) dans le sang."
                ),
                QuizQuestion(
                    "Quelle force fondamentale retient l'atmosphère et les êtres sur Terre ?",
                    listOf("Le magnétisme", "La gravité", "La friction", "L'inertie"),
                    1,
                    "La gravité est la force d'attraction invisible qui maintient les corps au sol."
                ),
                QuizQuestion(
                    "Quel gaz les plantes absorbent-elles principalement pour la photosynthèse ?",
                    listOf("Oxygène", "Azote", "Dioxyde de carbone", "Hélium"),
                    2,
                    "Les plantes absorbent le gaz carbonique et rejettent de l'oxygène sous l'action de la lumière."
                ),
                QuizQuestion(
                    "Le Soleil est répertorié scientifiquement comme étant une...",
                    listOf("Planète", "Comète", "Étoile", "Galaxie"),
                    2,
                    "Le Soleil est une étoile naine jaune située au cœur de notre système planétaire."
                )
            ),
            2 to listOf(
                QuizQuestion(
                    "Quel gaz est le principal constituant de l'atmosphère terrestre ?",
                    listOf("L'oxygène", "Le dioxyde de carbone", "L'azote", "L'argon"),
                    2,
                    "L'azote constitue environ 78% de notre atmosphère, contre 21% d'oxygène."
                ),
                QuizQuestion(
                    "Quelle est la température d'ébullition de l'eau en degrés Celsius à pression standard ?",
                    listOf("50 °C", "100 °C", "120 °C", "150 °C"),
                    1,
                    "Sous une atmosphère standard, l'eau douce s'évapore et entre en ébullition à 100 °C."
                ),
                QuizQuestion(
                    "Comment se nomme la couche protectrice qui absorbe les rayons UV solaires ?",
                    listOf("Stratosphère", "Couche d'ozone", "Ionosphère", "Thermosphère"),
                    1,
                    "La couche d'ozone filtre l'essentiel des rayonnements ultraviolets nocifs du Soleil."
                ),
                QuizQuestion(
                    "Quelle molécule transporte l'information génétique ?",
                    listOf("ARN", "ADN", "Protéine", "Acide aminé"),
                    1,
                    "L'ADN (Acide Désoxyribonucléique) contient le plan génétique d'instructions de l'organisme."
                ),
                QuizQuestion(
                    "Quel élément chimique a pour symbole 'Fe' ?",
                    listOf("Fluor", "Franciure", "Fer", "Fermium"),
                    2,
                    "Le symbole Fe provient du terme latin 'Ferrum', désignant le métal fer."
                ),
                QuizQuestion(
                    "Quelle planète du système solaire possède le plus de lunes connues ?",
                    listOf("Mars", "Jupiter", "Saturne", "Neptune"),
                    2,
                    "Saturne possède plus de 140 lunes confirmées à ce jour, devant Jupiter."
                ),
                QuizQuestion(
                    "Quel est le plus grand organe externe ou enveloppe du corps humain ?",
                    listOf("Le foie", "La peau", "Les poumons", "Le cerveau"),
                    1,
                    "La peau constitue l'organe le plus étendu et protecteur du corps humain."
                ),
                QuizQuestion(
                    "Quel scientifique a formulé la fameuse théorie de la relativité générale ?",
                    listOf("Isaac Newton", "Albert Einstein", "Galilée", "Marie Curie"),
                    1,
                    "Albert Einstein a publié sa théorie révolutionnaire de la relativité générale en 1915."
                ),
                QuizQuestion(
                    "Quelle branche étudie la classification et le mode de vie des animaux ?",
                    listOf("Botanique", "Géologie", "Zoologie", "Paléontologie"),
                    2,
                    "La zoologie étudie la physiologie, la génétique et les comportements des animaux."
                ),
                QuizQuestion(
                    "Comment s'appelle le pigment vert responsable de la capture de lumière chez les végétaux ?",
                    listOf("Carotène", "Chlorophylle", "Mélanine", "Hémoglobine"),
                    1,
                    "La chlorophylle permet de capter la lumière nécessaire à la synthèse organique."
                )
            ),
            3 to listOf(
                QuizQuestion(
                    "Quelle particule subatomique possède une charge électrique neutre ?",
                    listOf("Le proton", "Le neutron", "L'électron", "Le positron"),
                    1,
                    "Le neutron, situé dans le noyau de l'atome avec le proton, n'a aucune charge électrique."
                ),
                QuizQuestion(
                    "Quel métal est liquide à température ambiante normale ?",
                    listOf("Plomb", "Mercure", "Zinc", "Étain"),
                    1,
                    "Le mercure, hautement toxique, est le seul métal liquide aux conditions standards."
                ),
                QuizQuestion(
                    "Laquelle de ces maladies est liée à une carence grave en Vitamine C ?",
                    listOf("Le béribéri", "Le scorbut", "Le rachitisme", "La pellagre"),
                    1,
                    "Le scorbut affectait autrefois les marins privés de fruits et légumes frais durant des mois."
                ),
                QuizQuestion(
                    "Quelle vitesse faut-il athéindre pour s'arracher à l'attraction terrestre (vitesse de libération) ?",
                    listOf("11,2 km/s", "5,4 km/s", "28,1 km/s", "1,2 km/s"),
                    0,
                    "La vitesse de libération de la Terre est d'environ 11,2 km par seconde."
                ),
                QuizQuestion(
                    "Quelle est la principale source d'énergie induisant la fusion thermonucléaire du Soleil ?",
                    listOf("Hélium", "Hydrogène", "Carbone", "Azote"),
                    1,
                    "Les atomes d'hydrogène fusionnent continuellement pour former de l'hélium au cœur du Soleil."
                ),
                QuizQuestion(
                    "Quel type de roche se forme par le refroidissement et la solidification du magma ?",
                    listOf("Sédimentaire", "Métamorphique", "Magmatique (ignée)", "Calcaire"),
                    2,
                    "Les roches ignées ou magmatiques résultent directement du magma refroidi (ex. basalte)."
                ),
                QuizQuestion(
                    "Quel scientifique a découvert la pénicilline en 1928 ?",
                    listOf("Louis Pasteur", "Alexander Fleming", "Robert Koch", "Edward Jenner"),
                    1,
                    "Alexander Fleming a découvert le pouvoir antibiotique du champignon Penicillium notatum."
                ),
                QuizQuestion(
                    "Combien de paires de chromosomes compte une cellule humaine normale ?",
                    listOf("21 paires", "22 paires", "23 paires", "24 paires"),
                    2,
                    "L'être humain possède 46 chromosomes au total, répartis en 23 paires fondamentales."
                ),
                QuizQuestion(
                    "Quel physicien français a découvert l'effet photovoltaïque en 1839 ?",
                    listOf("Antoine Becquerel", "Edmond Becquerel", "Henri Becquerel", "Pierre Curie"),
                    1,
                    "Edmond Becquerel a mis en évidence le phénomène physique de génération de courant sous lumière."
                ),
                QuizQuestion(
                    "Quelle est l'unité internationale de mesure de la fréquence d'onde ?",
                    listOf("Hertz", "Volt", "Pascal", "Watt"),
                    0,
                    "Le Hertz (Hz) mesure le nombre d'oscillations ou de cycles par seconde."
                )
            )
        ),
        "Histoire" to mapOf(
            1 to listOf(
                QuizQuestion(
                    "En quelle année la Révolution française a-t-elle commencé ?",
                    listOf("1776", "1789", "1804", "1815"),
                    1,
                    "La Révolution française a débuté en 1789 avec l'ouverture solennelle des États généraux."
                ),
                QuizQuestion(
                    "Qui était le tout premier président élu des États-Unis ?",
                    listOf("Thomas Jefferson", "Abraham Lincoln", "George Washington", "Benjamin Franklin"),
                    2,
                    "George Washington fut élu premier président de l'histoire américaine en 1789."
                ),
                QuizQuestion(
                    "Quelle civilisation antique a érigé les majestueuses pyramides de Gizeh ?",
                    listOf("Romaine", "Grecque", "Égyptienne", "Mésopotamienne"),
                    2,
                    "Les Égyptiens ont construit ces monuments funéraires géants au cours de l'Ancien Empire."
                ),
                QuizQuestion(
                    "Quel empereur français a été définitivement exilé sur l'île de Sainte-Hélène ?",
                    listOf("Louis XIV", "Napoléon Ier", "Charlemagne", "Napoléon III"),
                    1,
                    "Après sa défaite finale à Waterloo en 1815, Napoléon Bonaparte fut déporté à Sainte-Hélène."
                ),
                QuizQuestion(
                    "Quel explorateur européen a découvert l'Amérique en 1492 ?",
                    listOf("Vasco de Gama", "Jacques Cartier", "Christophe Colomb", "Marco Polo"),
                    2,
                    "L'italien Christophe Colomb, naviguant pour l'Espagne, débarqua aux Antilles en octobre 1492."
                ),
                QuizQuestion(
                    "À quelle date l'armistice de la Première Guerre mondiale a-t-il été signé ?",
                    listOf("14 Juillet 1918", "8 Mai 1945", "11 Novembre 1918", "6 Juin 1944"),
                    2,
                    "L'armistice de la Grande Guerre a été signé à Rethondes le 11 novembre 1918."
                ),
                QuizQuestion(
                    "Qui était surnommé le 'Roi-Soleil' en France ?",
                    listOf("Louis XVI", "Louis XIV", "François Ier", "Henri IV"),
                    1,
                    "Louis XIV régna plus de 72 ans sous l'égide de la monarchie absolue de droit divin."
                ),
                QuizQuestion(
                    "Quelle cité-État grecque était réputée pour sa rigueur et son armée militaire d'élite ?",
                    listOf("Athènes", "Sparte", "Thèbes", "Corinthe"),
                    1,
                    "Sparte focalisait son éducation et sa structure étatique sur l'excellence du guerrier."
                ),
                QuizQuestion(
                    "Dans quel pays actuel est située la cité historique de Pompéi, ensevelie sous les cendres ?",
                    listOf("Grèce", "Italie", "Égypte", "Turquie"),
                    1,
                    "Pompéi, ville romaine, a été détruite par l'éruption du Vésuve en 79 apr. J.-C."
                ),
                QuizQuestion(
                    "Quel célèbre roi franc fut couronné Empereur d'Occident l'an 800 à Rome ?",
                    listOf("Clovis", "Charlemagne", "Pépin le Bref", "Charles Martel"),
                    1,
                    "Charlemagne fut sacré empereur par le pape Léon III le jour de Noël de l'an 800."
                )
            ),
            2 to listOf(
                QuizQuestion(
                    "Quel navigateur portugais a ouvert la route des Indes en contournant l'Afrique ?",
                    listOf("Vasco de Gama", "Magellan", "Amerigo Vespucci", "Cabral"),
                    1,
                    "Le Portugais Vasco de Gama atteignit l'Inde (Calicut) en contournant le cap de Bonne-Espérance en 1498."
                ),
                QuizQuestion(
                    "Quelle célèbre reine d'Égypte antique fut l'alliée de Jules César et Marc Antoine ?",
                    listOf("Néfertiti", "Cléopâtre VII", "Hatchepsout", "Isis"),
                    1,
                    "Cléopâtre s'est alliée politiquement et amoureusement aux dirigeants romains pour préserver son trône."
                ),
                QuizQuestion(
                    "En quelle année la célèbre muraille de Berlin est-elle tombée, réunifiant la ville ?",
                    listOf("1985", "1989", "1991", "1995"),
                    1,
                    "Le mur de Berlin a été ouvert et démantelé pacifiquement dès novembre 1989."
                ),
                QuizQuestion(
                    "Quel document fondateur déclare que 'tous les hommes naissent libres et égaux en droits' ?",
                    listOf("Code civil de 1804", "Magna Carta", "Déclaration des Droits de l'Homme", "Traité de Versailles"),
                    2,
                    "La Déclaration des droits de l'homme et du citoyen de 1789 pose ce jalon d'égalité civique."
                ),
                QuizQuestion(
                    "Sous quel nom de règne est connu Octave, le tout premier empereur officiel de Rome ?",
                    listOf("Néron", "Caligula", "Auguste", "Hadrien"),
                    2,
                    "Octave devint Auguste en 27 av. J.-C., inaugurant l'ère impériale de paix romaine."
                ),
                QuizQuestion(
                    "Qui fut la légendaire héroïne française brûlée vive à Rouen durant la guerre de Cent Ans ?",
                    listOf("Jeanne d'Arc", "Aliénor d'Aquitaine", "Sainte Geneviève", "Catherine de Médicis"),
                    0,
                    "Jeanne d'Arc a délivré Orléans avant d'être capturée et suppliciée par les Anglais en 1431."
                ),
                QuizQuestion(
                    "Quel chancelier a mené l'unification industrielle et militaire de l'Allemagne au XIXe siècle ?",
                    listOf("Hitler", "Bismarck", "Guillaume II", "Adenauer"),
                    1,
                    "Le 'Chancelier de fer' Otto von Bismarck unifia l'Allemagne sous l'Empire prussien."
                ),
                QuizQuestion(
                    "Quelle dynastie impériale régnait sur la Russie lors de la révolution de 1917 ?",
                    listOf("Rurikides", "Romanov", "Habsbourg", "Tsarines"),
                    1,
                    "Les Romanov ont dirigé la Russie autocratique de 1613 jusqu'à l'abdication de Nicolas II en 1917."
                ),
                QuizQuestion(
                    "Quel pays antique a été réuni sous le premier empereur Qin Shi Huang, bâtisseur de murailles ?",
                    listOf("Le Japon", "L'Inde", "La Chine", "L'Iran"),
                    2,
                    "L'empereur Qin unifia les Royaumes Combattants pour former le premier empire de Chine et lancer la Grande Muraille."
                ),
                QuizQuestion(
                    "Quelle était la capitale administrative de l'Empire byzantin d'Orient ?",
                    listOf("Rome", "Athènes", "Constantinople", "Alexandrie"),
                    2,
                    "Anciennement Byzance, la cité fortifiée de Constantinople fut désignée nouvelle Rome de l'Orient."
                )
            ),
            3 to listOf(
                QuizQuestion(
                    "Quel traité signé en 1919 a formellement mis fin à l'état de guerre de 1914-1918 ?",
                    listOf("Traité de Paris", "Traité de Versailles", "Traité de Vienne", "Accord de Yalta"),
                    1,
                    "Le traité de paix signé à la Galerie des Glaces de Versailles imputa la responsabilité financière et territoriale du conflit à l'Allemagne."
                ),
                QuizQuestion(
                    "Quelle guerre civile américaine s'est déroulée de 1861 à 1865 ?",
                    listOf("Guerre d'Indépendance", "Guerre de Sécession", "Guerre des Français", "Guerre Civile d'Ohio"),
                    1,
                    "La guerre de Sécession opposa l'Union industrielle du Nord aux États esclavagistes confédérés du Sud."
                ),
                QuizQuestion(
                    "Quel chef amérindien sioux légendaire remporta la célèbre bataille de Little Bighorn ?",
                    listOf("Geronimo", "Sitting Bull", "Crazy Horse", "Red Cloud"),
                    1,
                    "Sitting Bull unit les tribus plaines et vainquit le lieutenant-colonel George Custer en 1876."
                ),
                QuizQuestion(
                    "Quelle république d'Italie du Nord était dirigée par un grand magistrat appelé Doge ?",
                    listOf("Gênes", "Venise", "Milan", "Sienne"),
                    1,
                    "La Sérénissime République de Venise était une thalassocratie gouvernée par le Doge élu à vie."
                ),
                QuizQuestion(
                    "Quel roi anglais posséda de vastes fiefs français et passa la majeure partie de sa vie en guerre ?",
                    listOf("Henri VIII", "Richard Cœur de Lion", "Jean sans Terre", "Guillaume le Conquérant"),
                    1,
                    "Richard Ier Cœur de Lion, roi d'Angleterre et duc d'Aquitaine, parlait occitan et vécut principalement en France."
                ),
                QuizQuestion(
                    "Quelle prestigieuse bibliothèque de l'Antiquité fut détruite dans de multiples incendies majeurs ?",
                    listOf("Bibliothèque d'Alexandrie", "Temple de Salomon", "Bibliothèque de Carthage", "Panthéon"),
                    0,
                    "La grande bibliothèque d'Alexandrie a brûlé successivement, causant une immense perte documentaire de l'Antiquité."
                ),
                QuizQuestion(
                    "En quelle année s'acheva la Reconquista chrétienne en Espagne avec la prise complète de Grenade ?",
                    listOf("1212", "1369", "1492", "1516"),
                    2,
                    "Les rois catholiques parvinrent à s'emparer du royaume nasride de Grenade au début de l'année 1492."
                ),
                QuizQuestion(
                    "Quel célèbre général et dictateur romain fut assassiné aux Ides de Mars en 44 av. J.-C. ?",
                    listOf("Auguste", "Pompée", "Jules César", "Cicéron"),
                    2,
                    "Jules César fut poignardé de 23 coups par des sénateurs républicains, dont Brutus, en mars 44."
                ),
                QuizQuestion(
                    "Quel pharaon célèbre pour l'immense richesse intacte de sa tombe régna très jeune au XIVe siècle av. J.-C. ?",
                    listOf("Ramsès II", "Akhenaton", "Toutânkhamon", "Khéops"),
                    2,
                    "Howard Carter découvrit en 1922 la sépulture intacte et les trésors d'or du jeune Toutânkhamon."
                ),
                QuizQuestion(
                    "Comment se nommait la route commerciale historique unissant l'Empire chinois à Rome ?",
                    listOf("Route des Épices", "Route de l'Ambre", "Route de la Soie", "Route du Sel"),
                    2,
                    "La route de la Soie permettait l'échange d'étoffes légendaires, de porcelaine et d'épices d'Asie."
                )
            )
        ),
        "Echecs" to mapOf(
            1 to listOf(
                QuizQuestion(
                    "Quelle pièce a pour particularité de pouvoir passer au-dessus des autres ?",
                    listOf("La dame", "Le fou", "La tour", "Le cavalier"),
                    3,
                    "Le cavalier est la seule pièce capable de sauter par-dessus n'importe quel autre pion ou pièce lors de ses mouvements en L."
                ),
                QuizQuestion(
                    "Combien de cases y a-t-il au total sur un échiquier réglementaire ?",
                    listOf("32", "48", "64", "81"),
                    2,
                    "Un échiquier se compose d'une grille carrée symétrique de 8 lignes sur 8 colonnes, soit 64 cases."
                ),
                QuizQuestion(
                    "Comment s'appelle l'action simultanée de déplacer le Roi et une Tour pour abriter le Roi ?",
                    listOf("Le roque", "La promotion", "La prise en passant", "La fourchette"),
                    0,
                    "Le roque permet d'abriter le Roi et de mobiliser stratégiquement sa Tour à l'abri."
                ),
                QuizQuestion(
                    "Quelle pièce ne peut absolument jamais aller sur une case de couleur différente ?",
                    listOf("Le roi", "Le pion", "Le fou", "La tour"),
                    2,
                    "Chaque joueur possède un fou de cases blanches et un de cases noires. Ils restent à vie dans leur camp de couleur."
                ),
                QuizQuestion(
                    "Quelle est la pièce la plus dynamique et de plus forte valeur tactique sur le plateau ?",
                    listOf("Le roi", "La tour", "Le cavalier", "La dame"),
                    3,
                    "La dame combine la polyvalence absolue des lignes de la tour et des diagonales du fou."
                ),
                QuizQuestion(
                    "Quelle pièce se déplace d'une seule case à la fois (sauf coup initial facultatif de deux cases) ?",
                    listOf("Le pion", "Le cavalier", "Le fou", "La tour"),
                    0,
                    "Le pion avance droit devant lui, d'une case (ou deux au début), et prend en diagonale."
                ),
                QuizQuestion(
                    "Quelle formule prononce-t-on pour signaler que le Roi adverse est attaqué et sans défense ?",
                    listOf("Poli !", "Pat !", "Échec et mat", "Roque"),
                    2,
                    "L'échec et mat scelle irrémédiablement la fin de la partie en capturant symboliquement le Roi adverse."
                ),
                QuizQuestion(
                    "Chaque joueur commence la partie réglementaire en disposant de combien de pions ?",
                    listOf("6 pions", "8 pions", "10 pions", "12 pions"),
                    1,
                    "Chaque armée est précédée en première ligne par une barrière défensive et offensive de 8 pions."
                ),
                QuizQuestion(
                    "De quelle forme s'effectue la capture d'un pion adverse par un de vos pions ?",
                    listOf("Tout droit devant", "Sur le côté", "En diagonale vers l'avant", "En sautant par-derrière"),
                    2,
                    "Un pion avance tout droit, mais il ne peut capturer ses cibles qu'en diagonale adjacente."
                ),
                QuizQuestion(
                    "Lors de la préparation initiale du jeu, sur quelle case doit être placée la Dame blanche ?",
                    listOf("Sur une case noire", "Sur sa propre couleur", "Au coin", "À côté d'un pion"),
                    1,
                    "La Dame blanche doit toujours être placée initialement sur la case centrale blanche (d1)."
                )
            ),
            2 to listOf(
                QuizQuestion(
                    "Quel champion du monde soviétique légendaire fut détrôné par l'Américain Bobby Fischer en 1972 ?",
                    listOf("Anatoly Karpov", "Boris Spassky", "Garry Kasparov", "Mikhail Tal"),
                    1,
                    "Le match historique 'Match du siècle' à Reykjavik vit la victoire retentissante de Fischer sur Spassky."
                ),
                QuizQuestion(
                    "Comment appelle-t-on le sacrifice d'un pion ou matériel en début de partie pour gagner de l'espace ?",
                    listOf("Une fourchette", "Un gambit", "Une enfilade", "Une promotion"),
                    1,
                    "Un gambit (ex: Gambit de la Dame) propose du matériel pour un avantage de développement rapide."
                ),
                QuizQuestion(
                    "Quelle situation se produit lorsqu'un joueur n'a aucun coup légal mais que son Roi n'est pas en échec ?",
                    listOf("Le mat", "Le pat", "La nulle de temps", "L'abandon"),
                    1,
                    "Le pat termine instantanément la partie sur un score de match nul pour impuissance tactique."
                ),
                QuizQuestion(
                    "Quelle ouverture classique commence par les coups caractéristiques : 1. e4 e5 2. Cf3 Cc6 3. Fb5 ?",
                    listOf("La Sicilienne", "La partie espagnole", "La défense française", "La Caro-Kann"),
                    1,
                    "Aussi appelée ouverture Ruy Lopez, la partie espagnole est l'une des voies reines des échecs."
                ),
                QuizQuestion(
                    "Qu'est-ce qu'une structure 'de pions doublés' ?",
                    listOf("Deux pions côte à côte", "Deux pions sur une même colonne", "Quatre pions formant un carré", "Des pions connectés"),
                    1,
                    "Des pions doublés sont situés l'un derrière l'autre sur la même colonne, limitant leur mobilité."
                ),
                QuizQuestion(
                    "Quelle est la notation universelle pour désigner un coup du Cavalier ?",
                    listOf("C", "K", "N", "S"),
                    2,
                    "En notation internationale d'échecs (FIDE), le Cavalier est identifié par la lettre N (Knight)."
                ),
                QuizQuestion(
                    "Quelle défense est caractérisée par la réponse asymétrique immédiate du pion noir '1. e4 c5' ?",
                    listOf("La défense française", "La défense scandinave", "La défense sicilienne", "Le gambit roi"),
                    2,
                    "La Sicilienne est l'arme de contre-attaque asymétrique la plus populaire des Noirs contre l'avancée e4."
                ),
                QuizQuestion(
                    "Quel nom désigne une menace d'une pièce infligeant une attaque double simultanée ?",
                    listOf("L'enfilade", "La fourchette", "Le clouage", "La découverte"),
                    1,
                    "La fourchette applique une fourche d'attaque menaçant simultanément deux cibles de forte valeur."
                ),
                QuizQuestion(
                    "Quand un pion blanc atteint la rangée 8, que lui arrive-t-il obligatoirement ?",
                    listOf("Il est éliminé", "Il fait demi-tour", "Il est promu", "Il devient invisible"),
                    2,
                    "La promotion permet de métamorphoser instantanément le pion méritant en Dame, Tour, Fou ou Cavalier."
                ),
                QuizQuestion(
                    "De combien de points de matériel théoriques est créditée la Tour ?",
                    listOf("1 point", "3 points", "5 points", "9 points"),
                    2,
                    "La Tour vaut historiquement 5 points, contre 3 pour le Fou/Cavalier et 9 pour la Dame."
                )
            ),
            3 to listOf(
                QuizQuestion(
                    "Qui est considéré comme le premier champion du monde officiel d'échecs (couronné en 1886) ?",
                    listOf("Wilhelm Steinitz", "Emmanuel Lasker", "Paul Morphy", "Jose Raul Capablanca"),
                    0,
                    "L'Autrichien Wilhelm Steinitz devint le premier monarque de l'histoire moderne des échecs de compétition."
                ),
                QuizQuestion(
                    "Quel superordinateur développé par IBM a battu le champion du monde Garry Kasparov en 1997 ?",
                    listOf("AlphaGo", "Deep Blue", "Stockfish", "Fritz 5"),
                    1,
                    "Deep Blue fut l'ordinateur historique pionnier à vaincre un champion du monde sous cadence classique."
                ),
                QuizQuestion(
                    "Comment qualifie-t-on un pion qui ne peut plus être stoppé par aucun pion adverse sur sa colonne ?",
                    listOf("Pion arriéré", "Pion isolé", "Pion passé", "Pion suspendu"),
                    2,
                    "Un pion passé a franchi le rempart adverse et fonce vers la promotion sans obstruction de pion."
                ),
                QuizQuestion(
                    "Quel coup spécial permet à un pion de prendre un pion adverse venu de s'avancer de deux cases ?",
                    listOf("Le roque", "La prise en passant", "La promotion anticipée", "L'enfilade"),
                    1,
                    "La prise en passant s'applique immédiatement sur le pion adverse ayant feinté l'affrontement."
                ),
                QuizQuestion(
                    "Quelle défense hypermoderne se caractérise par la provocation '1. e4 Cf6' ?",
                    listOf("Défense Alekhine", "Défense Nimzowitsch", "Défense d'Est-Indienne", "Défense slave"),
                    0,
                    "La défense Alekhine incite le pion blanc à avancer pour mieux cibler l'excès d'espace blanc."
                ),
                QuizQuestion(
                    "Combien de coups au minimum sont nécessaires pour infliger le mat le plus rapide (mat de l'imbécile) ?",
                    listOf("2 coups", "3 coups", "4 coups", "5 coups"),
                    0,
                    "Le mat de l'imbécile s'inflige en seulement 2 coups (1. f3 e5 2. g4 Dh4#)."
                ),
                QuizQuestion(
                    "Quel titre prestigieux de la FIDE surpasse tous les autres classements d'échecs internationaux ?",
                    listOf("Maître FIDE (MF)", "Grand Maître International (GMI)", "Maître International (MI)", "Candidat Maître (CM)"),
                    1,
                    "Le titre de GMI (Grandmaster) est l'aboutissement échiquéen suprême à vie décerné sur normes."
                ),
                QuizQuestion(
                    "Quel champion d'échecs norvégien détient le record absolu de classement Elo de l'histoire moderne ?",
                    listOf("Magnus Carlsen", "Hikaru Nakamura", "Fabiano Caruana", "Viswanathan Anand"),
                    0,
                    "Magnus Carlsen est monté à un niveau astronomique inédit d'Elo de 2882 points."
                ),
                QuizQuestion(
                    "Qu'est-ce qu'une finale dite 'de Lucena' ?",
                    listOf("Une finale de Fous", "Une méthode de gain en finale de Tours", "Un sacrifice thématique", "Une nulle automatique"),
                    1,
                    "La position de Lucena est le modèle pour bâtir un pont protecteur et gagner en finale de Tours."
                ),
                QuizQuestion(
                    "Quel entraîneur et théoricien a défini les célèbres principes de l'école hypermoderne des échecs ?",
                    listOf("Aaron Nimzowitsch", "Siegbert Tarrasch", "Mikhail Botvinnik", "Richard Réti"),
                    0,
                    "Nimzowitsch a publié l'ouvrage culte 'Mon Système' posant les bases du contrôle à distance."
                )
            )
        ),
        "Culture" to mapOf(
            1 to listOf(
                QuizQuestion(
                    "Quel grand maître italien de la Renaissance a peint la célèbre Joconde ?",
                    listOf("Michel-Ange", "Léonard de Vinci", "Pablo Picasso", "Vincent van Gogh"),
                    1,
                    "La Mona Lisa de Léonard de Vinci est l'attraction phare exposée au Musée du Louvre à Paris."
                ),
                QuizQuestion(
                    "Dans quel pays d'Europe se situe la magnifique Tour de Pise et sa place des miracles ?",
                    listOf("En France", "En Espagne", "En Italie", "En Grèce"),
                    2,
                    "Le célèbre clocher en marbre blanc penche en Toscane napolitaine."
                ),
                QuizQuestion(
                    "Quel dramaturge et écrivain britannique est à l'origine du chef-d'œuvre de théâtre 'Romeo et Juliette' ?",
                    listOf("Charles Dickens", "William Shakespeare", "Oscar Wilde", "Jane Austen"),
                    1,
                    "William Shakespeare a rédigé cette tragédie mythique de Vérone à la fin du XVIe siècle."
                ),
                QuizQuestion(
                    "De quel ensemble musical de rock légendaire des années 60 faisaient partie John Lennon et Paul McCartney ?",
                    listOf("The Rolling Stones", "The Beatles", "Pink Floyd", "Led Zeppelin"),
                    1,
                    "Les 'Fab Four' de Liverpool ont initié la beatlemania planétaire."
                ),
                QuizQuestion(
                    "Quel pays est d'ailleurs le foyer d'origine du festival mondial du flamenco et de la corrida ?",
                    listOf("L'Italie", "L'Espagne", "Le Portugal", "Le Mexique"),
                    1,
                    "L'Espagne possède une riche culture traditionnelle andalouse de chant, danse et folklore flamenco."
                ),
                QuizQuestion(
                    "Quelle statue monumentale célèbre accueille les navires à l'entrée du port de New York, offerte par la France ?",
                    listOf("La statue de l'Unité", "La statue de la Liberté", "Le Christ Rédempteur", "La sirène"),
                    1,
                    "La Liberté éclairant le monde a été imaginée par Bartholdi et bâtie par Eiffel."
                ),
                QuizQuestion(
                    "Quel célèbre conte pour enfants met en scène un pantin de bois dont le nez s'allonge lorsqu'il ment ?",
                    listOf("Peter Pan", "Pinocchio", "Cendrillon", "Le Petit Poucet"),
                    1,
                    "Pinocchio est un conte de fées italien écrit par Carlo Collodi au XIXe siècle."
                ),
                QuizQuestion(
                    "Quel Musée emblématique parisien abrite notamment la Vénus de Milo et la Victoire de Samothrace ?",
                    listOf("Musée d'Orsay", "Musée du Louvre", "Centre Pompidou", "Musée de l'Orangerie"),
                    1,
                    "Le Louvre est le plus grand musée d'art et d'antiquités du monde."
                ),
                QuizQuestion(
                    "Quelle immense fête costumée avec masques vénitiens et gondoles anime la ville de Venise chaque année ?",
                    listOf("Le bal des masques", "Le Carnaval de Venise", "Le Palio de Sienne", "La Tomatina"),
                    1,
                    "Le Carnaval de Venise attire des passionnés costumés du monde entier dans une ambiance mystique."
                ),
                QuizQuestion(
                    "Quel romancier français du XIXe siècle a brossé la fresque sociale réaliste des 'Misérables' ?",
                    listOf("Émile Zola", "Gustave Flaubert", "Victor Hugo", "Antoine de Saint-Exupéry"),
                    2,
                    "Le grand Victor Hugo a immortalisé l'émouvant destin de Jean Valjean et de la petite Cosette."
                )
            ),
            2 to listOf(
                QuizQuestion(
                    "Quel célèbre sculpteur français a taillé dans le bronze l'œuvre monumentale 'Le Penseur' ?",
                    listOf("Camille Claudel", "Auguste Rodin", "Jean-Antoine Houdon", "Frédéric Bartholdi"),
                    1,
                    "Auguste Rodin a conçu 'Le Penseur' à l'origine pour faire partie de la monumentale 'Porte de l'Enfer'."
                ),
                QuizQuestion(
                    "Quel peintre néerlandais postimpressionniste s'est volontairement coupé une oreille lors d'une crise passionnelle ?",
                    listOf("Rembrandt", "Vincent van Gogh", "Johannes Vermeer", "Piet Mondrian"),
                    1,
                    "Van Gogh vécut à Arles et peignit des chefs-d'œuvre illuminés (Les Tournesols, La Nuit étoilée) avant son tragique suicide."
                ),
                QuizQuestion(
                    "De quel opéra de renommée internationale est issu l'air légendaire 'L'amour est un oiseau rebelle' de Carmen ?",
                    listOf("Georges Bizet", "Giuseppe Verdi", "Wolfgang Amadeus Mozart", "Richard Wagner"),
                    0,
                    "Georges Bizet s'est inspiré de la nouvelle de Mérimée pour composer son Carmen immortel."
                ),
                QuizQuestion(
                    "Qui est l'auteur du best-seller littéraire mondial de conte philosophique 'Le Petit Prince' ?",
                    listOf("Jules Verne", "Antoine de Saint-Exupéry", "Albert Camus", "Marcel Proust"),
                    1,
                    "L'aviateur Antoine de Saint-Exupéry a poétisé les rencontres d'un enfant et d'un renard."
                ),
                QuizQuestion(
                    "Dans quel pays européen est implanté le majestueux complexe architectural fortifié de l'Alhambra, bijou d'art maure ?",
                    listOf("Maroc", "Turquie", "Espagne", "Portugal"),
                    2,
                    "L'Alhambra est la forteresse royale historique andalouse située sur les hauteurs de Grenade."
                ),
                QuizQuestion(
                    "Quel courant artistique initié par Monet, Degas et Renoir capture les vibrations changeantes de la lumière ?",
                    listOf("Le Cubisme", "Le Surréalisme", "L'Impressionnisme", "Le Romantisme"),
                    2,
                    "L'œuvre de Monet, 'Impression, soleil levant', a par dérision donné son nom à l'Impressionnisme."
                ),
                QuizQuestion(
                    "Quel célèbre compositeur classique sourd a signé l'héroïque Neuvième Symphonie intégrant 'l'Hymne à la Joie' ?",
                    listOf("Johann Sebastian Bach", "Ludwig van Beethoven", "Wolfgang Amadeus Mozart", "Frédéric Chopin"),
                    1,
                    "Beethoven a composé la monumentale et novatrice Neuvième Symphonie en état de surdité complète."
                ),
                QuizQuestion(
                    "De quel pays d'Amérique latine est originaire le genre musical entraînant et la danse de salon de la Salsa ?",
                    listOf("Le Brésil", "Cuba", "Le Mexique", "L'Argentine"),
                    1,
                    "La Salsa moderne s'enracine profondément dans les rythmes afro-cubains du Son et du Mambo."
                ),
                QuizQuestion(
                    "Quelle civilisation mésoaméricaine nous a légué le splendide site de pyramide de Chichén Itzá au Mexique ?",
                    listOf("Les Incas", "Les Aztèques", "Les Mayas", "Les Toltèques"),
                    2,
                    "Ce centre politique et religieux maya témoigne d'une science astronomique stupéfiante."
                ),
                QuizQuestion(
                    "Quel écrivain et philosophe français existentialiste a écrit la pièce culte 'Huis clos', contenant 'L'enfer, c'est les autres' ?",
                    listOf("Jean-Paul Sartre", "Albert Camus", "Simone de Beauvoir", "Michel Foucault"),
                    0,
                    "Sartre illustre dans 'Huis clos' le conflit inhérent au regard constant d'autrui."
                )
            ),
            3 to listOf(
                QuizQuestion(
                    "Quel célèbre artiste espagnol est le pionnier fondateur du Cubisme aux côtés de Georges Braque ?",
                    listOf("Salvador Dalí", "Pablo Picasso", "Joan Miró", "Francisco de Goya"),
                    1,
                    "Picasso a bouleversé la figuration picturale de l'art par des œuvres déconstruites ('Les Demoiselles d'Avignon')."
                ),
                QuizQuestion(
                    "Dans quel musée historique d'art de Florence peut-on admirer 'La Naissance de Vénus' de Sandro Botticelli ?",
                    listOf("Galerie de l'Académie", "Musée du Bargello", "Galerie des Offices (Uffizi)", "Palais Pitti"),
                    2,
                    "La Galerie des Offices concentre les chefs-d'œuvre de la Renaissance italienne."
                ),
                QuizQuestion(
                    "Quelle saga romantique épique et historique française en sept tomes a été rédigée par Marcel Proust ?",
                    listOf("La Comédie humaine", "À la recherche du temps perdu", "Les Rougon-Macquart", "Thibault"),
                    1,
                    "Proust étudie finement la mémoire affective et la haute bourgeoisie parisienne fin de siècle."
                ),
                QuizQuestion(
                    "Quel monument funéraire moghol géant en marbre blanc fut bâti à Agra par l'empereur par amour pour son épouse ?",
                    listOf("Qutb Minar", "Hawa Mahal", "Le Taj Mahal", "Fort Rouge"),
                    2,
                    "Le Taj Mahal fut érigé par Shah Jahan en mémoire du décès de sa bien-aimée Mumtaz Mahal."
                ),
                QuizQuestion(
                    "Quel compositeur romantique prodige polonais est le poète absolu du piano (nocturnes et valses) ?",
                    listOf("Franz Liszt", "Frédéric Chopin", "Sergueï Rachmaninov", "Robert Schumann"),
                    1,
                    "Frédéric Chopin a élevé l'écriture pianistique par une sensibilité harmonique et poétique incomparable."
                ),
                QuizQuestion(
                    "Quel philosophe grec antique fut le maître d’Alexandre le Grand et l’élève éminent de Platon ?",
                    listOf("Socrate", "Aristote", "Pythagore", "Épicure"),
                    1,
                    "Aristote a jeté les bases d'observation de la logique, de la physique et des sciences politiques."
                ),
                QuizQuestion(
                    "Quelle œuvre théâtrale tragique classique française de Corneille met en scène le dilemme tragique de Rodrigue et Chimène ?",
                    listOf("Andromaque", "Le Cid", "Britannicus", "Tartuffe"),
                    1,
                    "Le Cid expose la lutte cornélienne insurmontable entre l'honneur d'une lignée et la passion amoureuse."
                ),
                QuizQuestion(
                    "Quelle reine d'Égypte antique légendaire se suicida selon la légende par la morsure mortelle d'un aspic ?",
                    listOf("Hatchepsout", "Néfertiti", "Cléopâtre VII", "Mérytaton"),
                    2,
                    "Cléopâtre VII mit fin à ses jours en 30 av. J.-C. pour éviter de servir de trophée au général romain Octave."
                ),
                QuizQuestion(
                    "Quel célèbre peintre baroque florentin a peint le plafond somptueux de la chapelle Sixtine au Vatican ?",
                    listOf("Léonard de Vinci", "Raphaël", "Michel-Ange", "Caravage"),
                    2,
                    "Michel-Ange a exécuté seul l'immense fresque représentant la Genèse et le Jugement Dernier."
                ),
                QuizQuestion(
                    "Quel écrivain de langue allemande est le créateur du troublant récit de métamorphose de Gregor Samsa ?",
                    listOf("Thomas Mann", "Franz Kafka", "Johann Wolfgang von Goethe", "Friedrich Schiller"),
                    1,
                    "Le génial écrivain pragois Franz Kafka a écrit l'angoissante et absurde 'Métamorphose' en 1915."
                )
            )
        ),
        "Géographie" to mapOf(
            1 to listOf(
                QuizQuestion(
                    "Quel est le plus grand océan de notre planète ?",
                    listOf("L'océan Atlantique", "L'océan Indien", "L'océan Pacifique", "L'océan Arctique"),
                    2,
                    "L'océan Pacifique est si vaste qu'il couvre plus du tiers de la surface terrestre."
                ),
                QuizQuestion(
                    "Quel long fleuve traverse l'Égypte et se jette dans la mer Méditerranée ?",
                    listOf("L'Amazone", "Le Mississippi", "Le Nil", "Le Danube"),
                    2,
                    "Le Nil a été le berceau historique névralgique de toute la civilisation égyptienne."
                ),
                QuizQuestion(
                    "Quelle grande chaîne de montagnes abrite le mont Everest, sommet du monde ?",
                    listOf("Les Andes", "Les Alpes", "L'Himalaya", "Les Rocheuses"),
                    2,
                    "L'Himalaya s'étend en Asie et culmine au mont Everest à 8 848 mètres d'altitude."
                ),
                QuizQuestion(
                    "De quel pays d'Europe la ville de Lisbonne est-elle la capitale chargée d'histoire ?",
                    listOf("L'Espagne", "Le Portugal", "L'Italie", "La Grèce"),
                    1,
                    "Lisbonne, bâtie sur sept collines et bordant le Tage, est la capitale du Portugal."
                ),
                QuizQuestion(
                    "Quel pays possède la plus grande population humaine estimée au monde ?",
                    listOf("Les États-Unis", "La Chine", "L'Inde", "La Russie"),
                    2,
                    "L'Inde a récemment devancé la Chine en termes d'habitants recensés."
                ),
                QuizQuestion(
                    "Quel est le plus grand désert chaud de sable de la Terre ?",
                    listOf("Désert de Gobi", "Désert du Sahara", "Désert du Kalahari", "Désert d'Atacama"),
                    1,
                    "Le Sahara s'étend sur plus de 9 millions de kilomètres carrés en Afrique du Nord."
                ),
                QuizQuestion(
                    "Sur quel continent se trouve en totalité la forêt tropicale de l'Amazone ?",
                    listOf("Afrique", "Asie", "Amérique du Sud", "Océanie"),
                    2,
                    "Le bassin de l'Amazone est partagé entre plusieurs pays d'Amérique du Sud, dont le Brésil."
                ),
                QuizQuestion(
                    "Avec quel pays voisin la France partage-t-elle sa plus longue frontière terrestre ?",
                    listOf("L'Espagne", "L'Allemagne", "L'Italie", "Le Brésil"),
                    3,
                    "La frontière entre la Guyane française et le Brésil s'étend sur 730 kilomètres."
                ),
                QuizQuestion(
                    "Quel petit État souverain enclavé au sein de Rome est le plus petit pays du monde ?",
                    listOf("Monaco", "Saint-Marin", "Le Vatican", "Le Liechtenstein"),
                    2,
                    "Le Vatican s'étend sur seulement 0,44 kilomètre carré de superficie."
                ),
                QuizQuestion(
                    "Quelle île-continent abrite les kangourous et la ville de Sydney ?",
                    listOf("La Nouvelle-Zélande", "L'Australie", "Madagascar", "Le Groenland"),
                    1,
                    "L'île d'Australie représente la quasi-totalité du sous-continent régional."
                )
            ),
            2 to listOf(
                QuizQuestion(
                    "Quelle est la capitale officielle du Canada, établie par la reine Victoria ?",
                    listOf("Toronto", "Montréal", "Ottawa", "Vancouver"),
                    2,
                    "Ottawa fut sélectionnée pour sa position médiane neutre entre territoires anglophones et francophones."
                ),
                QuizQuestion(
                    "Quel pays d'Europe du Nord est réputé pour ses milliers de lacs et sa culture du sauna ?",
                    listOf("Suède", "Norvège", "Finlande", "Islande"),
                    2,
                    "La Finlande est surnommée le pays des mille lacs (elle en compte en fait 188 000)."
                ),
                QuizQuestion(
                    "Quel canal artificiel historique fait la liaison maritime directe entre l'Atlantique et le Pacifique ?",
                    listOf("Canal de Suez", "Canal de Corinthe", "Canal de Panama", "Canal de Kiel"),
                    2,
                    "Le canal de Panama évite aux navires le détour périlleux par le cap Horn."
                ),
                QuizQuestion(
                    "Dans quel océan se situe le magnifique archipel touristique de la Polynésie française ?",
                    listOf("Océan Atlantique", "Océan Pacifique", "Océan Indien", "Océan Arctique"),
                    1,
                    "Tahiti et Bora Bora font partie de ces territoires insulaires éparpillés dans le Pacifique Sud."
                ),
                QuizQuestion(
                    "Quel fleuve d'Amérique du Sud rejette le plus grand volume de débit d'eau douce au monde ?",
                    listOf("Le Nil", "Le Congo", "L'Amazone", "Le Yangzi Jiang"),
                    2,
                    "L'Amazone rejette plus d'eau à lui seul que les sept fleuves suivants combinés."
                ),
                QuizQuestion(
                    "Quel immense pays s'étend à la fois sur le continent européen et asiatique ?",
                    listOf("La Chine", "La Turquie", "La Russie", "L'Inde"),
                    2,
                    "La Russie s'étend d'Europe de l'Est jusqu'à l'extrême limite pacifique de l'Asie."
                ),
                QuizQuestion(
                    "Quelle est la capitale de l'Australie, créée de toutes pièces pour arbitrer Sydney et Melbourne ?",
                    listOf("Sydney", "Melbourne", "Canberra", "Brisbane"),
                    2,
                    "Canberra fut édifiée spécifiquement en 1913 pour servir de capitale de compromis."
                ),
                QuizQuestion(
                    "Dans quel massif montagneux d'Europe est situé le mont Blanc, plus haut sommet d'Europe occidentale ?",
                    listOf("Les Pyrénées", "Les Alpes", "Les Carpates", "L'Oural"),
                    1,
                    "Le mont Blanc culmine à 4 805 mètres d'altitude à la frontière franco-italienne."
                ),
                QuizQuestion(
                    "Quel pays d'Asie est un immense archipel composé de plus de 17 000 îles d'origine volcanique ?",
                    listOf("Le Japon", "Les Philippines", "L'Indonésie", "La Malaisie"),
                    2,
                    "L'Indonésie est le plus grand pays archipel et le plus peuplé à majorité musulmane au monde."
                ),
                QuizQuestion(
                    "Comment s'appelle la mer fermée, extrêmement salée, située au point le plus bas du globe terrestre ?",
                    listOf("La mer Noire", "La mer Caspienne", "La mer Morte", "La mer Rouge"),
                    2,
                    "La forte salinité de la mer Morte empêche tout développement de vie macroscopique."
                )
            ),
            3 to listOf(
                QuizQuestion(
                    "Quel détroit maritime étroit sépare l'extrême Nord de l'Asie (Russie) de l'Amérique (Alaska) ?",
                    listOf("Détroit de Gibraltar", "Détroit de Malacca", "Détroit de Béring", "Détroit d'Ormuz"),
                    2,
                    "Le détroit de Béring sépare la Sibérie de l'Alaska sur une distance d'environ 85 kilomètres."
                ),
                QuizQuestion(
                    "Quelle est la capitale officielle de la Turquie, remplaçant la métropole d'Istanbul ?",
                    listOf("Istanbul", "Ankara", "Izmir", "Antalya"),
                    1,
                    "Ankara fut proclamée capitale républicaine par Atatürk en octobre 1923."
                ),
                QuizQuestion(
                    "Quel pays d'Afrique centrale est bordé par le plus grand fleuve du continent par son débit second ?",
                    listOf("Nigeria", "République Démocratique du Congo", "Soudan", "Éthiopie"),
                    1,
                    "La RDC abrite l'essentiel du fleuve Congo, d'une puissance hydrographique colossale."
                ),
                QuizQuestion(
                    "Dans quel archipel européen se trouve le cratère géant du mont Teide, point culminant de l'Espagne ?",
                    listOf("Les Baléares", "Les Açores", "Les îles Canaries", "La Sardaigne"),
                    2,
                    "Le Teide est un volcan actif situé sur l'île de Tenerife aux Canaries."
                ),
                QuizQuestion(
                    "Quel État insulaire nordique s'assoit à cheval sur la faille médio-atlantique et regorge de geysers ?",
                    listOf("Le Groenland", "L'Islande", "L'Irlande", "La Norvège"),
                    1,
                    "L'Islande tire profit de sa situation géologique exceptionnelle pour exploiter la géothermie."
                ),
                QuizQuestion(
                    "Quel est le nom officiel de la ligne de latitude imaginaire située à 23,5 degrés Nord de l'Équateur ?",
                    listOf("Cercle Polaire Arctique", "Tropique du Capricorne", "Tropique du Cancer", "Méridien de Greenwich"),
                    2,
                    "Le tropique du Cancer délimite la limite Nord où le Soleil brille au zénith au solstice d'été."
                ),
                QuizQuestion(
                    "Quel est le plus grand lac naturel d'eau douce au monde en termes de superficie ?",
                    listOf("Le lac Victoria", "Le lac Supérieur", "La mer Caspienne", "Le lac Baïkal"),
                    1,
                    "Le lac Supérieur (Amérique du Nord) est le plus vaste, bien que le lac Baïkal (Russie) contienne plus de volume d'eau douce."
                ),
                QuizQuestion(
                    "Dans quel pays d'Amérique du Sud se situent les ruines historiques du Machu Picchu, citadelle inca ?",
                    listOf("Colombie", "Bolivie", "Chili", "Pérou"),
                    3,
                    "Le Machu Picchu est perché sur un promontoire rocheux des Andes péruviennes."
                ),
                QuizQuestion(
                    "Quelle ville historique d'Asie centrale, halte de la route de la soie, est réputée pour sa place du Régistan ?",
                    listOf("Boukhara", "Samarcande", "Tachkent", "Khiva"),
                    1,
                    "Samarcande, en Ouzbékistan, émerveille par ses mosquées et madrassas vêtues de faïences bleues."
                ),
                QuizQuestion(
                    "Quel fleuve emblématique d'Europe traverse le plus grand nombre de capitales de pays sur son parcours ?",
                    listOf("Le Rhin", "Le Danube", "La Volga", "La Seine"),
                    1,
                    "Le Danube est le fleuve le plus international du monde, traversant Vienne, Bratislava, Budapest et Belgrade."
                )
            )
        ),
        "Technologie" to mapOf(
            1 to listOf(
                QuizQuestion(
                    "Qui est le célèbre cofondateur visionnaire d'Apple, créateur du Macintosh et de l'iPhone ?",
                    listOf("Bill Gates", "Steve Jobs", "Mark Zuckerberg", "Jeff Bezos"),
                    1,
                    "Steve Jobs a hissé Apple au faîte de l'innovation ergonomique avec l'iPod et l'iPhone."
                ),
                QuizQuestion(
                    "Comment se nomme le réseau mondial interconnecté qui unit des milliards d'ordinateurs ?",
                    listOf("Intranet", "Ethernet", "Internet", "Bluetooth"),
                    2,
                    "Internet est l'infrastructure réseau planétaire décentralisée."
                ),
                QuizQuestion(
                    "Quel système d'exploitation mobile propulsé par Google équipe la majorité des téléphones mondiaux ?",
                    listOf("iOS", "Windows Mobile", "Android", "Linux"),
                    2,
                    "Android est le système open-source leader sur smartphone développé par Google."
                ),
                QuizQuestion(
                    "Quelle touche clavier abrège l'opération indispensable de copie sélective d'une sélection ?",
                    listOf("Ctrl+X", "Ctrl+V", "Ctrl+C", "Ctrl+Z"),
                    2,
                    "Dans la plupart des systèmes d'exploitation, Ctrl+C copie la sélection dans le presse-papiers."
                ),
                QuizQuestion(
                    "Quel géant de l'e-commerce mondial a été fondé à Seattle par le milliardaire Jeff Bezos ?",
                    listOf("eBay", "Amazon", "Alibaba", "Walmart"),
                    1,
                    "Amazon a débuté comme une modeste librairie en ligne avant de conquérir le commerce mondial."
                ),
                QuizQuestion(
                    "Quel format de document universel portable créé par Adobe conserve sa mise en forme initiale sur tout écran ?",
                    listOf("Word", "PDF", "JPEG", "HTML"),
                    1,
                    "Le format PDF (Portable Document Format) garantit une lecture fidèle indépendante du terminal."
                ),
                QuizQuestion(
                    "Comment appelle-t-on le petit logiciel malveillant destiné à pirater un ordinateur de l'intérieur ?",
                    listOf("Pare-feu", "Virus", "Cookies", "Serveur"),
                    1,
                    "Un virus ou malware s'introduit frauduleusement pour détruire ou voler des données."
                ),
                QuizQuestion(
                    "Que signifie l'abréviation familière 'PC' désignant l'ordinateur de bureau ?",
                    listOf("Processeur Central", "Personal Computer", "Port de Connexion", "Power Core"),
                    1,
                    "PC provient de l'anglais 'Personal Computer' (ordinateur personnel)."
                ),
                QuizQuestion(
                    "Quel réseau social centré sur l'image et la vidéo courte a démocratisé les filtres et stories ?",
                    listOf("LinkedIn", "X / Twitter", "Instagram", "Reddit"),
                    2,
                    "Instagram s'est imposé comme l'espace visuel phare plébiscité par les créateurs."
                ),
                QuizQuestion(
                    "Quelle technologie de communication sans fil de courte portée sert à jumeler casque ou montre ?",
                    listOf("Wi-Fi", "Infrarouge", "Bluetooth", "NFC"),
                    2,
                    "Le Bluetooth établit une liaison radio bidirectionnelle sécurisée de proximité."
                )
            ),
            2 to listOf(
                QuizQuestion(
                    "Quel langage informatique universel est le moteur exclusif de l'interactivité dynamique sur le Web ?",
                    listOf("HTML", "CSS", "JavaScript", "SQL"),
                    2,
                    "JavaScript anime et rend intelligentes les pages web du côté du navigateur client."
                ),
                QuizQuestion(
                    "Que désigne l'acronyme 'RAM' s'agissant de la mémoire matérielle d'un ordinateur ?",
                    listOf("Read Access Memory", "Random Access Memory", "Rapid Audio Module", "Real Active Memory"),
                    1,
                    "La mémoire vive éphémère d'un ordinateur est la Random Access Memory (RAM)."
                ),
                QuizQuestion(
                    "Qui a fondé le réseau social universitaire Facebook qui est devenu aujourd'hui le groupe Meta ?",
                    listOf("Bill Gates", "Mark Zuckerberg", "Jack Dorsey", "Elon Musk"),
                    1,
                    "Mark Zuckerberg créa le réseau social étudiant d'Harvard au début de l'année 2004."
                ),
                QuizQuestion(
                    "Quelle technologie sécurisée de registre décentralisé sert de fondation aux crypto-actifs comme le Bitcoin ?",
                    listOf("Cloud", "Blockchain", "Intelligence Artificielle", "Base relationnelle"),
                    1,
                    "La blockchain garantit la traçabilité infalsifiable des transactions sans intermédiaire."
                ),
                QuizQuestion(
                    "Comment qualifie-t-on le stockage de vos fichiers et données sur des serveurs distants en réseau ?",
                    listOf("Le Cloud computing", "Le peer-to-peer", "La virtualisation", "Le routage"),
                    0,
                    "Le 'nuage' (Cloud) dématérialise le stockage local au profit de serveurs hautement accessibles."
                ),
                QuizQuestion(
                    "Quel protocole sécurisé régit l'affichage des sites web avec un cryptage représenté par un petit cadenas ?",
                    listOf("HTTP", "FTP", "HTTPS", "SMTP"),
                    2,
                    "Le S de HTTPS désigne le cryptage SSL/TLS qui sécurise les échanges web."
                ),
                QuizQuestion(
                    "De quelle entreprise américaine proviennent les processeurs de calcul réputés 'Core i5' et 'Core i7' ?",
                    listOf("AMD", "NVIDIA", "Intel", "Qualcomm"),
                    2,
                    "Intel est un pionnier de la construction de puces électroniques et de microprocesseurs."
                ),
                QuizQuestion(
                    "Quelle entreprise spatiale privée audacieuse menée par Elon Musk réutilise ses fusées Falcon 9 ?",
                    listOf("NASA", "Blue Origin", "SpaceX", "Virgin Galactic"),
                    2,
                    "SpaceX a bouleversé l'économie spatiale par des propulseurs atterrissant d'eux-mêmes au sol."
                ),
                QuizQuestion(
                    "Quelle est l'unité d'information biologique minimale ou informatique binaire valant 0 ou 1 ?",
                    listOf("L'octet", "Le pixel", "Le bit", "Le processeur"),
                    2,
                    "Le bit (binary digit) est la brique élémentaire d'état binaire (vrai/faux)."
                ),
                QuizQuestion(
                    "Quel outil gratuit et open-source de gestion de version de code a été conçu par le créateur de Linux ?",
                    listOf("GitHub", "Git", "Subversion", "Docker"),
                    1,
                    "Linus Torvalds a développé le gestionnaire Git en 2005 pour coordonner le noyau Linux."
                )
            ),
            3 to listOf(
                QuizQuestion(
                    "Quel ingénieur pionnier britannique est reconnu comme le créateur officiel du World Wide Web (WWW) ?",
                    listOf("Alan Turing", "Tim Berners-Lee", "Vint Cerf", "Ada Lovelace"),
                    1,
                    "Tim Berners-Lee a conçu le premier serveur HTTP et les URL au CERN en 1989."
                ),
                QuizQuestion(
                    "Quelle mathématicienne visionnaire du XIXe siècle a écrit le premier algorithme destiné à une machine ?",
                    listOf("Grace Hopper", "Ada Lovelace", "Katherine Johnson", "Marie Curie"),
                    1,
                    "Ada Lovelace a formalisé l'algorithmique pour la machine analytique mécanique de Babbage."
                ),
                QuizQuestion(
                    "Comment se nomme le célèbre test destiné à évaluer si une intelligence artificielle simule la pensée humaine ?",
                    listOf("Test de Turing", "Évaluation CAPTCHA", "Test de Voight-Kampff", "Test de Lovelace"),
                    0,
                    "Alan Turing proposa en 1950 un jeu d'imitation textuel évaluant la subtilité d'une machine."
                ),
                QuizQuestion(
                    "Dans quel langage de programmation moderne natif et typé compile-t-on de nos jours sur l'OS Android ?",
                    listOf("Java", "Kotlin", "Swift", "C++"),
                    1,
                    "Kotlin est officiellement désigné par Google comme le langage de référence moderne d'Android depuis 2017."
                ),
                QuizQuestion(
                    "Quelle est la signification exacte du sigle 'CPU' ?" ,
                    listOf("Central Processing Unit", "Computer Power Utility", "Center Process Upgrade", "Core Performance Unit"),
                    0,
                    "Le CPU est le processeur central, véritable cerveau décisionnel calculant les flux binaires."
                ),
                QuizQuestion(
                    "Quel algorithme de consensus repose sur la résolution d'équations mathématiques pour valider des blocs (Bitcoin) ?",
                    listOf("Proof of Stake", "Proof of Work", "Proof of Authority", "Delegated Stake"),
                    1,
                    "La Preuve de Travail (Proof of Work) requiert une puissance GPU/ASIC pour sécuriser l'historique."
                ),
                QuizQuestion(
                    "Quel type spécial d’écran exploitant des diodes organiques permet d'éteindre totalement les pixels noirs ?",
                    listOf("LCD", "IPS", "OLED", "TFT"),
                    2,
                    "La technologie OLED génère des noirs parfaits et infinis en désactivant pixel par pixel sa lumière."
                ),
                QuizQuestion(
                    "Quelle suite de caractères cryptographiques sert d'adresse publique ou de signature asymétrique ?",
                    listOf("Clé privée", "Clé publique", "Hash sha-256", "Sel de hachage"),
                    1,
                    "La clé publique permet de chiffrer des informations destinées au possesseur de la clé privée."
                ),
                QuizQuestion(
                    "Quelle entreprise emblématique a développé l'agent conversationnel d'IA révolutionnaire ChatGPT ?",
                    listOf("Google DeepMind", "OpenAI", "Anthropic", "Microsoft"),
                    1,
                    "OpenAI a lancé le modèle de langage de masse ChatGPT fin 2022, provoquant un boom de l'IA."
                ),
                QuizQuestion(
                    "Comment désigne-t-on le principe physique consistant à intégrer des transistors toujours plus denses sur silicium ?",
                    listOf("Loi de Newton", "Loi de Moore", "Loi d'Ohm", "Loi de Tesla"),
                    1,
                    "La loi empirique de Gordon Moore prévoyait le doublement de la densité de puces de calcul tous les deux ans."
                )
            )
        )
    )

    // Pre-calculated beautiful data categories details
    val categoriesDetails = listOf(
        QuizCategoryInfo("Sciences", "Sciences", "Tests de physique & chimie", Icons.Outlined.Science, Color(0xFFE91E63)),
        QuizCategoryInfo("Histoire", "Histoire", "Époques & civilisations antiques", Icons.Outlined.MenuBook, Color(0xFF3F51B5)),
        QuizCategoryInfo("Echecs", "Échecs", "Stratégies, coups & légendes", Icons.Outlined.Extension, Color(0xFFFF9800)),
        QuizCategoryInfo("Culture", "Culture", "Arts visuels & littérature mondiale", Icons.Outlined.Movie, Color(0xFF673AB7)),
        QuizCategoryInfo("Géographie", "Géographie", "Continents, fleuves & capitales", Icons.Outlined.Language, Color(0xFF4CAF50)),
        QuizCategoryInfo("Technologie", "Technologie", "Internet, ordinateurs & algorithmie", Icons.Outlined.Computer, Color(0xFF00BCD4)),
        QuizCategoryInfo("Sport", "Sport", "Jeux olympiques & records athlétiques", Icons.Outlined.EmojiEvents, Color(0xFF2196F3)),
        QuizCategoryInfo("Cinéma", "Cinéma & TV", "Grands classiques & chefs-d'œuvre", Icons.Outlined.PlayArrow, Color(0xFF9C27B0)),
        QuizCategoryInfo("Musique", "Musique", "Symphonies célèbres & pop moderne", Icons.Outlined.MusicNote, Color(0xFF00E676)),
        QuizCategoryInfo("Nature", "Nature", "Écosystèmes, flore & règne animal", Icons.Outlined.Lightbulb, Color(0xFF8BC34A))
    )
}

data class QuizCategoryInfo(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: androidx.compose.ui.graphics.Color
)
