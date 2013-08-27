/*
 * ProfileViewWizard.java
 *
 * Copyright (C) 2012-2013 LucasEasedUp
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
package io.github.lucaseasedup.logit.command.wizard;

import static io.github.lucaseasedup.logit.LogItPlugin.getMessage;
import io.github.lucaseasedup.logit.profile.field.Field;
import java.util.List;
import org.bukkit.command.CommandSender;

public final class ProfileViewWizard extends Wizard
{
    public ProfileViewWizard(CommandSender sender, String player)
    {
        super(sender, new String[0], Step.VIEW);
        
        this.player = player;
    }
    
    @Override
    protected void onCreate()
    {
        List<Field> fields = getCore().getProfileManager().getDefinedFields();
        
        sendMessage("");
        sendMessage(getMessage("PROFILE_HEADER")
                .replace("%player%", player));
        sendMessage(getMessage("ORANGE_HORIZONTAL_LINE"));
        
        if (!fields.isEmpty())
        {
            for (Field field : fields)
            {
                Object value = getCore().getProfileManager()
                        .getProfileObject(player, field.getName());
                
                if (value == null)
                {
                    value = "";
                }
                
                sendMessage(getMessage("PROFILE_VIEW_FIELD")
                        .replace("%field%", field.getName())
                        .replace("%value%", value.toString()));
            }
        }
        else
        {
            sendMessage(getMessage("PROFILE_NO_FIELDS"));
        }
        
        cancelWizard();
    }
    
    @Override
    protected void onMessage(String message)
    {
    }
    
    public static enum Step
    {
        VIEW
    }
    
    private final String player;
}
