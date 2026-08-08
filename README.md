# 🤝 CordFriends v1.1.6 - Système d'Amis pour BungeeCord & Velocity

Transformez votre réseau Minecraft avec un système d'amis complet, des messages privés inter-serveurs et une interface graphique intuitive ! 🚀

**Support complet de Velocity !** Le plugin fonctionne parfaitement avec les deux principaux proxies modernes : BungeeCord et Velocity.

---

## ✨ Nouveautés de la version 1.1.6

### 🖼️ Système de Skins Amélioré v2
- **API Paper corrigée** : Mise à jour vers la dernière API Paper pour une meilleure compatibilité avec Minecraft 1.21+
- **Chargement asynchrone optimisé** : Les skins des amis sont récupérés directement depuis les serveurs de Mojang pour un affichage parfait
- **Gestion des UUID** : Support amélioré des UUID version 3 (hors-ligne) et version 4 (premium) pour une détection plus fiable

### 🔧 Améliorations Techniques
- Refactorisation du code pour une meilleure maintenabilité
- Meilleure gestion des erreurs lors des appels réseau
- Support de Minecraft 1.21

---

## 📦 Installation

L'installation est simple :


### 1️⃣ Côté BungeeCord
📂 Placez le plugin dans le dossier :


```
/plugins
```


Puis redémarrez votre proxy BungeeCord.


### 2️⃣ Côté Spigot / Paper
Installez également la version Spigot/Paper du plugin sur chaque serveur de votre réseau.


Après l'installation, redémarrez chaque serveur afin que toutes les fonctionnalités soient opérationnelles.


⚠️ Les deux parties (BungeeCord + Spigot/Paper) sont obligatoires pour un fonctionnement complet.


## ✅ Compatibilité

🖥️ Compatible avec ChestCommands


Pour ouvrir directement l'interface graphique, utilisez la commande :


```
op: execute as @s run friend gui
```


## 👥 Système d'amis

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


## 🚫 Système de blocage

Empêchez certains joueurs de vous contacter :


```
/friend block
/friend unblock
/friend blocked
```

Un joueur bloqué ne pourra plus :


❌ Vous envoyer une demande d'ami
❌ Vous envoyer un message privé
❌ Vous envoyer un courrier


## 💬 Messages privés inter-serveurs

Communiquez avec n'importe quel joueur du réseau grâce aux commandes :


```
/msg
/r
```

⚡ Les messages sont envoyés instantanément, même si le joueur se trouve sur un autre serveur.


📬 Si le destinataire est hors ligne, le message est automatiquement enregistré en courrier.


## ✉️ Courrier hors-ligne

Envoyez des messages même lorsqu'un joueur est déconnecté :


```
/mail send
/mail read
/mail clear
```

Par défaut, tous les joueurs peuvent envoyer un courrier, qu'ils soient amis ou non.


⚙️ Ce comportement est entièrement configurable.


## 🎯 Auto-complétion intelligente

Toutes les commandes disposent d'une auto-complétion (TAB) :


✅ Sous-commandes
✅ Noms des joueurs concernés


Une utilisation beaucoup plus rapide et agréable.


## 🖥️ Interface graphique

Ouvrez le menu avec :


```
/friend gui
```

Depuis cette interface, vous pouvez :


👥 Voir votre liste d'amis
🎮 Rejoindre un ami en un clic
💬 Lui envoyer un message privé directement


## 🌐 Fonctionnement sur tout le réseau

Toute la logique du plugin est gérée directement par BungeeCord, qui centralise les informations de l'ensemble de votre réseau.


Cela permet de profiter de toutes les fonctionnalités entre tous vos serveurs, de manière totalement transparente.


✨ Une seule installation sur le proxy suffit pour synchroniser les amis, les messages privés et le courrier sur l'ensemble du réseau.


## 📥 Téléchargement

Téléchargez les fichiers JAR depuis la page des [releases](https://github.com/herocraftlol/CityFriend/releases).

Versions disponibles :
- `CordFriends-Velocity-1.1.6.jar` pour Velocity
- `CordFriends-Bungee-1.1.6.jar` pour BungeeCord
- `CordFriends-Spigot-1.1.6.jar` pour Spigot/Paper

## 🛠️ Compilation

```bash
# Compiler tous les modules
mvn clean package

# Compiler un module spécifique
cd crossfriends-velocity && mvn clean package
cd crossfriends-bungee && mvn clean package
cd crossfriends-spigot && mvn clean package
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
| Skins des joueurs | ✅ | ✅ |
