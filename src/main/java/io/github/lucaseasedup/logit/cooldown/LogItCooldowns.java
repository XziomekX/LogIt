package io.github.lucaseasedup.logit.cooldown;

import io.github.lucaseasedup.logit.LogItCore;
import io.github.lucaseasedup.logit.config.TimeUnit;
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
