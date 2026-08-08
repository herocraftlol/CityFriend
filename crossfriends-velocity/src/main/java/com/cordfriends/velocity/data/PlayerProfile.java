package com.cordfriends.velocity.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Profil persistant d'un joueur : sa liste d'amis, ses demandes d'amis
 * (entrantes et sortantes) et sa boite de reception (courrier hors-ligne).
 * Un fichier JSON par joueur est conserve sur le disque par le DataManager.
 */
public class PlayerProfile {

    private UUID uuid;
    private String name;
    private Set<UUID> friends = new HashSet<>();
    private Set<UUID> incomingRequests = new HashSet<>();
    private Set<UUID> outgoingRequests = new HashSet<>();
    private Set<UUID> blocked = new HashSet<>();
    private List<MailMessage> mailbox = new ArrayList<>();

    /**
     * Derniere texture de skin connue pour ce joueur (propriete "textures" de son
     * GameProfile, valeur encodee en base64 + sa signature Mojang), capturee a chaque
     * connexion au proxy. Fonctionne aussi bien pour les comptes premium que crackes
     * (des lors qu'un systeme quelconque - Mojang ou un plugin de skins - a fourni
     * une texture au moment de la connexion), et evite de dependre de Mojang pour
     * afficher la tete du joueur dans le menu d'amis, y compris pour un ami hors-ligne.
     */
    private String skinValue;
    private String skinSignature;

    /** Constructeur vide requis par Gson pour la deserialisation. */
    public PlayerProfile() {
    }

    public PlayerProfile(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<UUID> getFriends() {
        return friends;
    }

    public Set<UUID> getIncomingRequests() {
        return incomingRequests;
    }

    public Set<UUID> getOutgoingRequests() {
        return outgoingRequests;
    }

    public Set<UUID> getBlocked() {
        return blocked;
    }

    public List<MailMessage> getMailbox() {
        return mailbox;
    }

    public String getSkinValue() {
        return skinValue;
    }

    public String getSkinSignature() {
        return skinSignature;
    }

    public void setSkin(String skinValue, String skinSignature) {
        this.skinValue = skinValue;
        this.skinSignature = skinSignature;
    }
}
