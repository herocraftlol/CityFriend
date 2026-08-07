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
import java.util.Optional;
import java.util.UUID;

/**
 * /r <message>
 * Repond directement au dernier joueur avec qui une conversation a eu lieu
 * (via /msg ou /r), sans avoir a retaper son nom.
 */
public class ReplyCommand implements SimpleCommand {

    private final CrossFriendsVelocityPlugin plugin;

    public ReplyCommand(CrossFriendsVelocityPlugin plugin) {
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
        if (args.length < 1) {
            sender.sendMessage(Component.text("Usage : /r <message>", NamedTextColor.RED));
            return;
        }

        Player player = (Player) sender;
        DataManager dm = plugin.getDataManager();
        UUID lastUuid = dm.getLastConversant(player.getUniqueId());

        if (lastUuid == null) {
            player.sendMessage(Component.text("Vous n'avez personne a qui repondre pour le moment.", NamedTextColor.RED));
            return;
        }

        String message = String.join(" ", Arrays.asList(args));

        if (dm.isBlocked(player.getUniqueId(), lastUuid)) {
            player.sendMessage(Component.text("Impossible de repondre a ce joueur (bloque).", NamedTextColor.RED));
            return;
        }

        if (plugin.isRequireFriendship() && !player.hasPermission("crossfriends.bypass")) {
            PlayerProfile senderProfile = dm.getProfile(player.getUniqueId());
            if (!senderProfile.getFriends().contains(lastUuid)) {
                player.sendMessage(Component.text("Vous devez etre ami avec cette personne pour lui repondre.", NamedTextColor.RED));
                return;
            }
        }

        Optional<Player> targetPlayerOpt = plugin.getServer().getPlayer(lastUuid);
        if (targetPlayerOpt.isPresent()) {
            Player targetPlayer = targetPlayerOpt.get();
            targetPlayer.sendMessage(Component.text("[" + player.getUsername() + " -> Vous] ", NamedTextColor.LIGHT_PURPLE)
                    .append(Component.text(message, NamedTextColor.WHITE)));
            player.sendMessage(Component.text("[Vous -> " + targetPlayer.getUsername() + "] ", NamedTextColor.LIGHT_PURPLE)
                    .append(Component.text(message, NamedTextColor.WHITE)));
            dm.setLastConversant(targetPlayer.getUniqueId(), player.getUniqueId());
        } else {
            PlayerProfile targetProfile = dm.getProfile(lastUuid);
            targetProfile.getMailbox().add(new MailMessage(player.getUniqueId(), player.getUsername(), message, System.currentTimeMillis()));
            dm.saveProfile(targetProfile);
            player.sendMessage(Component.text("Ce joueur est hors-ligne, votre message a ete sauvegarde.", NamedTextColor.YELLOW));
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return true;
    }
}
