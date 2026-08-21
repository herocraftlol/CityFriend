# 🤝 CordFriends v1.2.0 — Système d'Amis pour BungeeCord & Velocity

Transformez votre réseau Minecraft avec un système d'amis complet, des messages privés inter-serveurs, un courrier hors-ligne et une interface graphique intuitive — le tout synchronisé sur l'ensemble de votre réseau ! 🚀

CordFriends s'installe sur votre proxy (**BungeeCord** ou **Velocity**) et s'accompagne d'un petit module **Spigot/Paper** à placer sur chaque serveur backend pour afficher le menu d'amis en jeu. Une seule configuration centralisée suffit pour profiter de toutes les fonctionnalités entre tous vos serveurs.

> 🔎 **Ce plugin fait quoi ?** Il ajoute un vrai système social à votre réseau : liste d'amis persistante, demandes/blocages, messagerie privée instantanée entre serveurs, courrier hors-ligne, et un menu graphique en jeu (têtes de joueurs, skins, rejoindre un ami en un clic). Tout est piloté depuis le proxy, donc unifié sur l'ensemble du réseau.

---

## ✨ Nouveautés de la version 1.2.0 — « Rejoindre un ami » simplifié, sans téléportation

Cette version repense le bouton **« Rejoindre un ami »** du menu d'amis : fini la téléportation forcée aux coordonnées exactes de votre ami — rejoindre quelqu'un se comporte désormais exactement comme si vous rejoigniez son serveur/monde normalement. 🎯

### 🚶 Rejoindre un ami = changer de serveur, tout simplement
- **Plus de téléportation aux coordonnées de l'ami** : un clic gauche sur un ami vous transfère sur son serveur, et c'est tout. Vous arrivez comme un joueur normal qui se connecte à ce serveur — aucun déplacement automatique jusqu'à sa position, ni sur le même serveur ni sur un autre.
- **Déjà sur le même serveur ?** Au lieu de vous téléporter de force jusqu'à votre ami, le plugin vous informe simplement : *« Vous êtes déjà sur le même serveur que X. »* À vous de le retrouver comme bon vous semble !

### 🧹 Code allégé et plus fiable
- Suppression du message plugin `TELEPORT_TO` et de toute la mécanique de téléportation différée (file d'attente des téléportations, écouteurs de changement de serveur) côté **BungeeCord**, **Velocity** et **Spigot**.
- Moins de cas limites à gérer : plus de téléportation qui échoue si l'ami se déconnecte pendant le transfert, plus de délai artificiel d'une seconde après l'arrivée sur le serveur.

### 📦 Mis à jour également
- Numéro de version unifié à **1.2.0** (parent, modules Bungee/Velocity/Spigot, `plugin.yml`, descripteur `@Plugin`).

---

<details>
<summary>📜 Historique : version 1.1.9 — Skins v6 : SkinsRestorer côté Proxy</summary>

La version 1.1.9 a déplacé la recherche des skins **SkinsRestorer directement sur le proxy** (Velocity), là où vit réellement la base de données des skins en mode multi-serveurs : skins plus fiables, plus à jour, et récupérables même pour des amis **hors-ligne**.

- **Pont proxy** (`SkinsRestorerBridge` côté Velocity) : le proxy interroge lui-même `PlayerStorage` de SkinsRestorer.
- **Skins pour les amis hors-ligne** : `FriendGuiOpener` demande le skin à SkinsRestorer pour les amis déconnectés.
- **Priorité à la fraîcheur** : texture « live » pour un ami en ligne, SkinsRestorer pour un ami hors-ligne.
- SkinsRestorer déclaré en dépendance optionnelle dans le descripteur Velocity.
- Corrections : logs Spigot assainis en mode proxy, API de profil Paper 1.21+, logger Velocity SLF4J.

</details>

---

## 📦 Contenu de la release

| Fichier | Rôle |
|---------|------|
| `CordFriends-Velocity-1.2.0.jar` | Plugin proxy pour **Velocity** |
| `CordFriends-Bungee-1.2.0.jar` | Plugin proxy pour **BungeeCord** |
| `CordFriends-Spigot-1.2.0.jar` | Module interface graphique pour **Spigot/Paper** (à installer sur chaque serveur backend) |
| `CordFriends-v1.2.0-src.tar.gz` | Code source complet de la v1.2.0 |

> ⚠️ Les deux parties (**Proxy + Spigot/Paper**) sont obligatoires pour un fonctionnement complet.

---

## 📥 Installation

### 🔄 Pour Velocity
1️⃣ Placez `CordFriends-Velocity-1.2.0.jar` dans le dossier `/plugins` de Velocity.
2️⃣ Installez `CordFriends-Spigot-1.2.0.jar` sur **chaque** serveur backend.
3️⃣ *(Optionnel)* Ajoutez **SkinsRestorer** sur votre proxy (et activez `server.proxyMode.api` côté backend) pour activer l'affichage des skins personnalisés.
4️⃣ Redémarrez Velocity et tous vos serveurs.

### 🟢 Pour BungeeCord
1️⃣ Placez `CordFriends-Bungee-1.2.0.jar` dans le dossier `/plugins` de BungeeCord.
2️⃣ Installez `CordFriends-Spigot-1.2.0.jar` sur **chaque** serveur backend.
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

Depuis le menu, vous pouvez : voir votre liste d'amis (avec leurs skins), rejoindre le serveur d'un ami en un clic (clic gauche — simple transfert de serveur, sans téléportation forcée) et préparer un message privé (clic droit).

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
| Intégration SkinsRestorer | ✅ | ✅ (proxy, v6) |

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
- `crossfriends-bungee/target/CordFriends-Bungee-1.2.0.jar`
- `crossfriends-velocity/target/CordFriends-Velocity-1.2.0.jar`
- `crossfriends-spigot/target/CordFriends-Spigot-1.2.0.jar`

**Pré-requis** : JDK 21 et Maven 3.9+.

---

## 📥 Téléchargement

Téléchargez les fichiers JAR depuis la page des [releases](https://github.com/herocraftlol/CityFriend/releases).

---

## 📜 Licence

Ce plugin est fourni tel quel pour la communauté Minecraft. Consultez le dépôt pour les conditions d'utilisation.
