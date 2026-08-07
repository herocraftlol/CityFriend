package com.cordfriends.velocity.commands;

import com.cordfriends.velocity.CrossFriendsVelocityPlugin;
import com.cordfriends.velocity.data.DataManager;
import com.cordfriends.velocity.data.PlayerProfile;
import com.cordfriends.velocity.gui.FriendGuiOpener;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * /friend add|accept|deny|remove|list|requests|block|unblock|blocked|gui
 * Fonctionne quel que soit le sous-serveur sur lequel se trouvent les joueurs,
 * car la commande est traitee directement par le proxy Velocity.
 */
public class FriendCommand implements SimpleCommand {

    private static final List<String> SUBCOMMANDS = Arrays.asList(
            "add", "accept", "deny", "remove", "list", "requests", "block", "unblock", "blocked", "gui"
    );

    private final CrossFriendsVelocityPlugin plugin;

    public FriendCommand(CrossFriendsVelocityPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();

        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Cette commande doit etre executee par un joueur.", NamedTextColor.RED));
            return;
        }
        Player player = (Player) sender;

        if (args.length == 0) {
            sendUsage(player);
            return;
        }

        DataManager dm = plugin.getDataManager();
        PlayerProfile profile = dm.getProfile(player.getUniqueId());

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "add":
            case "request":
                if (args.length < 2) {
                    player.sendMessage(Component.text("Usage : /friend add <joueur>", NamedTextColor.RED));
                    return;
                }
                handleAdd(player, profile, args[1]);
                break;

            case "accept":
                if (args.length < 2) {
                    player.sendMessage(Component.text("Usage : /friend accept <joueur>", NamedTextColor.RED));
                    return;
                }
                handleAccept(player, profile, args[1]);
                break;

            case "deny":
            case "refuse":
                if (args.length < 2) {
                    player.sendMessage(Component.text("Usage : /friend deny <joueur>", NamedTextColor.RED));
                    return;
                }
                handleDeny(player, profile, args[1]);
                break;

            case "remove":
            case "delete":
                if (args.length < 2) {
                    player.sendMessage(Component.text("Usage : /friend remove <joueur>", NamedTextColor.RED));
                    return;
                }
                handleRemove(player, profile, args[1]);
                break;

            case "list":
                handleList(player, profile);
                break;

            case "requests":
            case "pending":
                handleRequests(player, profile);
                break;

            case "block":
                if (args.length < 2) {
                    player.sendMessage(Component.text("Usage : /friend block <joueur>", NamedTextColor.RED));
                    return;
                }
                handleBlock(player, profile, args[1]);
                break;

            case "unblock":
                if (args.length < 2) {
                    player.sendMessage(Component.text("Usage : /friend unblock <joueur>", NamedTextColor.RED));
                    return;
                }
                handleUnblock(player, profile, args[1]);
                break;

            case "blocked":
                handleBlockedList(player, profile);
                break;

            case "gui":
            case "menu":
                handleGui(player, profile);
                break;

