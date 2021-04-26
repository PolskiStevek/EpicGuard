package me.xneox.epicguard.core.command;

import java.util.UUID;

/**
 * This represents a sender of the command /epicguard, who will be sent back the output
 * message based on the command he used.
 *
 * It can either be the console, or the online player. Platform needs to specify this.
 *
 * @param <C> Type of the command sender.
 */
public abstract class Sender<C> {
    protected final C sender;

    protected Sender(C sender) {
        this.sender = sender;
    }

    public abstract void sendMessage(String message);

    public abstract boolean isPlayer();

    public abstract UUID getUUID();
}
