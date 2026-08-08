package com.cordfriends.velocity.gui;

import com.cordfriends.velocity.CrossFriendsVelocityPlugin;
import com.cordfriends.velocity.data.DataManager;
import com.cordfriends.velocity.data.PlayerProfile;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.util.GameProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Optional;
import java.util.UUID;

/**
 * Logique commune d'ouverture du menu d'amis (envoi du plugin-message "OPEN_GUI"
 * vers le module Spigot sur lequel se trouve le joueur).
 *
 * Utilisee a la fois par :
 * - /friend gui execute directement sur le proxy (FriendCommand) ;
 * - une demande d'ouverture recue depuis le serveur backend (GuiBridgeListener),
 *   ce qui permet a un plugin Spigot tiers (ChestCommands, /execute as ... run friend gui, etc.)
 *   de declencher l'ouverture du menu sans connaitre la logique d'amitie.
 */
public final class FriendGuiOpener {

    private FriendGuiOpener() {
    }

    public static void open(CrossFriendsVelocityPlugin plugin, Player player) {
        DataManager dm = plugin.getDataManager();
        PlayerProfile profile = dm.getProfile(player.getUniqueId());

        if (profile.getFriends().isEmpty()) {
            player.sendMessage(Component.text("Vous n'avez pas encore d'amis. Utilisez /friend add <joueur>.", NamedTextColor.YELLOW));
            return;
        }
        Optional<ServerConnection> serverConnOpt = player.getCurrentServer();
        if (serverConnOpt.isEmpty()) {
            player.sendMessage(Component.text("Impossible d'ouvrir l'interface pour le moment, reessayez dans un instant.", NamedTextColor.RED));
            return;
        }

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("OPEN_GUI");
        out.writeInt(profile.getFriends().size());

        for (UUID uuid : profile.getFriends()) {
            PlayerProfile friendProfile = dm.getProfile(uuid);
            String name = friendProfile.getName() != null ? friendProfile.getName() : uuid.toString();
            Optional<Player> onlineOpt = plugin.getServer().getPlayer(uuid);
            boolean isOnline = onlineOpt.isPresent();
            String server = "";
            String skinValue = friendProfile.getSkinValue();
            String skinSignature = friendProfile.getSkinSignature();
            if (isOnline) {
                Optional<ServerConnection> onlineServerConn = onlineOpt.get().getCurrentServer();
                if (onlineServerConn.isPresent()) {
                    server = onlineServerConn.get().getServerInfo().getName();
                }
                // Ami actuellement connecte : on prefere sa texture "live" (son skin a pu
                // changer depuis la derniere fois qu'on l'a enregistree, ex : /skin en cours de session).
                for (GameProfile.Property property : onlineOpt.get().getGameProfileProperties()) {
                    if ("textures".equals(property.getName())) {
                        skinValue = property.getValue();
                        skinSignature = property.getSignature();
                        break;
                    }
                }
            }

            out.writeUTF(uuid.toString());
            out.writeUTF(name);
            out.writeBoolean(isOnline);
            out.writeUTF(server);
            out.writeUTF(skinValue != null ? skinValue : "");
            out.writeUTF(skinSignature != null ? skinSignature : "");
        }

        serverConnOpt.get().sendPluginMessage(CrossFriendsVelocityPlugin.CHANNEL, out.toByteArray());
        player.sendMessage(Component.text("Ouverture de l'interface d'amis...", NamedTextColor.GRAY));
    }
}
