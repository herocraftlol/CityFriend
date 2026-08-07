package com.cordfriends.velocity.commands;

import com.cordfriends.velocity.CrossFriendsVelocityPlugin;
import com.cordfriends.velocity.data.DataManager;
import com.cordfriends.velocity.data.PlayerProfile;
import com.velocitypowered.api.proxy.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Petites fonctions partagees pour generer les suggestions de l'autocompletion (tab)
 * des differentes commandes (/friend, /msg, /mail...).
 */
final class TabCompleteUtil {

    private TabCompleteUtil() {
    }

    /** Suggere les pseudos des joueurs actuellement connectes sur le reseau, filtres par prefixe. */
    static List<String> onlinePlayerNames(CrossFriendsVelocityPlugin plugin, String partial) {
        String lower = partial.toLowerCase(Locale.ROOT);
        List<String> out = new ArrayList<>();
        for (Player p : plugin.getServer().getAllPlayers()) {
            if (p.getUsername().toLowerCase(Locale.ROOT).startsWith(lower)) {
                out.add(p.getUsername());
            }
        }
        return out;
    }

    /** Resout une collection d'UUID en pseudos connus, filtres par prefixe (utilise pour amis, demandes, etc.). */
    static List<String> namesFromUuids(DataManager dm, Collection<UUID> uuids, String partial) {
        String lower = partial.toLowerCase(Locale.ROOT);
        List<String> out = new ArrayList<>();
        for (UUID uuid : uuids) {
            PlayerProfile profile = dm.getProfile(uuid);
            String name = profile.getName();
            if (name != null && name.toLowerCase(Locale.ROOT).startsWith(lower)) {
                out.add(name);
            }
        }
        return out;
    }

    /** Filtre une liste fixe de mots-cles (sous-commandes) par prefixe. */
    static List<String> filterKeywords(Collection<String> keywords, String partial) {
        String lower = partial.toLowerCase(Locale.ROOT);
        List<String> out = new ArrayList<>();
        for (String keyword : keywords) {
            if (keyword.startsWith(lower)) {
                out.add(keyword);
            }
        }
        return out;
    }
}
