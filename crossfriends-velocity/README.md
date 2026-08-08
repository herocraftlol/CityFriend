# CordFriends-Velocity

Portage du module proxy de CordFriends (a l'origine ecrit pour BungeeCord) vers
[Velocity](https://papermc.io/software/velocity). Fonctionnellement identique a
`crossfriends-bungee` : amis, messages prives (`/msg`, `/r`), courrier
hors-ligne (`/mail`) et pont d'interface graphique avec le module Spigot
(`crossfriends-spigot`, inchange, tourne toujours sur les serveurs backend).

## Ce qui a change par rapport a la version BungeeCord

| BungeeCord | Velocity |
|---|---|
| `Plugin` (classe a etendre) + `plugin.yml` | Classe annotee `@Plugin(id=..., ...)`, pas de `plugin.yml` |
| `ProxiedPlayer` | `Player` |
| `getProxy()` | `ProxyServer` injecte via `@Inject` dans le constructeur |
| `Command` / `TabExecutor` | `SimpleCommand` (`execute`, `suggest`, `hasPermission`) |
| `net.md_5.bungee.api.ChatColor` / `TextComponent` | Adventure (`net.kyori.adventure.text.Component`, `NamedTextColor`, `ClickEvent`, `HoverEvent`) |
| `@EventHandler` + `Listener` | `@Subscribe` (les classes de listener n'ont plus besoin d'implementer une interface) |
| `PostLoginEvent`, `PlayerDisconnectEvent`, `ServerSwitchEvent` | `PostLoginEvent`, `DisconnectEvent`, `ServerConnectedEvent` (memes noms ou equivalents, package `com.velocitypowered.api.event.*`) |
| `getProxy().getScheduler().schedule(...)` | `server.getScheduler().buildTask(plugin, runnable).delay(...).repeat(...).schedule()` |
| `ConfigurationProvider` / `YamlConfiguration` | Velocity ne fournit pas d'API de config integree : `config.yml` est lu par un tres petit parseur maison (le fichier ne contient que 3 booleens, donc pas besoin d'ajouter SnakeYAML ou Configurate) |
| Canal de plugin-messaging enregistre via `getProxy().registerChannel(...)` | `server.getChannelRegistrar().register(MinecraftChannelIdentifier.from("crossfriends:main"))` |

Le nom du canal de plugin-messaging (`crossfriends:main`) reste identique, donc
**le module `crossfriends-spigot` n'a besoin d'aucune modification** : il
continue de fonctionner tel quel derriere un proxy Velocity.

## Compilation

```bash
mvn -pl crossfriends-velocity -am clean package
```

Le jar produit est `crossfriends-velocity/target/CordFriends-Velocity-1.1.8.jar`,
a placer dans le dossier `plugins/` de Velocity.

> **Note sur la version de `velocity-api`** : le pom utilise
> `com.velocitypowered:velocity-api:3.4.0-SNAPSHOT` (depot
> `https://repo.papermc.io/repository/maven-public/`), qui est la version de
> developpement courante publiee officiellement par PaperMC et utilisee par la
> quasi-totalite des plugins Velocity actuels. Verifiez si une version plus
> recente est disponible au moment de la compilation et mettez a jour le
> numero de version si besoin.

## Configuration

`config.yml` (genere automatiquement dans `plugins/CordFriends-Velocity/` au
premier lancement) est strictement identique a celui de la version BungeeCord :

```yaml
require-friendship: true
mail-require-friendship: false
notify-on-join: true
```
