/*
 * EpicGuard is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EpicGuard is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package me.xneox.epicguard.bungee;

import me.xneox.epicguard.bungee.command.BungeeCommandExecutor;
import me.xneox.epicguard.bungee.listener.*;
import me.xneox.epicguard.core.EpicGuard;
import me.xneox.epicguard.core.Platform;
import me.xneox.epicguard.core.command.CommandExecutor;
import me.xneox.epicguard.core.logging.GuardLogger;
import me.xneox.epicguard.core.logging.impl.JavaLogger;
import me.xneox.epicguard.core.logging.impl.SLF4JLogger;
import me.xneox.epicguard.core.user.User;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import org.bstats.bungeecord.Metrics;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class EpicGuardBungee extends Plugin implements Platform {
    private EpicGuard epicGuard;
    private GuardLogger logger;

    @Override
    public void onEnable() {
        // Waterfall uses Log4J so we can utilize it for better compatibility.
        // This will fall back to the java logger, for example when using BungeeCord.
        try {
            Class.forName("io.github.waterfallmc.waterfall.log4j.WaterfallLogger");
            this.logger = new SLF4JLogger(this.getSLF4JLogger());
        } catch (ClassNotFoundException e) {
            this.logger = new JavaLogger(this.getLogger());
        }

        this.epicGuard = new EpicGuard(this);

        PluginManager pm = this.getProxy().getPluginManager();
        pm.registerListener(this, new PreLoginListener(this.epicGuard));
        pm.registerListener(this, new DisconnectListener(this.epicGuard));
        pm.registerListener(this, new PostLoginListener(this.epicGuard));
        pm.registerListener(this, new ServerPingListener(this.epicGuard));
        pm.registerListener(this, new PlayerSettingsListener(this.epicGuard));

        pm.registerCommand(this, new BungeeCommandExecutor(new CommandExecutor(this.epicGuard)));

        new Metrics(this, 5956);
    }

    @Override
    public void onDisable() {
        this.epicGuard.shutdown();
    }

    @Override
    public @NotNull GuardLogger logger() {
        return this.logger;
    }

    @Override
    public void sendActionBar(@NotNull String message, @NotNull User user) {
        ProxyServer.getInstance().getPlayer(user.uuid()).sendMessage(ChatMessageType.ACTION_BAR, BungeeUtils.createComponent(message));
    }

    @Override
    public void disconnectUser(@NotNull User user, @NotNull String message) {
        ProxyServer.getInstance().getPlayer(user.uuid()).disconnect(BungeeUtils.createComponent(message));
    }

    @Override
    public String version() {
        return this.getDescription().getVersion();
    }

    @Override
    public void runTaskLater(@NotNull Runnable task, long seconds) {
        this.getProxy().getScheduler().schedule(this, task, seconds, TimeUnit.SECONDS);
    }

    @Override
    public void runTaskRepeating(@NotNull Runnable task, long seconds) {
        this.getProxy().getScheduler().schedule(this, task, seconds, seconds, TimeUnit.SECONDS);
    }
}
