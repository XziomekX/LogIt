/*
 * QuitMessageGenerator.java
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
package io.github.lucaseasedup.logit.util;

import static io.github.lucaseasedup.logit.util.MessageHelper.t;
import io.github.lucaseasedup.logit.LogItCore;
import org.bukkit.entity.Player;

public final class QuitMessageGenerator
{
    private QuitMessageGenerator()
    {
    }
    
    public static String generate(Player player)
    {
        LogItCore core = LogItCore.getInstance();
        
        assert core != null;
        
        String message;
        
        if (core.getConfig("config.yml").getBoolean("messages.beautify"))
        {
            message = t("quit.beautified");
        }
        else
        {
            message = t("quit.native");
        }
        
        return message.replace("{0}", player.getName());
    }
}
