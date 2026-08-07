# 🤝 CordFriends v1.1.3 - Système d'Amis pour BungeeCord & Velocity

Transformez votre réseau Minecraft avec un système d'amis complet, des messages privés inter-serveurs et une interface graphique intuitive ! 🚀

**Nouvelle version avec support complet de Velocity !** Le plugin fonctionne désormais parfaitement avec les deux principaux proxies modernes : BungeeCord et Velocity.


## 📦 Installation

L'installation est simple selon votre proxy :

### 🔄 Pour Velocity (NOUVEAU !)

1️⃣ Placez le plugin `CordFriends-Velocity-1.1.3.jar` dans le dossier :

```
/plugins
```

2️⃣ Installez également la version Spigot/Paper du plugin sur chaque serveur backend de votre réseau.

3️⃣ Redémarrez Velocity et tous vos serveurs.


### 🟢 Pour BungeeCord

1️⃣ Placez le plugin `CordFriends-Bungee-1.1.3.jar` dans le dossier :

```
/plugins
```

2️⃣ Installez également la version Spigot/Paper du plugin sur chaque serveur de votre réseau.

3️⃣ Redémarrez le proxy et tous vos serveurs.


⚠️ **Important** : Les deux parties (Proxy + Spigot/Paper) sont obligatoires pour un fonctionnement complet.


## ✨ Nouveautés de la version 1.1.3

- 🆕 **Support Velocity** - Le plugin est maintenant compatible avec Velocity, le proxy moderne soutenu par PaperMC
- 🔄 **Double compatibilité** - Un seul plugin pour les deux principaux proxies du marché
- 📦 **Fichiers séparés** - Téléchargez la version adaptée à votre proxy


## ✅ Compatibilité

🖥️ Compatible avec ChestCommands

Pour ouvrir directement l'interface graphique, utilisez la commande :

```
op: execute as @s run friend gui
```


## 👥 Système d'Amis

Gérez facilement votre liste d'amis avec :

```
/friend add
/friend accept
/friend deny
/friend remove
/friend list
/friend requests
```

✨ Lorsqu'un joueur vous envoie une demande d'ami, des boutons cliquables permettent d'accepter ou de refuser instantanément.


## 🚫 Système de Blocage

Empêchez certains joueurs de vous contacter :

```
/friend block
/friend unblock
/friend blocked
```

Un joueur bloqué ne pourra plus :

- ❌ Vous envoyer une demande d'ami
- ❌ Vous envoyer un message privé
- ❌ Vous envoyer un courrier


## 💬 Messages Privés Inter-Serveurs

Communiquez avec n'importe quel joueur du réseau grâce aux commandes :

```
/msg
/r
```

⚡ Les messages sont envoyés instantanément, même si le joueur se trouve sur un autre serveur.

📬 Si le destinataire est hors ligne, le message est automatiquement enregistré en courrier.


## ✉️ Courrier Hors-Ligne

Envoyez des messages même lorsqu'un joueur est déconnecté :

```
/mail send
/mail read
/mail clear
```

Par défaut, tous les joueurs peuvent envoyer un courrier, qu'ils soient amis ou non.

⚙️ Ce comportement est entièrement configurable.


## 🎯 Auto-Complétion Intelligente

Toutes les commandes disposent d'une auto-complétion (TAB) :

- ✅ Sous-commandes
- ✅ Noms des joueurs concernés

Une utilisation beaucoup plus rapide et agréable.


## 🖥️ Interface Graphique

Ouvrez le menu avec :

```
/friend gui
```

Depuis cette interface, vous pouvez :

- 👥 Voir votre liste d'amis
- 🎮 Rejoindre un ami en un clic
- 💬 Lui envoyer un message privé directement


## 🌐 Fonctionnement sur Tout le Réseau

Toute la logique du plugin est gérée directement par le proxy (BungeeCord ou Velocity), qui centralise les informations de l'ensemble de votre réseau.

Cela permet de profiter de toutes les fonctionnalités entre tous vos serveurs, de manière totalement transparente.

✨ Une seule installation sur le proxy suffit pour synchroniser les amis, les messages privés et le courrier sur l'ensemble du réseau.


## 📥 Téléchargement

Téléchargez les fichiers JAR depuis la page des [releases](https://github.com/herocraftlol/CityFriend/releases).

Choisissez la version adaptée à votre proxy :
- `CordFriends-Velocity-1.1.3.jar` pour Velocity
- `CordFriends-Bungee-1.1.3.jar` pour BungeeCord
- `CordFriends-Spigot-1.1.3.jar` pour vos serveurs Spigot/Paper


## 🛠️ Compilation

```bash
# Compiler tous les modules
mvn clean package

# Ou compiler un module spécifique
cd crossfriends-bungee && mvn clean package
cd ../crossfriends-velocity && mvn clean package
cd ../crossfriends-spigot && mvn clean package
```


## 📋 Tableau de Compatibilité

| Composant | BungeeCord | Velocity |
|-----------|------------|----------|
| Proxy plugin | ✅ | ✅ |
| Spigot/Paper plugin | ✅ | ✅ |
| Messages privés | ✅ | ✅ |
| Système d'amis | ✅ | ✅ |
| Courrier | ✅ | ✅ |
| Interface graphique | ✅ | ✅ |