            default:
                sendUsage(player);
        }
    }

    // ---------------------------------------------------------------
    // Demandes d'amis
    // ---------------------------------------------------------------

    private void handleAdd(Player sender, PlayerProfile senderProfile, String targetName) {
        if (targetName.equalsIgnoreCase(sender.getUsername())) {
            sender.sendMessage(Component.text("Vous ne pouvez pas vous ajouter vous-meme.", NamedTextColor.RED));
            return;
        }

        DataManager dm = plugin.getDataManager();
        Optional<UUID> targetUuidOpt = dm.resolveUuid(targetName);
        if (targetUuidOpt.isEmpty()) {
            sender.sendMessage(Component.text("Joueur inconnu : " + targetName
                    + " (il doit s'etre connecte au moins une fois sur le reseau).", NamedTextColor.RED));
            return;
        }
        UUID targetUuid = targetUuidOpt.get();

        if (senderProfile.getBlocked().contains(targetUuid)) {
            sender.sendMessage(Component.text("Vous avez bloque ce joueur. Utilisez /friend unblock " + targetName + " avant.", NamedTextColor.RED));
            return;
        }
        PlayerProfile targetProfile = dm.getProfile(targetUuid);
        if (targetProfile.getBlocked().contains(sender.getUniqueId())) {
            sender.sendMessage(Component.text("Ce joueur n'accepte pas vos demandes d'ami.", NamedTextColor.RED));
            return;
        }

        if (senderProfile.getFriends().contains(targetUuid)) {
            sender.sendMessage(Component.text(targetName + " est deja dans votre liste d'amis.", NamedTextColor.YELLOW));
            return;
        }
        if (senderProfile.getOutgoingRequests().contains(targetUuid)) {
            sender.sendMessage(Component.text("Demande deja envoyee a " + targetName + ".", NamedTextColor.YELLOW));
            return;
        }

        // Si la cible nous avait deja envoye une demande, on l'accepte directement
        if (senderProfile.getIncomingRequests().contains(targetUuid)) {
            acceptFriendship(sender, senderProfile, targetUuid, targetProfile);
            return;
        }

        senderProfile.getOutgoingRequests().add(targetUuid);
        targetProfile.getIncomingRequests().add(sender.getUniqueId());
        dm.saveProfile(senderProfile);
        dm.saveProfile(targetProfile);

        sender.sendMessage(Component.text("Demande d'ami envoyee a " + targetName + ".", NamedTextColor.GREEN));

        Optional<Player> targetPlayerOpt = plugin.getServer().getPlayer(targetUuid);
        targetPlayerOpt.ifPresent(targetPlayer -> notifyIncomingRequest(targetPlayer, sender.getUsername()));
    }

    private void notifyIncomingRequest(Player target, String fromName) {
        Component accept = Component.text("[Accepter]", NamedTextColor.GREEN)
                .clickEvent(ClickEvent.runCommand("/friend accept " + fromName))
                .hoverEvent(HoverEvent.showText(Component.text("Cliquez pour accepter")));

        Component deny = Component.text(" [Refuser]", NamedTextColor.RED)
                .clickEvent(ClickEvent.runCommand("/friend deny " + fromName))
                .hoverEvent(HoverEvent.showText(Component.text("Cliquez pour refuser")));

        Component message = Component.text(fromName + " vous a envoye une demande d'ami. ", NamedTextColor.GOLD)
                .append(accept)
                .append(deny);

        target.sendMessage(message);
    }

    private void handleAccept(Player sender, PlayerProfile senderProfile, String fromName) {
        DataManager dm = plugin.getDataManager();
        Optional<UUID> fromUuidOpt = dm.resolveUuid(fromName);
        if (fromUuidOpt.isEmpty() || !senderProfile.getIncomingRequests().contains(fromUuidOpt.get())) {
            sender.sendMessage(Component.text("Aucune demande en attente de " + fromName + ".", NamedTextColor.RED));
            return;
        }
        UUID fromUuid = fromUuidOpt.get();
        PlayerProfile fromProfile = dm.getProfile(fromUuid);
        acceptFriendship(sender, senderProfile, fromUuid, fromProfile);
    }

    private void acceptFriendship(Player sender, PlayerProfile senderProfile, UUID otherUuid, PlayerProfile otherProfile) {
        senderProfile.getIncomingRequests().remove(otherUuid);
        senderProfile.getOutgoingRequests().remove(otherUuid);
        otherProfile.getIncomingRequests().remove(sender.getUniqueId());
        otherProfile.getOutgoingRequests().remove(sender.getUniqueId());

        senderProfile.getFriends().add(otherUuid);
        otherProfile.getFriends().add(sender.getUniqueId());

        DataManager dm = plugin.getDataManager();
        dm.saveProfile(senderProfile);
        dm.saveProfile(otherProfile);

        String otherName = otherProfile.getName() != null ? otherProfile.getName() : "?";
        sender.sendMessage(Component.text("Vous etes maintenant ami avec " + otherName + " !", NamedTextColor.GREEN));

        Optional<Player> otherPlayerOpt = plugin.getServer().getPlayer(otherUuid);
        otherPlayerOpt.ifPresent(otherPlayer -> otherPlayer.sendMessage(
                Component.text(sender.getUsername() + " a accepte votre demande d'ami !", NamedTextColor.GREEN)));
    }

    private void handleDeny(Player sender, PlayerProfile senderProfile, String fromName) {
        DataManager dm = plugin.getDataManager();
        Optional<UUID> fromUuidOpt = dm.resolveUuid(fromName);
        if (fromUuidOpt.isEmpty() || !senderProfile.getIncomingRequests().remove(fromUuidOpt.get())) {
            sender.sendMessage(Component.text("Aucune demande en attente de " + fromName + ".", NamedTextColor.RED));
            return;
        }
        UUID fromUuid = fromUuidOpt.get();
        PlayerProfile fromProfile = dm.getProfile(fromUuid);
        fromProfile.getOutgoingRequests().remove(sender.getUniqueId());

        dm.saveProfile(senderProfile);
        dm.saveProfile(fromProfile);

        sender.sendMessage(Component.text("Demande de " + fromName + " refusee.", NamedTextColor.YELLOW));

        Optional<Player> fromPlayerOpt = plugin.getServer().getPlayer(fromUuid);
        fromPlayerOpt.ifPresent(fromPlayer -> fromPlayer.sendMessage(
                Component.text(sender.getUsername() + " a refuse votre demande d'ami.", NamedTextColor.YELLOW)));
    }

    private void handleRemove(Player sender, PlayerProfile senderProfile, String targetName) {
        DataManager dm = plugin.getDataManager();
        Optional<UUID> targetUuidOpt = dm.resolveUuid(targetName);
        if (targetUuidOpt.isEmpty() || !senderProfile.getFriends().remove(targetUuidOpt.get())) {
            sender.sendMessage(Component.text(targetName + " n'est pas dans votre liste d'amis.", NamedTextColor.RED));
            return;
        }
        UUID targetUuid = targetUuidOpt.get();
        PlayerProfile targetProfile = dm.getProfile(targetUuid);
        targetProfile.getFriends().remove(sender.getUniqueId());

        dm.saveProfile(senderProfile);
        dm.saveProfile(targetProfile);

        sender.sendMessage(Component.text(targetName + " a ete retire de votre liste d'amis.", NamedTextColor.YELLOW));
    }

    private void handleList(Player sender, PlayerProfile profile) {
        if (profile.getFriends().isEmpty()) {
            sender.sendMessage(Component.text("Vous n'avez pas encore d'amis. Utilisez /friend add <joueur>.", NamedTextColor.YELLOW));
            return;
        }
        DataManager dm = plugin.getDataManager();
        sender.sendMessage(Component.text("=== Votre liste d'amis (" + profile.getFriends().size() + ") ===", NamedTextColor.GOLD));
        for (UUID uuid : profile.getFriends()) {
            PlayerProfile friendProfile = dm.getProfile(uuid);
            String name = friendProfile.getName() != null ? friendProfile.getName() : uuid.toString();
            Optional<Player> onlineOpt = plugin.getServer().getPlayer(uuid);
            if (onlineOpt.isPresent()) {
                Optional<ServerConnection> serverConn = onlineOpt.get().getCurrentServer();
                String server = serverConn.isPresent() ? serverConn.get().getServerInfo().getName() : "?";
                sender.sendMessage(Component.text("- " + name, NamedTextColor.GREEN)
                        .append(Component.text(" (en ligne sur " + server + ")", NamedTextColor.GRAY)));
            } else {
                sender.sendMessage(Component.text("- " + name + " (hors ligne)", NamedTextColor.DARK_GRAY));
            }
        }
        sender.sendMessage(Component.text("Astuce : /friend gui pour une interface cliquable.", NamedTextColor.GRAY));
    }

    private void handleRequests(Player sender, PlayerProfile profile) {
        if (profile.getIncomingRequests().isEmpty()) {
            sender.sendMessage(Component.text("Vous n'avez aucune demande d'ami en attente.", NamedTextColor.YELLOW));
            return;
        }
        DataManager dm = plugin.getDataManager();
        sender.sendMessage(Component.text("=== Demandes en attente ===", NamedTextColor.GOLD));
        for (UUID uuid : profile.getIncomingRequests()) {
            PlayerProfile fromProfile = dm.getProfile(uuid);
            String name = fromProfile.getName() != null ? fromProfile.getName() : uuid.toString();
            sender.sendMessage(Component.text("- " + name, NamedTextColor.AQUA)
                    .append(Component.text("  /friend accept " + name, NamedTextColor.GREEN))
                    .append(Component.text("  /friend deny " + name, NamedTextColor.RED)));
        }
    }

    // ---------------------------------------------------------------
    // Blocage
    // ---------------------------------------------------------------

    private void handleBlock(Player sender, PlayerProfile senderProfile, String targetName) {
        if (targetName.equalsIgnoreCase(sender.getUsername())) {
            sender.sendMessage(Component.text("Vous ne pouvez pas vous bloquer vous-meme.", NamedTextColor.RED));
            return;
        }
        DataManager dm = plugin.getDataManager();
        Optional<UUID> targetUuidOpt = dm.resolveUuid(targetName);
        if (targetUuidOpt.isEmpty()) {
            sender.sendMessage(Component.text("Joueur inconnu : " + targetName, NamedTextColor.RED));
            return;
        }
        UUID targetUuid = targetUuidOpt.get();

        if (!senderProfile.getBlocked().add(targetUuid)) {
            sender.sendMessage(Component.text(targetName + " est deja bloque.", NamedTextColor.YELLOW));
            return;
        }

        // On retire automatiquement l'amitie et les demandes en cours dans les deux sens
        PlayerProfile targetProfile = dm.getProfile(targetUuid);
        senderProfile.getFriends().remove(targetUuid);
        senderProfile.getIncomingRequests().remove(targetUuid);
        senderProfile.getOutgoingRequests().remove(targetUuid);
        targetProfile.getFriends().remove(sender.getUniqueId());
        targetProfile.getIncomingRequests().remove(sender.getUniqueId());
        targetProfile.getOutgoingRequests().remove(sender.getUniqueId());

        dm.saveProfile(senderProfile);
        dm.saveProfile(targetProfile);

        sender.sendMessage(Component.text(targetName + " a ete bloque. Il ne peut plus vous envoyer de demande d'ami ni de message.", NamedTextColor.GREEN));
    }

    private void handleUnblock(Player sender, PlayerProfile senderProfile, String targetName) {
        DataManager dm = plugin.getDataManager();
        Optional<UUID> targetUuidOpt = dm.resolveUuid(targetName);
        if (targetUuidOpt.isEmpty() || !senderProfile.getBlocked().remove(targetUuidOpt.get())) {
            sender.sendMessage(Component.text(targetName + " n'est pas dans votre liste de joueurs bloques.", NamedTextColor.RED));
            return;
        }
        dm.saveProfile(senderProfile);
        sender.sendMessage(Component.text(targetName + " a ete debloque.", NamedTextColor.GREEN));
    }

    private void handleBlockedList(Player sender, PlayerProfile profile) {
        if (profile.getBlocked().isEmpty()) {
            sender.sendMessage(Component.text("Vous n'avez bloque aucun joueur.", NamedTextColor.YELLOW));
            return;
        }
        DataManager dm = plugin.getDataManager();
        sender.sendMessage(Component.text("=== Joueurs bloques (" + profile.getBlocked().size() + ") ===", NamedTextColor.GOLD));
        for (UUID uuid : profile.getBlocked()) {
            PlayerProfile blockedProfile = dm.getProfile(uuid);
            String name = blockedProfile.getName() != null ? blockedProfile.getName() : uuid.toString();
            sender.sendMessage(Component.text("- " + name, NamedTextColor.DARK_GRAY));
        }
    }

    // ---------------------------------------------------------------
    // Interface graphique (necessite le module Spigot sur le serveur backend)
    // ---------------------------------------------------------------

    private void handleGui(Player sender, PlayerProfile profile) {
        FriendGuiOpener.open(plugin, sender);
    }

    // ---------------------------------------------------------------
    // Aide et tab-completion
    // ---------------------------------------------------------------

    private void sendUsage(Player player) {
        player.sendMessage(Component.text("=== Commandes /friend ===", NamedTextColor.GOLD));
        player.sendMessage(Component.text("/friend add <joueur>", NamedTextColor.AQUA)
                .append(Component.text(" - Envoyer une demande d'ami", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/friend accept <joueur>", NamedTextColor.AQUA)
                .append(Component.text(" - Accepter une demande", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/friend deny <joueur>", NamedTextColor.AQUA)
                .append(Component.text(" - Refuser une demande", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/friend remove <joueur>", NamedTextColor.AQUA)
                .append(Component.text(" - Retirer un ami", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/friend list", NamedTextColor.AQUA)
                .append(Component.text(" - Voir votre liste d'amis", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/friend requests", NamedTextColor.AQUA)
                .append(Component.text(" - Voir les demandes en attente", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/friend block <joueur>", NamedTextColor.AQUA)
                .append(Component.text(" - Bloquer un joueur", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/friend unblock <joueur>", NamedTextColor.AQUA)
                .append(Component.text(" - Debloquer un joueur", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/friend blocked", NamedTextColor.AQUA)
                .append(Component.text(" - Voir les joueurs bloques", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/friend gui", NamedTextColor.AQUA)
                .append(Component.text(" - Ouvrir l'interface graphique d'amis", NamedTextColor.GRAY)));
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();

        if (!(sender instanceof Player player)) {
            return List.of();
        }
        DataManager dm = plugin.getDataManager();

        if (args.length <= 1) {
            String partial = args.length == 0 ? "" : args[0];
            return TabCompleteUtil.filterKeywords(SUBCOMMANDS, partial);
        }

        if (args.length == 2) {
            PlayerProfile profile = dm.getProfile(player.getUniqueId());
            String sub = args[0].toLowerCase(Locale.ROOT);
            String partial = args[1];

            switch (sub) {
                case "add":
                case "request":
                    return TabCompleteUtil.onlinePlayerNames(plugin, partial);
                case "accept":
                case "deny":
                case "refuse":
                    return TabCompleteUtil.namesFromUuids(dm, profile.getIncomingRequests(), partial);
                case "remove":
                case "delete":
                    return TabCompleteUtil.namesFromUuids(dm, profile.getFriends(), partial);
                case "block":
                    return TabCompleteUtil.onlinePlayerNames(plugin, partial);
                case "unblock":
                    return TabCompleteUtil.namesFromUuids(dm, profile.getBlocked(), partial);
                default:
                    return List.of();
            }
        }

        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return true;
    }
}
