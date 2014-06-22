/*
 * HubCommands.java
 *
 * Copyright (C) 2012-2014 LucasEasedUp
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.lucaseasedup.logit.command.hub;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

public final class HubCommands
{
    private HubCommands()
    {
    }
    
    public static Iterator<HubCommand> iterator()
    {
        return new Iterator<HubCommand>()
        {
            @Override
            public boolean hasNext()
            {
                return it.hasNext();
            }
            
            @Override
            public HubCommand next()
            {
                return it.next();
            }
            
            @Override
            public void remove()
            {
                throw new UnsupportedOperationException();
            }
            
            private final Iterator<HubCommand> it = hubCommands.iterator();
        };
    }
    
    private static final Set<HubCommand> hubCommands = new LinkedHashSet<>();
    
    static
    {
        hubCommands.add(new HelpHubCommand());
        hubCommands.add(new VersionHubCommand());
        hubCommands.add(new StartHubCommand());
        hubCommands.add(new StopHubCommand());
        hubCommands.add(new ReloadHubCommand());
        hubCommands.add(new BackupForceHubCommand());
        hubCommands.add(new BackupRestoreHubCommand());
        hubCommands.add(new BackupRestoreFileHubCommand());
        hubCommands.add(new BackupRestoreTimeHubCommand());
        hubCommands.add(new BackupRemoveHubCommand());
        hubCommands.add(new GotowrHubCommand());
        hubCommands.add(new GlobalpassHubCommand());
        hubCommands.add(new AccountStatusHubCommand());
        hubCommands.add(new AccountInfoHubCommand());
        hubCommands.add(new IpcountHubCommand());
        hubCommands.add(new ConfigGetHubCommand());
        hubCommands.add(new ConfigSetHubCommand());
        hubCommands.add(new ConfigListHubCommand());
        hubCommands.add(new ConfigReloadHubCommand());
        hubCommands.add(new ConvertHubCommand());
        hubCommands.add(new StatsHubCommand());
        hubCommands.add(new ImportAuthMeHubCommand());
    }
}
