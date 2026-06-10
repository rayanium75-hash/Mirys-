# Guide de Configuration Codemagic pour Mirys 🚀

Ce document explique étape par étape comment configurer l'application **Mirys** sur la plateforme de build **Codemagic** pour générer automatiquement vos APK (pour tests) et vos AAB (pour Google Play) à partir de votre dépôt GitHub.

---

## 1. Structure du projet et préparation sur GitHub

Grâce à nos modifications, le projet contient déjà tous les éléments requis :
- **Gradle Wrapper** (`gradlew`, `gradlew.bat` et `gradle/wrapper/`) : Permet à Codemagic de compiler sans installer Gradle manuellement.
- **Config Codemagic** (`codemagic.yaml`) : Un pipeline de CI/CD pré-implémenté et configuré, qui gère :
  - La configuration automatique des variables d'environnement (dont votre clé d'API Gemini).
  - L'exécution de la suite de tests unitaires locaux.
  - La compilation de l'APK de Débogage (`debug.apk`).
  - L'assemblage de l'APK de Production signé ou non-signé et de l'Android App Bundle (`.aab`).
  - L'envoi automatique d'un rapport de build et des artéfacts par e-mail à `rayanium75@gmail.com`.

### Étape de publication :
Faites un push de ce dépôt complet sur votre compte **GitHub**.

---

## 2. Connexion à Codemagic

1. Allez sur le site officiel de [Codemagic](https://codemagic.io/).
2. Connectez-vous avec votre compte **GitHub**.
3. Sur votre tableau de bord, cliquez sur **"Add application"**.
4. Sélectionnez le dépôt de votre application **Mirys** (depuis GitHub) et choisissez le type **Android App**.
5. Cliquez sur **Finish**. Codemagic détectera automatiquement le fichier `codemagic.yaml` situé à la racine du projet !

---

## 3. Configuration des Variables de Chiffrement (Secrets) 🔐

Pour que l'IA fonctionne et que l'application puisse être signée, vous devez renseigner vos clés secrètes dans l'interface de Codemagic (onglet **Environment variables** sur l'application dans Codemagic) :

### A. Clé d'API Gemini (Mirys IA)
- **Name :** `GEMINI_API_KEY`
- **Value :** Votre clé d'API Gemini secrète (créée sur [Google AI Studio](https://aistudio.google.com/)).
- **Group :** `api_keys`

### B. Clé de Signature de Production (Optionnel, requis pour Google Play)
Si vous souhaitez générer une version de production signée, ajoutez ces 3 variables :
1. **`KEYSTORE_BASE64`** : Le contenu en base64 de votre keystore `.jks`.
   *(Pour l'obtenir sous Linux/macOS : `base64 mon-keystore.jks | pbcopy` ou sous Windows PowerShell : `[Convert]::ToBase64String([IO.File]::ReadAllBytes("mon-keystore.jks"))`)*
2. **`STORE_PASSWORD`** : Le mot de passe général de votre Keystore.
3. **`KEY_PASSWORD`** : Le mot de passe de la clé de signature spécifique (alias `upload`).
- **Group obligatoire de ces variables :** `keystore_credentials`

---

## 4. Lancement du premier Build ⚡

1. Dans les paramètres de l'application sur Codemagic, cliquez sur **"Start new build"** (Débuter un nouveau build).
2. Sélectionnez votre branche principale (ex: `main` ou `master`).
3. Sélectionnez le workflow **android-build** (Mirys Android Build Pipeline).
4. Cliquez sur **Start build**.

### Ce que fait le pipeline :
- Téléchargement du projet et des dépendances.
- Restauration de l'environnement Java 17.
- Écriture du fichier `.env` de production pour le plugin de secrets.
- Exécution des tests unitaires sanitaires du projet.
- Génération des artéfacts (fichiers `.apk` et `.aab`).
- Envoi automatique des artéfacts compilés prêts à installer directement sur votre e-mail `rayanium75@gmail.com` !

---

## 🌟 Support et Automatisation
Toute modification poussée (`git push`) sur votre branche déclenchera automatiquement un nouveau build, vous garantissant que votre code compile toujours sainement avant d'atteindre vos utilisateurs !
