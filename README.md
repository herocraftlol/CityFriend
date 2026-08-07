# 🤝 CordFriends v1.1.5 - Système d'Amis pour BungeeCord & Velocity

Transformez votre réseau Minecraft avec un système d'amis complet, des messages privés inter-serveurs et une interface graphique intuitive ! 🚀

**Support complet de Velocity !** Le plugin fonctionne parfaitement avec les deux principaux proxies modernes : BungeeCord et Velocity.

---

## ✨ Nouveautés de la version 1.1.5

### 🖼️ Amélioration des skins de joueurs
- **Chargement asynchrone des skins** : Les skins des amis sont maintenant récupérés directement depuis les serveurs de Mojang, garantissant l'affichage des skins les plus récents
- **Interface graphique améliorée** : Les têtes de joueurs affichent maintenant le skin exact de chaque ami, en ligne ou hors-ligne
- **Performance optimisée** : Le chargement des skins s'effectue en arrière-plan sans impacter le serveur

### 🐛 Corrections de bugs
- Améliorations de stabilité et corrections générales
- Meilleure gestion des erreurs lors du chargement des skins

---

## 📦 Installation

L'installation est simple selon votre proxy :

### 🔄 Pour Velocity

1️⃣ Placez le plugin `CordFriends-Velocity-1.1.5.jar` dans le dossier :

```
/plugins
```

2️⃣ Installez également la version Spigot/Paper du plugin sur chaque serveur backend.

3️⃣ Redémarrez Velocity et tous vos serveurs.


### 🟢 Pour BungeeCord

1️⃣ Placez le plugin `CordFriends-Bungee-1.1.5.jar` dans le dossier :

```
/plugins
```

2️⃣ Installez également la version Spigot/Paper du plugin sur chaque serveur.

3️⃣ Redémarrez le proxy et tous vos serveurs.


⚠️ **Important** : Les deux parties (Proxy + Spigot/Paper) sont obligatoires.


## ✨ Nouveautés de la version 1.1.5

- 🐛 **Corrections de bugs** - Améliorations de stabilité et corrections
- 🔧 **Optimisations** - Performance accrue du système


## ✅ Compatibilité

🖥️ Compatible avec ChestCommands

Pour ouvrir directement l'interface graphique :

```
op: execute as @s run friend gui
```


## 👥 Système d'Amis

```
/friend add
/friend accept
/friend deny
/friend remove
/friend list
/friend requests
```


## 🚫 Système de Blocage

```
/friend block
/friend unblock
/friend blocked
```


## 💬 Messages Privés Inter-Serveurs

```
/msg
/r
```

⚡ Les messages sont envoyés instantanément sur tout le réseau.


## ✉️ Courrier Hors-Ligne

```
/mail send
/mail read
/mail clear
```


## 🎯 Auto-Complétion Intelligente

Toutes les commandes supportent TAB pour l'auto-complétion.


## 🖥️ Interface Graphique

```
/friend gui
```


## 🌐 Fonctionnement sur Tout le Réseau

Toute la logique est gérée par le proxy (BungeeCord ou Velocity).


## 📥 Téléchargement

Téléchargez les fichiers JAR depuis la page des [releases](https://github.com/herocraftlol/CityFriend/releases).

Versions disponibles :
- `CordFriends-Velocity-1.1.5.jar` pour Velocity
- `CordFriends-Bungee-1.1.5.jar` pour BungeeCord
- `CordFriends-Spigot-1.1.5.jar` pour Spigot/Paper


## 🛠️ Compilation

```bash
# Compiler tous les modules
mvn clean package

# Compiler un module spécifique
cd crossfriends-velocity && mvn clean package
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
