package com.cordfriends.spigot;

import java.util.UUID;

/**
 * Represente une ligne du menu d'amis, recue depuis le proxy BungeeCord/Velocity
 * via le canal de plugin-messaging "crossfriends:main".
 *
 * skinValue/skinSignature contiennent la propriete "textures" (skin) du joueur,
 * telle que capturee par le proxy a sa connexion (fonctionne pour les comptes
 * premium comme pour les comptes hors-ligne geres par un systeme de skins).
 * Elles sont vides si le proxy n'a jamais vu ce joueur ou n'a pas de texture pour lui.
 */
public record FriendEntry(UUID uuid, String name, boolean online, String server,
                           String skinValue, String skinSignature) {

    public boolean hasCachedSkin() {
        return skinValue != null && !skinValue.isEmpty();
    }
}
