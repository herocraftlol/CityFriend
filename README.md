# 🤝 CordFriends v1.1.8 — Système d'Amis pour BungeeCord & Velocity

Transformez votre réseau Minecraft avec un système d'amis complet, des messages privés inter-serveurs, un courrier hors-ligne et une interface graphique intuitive — le tout synchronisé sur l'ensemble de votre réseau ! 🚀

CordFriends s'installe sur votre proxy (**BungeeCord** ou **Velocity**) et s'accompagne d'un petit module **Spigot/Paper** à placer sur chaque serveur backend pour afficher le menu d'amis en jeu. Une seule configuration centralisée suffit pour profiter de toutes les fonctionnalités entre tous vos serveurs.

---

## ✨ Nouveautés de la version 1.1.8 — Skins v5 + SkinsRestorer

Cette version apporte une refonte complète de l'affichage des skins dans le menu d'amis, pour des têtes de joueurs toujours à jour, même hors-ligne.

### 🖼️ Intégration SkinsRestorer
- **Pont natif vers [SkinsRestorer](https://github.com/SkinsRestorer/SkinsRestorer)** : les skins définis via `/skin` sont maintenant récupérés automatiquement, même pour des comptes hors-ligne / crackés qui n'ont pas d'équivalent premium.
- **Fonctionnement hors-ligne** : contrairement à une simple recherche Mojang, SkinsRestorer garde sa propre base de skins par UUID/pseudo, interrogeable à tout moment — y compris quand l'ami est déconnecté.
- **Intégration optionnelle et sûre** : SkinsRestorer est déclaré en `softdepend`. S'il n'est pas installé (ou si sa version diffère), le menu d'amis continue de fonctionner normalement, l'integration est simplement désactivée.

### ⚡ Cache des skins par le proxy
- **Capture à la connexion** : le proxy (Velocity) enregistre la propriété `textures` du GameProfile de chaque joueur dès sa connexion. Ces skins sont ensuite réutilisés instantanément dans le menu, sans aucun appel réseau.
- **Affichage immédiat** : les amis dont le skin est déjà en cache apparaissent avec la bonne tête dès l'ouverture du menu.

### 🔍 Recherche intelligente (fallback en arrière-plan)
Pour les amis sans skin en cache, le module Spigot/Paper tente, en arrière-plan et sans bloquer le serveur :
1. **SkinsRestorer** en priorité (comptes hors-ligne inclus) ;
2. puis **Mojang** par UUID (compte premium) ou par pseudo (compte hors-ligne généré localement).

### 🔧 Corrections techniques
- Correction de l'API de profil Paper (`com.destroystokyo.paper.profile`) pour Minecraft 1.21+.
- Correction du logger Velocity (SLF4J) lors de la capture des skins.
- Protocole de plugin-messaging étendu pour transmettre les skins du proxy vers le menu Spigot.

---

## 📦 Contenu de la release

| Fichier | Rôle |
|---------|------|
| `CordFriends-Velocity-1.1.8.jar` | Plugin proxy pour **Velocity** |
| `CordFriends-Bungee-1.1.8.jar` | Plugin proxy pour **BungeeCord** |
| `CordFriends-Spigot-1.1.8.jar` | Module interface graphique pour **Spigot/Paper** (à installer sur chaque serveur backend) |
| `CordFriends-v1.1.8-src.tar.gz` | Code source complet de la v1.1.8 |

> ⚠️ Les deux parties (**Proxy + Spigot/Paper**) sont obligatoires pour un fonctionnement complet.

---

## 📥 Installation

### 🔄 Pour Velocity
1️⃣ Placez `CordFriends-Velocity-1.1.8.jar` dans le dossier `/plugins` de Velocity.
2️⃣ Installez `CordFriends-Spigot-1.1.8.jar` sur **chaque** serveur backend.
3️⃣ *(Optionnel)* Ajoutez **SkinsRestorer** sur vos serveurs backend pour activer l'affichage des skins personnalisés.
4️⃣ Redémarrez Velocity et tous vos serveurs.

### 🟢 Pour BungeeCord
1️⃣ Placez `CordFriends-Bungee-1.1.8.jar` dans le dossier `/plugins` de BungeeCord.
2️⃣ Installez `CordFriends-Spigot-1.1.8.jar` sur **chaque** serveur backend.
3️⃣ *(Optionnel)* Ajoutez **SkinsRestorer** sur vos serveurs backend.
4️⃣ Redémarrez le proxy et tous vos serveurs.

---

## 👥 Système d'Amis

```
/friend add <joueur>
/friend accept <joueur>
/friend deny <joueur>
/friend remove <joueur>
/friend list
/friend requests
```

✨ Lors d'une demande d'ami, des boutons cliquables permettent d'accepter ou de refuser instantanément.

## 🚫 Système de Blocage

```
/friend block <joueur>
/friend unblock <joueur>
/friend blocked
```

Un joueur bloqué ne peut plus : vous envoyer une demande d'ami, vous envoyer un message privé, ni vous envoyer du courrier.

## 💬 Messages Privés Inter-Serveurs

```
/msg <joueur> <message>
/r <message>
```

⚡ Les messages sont envoyés instantanément, même si le joueur se trouve sur un autre serveur. Si le destinataire est hors-ligne, le message est automatiquement enregistré en courrier.

## ✉️ Courrier Hors-Ligne

```
/mail send <joueur> <message>
/mail read
/mail clear
```

⚙️ Par défaut, tous les joueurs peuvent envoyer du courrier (amis ou non). Ce comportement est configurable.

## 🖥️ Interface Graphique

```
/friend gui
```

Depuis le menu, vous pouvez : voir votre liste d'amis (avec leurs skins), rejoindre un ami en un clic (clic gauche) et préparer un message privé (clic droit).

## 🎯 Auto-Complétion Intelligente

Toutes les commandes supportent la touche **TAB** : sous-commandes et noms des joueurs concernés.

---

## ⚙️ Configuration

Le fichier `config.yml` (généré sur le proxy) permet de régler :

| Option | Description | Défaut |
|--------|-------------|--------|
| `require-friendship` | Exiger d'être amis pour `/msg` et `/r` | `true` |
| `mail-require-friendship` | Exiger d'être amis pour `/mail send` | `false` |
| `notify-on-join` | Récapitulatif (demandes + courrier) à la connexion | `true` |

La permission `crossfriends.bypass` contourne la restriction d'amitié pour les messages privés.

---

## ✅ Compatibilité

🖥️ Compatible avec **ChestCommands** — pour ouvrir l'interface depuis un menu tiers :
```
op: execute as @s run friend gui
```

| Composant | BungeeCord | Velocity |
|-----------|:----------:|:--------:|
| Plugin proxy | ✅ | ✅ |
| Module Spigot/Paper | ✅ | ✅ |
| Messages privés | ✅ | ✅ |
| Système d'amis | ✅ | ✅ |
| Courrier hors-ligne | ✅ | ✅ |
| Interface graphique | ✅ | ✅ |
| Cache des skins | ✅ (via Spigot) | ✅ (proxy) |
| Intégration SkinsRestorer | ✅ | ✅ |

---

## 🌐 Fonctionnement sur Tout le Réseau

Toute la logique (amis, blocages, courrier, messages) est gérée par le proxy, qui centralise les informations de l'ensemble du réseau. Une seule installation suffit pour synchroniser les amis, les messages privés et le courrier sur tous vos serveurs, de manière totalement transparente.

---

## 🛠️ Compilation

```bash
# Compiler tous les modules
mvn clean package

# Compiler un module spécifique
cd crossfriends-velocity && mvn clean package
```

Les jars produits se trouvent dans `*/target/` :
- `crossfriends-bungee/target/CordFriends-Bungee-1.1.8.jar`
- `crossfriends-velocity/target/CordFriends-Velocity-1.1.8.jar`
- `crossfriends-spigot/target/CordFriends-Spigot-1.1.8.jar`

**Pré-requis** : JDK 21 et Maven 3.9+.

---

## 📥 Téléchargement

Téléchargez les fichiers JAR depuis la page des [releases](https://github.com/herocraftlol/CityFriend/releases).

---

## 📜 Licence

Ce plugin est fourni tel quel pour la communauté Minecraft. Consultez le dépôt pour les conditions d'utilisation.
