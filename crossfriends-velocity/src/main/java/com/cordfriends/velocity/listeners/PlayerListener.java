package com.cordfriends.velocity.listeners;

import com.cordfriends.velocity.CrossFriendsVelocityPlugin;
import com.cordfriends.velocity.data.DataManager;
import com.cordfriends.velocity.data.MailMessage;
import com.cordfriends.velocity.data.PlayerProfile;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.util.GameProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.concurrent.TimeUnit;

/**
 * PostLoginEvent se declenche une seule fois quand le joueur rejoint le PROXY
 * (et non a chaque changement de sous-serveur), ce qui en fait l'endroit ideal
 * pour charger son profil et le notifier de son courrier / ses demandes d'amis.
 */
public class PlayerListener {

    private final CrossFriendsVelocityPlugin plugin;

    public PlayerListener(CrossFriendsVelocityPlugin plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        Player player = event.getPlayer();
        DataManager dm = plugin.getDataManager();

        PlayerProfile profile = dm.getProfile(player.getUniqueId());
        profile.setName(player.getUsername());
        dm.registerName(player.getUniqueId(), player.getUsername());
        captureSkin(player, profile);

        if (!plugin.isNotifyOnJoin()) {
            return;
        }

        // Petit delai pour laisser le client finir de se connecter avant de lui envoyer un message
        plugin.getServer().getScheduler().buildTask(plugin, () -> {
            int unread = 0;
            for (MailMessage mail : profile.getMailbox()) {
                if (!mail.isRead()) {
                    unread++;
                }
            }
            int pendingRequests = profile.getIncomingRequests().size();

            if (unread > 0) {
                player.sendMessage(Component.text("Vous avez " + unread + " message(s) en attente. Tapez /mail read.", NamedTextColor.AQUA));
            }
            if (pendingRequests > 0) {
                player.sendMessage(Component.text("Vous avez " + pendingRequests + " demande(s) d'ami en attente. Tapez /friend requests.", NamedTextColor.GOLD));
            }
        }).delay(2, TimeUnit.SECONDS).schedule();
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        plugin.getDataManager().unload(event.getPlayer().getUniqueId());
    }

    /**
     * Recupere la propriete "textures" (skin) du GameProfile fourni par le client a la
     * connexion et la stocke sur le profil du joueur. C'est cette valeur, deja fournie
     * par le client/serveur d'authentification au moment du login (que le compte soit
     * premium ou gere par un systeme de skins pour les comptes hors-ligne), qui est
     * ensuite reutilisee pour afficher la tete du joueur dans le menu d'amis - sans
     * jamais avoir besoin d'interroger Mojang depuis les serveurs backend.
     */
    private void captureSkin(Player player, PlayerProfile profile) {
        for (GameProfile.Property property : player.getGameProfileProperties()) {
            if ("textures".equals(property.getName())) {
                profile.setSkin(property.getValue(), property.getSignature());
                plugin.getDataManager().saveProfile(profile);
                return;
            }
        }
        // Le client n'a fourni aucune texture (ex : compte hors-ligne sans systeme de skin) :
        // on ne touche pas a une eventuelle valeur precedemment enregistree.
    }
}
