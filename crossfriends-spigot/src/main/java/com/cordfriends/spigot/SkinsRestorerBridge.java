package com.cordfriends.spigot;

import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.SkinsRestorerProvider;
import net.skinsrestorer.api.property.SkinProperty;
import net.skinsrestorer.api.storage.PlayerStorage;
import org.bukkit.Bukkit;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Pont optionnel vers l'API de SkinsRestorer.
 *
 * Interet par rapport a une recherche Mojang classique : SkinsRestorer garde sa
 * propre base de donnees de skins par UUID/pseudo (alimentee via /skin), qui est
 * interrogeable a tout moment - meme si le joueur n'est pas connecte et meme s'il
 * s'agit d'un compte hors-ligne/cracke sans equivalent premium. On n'a donc pas
 * besoin d'attendre qu'il se reconnecte pour retrouver son skin.
 *
 * Isole dans sa propre classe et protege par try/catch (Throwable inclus) pour que
 * le reste du plugin continue de fonctionner normalement si SkinsRestorer n'est pas
 * installe, ou si sa version presente une API incompatible avec celle utilisee ici
 * (net.skinsrestorer:skinsrestorer-api:15.5.2).
 */
final class SkinsRestorerBridge {

    private SkinsRestorerBridge() {
    }

    /**
     * Represente une texture de skin brute (valeur + signature), telle qu'attendue
     * par SkullMeta#setPlayerProfile / ProfileProperty cote Bukkit.
     */
    record SkinData(String value, String signature) {
    }

    /**
     * Tente de recuperer le skin connu de SkinsRestorer pour ce joueur.
     * Retourne Optional.empty() si SkinsRestorer est absent, n'a pas ce joueur en
     * base, ou si l'appel echoue pour une raison quelconque (on ne veut jamais
     * planter le menu d'amis a cause d'une integration optionnelle).
     */
    static Optional<SkinData> lookup(UUID uuid, String name, Logger logger) {
        if (Bukkit.getPluginManager().getPlugin("SkinsRestorer") == null) {
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
            logger.warning("Integration SkinsRestorer indisponible pour " + name
                    + " (" + t.getClass().getSimpleName() + " : " + t.getMessage()
                    + "). Verifiez que la version installee correspond a l'API compilee (15.5.2).");
            return Optional.empty();
        }
    }
}
