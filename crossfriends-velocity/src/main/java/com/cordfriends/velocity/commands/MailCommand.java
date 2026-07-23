package com.cordfriends.velocity.commands;

import com.cordfriends.velocity.CrossFriendsVelocityPlugin;
import com.cordfriends.velocity.data.DataManager;
import com.cordfriends.velocity.data.MailMessage;
import com.cordfriends.velocity.data.PlayerProfile;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * /mail send <joueur> <message> - laisser un message qu'un joueur verra a sa prochaine connexion
 * /mail read                    - lire son courrier en attente
 * /mail clear                   - vider sa boite de reception
 *
 * Par defaut (mail-require-friendship: false dans config.yml), on peut laisser
 * un mail a n'importe quel joueur deja vu sur le reseau, ami ou non.
 */
public class MailCommand implements SimpleCommand {

    private static final List<String> SUBCOMMANDS = Arrays.asList("send", "read", "clear");

    private final CrossFriendsVelocityPlugin plugin;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM HH:mm");

    public MailCommand(CrossFriendsVelocityPlugin plugin) {
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

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "send":
                if (args.length < 3) {
                    player.sendMessage(Component.text("Usage : /mail send <joueur> <message>", NamedTextColor.RED));
                    return;
                }
                sendMail(player, args[1], String.join(" ", Arrays.asList(args).subList(2, args.length)));
                break;

            case "read":
            case "list":
                readMail(player);
                break;

            case "clear":
                clearMail(player);
                break;

            default:
                sendUsage(player);
        }
    }

    private void sendMail(Player player, String targetName, String message) {
        if (targetName.equalsIgnoreCase(player.getUsername())) {
            player.sendMessage(Component.text("Vous ne pouvez pas vous laisser un message a vous-meme.", NamedTextColor.RED));
            return;
        }

        DataManager dm = plugin.getDataManager();
        Optional<UUID> targetUuidOpt = dm.resolveUuid(targetName);
        if (targetUuidOpt.isEmpty()) {
            player.sendMessage(Component.text("Joueur inconnu : " + targetName, NamedTextColor.RED));
            return;
        }
        UUID targetUuid = targetUuidOpt.get();

        if (dm.isBlocked(player.getUniqueId(), targetUuid)) {
            player.sendMessage(Component.text("Impossible de laisser un message a " + targetName + " (joueur bloque).", NamedTextColor.RED));
            return;
        }

        if (plugin.isMailRequireFriendship() && !player.hasPermission("crossfriends.bypass")) {
            PlayerProfile senderProfile = dm.getProfile(player.getUniqueId());
            if (!senderProfile.getFriends().contains(targetUuid)) {
                player.sendMessage(Component.text("Vous devez etre ami avec " + targetName + " pour lui laisser un message.", NamedTextColor.RED));
                return;
            }
        }

        PlayerProfile targetProfile = dm.getProfile(targetUuid);
        targetProfile.getMailbox().add(new MailMessage(player.getUniqueId(), player.getUsername(), message, System.currentTimeMillis()));
        dm.saveProfile(targetProfile);

        player.sendMessage(Component.text("Message laisse a " + targetName + ", il le verra a sa prochaine connexion.", NamedTextColor.GREEN));

        Optional<Player> targetPlayerOpt = plugin.getServer().getPlayer(targetUuid);
        targetPlayerOpt.ifPresent(targetPlayer -> targetPlayer.sendMessage(
                Component.text("Vous avez recu un nouveau message de " + player.getUsername()
                        + ". Tapez /mail read pour le lire.", NamedTextColor.AQUA)));
    }

    private void readMail(Player player) {
        DataManager dm = plugin.getDataManager();
        PlayerProfile profile = dm.getProfile(player.getUniqueId());
        List<MailMessage> mailbox = profile.getMailbox();

        if (mailbox.isEmpty()) {
            player.sendMessage(Component.text("Vous n'avez aucun message en attente.", NamedTextColor.YELLOW));
            return;
        }

        player.sendMessage(Component.text("=== Vos messages (" + mailbox.size() + ") ===", NamedTextColor.GOLD));
        for (MailMessage mail : mailbox) {
            String date = dateFormat.format(new Date(mail.getTimestamp()));
            player.sendMessage(Component.text("[" + date + "] ", NamedTextColor.AQUA)
                    .append(Component.text(mail.getSenderName(), NamedTextColor.GREEN))
                    .append(Component.text(" : ", NamedTextColor.GRAY))
                    .append(Component.text(mail.getMessage(), NamedTextColor.WHITE)));
            mail.setRead(true);
        }
        dm.saveProfile(profile);
        player.sendMessage(Component.text("Utilisez /mail clear pour vider votre boite de reception.", NamedTextColor.GRAY));
    }

    private void clearMail(Player player) {
        DataManager dm = plugin.getDataManager();
        PlayerProfile profile = dm.getProfile(player.getUniqueId());
        int count = profile.getMailbox().size();
        profile.getMailbox().clear();
        dm.saveProfile(profile);
        player.sendMessage(Component.text(count + " message(s) supprime(s).", NamedTextColor.GREEN));
    }

    private void sendUsage(Player player) {
        player.sendMessage(Component.text("=== Commandes /mail ===", NamedTextColor.GOLD));
        player.sendMessage(Component.text("/mail send <joueur> <message>", NamedTextColor.AQUA)
                .append(Component.text(" - Laisser un message a voir plus tard", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/mail read", NamedTextColor.AQUA)
                .append(Component.text(" - Lire vos messages en attente", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/mail clear", NamedTextColor.AQUA)
                .append(Component.text(" - Vider votre boite de reception", NamedTextColor.GRAY)));
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length <= 1) {
            String partial = args.length == 0 ? "" : args[0];
            return TabCompleteUtil.filterKeywords(SUBCOMMANDS, partial);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("send")) {
            return TabCompleteUtil.onlinePlayerNames(plugin, args[1]);
        }
        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return true;
    }
}
