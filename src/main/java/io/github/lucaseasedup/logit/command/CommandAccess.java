/*
 * CommandAccess.java
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
package io.github.lucaseasedup.logit.command;

public final class CommandAccess
{
    private CommandAccess()
    {
    }
    
    public String getPermission()
    {
        return permission;
    }
    
    public boolean isPlayerOnly()
    {
        return playerOnly;
    }
    
    public boolean isRunningCoreRequired()
    {
        return runningCoreRequired;
    }
    
    public static final class Builder
    {
        public Builder permission(String permission)
        {
            if (permission == null)
                throw new IllegalArgumentException();
            
            this.permission = permission;
            
            return this;
        }
        
        public Builder playerOnly(boolean playerOnly)
        {
            this.playerOnly = playerOnly;
            
            return this;
        }
        
        public Builder runningCoreRequired(boolean runningCoreRequired)
        {
            this.runningCoreRequired = runningCoreRequired;
            
            return this;
        }
        
        public CommandAccess build()
        {
            CommandAccess commandAccess = new CommandAccess();
            
            commandAccess.permission = permission;
            commandAccess.playerOnly = playerOnly;
            commandAccess.runningCoreRequired = runningCoreRequired;
            
            return commandAccess;
        }
        
        private String permission;
        private boolean playerOnly;
        private boolean runningCoreRequired;
    }
    
    private String permission;
    private boolean playerOnly;
    private boolean runningCoreRequired;
}
