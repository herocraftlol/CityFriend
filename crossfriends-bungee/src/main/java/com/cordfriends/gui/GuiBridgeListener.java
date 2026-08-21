package com.cordfriends.gui;

import com.cordfriends.CrossFriendsPlugin;
import com.cordfriends.data.DataManager;
import com.cordfriends.data.PlayerProfile;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

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
public class GuiBridgeListener implements Listener {

    private final CrossFriendsPlugin plugin;

    public GuiBridgeListener(CrossFriendsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) {
        if (!CrossFriendsPlugin.CHANNEL.equals(event.getTag())) {
            return;
        }
        // On ne traite que les messages provenant d'un serveur backend (jamais d'un client) et destines a un joueur
        if (!(event.getSender() instanceof Server) || !(event.getReceiver() instanceof ProxiedPlayer)) {
            return;
        }
        event.setCancelled(true);

        ProxiedPlayer requester = (ProxiedPlayer) event.getReceiver();
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
                plugin.getLogger().warning("Message JOIN_REQUEST invalide recu de " + requester.getName());
            }
        } else if ("OPEN_GUI_REQUEST".equals(action)) {
            FriendGuiOpener.open(plugin, requester);
        }
    }

    private void handleJoinRequest(ProxiedPlayer requester, String friendUuidRaw) {
        UUID friendUuid;
        try {
            friendUuid = UUID.fromString(friendUuidRaw);
        } catch (IllegalArgumentException e) {
            return;
        }

        DataManager dm = plugin.getDataManager();
        PlayerProfile requesterProfile = dm.getProfile(requester.getUniqueId());

        if (!requesterProfile.getFriends().contains(friendUuid)) {
            requester.sendMessage(ChatColor.RED + "Vous n'etes plus ami avec ce joueur.");
            return;
        }
        if (dm.isBlocked(requester.getUniqueId(), friendUuid)) {
            requester.sendMessage(ChatColor.RED + "Action impossible avec ce joueur.");
            return;
        }

        ProxiedPlayer friendPlayer = plugin.getProxy().getPlayer(friendUuid);
        if (friendPlayer == null || friendPlayer.getServer() == null) {
            requester.sendMessage(ChatColor.RED + "Ce joueur est hors-ligne pour le moment.");
            return;
        }

        ServerInfo targetServer = friendPlayer.getServer().getInfo();
        String friendName = friendPlayer.getName();

        if (requester.getServer() != null && requester.getServer().getInfo().equals(targetServer)) {
            // Deja sur le meme serveur/monde que l'ami : rien a faire, on ne teleporte jamais
            // vers ses coordonnees exactes.
            requester.sendMessage(ChatColor.YELLOW + "Vous etes deja sur le meme serveur que " + friendName + ".");
            return;
        }

        // Serveur different : on se contente de basculer dessus, exactement comme si le joueur
        // le rejoignait normalement (aucune teleportation aux coordonnees de l'ami une fois arrive).
        requester.sendMessage(ChatColor.AQUA + "Connexion vers le serveur de " + friendName + "...");
        requester.connect(targetServer);
    }
}
