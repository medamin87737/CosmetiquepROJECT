# Cosmétique Shop (Android)



Application boutique cosmétique — Java, XML Views, ViewBinding, SQLite locale.



## Installation



1. Android Studio + SDK 36

2. `local.properties` : `sdk.dir=...`

3. Build debug : `.\gradlew.bat assembleDebug`

4. Installer l’APK : `app/build/outputs/apk/debug/app-debug.apk`



## Test sur téléphone (USB)



1. Sur le téléphone : **Paramètres → À propos** → appuyer 7 fois sur **Numéro de build** pour activer les options développeur.

2. **Options développeur** → activer **Débogage USB**.

3. Brancher le câble USB ; accepter l’empreinte RSA sur l’appareil si demandé.

4. Dans Android Studio : menu **Run** → choisir l’appareil → **Run 'app'** (ou `.\gradlew.bat installDebug` puis lancer l’icône).



### Base de données v4 (images démo)



Après mise à jour en **DB_VERSION 4**, les seeds incluent des images Unsplash (HTTPS). Si l’app était déjà installée : **désinstaller l’app** ou **Effacer les données** (Paramètres → Applications → Cosmétique Shop → Stockage) pour recréer la base avec les nouvelles images.



## Comptes démo



| Rôle | Email | Mot de passe | Notes |

|------|-------|--------------|-------|

| Admin | `admin@shop.tn` | `Admin@2026` | Connexion directe (pas de changement MDP forcé) |

| Client | `sarra.benali@email.tn` | `Client@2026` | |



## Vidéos (optionnel)



Placer des MP4 dans `app/src/main/res/raw/` :



- `video_hero.mp4` — bannière accueil

- `video_soin.mp4`, `video_maquillage.mp4`, `video_nouveautes.mp4` — sections



Si absent, une image héro beauté (Unsplash) s’affiche via Glide (`RawVideoHelper`).



## Architecture



```

com.esb.cosmetiqueshop

├── data/db, dao, repository

├── model

├── ui/ (splash, auth, publichome, client, product, checkout, order, admin, messages, notifications)

├── adapter

├── notifications

└── utils

```



- Écrans → repositories uniquement (pas de SQL dans l’UI)

- DB : `DatabaseHelper` + seed démo (v4)

- Session : `SessionManager` (SharedPreferences)

- Prix en millimes : `PriceUtils`



## Parcours



- **Splash** → public / client (`ClientMainActivity`) / admin (`AdminMainActivity`)

- **Client** : bottom nav Accueil, Boutique, Panier, Commandes, Profil

- **Admin** : drawer Dashboard, Produits, Catégories, Commandes, Livraisons, Clients, Messages, Exports CSV



## Tests unitaires



`.\gradlew.bat test` — `PriceUtilsTest`, `OrderStatusTest`
