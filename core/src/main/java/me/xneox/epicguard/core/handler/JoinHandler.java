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

package me.xneox.epicguard.core.handler;

import me.xneox.epicguard.core.EpicGuard;
import me.xneox.epicguard.core.check.WhitelistMode;
import me.xneox.epicguard.core.user.User;
import me.xneox.epicguard.core.util.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Handler for the PlayerJoin or PostLogin listeners
 * Used for the auto-whitelist feature, and for SettingsCheck.
 */
public class JoinHandler {
    private final EpicGuard epicGuard;

    public JoinHandler(EpicGuard epicGuard) {
        this.epicGuard = epicGuard;
    }

    /**
     * Handling the player who just have joined to the server.
     *
     * @param uuid UUID of the online player.
     * @param address Address of the online player.
     * @param nickname Nickname of the online player.
     */
    public void handle(@NotNull UUID uuid, @NotNull String address, @NotNull String nickname) {
        Validate.notNull(uuid, "UUID cannot be null!");
        Validate.notNull(address, "Address cannot be null!");

        User user = this.epicGuard.userManager().getOrCreate(uuid);

        // Schedule a delayed task to whitelist the player.
        WhitelistMode mode = WhitelistMode.valueOf(this.epicGuard.config().autoWhitelist().mode());
        if (mode != WhitelistMode.DISABLED) {
            this.epicGuard.platform().runTaskLater(() -> {
                if (user != null) {
                    if ((mode == WhitelistMode.MIXED || mode == WhitelistMode.ADDRESS) && !this.epicGuard.storageManager().isWhitelisted(address)) {
                        this.epicGuard.storageManager().whitelistPut(address);
                    }

                    if ((mode == WhitelistMode.MIXED || mode == WhitelistMode.NICKNAME) && !this.epicGuard.storageManager().isWhitelisted(nickname)) {
                        this.epicGuard.storageManager().whitelistPut(nickname);
                    }
                }
            }, this.epicGuard.config().autoWhitelist().timeOnline());
        }

        // Schedule a delayed task to check if the player has sent the Settings packet.
        if (this.epicGuard.config().settingsCheck().enabled()) {
            this.epicGuard.platform().runTaskLater(() -> {
                if (user != null && !user.settingsChanged()) {
                    this.epicGuard.platform().disconnectUser(user, StringUtils.buildMultilineString(this.epicGuard.messages().disconnect().settingsPacket()));
                }
            }, this.epicGuard.config().settingsCheck().delay());
        }
    }
}
