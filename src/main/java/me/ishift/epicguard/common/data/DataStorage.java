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

package me.ishift.epicguard.common.data;

import de.leonhard.storage.Config;
import de.leonhard.storage.Yaml;

import java.util.Collection;
import java.util.HashSet;

public abstract class DataStorage {
    public Config config;

    public Collection<String> rejoinData;
    public Collection<String> pingData;
    public Collection<String> blacklist;
    public Collection<String> whitelist;

    public DataStorage() {
        this.config = new Config("storage", "plugins/EpicGuard");
        this.blacklist = new HashSet<>();
        this.whitelist = new HashSet<>();
        this.rejoinData = new HashSet<>();
        this.pingData = new HashSet<>();
    }

    public void blacklist(String address) {
        this.blacklist.add(address);
    }

    public void whitelist(String address) {
        this.blacklist.remove(address);
        this.whitelist.add(address);
    }

    public Collection<String> getPingData() {
        return pingData;
    }

    public Collection<String> getRejoinData() {
        return rejoinData;
    }

    public Collection<String> getBlacklist() {
        return blacklist;
    }

    public Collection<String> getWhitelist() {
        return whitelist;
    }

    public abstract void load();

    public abstract void save();
}
