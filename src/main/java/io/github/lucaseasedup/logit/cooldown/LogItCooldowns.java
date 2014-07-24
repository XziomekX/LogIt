/*
 * LogItCooldowns.java
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
package io.github.lucaseasedup.logit.cooldown;

import io.github.lucaseasedup.logit.LogItCore;
import io.github.lucaseasedup.logit.TimeUnit;
import org.bukkit.entity.Player;

public final class LogItCooldowns
{
    private LogItCooldowns()
    {
    }
    
    public static void activate(Player player, Cooldown cooldown)
    {
        if (player == null || cooldown == null)
            throw new IllegalArgumentException();
        
        LogItCore logItCore = LogItCore.getInstance();
        
        if (logItCore == null)
            throw new IllegalStateException();
        
        long cooldownTime;
        
        if (cooldown == REGISTER)
        {
            cooldownTime = logItCore.getConfig("config.yml")
                    .getTime("cooldowns.register", TimeUnit.MILLISECONDS);
        }
        else if (cooldown == UNREGISTER)
        {
            cooldownTime = logItCore.getConfig("config.yml")
                    .getTime("cooldowns.unregister", TimeUnit.MILLISECONDS);
        }
        else if (cooldown == CHANGEPASS)
        {
            cooldownTime = logItCore.getConfig("config.yml")
                    .getTime("cooldowns.changepass", TimeUnit.MILLISECONDS);
        }
        else if (cooldown == CHANGEEMAIL)
        {
            cooldownTime = logItCore.getConfig("config.yml")
                    .getTime("cooldowns.changeemail", TimeUnit.MILLISECONDS);
        }
        else if (cooldown == RECOVERPASS)
        {
            cooldownTime = logItCore.getConfig("config.yml")
                    .getTime("cooldowns.recoverpass", TimeUnit.MILLISECONDS);
        }
        else
        {
            throw new IllegalArgumentException("Unknown cooldown type: " + cooldown.getName());
        }
        
        logItCore.getCooldownManager().activateCooldown(player,
                LogItCooldowns.CHANGEPASS, cooldownTime);
    }
    
    public static final Cooldown REGISTER = new Cooldown("logit.register");
    public static final Cooldown UNREGISTER = new Cooldown("logit.unregister");
    public static final Cooldown CHANGEPASS = new Cooldown("logit.changepass");
    public static final Cooldown CHANGEEMAIL = new Cooldown("logit.changeemail");
    public static final Cooldown RECOVERPASS = new Cooldown("logit.recoverpass");
}
