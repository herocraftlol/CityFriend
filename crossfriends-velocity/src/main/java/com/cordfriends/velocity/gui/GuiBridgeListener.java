package com.cordfriends.velocity.gui;

import com.cordfriends.velocity.CrossFriendsVelocityPlugin;
import com.cordfriends.velocity.data.DataManager;
import com.cordfriends.velocity.data.PlayerProfile;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Optional;
import java.util.UUID;

/**
 * Pont entre le proxy et le module Spigot d'interface (menu d'amis affiche en jeu) :
 * - recoit la demande "rejoindre cet ami" envoyee par le module Spigot via plugin-messaging ;
 * - bascule le joueur sur le bon serveur (le proxy est seul a pouvoir le faire).
 *
 * Rejoindre un ami ne fait que basculer sur le meme serveur (comme si on rejoignait
 * ce serveur/monde normalement) : il n'y a aucune teleportation aux coordonnees exactes
 * de l'ami, ni sur ce serveur ni sur un autre.
 *
 * Le module Spigot, lui, se contente d'afficher l'inventaire et de relayer les clics :
 * il n'a pas besoin de connaitre la logique d'amitie / blocage, deja verifiee ici.
 */
public class GuiBridgeListener {

    private final CrossFriendsVelocityPlugin plugin;

    public GuiBridgeListener(CrossFriendsVelocityPlugin plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        if (!CrossFriendsVelocityPlugin.CHANNEL.equals(event.getIdentifier())) {
            return;
        }
        // On ne traite que les messages provenant d'un serveur backend (jamais d'un client) et destines a un joueur
        if (!(event.getSource() instanceof ServerConnection) || !(event.getTarget() instanceof Player)) {
            return;
        }
        event.setResult(PluginMessageEvent.ForwardResult.handled());

        Player requester = (Player) event.getTarget();
        ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());

        String action;
        try {
            action = in.readUTF();
        } catch (Exception e) {
            return;
        }

        if ("JOIN_REQUEST".equals(action)) {
            try {
                handleJoinRequest(requester, in.readUTF());
            } catch (Exception e) {
                plugin.getLogger().warn("Message JOIN_REQUEST invalide recu de {}", requester.getUsername());
            }
        } else if ("OPEN_GUI_REQUEST".equals(action)) {
            FriendGuiOpener.open(plugin, requester);
        }
    }

    private void handleJoinRequest(Player requester, String friendUuidRaw) {
        UUID friendUuid;
        try {
            friendUuid = UUID.fromString(friendUuidRaw);
        } catch (IllegalArgumentException e) {
            return;
        }

        DataManager dm = plugin.getDataManager();
        PlayerProfile requesterProfile = dm.getProfile(requester.getUniqueId());

        if (!requesterProfile.getFriends().contains(friendUuid)) {
            requester.sendMessage(Component.text("Vous n'etes plus ami avec ce joueur.", NamedTextColor.RED));
            return;
        }
        if (dm.isBlocked(requester.getUniqueId(), friendUuid)) {
            requester.sendMessage(Component.text("Action impossible avec ce joueur.", NamedTextColor.RED));
            return;
        }

        Optional<Player> friendPlayerOpt = plugin.getServer().getPlayer(friendUuid);
        if (friendPlayerOpt.isEmpty() || friendPlayerOpt.get().getCurrentServer().isEmpty()) {
            requester.sendMessage(Component.text("Ce joueur est hors-ligne pour le moment.", NamedTextColor.RED));
            return;
        }
        Player friendPlayer = friendPlayerOpt.get();

        RegisteredServer targetServer = friendPlayer.getCurrentServer().get().getServer();
        String friendName = friendPlayer.getUsername();

        Optional<ServerConnection> requesterServerOpt = requester.getCurrentServer();
        if (requesterServerOpt.isPresent() && requesterServerOpt.get().getServer().equals(targetServer)) {
            // Deja sur le meme serveur/monde que l'ami : rien a faire, on ne teleporte jamais
            // vers ses coordonnees exactes.
            requester.sendMessage(Component.text("Vous etes deja sur le meme serveur que " + friendName + ".", NamedTextColor.YELLOW));
            return;
        }

        // Serveur different : on se contente de basculer dessus, exactement comme si le joueur
        // le rejoignait normalement (aucune teleportation aux coordonnees de l'ami une fois arrive).
        requester.sendMessage(Component.text("Connexion vers le serveur de " + friendName + "...", NamedTextColor.AQUA));
        requester.createConnectionRequest(targetServer).connect();
    }
}
