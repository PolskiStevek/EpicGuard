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

package me.xneox.epicguard.core.manager;

import com.google.common.net.InetAddresses;
import de.leonhard.storage.Json;
import me.xneox.epicguard.core.user.PendingUser;
import org.diorite.libs.org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * This class manages the stored data and some global cache.
 *
 * TODO: Other storage implementations than just JSON.
 */
@SuppressWarnings("UnstableApiUsage")
public class StorageManager {
    private final Json data;

    private final Collection<String> pingCache = new HashSet<>();
    private final Map<String, List<String>> accountMap;

    private final Collection<String> blacklist;
    private final Collection<String> whitelist;

    private final Collection<String> nameBlacklist;
    private final Collection<String> nameWhitelist;

    public StorageManager() {
        this.data = new Json("storage", "plugins/EpicGuard/data");

        this.accountMap = this.data.getOrSetDefault("account-data", new HashMap<>());

        this.blacklist = this.data.getOrSetDefault("blacklist", new HashSet<>());
        this.whitelist = this.data.getOrSetDefault("whitelist", new HashSet<>());

        this.nameBlacklist = this.data.getOrSetDefault("name-blacklist", new HashSet<>());
        this.nameWhitelist = this.data.getOrSetDefault("name-whitelist", new HashSet<>());
    }

    public void save() {
        this.data.set("blacklist", this.blacklist);
        this.data.set("whitelist", this.whitelist);

        this.data.set("name-blacklist", this.nameBlacklist);
        this.data.set("name-whitelist", this.nameWhitelist);

        this.data.set("account-data", this.accountMap);
    }

    /**
     * Retrieves a list of nicknames used by specified IP Address.
     */
    @Nonnull
    public List<String> getAccounts(@Nonnull PendingUser user) {
        Validate.notNull(user, "BotUser cannot be null!");
        return this.accountMap.getOrDefault(user.getAddress(), new ArrayList<>());
    }


    /**
     * If the user's address is not in the accountMap, it will be added.
     */
    public void updateAccounts(@Nonnull PendingUser user) {
        Validate.notNull(user, "BotUser cannot be null!");

        List<String> accounts = this.getAccounts(user);
        if (!accounts.contains(user.getNickname())) {
            accounts.add(user.getNickname());
        }

        this.accountMap.put(user.getAddress(), accounts);
    }

    /**
     * Searches for the last used address of the specified nickname.
     * Returns null if not found.
     */
    @Nullable
    public String findByNickname(String nickname) {
        return this.accountMap.entrySet().stream()
                .filter(entry -> entry.getValue().stream().anyMatch(nick -> nick.equalsIgnoreCase(nickname)))
                .findFirst()
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    /**
     * @param value The user's address or nickname.
     */
    public void blacklist(String value) {
        if (InetAddresses.isInetAddress(value)) {
            if (!this.blacklist.contains(value)) {
                this.blacklist.add(value);
            }
        } else {
            if (!this.nameBlacklist.contains(value)) {
                this.nameBlacklist.add(value);
            }
        }
    }

    /**
     * @param value The user's address or nickname.
     */
    public void whitelist(String value) {
        if (InetAddresses.isInetAddress(value)) {
            if (!this.whitelist.contains(value)) {
                this.whitelist.add(value);
            }
        } else {
            if (!this.nameWhitelist.contains(value)) {
                this.nameWhitelist.add(value);
            }
        }
    }

    /**
     * @param value The user's address or nickname.
     */
    public boolean isBlacklisted(String value) {
        if (InetAddresses.isInetAddress(value)) {
            return this.blacklist.contains(value);
        }
        return this.nameBlacklist.contains(value);
    }

    /**
     * @param value The user's address or nickname.
     */
    public boolean isWhitelisted(String value) {
        if (InetAddresses.isInetAddress(value)) {
            return this.whitelist.contains(value);
        }
        return this.nameWhitelist.contains(value);
    }

    /**
     * @param value The user's address or nickname.
     */
    public void removeFromBlacklist(String value) {
        if (InetAddresses.isInetAddress(value)) {
            this.blacklist.remove(value);
        } else {
            this.nameBlacklist.remove(value);
        }
    }

    /**
     * @param value The user's address or nickname.
     */
    public void removeFromWhitelist(String value) {
        if (InetAddresses.isInetAddress(value)) {
            this.whitelist.remove(value);
        } else {
            this.nameWhitelist.remove(value);
        }
    }

    // Yes, the same comment is repeated 6 TIMES. I had no other idea lol

    @Nonnull
    public Collection<String> getBlacklist() {
        return this.blacklist;
    }

    @Nonnull
    public Collection<String> getWhitelist() {
        return this.whitelist;
    }

    @Nonnull
    public Collection<String> getPingCache() {
        return this.pingCache;
    }
}
