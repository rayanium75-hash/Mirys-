# Mirys

Application Android de bien-être personnel, journal d'humeur et planificateur de tâches intelligent.

## Lancer localement

**Prérequis :** [Android Studio](https://developer.android.com/studio)

1. Ouvrir Android Studio
2. Sélectionner **Open** et choisir le dossier du projet
3. Créer un fichier `.env` à la racine avec la clé `AI_API_KEY`
4. Supprimer cette ligne dans `app/build.gradle.kts` : `signingConfig = signingConfigs.getByName("debugConfig")`
5. Lancer sur un émulateur ou appareil physique
