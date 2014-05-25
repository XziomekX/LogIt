/*
 * CommandHelpLine.java
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

public final class CommandHelpLine
{
    private CommandHelpLine(String command, String descriptionLabel, String optionalParam)
    {
        this.command = command;
        this.descriptionLabel = descriptionLabel;
        this.optionalParam = optionalParam;
    }
    
    public String getCommand()
    {
        return command;
    }
    
    public String getDescriptionLabel()
    {
        return descriptionLabel;
    }
    
    public String getOptionalParam()
    {
        return optionalParam;
    }
    
    public boolean hasOptionalParam()
    {
        return optionalParam != null;
    }
    
    public static final class Builder
    {
        public Builder command(String command)
        {
            this.command = command;
            
            return this;
        }
        
        public Builder descriptionLabel(String descriptionLabel)
        {
            this.descriptionLabel = descriptionLabel;
            
            return this;
        }
        
        public Builder optionalParam(String optionalParam)
        {
            this.optionalParam = optionalParam;
            
            return this;
        }
        
        public CommandHelpLine build()
        {
            return new CommandHelpLine(command, descriptionLabel, optionalParam);
        }
        
        private String command;
        private String descriptionLabel;
        private String optionalParam;
    }
    
    private final String command;
    private final String descriptionLabel;
    private final String optionalParam;
}
