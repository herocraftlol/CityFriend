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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * /msg <joueur> <message>
 * Si le destinataire est en ligne (sur n'importe quel serveur du reseau), le
 * message est livre instantanement via le proxy. S'il est hors-ligne, le
 * message est automatiquement sauvegarde comme courrier et lui sera presente
 * a sa prochaine connexion.
 */
public class MsgCommand implements SimpleCommand {

    private final CrossFriendsVelocityPlugin plugin;

    public MsgCommand(CrossFriendsVelocityPlugin plugin) {
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
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage : /msg <joueur> <message>", NamedTextColor.RED));
            return;
        }

        Player player = (Player) sender;
        String targetName = args[0];

        if (targetName.equalsIgnoreCase(player.getUsername())) {
            player.sendMessage(Component.text("Vous ne pouvez pas vous envoyer un message a vous-meme.", NamedTextColor.RED));
            return;
        }

        String message = String.join(" ", Arrays.asList(args).subList(1, args.length));

        DataManager dm = plugin.getDataManager();
        Optional<UUID> targetUuidOpt = dm.resolveUuid(targetName);
        if (targetUuidOpt.isEmpty()) {
            player.sendMessage(Component.text("Joueur inconnu : " + targetName, NamedTextColor.RED));
            return;
        }
        UUID targetUuid = targetUuidOpt.get();

        if (dm.isBlocked(player.getUniqueId(), targetUuid)) {
            player.sendMessage(Component.text("Impossible d'envoyer un message a " + targetName + " (joueur bloque).", NamedTextColor.RED));
            return;
        }

        if (plugin.isRequireFriendship() && !player.hasPermission("crossfriends.bypass")) {
            PlayerProfile senderProfile = dm.getProfile(player.getUniqueId());
            if (!senderProfile.getFriends().contains(targetUuid)) {
                player.sendMessage(Component.text("Vous devez etre ami avec " + targetName
                        + " pour lui envoyer un message prive (/friend add " + targetName + ").", NamedTextColor.RED));
                return;
            }
        }

        Optional<Player> targetPlayerOpt = plugin.getServer().getPlayer(targetUuid);

        if (targetPlayerOpt.isPresent()) {
            // Le destinataire est en ligne, quel que soit son serveur : livraison instantanee
            Player targetPlayer = targetPlayerOpt.get();
            targetPlayer.sendMessage(Component.text("[" + player.getUsername() + " -> Vous] ", NamedTextColor.LIGHT_PURPLE)
                    .append(Component.text(message, NamedTextColor.WHITE)));
            player.sendMessage(Component.text("[Vous -> " + targetPlayer.getUsername() + "] ", NamedTextColor.LIGHT_PURPLE)
                    .append(Component.text(message, NamedTextColor.WHITE)));

            dm.setLastConversant(player.getUniqueId(), targetUuid);
            dm.setLastConversant(targetUuid, player.getUniqueId());
        } else {
            // Hors-ligne : le message est conserve comme courrier en attente
            PlayerProfile targetProfile = dm.getProfile(targetUuid);
            targetProfile.getMailbox().add(new MailMessage(player.getUniqueId(), player.getUsername(), message, System.currentTimeMillis()));
            dm.saveProfile(targetProfile);

            player.sendMessage(Component.text(targetName
                    + " est hors-ligne. Votre message a ete sauvegarde et lui sera remis a sa connexion.", NamedTextColor.YELLOW));
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length == 1) {
            return TabCompleteUtil.onlinePlayerNames(plugin, args[0]);
        }
        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return true;
    }
}
