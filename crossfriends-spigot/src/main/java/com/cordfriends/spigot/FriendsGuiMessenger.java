package com.cordfriends.spigot;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.messaging.PluginMessageListener;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Recoit les messages venant du proxy BungeeCord sur le canal "crossfriends:main" :
 * - OPEN_GUI : construit et ouvre l'inventaire listant les amis du joueur.
 *
 * Rejoindre un ami ne fait que basculer le joueur sur le meme serveur cote proxy ;
 * ce module n'effectue plus aucune teleportation aux coordonnees de l'ami une fois arrive.
 */
public class FriendsGuiMessenger implements PluginMessageListener {

    private final CrossFriendsSpigotPlugin plugin;

    public FriendsGuiMessenger(CrossFriendsSpigotPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!CrossFriendsSpigotPlugin.CHANNEL.equals(channel)) {
            return;
        }

        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String action;
        try {
            action = in.readUTF();
        } catch (Exception e) {
            return;
        }

        if ("OPEN_GUI".equals(action)) {
            handleOpenGui(player, in);
        }
    }

    private void handleOpenGui(Player player, ByteArrayDataInput in) {
        int count = in.readInt();
        List<FriendEntry> entries = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            UUID uuid = UUID.fromString(in.readUTF());
            String name = in.readUTF();
            boolean online = in.readBoolean();
            String server = in.readUTF();
            String skinValue = in.readUTF();
            String skinSignature = in.readUTF();
            entries.add(new FriendEntry(uuid, name, online, server, skinValue, skinSignature));
        }

        // Les inventaires doivent etre crees/ouverts sur le thread principal du serveur
        Bukkit.getScheduler().runTask(plugin, () -> openMenu(player, entries));
    }

    private void openMenu(Player player, List<FriendEntry> entries) {
        int size = Math.max(9, Math.min(54, ((entries.size() - 1) / 9 + 1) * 9));
        FriendsMenuHolder holder = new FriendsMenuHolder();
        Inventory inventory = Bukkit.createInventory(holder, size, ChatColor.DARK_PURPLE + "Vos amis");
        holder.setInventory(inventory);

        int slot = 0;
        for (FriendEntry entry : entries) {
            if (slot >= size) {
                break;
            }
            inventory.setItem(slot, buildHead(entry));
            holder.put(slot, entry);
            slot++;
        }

        player.openInventory(inventory);
        loadSkinsAsync(player, inventory, holder);
    }

    /**
     * Recupere le vrai skin de chaque ami en arriere-plan (appel reseau vers Mojang via
     * PlayerProfile#complete) puis met a jour les tetes deja affichees dans le menu.
     *
     * On ne peut pas se contenter de OfflinePlayer#setOwningPlayer : cela n'affiche le bon
     * skin que si le joueur a deja rejoint CE serveur backend. En completant un PlayerProfile,
     * on recupere la texture quel que soit le serveur ou l'ami a ete vu pour la derniere fois.
     * Le tout se fait hors du thread principal car l'appel reseau est bloquant.
     */
    private void loadSkinsAsync(Player player, Inventory inventory, FriendsMenuHolder holder) {
        List<Map.Entry<Integer, FriendEntry>> needsLookup = new ArrayList<>();
        for (Map.Entry<Integer, FriendEntry> mapEntry : holder.getSlots().entrySet()) {
            if (!mapEntry.getValue().hasCachedSkin()) {
                needsLookup.add(mapEntry);
            }
        }
        if (needsLookup.isEmpty()) {
            plugin.getLogger().info("Tous les amis ont un skin en cache (fourni par le proxy), aucune requete Mojang necessaire.");
            return;
        }

        plugin.getLogger().info("Skin en cache absent pour " + needsLookup.size()
                + " ami(s), tentative via SkinsRestorer puis Mojang en arriere-plan...");
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            for (Map.Entry<Integer, FriendEntry> mapEntry : needsLookup) {
                int slot = mapEntry.getKey();
                FriendEntry entry = mapEntry.getValue();

                // 1) SkinsRestorer : fonctionne meme hors-ligne, meme pour un compte cracke
                // sans equivalent Mojang, des lors que ce pseudo a deja un skin enregistre
                // chez lui (ex : via /skin). Prioritaire car ne necessite aucune reconnexion.
                Optional<SkinsRestorerBridge.SkinData> srSkin =
                        SkinsRestorerBridge.lookup(entry.uuid(), entry.name(), plugin.getLogger());
                if (srSkin.isPresent()) {
                    PlayerProfile profile = Bukkit.createProfile(entry.uuid(), entry.name());
                    profile.setProperty(new ProfileProperty("textures", srSkin.get().value(), srSkin.get().signature()));
                    plugin.getLogger().info("Skin recupere via SkinsRestorer pour " + entry.name() + ".");
                    Bukkit.getScheduler().runTask(plugin, () -> applySkin(player, inventory, slot, profile));
                    continue;
                }

                // 2) A defaut, on retente via Mojang (uniquement utile pour un vrai compte premium,
                // ou pour un pseudo cracke qui correspondrait par coincidence a un compte premium).
                // Un UUID de version 4 est un vrai UUID Mojang : on cherche directement par UUID,
                // ce qui est fiable meme si le pseudo a change depuis. Un UUID de version 3 est
                // genere localement a partir du pseudo (joueur hors-ligne / cracke) : Mojang n'a
                // aucune donnee pour cet UUID, donc on cherche par pseudo a la place.
                boolean realMojangUuid = entry.uuid().version() == 4;
                PlayerProfile profile = realMojangUuid
                        ? Bukkit.createProfile(entry.uuid(), entry.name())
                        : Bukkit.createProfile(entry.name());

                boolean fetched;
                try {
                    // Appel bloquant (reseau) : recupere les proprietes de texture (skin) aupres de Mojang.
                    fetched = profile.complete(true);
                } catch (Exception e) {
                    // Mojang injoignable, timeout, rate-limit... on garde la tete par defaut pour cet ami.
                    plugin.getLogger().warning("Echec recuperation skin pour " + entry.name()
                            + " (" + entry.uuid() + ") : " + e.getClass().getSimpleName() + " - " + e.getMessage());
                    fetched = false;
                }

                if (!fetched) {
                    plugin.getLogger().info((realMojangUuid ? "UUID premium" : "UUID hors-ligne, recherche par pseudo")
                            + " : aucun skin trouve pour " + entry.name() + " (" + entry.uuid()
                            + "), tete par defaut conservee.");
                    continue;
                }

                if (profile.getTextures() == null || profile.getTextures().getSkin() == null) {
                    plugin.getLogger().warning("Profil complete mais aucune texture de skin trouvee pour "
                            + entry.name() + " (" + entry.uuid() + ").");
                }

                Bukkit.getScheduler().runTask(plugin, () -> applySkin(player, inventory, slot, profile));
            }
        });
    }

    private void applySkin(Player player, Inventory inventory, int slot, PlayerProfile profile) {
        if (!player.isOnline() || !player.getOpenInventory().getTopInventory().equals(inventory)) {
            plugin.getLogger().info("Skin recupere pour le slot " + slot + " mais menu deja ferme/change, mise a jour ignoree.");
            return;
        }
        ItemStack item = inventory.getItem(slot);
        if (item == null || item.getType() != Material.PLAYER_HEAD) {
            return;
        }
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta == null) {
            return;
        }
        meta.setPlayerProfile(profile);
        item.setItemMeta(meta);
        inventory.setItem(slot, item);
        plugin.getLogger().info("Skin applique avec succes pour le slot " + slot + " (" + profile.getName() + ").");
    }

    private ItemStack buildHead(FriendEntry entry) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta != null) {
            PlayerProfile profile = Bukkit.createProfile(entry.uuid(), entry.name());
            if (entry.hasCachedSkin()) {
                // Texture deja connue (capturee par le proxy a la connexion de cet ami) :
                // on l'applique directement, sans aucun appel reseau vers Mojang.
                String signature = entry.skinSignature() != null ? entry.skinSignature() : "";
                profile.setProperty(new ProfileProperty("textures", entry.skinValue(), signature));
            }
            meta.setPlayerProfile(profile);
            meta.setDisplayName((entry.online() ? ChatColor.GREEN : ChatColor.DARK_GRAY) + entry.name());

            List<String> lore = new ArrayList<>();
            lore.add(entry.online()
                    ? ChatColor.GREEN + "En ligne sur " + entry.server()
                    : ChatColor.GRAY + "Hors-ligne");
            lore.add("");
            if (entry.online()) {
                lore.add(ChatColor.YELLOW + "Clic gauche" + ChatColor.GRAY + " : rejoindre son serveur");
            } else {
                lore.add(ChatColor.DARK_GRAY + "Indisponible (hors-ligne)");
            }
            lore.add(ChatColor.YELLOW + "Clic droit" + ChatColor.GRAY + " : preparer un message");
            meta.setLore(lore);

            item.setItemMeta(meta);
        }
        return item;
    }
}
