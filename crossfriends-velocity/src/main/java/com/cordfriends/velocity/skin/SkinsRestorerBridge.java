package com.cordfriends.velocity.skin;

import com.velocitypowered.api.proxy.ProxyServer;
import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.SkinsRestorerProvider;
import net.skinsrestorer.api.property.SkinProperty;
import net.skinsrestorer.api.storage.PlayerStorage;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.UUID;

/**
 * Pont optionnel vers l'API de SkinsRestorer, interroge depuis le PROXY.
 *
 * Contrairement au module Spigot : quand SkinsRestorer tourne en "mode proxy"
 * (server.proxyMode.api active cote backend, cas le plus courant sur un reseau
 * multi-serveurs), la vraie base de donnees des skins vit ICI, sur le proxy - pas
 * sur les serveurs Paper. C'est donc le proxy qui doit interroger PlayerStorage ;
 * le module Spigot ne fait que recevoir le resultat deja calcule via le canal de
 * plugin-messaging "crossfriends:main" (voir FriendGuiOpener).
 *
 * Comme cote Spigot, isole dans sa propre classe et protege par try/catch
 * (Throwable inclus) pour ne jamais casser le plugin si SkinsRestorer est absent
 * ou incompatible avec l'API compilee ici (net.skinsrestorer:skinsrestorer-api:15.5.2).
 */
public final class SkinsRestorerBridge {

    private SkinsRestorerBridge() {
    }

    public record SkinData(String value, String signature) {
    }

    public static Optional<SkinData> lookup(ProxyServer server, UUID uuid, String name, Logger logger) {
        if (server.getPluginManager().getPlugin("skinsrestorer").isEmpty()) {
            return Optional.empty();
        }
        try {
            SkinsRestorer api = SkinsRestorerProvider.get();
            PlayerStorage playerStorage = api.getPlayerStorage();
            Optional<SkinProperty> property = playerStorage.getSkinForPlayer(uuid, name);
            if (property.isEmpty()) {
                return Optional.empty();
            }
            SkinProperty skinProperty = property.get();
            return Optional.of(new SkinData(skinProperty.getValue(), skinProperty.getSignature()));
        } catch (Throwable t) {
            logger.warn("Integration SkinsRestorer indisponible pour " + name
                    + " (" + t.getClass().getSimpleName() + " : " + t.getMessage()
                    + "). Verifiez que la version installee correspond a l'API compilee (15.5.2).");
            return Optional.empty();
        }
    }
}
