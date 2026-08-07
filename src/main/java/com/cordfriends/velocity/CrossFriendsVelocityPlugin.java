package com.cordfriends.velocity;

import com.cordfriends.velocity.commands.FriendCommand;
import com.cordfriends.velocity.commands.MailCommand;
import com.cordfriends.velocity.commands.MsgCommand;
import com.cordfriends.velocity.commands.ReplyCommand;
import com.cordfriends.velocity.data.DataManager;
import com.cordfriends.velocity.gui.GuiBridgeListener;
import com.cordfriends.velocity.listeners.PlayerListener;
import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Point d'entree du plugin CordFriends sur Velocity. Portage direct de la
 * version BungeeCord : meme logique (amis / messages prives / courrier
 * hors-ligne / pont d'interface graphique avec le module Spigot), mais
 * s'appuie sur l'API Velocity (evenements @Subscribe, ProxyServer, Adventure
 * pour le texte, etc.) plutot que sur l'API BungeeCord.
 */
@Plugin(
        id = "cordfriends",
        name = "CordFriends",
        version = "1.1.2",
        description = "Systeme d'amis, messages prives et courrier hors-ligne inter-serveur pour Velocity",
        authors = {"CordFriends"}
)
public class CrossFriendsVelocityPlugin {

    /** Canal de plugin-messaging utilise pour communiquer avec le module Spigot (interface graphique). */
    public static final ChannelIdentifier CHANNEL = MinecraftChannelIdentifier.from("crossfriends:main");

    private static CrossFriendsVelocityPlugin instance;

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;

    private DataManager dataManager;
    private boolean requireFriendship = true;
    private boolean mailRequireFriendship = false;
    private boolean notifyOnJoin = true;

    @Inject
    public CrossFriendsVelocityPlugin(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        instance = this;

        try {
            Files.createDirectories(dataDirectory);
        } catch (IOException e) {
            logger.warn("Impossible de creer le dossier de donnees : {}", e.getMessage());
        }

        loadConfig();

        this.dataManager = new DataManager(this);

        server.getChannelRegistrar().register(CHANNEL);

        CommandManager commandManager = server.getCommandManager();
        commandManager.register(
                commandManager.metaBuilder("friend").aliases("f", "amis", "ami", "friends").plugin(this).build(),
                new FriendCommand(this));
        commandManager.register(
                commandManager.metaBuilder("msg").aliases("tell", "whisper", "w", "pm").plugin(this).build(),
                new MsgCommand(this));
        commandManager.register(
                commandManager.metaBuilder("r").aliases("reply").plugin(this).build(),
                new ReplyCommand(this));
        commandManager.register(
                commandManager.metaBuilder("mail").aliases("courrier").plugin(this).build(),
                new MailCommand(this));

        server.getEventManager().register(this, new PlayerListener(this));
        server.getEventManager().register(this, new GuiBridgeListener(this));

        // Sauvegarde periodique de securite, toutes les 5 minutes
        server.getScheduler().buildTask(this, () -> dataManager.saveAll())
                .delay(5, TimeUnit.MINUTES)
                .repeat(5, TimeUnit.MINUTES)
                .schedule();

        logger.info("CrossFriends actif : amis / messages prives / courrier / interface inter-serveur prets.");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (dataManager != null) {
            dataManager.saveAll();
        }
        logger.info("CrossFriends desactive, donnees sauvegardees.");
    }

    private void loadConfig() {
        try {
            File configFile = dataDirectory.resolve("config.yml").toFile();
            if (!configFile.exists()) {
                try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.yml")) {
                    if (in != null) {
                        Files.copy(in, configFile.toPath());
                    }
                }
            }
            if (configFile.exists()) {
                List<String> lines = Files.readAllLines(configFile.toPath(), StandardCharsets.UTF_8);
                requireFriendship = readBoolean(lines, "require-friendship", true);
                mailRequireFriendship = readBoolean(lines, "mail-require-friendship", false);
                notifyOnJoin = readBoolean(lines, "notify-on-join", true);
            }
        } catch (IOException e) {
            logger.warn("Impossible de charger config.yml, valeurs par defaut utilisees : {}", e.getMessage());
        }
    }

    /**
     * Lecture volontairement minimaliste d'une valeur booleenne "cle: true|false" dans un
     * fichier YAML tres simple (le fichier config.yml de ce plugin ne contient que des
     * booleens au premier niveau). Evite d'ajouter une dependance YAML supplementaire
     * (Velocity, contrairement a BungeeCord, ne fournit pas d'API de configuration integree).
     */
    private boolean readBoolean(List<String> lines, String key, boolean defaultValue) {
        Pattern pattern = Pattern.compile("^\\s*" + Pattern.quote(key) + "\\s*:\\s*(true|false)\\s*(#.*)?$",
                Pattern.CASE_INSENSITIVE);
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                return Boolean.parseBoolean(matcher.group(1));
            }
        }
        return defaultValue;
    }

    public static CrossFriendsVelocityPlugin getInstance() {
        return instance;
    }

    public ProxyServer getServer() {
        return server;
    }

    public Logger getLogger() {
        return logger;
    }

    public Path getDataDirectory() {
        return dataDirectory;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public boolean isRequireFriendship() {
        return requireFriendship;
    }

    public boolean isMailRequireFriendship() {
        return mailRequireFriendship;
    }

    public boolean isNotifyOnJoin() {
        return notifyOnJoin;
    }
}
