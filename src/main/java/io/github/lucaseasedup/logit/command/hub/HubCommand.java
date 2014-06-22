/*
 * HubCommand.java
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

import io.github.lucaseasedup.logit.LogItCoreObject;
import io.github.lucaseasedup.logit.command.CommandHelpLine;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.CommandSender;

public abstract class HubCommand extends LogItCoreObject
{
    public HubCommand(String subcommand,
                      String[] params,
                      String permission,
                      boolean playerOnly,
                      boolean requiresRunningCore,
                      CommandHelpLine helpLine)
    {
        if (subcommand == null || params == null || permission == null || helpLine == null)
            throw new IllegalArgumentException();
        
        this.subcommand = subcommand;
        this.params = Arrays.asList(params);
        this.permission = permission;
        this.playerOnly = playerOnly;
        this.requiresRunningCore = requiresRunningCore;
        this.helpLine = helpLine;
    }
    
    public abstract void execute(CommandSender sender, String[] args);
    
    @SuppressWarnings("unused")
    public List<String> complete(CommandSender sender, String[] args)
    {
        return null;
    }
    
    public String getSubcommand()
    {
        return subcommand;
    }
    
    public List<String> getParams()
    {
        return Collections.unmodifiableList(params);
    }
    
    public String getPermission()
    {
        return permission;
    }
    
    public boolean isPlayerOnly()
    {
        return playerOnly;
    }
    
    public boolean requiresRunningCore()
    {
        return requiresRunningCore;
    }
    
    public CommandHelpLine getHelpLine()
    {
        return helpLine;
    }
    
    private final String subcommand;
    private final List<String> params;
    private final String permission;
    private final boolean playerOnly;
    private final boolean requiresRunningCore;
    private final CommandHelpLine helpLine;
}
