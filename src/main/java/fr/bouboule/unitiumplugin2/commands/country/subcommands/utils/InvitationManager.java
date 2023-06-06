package fr.bouboule.unitiumplugin2.commands.country.subcommands.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InvitationManager {
    private final Map<UUID, UUID> pendingInvitations;

    public InvitationManager() {
        this.pendingInvitations = new HashMap<>();
    }

    public void addInvitation(UUID invitedUUID, UUID inviterUUID) {
        pendingInvitations.put(invitedUUID, inviterUUID);
    }

    public boolean hasPendingInvitation(UUID invitedUUID) {
        return pendingInvitations.containsKey(invitedUUID);
    }

    public void removeInvitation(UUID invitedUUID) {
        pendingInvitations.remove(invitedUUID);
    }
    public UUID getInviterUUID(UUID invitedUUID) {
        return pendingInvitations.get(invitedUUID);
    }
}