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

package me.xneox.epicguard.core.check.impl;

import me.xneox.epicguard.core.EpicGuard;
import me.xneox.epicguard.core.check.Check;
import me.xneox.epicguard.core.check.CheckMode;
import me.xneox.epicguard.core.user.PendingUser;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * This will check if the user is using a VPN or a proxy.
 */
public class ProxyCheck extends Check {
    public ProxyCheck(EpicGuard epicGuard) {
        super(epicGuard);
    }

    @Override
    public boolean handle(@NotNull PendingUser user) {
        CheckMode mode = CheckMode.valueOf(this.epicGuard.config().proxyCheck().checkMode());
        return this.evaluate(mode, this.epicGuard.proxyManager().isProxy(user.address()));
    }

    @Override
    public @NotNull List<String> kickMessage() {
        return this.epicGuard.messages().disconnect().proxy();
    }
}
